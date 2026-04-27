package shblock.interactivecorporea.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.SoundType;
import shblock.interactivecorporea.common.tile.TileItemQuantizationDevice;
import shblock.interactivecorporea.common.tile.ModTiles;

public class BlockItemQuantizationDevice extends Block implements EntityBlock {
  public static final String NAME = "item_quantization_device";

  public BlockItemQuantizationDevice() {
    super(Properties.of().strength(5.5F).sound(SoundType.METAL).noOcclusion());
  }

  @Override
  public boolean hasAnalogOutputSignal(BlockState state) {
    return true;
  }

  @Override
  public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
    BlockEntity te = world.getBlockEntity(pos);
    if (te instanceof TileItemQuantizationDevice) {
      TileItemQuantizationDevice iqd = (TileItemQuantizationDevice) te;
      return iqd.getComparatorLevel();
    }
    return 0;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new TileItemQuantizationDevice(pos, state);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
    if (!level.isClientSide && type == ModTiles.itemQuantizationDevice) {
      return (lvl, pos, blockState, blockEntity) -> TileItemQuantizationDevice.serverTick(lvl, pos, blockState, (TileItemQuantizationDevice) blockEntity);
    }
    return null;
  }
}
