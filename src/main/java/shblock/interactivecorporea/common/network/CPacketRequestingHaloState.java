package shblock.interactivecorporea.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import shblock.interactivecorporea.common.requestinghalo.RequestingHaloServerState;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.NetworkHelper;

import java.util.function.Supplier;

public class CPacketRequestingHaloState {
  private final CISlotPointer slot;
  private final boolean open;
  private final float rotationOffset;

  public CPacketRequestingHaloState(CISlotPointer slot, boolean open) {
    this(slot, open, 0F);
  }

  public CPacketRequestingHaloState(CISlotPointer slot, boolean open, double rotationOffset) {
    this.slot = slot;
    this.open = open;
    this.rotationOffset = (float) rotationOffset;
  }

  public static CPacketRequestingHaloState decode(FriendlyByteBuf buf) {
    return new CPacketRequestingHaloState(NetworkHelper.readCISlotPointer(buf), buf.readBoolean(), buf.readFloat());
  }

  public void encode(FriendlyByteBuf buf) {
    NetworkHelper.writeCISlotPointer(buf, slot);
    buf.writeBoolean(open);
    buf.writeFloat(rotationOffset);
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      if (player == null) return;
      if (!open) {
        RequestingHaloServerState.close(player);
        CPacketRequestItemListUpdate.broadcastRemoteClose(player);
        return;
      }

      ItemStack halo = slot.getStack(player);
      if (!(halo.getItem() instanceof ItemRequestingHalo)) {
        RequestingHaloServerState.close(player);
        return;
      }
      if (!ItemRequestingHalo.canPlayerAccessNetwork(player, halo)) {
        RequestingHaloServerState.close(player);
        CPacketRequestItemListUpdate.broadcastRemoteClose(player);
        return;
      }
      RequestingHaloServerState.open(player, slot);
      CPacketRequestItemListUpdate.broadcastRemoteState(player, halo, rotationOffset);
    });
    ctx.get().setPacketHandled(true);
  }
}