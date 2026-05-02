package shblock.interactivecorporea.common.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.INBTSerializable;
import shblock.interactivecorporea.ModConfig;
import shblock.interactivecorporea.client.util.RenderTick;
import shblock.interactivecorporea.common.corporea.CorporeaUtil;
import shblock.interactivecorporea.common.item.HaloModule;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import shblock.interactivecorporea.common.network.ModPacketHandler;
import shblock.interactivecorporea.common.network.SPacketPlayQuantizationEffect;
import shblock.interactivecorporea.common.requestinghalo.HaloAttractServerHandler;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.NBTTagHelper;
import shblock.interactivecorporea.common.util.StackHelper;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.corporea.CorporeaResult;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.common.block.block_entity.corporea.BaseCorporeaBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.impl.corporea.CorporeaItemStackMatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileItemQuantizationDevice extends BaseCorporeaBlockEntity implements ManaReceiver {
  private int mana;
  private LazyOptional<ManaReceiver> manaReceiverCapability = LazyOptional.of(() -> this);

  private final List<Sender> senders = new ArrayList<>();

  public TileItemQuantizationDevice(BlockPos pos, BlockState state) {
    super(ModTiles.itemQuantizationDevice, pos, state);
  }

  public int getManaCost(int itemAmount) {
    return getExtractManaCost(itemAmount);
  }

  public int getExtractManaCost(int itemAmount) {
    return itemAmount * ModConfig.COMMON.quantizationExtractConsumption.get();
  }

  public int getInsertManaCost(int itemAmount) {
    return itemAmount * ModConfig.COMMON.quantizationInsertConsumption.get();
  }

  public int requestItem(ItemStack stack, Vector3 requestPos, Vector3 normal, ServerPlayer player, ItemStack halo) {
    if (level == null) return 0;
    if (getSpark() == null) return 0;
    int manaCost = getManaCost(stack.getCount());
    if (manaCost > mana) {
      return 0;
    }
    CorporeaResult result = CorporeaUtil.requestItemNoIntercept(new CorporeaItemStackMatcher(stack, true), stack.getCount(), getSpark(), player, true);
    List<ItemStack> stacks = result.stacks();
    if (stacks.isEmpty()) {
      return 0;
    }
    ItemStack resultStack = stacks.get(0);
    for (int i = 1; i < stacks.size(); i++) {
      if (StackHelper.equalItemAndTag(resultStack, stacks.get(i))) {
        resultStack.grow(stacks.get(i).getCount());
      } else {
        level.addFreshEntity(new ItemEntity(level, getBlockPos().getX() + .5F, getBlockPos().getY() + 1F, getBlockPos().getZ() + .5F, stacks.get(i)));
      }
    }
    Vector3 fromPos = new Vector3(getBlockPos().getX() + .5, getBlockPos().getY() + .5, getBlockPos().getZ() + .5);
    senders.add(new Sender(resultStack, level, fromPos, requestPos, normal, player, ItemRequestingHalo.isModuleInstalled(halo, HaloModule.MAGNATE)));
    setChanged();
    VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
    consumeMana(getManaCost(resultStack.getCount()));
    return resultStack.getCount();
  }

  public ItemStack insertItem(ItemStack stack) {
    if (level == null || stack.isEmpty()) return stack;
    if (getSpark() == null) return stack;

    int perItemCost = ModConfig.COMMON.quantizationInsertConsumption.get();
    int maxByMana = perItemCost <= 0 ? stack.getCount() : Math.min(stack.getCount(), mana / perItemCost);
    if (maxByMana <= 0) {
      return stack;
    }

    ItemStack toInsert = stack.copy();
    toInsert.setCount(maxByMana);
    ItemStack insertRemainder = CorporeaUtil.insertItem(getSpark(), toInsert);
    int inserted = maxByMana - insertRemainder.getCount();
    if (inserted <= 0) {
      return stack;
    }

    consumeMana(getInsertManaCost(inserted));
    playInsertEffect(stack.copyWithCount(inserted));

    ItemStack result = stack.copy();
    result.setCount(stack.getCount() - inserted);
    return result;
  }

  private void playInsertEffect(ItemStack stack) {
    if (!(level instanceof ServerLevel serverLevel)) return;
    int speed = ModConfig.COMMON.quantizationAnimationSpeed.get();
    Vector3 pos = new Vector3(getBlockPos().getX() + .5, getBlockPos().getY() + .5, getBlockPos().getZ() + .5);
    ModPacketHandler.sendToPlayersInWorld(serverLevel, new SPacketPlayQuantizationEffect(stack, speed, pos, 1));
  }

  public static void serverTick(Level level, BlockPos worldPosition, BlockState state, TileItemQuantizationDevice self) {
    if (self.level == null) return;
    if (!level.isClientSide) {
      boolean dirty = false;
      for (int i = self.senders.size() - 1; i >= 0; i--) {
        if (self.senders.get(i).tick()) {
          self.senders.remove(i);
        }
        dirty = true;
      }
      if (dirty) {
        self.setChanged();
        VanillaPacketDispatcher.dispatchTEToNearbyPlayers(self);
      }
    }
  }

  @Override
  public void readPacketNBT(CompoundTag cmp) {
    mana = cmp.contains("mana") ? cmp.getInt("mana") : 0;

    senders.clear();
    if (cmp.contains("senders", Tag.TAG_LIST)) {
      ListTag listNBT = cmp.getList("senders", Tag.TAG_COMPOUND);
      for (Tag nbt : listNBT) {
        Sender sender = new Sender();
        sender.deserializeNBT((CompoundTag) nbt);
        senders.add(sender);
      }
    }
  }

  @Override
  public void writePacketNBT(CompoundTag cmp) {
    cmp.putInt("mana", mana);

    ListTag listNBT = new ListTag();
    for (Sender sender : senders) {
      listNBT.add(sender.serializeNBT());
    }
    cmp.put("senders", listNBT);
  }

  public double getLightRelayRenderScale() {
    double scale = 0;
    for (Sender sender : this.senders) {
      double s = sender.getLightRelayRenderScale();
      if (scale < s)
        scale = s;
    }
    return scale;
  }

  @Override
  public boolean isFull() {
    return mana >= getManaCapacity();
  }

  @Override
  public void receiveMana(int receive) {
    mana += receive;
    if (mana >= getManaCapacity()) {
      mana = getManaCapacity();
    }
    setChanged();
  }

  @Override
  public boolean canReceiveManaFromBursts() { return true; }

  @Override
  public Level getManaReceiverLevel() {
    return level;
  }

  @Override
  public BlockPos getManaReceiverPos() {
    return getBlockPos();
  }

  @Override
  public int getCurrentMana() {
    return mana;
  }

  public int getManaCapacity() {
    return ModConfig.COMMON.quantizationDeviceManaCapacity.get();
  }

  public int getComparatorLevel() {
    return ManaPoolBlockEntity.calculateComparatorLevel(mana, getManaCapacity());
  }

  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
    if (cap == BotaniaForgeCapabilities.MANA_RECEIVER) {
      return BotaniaForgeCapabilities.MANA_RECEIVER.orEmpty(cap, manaReceiverCapability);
    }
    return super.getCapability(cap, side);
  }

  @Override
  public void invalidateCaps() {
    super.invalidateCaps();
    manaReceiverCapability.invalidate();
  }

  @Override
  public void reviveCaps() {
    super.reviveCaps();
    manaReceiverCapability = LazyOptional.of(() -> this);
  }

  public boolean consumeMana(int consume) {
    if (consume <= mana) {
      mana -= consume;
      setChanged();
      return true;
    }
    return false;
  }

  private static class Sender implements INBTSerializable<CompoundTag> {
    private int type; // 0: spawn item entity, 1: crafting slot
    private ItemStack stack;
    private Level world;
    private Vector3 fromPos;
    private Vector3 pos;
    private Vector3 normal;
    private int time = ModConfig.COMMON.quantizationAnimationSpeed.get() * 2;
    private Player player;
    private boolean shouldAttract;
    private CISlotPointer haloSlot;
    private int slot;

    public Sender() { } // for deserializeNBT

    public Sender(int type, ItemStack stack, Level world, Vector3 fromPos, Vector3 pos, Vector3 normal, @Nullable Player player, boolean shouldAttract, @Nullable CISlotPointer haloSlot, int slot) {
      this.type = type;
      this.stack = stack.copy();
      this.world = world;
      this.fromPos = fromPos;
      this.pos = pos;
      this.normal = normal;
      this.player = player;
      this.shouldAttract = shouldAttract;
      this.haloSlot = haloSlot;
      this.slot = slot;
    }

    /**
     * For spawning item entity
     */
    public Sender(ItemStack stack, Level world, Vector3 fromPos, Vector3 pos, Vector3 normal, @Nullable Player player, boolean shouldAttract) {
      this(0, stack, world, fromPos, pos, normal, player, shouldAttract, null, -1);
    }

    /**
     * For input to crafting slot
     * @param player used when input failed and spawns item entity
     * @param shouldAttract used when input failed and spawns item entity
     */
    public Sender(ItemStack stack, Level world, Vector3 fromPos, Vector3 pos, @Nullable Player player, boolean shouldAttract, @Nullable CISlotPointer haloSlot, int slot) {
      this(1, stack, world, fromPos, pos, new Vector3(0, 1, 0), player, shouldAttract, haloSlot, slot);
    }

    /**
     * @return if this sender should be removed (item entity has been summoned)
     */
    public boolean tick() {
      int spd = ModConfig.COMMON.quantizationAnimationSpeed.get();
      if (!world.isClientSide) {
        if (time == spd * 2) { // first tick
          ModPacketHandler.sendToPlayersInWorld((ServerLevel) world, new SPacketPlayQuantizationEffect(stack, spd, fromPos, 1));
        } else if (time == spd) {
          ModPacketHandler.sendToPlayersInWorld((ServerLevel) world, new SPacketPlayQuantizationEffect(stack, spd, pos, normal, 1));
        } else if (time == 0) {
          onComplete();
        }
        time--;
      }
      return time < 0;
    }

    private double getLightRelayRenderScale() {
      return 1 - (Math.cos(Math.max(time - 10D - RenderTick.pt, 0D) / 10D * Math.PI) + 1) / 2;
    }

    @Override
    public CompoundTag serializeNBT() {
      CompoundTag nbt = new CompoundTag();
      nbt.putInt("type", type);
      nbt.put("stack", stack.save(new CompoundTag()));
      NBTTagHelper.putWorld(nbt, "world", world);
      nbt.put("fromPos", NBTTagHelper.putVector3(fromPos));
      nbt.put("pos", NBTTagHelper.putVector3(pos));
      nbt.put("normal", NBTTagHelper.putVector3(normal));
      nbt.putInt("time", time);
      if (player != null) {
        nbt.putUUID("player", player.getUUID());
      }
      if (type == 1) {
        nbt.put("haloSlot", NBTTagHelper.putCISlot(haloSlot));
        nbt.putInt("craftingSlot", slot);
      }

      return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
      this.type = nbt.getInt("type");
      this.stack = ItemStack.of(nbt.getCompound("stack"));
      this.world = NBTTagHelper.getWorld(nbt, "world");
      this.fromPos = NBTTagHelper.getVector3(nbt.getCompound("fromPos"));
      this.pos = NBTTagHelper.getVector3(nbt.getCompound("pos"));
      this.normal = NBTTagHelper.getVector3(nbt.getCompound("normal"));
      this.time = nbt.getInt("time");
      if (nbt.hasUUID("player")) {
        player = world.getPlayerByUUID(nbt.getUUID("player"));
      }
      if (type == 1) {
        haloSlot = NBTTagHelper.getCISlot(nbt.getCompound("haloSlot"));
        slot = nbt.getInt("craftingSlot");
      }
    }

    private void onComplete() {
      switch (type) {
        case 0: // spawn item entity
          spawnItemEntity();
          break;
        case 1: // to crafting slot
          if (player == null || haloSlot == null || slot == -1)
            spawnItemEntity();

          ItemStack halo = haloSlot.getStack(player);
          if (halo.isEmpty())
            spawnItemEntity();

          ItemStack oldStack = ItemRequestingHalo.getStackInCraftingSlot(halo, slot);
          if (StackHelper.equalItemAndTag(oldStack, stack)) {
            oldStack.grow(stack.getCount());
            ItemRequestingHalo.setStackInCraftingSlot(halo, slot, oldStack);
          } else {
            spawnItemEntity();
          }
          break;
      }
    }

    private void spawnItemEntity() {
      while (stack.getCount() > 0) {
        int cnt = Math.min(stack.getCount(), stack.getMaxStackSize());
        ItemStack spawnStack = stack.copy();
        spawnStack.setCount(cnt);
        stack.shrink(cnt);
        ItemEntity entity = new ItemEntity(world, pos.x, pos.y, pos.z, spawnStack);
        entity.setDeltaMovement(0, 0, 0);
        world.addFreshEntity(entity);

        if (shouldAttract && (player != null)) {
          HaloAttractServerHandler.addToAttractedItems(player, entity);
        }
      }
    }
  }
}
