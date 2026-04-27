package shblock.interactivecorporea.common.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.common.block.ModBlocks;

public class ModItems {
  public static final ItemRequestingHalo requestingHalo = new ItemRequestingHalo();

  public static class BlockItems {
    public static final BlockItem itemQuantizationDevice = new BlockItem(ModBlocks.itemQuantizationDevice, new Item.Properties());
//        public static final BlockItem itemWormholeProjector = new BlockItem(ModBlocks.itemWormholeProjector, new Item.Properties().group(IC.ITEM_GROUP));
  }
}
