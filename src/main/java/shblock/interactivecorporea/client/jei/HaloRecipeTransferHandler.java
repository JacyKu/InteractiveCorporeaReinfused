package shblock.interactivecorporea.client.jei;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;

import java.util.List;
import java.util.Optional;

public class HaloRecipeTransferHandler implements IRecipeTransferHandler<DummyTransferringContainer, CraftingRecipe> {
  @Override
  public Class<DummyTransferringContainer> getContainerClass() {
    return DummyTransferringContainer.class;
  }

  @Override
  public Optional<MenuType<DummyTransferringContainer>> getMenuType() {
    return Optional.empty();
  }

  @Override
  public RecipeType<CraftingRecipe> getRecipeType() {
    return RecipeTypes.CRAFTING;
  }

  private static boolean shouldClose() {
    return RequestingHaloInterfaceHandler.getInterface() == null || RequestingHaloInterfaceHandler.getInterface().isOpenClose();
  }

  @Override
  public IRecipeTransferError transferRecipe(DummyTransferringContainer container, CraftingRecipe recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
    if (shouldClose()) {
      Minecraft.getInstance().setScreen(null);
      return null;
    }
    if (doTransfer) {
      boolean didChangeRecipe = false;
      List<IRecipeSlotView> inputSlots = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT);
      int limit = Math.min(9, inputSlots.size());
      for (int index = 0; index < limit; index++) {
        ItemStack stack = inputSlots.get(index).getDisplayedItemStack().orElse(ItemStack.EMPTY);
        if (RequestingHaloInterfaceHandler.getInterface().getCraftingInterface().tryPlaceShadowItem(index, stack)) {
          didChangeRecipe = true;
        }
      }

      if (didChangeRecipe) {
        RequestingHaloInterfaceHandler.getInterface().getCraftingInterface().updateRecipe();
      }

      container.shouldClose = true;
    }
    return null;
  }
}
