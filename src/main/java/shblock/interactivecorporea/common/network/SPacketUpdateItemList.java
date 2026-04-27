package shblock.interactivecorporea.common.network;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;
import shblock.interactivecorporea.common.util.NetworkHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SPacketUpdateItemList {
  private final List<ItemStack> itemList;

  public SPacketUpdateItemList(List<ItemStack> itemList) {
    this.itemList = itemList;
  }

  public static SPacketUpdateItemList decode(FriendlyByteBuf buf) {
    int len = buf.readVarInt();
    List<ItemStack> itemList = new ArrayList<>();
    for (int i = 0; i < len; i++) {
      itemList.add(NetworkHelper.readBigStack(buf));
    }
    return new SPacketUpdateItemList(itemList);
  }

  public void encode(FriendlyByteBuf buf) {
    buf.writeVarInt(itemList.size());
    for (ItemStack stack : itemList) {
      NetworkHelper.writeBigStack(buf, stack, false);
    }
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      RequestingHaloInterfaceHandler.handleUpdatePacket(itemList);
    });
    ctx.get().setPacketHandled(true);
  }
}
