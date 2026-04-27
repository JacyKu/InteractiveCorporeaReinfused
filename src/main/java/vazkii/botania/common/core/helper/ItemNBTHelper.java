package vazkii.botania.common.core.helper;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public final class ItemNBTHelper {
  private ItemNBTHelper() {
  }

  public static Tag get(ItemStack stack, String key) {
    return vazkii.botania.common.helper.ItemNBTHelper.get(stack, key);
  }

  public static int getInt(ItemStack stack, String key, int defaultValue) {
    return vazkii.botania.common.helper.ItemNBTHelper.getInt(stack, key, defaultValue);
  }

  public static String getString(ItemStack stack, String key, String defaultValue) {
    return vazkii.botania.common.helper.ItemNBTHelper.getString(stack, key, defaultValue);
  }

  public static ListTag getList(ItemStack stack, String key, int objtype, boolean create) {
    return vazkii.botania.common.helper.ItemNBTHelper.getList(stack, key, objtype, create);
  }

  public static void set(ItemStack stack, String key, Tag tag) {
    vazkii.botania.common.helper.ItemNBTHelper.set(stack, key, tag);
  }

  public static void setInt(ItemStack stack, String key, int value) {
    vazkii.botania.common.helper.ItemNBTHelper.setInt(stack, key, value);
  }

  public static void setString(ItemStack stack, String key, String value) {
    vazkii.botania.common.helper.ItemNBTHelper.setString(stack, key, value);
  }

  public static void setList(ItemStack stack, String key, ListTag value) {
    vazkii.botania.common.helper.ItemNBTHelper.set(stack, key, value);
  }

  public static boolean matchTag(Tag a, Tag b) {
    return Objects.equals(a, b);
  }
}