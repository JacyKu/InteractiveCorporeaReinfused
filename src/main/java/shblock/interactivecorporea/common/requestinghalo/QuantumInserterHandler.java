package shblock.interactivecorporea.common.requestinghalo;

import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.ModSounds;
import shblock.interactivecorporea.common.item.HaloModule;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import shblock.interactivecorporea.common.network.CPacketRequestItemListUpdate;
import shblock.interactivecorporea.common.tile.TileItemQuantizationDevice;

@Mod.EventBusSubscriber(modid = IC.MODID)
public class QuantumInserterHandler {
  private static boolean bypassTossHandling = false;

  @SubscribeEvent
  public static void onItemToss(ItemTossEvent event) {
    if (bypassTossHandling || !(event.getPlayer() instanceof ServerPlayer player)) {
      return;
    }

    ItemStack halo = RequestingHaloServerState.getOpenHalo(player);
    if (!canUseQuantumInserter(halo)) {
      return;
    }

    ItemEntity itemEntity = event.getEntity();
    ItemStack dropped = itemEntity.getItem();
    if (dropped.isEmpty() || dropped.getItem() instanceof ItemRequestingHalo) {
      return;
    }

    ItemStack toInsert = dropped.copy();
    ItemStack remainder = insertItem(player, halo, toInsert);
    int inserted = toInsert.getCount() - remainder.getCount();
    if (inserted <= 0) {
      playReachEdge(player);
      return;
    }

    if (remainder.isEmpty()) {
      event.setCanceled(true);
      itemEntity.discard();
    } else {
      itemEntity.setItem(remainder);
      playReachEdge(player);
    }
  }

  public static boolean canUseQuantumInserter(ItemStack halo) {
    return halo != null
        && halo.getItem() instanceof ItemRequestingHalo
        && ItemRequestingHalo.isModuleInstalled(halo, HaloModule.QUANTUM_INSERTER)
        && ItemRequestingHalo.getBoundSenderPosition(halo) != null;
  }

  public static ItemStack insertItem(ServerPlayer player, ItemStack halo, ItemStack stack) {
    if (!canUseQuantumInserter(halo) || stack.isEmpty() || stack.getItem() instanceof ItemRequestingHalo) {
      return stack;
    }
    if (!ItemRequestingHalo.canPlayerAccessNetwork(player, halo)) {
      player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.haloOutOfRange, SoundSource.PLAYERS, 1F, 1F);
      return stack;
    }

    TileItemQuantizationDevice device = getBoundDevice(player, halo);
    if (device == null) {
      return stack;
    }

    ItemStack remainder = device.insertItem(stack);
    if (remainder.getCount() != stack.getCount()) {
      CPacketRequestItemListUpdate.sendItemListToPlayer(player, halo);
      CPacketRequestItemListUpdate.broadcastRemoteState(player, halo);
    }
    return remainder;
  }

  public static void dropExcess(ServerPlayer player, ItemStack stack) {
    if (stack.isEmpty()) {
      return;
    }

    bypassTossHandling = true;
    try {
      ItemEntity itemEntity = player.drop(stack, false, true);
      if (itemEntity != null) {
        itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().scale(.5D));
      }
    } finally {
      bypassTossHandling = false;
    }
    playReachEdge(player);
  }

  public static void playReachEdge(ServerPlayer player) {
    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.haloReachEdge, SoundSource.PLAYERS, 1F, 1F);
  }

  private static TileItemQuantizationDevice getBoundDevice(ServerPlayer player, ItemStack halo) {
    GlobalPos pos = ItemRequestingHalo.getBoundSenderPosition(halo);
    if (pos == null) {
      return null;
    }

    Level world = player.server.getLevel(pos.dimension());
    if (world == null) {
      return null;
    }

    BlockEntity blockEntity = world.getBlockEntity(pos.pos());
    return blockEntity instanceof TileItemQuantizationDevice device ? device : null;
  }
}