package shblock.interactivecorporea;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import shblock.interactivecorporea.client.particle.QuantizationParticleType;
import shblock.interactivecorporea.common.block.BlockItemQuantizationDevice;
import shblock.interactivecorporea.common.block.ModBlocks;
import shblock.interactivecorporea.common.crafting.RequestingHaloAddModuleRecipe;
import shblock.interactivecorporea.common.crafting.RequestingHaloRemoveModuleRecipe;
import shblock.interactivecorporea.common.item.ModItems;
import shblock.interactivecorporea.common.tile.ModTiles;

@Mod.EventBusSubscriber(modid = IC.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryHandler {
  private static final ResourceKey<CreativeModeTab> MAIN_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, id("interactive_corporea"));

  private static ResourceLocation id(String name) {
    return new ResourceLocation(IC.MODID, name);
  }

  @SubscribeEvent
  public static void onRegister(RegisterEvent event) {
    if (event.getRegistryKey().equals(Registries.ITEM)) {
      event.register(Registries.ITEM, id("requesting_halo"), () -> ModItems.requestingHalo);
      event.register(Registries.ITEM, id(BlockItemQuantizationDevice.NAME), () -> ModItems.BlockItems.itemQuantizationDevice);
    } else if (event.getRegistryKey().equals(Registries.CREATIVE_MODE_TAB)) {
      event.register(Registries.CREATIVE_MODE_TAB, MAIN_TAB_KEY.location(), () -> CreativeModeTab.builder()
          .title(Component.translatable("itemGroup.interactive_corporea"))
          .icon(() -> new ItemStack(ModItems.requestingHalo))
          .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
          .withSearchBar()
          .displayItems((parameters, output) -> {
            output.accept(ModItems.requestingHalo);
            output.accept(ModItems.BlockItems.itemQuantizationDevice);
          })
          .build());
    } else if (event.getRegistryKey().equals(Registries.BLOCK)) {
      event.register(Registries.BLOCK, id(BlockItemQuantizationDevice.NAME), () -> ModBlocks.itemQuantizationDevice);
    } else if (event.getRegistryKey().equals(Registries.BLOCK_ENTITY_TYPE)) {
      event.register(Registries.BLOCK_ENTITY_TYPE, id(BlockItemQuantizationDevice.NAME), () -> ModTiles.itemQuantizationDevice);
    } else if (event.getRegistryKey().equals(Registries.PARTICLE_TYPE)) {
      event.register(Registries.PARTICLE_TYPE, id("quantization"), () -> QuantizationParticleType.INSTANCE);
    } else if (event.getRegistryKey().equals(Registries.RECIPE_SERIALIZER)) {
      event.register(Registries.RECIPE_SERIALIZER, id("requesting_halo_add_module"), () -> RequestingHaloAddModuleRecipe.SERIALIZER);
      event.register(Registries.RECIPE_SERIALIZER, id("requesting_halo_remove_module"), () -> RequestingHaloRemoveModuleRecipe.SERIALIZER);
    } else if (event.getRegistryKey().equals(Registries.SOUND_EVENT)) {
      event.register(Registries.SOUND_EVENT, id("halo.open"), () -> ModSounds.haloOpen);
      event.register(Registries.SOUND_EVENT, id("halo.close"), () -> ModSounds.haloClose);
      event.register(Registries.SOUND_EVENT, id("halo.list_update"), () -> ModSounds.haloListUpdate);
      event.register(Registries.SOUND_EVENT, id("halo.select"), () -> ModSounds.haloSelect);
      event.register(Registries.SOUND_EVENT, id("halo.request"), () -> ModSounds.haloRequest);
      event.register(Registries.SOUND_EVENT, id("halo.reach_edge"), () -> ModSounds.haloReachEdge);
      event.register(Registries.SOUND_EVENT, id("halo.out_of_range"), () -> ModSounds.haloOutOfRange);
      event.register(Registries.SOUND_EVENT, id("quantum.send"), () -> ModSounds.quantumSend);
      event.register(Registries.SOUND_EVENT, id("quantum.receive"), () -> ModSounds.quantumReceive);
    }
  }
}
