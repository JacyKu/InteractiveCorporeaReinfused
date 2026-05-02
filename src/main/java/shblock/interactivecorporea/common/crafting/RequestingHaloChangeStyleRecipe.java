package shblock.interactivecorporea.common.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import shblock.interactivecorporea.common.item.HaloInterfaceStyle;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;

public class RequestingHaloChangeStyleRecipe extends CustomRecipe {
  public static final SimpleCraftingRecipeSerializer<RequestingHaloChangeStyleRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(RequestingHaloChangeStyleRecipe::new);

  public RequestingHaloChangeStyleRecipe(ResourceLocation id, CraftingBookCategory category) {
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
    HaloInterfaceStyle style = null;
    net.minecraft.world.item.DyeColor tintDye = null;

    for (int i = 0; i < inv.getContainerSize(); i++) {
      ItemStack stack = inv.getItem(i);
      if (stack.isEmpty()) continue;

      if (stack.getItem() instanceof ItemRequestingHalo) {
        if (!halo.isEmpty()) return ItemStack.EMPTY;
        halo = stack.copy();
      } else {
        HaloInterfaceStyle flowerStyle = HaloInterfaceStyle.fromFlower(stack);
        if (flowerStyle != null) {
          if (style != null || tintDye != null) return ItemStack.EMPTY;
          style = flowerStyle;
          continue;
        }
        net.minecraft.world.item.DyeColor dye = HaloInterfaceStyle.dyeColorFromPetal(stack);
        if (dye != null) {
          if (tintDye != null || style != null) return ItemStack.EMPTY;
          tintDye = dye;
          continue;
        }
        HaloInterfaceStyle petalStyle = HaloInterfaceStyle.fromPetal(stack);
        if (petalStyle != null) {
          if (style != null || tintDye != null) return ItemStack.EMPTY;
          style = petalStyle;
          continue;
        }
        return ItemStack.EMPTY;
      }
    }

    if (halo.isEmpty() || (style == null && tintDye == null)) return ItemStack.EMPTY;

    boolean styleChanges = style != null && ItemRequestingHalo.getInterfaceStyle(halo) != style;
    boolean tintChanges  = tintDye != null;
    if (!styleChanges && !tintChanges) return ItemStack.EMPTY;

    if (styleChanges) ItemRequestingHalo.setInterfaceStyle(halo, style);
    if (tintChanges)  ItemRequestingHalo.setHaloTintFromDye(halo, tintDye);
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