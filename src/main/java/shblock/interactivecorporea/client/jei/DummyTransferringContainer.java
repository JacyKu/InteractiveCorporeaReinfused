package shblock.interactivecorporea.client.jei;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterface;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;
import shblock.interactivecorporea.client.requestinghalo.crafting.HaloCraftingInterface;

import javax.annotation.Nullable;

public class DummyTransferringContainer extends AbstractContainerMenu {
  private static final int OFF_SCREEN = -10000;

  public boolean shouldClose = false;

  public DummyTransferringContainer(Inventory playerInventory) {
    this(IC.DUMMY_TRANSFERRING.get(), 0, playerInventory);
  }

  public DummyTransferringContainer(int containerId, Inventory playerInventory) {
    this(IC.DUMMY_TRANSFERRING.get(), containerId, playerInventory);
  }

  private DummyTransferringContainer(@Nullable MenuType<?> type, int containerId, @Nullable Inventory playerInventory) {
    super(type, containerId);

    SimpleContainer craftInv = new SimpleContainer(9);
    addSlot(new Slot(craftInv, 0, OFF_SCREEN, OFF_SCREEN) {
      @Override
      public boolean mayPlace(ItemStack stack) {
        return false;
      }
    });
    for (int i = 1; i <= 9; i++) {
      final int idx = i - 1;
      addSlot(new Slot(craftInv, i, OFF_SCREEN, OFF_SCREEN) {
        @Override
        public void set(ItemStack stack) {
          super.set(stack);
          RequestingHaloInterface halo = RequestingHaloInterfaceHandler.getInterface();
          if (halo != null) {
            HaloCraftingInterface crafting = halo.getCraftingInterface();
            if (crafting != null) {
              crafting.tryPlaceShadowItem(idx, stack);
              crafting.updateRecipe();
            }
          }
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
          return true;
        }
      });
    }

    if (playerInventory != null) {
      for (int row = 0; row < 3; row++) {
        for (int col = 0; col < 9; col++) {
          addSlot(new Slot(playerInventory, col + row * 9 + 9, OFF_SCREEN, OFF_SCREEN));
        }
      }
      for (int col = 0; col < 9; col++) {
        addSlot(new Slot(playerInventory, col, OFF_SCREEN, OFF_SCREEN));
      }
    }
  }

  protected DummyTransferringContainer() {
    this(null, 0, null);
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
