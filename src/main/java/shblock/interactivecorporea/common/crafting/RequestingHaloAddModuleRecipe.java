package shblock.interactivecorporea.common.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import shblock.interactivecorporea.common.item.HaloModule;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;

public class RequestingHaloAddModuleRecipe extends CustomRecipe {
  public static final SimpleCraftingRecipeSerializer<RequestingHaloAddModuleRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(RequestingHaloAddModuleRecipe::new);

  public RequestingHaloAddModuleRecipe(ResourceLocation id, CraftingBookCategory category) {
    super(id, category);
  }

  @Override
  public boolean matches(CraftingContainer inv, Level world) {
    boolean foundHalo = false;
    boolean foundModule = false;

    for (int i = 0; i < inv.getContainerSize(); i++) {
      ItemStack stack = inv.getItem(i);
      if (!stack.isEmpty()) {
        if (stack.getItem() instanceof ItemRequestingHalo) {
          if (foundHalo) return false;
          foundHalo = true;
        } else {
          if (HaloModule.fromItem(stack.getItem()) != null) {
            if (foundModule) return false;
            foundModule = true;
          } else {
            return false;
          }
        }
      }
    }

    return foundHalo && foundModule;
  }

  @Override
  public ItemStack assemble(CraftingContainer inv, RegistryAccess registries) {
    ItemStack halo = null;
    HaloModule module = null;

    for (int i = 0; i < inv.getContainerSize(); i++) {
      ItemStack stack = inv.getItem(i);
      if (!stack.isEmpty()) {
        if (stack.getItem() instanceof ItemRequestingHalo) {
          if (halo != null) return ItemStack.EMPTY;
          halo = stack.copy();
        } else {
          HaloModule m = HaloModule.fromItem(stack.getItem());
          if (m != null) {
            if (module != null) return ItemStack.EMPTY;
            module = m;
          } else {
            return ItemStack.EMPTY;
          }
        }
      }
    }

    if (halo == null || module == null) return ItemStack.EMPTY;

    if (!ItemRequestingHalo.installModule(halo, module)) {
      return ItemStack.EMPTY;
    }

    return halo;
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return width * height >= 2;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return SERIALIZER;
  }
}
