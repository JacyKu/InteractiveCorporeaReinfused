package shblock.interactivecorporea.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;

import java.util.function.Supplier;

public class SPacketRequestResult {
  private final int requestId;
  private final int successAmount;

  public SPacketRequestResult(int requestId, int successAmount) {
    this.requestId = requestId;
    this.successAmount = successAmount;
  }

  public static SPacketRequestResult decode(FriendlyByteBuf buf) {
    return new SPacketRequestResult(buf.readInt(), buf.readInt());
  }

  public void encode(FriendlyByteBuf buf) {
    buf.writeInt(requestId);
    buf.writeInt(successAmount);
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      RequestingHaloInterfaceHandler.handleRequestResultPacket(requestId, successAmount);
    });
    ctx.get().setPacketHandled(true);
  }
}
