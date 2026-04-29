package shblock.interactivecorporea.common.requestinghalo;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import shblock.interactivecorporea.common.util.CISlotPointer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = IC.MODID)
public class RequestingHaloServerState {
  private static final Map<UUID, CISlotPointer> OPEN_HALOS = new HashMap<>();

  public static void open(ServerPlayer player, CISlotPointer slot) {
    OPEN_HALOS.put(player.getUUID(), slot);
  }

  public static void close(ServerPlayer player) {
    OPEN_HALOS.remove(player.getUUID());
  }

  @Nullable
  public static ItemStack getOpenHalo(ServerPlayer player) {
    CISlotPointer slot = OPEN_HALOS.get(player.getUUID());
    if (slot == null) {
      return null;
    }

    ItemStack halo = slot.getStack(player);
    if (!(halo.getItem() instanceof ItemRequestingHalo)) {
      close(player);
      return null;
    }
    return halo;
  }

  @SubscribeEvent
  public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
    if (event.getEntity() instanceof ServerPlayer player) {
      close(player);
    }
  }
}