package shblock.interactivecorporea.client.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.client.jei.DummyTransferringContainer;
import shblock.interactivecorporea.client.jei.DummyTransferringGui;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterface;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;
import shblock.interactivecorporea.client.requestinghalo.crafting.HaloCraftingInterface;

import java.util.List;
import java.util.function.Supplier;

import static dev.emi.emi.api.EmiApi.getHoveredStack;

@EmiEntrypoint
public class ICEmiPlugin implements EmiPlugin {
  @Override
  public void register(EmiRegistry registry) {
    registry.addRecipeHandler(IC.DUMMY_TRANSFERRING.get(), new StandardRecipeHandler<DummyTransferringContainer>() {
      @Override
      public List<Slot> getInputSources(DummyTransferringContainer container) {
        return container.slots.subList(10, container.slots.size());
      }

      @Override
      public List<Slot> getCraftingSlots(DummyTransferringContainer container) {
        return container.slots.subList(1, 10);
      }

      @Override
      public Slot getOutputSlot(DummyTransferringContainer container) {
        return container.slots.get(0);
      }

      @Override
      public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING;
      }

      @Override
      public boolean canCraft(EmiRecipe recipe, EmiCraftContext<DummyTransferringContainer> context) {
        return supportsRecipe(recipe);
      }

      @Override
      public boolean craft(EmiRecipe recipe, EmiCraftContext<DummyTransferringContainer> context) {
        RequestingHaloInterface halo = RequestingHaloInterfaceHandler.getInterface();
        if (halo == null) return false;
        HaloCraftingInterface crafting = halo.getCraftingInterface();
        if (crafting == null) return false;

        List<EmiIngredient> inputs = recipe.getInputs();
        boolean changed = false;
        int limit = Math.min(9, inputs.size());
        for (int i = 0; i < limit; i++) {
          EmiIngredient ingredient = inputs.get(i);
          if (!ingredient.isEmpty()) {
            List<EmiStack> stacks = ingredient.getEmiStacks();
            if (!stacks.isEmpty()) {
              ItemStack stack = stacks.get(0).getItemStack().copy();
              stack.setCount(1);
              if (crafting.tryPlaceShadowItem(i, stack)) {
                changed = true;
              }
            }
          }
        }
        if (changed) {
          crafting.updateRecipe();
        }
        context.getScreenHandler().shouldClose = true;
        return true;
      }
    });

    registry.addExclusionArea(DummyTransferringGui.class, (screen, consumer) -> {
      consumer.accept(new Bounds(screen.getGuiLeft(), screen.getGuiTop(), screen.getXSize(), screen.getYSize()));
    });

    Supplier<ItemStack> previous = RequestingHaloInterfaceHandler.jeiUnderMouseGetter;
    RequestingHaloInterfaceHandler.jeiUnderMouseGetter = () -> {
      EmiStackInteraction interaction = getHoveredStack(true);
      if (interaction != null && !interaction.isEmpty()) {
        EmiIngredient ingredient = interaction.getStack();
        if (ingredient instanceof EmiStack emiStack) {
          return emiStack.getItemStack();
        }
      }
      if (previous != null) {
        ItemStack fallback = previous.get();
        if (fallback != null && !fallback.isEmpty()) {
          return fallback;
        }
      }
      return ItemStack.EMPTY;
    };
  }
}
