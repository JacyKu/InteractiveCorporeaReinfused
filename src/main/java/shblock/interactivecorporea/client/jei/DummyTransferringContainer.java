package shblock.interactivecorporea.client.jei;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class DummyTransferringContainer extends AbstractContainerMenu {
  public boolean shouldClose = false;

  protected DummyTransferringContainer() {
    super(null, 0);
  }

  @Override
  public boolean stillValid(Player playerIn) {
    return true;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    return ItemStack.EMPTY;
  }
}
