package shblock.interactivecorporea.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import shblock.interactivecorporea.common.corporea.CorporeaUtil;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import shblock.interactivecorporea.common.tile.TileItemQuantizationDevice;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.NetworkHelper;
import vazkii.botania.api.corporea.CorporeaHelper;
import vazkii.botania.api.corporea.CorporeaSpark;
import vazkii.botania.common.core.helper.ItemNBTHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class CPacketRequestItemListUpdate {
  private static final float INITIAL_ROTATION = 36F;
  private static final String PREFIX_LIST_HEIGHT = "settings_item_list_height";
  private static final Map<UUID, Float> REMOTE_ROTATION_OFFSETS = new HashMap<>();
  private static final Map<UUID, Integer> REMOTE_LIST_HEIGHTS = new HashMap<>();

  private final CISlotPointer slot;

  public CPacketRequestItemListUpdate(CISlotPointer slot) {
    this.slot = slot;
  }

  public static CPacketRequestItemListUpdate decode(FriendlyByteBuf buf) {
    return new CPacketRequestItemListUpdate(NetworkHelper.readCISlotPointer(buf));
  }

  public void encode(FriendlyByteBuf buf) {
    NetworkHelper.writeCISlotPointer(buf, slot);
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      if (player == null) return;
      ItemStack stack = slot.getStack(player);
      if (!(stack.getItem() instanceof ItemRequestingHalo)) {
        return;
      }
      if (!ItemRequestingHalo.canPlayerAccessNetwork(player, stack)) {
        return;
      }
      GlobalPos pos = ItemRequestingHalo.getBoundIndexPosition(stack);
      if (pos == null) {
        return;
      }
      List<ItemStack> result = queryAt(player, pos);
      if (result == null) {
        return;
      }

      GlobalPos senderPos = ItemRequestingHalo.getBoundSenderPosition(stack);
      if (result.isEmpty() && senderPos != null) {
        List<ItemStack> senderResult = querySenderAt(player, senderPos);
        if (senderResult != null) {
          result = senderResult;
        }
      }

      ModPacketHandler.sendToPlayer(player, new SPacketUpdateItemList(result));
      broadcastRemoteState(player, stack, result, getRemoteRotationOffset(player));
    });
    ctx.get().setPacketHandled(true);
  }

  public static void broadcastRemoteState(ServerPlayer player, ItemStack halo) {
    List<ItemStack> result = queryForHalo(player, halo);
    broadcastRemoteState(player, halo, result == null ? List.of() : result, getRemoteRotationOffset(player));
  }

  public static void broadcastRemoteState(ServerPlayer player, ItemStack halo, float rotationOffset) {
    REMOTE_ROTATION_OFFSETS.put(player.getUUID(), rotationOffset);
    List<ItemStack> result = queryForHalo(player, halo);
    broadcastRemoteState(player, halo, result == null ? List.of() : result, rotationOffset);
  }

  public static void sendItemListToPlayer(ServerPlayer player, ItemStack halo) {
    List<ItemStack> result = queryForHalo(player, halo);
    if (result != null) {
      ModPacketHandler.sendToPlayer(player, new SPacketUpdateItemList(result));
    }
  }

  public static void broadcastRemoteClose(ServerPlayer player) {
    REMOTE_ROTATION_OFFSETS.remove(player.getUUID());
    REMOTE_LIST_HEIGHTS.remove(player.getUUID());
    ModPacketHandler.sendToPlayersInWorldExcept(player, SPacketRemoteRequestingHaloState.close(player.getId()));
  }

  public static void updateRemoteViewState(ServerPlayer player, float rotationOffset, int listHeight) {
    REMOTE_ROTATION_OFFSETS.put(player.getUUID(), rotationOffset);
    REMOTE_LIST_HEIGHTS.put(player.getUUID(), Math.max(1, Math.min(16, listHeight)));
  }

  private static void broadcastRemoteState(ServerPlayer player, ItemStack halo, List<ItemStack> result, float rotationOffset) {
    ModPacketHandler.sendToPlayersInWorldExcept(player, SPacketRemoteRequestingHaloState.open(
        player.getId(),
        rotationOffset,
        getRemoteListHeight(player, halo),
        true,
        result
    ));
  }

  private static float getRemoteRotationOffset(ServerPlayer player) {
    return REMOTE_ROTATION_OFFSETS.getOrDefault(player.getUUID(), player.getYRot() - INITIAL_ROTATION);
  }

  private static int getRemoteListHeight(ServerPlayer player, ItemStack halo) {
    return REMOTE_LIST_HEIGHTS.getOrDefault(player.getUUID(), ItemNBTHelper.getInt(halo, PREFIX_LIST_HEIGHT, 5));
  }

  private static List<ItemStack> queryForHalo(ServerPlayer player, ItemStack stack) {
    if (!ItemRequestingHalo.canPlayerAccessNetwork(player, stack)) {
      return List.of();
    }

    GlobalPos pos = ItemRequestingHalo.getBoundIndexPosition(stack);
    if (pos == null) {
      return List.of();
    }
    List<ItemStack> result = queryAt(player, pos);
    if (result == null) {
      return null;
    }

    GlobalPos senderPos = ItemRequestingHalo.getBoundSenderPosition(stack);
    if (result.isEmpty() && senderPos != null) {
      List<ItemStack> senderResult = querySenderAt(player, senderPos);
      if (senderResult != null) {
        result = senderResult;
      }
    }
    return result;
  }

  private static List<ItemStack> queryAt(ServerPlayer player, GlobalPos pos) {
    var world = player.server.getLevel(pos.dimension());
    if (world == null) {
      return null;
    }
    CorporeaSpark spark = CorporeaHelper.instance().getSparkForBlock(world, pos.pos());
    if (spark == null) {
      return null;
    }
    return CorporeaUtil.getAllItemsCompacted(spark);
  }

  private static List<ItemStack> querySenderAt(ServerPlayer player, GlobalPos pos) {
    var world = player.server.getLevel(pos.dimension());
    if (world == null) {
      return null;
    }
    BlockEntity blockEntity = world.getBlockEntity(pos.pos());
    if (!(blockEntity instanceof TileItemQuantizationDevice device)) {
      return null;
    }
    CorporeaSpark spark = device.getSpark();
    if (spark == null) {
      return null;
    }
    return CorporeaUtil.getAllItemsCompacted(spark);
  }
}
