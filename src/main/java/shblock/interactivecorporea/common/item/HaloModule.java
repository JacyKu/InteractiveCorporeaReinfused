package shblock.interactivecorporea.common.item;

import net.minecraft.world.item.Item;
import shblock.interactivecorporea.IC;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.item.BotaniaItems;

import javax.annotation.Nullable;

public enum HaloModule {
  HUD("hud", BotaniaItems.monocle),
  RECEIVE("receive", BotaniaBlocks.lightRelayDefault.asItem()),
  SEARCH("search", BotaniaItems.itemFinder),
  UPDATE("update", BotaniaBlocks.hourglass.asItem()),
  AMOUNT_SORT("amount_sort", BotaniaItems.corporeaSparkMaster, false),
  MAGNATE("magnate", BotaniaItems.magnetRing),
  CRAFTING("crafting", BotaniaItems.autocraftingHalo);

  public final int bitMask;
  public final String translationKey;
  public final Item item;
  private final boolean craftable;

  HaloModule(String name, Item item) {
    this(name, item, true);
  }

  HaloModule(String name, Item item, boolean craftable) {
    this.bitMask = 1 << ordinal();
    this.translationKey = IC.MODID + ".halo_module." + name;
    this.item = item;
    this.craftable = craftable;
  }

  public boolean containsThis(int mask) {
    return (mask & bitMask) != 0;
  }

  public Item getItem() {
    return item;
  }

  public boolean isCraftable() {
    return craftable;
  }

  @Nullable
  public static HaloModule fromItem(Item item) {
    for (HaloModule module : HaloModule.values()) {
      if (module.craftable && module.item.equals(item)) {
        return module;
      }
    }
    return null;
  }
}
