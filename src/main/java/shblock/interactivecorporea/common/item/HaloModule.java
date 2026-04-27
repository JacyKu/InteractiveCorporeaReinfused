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
  AMOUNT_SORT("amount_sort", BotaniaItems.corporeaSparkMaster),
  MAGNATE("magnate", BotaniaItems.magnetRing),
  CRAFTING("crafting", BotaniaItems.autocraftingHalo);

  public final int bitMask;
  public final String translationKey;
  public final Item item;

  HaloModule(String name, Item item) {
    this.bitMask = 1 << ordinal();
    this.translationKey = IC.MODID + ".halo_module." + name;
    this.item = item;
  }

  public boolean containsThis(int mask) {
    return (mask & bitMask) != 0;
  }

  public Item getItem() {
    return item;
  }

  @Nullable
  public static HaloModule fromItem(Item item) {
    for (HaloModule module : HaloModule.values()) {
      if (module.item.equals(item)) {
        return module;
      }
    }
    return null;
  }
}
