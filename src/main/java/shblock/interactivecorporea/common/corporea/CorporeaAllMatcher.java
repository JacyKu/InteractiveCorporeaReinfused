package shblock.interactivecorporea.common.corporea;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import vazkii.botania.api.corporea.CorporeaRequestMatcher;

public class CorporeaAllMatcher implements CorporeaRequestMatcher {
  @Override
  public boolean test(ItemStack stack) {
    return true;
  }

  @Override
  public Component getRequestName() {
    return Component.literal("internal request");
  }
}
