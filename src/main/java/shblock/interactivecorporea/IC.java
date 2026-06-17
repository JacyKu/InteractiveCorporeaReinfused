package shblock.interactivecorporea;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shblock.interactivecorporea.client.jei.DummyTransferringContainer;
import shblock.interactivecorporea.common.corporea.CorporeaUtil;
import shblock.interactivecorporea.common.network.ModPacketHandler;

@Mod(IC.MODID)
public class IC {
  public static final String MODID = "interactive_corporea";

  private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
  public static final RegistryObject<MenuType<DummyTransferringContainer>> DUMMY_TRANSFERRING = MENU_TYPES.register("dummy_transferring",
      () -> IForgeMenuType.create((windowId, inv, data) -> new DummyTransferringContainer(windowId, inv)));

  @OnlyIn(Dist.CLIENT)
  public static final String KEY_CATEGORY = "key.interactive_corporea.category";

  public IC() {
    MENU_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
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
