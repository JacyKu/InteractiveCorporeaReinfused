package shblock.interactivecorporea.client.render;

import toni.sodiumdynamiclights.api.DynamicLightHandler;
import toni.sodiumdynamiclights.api.DynamicLightHandlers;
import toni.sodiumdynamiclights.SodiumDynamicLights;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;

public class DynamicLightsCompat {
  private static AnchorDynamicLightSource anchorLight;

  public static void register() {
    anchorLight = new AnchorDynamicLightSource();

    DynamicLightHandlers.registerDynamicLightHandler(EntityType.PLAYER,
        (DynamicLightHandler<net.minecraft.world.entity.player.Player>) player -> {
          if (player != Minecraft.getInstance().player) return 0;
          var face = RequestingHaloInterfaceHandler.getInterface();
          if (face == null || face.isAnchored()) return 0;
          return (int) Math.round(face.getOpenCloseProgress() * AnchorDynamicLightSource.MAX_LUMINANCE);
        });
  }

  public static void activateAnchorLight(double x, double y, double z) {
    if (anchorLight == null) return;
    anchorLight.x = x;
    anchorLight.y = y;
    anchorLight.z = z;
    SodiumDynamicLights.get().addLightSource(anchorLight);
  }

  public static void deactivateAnchorLight() {
    if (anchorLight == null) return;
    SodiumDynamicLights.get().removeLightSource(anchorLight);
  }
}
