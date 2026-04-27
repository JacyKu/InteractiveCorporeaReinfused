package shblock.interactivecorporea.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class BlockItemWormholeProjector extends Block {
  private static final EnumProperty<Type> TYPE = EnumProperty.create("type", Type.class);

  public BlockItemWormholeProjector() {
    super(BlockBehaviour.Properties.of().strength(2, 10).sound(SoundType.STONE));
    registerDefaultState(stateDefinition.any().setValue(TYPE, Type.BOTTOM));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(TYPE);
  }

  public enum Type implements StringRepresentable {
    TOP("top"),
    BOTTOM("bottom");

    private final String name;

    Type(String name) {
      this.name = name;
    }

    public String toString() {
      return this.name;
    }

    public String getString() {
      return this.name;
    }

    @Override
    public String getSerializedName() {
      return this.name;
    }
  }
}
