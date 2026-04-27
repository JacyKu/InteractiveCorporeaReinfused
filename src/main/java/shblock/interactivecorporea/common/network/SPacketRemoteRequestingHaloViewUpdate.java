package shblock.interactivecorporea.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;

import java.util.function.Supplier;

public class SPacketRemoteRequestingHaloViewUpdate {
  private final int playerId;
  private final float rotationOffset;
  private final float relativeRotation;
  private final int listHeight;
  private final boolean hasSelection;
  private final float selectionX;
  private final float selectionY;
  private final String searchString;

  public SPacketRemoteRequestingHaloViewUpdate(int playerId, float rotationOffset, float relativeRotation, boolean hasSelection, float selectionX, float selectionY) {
    this(playerId, rotationOffset, relativeRotation, 5, hasSelection, selectionX, selectionY, "");
  }

  public SPacketRemoteRequestingHaloViewUpdate(int playerId, float rotationOffset, float relativeRotation, boolean hasSelection, float selectionX, float selectionY, String searchString) {
    this(playerId, rotationOffset, relativeRotation, 5, hasSelection, selectionX, selectionY, searchString);
  }

  public SPacketRemoteRequestingHaloViewUpdate(int playerId, float rotationOffset, float relativeRotation, int listHeight, boolean hasSelection, float selectionX, float selectionY, String searchString) {
    this.playerId = playerId;
    this.rotationOffset = rotationOffset;
    this.relativeRotation = relativeRotation;
    this.listHeight = listHeight;
    this.hasSelection = hasSelection;
    this.selectionX = selectionX;
    this.selectionY = selectionY;
    this.searchString = searchString;
  }

  public static SPacketRemoteRequestingHaloViewUpdate decode(FriendlyByteBuf buf) {
    return new SPacketRemoteRequestingHaloViewUpdate(buf.readVarInt(), buf.readFloat(), buf.readFloat(), buf.readVarInt(), buf.readBoolean(), buf.readFloat(), buf.readFloat(), buf.readUtf());
  }

  public void encode(FriendlyByteBuf buf) {
    buf.writeVarInt(playerId);
    buf.writeFloat(rotationOffset);
    buf.writeFloat(relativeRotation);
    buf.writeVarInt(listHeight);
    buf.writeBoolean(hasSelection);
    buf.writeFloat(selectionX);
    buf.writeFloat(selectionY);
    buf.writeUtf(searchString);
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> RequestingHaloInterfaceHandler.handleRemoteViewUpdate(playerId, rotationOffset, relativeRotation, listHeight, hasSelection, selectionX, selectionY, searchString));
    ctx.get().setPacketHandled(true);
  }
}