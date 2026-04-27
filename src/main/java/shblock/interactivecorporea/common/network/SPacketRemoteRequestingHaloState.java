package shblock.interactivecorporea.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;
import shblock.interactivecorporea.common.util.NetworkHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class SPacketRemoteRequestingHaloState {
  private final int playerId;
  private final boolean open;
  private final float rotationOffset;
  private final int listHeight;
  private final boolean sortByAmount;
  private final List<ItemStack> itemList;

  public SPacketRemoteRequestingHaloState(int playerId, boolean open, float rotationOffset, int listHeight, boolean sortByAmount, List<ItemStack> itemList) {
    this.playerId = playerId;
    this.open = open;
    this.rotationOffset = rotationOffset;
    this.listHeight = listHeight;
    this.sortByAmount = sortByAmount;
    this.itemList = itemList;
  }

  public static SPacketRemoteRequestingHaloState open(int playerId, float rotationOffset, int listHeight, boolean sortByAmount, List<ItemStack> itemList) {
    return new SPacketRemoteRequestingHaloState(playerId, true, rotationOffset, listHeight, sortByAmount, itemList);
  }

  public static SPacketRemoteRequestingHaloState close(int playerId) {
    return new SPacketRemoteRequestingHaloState(playerId, false, 0F, 5, false, Collections.emptyList());
  }

  public static SPacketRemoteRequestingHaloState decode(FriendlyByteBuf buf) {
    int playerId = buf.readVarInt();
    boolean open = buf.readBoolean();
    if (!open) {
      return close(playerId);
    }

    float rotationOffset = buf.readFloat();
    int listHeight = buf.readVarInt();
    boolean sortByAmount = buf.readBoolean();
    int len = buf.readVarInt();
    List<ItemStack> itemList = new ArrayList<>();
    for (int i = 0; i < len; i++) {
      itemList.add(NetworkHelper.readBigStack(buf));
    }
    return open(playerId, rotationOffset, listHeight, sortByAmount, itemList);
  }

  public void encode(FriendlyByteBuf buf) {
    buf.writeVarInt(playerId);
    buf.writeBoolean(open);
    if (!open) {
      return;
    }

    buf.writeFloat(rotationOffset);
    buf.writeVarInt(listHeight);
    buf.writeBoolean(sortByAmount);
    buf.writeVarInt(itemList.size());
    for (ItemStack stack : itemList) {
      NetworkHelper.writeBigStack(buf, stack, false);
    }
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> RequestingHaloInterfaceHandler.handleRemoteState(playerId, open, rotationOffset, listHeight, sortByAmount, itemList));
    ctx.get().setPacketHandled(true);
  }
}