package shblock.interactivecorporea.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import shblock.interactivecorporea.IC;

import java.util.Optional;

public class ModPacketHandler {
  private static final String PROTOCOL_VERSION = "1";
  public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
      new ResourceLocation(IC.MODID, "main"),
      () -> PROTOCOL_VERSION,
      PROTOCOL_VERSION::equals,
      PROTOCOL_VERSION::equals
  );

  public static void init() {
    int id = 0;
    CHANNEL.registerMessage(id++, CPacketRequestItemListUpdate.class, CPacketRequestItemListUpdate::encode, CPacketRequestItemListUpdate::decode, CPacketRequestItemListUpdate::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    CHANNEL.registerMessage(id++, SPacketUpdateItemList.class, SPacketUpdateItemList::encode, SPacketUpdateItemList::decode, SPacketUpdateItemList::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    CHANNEL.registerMessage(id++, CPacketRequestItem.class, CPacketRequestItem::encode, CPacketRequestItem::decode, CPacketRequestItem::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    CHANNEL.registerMessage(id++, SPacketPlayQuantizationEffect.class, SPacketPlayQuantizationEffect::encode, SPacketPlayQuantizationEffect::decode, SPacketPlayQuantizationEffect::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    CHANNEL.registerMessage(id++, SPacketRequestResult.class, SPacketRequestResult::encode, SPacketRequestResult::decode, SPacketRequestResult::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    CHANNEL.registerMessage(id++, CPacketChangeStackInHaloCraftingSlot.class, CPacketChangeStackInHaloCraftingSlot::encode, CPacketChangeStackInHaloCraftingSlot::decode, CPacketChangeStackInHaloCraftingSlot::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    CHANNEL.registerMessage(id++, CPacketDoCraft.class, CPacketDoCraft::encode, CPacketDoCraft::decode, CPacketDoCraft::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    CHANNEL.registerMessage(id++, SPacketCraftingState.class, SPacketCraftingState::encode, SPacketCraftingState::decode, SPacketCraftingState::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    CHANNEL.registerMessage(id++, CPacketInsertDroppedItem.class, CPacketInsertDroppedItem::encode, CPacketInsertDroppedItem::decode, CPacketInsertDroppedItem::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    CHANNEL.registerMessage(id++, CPacketRequestingHaloState.class, CPacketRequestingHaloState::encode, CPacketRequestingHaloState::decode, CPacketRequestingHaloState::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    CHANNEL.registerMessage(id++, SPacketRemoteRequestingHaloState.class, SPacketRemoteRequestingHaloState::encode, SPacketRemoteRequestingHaloState::decode, SPacketRemoteRequestingHaloState::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    CHANNEL.registerMessage(id++, CPacketRequestingHaloViewUpdate.class, CPacketRequestingHaloViewUpdate::encode, CPacketRequestingHaloViewUpdate::decode, CPacketRequestingHaloViewUpdate::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    CHANNEL.registerMessage(id++, SPacketRemoteRequestingHaloViewUpdate.class, SPacketRemoteRequestingHaloViewUpdate::encode, SPacketRemoteRequestingHaloViewUpdate::decode, SPacketRemoteRequestingHaloViewUpdate::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
  }

  public static void sendToPlayer(ServerPlayer player, Object message) {
    CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
  }

  public static void sendToPlayersInWorld(ServerLevel world, Object message) {
    for (ServerPlayer player : world.getPlayers(serverPlayer -> true)) {
      sendToPlayer(player, message);
    }
  }

  public static void sendToPlayersInWorldExcept(ServerPlayer source, Object message) {
    if (!(source.level() instanceof ServerLevel world)) {
      return;
    }
    for (ServerPlayer player : world.getPlayers(serverPlayer -> serverPlayer != source)) {
      sendToPlayer(player, message);
    }
  }

  public static void sendToServer(Object message) {
    CHANNEL.sendToServer(message);
  }
}
