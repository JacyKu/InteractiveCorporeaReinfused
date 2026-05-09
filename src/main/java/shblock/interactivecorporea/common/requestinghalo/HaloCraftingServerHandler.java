package shblock.interactivecorporea.common.requestinghalo;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;
import vazkii.botania.api.corporea.CorporeaResult;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.common.corporea.CorporeaUtil;
import shblock.interactivecorporea.common.item.HaloModule;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import shblock.interactivecorporea.common.network.CPacketRequestItemListUpdate;
import shblock.interactivecorporea.common.tile.TileItemQuantizationDevice;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.ItemListHelper;
import shblock.interactivecorporea.common.util.StackHelper;
import vazkii.botania.common.impl.corporea.CorporeaItemStackMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = IC.MODID)
public class HaloCraftingServerHandler {
  public static boolean doCraft(ServerPlayer player, CISlotPointer haloSlot, int requestId) {
    ItemStack halo = haloSlot.getStack(player);
    if (!(halo.getItem() instanceof ItemRequestingHalo) || !ItemRequestingHalo.isModuleInstalled(halo, HaloModule.CRAFTING)) {
      return false;
    }

    CraftingContainer shadowCraftingInventory = createCraftingInventory(halo, true);
    Optional<CraftingRecipe> shadowRecipe = player.serverLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, shadowCraftingInventory, player.serverLevel());
    if (shadowRecipe.isEmpty()) {
      return false;
    }

    boolean haloChanged = fillMissingIngredients(player, halo);

    CraftingContainer actualCraftingInventory = createCraftingInventory(halo, false);
    Optional<CraftingRecipe> recipe = player.serverLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, actualCraftingInventory, player.serverLevel());
    if (recipe.isEmpty()) {
      if (haloChanged) {
        refreshHaloState(player, halo);
      }
      return false;
    }

    CraftingRecipe actualRecipe = recipe.get();
    ItemStack output = actualRecipe.assemble(actualCraftingInventory, player.serverLevel().registryAccess());
    if (output.isEmpty()) {
      if (haloChanged) {
        refreshHaloState(player, halo);
      }
      return false;
    }

    consumeCraftingIngredients(player, halo, actualCraftingInventory, actualRecipe.getRemainingItems(actualCraftingInventory));
    ItemHandlerHelper.giveItemToPlayer(player, output.copy());
    player.getInventory().setChanged();
    refreshHaloState(player, halo);
    return true;
  }

  @SubscribeEvent
  public static void onTick(TickEvent.ServerTickEvent event) {

  }

  private static CraftingContainer createCraftingInventory(ItemStack halo, boolean useShadowStacks) {
    CraftingContainer craftingInventory = new TransientCraftingContainer(new AbstractContainerMenu(MenuType.CRAFTING, -1) {
      @Override
      public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
      }

      @Override
      public boolean stillValid(Player player) {
        return false;
      }
    }, 3, 3);

    for (int i = 0; i < 9; i++) {
      ItemStack stack = useShadowStacks
          ? ItemRequestingHalo.getShadowStackInCraftingSlot(halo, i)
          : ItemRequestingHalo.getStackInCraftingSlot(halo, i);
      if (stack.isEmpty()) {
        continue;
      }
      craftingInventory.setItem(i, stack.copyWithCount(1));
    }

    return craftingInventory;
  }

  private static boolean fillMissingIngredients(ServerPlayer player, ItemStack halo) {
    List<ItemStack> missingInputs = new ArrayList<>();
    for (int i = 0; i < 9; i++) {
      ItemStack realStack = ItemRequestingHalo.getStackInCraftingSlot(halo, i);
      ItemStack shadowStack = ItemRequestingHalo.getShadowStackInCraftingSlot(halo, i);
      if (!realStack.isEmpty() || shadowStack.isEmpty()) {
        continue;
      }
      ItemListHelper.addToListCompacted(missingInputs, shadowStack.copyWithCount(1));
    }

    if (missingInputs.isEmpty()) {
      return false;
    }
    if (!ItemRequestingHalo.canPlayerAccessNetwork(player, halo)) {
      return false;
    }

    TileItemQuantizationDevice device = getBoundDevice(player, halo);
    if (device == null || device.getSpark() == null) {
      return false;
    }

    int totalRequestedItems = 0;
    for (ItemStack requestStack : missingInputs) {
      totalRequestedItems += requestStack.getCount();
      CorporeaResult previewResult = CorporeaUtil.requestItemNoIntercept(
          new CorporeaItemStackMatcher(requestStack, true),
          requestStack.getCount(),
          device.getSpark(),
          player,
          false
      );
      if (previewResult.matchedCount() < requestStack.getCount()) {
        return false;
      }
    }

    if (device.getExtractManaCost(totalRequestedItems) > device.getCurrentMana()) {
      return false;
    }

    boolean changed = false;
    for (ItemStack requestStack : missingInputs) {
      CorporeaResult result = CorporeaUtil.requestItemNoIntercept(
          new CorporeaItemStackMatcher(requestStack, true),
          requestStack.getCount(),
          device.getSpark(),
          player,
          true
      );
      int extractedCount = result.extractedCount();
      if (extractedCount <= 0) {
        return changed;
      }

      device.consumeMana(device.getExtractManaCost(extractedCount));
      changed = true;
      fillMatchingEmptySlots(halo, requestStack, extractedCount);

      if (extractedCount < requestStack.getCount()) {
        return true;
      }
    }
    return changed;
  }

  private static void fillMatchingEmptySlots(ItemStack halo, ItemStack requestedStack, int extractedCount) {
    ItemStack singleItem = requestedStack.copyWithCount(1);
    int remaining = extractedCount;
    for (int i = 0; i < 9 && remaining > 0; i++) {
      if (!ItemRequestingHalo.getStackInCraftingSlot(halo, i).isEmpty()) {
        continue;
      }
      ItemStack shadowStack = ItemRequestingHalo.getShadowStackInCraftingSlot(halo, i);
      if (!StackHelper.equalItemAndTag(shadowStack, singleItem)) {
        continue;
      }
      ItemRequestingHalo.setStackInCraftingSlot(halo, i, singleItem.copy());
      remaining--;
    }
  }

  private static void consumeCraftingIngredients(ServerPlayer player, ItemStack halo, CraftingContainer craftingInventory, NonNullList<ItemStack> remainingItems) {
    for (int i = 0; i < 9; i++) {
      if (craftingInventory.getItem(i).isEmpty()) {
        continue;
      }

      ItemStack currentStack = ItemRequestingHalo.getStackInCraftingSlot(halo, i).copy();
      if (!currentStack.isEmpty()) {
        currentStack.shrink(1);
        if (currentStack.getCount() <= 0) {
          currentStack = ItemStack.EMPTY;
        }
      }

      ItemStack remainingItem = remainingItems.get(i);
      if (!remainingItem.isEmpty()) {
        remainingItem = remainingItem.copy();
        if (currentStack.isEmpty()) {
          currentStack = remainingItem;
        } else if (StackHelper.equalItemAndTag(currentStack, remainingItem)
            && currentStack.getCount() + remainingItem.getCount() <= currentStack.getMaxStackSize()) {
          currentStack.grow(remainingItem.getCount());
        } else {
          ItemHandlerHelper.giveItemToPlayer(player, remainingItem);
        }
      }

      ItemRequestingHalo.setStackInCraftingSlot(halo, i, currentStack);
    }
  }

  private static void refreshHaloState(ServerPlayer player, ItemStack halo) {
    CPacketRequestItemListUpdate.sendItemListToPlayer(player, halo);
    CPacketRequestItemListUpdate.broadcastRemoteState(player, halo);
  }

  private static TileItemQuantizationDevice getBoundDevice(ServerPlayer player, ItemStack halo) {
    GlobalPos pos = ItemRequestingHalo.getBoundSenderPosition(halo);
    if (pos == null) {
      return null;
    }

    Level world = player.server.getLevel(pos.dimension());
    if (world == null) {
      return null;
    }

    BlockEntity blockEntity = world.getBlockEntity(pos.pos());
    return blockEntity instanceof TileItemQuantizationDevice device ? device : null;
  }
}
