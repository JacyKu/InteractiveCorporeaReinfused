package shblock.interactivecorporea.common.requestinghalo;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.ModConfig;
import shblock.interactivecorporea.common.item.HaloModule;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.core.helper.MathHelper;
import vazkii.botania.common.core.helper.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = IC.MODID)
public class HaloAttractServerHandler {
  private static final Map<Player, List<ItemEntity>> attractedItems = new HashMap<>();

  @SubscribeEvent
  public static void onTick(TickEvent.ServerTickEvent event) {
    for (Map.Entry<Player, List<ItemEntity>> entry : attractedItems.entrySet()) {
      Player player = entry.getKey();
      List<ItemEntity> list = entry.getValue();
      if (player.isAlive()) {
        Vector3 pos = new Vector3(player.getX(), player.getY() + .75, player.getZ());
        for (int i = list.size() - 1; i >= 0; i--) {
          ItemEntity item = list.get(i);
          if (!item.isAlive()) {
            list.remove(i);
            continue;
          }

          doAttract(pos, item);
        }
      }
    }
  }

  private static void doAttract(Vector3 pos, ItemEntity item) {
    MathHelper.setEntityMotionFromVector(item, pos, 0.3F);
    item.hasImpulse = true;
    item.hurtMarked = true;

    ServerLevel world = (ServerLevel) item.level();
    boolean red = world.getRandom().nextBoolean();
    float r = red ? 1F : 0F;
    float b = red ? 0F : 1F;
    int particleCount = ModConfig.scaleParticleCount(1);
    if (particleCount > 0) {
      world.sendParticles(
          SparkleParticleData.sparkle(3F, r, 0, b, 10),
          item.getX(),
          item.getY() + .2,
          item.getZ(),
          particleCount, .1, .1, .1, 1F
      );
    }
  }

  public static void addToAttractedItems(Player player, ItemEntity item) {
    List<ItemEntity> list = attractedItems.computeIfAbsent(player, k -> new ArrayList<>());
    list.add(item);
  }

  /**
   * @return if the item was attracted (if the halo has the magnate module)
   */
  public static boolean attractIfHasModule(Player player, ItemEntity item, ItemStack halo) {
    if (ItemRequestingHalo.isModuleInstalled(halo, HaloModule.MAGNATE)) {
      addToAttractedItems(player, item);
      return true;
    } else {
      return false;
    }
  }
}
