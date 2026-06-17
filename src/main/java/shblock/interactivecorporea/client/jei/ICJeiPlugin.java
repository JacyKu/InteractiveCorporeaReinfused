package shblock.interactivecorporea.client.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@JeiPlugin
public class ICJeiPlugin implements IModPlugin {
  private static final ResourceLocation ID = new ResourceLocation(IC.MODID, "main");

  @Override
  public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    registration.addRecipeTransferHandler(new HaloRecipeTransferHandler(), RecipeTypes.CRAFTING);
  }

  @Override
  public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
    Object bookmarkSource = resolveBookmarkList(jeiRuntime);
    RequestingHaloInterfaceHandler.favoritesSupplier = () -> getBookmarkedStacks(jeiRuntime, bookmarkSource);

    Supplier<ItemStack> previous = RequestingHaloInterfaceHandler.jeiUnderMouseGetter;
    RequestingHaloInterfaceHandler.jeiUnderMouseGetter = () -> {
      Object o = jeiRuntime.getIngredientListOverlay().getIngredientUnderMouse();

      if (o == null && Minecraft.getInstance().screen == jeiRuntime.getRecipesGui()) {
        o = jeiRuntime.getRecipesGui().getIngredientUnderMouse(VanillaTypes.ITEM_STACK);
      }

      if (o == null) {
        o = jeiRuntime.getBookmarkOverlay().getIngredientUnderMouse();
      }

      if (o instanceof ItemStack) {
        return (ItemStack) o;
      }
      if (previous != null) {
        ItemStack fallback = previous.get();
        if (fallback != null) return fallback;
      }
      return null;
    };
  }

  private static Object resolveBookmarkList(IJeiRuntime jeiRuntime) {
    try {
      Object overlay = jeiRuntime.getBookmarkOverlay();
      for (Field field : getAllFields(overlay.getClass())) {
        field.setAccessible(true);
        Object value = field.get(overlay);
        if (value == null) continue;
        if (hasMethod(value.getClass(), "getIngredientList")) {
          return value;
        }
      }
    } catch (Exception ignored) {
    }
    return null;
  }

  private static List<ItemStack> getBookmarkedStacks(IJeiRuntime jeiRuntime, Object bookmarkSource) {
    List<ItemStack> result = new ArrayList<>();
    if (bookmarkSource == null) return result;
    try {
      Method m = bookmarkSource.getClass().getMethod("getIngredientList");
      List<?> ingredients = (List<?>) m.invoke(bookmarkSource);
      for (Object obj : ingredients) {
        if (obj instanceof ITypedIngredient<?> typed) {
          if (typed.getType() == VanillaTypes.ITEM_STACK) {
            result.add((ItemStack) typed.getIngredient());
          }
        }
      }
    } catch (Exception ignored) {
    }
    return result;
  }

  private static boolean hasMethod(Class<?> clazz, String methodName) {
    try {
      clazz.getMethod(methodName);
      return true;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

  private static List<Field> getAllFields(Class<?> clazz) {
    List<Field> fields = new ArrayList<>();
    Class<?> current = clazz;
    while (current != null && current != Object.class) {
      for (Field f : current.getDeclaredFields()) {
        fields.add(f);
      }
      current = current.getSuperclass();
    }
    return fields;
  }

  @Override
  public ResourceLocation getPluginUid() {
    return ID;
  }
}
