package shblock.interactivecorporea.common.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.client.renderer.item.ISTERRequestingHalo;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;
import shblock.interactivecorporea.client.util.KeyboardHelper;
import shblock.interactivecorporea.common.block.BlockItemQuantizationDevice;
import shblock.interactivecorporea.common.corporea.CorporeaUtil;
import shblock.interactivecorporea.common.util.NBTTagHelper;
import shblock.interactivecorporea.common.util.StackHelper;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.core.helper.ItemNBTHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ItemRequestingHalo extends Item {
  private static final int BASE_NETWORK_RANGE = 10;
  private static final String PREFIX_INDEX_POS = "bound_position";
  private static final String PREFIX_SENDER_POS = "sender_position";
  private static final String PREFIX_MODULES = "modules";
  private static final String PREFIX_CRAFTING_SLOT_ITEMS = "crafting_slot_items";
  private static final String PREFIX_INTERFACE_STYLE = "interface_style";
  private static final String PREFIX_HALO_TINT = "halo_tint"; // packed 0xRRGGBB, default 0xFFFFFF
  private static final String PREFIX_HALO_TINT_DYE = "halo_tint_dye"; // DyeColor.getId(), -1 = default

  public ItemRequestingHalo() {
    super(new Properties().stacksTo(1));
  }

  @Override
  public void initializeClient(Consumer<IClientItemExtensions> consumer) {
    consumer.accept(new IClientItemExtensions() {
      private final BlockEntityWithoutLevelRenderer renderer = new ISTERRequestingHalo();

      @Override
      public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return renderer;
      }
    });
  }

  @Override
  public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean isSelected) {
    super.inventoryTick(stack, world, entity, slot, isSelected);
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (world.isClientSide) {
      if (hand == InteractionHand.MAIN_HAND) {
        if (RequestingHaloInterfaceHandler.isInterfaceOpened()) {
          RequestingHaloInterfaceHandler.closeInterface();
        } else {
          RequestingHaloInterfaceHandler.tryOpen(player);
        }
      }
    }
    return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
  }

  @Nullable
  public static GlobalPos getBoundIndexPosition(ItemStack stack) {
    return NBTTagHelper.getGlobalPos(ItemNBTHelper.get(stack, PREFIX_INDEX_POS));
  }

  @Nullable
  public static GlobalPos getBoundSenderPosition(ItemStack stack) {
    return NBTTagHelper.getGlobalPos(ItemNBTHelper.get(stack, PREFIX_SENDER_POS));
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    Level world = context.getLevel();
    BlockPos pos = context.getClickedPos();
    Block block = world.getBlockState(pos).getBlock();
    Player player = context.getPlayer();
    if (player == null)
      return InteractionResult.PASS;
    if (player.isShiftKeyDown()) {
      String prefix;
      if (block == BotaniaBlocks.corporeaIndex) {
        prefix = PREFIX_INDEX_POS;
      } else if (block instanceof BlockItemQuantizationDevice) {
        prefix = PREFIX_SENDER_POS;
      } else {
        return InteractionResult.CONSUME;
      }

      GlobalPos globalPos = GlobalPos.of(world.dimension(), pos);
      ItemNBTHelper.set(context.getItemInHand(), prefix, NBTTagHelper.putGlobalPos(globalPos));
      return InteractionResult.sidedSuccess(world.isClientSide);
    }
    return InteractionResult.PASS;
  }

  /**
   * Try to install a module
   * @return if the installation was successful
   */
  public static boolean installModule(ItemStack stack, HaloModule module) {
    if (!canInstallModule(stack, module)) return false;
    int mask = ItemNBTHelper.getInt(stack, PREFIX_MODULES, 0);
    ItemNBTHelper.setInt(stack, PREFIX_MODULES, mask | module.bitMask);
    return true;
  }

  public static boolean canInstallModule(ItemStack stack, HaloModule module) {
    int mask = ItemNBTHelper.getInt(stack, PREFIX_MODULES, 0);
    if (module.containsThis(mask)) return false;
    return !module.isRangeModule() || getInstalledRangeModule(stack) == null;
  }

  public static boolean uninstallModule(ItemStack stack, HaloModule module) {
    int mask = ItemNBTHelper.getInt(stack, PREFIX_MODULES, 0);
    if (module.containsThis(mask)) {
      ItemNBTHelper.setInt(stack, PREFIX_MODULES, mask - module.bitMask);
      return true;
    }
    return false;
  }

  public static boolean isModuleInstalled(ItemStack stack, HaloModule module) {
    int mask = ItemNBTHelper.getInt(stack, PREFIX_MODULES, 0);
    return module.containsThis(mask);
  }

  public static boolean isAnyModuleInstalled(ItemStack stack) {
    int mask = ItemNBTHelper.getInt(stack, PREFIX_MODULES, 0);
    for (HaloModule module : HaloModule.values()) {
      if (module.containsThis(mask)) {
        return true;
      }
    }
    return false;
  }

  public static int getNetworkRange(ItemStack stack) {
    return BASE_NETWORK_RANGE + getRangeBonus(stack);
  }

  public static int getRangeBonus(ItemStack stack) {
    int bonus = 0;
    for (HaloModule module : HaloModule.values()) {
      if (module.isRangeModule() && isModuleInstalled(stack, module)) {
        bonus += module.rangeBonus;
      }
    }
    return bonus;
  }

  @Nullable
  public static HaloModule getInstalledRangeModule(ItemStack stack) {
    for (HaloModule module : HaloModule.values()) {
      if (module.isRangeModule() && isModuleInstalled(stack, module)) {
        return module;
      }
    }
    return null;
  }

  public static boolean canPlayerAccessNetwork(Player player, ItemStack stack) {
    GlobalPos pos = getBoundIndexPosition(stack);
    return pos != null && CorporeaUtil.isEntityInRangeOfNetwork(player, pos, getNetworkRange(stack));
  }

  public static HaloInterfaceStyle getInterfaceStyle(ItemStack stack) {
    return HaloInterfaceStyle.fromSerializedName(ItemNBTHelper.getString(stack, PREFIX_INTERFACE_STYLE, HaloInterfaceStyle.CLASSIC.getSerializedName()));
  }

  public static void setInterfaceStyle(ItemStack stack, HaloInterfaceStyle style) {
    ItemNBTHelper.setString(stack, PREFIX_INTERFACE_STYLE, style.getSerializedName());
  }

  public static int getHaloTintPacked(ItemStack stack) {
    return ItemNBTHelper.getInt(stack, PREFIX_HALO_TINT, 0xFFFFFF);
  }

  public static float[] getHaloTintColor(ItemStack stack) {
    return unpackTint(getHaloTintPacked(stack));
  }

  public static float[] unpackTint(int packed) {
    return new float[] {
        ((packed >> 16) & 0xFF) / 255.0f,
        ((packed >>  8) & 0xFF) / 255.0f,
        ( packed        & 0xFF) / 255.0f
    };
  }

  public static void setHaloTintFromDye(ItemStack stack, net.minecraft.world.item.DyeColor dye) {
    float[] raw = dye.getTextureDiffuseColors();
    float max = Math.max(raw[0], Math.max(raw[1], raw[2]));
    float scale = (max < 0.55f && max > 0f) ? 0.55f / max : 1.0f;
    int r = Math.min(255, (int)(raw[0] * scale * 255));
    int g = Math.min(255, (int)(raw[1] * scale * 255));
    int b = Math.min(255, (int)(raw[2] * scale * 255));
    ItemNBTHelper.setInt(stack, PREFIX_HALO_TINT, (r << 16) | (g << 8) | b);
    ItemNBTHelper.setInt(stack, PREFIX_HALO_TINT_DYE, dye.getId());
  }

  @Nullable
  public static DyeColor getTintDyeColor(ItemStack stack) {
    int id = ItemNBTHelper.getInt(stack, PREFIX_HALO_TINT_DYE, -1);
    if (id < 0) return null;
    return DyeColor.byId(id);
  }

  private static String formatDyeName(DyeColor dye) {
    String[] words = dye.getName().split("_");
    StringBuilder sb = new StringBuilder();
    for (String word : words) {
      if (sb.length() > 0) sb.append(' ');
      sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
    }
    return sb.toString();
  }

  public static ListTag getOrCreateCraftingSlotNBTList(ItemStack halo) {
    boolean didChange = false;
    ListTag nbt = ItemNBTHelper.getList(halo, PREFIX_CRAFTING_SLOT_ITEMS, Tag.TAG_COMPOUND, true);
    if (nbt == null) {
      nbt = new ListTag();
      didChange = true;
    }
    for (int i = nbt.size(); i < 9; i++) {
      nbt.add(new CompoundTag());
    }
    if (didChange)
      ItemNBTHelper.setList(halo, PREFIX_CRAFTING_SLOT_ITEMS, nbt);
    return nbt;
  }

  /**
   * Should only be called on server!!!
   * @param replaceHandler called when the stack in that slot got replaced (probably drop the item as entity right here)
   * @param addStackHandler called when the adding stack got changed (with the changed stack passed in)
   * @return the old item in that slot
   */
  public static boolean tryPutStackInCraftingSlot(ItemStack halo, int slot, ItemStack orgAddStack, Consumer<ItemStack> replaceHandler, Consumer<ItemStack> addStackHandler) {
    if (slot > 8 || slot < 0 || orgAddStack.isEmpty()) return false;

    ItemStack addStack = orgAddStack.copy();
    ItemStack orgStack = getStackInCraftingSlot(halo, slot);

    Pair<Boolean, ItemStack> addResult = StackHelper.addToAnotherStack(addStack, orgStack);
    orgStack = addResult.getSecond();

    ListTag list = getOrCreateCraftingSlotNBTList(halo);
    if (addResult.getFirst()) {
      addStackHandler.accept(addStack);
      list.set(slot, orgStack.save(new CompoundTag()));
    } else {
      replaceHandler.accept(orgStack);
      addStackHandler.accept(ItemStack.EMPTY);
      list.set(slot, addStack.save(new CompoundTag()));
    }

    return true;
  }

  /**
   * This could be called on both client and server
   */
  public static ItemStack getStackInCraftingSlot(ItemStack halo, int slot) {
    ListTag list = getOrCreateCraftingSlotNBTList(halo);
    return ItemStack.of(list.getCompound(slot));
  }

  public static ItemStack setStackInCraftingSlot(ItemStack halo, int slot, ItemStack newStack) {
    ListTag list = getOrCreateCraftingSlotNBTList(halo);
    ItemStack oldStack = ItemStack.of(list.getCompound(slot));
    list.set(slot, newStack.isEmpty() ? new CompoundTag() : newStack.save(new CompoundTag()));
    return oldStack;
  }

  private String globalPosToString(GlobalPos pos) {
    return pos.dimension().location() + " (" + pos.pos().toShortString() + ")";
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
    GlobalPos indexPos = getBoundIndexPosition(stack);
    GlobalPos senderPos = getBoundSenderPosition(stack);

    if (KeyboardHelper.hasShiftDown()) {
      String temp;
      temp = indexPos == null ? I18n.get(IC.MODID + ".requesting_halo.tooltip.null") : "\n    " + globalPosToString(indexPos);
      tooltip.add(Component.literal(I18n.get(IC.MODID + ".requesting_halo.tooltip.index_pos") + temp));

      temp = senderPos == null ? I18n.get(IC.MODID + ".requesting_halo.tooltip.null") : "\n    " + globalPosToString(senderPos);
      tooltip.add(Component.literal(I18n.get(IC.MODID + ".requesting_halo.tooltip.sender_pos") + temp));

      HaloInterfaceStyle style = getInterfaceStyle(stack);
      tooltip.add(Component.literal(I18n.get(IC.MODID + ".requesting_halo.tooltip.style", I18n.get(style.translationKey))));

      DyeColor tintDye = getTintDyeColor(stack);
      String tintName = tintDye != null ? formatDyeName(tintDye) : I18n.get(IC.MODID + ".requesting_halo.tooltip.tint_default");
      tooltip.add(Component.literal(I18n.get(IC.MODID + ".requesting_halo.tooltip.tint", tintName)));

      if (ItemRequestingHalo.isAnyModuleInstalled(stack)) {
        StringBuilder builder = new StringBuilder(I18n.get(IC.MODID + ".requesting_halo.tooltip.modules_prefix"));
        builder.append("§r| ");
        for (HaloModule module : HaloModule.values()) {
          if (module.isRangeModule()) {
            continue;
          }
          boolean installed = isModuleInstalled(stack, module);
          builder.append(installed ? "§6" : "§8");
          builder.append(I18n.get(module.translationKey));
          builder.append("§r | ");
        }
        int rangeBonus = getRangeBonus(stack);
        builder.append(rangeBonus > 0 ? "§6" : "§8");
        builder.append(I18n.get(IC.MODID + ".halo_module.range", rangeBonus));
        builder.append("§r | ");
        tooltip.add(Component.literal(builder.toString()));
      } else {
        tooltip.add(Component.literal(
            I18n.get(IC.MODID + ".requesting_halo.tooltip.modules_prefix") +
                I18n.get(IC.MODID + ".requesting_halo.tooltip.null")
        ));
      }
    } else {
      tooltip.add(Component.translatable(IC.MODID + ".tooltip.shift_for_more"));
    }
  }

  @Override
  public boolean canAttackBlock(BlockState bs, Level world, BlockPos pos, Player player) {
    return false;
  }

  public boolean usesMana(ItemStack stack) {
    return true;
  }
}
