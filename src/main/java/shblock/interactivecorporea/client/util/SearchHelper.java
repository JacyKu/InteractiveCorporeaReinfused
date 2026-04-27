package shblock.interactivecorporea.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchHelper {
  private static final Minecraft mc = Minecraft.getInstance();

  public static boolean matchItem(ItemStack stack, String[] segments) {
    for (String seg : segments) {
      if (seg.length() == 0) continue;

      String subSeg = seg.substring(1);
      Item item = stack.getItem();
      switch (seg.charAt(0)) {
        case '@': // Mod Name
          String modid = item.getCreatorModId(stack);
          String modName = ModList.get().getModContainerById(modid)
              .map(modContainer -> modContainer.getModInfo().getDisplayName())
              .orElse(modid);
          if (!matchString(modName, subSeg)) {
            return false;
          } else {
            break;
          }
        case '#': // Tooltip
          List<Component> textComponents = stack.getTooltipLines(mc.player, mc.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL);
          boolean anyTooltipMatch = false;
          for (Component textComponent : textComponents) {
            if (matchString(textComponent.getString(), subSeg)) {
              anyTooltipMatch = true;
              break;
            }
          }
          if (!anyTooltipMatch) {
            return false;
          }
          break;
        case '$': // Tag
          Set<ResourceLocation> tags = new HashSet<>();
          item.builtInRegistryHolder().tags().map(tag -> tag.location()).forEach(tags::add);
          if (item instanceof BlockItem) {
            ((BlockItem) item).getBlock().builtInRegistryHolder().tags().map(tag -> tag.location()).forEach(tags::add);
          }
          boolean anyTagMatch = false;
          for (ResourceLocation tag : tags) {
            if (matchString(tag.toString(), subSeg)) {
              anyTagMatch = true;
              break;
            }
          }
          if (!anyTagMatch) {
            return false;
          }
          break;
        case '%': // Creative Tab
          boolean anyTabMatch = false;
          for (var group : BuiltInRegistries.CREATIVE_MODE_TAB) {
            if (group.shouldDisplay() && group.contains(stack) && matchString(group.getDisplayName().getString(), subSeg)) {
              anyTabMatch = true;
              break;
            }
          }
          if (!anyTabMatch) {
            return false;
          }
          break;
        case '&': // Resource Id
          ResourceLocation rid = BuiltInRegistries.ITEM.getKey(item);
          if (!matchString(rid.toString(), subSeg)) {
            return false;
          }
          break;
        default:
          if (!matchString(stack.getHoverName().getString(), subSeg)) {
            return false;
          }
          break;
      }
    }

    return true;
  }

  public static boolean matchString(@Nullable String text, @Nullable String filter) {
    if (text == null || filter == null) {
      return true;
    }
    return text.toLowerCase().contains(filter.toLowerCase());
  }
}
