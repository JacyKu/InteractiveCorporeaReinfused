package shblock.interactivecorporea.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import shblock.interactivecorporea.common.requestinghalo.HaloCraftingServerHandler;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.NetworkHelper;

import java.util.function.Supplier;

public class CPacketDoCraft {
  private final CISlotPointer haloSlot;
  private final int requestId; // only used by client to identify the requesting event to play animations

  public CPacketDoCraft(CISlotPointer haloSlot, int requestId) {
    this.haloSlot = haloSlot;
    this.requestId = requestId;
  }

  public static CPacketDoCraft decode(FriendlyByteBuf buf) {
    return new CPacketDoCraft(
        NetworkHelper.readCISlotPointer(buf),
        buf.readInt()
    );
  }

  public void encode(FriendlyByteBuf buf) {
    NetworkHelper.writeCISlotPointer(buf, haloSlot);
    buf.writeInt(requestId);
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      if (ctx.get().getSender() == null) {
        return;
      }
      HaloCraftingServerHandler.doCraft(ctx.get().getSender(), haloSlot, requestId);
    });
    ctx.get().setPacketHandled(true);
  }
}
