package shblock.interactivecorporea;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import shblock.interactivecorporea.client.particle.QuantizationParticleType;
import shblock.interactivecorporea.client.render.ModRenderTypes;
import shblock.interactivecorporea.client.renderer.tile.TERItemQuantizationDevice;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterface;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;
import shblock.interactivecorporea.common.block.ModBlocks;
import shblock.interactivecorporea.common.tile.ModTiles;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.io.IOException;

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
    event.register(RequestingHaloInterface.KEY_ANCHOR);
  }

  @SubscribeEvent
  public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(ModTiles.itemQuantizationDevice, TERItemQuantizationDevice::new);
  }

  @SubscribeEvent
  public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
    event.registerSpriteSet(QuantizationParticleType.INSTANCE, QuantizationParticleType.Factory::new);
  }

  @SubscribeEvent
  public static void registerShaders(RegisterShadersEvent event) {
    try {
      event.registerShader(
          new ShaderInstance(event.getResourceProvider(),
              new ResourceLocation(IC.MODID, "halo_clouds"),
              DefaultVertexFormat.POSITION_COLOR_TEX),
          inst -> ModRenderTypes.haloCloudShader = inst
      );
      event.registerShader(
          new ShaderInstance(event.getResourceProvider(),
              new ResourceLocation(IC.MODID, "halo_space"),
              DefaultVertexFormat.POSITION_COLOR_TEX),
          inst -> ModRenderTypes.haloSpaceShader = inst
      );
      event.registerShader(
          new ShaderInstance(event.getResourceProvider(),
              new ResourceLocation(IC.MODID, "halo_fallingstars"),
              DefaultVertexFormat.POSITION_COLOR_TEX),
          inst -> ModRenderTypes.haloFallingStarsShader = inst
      );
      event.registerShader(
          new ShaderInstance(event.getResourceProvider(),
              new ResourceLocation(IC.MODID, "halo_lavalamp"),
              DefaultVertexFormat.POSITION_COLOR_TEX),
          inst -> ModRenderTypes.haloLavaLampShader = inst
      );
      event.registerShader(
          new ShaderInstance(event.getResourceProvider(),
              new ResourceLocation(IC.MODID, "halo_depthmap"),
              DefaultVertexFormat.POSITION_COLOR_TEX),
          inst -> ModRenderTypes.haloDepthMapShader = inst
      );
      event.registerShader(
          new ShaderInstance(event.getResourceProvider(),
              new ResourceLocation(IC.MODID, "halo_foggyclouds"),
              DefaultVertexFormat.POSITION_COLOR_TEX),
          inst -> ModRenderTypes.haloFoggyCloudShader = inst
      );
      event.registerShader(
          new ShaderInstance(event.getResourceProvider(),
              new ResourceLocation(IC.MODID, "halo_glassliquid"),
              DefaultVertexFormat.POSITION_COLOR_TEX),
          inst -> ModRenderTypes.haloGlassLiquidShader = inst
      );
      event.registerShader(
          new ShaderInstance(event.getResourceProvider(),
              new ResourceLocation(IC.MODID, "halo_metalclouds"),
              DefaultVertexFormat.POSITION_COLOR_TEX),
          inst -> ModRenderTypes.haloMetalCloudShader = inst
      );
      event.registerShader(
          new ShaderInstance(event.getResourceProvider(),
              new ResourceLocation(IC.MODID, "halo_smokish"),
              DefaultVertexFormat.POSITION_COLOR_TEX),
          inst -> ModRenderTypes.haloSmokishShader = inst
      );
      event.registerShader(
          new ShaderInstance(event.getResourceProvider(),
              new ResourceLocation(IC.MODID, "halo_split"),
              DefaultVertexFormat.POSITION_COLOR_TEX),
          inst -> ModRenderTypes.haloSplitShader = inst
      );
      event.registerShader(
          new ShaderInstance(event.getResourceProvider(),
              new ResourceLocation(IC.MODID, "halo_wavyfog"),
              DefaultVertexFormat.POSITION_COLOR_TEX),
          inst -> ModRenderTypes.haloWavyFogShader = inst
      );
      event.registerShader(
          new ShaderInstance(event.getResourceProvider(),
              new ResourceLocation(IC.MODID, "halo_wavypattern"),
              DefaultVertexFormat.POSITION_COLOR_TEX),
          inst -> ModRenderTypes.haloWavyPatternShader = inst
      );
    } catch (IOException e) {
      throw new RuntimeException("Failed to register halo shaders", e);
    }
  }
}