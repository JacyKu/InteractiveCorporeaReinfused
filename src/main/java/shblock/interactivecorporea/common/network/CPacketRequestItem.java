package shblock.interactivecorporea.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import shblock.interactivecorporea.common.tile.TileItemQuantizationDevice;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.NetworkHelper;
import vazkii.botania.common.core.helper.Vector3;

import java.util.function.Supplier;

public class CPacketRequestItem {
  private final CISlotPointer slot;
  private final ItemStack stack;
  private final Vector3 requestPos;
  private final Vector3 normal;
  private final int requestId; // only use by the client to identify the request in the PacketRequestResult

  public CPacketRequestItem(CISlotPointer slot, ItemStack stack, Vector3 requestPos, Vector3 normal, int requestId) {
    this.slot = slot;
    this.stack = stack;
    this.requestPos = requestPos;
    this.normal = normal;
    this.requestId = requestId;
  }

  public static CPacketRequestItem decode(FriendlyByteBuf buf) {
    return new CPacketRequestItem(NetworkHelper.readCISlotPointer(buf), NetworkHelper.readBigStack(buf), NetworkHelper.readVector3(buf), NetworkHelper.readVector3(buf), buf.readInt());
  }

  public void encode(FriendlyByteBuf buf) {
    NetworkHelper.writeCISlotPointer(buf, slot);
    NetworkHelper.writeBigStack(buf, stack, false);
    NetworkHelper.writeVector3(buf, requestPos);
    NetworkHelper.writeVector3(buf, normal);
    buf.writeInt(requestId);
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      if (player == null) return;
      ItemStack halo = slot.getStack(player);
      if (!(halo.getItem() instanceof ItemRequestingHalo)) return;
      if (!ItemRequestingHalo.canPlayerAccessNetwork(player, halo)) return;
      GlobalPos pos = ItemRequestingHalo.getBoundSenderPosition(halo);
      if (pos == null) return;
      Level world = player.server.getLevel(pos.dimension());
      if (world == null) return;
      BlockEntity te = world.getBlockEntity(pos.pos());
      if (!(te instanceof TileItemQuantizationDevice)) return;
      TileItemQuantizationDevice qd = (TileItemQuantizationDevice) te;
      ItemStack reqStack = stack.copy();
      int successAmount = qd.requestItem(reqStack, requestPos, normal, player, halo);

      ModPacketHandler.sendToPlayer(player, new SPacketRequestResult(requestId, successAmount));
      CPacketRequestItemListUpdate.sendItemListToPlayer(player, halo);
      CPacketRequestItemListUpdate.broadcastRemoteState(player, halo);
    });
  }
}
