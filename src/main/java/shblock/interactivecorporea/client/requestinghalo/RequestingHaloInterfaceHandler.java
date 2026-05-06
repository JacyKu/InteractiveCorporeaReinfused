package shblock.interactivecorporea.client.requestinghalo;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.ModSounds;
import shblock.interactivecorporea.client.render.DeferredWorldRenderQueue;
import shblock.interactivecorporea.client.renderer.tile.QuantizationDeviceWandHUD;
import shblock.interactivecorporea.common.item.HaloInterfaceStyle;
import shblock.interactivecorporea.common.network.CPacketRequestingHaloState;
import shblock.interactivecorporea.common.network.CPacketRequestingHaloViewUpdate;
import shblock.interactivecorporea.common.network.ModPacketHandler;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import shblock.interactivecorporea.common.tile.TileItemQuantizationDevice;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.CurioSlotPointer;
import shblock.interactivecorporea.common.util.ToolItemHelper;
import shblock.interactivecorporea.common.util.Vec2d;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.block.WandHUD;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.lwjgl.glfw.GLFW.*;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = IC.MODID)
public class RequestingHaloInterfaceHandler {
  private static final Minecraft mc = Minecraft.getInstance();
  private static RequestingHaloInterface haloInterface;
  private static final Map<Integer, RemoteRequestingHaloInterface> remoteInterfaces = new HashMap<>();
  private static int lastViewSyncTick = -1;

  public static final KeyMapping KEY_BINDING = new KeyMapping(
      "key.interactive_corporea.requesting_halo",
      KeyConflictContext.IN_GAME,
      InputConstants.Type.KEYSYM.getOrCreate(GLFW_KEY_TAB),
      IC.KEY_CATEGORY
  );

  public static RequestingHaloInterface getInterface() {
    return haloInterface;
  }

  public static void openInterface(RequestingHaloInterface face) {
    haloInterface = face;
    setupKeyboardListener();
    face.playSound(ModSounds.haloOpen, 1F);
    syncRemoteState(true);
  }

  public static boolean isInterfaceOpened() {
    return haloInterface != null;
  }

  private static void setupKeyboardListener() {
    KeyMapping.releaseAll();
    glfwSetKeyCallback(mc.getWindow().getWindow(),
        (windowPointer, key, scanCode, action, modifiers) -> {
          preKeyEvent(key, scanCode, action, modifiers);
          if ((!shouldCancelKeyEvent(key, scanCode)) || mc.screen != null) {
            mc.execute(() -> mc.keyboardHandler.keyPress(windowPointer, key, scanCode, action, modifiers));
          }
        });
    glfwSetCharModsCallback(mc.getWindow().getWindow(), (windowPointer, codePoint, modifiers) -> {
      Screen screen = mc.screen;
      if (screen != null) {
        mc.execute(() -> {
          for (char character : Character.toChars(codePoint)) {
            screen.charTyped(character, modifiers);
          }
        });
      } else {
        RequestingHaloInterfaceHandler.charCallback(codePoint, modifiers);
      }
    });
  }

  public static void resetKeyboardListener() {
    mc.keyboardHandler.setup(mc.getWindow().getWindow());
  }

  /**
   * Start the close animation
   */
  public static void closeInterface() {
    if (getInterface() != null) {
      getInterface().startClose();
      resetKeyboardListener();
    }
  }

  /**
   * Close the Interface immediately
   */
  public static void clearInterface() {
    if (haloInterface != null) {
      syncRemoteState(false);
      haloInterface.close();
      haloInterface = null;
      resetKeyboardListener();
      lastViewSyncTick = -1;
    }
  }

  static void syncRemoteState(boolean open) {
    if (haloInterface != null) {
      ModPacketHandler.sendToServer(new CPacketRequestingHaloState(haloInterface.getSlot(), open, haloInterface.getRotationOffset()));
    }
  }

  public static CISlotPointer getFirstHaloSlot(Player player) {
    if (player.getInventory().getSelected().getItem() instanceof ItemRequestingHalo) {
      return new CISlotPointer(player.getInventory().selected);
    }

    CurioSlotPointer cSlot = ToolItemHelper.getFirstMatchedCurioSlot(player, ItemRequestingHalo.class);
    if (cSlot != null) {
      return new CISlotPointer(cSlot);
    }

    int slot = ToolItemHelper.getFirstMatchedSlotInInventory(player, ItemRequestingHalo.class);
    if (slot != -1) {
      return new CISlotPointer(slot);
    }

    return null;
  }

  public static boolean tryOpen(Player player) {
    CISlotPointer slot = getFirstHaloSlot(player);
    if (slot != null) {
      ItemStack halo = slot.getStack(player);
      if (!ItemRequestingHalo.canPlayerAccessNetwork(player, halo)) {
        playOutOfRangeSound(player);
        return false;
      }
      openInterface(new RequestingHaloInterface(slot));
      return true;
    }
    return false;
  }

  private static void playOutOfRangeSound(Player player) {
    if (mc.level != null) {
      mc.level.playLocalSound(player.getX(), player.getY(), player.getZ(), ModSounds.haloOutOfRange, SoundSource.PLAYERS, 1F, 1F, false);
    }
  }

  /**
   * Check if the ItemStack in the slot of current opened interface is still the original one (If the halo item has not been changed)
   */
  public static boolean slotStillValid() {
    ItemStack currentStack = getInterface().getSlot().getStack(mc.player);
    return ItemStack.isSameItemSameTags(currentStack, getInterface().getHaloItem());
  }

  public static void handleUpdatePacket(List<ItemStack> itemList) {
    if (getInterface() != null) {
      getInterface().handleUpdatePacket(itemList);
    }
  }

  public static void handleRequestResultPacket(int requestId, int successAmount) {
    if (getInterface() != null) {
      getInterface().handleRequestResultPacket(requestId, successAmount);
    }
  }

  public static void handleRemoteState(int playerId, boolean open, float rotationOffset, int listHeight, boolean sortByAmount, List<ItemStack> itemList, HaloInterfaceStyle interfaceStyle, int haloTint) {
    if (mc.player != null && mc.player.getId() == playerId) {
      return;
    }

    RemoteRequestingHaloInterface remote = remoteInterfaces.get(playerId);
    if (!open) {
      if (remote != null) {
        remote.startClose();
      }
      return;
    }

    if (remote == null) {
      remote = new RemoteRequestingHaloInterface(playerId, rotationOffset, listHeight, sortByAmount, itemList, interfaceStyle, haloTint);
      remoteInterfaces.put(playerId, remote);
    } else {
      remote.update(rotationOffset, listHeight, sortByAmount, itemList, interfaceStyle, haloTint);
    }
  }

  public static void handleRemoteViewUpdate(int playerId, float rotationOffset, float relativeRotation, boolean hasSelection, float selectionX, float selectionY) {
    handleRemoteViewUpdate(playerId, rotationOffset, relativeRotation, 5, hasSelection, selectionX, selectionY, "", false, 0, 0, 0);
  }

  public static void handleRemoteViewUpdate(int playerId, float rotationOffset, float relativeRotation, int listHeight, boolean hasSelection, float selectionX, float selectionY, String searchString) {
    handleRemoteViewUpdate(playerId, rotationOffset, relativeRotation, listHeight, hasSelection, selectionX, selectionY, searchString, false, 0, 0, 0);
  }

  public static void handleRemoteViewUpdate(int playerId, float rotationOffset, float relativeRotation, int listHeight, boolean hasSelection, float selectionX, float selectionY, String searchString, boolean anchored, double anchoredX, double anchoredY, double anchoredZ) {
    if (mc.player != null && mc.player.getId() == playerId) {
      return;
    }

    RemoteRequestingHaloInterface remote = remoteInterfaces.get(playerId);
    if (remote != null) {
      remote.updateView(rotationOffset, relativeRotation, listHeight, hasSelection, selectionX, selectionY, searchString, anchored, anchoredX, anchoredY, anchoredZ);
    }
  }

  @SubscribeEvent
  public static void onLogOut(ClientPlayerNetworkEvent.LoggingOut event) {
    haloInterface = null;
    remoteInterfaces.clear();
  }

  @SubscribeEvent
  public static void onWorldRender(RenderLevelStageEvent event) {
    if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
      DeferredWorldRenderQueue.flush();
      return;
    }
    if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
      return;
    }

    RequestingHaloInterface face = getInterface();
    if (face != null) {
      if (!slotStillValid()) {
        clearInterface();
        return;
      } else {
        face.updateHaloItem(face.getSlot().getStack(mc.player));
      }

      MatrixStack stack = new MatrixStack(event.getPoseStack());
      if (!face.render(stack, event.getCamera(), event.getPartialTick())) {
        clearInterface();
      } else {
        syncRemoteViewState(face);
      }
    }

    Iterator<Map.Entry<Integer, RemoteRequestingHaloInterface>> iterator = remoteInterfaces.entrySet().iterator();
    while (iterator.hasNext()) {
      RemoteRequestingHaloInterface remote = iterator.next().getValue();
      MatrixStack stack = new MatrixStack(event.getPoseStack());
      if (!remote.render(stack, event.getCamera(), event.getPartialTick())) {
        iterator.remove();
      }
    }
  }

  private static void syncRemoteViewState(RequestingHaloInterface face) {
    if (mc.player == null || mc.player.tickCount == lastViewSyncTick) {
      return;
    }
    lastViewSyncTick = mc.player.tickCount;

    Vec2d selectionPos = face.getSelectionTargetPos();
    net.minecraft.util.math.vector.Vector3d anchoredPos = face.getAnchoredWorldPos();
    ModPacketHandler.sendToServer(new CPacketRequestingHaloViewUpdate(
        (float) face.getRotationOffset(),
        (float) face.getRelativeRotation(),
      face.getListHeight(),
        selectionPos != null,
        selectionPos == null ? 0F : (float) selectionPos.x,
        selectionPos == null ? 0F : (float) selectionPos.y,
        face.getSearchString(),
        face.isAnchored(),
        anchoredPos != null ? anchoredPos.x : 0,
        anchoredPos != null ? anchoredPos.y : 0,
        anchoredPos != null ? anchoredPos.z : 0
    ));
  }

  @SubscribeEvent
  public static void onHudRender(RenderGuiOverlayEvent.Post event) {
    if (getInterface() == null) return;
    if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {
      getInterface().renderHud(event.getGuiGraphics(), event.getPartialTick(), event.getWindow());
    }
  }

  @SubscribeEvent
  public static void tick(TickEvent.ClientTickEvent event) {
    if (event.phase == TickEvent.Phase.START) {
      if (getInterface() != null) {
        getInterface().tick();
      }
      for (RemoteRequestingHaloInterface remote : remoteInterfaces.values()) {
        remote.tick();
      }
    }
  }

  @SubscribeEvent
  public static void onMouseInput(InputEvent.MouseButton.Pre event) {
    if (getInterface() != null) {
      if (!getInterface().isOpenClose()) {
        if (getInterface().onMouseInput(event.getButton(), event.getAction(), event.getModifiers())) {
          event.setCanceled(true);
        }
      }
    }
  }

  @SubscribeEvent
  public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
    if (getInterface() != null) {
      if (!getInterface().isOpenClose()) {
        if (getInterface().onMouseScroll(event.getScrollDelta(), event.isRightDown(), event.isMiddleDown(), event.isLeftDown())) {
          event.setCanceled(true);
        }
      }
    }
  }

  private static boolean shouldCancelKeyEvent(int key, int scanCode) {
    if (getInterface() == null)
      return false;
    return getInterface().shouldCancelKeyEvent(key, scanCode);
  }

  public static void preKeyEvent(int key, int scanCode, int action, int modifiers) {
    if (getInterface() != null) {
      getInterface().preKeyEvent(key, scanCode, action, modifiers);
    }
  }

  @SubscribeEvent
  public static void onKeyEvent(InputEvent.Key event) {
    if (getInterface() != null) {
      if (!getInterface().isOpenClose()) {
        getInterface().onKeyEvent(event.getKey(), event.getScanCode(), event.getAction(), event.getModifiers());
      }
    }
    if (mc.screen != null) return;
    if (KEY_BINDING.matches(event.getKey(), event.getScanCode()) && event.getAction() == GLFW_PRESS) {
      if (getInterface() == null) {
        tryOpen(mc.player);
      } else {
        closeInterface();
      }
    }
  }

  public static void charCallback(int codePoint, int modifiers) {
    if (mc.screen == null) {
      if (getInterface() != null) {
        getInterface().onCharEvent(codePoint, modifiers);
      }
    }
  }

  public static Supplier<ItemStack> jeiUnderMouseGetter = () -> null;

  public static ItemStack getUnderMouseItemStack() {
    Screen screen = mc.screen;
    if (screen instanceof AbstractContainerScreen) {
      Slot slot = ((AbstractContainerScreen<?>) screen).getSlotUnderMouse();
      if (slot != null) {
        return slot.getItem();
      }
    }

    if (screen != null) {
      return jeiUnderMouseGetter.get();
    }

    return ItemStack.EMPTY;
  }

  @SubscribeEvent
  public static void onAttachBlockEntityCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
    BlockEntity be = event.getObject();
    if (!(be instanceof TileItemQuantizationDevice tile)) return;
    WandHUD hud = new QuantizationDeviceWandHUD(tile);
    LazyOptional<WandHUD> lazy = LazyOptional.of(() -> hud);
    event.addCapability(
        new ResourceLocation(IC.MODID, "quantization_device_wand_hud"),
        new ICapabilityProvider() {
          @Override
          public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(
              net.minecraftforge.common.capabilities.Capability<T> cap,
              @javax.annotation.Nullable net.minecraft.core.Direction side) {
            return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, lazy);
          }
        }
    );
    event.addListener(lazy::invalidate);
  }
}
