package shblock.interactivecorporea;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import shblock.interactivecorporea.common.corporea.CorporeaUtil;
import shblock.interactivecorporea.common.network.ModPacketHandler;

@Mod(IC.MODID)
public class IC {
  public static final String MODID = "interactive_corporea";

  @OnlyIn(Dist.CLIENT)
  public static final String KEY_CATEGORY = "key.interactive_corporea.category";

  public IC() {
    FMLJavaModLoadingContext.get().getModEventBus().addListener(IC::setup);

    ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, shblock.interactivecorporea.ModConfig.CLIENT_SPEC);
    ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, shblock.interactivecorporea.ModConfig.COMMON_SPEC);
  }

  private static void setup(final FMLCommonSetupEvent event) {
    event.enqueueWork(() -> {
      ModPacketHandler.init();
      CorporeaUtil.init();
    });
  }
}
