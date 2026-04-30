package shblock.interactivecorporea.common.item;

import net.minecraft.world.item.Item;
import shblock.interactivecorporea.IC;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.item.BotaniaItems;

import javax.annotation.Nullable;

public enum HaloModule {
  HUD(0, "hud", BotaniaItems.monocle),
  RECEIVE(1, "receive", BotaniaBlocks.lightRelayDefault.asItem()),
  SEARCH(2, "search", BotaniaItems.itemFinder),
  UPDATE(3, "update", BotaniaBlocks.hourglass.asItem()),
  MAGNATE(5, "magnate", BotaniaItems.magnetRing, 10),
  CRAFTING(6, "crafting", BotaniaItems.autocraftingHalo),
  QUANTUM_INSERTER(7, "quantum_inserter", BotaniaItems.lensWarp),
  FAR_REACH(8, "far_reach", BotaniaItems.reachRing, 20),
  GREATER_MAGNATE(9, "greater_magnate", BotaniaItems.magnetRingGreater, 30),
  BLACK_HOLE(10, "black_hole", BotaniaItems.blackHoleTalisman, 50);

  public final int bitMask;
  public final String translationKey;
  public final Item item;
  public final int rangeBonus;

  HaloModule(int bitIndex, String name, Item item) {
    this(bitIndex, name, item, 0);
  }

  HaloModule(int bitIndex, String name, Item item, int rangeBonus) {
    this.bitMask = 1 << bitIndex;
    this.translationKey = IC.MODID + ".halo_module." + name;
    this.item = item;
    this.rangeBonus = rangeBonus;
  }

  public boolean containsThis(int mask) {
    return (mask & bitMask) != 0;
  }

  public boolean isRangeModule() {
    return rangeBonus > 0;
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
