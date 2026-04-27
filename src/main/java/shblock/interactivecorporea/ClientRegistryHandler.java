package shblock.interactivecorporea;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import shblock.interactivecorporea.client.particle.QuantizationParticleType;
import shblock.interactivecorporea.client.renderer.tile.TERItemQuantizationDevice;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterface;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;
import shblock.interactivecorporea.common.block.ModBlocks;
import shblock.interactivecorporea.common.tile.ModTiles;

@Mod.EventBusSubscriber(modid = IC.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistryHandler {
  @SubscribeEvent
  public static void onClientSetup(final FMLClientSetupEvent event) {
    event.enqueueWork(() -> ItemBlockRenderTypes.setRenderLayer(ModBlocks.itemQuantizationDevice, RenderType.translucent()));
  }

  @SubscribeEvent
  public static void registerKeys(RegisterKeyMappingsEvent event) {
    event.register(RequestingHaloInterfaceHandler.KEY_BINDING);
    event.register(RequestingHaloInterface.KEY_SEARCH);
    event.register(RequestingHaloInterface.KEY_REQUEST_UPDATE);
  }

  @SubscribeEvent
  public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(ModTiles.itemQuantizationDevice, TERItemQuantizationDevice::new);
  }

  @SubscribeEvent
  public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
    event.registerSpriteSet(QuantizationParticleType.INSTANCE, QuantizationParticleType.Factory::new);
  }
}