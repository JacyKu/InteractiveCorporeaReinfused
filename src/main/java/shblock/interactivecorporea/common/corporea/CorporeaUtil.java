package shblock.interactivecorporea.common.corporea;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.common.util.ItemListHelper;
import vazkii.botania.api.corporea.*;
import vazkii.botania.common.impl.corporea.CorporeaHelperImpl;
import vazkii.botania.common.impl.corporea.CorporeaRequestImpl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CorporeaUtil {
  private static final CorporeaAllMatcher ALL_MATCHER = new CorporeaAllMatcher();
  private static final double SPARK_SCAN_RANGE = 8D;
  private static final double BROAD_SPARK_SCAN_RANGE = 64D;

  public static void init() {
    CorporeaHelper.instance().registerRequestMatcher(new ResourceLocation(IC.MODID, "all"), CorporeaAllMatcher.class, nbt -> new CorporeaAllMatcher());
  }

  public static CorporeaSpark resolveRequestSpark(CorporeaSpark spark) {
    ensureNetworkReady(spark);
    if (spark.isMaster() || spark.getMaster() != null) {
      return spark;
    }

    CorporeaSpark resolved = spark;
    double bestDistance = Double.MAX_VALUE;
    AABB searchBox = spark.entity().getBoundingBox().inflate(SPARK_SCAN_RANGE);
    for (Entity entity : spark.entity().level().getEntitiesOfClass(Entity.class, searchBox, other -> other instanceof CorporeaSpark)) {
      CorporeaSpark otherSpark = (CorporeaSpark) entity;
      if (otherSpark == spark || !otherSpark.entity().isAlive() || otherSpark.getNetwork() != spark.getNetwork()) {
        continue;
      }

      ensureNetworkReady(otherSpark);
      CorporeaSpark candidate = otherSpark.isMaster() ? otherSpark : otherSpark.getMaster();
      if (candidate == null || !candidate.entity().isAlive()) {
        continue;
      }

      double distance = spark.entity().distanceToSqr(candidate.entity());
      if (distance < bestDistance) {
        bestDistance = distance;
        resolved = candidate;
      }
    }

    return resolved;
  }

  private static boolean ensureNetworkReady(CorporeaSpark spark) {
    CorporeaSpark master = spark.isMaster() ? spark : spark.getMaster();
    if (master == null || !master.entity().isAlive()) {
      return false;
    }

    Set<CorporeaSpark> connections = master.getConnections();
    if (master.getMaster() != null && connections != null && !connections.isEmpty()) {
      return false;
    }

    Set<CorporeaSpark> network = new LinkedHashSet<>();
    network.add(master);
    master.introduceNearbyTo(network, master);
    clearCorporeaNetworkCache();
    return true;
  }

  private static void clearCorporeaNetworkCache() {
    if (CorporeaHelper.instance() instanceof CorporeaHelperImpl helper) {
      helper.clearCache();
    }
  }

  public static Set<CorporeaNode> getNodesOnNetworkCompat(CorporeaSpark spark) {
    CorporeaSpark resolvedSpark = resolveRequestSpark(spark);
    Set<CorporeaNode> nodes = CorporeaHelper.instance().getNodesOnNetwork(resolvedSpark);
    if (!nodes.isEmpty()) {
      return nodes;
    }

    return collectNodesByProximity(resolvedSpark);
  }

  public static boolean isEntityInRangeOfNetwork(Entity entity, GlobalPos networkPos, double range) {
    if (entity == null || networkPos == null || !entity.level().dimension().equals(networkPos.dimension())) {
      return false;
    }

    CorporeaSpark spark = CorporeaHelper.instance().getSparkForBlock(entity.level(), networkPos.pos());
    return spark != null && isEntityInRangeOfNetwork(entity, spark, range);
  }

  public static boolean isEntityInRangeOfNetwork(Entity entity, CorporeaSpark spark, double range) {
    if (entity == null || spark == null || entity.level() != spark.entity().level()) {
      return false;
    }

    double rangeSqr = range * range;
    CorporeaSpark resolvedSpark = resolveRequestSpark(spark);
    if (spark.entity().distanceToSqr(entity) <= rangeSqr || resolvedSpark.entity().distanceToSqr(entity) <= rangeSqr) {
      return true;
    }

    for (CorporeaSpark networkSpark : collectSparksByProximity(resolvedSpark)) {
      if (networkSpark.entity().distanceToSqr(entity) <= rangeSqr) {
        return true;
      }
    }

    for (CorporeaNode node : getNodesOnNetworkCompat(resolvedSpark)) {
      if (node.getWorld() == entity.level() && isEntityInRangeOfNode(entity, node, rangeSqr)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isEntityInRangeOfNode(Entity entity, CorporeaNode node, double rangeSqr) {
    BlockPos pos = node.getPos();
    return entity.distanceToSqr(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D) <= rangeSqr;
  }

  private static Set<CorporeaSpark> collectSparksByProximity(CorporeaSpark rootSpark) {
    Set<CorporeaSpark> visited = new LinkedHashSet<>();
    Deque<CorporeaSpark> pending = new ArrayDeque<>();
    pending.add(rootSpark);
    visited.add(rootSpark);

    while (!pending.isEmpty()) {
      CorporeaSpark spark = pending.removeFirst();

      AABB searchBox = spark.entity().getBoundingBox().inflate(SPARK_SCAN_RANGE);
      for (Entity entity : spark.entity().level().getEntitiesOfClass(Entity.class, searchBox, other -> other instanceof CorporeaSpark)) {
        CorporeaSpark otherSpark = (CorporeaSpark) entity;
        if (!otherSpark.entity().isAlive() || visited.contains(otherSpark) || otherSpark.getNetwork() != rootSpark.getNetwork()) {
          continue;
        }

        visited.add(otherSpark);
        pending.addLast(otherSpark);
      }
    }
    return visited;
  }

  private static Set<CorporeaNode> collectNodesByProximity(CorporeaSpark rootSpark) {
    Set<CorporeaNode> nodes = new LinkedHashSet<>();
    Set<CorporeaSpark> visited = new LinkedHashSet<>();
    Deque<CorporeaSpark> pending = new ArrayDeque<>();
    pending.add(rootSpark);
    visited.add(rootSpark);

    while (!pending.isEmpty()) {
      CorporeaSpark spark = pending.removeFirst();
      nodes.add(spark.getSparkNode());

      AABB searchBox = spark.entity().getBoundingBox().inflate(SPARK_SCAN_RANGE);
      for (Entity entity : spark.entity().level().getEntitiesOfClass(Entity.class, searchBox, other -> other instanceof CorporeaSpark)) {
        CorporeaSpark otherSpark = (CorporeaSpark) entity;
        if (!otherSpark.entity().isAlive() || visited.contains(otherSpark) || otherSpark.getNetwork() != rootSpark.getNetwork()) {
          continue;
        }

        visited.add(otherSpark);
        pending.addLast(otherSpark);
      }
    }

    return nodes;
  }

  private static Set<CorporeaNode> collectNearbySameNetworkInventoryNodes(CorporeaSpark rootSpark) {
    Set<CorporeaNode> nodes = new LinkedHashSet<>();
    AABB searchBox = rootSpark.entity().getBoundingBox().inflate(BROAD_SPARK_SCAN_RANGE);
    for (Entity entity : rootSpark.entity().level().getEntitiesOfClass(Entity.class, searchBox, other -> other instanceof CorporeaSpark)) {
      CorporeaSpark spark = (CorporeaSpark) entity;
      if (!spark.entity().isAlive() || spark.getNetwork() != rootSpark.getNetwork()) {
        continue;
      }

      CorporeaNode node = spark.getSparkNode();
      if (hasVisibleInventory(node)) {
        nodes.add(node);
      }
    }
    return nodes;
  }

  private static boolean hasVisibleInventory(CorporeaNode node) {
    Level world = node.getWorld();
    BlockPos pos = node.getPos();
    BlockState blockState = world.getBlockState(pos);
    BlockEntity blockEntity = world.getBlockEntity(pos);
    IItemHandler itemHandler = getItemHandler(blockEntity);
    if (itemHandler != null && countNonEmptyHandlerSlots(itemHandler) > 0) {
      return true;
    }

    Container container = getContainer(world, pos, blockState, blockEntity);
    return container != null && countNonEmptyContainerSlots(container) > 0;
  }

  public static List<ItemStack> getAllItemsCompacted(CorporeaSpark spark) {
    CorporeaSpark resolvedSpark = resolveRequestSpark(spark);
    List<ItemStack> result = new ArrayList<>();
    Set<CorporeaNode> nodes = deduplicateInventoryNodes(getNodesOnNetworkCompat(resolvedSpark));

    CorporeaRequest req = new CorporeaRequestImpl(ALL_MATCHER, Integer.MAX_VALUE, null);
    for (CorporeaNode node : nodes) {
      List<ItemStack> c = node.countItems(req);
      for (ItemStack stack : c) {
        ItemListHelper.addToListCompacted(result, stack);
      }
    }

    if (!result.isEmpty()) {
      return result;
    }

    result = scanNodeInventories(nodes);
    if (!result.isEmpty()) {
      return result;
    }

    Set<CorporeaNode> nearbyInventoryNodes = deduplicateInventoryNodes(collectNearbySameNetworkInventoryNodes(resolvedSpark));
    return scanNodeInventories(nearbyInventoryNodes);
  }

  public static ItemStack insertItem(CorporeaSpark spark, ItemStack stack) {
    if (spark == null || stack.isEmpty()) {
      return stack;
    }

    CorporeaSpark resolvedSpark = resolveRequestSpark(spark);
    ItemStack remaining = insertIntoNodes(deduplicateInventoryNodes(getNodesOnNetworkCompat(resolvedSpark)), stack);
    if (remaining.isEmpty()) {
      return ItemStack.EMPTY;
    }

    return insertIntoNodes(deduplicateInventoryNodes(collectNearbySameNetworkInventoryNodes(resolvedSpark)), remaining);
  }

  private static Set<CorporeaNode> deduplicateInventoryNodes(Set<CorporeaNode> nodes) {
    Set<CorporeaNode> result = new LinkedHashSet<>();
    Set<GlobalPos> seenInventories = new LinkedHashSet<>();
    for (CorporeaNode node : nodes) {
      GlobalPos key = getInventoryKey(node);
      if (key == null || seenInventories.add(key)) {
        result.add(node);
      }
    }
    return result;
  }

  private static GlobalPos getInventoryKey(CorporeaNode node) {
    Level world = node.getWorld();
    BlockPos pos = node.getPos();
    BlockState blockState = world.getBlockState(pos);
    BlockEntity blockEntity = world.getBlockEntity(pos);

    if (blockEntity instanceof ChestBlockEntity && blockState.getBlock() instanceof ChestBlock && blockState.hasProperty(ChestBlock.TYPE) && blockState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
      BlockPos otherPos = pos.relative(ChestBlock.getConnectedDirection(blockState));
      if (world.getBlockEntity(otherPos) instanceof ChestBlockEntity) {
        pos = pos.asLong() <= otherPos.asLong() ? pos : otherPos;
      }
    }

    return GlobalPos.of(world.dimension(), pos);
  }

  private static List<ItemStack> scanNodeInventories(Set<CorporeaNode> nodes) {
    List<ItemStack> result = new ArrayList<>();
    for (CorporeaNode node : nodes) {
      addNodeInventoryContents(node, result);
    }
    return result;
  }

  private static void addNodeInventoryContents(CorporeaNode node, List<ItemStack> result) {
    Level world = node.getWorld();
    BlockPos pos = node.getPos();
    BlockState blockState = world.getBlockState(pos);
    BlockEntity blockEntity = world.getBlockEntity(pos);

    if (addItemHandlerContents(getItemHandler(blockEntity), result)) {
      return;
    }

    Container container = getContainer(world, pos, blockState, blockEntity);

    if (container == null) {
      return;
    }

    addContainerContents(container, result);
  }

  private static ItemStack insertIntoNodes(Set<CorporeaNode> nodes, ItemStack stack) {
    ItemStack remaining = stack.copy();
    for (CorporeaNode node : nodes) {
      remaining = insertIntoNode(node, remaining);
      if (remaining.isEmpty()) {
        return ItemStack.EMPTY;
      }
    }
    return remaining;
  }

  private static ItemStack insertIntoNode(CorporeaNode node, ItemStack stack) {
    Level world = node.getWorld();
    BlockPos pos = node.getPos();
    BlockState blockState = world.getBlockState(pos);
    BlockEntity blockEntity = world.getBlockEntity(pos);

    IItemHandler itemHandler = getItemHandler(blockEntity);
    if (itemHandler != null) {
      return insertIntoItemHandler(itemHandler, stack);
    }

    Container container = getContainer(world, pos, blockState, blockEntity);
    return container == null ? stack : insertIntoContainer(container, stack);
  }

  private static ItemStack insertIntoItemHandler(IItemHandler itemHandler, ItemStack stack) {
    ItemStack remaining = stack.copy();
    for (int slot = 0; slot < itemHandler.getSlots() && !remaining.isEmpty(); slot++) {
      remaining = itemHandler.insertItem(slot, remaining, false);
    }
    return remaining;
  }

  private static ItemStack insertIntoContainer(Container container, ItemStack stack) {
    ItemStack remaining = stack.copy();
    boolean changed = false;
    changed |= insertIntoContainerSlots(container, remaining, false);
    changed |= insertIntoContainerSlots(container, remaining, true);
    if (changed) {
      container.setChanged();
    }
    return remaining.isEmpty() ? ItemStack.EMPTY : remaining;
  }

  private static boolean insertIntoContainerSlots(Container container, ItemStack remaining, boolean emptySlotsOnly) {
    boolean changed = false;
    for (int slot : getInsertSlots(container)) {
      if (remaining.isEmpty()) {
        return changed;
      }
      if (!canPlaceItem(container, slot, remaining)) {
        continue;
      }

      ItemStack slotStack = container.getItem(slot);
      if (emptySlotsOnly) {
        if (!slotStack.isEmpty()) {
          continue;
        }
        int transferred = Math.min(remaining.getCount(), Math.min(remaining.getMaxStackSize(), container.getMaxStackSize()));
        ItemStack inserted = remaining.copyWithCount(transferred);
        container.setItem(slot, inserted);
        remaining.shrink(transferred);
        changed = true;
      } else if (ItemStack.isSameItemSameTags(slotStack, remaining)) {
        int transferred = Math.min(remaining.getCount(), Math.min(slotStack.getMaxStackSize(), container.getMaxStackSize()) - slotStack.getCount());
        if (transferred > 0) {
          slotStack.grow(transferred);
          remaining.shrink(transferred);
          changed = true;
        }
      }
    }
    return changed;
  }

  private static int[] getInsertSlots(Container container) {
    if (container instanceof WorldlyContainer worldlyContainer) {
      return worldlyContainer.getSlotsForFace(Direction.UP);
    }

    int[] slots = new int[container.getContainerSize()];
    for (int i = 0; i < slots.length; i++) {
      slots[i] = i;
    }
    return slots;
  }

  private static boolean canPlaceItem(Container container, int slot, ItemStack stack) {
    if (!container.canPlaceItem(slot, stack)) {
      return false;
    }
    return !(container instanceof WorldlyContainer worldlyContainer) || worldlyContainer.canPlaceItemThroughFace(slot, stack, Direction.UP);
  }

  private static void addContainerContents(Container container, List<ItemStack> result) {
    for (int slot = container.getContainerSize() - 1; slot >= 0; slot--) {
      ItemStack stack = container.getItem(slot);
      if (!stack.isEmpty()) {
        ItemListHelper.addToListCompacted(result, stack.copy());
      }
    }
  }

  private static Container getContainer(Level world, BlockPos pos, BlockState blockState, BlockEntity blockEntity) {
    Container container = null;
    Block block = blockState.getBlock();
    if (block instanceof WorldlyContainerHolder worldlyContainer) {
      container = worldlyContainer.getContainer(blockState, world, pos);
    } else if (blockEntity instanceof Container beContainer) {
      container = beContainer;
      if (container instanceof ChestBlockEntity && block instanceof ChestBlock chest) {
        container = ChestBlock.getContainer(chest, blockState, world, pos, true);
      }
    }
    return container;
  }

  private static int countNonEmptyHandlerSlots(IItemHandler itemHandler) {
    int nonEmpty = 0;
    for (int slot = itemHandler.getSlots() - 1; slot >= 0; slot--) {
      if (!itemHandler.getStackInSlot(slot).isEmpty()) {
        nonEmpty++;
      }
    }
    return nonEmpty;
  }

  private static int countNonEmptyContainerSlots(Container container) {
    int nonEmpty = 0;
    for (int slot = container.getContainerSize() - 1; slot >= 0; slot--) {
      if (!container.getItem(slot).isEmpty()) {
        nonEmpty++;
      }
    }
    return nonEmpty;
  }

  private static boolean addItemHandlerContents(IItemHandler itemHandler, List<ItemStack> result) {
    if (itemHandler == null) {
      return false;
    }

    boolean foundAny = false;
    for (int slot = itemHandler.getSlots() - 1; slot >= 0; slot--) {
      ItemStack stack = itemHandler.getStackInSlot(slot);
      if (!stack.isEmpty()) {
        ItemListHelper.addToListCompacted(result, stack.copy());
        foundAny = true;
      }
    }
    return foundAny;
  }

  private static IItemHandler getItemHandler(BlockEntity blockEntity) {
    if (blockEntity == null) {
      return null;
    }

    LazyOptional<IItemHandler> optional = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP);
    if (!optional.isPresent()) {
      optional = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER);
    }
    return optional.orElse(null);
  }

  // Botania copy from CorporeaHelperImpl: request from corporea without calling interceptors
  public static CorporeaResult requestItemNoIntercept(CorporeaRequestMatcher matcher, int itemCount, CorporeaSpark spark, LivingEntity requestor, boolean doit) {
    List<ItemStack> stacks = new ArrayList<>();
    Set<CorporeaNode> nodes = deduplicateInventoryNodes(getNodesOnNetworkCompat(spark));
    CorporeaRequest request = new CorporeaRequestImpl(matcher, itemCount, requestor);
    for (CorporeaNode node : nodes) {
      if (doit) {
        stacks.addAll(node.extractItems(request));
      } else {
        stacks.addAll(node.countItems(request));
      }
    }

    if (stacks.isEmpty()) {
      Set<CorporeaNode> nearbyInventoryNodes = deduplicateInventoryNodes(collectNearbySameNetworkInventoryNodes(resolveRequestSpark(spark)));
      request = new CorporeaRequestImpl(matcher, itemCount, requestor);
      for (CorporeaNode node : nearbyInventoryNodes) {
        if (doit) {
          stacks.addAll(node.extractItems(request));
        } else {
          stacks.addAll(node.countItems(request));
        }
      }
    }

    int matchedCount = request.getFound();
    int extractedCount = request.getExtracted();
    return new CorporeaResult() {
      @Override
      public List<ItemStack> stacks() {
        return stacks;
      }

      @Override
      public int matchedCount() {
        return matchedCount;
      }

      @Override
      public int extractedCount() {
        return extractedCount;
      }
    };
  }
}
