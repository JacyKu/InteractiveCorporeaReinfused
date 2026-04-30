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

import java.util.ArrayList;
import java.util.List;

public class RequestingHaloAddModuleRecipe extends CustomRecipe {
  public static final SimpleCraftingRecipeSerializer<RequestingHaloAddModuleRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(RequestingHaloAddModuleRecipe::new);

  public RequestingHaloAddModuleRecipe(ResourceLocation id, CraftingBookCategory category) {
    super(id, category);
  }

  @Override
  public boolean matches(CraftingContainer inv, Level world) {
    return !assembleInternal(inv).isEmpty();
  }

  @Override
  public ItemStack assemble(CraftingContainer inv, RegistryAccess registries) {
    return assembleInternal(inv);
  }

  private static ItemStack assembleInternal(CraftingContainer inv) {
    ItemStack halo = ItemStack.EMPTY;
    List<HaloModule> modules = new ArrayList<>();

    for (int i = 0; i < inv.getContainerSize(); i++) {
      ItemStack stack = inv.getItem(i);
      if (!stack.isEmpty()) {
        if (stack.getItem() instanceof ItemRequestingHalo) {
          if (!halo.isEmpty()) return ItemStack.EMPTY;
          halo = stack.copy();
        } else {
          HaloModule module = HaloModule.fromItem(stack.getItem());
          if (module == null) return ItemStack.EMPTY;
          modules.add(module);
        }
      }
    }

    if (halo.isEmpty() || modules.isEmpty()) return ItemStack.EMPTY;

    for (HaloModule module : modules) {
      if (!ItemRequestingHalo.installModule(halo, module)) {
        return ItemStack.EMPTY;
      }
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
