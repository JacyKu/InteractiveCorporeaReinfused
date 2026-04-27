package shblock.interactivecorporea.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CPacketRequestingHaloViewUpdate {
  private final float rotationOffset;
  private final float relativeRotation;
  private final int listHeight;
  private final boolean hasSelection;
  private final float selectionX;
  private final float selectionY;
  private final String searchString;

  public CPacketRequestingHaloViewUpdate(float rotationOffset, float relativeRotation, boolean hasSelection, float selectionX, float selectionY) {
    this(rotationOffset, relativeRotation, 5, hasSelection, selectionX, selectionY, "");
  }

  public CPacketRequestingHaloViewUpdate(float rotationOffset, float relativeRotation, boolean hasSelection, float selectionX, float selectionY, String searchString) {
    this(rotationOffset, relativeRotation, 5, hasSelection, selectionX, selectionY, searchString);
  }

  public CPacketRequestingHaloViewUpdate(float rotationOffset, float relativeRotation, int listHeight, boolean hasSelection, float selectionX, float selectionY, String searchString) {
    this.rotationOffset = rotationOffset;
    this.relativeRotation = relativeRotation;
    this.listHeight = listHeight;
    this.hasSelection = hasSelection;
    this.selectionX = selectionX;
    this.selectionY = selectionY;
    this.searchString = searchString;
  }

  public static CPacketRequestingHaloViewUpdate decode(FriendlyByteBuf buf) {
    return new CPacketRequestingHaloViewUpdate(buf.readFloat(), buf.readFloat(), buf.readVarInt(), buf.readBoolean(), buf.readFloat(), buf.readFloat(), buf.readUtf());
  }

  public void encode(FriendlyByteBuf buf) {
    buf.writeFloat(rotationOffset);
    buf.writeFloat(relativeRotation);
    buf.writeVarInt(listHeight);
    buf.writeBoolean(hasSelection);
    buf.writeFloat(selectionX);
    buf.writeFloat(selectionY);
    buf.writeUtf(searchString);
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      if (player == null) return;
        CPacketRequestItemListUpdate.updateRemoteViewState(player, rotationOffset, listHeight);
      ModPacketHandler.sendToPlayersInWorldExcept(player, new SPacketRemoteRequestingHaloViewUpdate(
          player.getId(),
          rotationOffset,
          relativeRotation,
          listHeight,
          hasSelection,
          selectionX,
          selectionY,
          searchString
      ));
    });
    ctx.get().setPacketHandled(true);
  }
}