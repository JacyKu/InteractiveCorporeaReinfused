package shblock.interactivecorporea.common.tile;

import net.minecraft.world.level.block.entity.BlockEntityType;
import shblock.interactivecorporea.common.block.ModBlocks;

@SuppressWarnings("ConstantConditions")
public class ModTiles {
  public static final BlockEntityType<TileItemQuantizationDevice> itemQuantizationDevice = BlockEntityType.Builder.of(TileItemQuantizationDevice::new, ModBlocks.itemQuantizationDevice).build(null);
}
