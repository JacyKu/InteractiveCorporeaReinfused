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
  private final boolean anchored;
  private final double anchoredX;
  private final double anchoredY;
  private final double anchoredZ;

  public CPacketRequestingHaloViewUpdate(float rotationOffset, float relativeRotation, boolean hasSelection, float selectionX, float selectionY) {
    this(rotationOffset, relativeRotation, 5, hasSelection, selectionX, selectionY, "", false, 0, 0, 0);
  }

  public CPacketRequestingHaloViewUpdate(float rotationOffset, float relativeRotation, boolean hasSelection, float selectionX, float selectionY, String searchString) {
    this(rotationOffset, relativeRotation, 5, hasSelection, selectionX, selectionY, searchString, false, 0, 0, 0);
  }

  public CPacketRequestingHaloViewUpdate(float rotationOffset, float relativeRotation, int listHeight, boolean hasSelection, float selectionX, float selectionY, String searchString) {
    this(rotationOffset, relativeRotation, listHeight, hasSelection, selectionX, selectionY, searchString, false, 0, 0, 0);
  }

  public CPacketRequestingHaloViewUpdate(float rotationOffset, float relativeRotation, int listHeight, boolean hasSelection, float selectionX, float selectionY, String searchString, boolean anchored, double anchoredX, double anchoredY, double anchoredZ) {
    this.rotationOffset = rotationOffset;
    this.relativeRotation = relativeRotation;
    this.listHeight = listHeight;
    this.hasSelection = hasSelection;
    this.selectionX = selectionX;
    this.selectionY = selectionY;
    this.searchString = searchString;
    this.anchored = anchored;
    this.anchoredX = anchoredX;
    this.anchoredY = anchoredY;
    this.anchoredZ = anchoredZ;
  }

  public static CPacketRequestingHaloViewUpdate decode(FriendlyByteBuf buf) {
    return new CPacketRequestingHaloViewUpdate(buf.readFloat(), buf.readFloat(), buf.readVarInt(), buf.readBoolean(), buf.readFloat(), buf.readFloat(), buf.readUtf(), buf.readBoolean(), buf.readDouble(), buf.readDouble(), buf.readDouble());
  }

  public void encode(FriendlyByteBuf buf) {
    buf.writeFloat(rotationOffset);
    buf.writeFloat(relativeRotation);
    buf.writeVarInt(listHeight);
    buf.writeBoolean(hasSelection);
    buf.writeFloat(selectionX);
    buf.writeFloat(selectionY);
    buf.writeUtf(searchString);
    buf.writeBoolean(anchored);
    buf.writeDouble(anchoredX);
    buf.writeDouble(anchoredY);
    buf.writeDouble(anchoredZ);
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
          searchString,
          anchored,
          anchoredX,
          anchoredY,
          anchoredZ
      ));
    });
    ctx.get().setPacketHandled(true);
  }
}