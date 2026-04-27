package shblock.interactivecorporea.common.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import shblock.interactivecorporea.common.item.HaloModule;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import vazkii.botania.common.crafting.recipe.RecipeUtils;

public class RequestingHaloRemoveModuleRecipe extends CustomRecipe {
  public static final SimpleCraftingRecipeSerializer<RequestingHaloRemoveModuleRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(RequestingHaloRemoveModuleRecipe::new);

  public RequestingHaloRemoveModuleRecipe(ResourceLocation id, CraftingBookCategory category) {
    super(id, category);
  }

  @Override
  public boolean matches(CraftingContainer inv, Level world) {
    boolean foundHalo = false;

    for (int i = 0; i < inv.getContainerSize(); i++) {
      ItemStack stack = inv.getItem(i);
      if (!stack.isEmpty()) {
        if (stack.getItem() instanceof ItemRequestingHalo) {
          if (!ItemRequestingHalo.isAnyModuleInstalled(stack)) return false;
          if (foundHalo) return false;
          foundHalo = true;
        } else {
          return false;
        }
      }
    }

    return foundHalo;
  }

  @Override
  public ItemStack assemble(CraftingContainer inv, RegistryAccess registries) {
    ItemStack halo = null;

    for (int i = 0; i < inv.getContainerSize(); i++) {
      ItemStack stack = inv.getItem(i);
      if (!stack.isEmpty()) {
        if (stack.getItem() instanceof ItemRequestingHalo) {
          if (!ItemRequestingHalo.isAnyModuleInstalled(stack)) return ItemStack.EMPTY;
          if (halo != null) return ItemStack.EMPTY;
          halo = stack;
        } else {
          return ItemStack.EMPTY;
        }
      }
    }

    if (halo == null) return ItemStack.EMPTY;

    for (HaloModule module : HaloModule.values()) {
      if (ItemRequestingHalo.isModuleInstalled(halo, module)) {
        return new ItemStack(module.getItem());
      }
    }

    return ItemStack.EMPTY;
  }

  @Override
  public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
    return RecipeUtils.getRemainingItemsSub(inv,
        s -> {
            if (s.getItem() instanceof ItemRequestingHalo) {
            ItemStack hs = s.copy();
            for (HaloModule module : HaloModule.values()) {
              if (ItemRequestingHalo.uninstallModule(hs, module)) {
                return hs;
              }
            }
          }
          return null;
        });
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return width + height > 0;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return SERIALIZER;
  }
}
