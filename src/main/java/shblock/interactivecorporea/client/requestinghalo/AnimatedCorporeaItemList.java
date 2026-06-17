package shblock.interactivecorporea.client.requestinghalo;

import net.minecraft.world.item.ItemStack;
import shblock.interactivecorporea.client.util.SearchHelper;
import shblock.interactivecorporea.common.util.StackHelper;
import shblock.interactivecorporea.common.util.Vec2i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AnimatedCorporeaItemList {
  private static double animationLength = 10F;

  private int height = 5;
  private String filter = "";
  private SortMode sortMode = SortMode.AMOUNT;
  private List<ItemStack> stackList;
  private final List<AnimatedItemStack> animatedList = new ArrayList<>();
  private final Map<Integer, AnimatedItemStack> requestIdMap = new HashMap<>();

  private boolean isFirstUpdate = true;
  private int numColumns = 1;
  private Supplier<List<ItemStack>> favoritesSupplier = null;
  private final Set<ItemKey> favoriteKeys = new HashSet<>();

  public AnimatedCorporeaItemList(int height) {
    this.height = height;
  }

  public void update(double dt) {
    for (int i = animatedList.size() - 1; i >= 0; i--) {
      if (animatedList.get(i).update(dt)) {
        animatedList.remove(i);
      }
    }
  }

  public void tick() {
    for (int i = animatedList.size() - 1; i >= 0; i--) {
      animatedList.get(i).tick();
    }
  }

  public void setFavoritesSupplier(Supplier<List<ItemStack>> supplier) {
    this.favoritesSupplier = supplier;
  }

  public void showInitialFavorites() {
    if (favoritesSupplier != null) {
      stackList = mergeFavorites(List.of());
      arrange(true);
    }
  }

  public void refreshFavorites() {
    if (stackList != null) {
      handleUpdatePacket(stackList);
    }
  }

  public void handleUpdatePacket(List<ItemStack> itemList) {
    stackList = mergeFavorites(itemList);
    arrange(isFirstUpdate);
  }

  private List<ItemStack> mergeFavorites(List<ItemStack> source) {
    favoriteKeys.clear();
    if (favoritesSupplier == null) return source;
    List<ItemStack> favs = favoritesSupplier.get();
    if (favs == null || favs.isEmpty()) return source;

    Set<ItemKey> sourceKeys = new HashSet<>();
    for (ItemStack s : source) {
      sourceKeys.add(new ItemKey(s));
    }

    List<ItemStack> merged = new ArrayList<>(source);
    for (ItemStack fav : favs) {
      ItemKey key = new ItemKey(fav);
      favoriteKeys.add(key);
      if (!sourceKeys.contains(key)) {
        ItemStack copy = fav.copy();
        copy.setCount(0);
        merged.add(copy);
      }
    }
    return merged;
  }

  private static class ItemKey {
    private final ItemStack stack;
    ItemKey(ItemStack s) { this.stack = s; }
    @Override
    public boolean equals(Object o) {
      if (!(o instanceof ItemKey)) return false;
      return StackHelper.equalItemAndTag(stack, ((ItemKey) o).stack);
    }
    @Override
    public int hashCode() {
      int result = stack.getItem().hashCode();
      if (stack.hasTag()) {
        result = 31 * result + stack.getTag().hashCode();
      }
      return result;
    }
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }

  public boolean hasItemsMatchingFilter() {
    return stackList == null || !filter(stackList).isEmpty();
  }

  private List<ItemStack> filter(List<ItemStack> list) {
    String[] segments = filter.split(" ");

    return list.stream().filter(
        stack -> SearchHelper.matchItem(stack, segments)
    ).collect(Collectors.toList());
  }

  private void sort() {
    animatedList.sort((a, b) -> {
      if (a.isRemoved() && !b.isRemoved()) {
        return 1;
      }
      if (b.isRemoved() && !a.isRemoved()) {
        return -1;
      }
      if (a.isRemoved() && b.isRemoved()) {
        return 0;
      }
      return sortMode.getSorter().compare(a.getStack(), b.getStack());
    });

    layoutInCurrentOrder();
  }

  private void layoutInCurrentOrder() {
    int y = 0;
    int x = 0;
    int maxX = 0;
    for (AnimatedItemStack stack : animatedList) {
      if (stack.isRemoved()) continue;
      maxX = x;
      if (!stack.isNew()) {
        stack.moveTo(x, y);
      } else {
        stack.setPos(x, y);
      }
      y++;
      if (y >= height) {
        y = 0;
        x++;
      }
    }
    numColumns = maxX + 1;
  }

  public double getColumnOffset() {
    return (numColumns - 1) / 2.0;
  }

  public void sortByAmount() {
    sortMode = SortMode.AMOUNT;
    sort();
  }

  private void sortAndLayout() {
    sort();
  }

  public void arrange() {
    arrange(true);
  }

  private void arrange(boolean updateOrder) {
    if (stackList == null) {
      return;
    }

    List<ItemStack> list = filter(stackList);
    for (AnimatedItemStack aniStack : animatedList) {
      ItemStack oldStack = aniStack.getStack();
      boolean found = false;
      for (int i = list.size() - 1; i >= 0; i--) {
        ItemStack stack = list.get(i);

        if (StackHelper.equalItemAndTag(oldStack, stack)) {
          if (aniStack.isRemoved()) {
            aniStack.fadeIn();
          }
          if (oldStack.getCount() != stack.getCount()) {
            aniStack.changeAmount(stack.getCount(), animationLength);
          }
          aniStack.setFavorite(favoriteKeys.contains(new ItemKey(stack)));
          list.remove(i);
          found = true;
          break;
        }
      }
      if (!found) {
        aniStack.removeWithAnimation();
      }
    }

    for (ItemStack stack : list) {
      AnimatedItemStack aniStack = new AnimatedItemStack(stack);
      if (favoriteKeys.contains(new ItemKey(stack))) {
        aniStack.setFavorite(true);
      }
      if (!isFirstUpdate) {
        aniStack.fadeIn();
      }
      animatedList.add(aniStack);
    }

    if (updateOrder) {
      sortAndLayout();
    } else {
      layoutInCurrentOrder();
    }

    isFirstUpdate = false;
  }

  public void removeAll() {
    for (AnimatedItemStack stack : animatedList) {
      stack.remove();
    }
  }

  private int nextRequestId = 0;

  public int onRequest(AnimatedItemStack aniStack) {
    int id = nextRequestId;
    requestIdMap.put(id, aniStack);
    nextRequestId++;

    return id;
  }

  public void handleRequestResultPacket(int requestId, int successAmount) {
    AnimatedItemStack aniStack = requestIdMap.get(requestId);
    if (aniStack != null && !aniStack.isRemoved()) {
      aniStack.handleRequestResult(successAmount);
      if (successAmount > 0) {
        aniStack.shrinkAmount(successAmount, animationLength);
        if (aniStack.getStack().getCount() <= 0) {
          aniStack.removeWithAnimation();
          layoutInCurrentOrder();
        }
      }
    }
    requestIdMap.remove(requestId);
  }

  public List<AnimatedItemStack> getAnimatedList() {
    return animatedList;
  }

  public void changeHeight(int delta) {
    height += delta;
    if (height < 1) {
      height = 1;
    }
    if (height > 16) {
      height = 16;
    }
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getHeight() {
    return height;
  }

  public void setSortMode(SortMode mode) {
    sortMode = mode;
  }
}
