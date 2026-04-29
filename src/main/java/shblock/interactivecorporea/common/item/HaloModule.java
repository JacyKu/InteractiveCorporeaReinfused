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
  MAGNATE(5, "magnate", BotaniaItems.magnetRing),
  CRAFTING(6, "crafting", BotaniaItems.autocraftingHalo);

  public final int bitMask;
  public final String translationKey;
  public final Item item;

  HaloModule(int bitIndex, String name, Item item) {
    this.bitMask = 1 << bitIndex;
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
