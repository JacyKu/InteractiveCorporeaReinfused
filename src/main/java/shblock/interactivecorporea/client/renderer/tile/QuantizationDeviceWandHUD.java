package shblock.interactivecorporea.client.renderer.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import shblock.interactivecorporea.common.block.ModBlocks;
import shblock.interactivecorporea.common.tile.TileItemQuantizationDevice;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.block.WandHUD;

public class QuantizationDeviceWandHUD implements WandHUD {
  private final TileItemQuantizationDevice tile;
  private static final ItemStack DISPLAY_STACK = new ItemStack(ModBlocks.itemQuantizationDevice);

  public QuantizationDeviceWandHUD(TileItemQuantizationDevice tile) {
    this.tile = tile;
  }

  @Override
  public void renderHUD(GuiGraphics gui, Minecraft mc) {
    String name = DISPLAY_STACK.getHoverName().getString();
    BotaniaAPIClient.instance().drawSimpleManaHUD(gui, 0x00BFFF,
        tile.getCurrentMana(), tile.getManaCapacity(), name);
  }
}
