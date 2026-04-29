package shblock.interactivecorporea.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import shblock.interactivecorporea.common.requestinghalo.QuantumInserterHandler;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.NetworkHelper;

import java.util.function.Supplier;

public class CPacketInsertDroppedItem {
  private final CISlotPointer haloSlot;
  private final boolean dropStack;

  public CPacketInsertDroppedItem(CISlotPointer haloSlot, boolean dropStack) {
    this.haloSlot = haloSlot;
    this.dropStack = dropStack;
  }

  public static CPacketInsertDroppedItem decode(FriendlyByteBuf buf) {
    return new CPacketInsertDroppedItem(NetworkHelper.readCISlotPointer(buf), buf.readBoolean());
  }

  public void encode(FriendlyByteBuf buf) {
    NetworkHelper.writeCISlotPointer(buf, haloSlot);
    buf.writeBoolean(dropStack);
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      if (player == null) return;

      ItemStack selected = player.getInventory().getSelected();
      if (selected.isEmpty() || selected.getItem() instanceof ItemRequestingHalo) {
        return;
      }

      ItemStack halo = haloSlot.getStack(player);
      if (!QuantumInserterHandler.canUseQuantumInserter(halo)) {
        return;
      }

      int dropCount = dropStack ? selected.getCount() : 1;
      ItemStack toInsert = selected.copyWithCount(dropCount);
      selected.shrink(dropCount);
      if (selected.isEmpty()) {
        player.getInventory().setItem(player.getInventory().selected, ItemStack.EMPTY);
      }

      ItemStack remainder = QuantumInserterHandler.insertItem(player, halo, toInsert);
      QuantumInserterHandler.dropExcess(player, remainder);
    });
    ctx.get().setPacketHandled(true);
  }
}
