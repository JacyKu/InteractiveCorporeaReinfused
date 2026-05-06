package shblock.interactivecorporea.client.requestinghalo;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.BufferBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.ModList;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.client.render.DynamicLightsCompat;
import shblock.interactivecorporea.ModConfig;
import shblock.interactivecorporea.ModSounds;
import shblock.interactivecorporea.client.requestinghalo.crafting.CraftingInterfaceSlot;
import shblock.interactivecorporea.client.requestinghalo.crafting.HaloCraftingInterface;
import shblock.interactivecorporea.client.util.KeyboardHelper;
import shblock.interactivecorporea.client.util.RenderTick;
import shblock.interactivecorporea.common.util.MathUtil;
import shblock.interactivecorporea.common.item.HaloInterfaceStyle;
import shblock.interactivecorporea.common.item.HaloModule;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import shblock.interactivecorporea.common.network.CPacketInsertDroppedItem;
import shblock.interactivecorporea.common.network.ModPacketHandler;
import shblock.interactivecorporea.common.network.CPacketRequestItem;
import shblock.interactivecorporea.common.network.CPacketRequestItemListUpdate;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.Ray3;
import shblock.interactivecorporea.common.util.Vec2d;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.core.helper.Vector3;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

@SuppressWarnings("ConstantConditions")
public class RequestingHaloInterface {
  private static final Minecraft mc = Minecraft.getInstance();
  private static final MultiBufferSource.BufferSource TEXT_BUFFERS = MultiBufferSource.immediate(new BufferBuilder(64));

  public static final KeyMapping KEY_SEARCH = new KeyMapping(
      "key." + IC.MODID + ".requesting_halo.search",
      KeyConflictContext.IN_GAME,
      InputConstants.Type.KEYSYM.getOrCreate(GLFW_KEY_TAB),
      IC.KEY_CATEGORY
  );

  public static final KeyMapping KEY_REQUEST_UPDATE = new KeyMapping(
      "key." + IC.MODID + ".requesting_halo.request_update",
      KeyConflictContext.IN_GAME,
      InputConstants.Type.KEYSYM.getOrCreate(GLFW_KEY_U),
      IC.KEY_CATEGORY
  );

  public static final KeyMapping KEY_ANCHOR = new KeyMapping(
      "key." + IC.MODID + ".requesting_halo.anchor",
      KeyConflictContext.IN_GAME,
      InputConstants.Type.KEYSYM.getOrCreate(GLFW_KEY_G),
      IC.KEY_CATEGORY
  );

  private final CISlotPointer slot;
  private ItemStack haloItem;

  private boolean opening = true;
  private boolean closing = false;
  private double openCloseProgress = 0F;
  private boolean isNormalClose = false;
  private final Random magicParticleRandom = new Random();

  public double getOpenCloseProgress() {
    return openCloseProgress;
  }

  private int tick = 0;

  private static final double INITIAL_ROTATION = 0;
  private double rotationOffset;
  private double relativeRotation = INITIAL_ROTATION;
  private double lastRelativeRotation = INITIAL_ROTATION;

  private boolean anchored = false;
  private Vector3d anchoredWorldPos = null;

  private double radius = 2F;
  private double height = 1F;

  private final HaloSearchBar searchBar = new HaloSearchBar();
  private final HaloCraftingInterface craftingInterface;

  private final AnimatedCorporeaItemList itemList;
  private final AnimatedItemSelectionBox selectionBox = new AnimatedItemSelectionBox(() -> playSound(ModSounds.haloSelect, .25F, 1F));
  private HaloPickedItem pickedItem = null;
  private boolean shouldPickedItemFadeWhenLookUp = false;
  private final List<HaloPickedItem> fadingPickedItems = new ArrayList<>();
  private double itemSpacing;
  private double itemRotSpacing;
  private double itemZOffset;
  private Vector3 bottomIntersect = new Vector3(0, 0, 0);

  private static final String PREFIX_LIST_HEIGHT = "settings_item_list_height";
  private static final String PREFIX_SEARCH_STRING = "settings_search_string";

  public RequestingHaloInterface(CISlotPointer slot) {
    this.slot = slot;
    this.haloItem = slot.getStack(mc.player);
    this.rotationOffset = mc.player.getYRot() - INITIAL_ROTATION;
    searchBar.setUpdateCallback(this::updateSearch);

    itemList = new AnimatedCorporeaItemList(ItemNBTHelper.getInt(haloItem, PREFIX_LIST_HEIGHT, 5));

    searchBar.setSearchString(ItemNBTHelper.getString(haloItem, PREFIX_SEARCH_STRING, ""));
    searchBar.moveToEnd();

    craftingInterface = new HaloCraftingInterface(slot, haloItem);
    craftingInterface.setTargetRotation(Math.toRadians(INITIAL_ROTATION));
  }

  public CISlotPointer getSlot() {
    return slot;
  }

  public ItemStack getHaloItem() {
    return haloItem;
  }

  /**
   * DO NOT USE THIS! This is only used to sync the stack when updating player inventory
   */
  public void updateHaloItem(ItemStack haloItem) {
    this.haloItem = haloItem;
    craftingInterface.haloStack = haloItem;
  }

  public boolean isModuleInstalled(HaloModule module) {
    return ItemRequestingHalo.isModuleInstalled(haloItem, module);
  }

  public boolean render(MatrixStack ms, Camera info, double pt) {
    if (!updateOpenClose()) {
      close();
      return false;
    }

    handleRotation();

    MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

    double renderPosX = info.getPosition().x;
    double renderPosY = info.getPosition().y;
    double renderPosZ = info.getPosition().z;

    ms.push();

    Player player = mc.player;
    Vector3d eyePos = new Vector3d(player.getEyePosition((float) pt));
    Vector3d haloCenter = anchored && anchoredWorldPos != null ? anchoredWorldPos : eyePos;

    ms.translate(haloCenter.x - renderPosX, haloCenter.y - renderPosY, haloCenter.z - renderPosZ);

    ms.push();
    ms.rotate(new Quaternion(Vector3f.YP, (float) (-rotationOffset - relativeRotation), true));
    double progress = Math.sin((Math.PI / 2F) * openCloseProgress);
    double fadeWidth = .3;
    double width = progress * (Math.PI * 0.25F);
    HaloInterfaceStyle interfaceStyle = ItemRequestingHalo.getInterfaceStyle(haloItem);
    HaloInterfaceBackground.render(ms, radius, height, progress, interfaceStyle, ItemRequestingHalo.getHaloTintColor(haloItem), rotationOffset + relativeRotation);
    ms.pop();
    spawnTransitionParticlesOnHalo(interfaceStyle, progress);

    double fadeDegrees = Math.toDegrees(fadeWidth);
    double widthDegrees = Math.toDegrees(width);

    itemList.update(RenderTick.delta);
    ms.push();
    ms.rotate(new Quaternion(Vector3f.YP, (float) -rotationOffset, true));
    double scale = 1D / itemList.getHeight() * height * 2D;
    itemSpacing = (1D / itemList.getHeight()) * 2D * height;
    itemRotSpacing = MathUtil.calcRadiansFromChord(radius, itemSpacing);
    itemZOffset = MathUtil.calcChordCenterDistance(radius, itemSpacing);
    double colOffset = itemList.getColumnOffset();
    Vec2d lookingPos = anchored ? calcLookingPosAnchored() : calcLookingPos(radius, itemSpacing, itemRotSpacing, colOffset);
    boolean haveSelectedItem = false;
    for (AnimatedItemStack aniStack : itemList.getAnimatedList()) {
      if (updateSelectionBox(aniStack, lookingPos)) {
        haveSelectedItem = true;
      }

      Vec2d pos = aniStack.getPos();
      float rot = (float) ((pos.x - colOffset) * itemRotSpacing);
      if (!anchored) {
        double degreeDiff = Math.abs(relativeRotation - Math.toDegrees(rot));
        if (degreeDiff >= widthDegrees) {
          continue;
        }
      }

      float currentScale;
      if (anchored) {
        currentScale = (float) scale;
      } else {
        double degreeDiff = Math.abs(relativeRotation - Math.toDegrees(rot));
        currentScale = (float) (scale * Math.sin(MathHelper.clamp(widthDegrees - degreeDiff, 0F, fadeDegrees) / fadeDegrees * Math.PI * .5F));
      }
      ms.push();
      ms.rotate(new Quaternion(Vector3f.YP, -rot, false));
      ms.translate(0F, -(pos.y - (itemList.getHeight() - 1D) / 2D) * itemSpacing, itemZOffset);
      ms.scale(currentScale, currentScale, currentScale);

      double pp = RenderTick.total * .025;
      double mp = 1 / currentScale * 0.0375;
      ms.translate(
          aniStack.noise.perlin(pp, 0, 0) * mp,
          aniStack.noise.perlin(0, pp, 0) * mp,
          0F
      );

      aniStack.renderItem(ms);

      ms.push();
      float ts = 1F / 24F;
      ms.scale(ts, ts, ts);
      ms.translate(-itemSpacing - 10D, -itemSpacing - 4D, -0.02);
      aniStack.renderAmount(ms, 0x00FFFFFF | 0xFF << 24, TEXT_BUFFERS);
      ms.pop();

      ms.push();
      ts = 1F / 18F;
      ms.scale(ts, ts, ts);
      ms.translate(0F, 0F, -0.05);
      aniStack.renderRequestResultAnimations(ms, buffers);
      ms.pop();

      ms.pop();
    }
    if (!haveSelectedItem || closing) {
      selectionBox.setTarget(null);
    }
    ms.pop();
    TEXT_BUFFERS.endBatch();

    selectionBox.update();

    ms.push();
    ms.rotate(new Quaternion(Vector3f.YP, (float) -rotationOffset, true));
    Vec2d selPos = selectionBox.getPos();
    ms.rotate(Vector3f.YP.rotation((float) (-(selPos.x - colOffset) * itemRotSpacing)));
    ms.translate(0F, -(selPos.y - (itemList.getHeight() - 1) / 2F) * itemSpacing, itemZOffset);
    float s = (float) scale;
    ms.scale(s, s, s);
    selectionBox.render(ms);
    ms.pop();

    if (isModuleInstalled(HaloModule.SEARCH)) {
      renderSearchBar(ms, pt);
    }

    renderPickedItem(ms);

    if (isModuleInstalled(HaloModule.CRAFTING)) {
      renderCrafting(ms, pt);
    }

    ms.pop();

    return true;
  }

  public void renderSearchBar(MatrixStack ms, double pt) {
    ms.push();
    ms.rotate(new Quaternion(Vector3f.YP, (float) (-rotationOffset - relativeRotation), true));
    ms.translate(0, 0, radius);
    ms.scale((float) Math.sin(openCloseProgress * Math.PI * .5), 1, (float) Math.max(Math.sin((openCloseProgress - .5) * Math.PI), .01));
    ms.translate(0, 0, -radius);
    searchBar.render(ms, radius, height);
    ms.pop();
  }

  public void renderCrafting(MatrixStack ms, double pt) {
    double size = .5;
    craftingInterface.setSize(size);
    craftingInterface.setPos(MathUtil.calcChordCenterDistance(radius, size * 2) - size - .1);

    double lockingRotation = 90;
    double relPlus = relativeRotation - INITIAL_ROTATION;
    double lastRelPlus = lastRelativeRotation - INITIAL_ROTATION;
    if (Math.floor(relPlus / lockingRotation) != Math.floor(lastRelPlus / lockingRotation)) {
      double relRot = Math.max(relPlus, lastRelPlus);
      double rotDegrees = Math.floor(relRot / lockingRotation) * lockingRotation + INITIAL_ROTATION;
      craftingInterface.setTargetRotation(Math.toRadians(rotDegrees));
    }

    ms.push();
    ms.translate(0, -height, 0);
    ms.rotate(Vector3f.YP.rotationDegrees((float) -rotationOffset));
    craftingInterface.render(ms, Math.sin(openCloseProgress * Math.PI / 2));
    ms.pop();
  }

  public void renderPickedItem(MatrixStack ms) {
    if (pickedItem != null) {
      if (pickedItem.render(ms)) {
        pickedItem = null;
      }
    }
    for (int i = fadingPickedItems.size() - 1; i >= 0; i--) {
      if (fadingPickedItems.get(i).render(ms))
        fadingPickedItems.remove(i);
    }
  }

  public void renderHud(GuiGraphics gui, float pt, Window window) {
    if (isModuleInstalled(HaloModule.HUD)) {
      AnimatedItemStack aniStack = selectionBox.getTarget();
      if (aniStack != null) {
        aniStack.renderHud(gui, pt, window);
      }
    }
  }

  public void tick() {
    drainManaOrClose(ModConfig.COMMON.requestingHaloStaticConsumption.get());

    itemList.tick();
    if (tick == 0) {
      requestItemListUpdate();
    } else if (tick % 20 == 0) {
      if (isModuleInstalled(HaloModule.UPDATE)) {
        requestItemListUpdate();
      }
    }

    Vector3 lookDir = new Vector3(Vector3d.fromPitchYaw(mc.player.getXRot(), mc.player.getYRot()));
    bottomIntersect = MathUtil.rayPlaneIntersection(
        new Ray3(new Vector3(0, 0, 0), lookDir),
        new Ray3(new Vector3(0, -height, 0), new Vector3(0, 1, 0))
    );
    if (pickedItem != null) {
      if (bottomIntersect != null && new Vector3(bottomIntersect.x, 0, bottomIntersect.z).mag() < radius * .94) {
        shouldPickedItemFadeWhenLookUp = true;
      }
      if (shouldPickedItemFadeWhenLookUp && Math.tan(Math.toRadians(mc.player.getXRot())) * radius < height - .05) {
        pickedItem.fadeOut();
        shouldPickedItemFadeWhenLookUp = false;
      }

      if (bottomIntersect != null && new Vector3(bottomIntersect.x, 0, bottomIntersect.z).mag() < radius) {
        pickedItem.setTargetPosition(bottomIntersect.add(0, .05, 0));
        pickedItem.setTargetRotationDegrees(-90, -mc.player.getYRot() + 180);
      } else {
        pickedItem.setTargetPosition(lookDir.multiply(radius * .8));
        pickedItem.setTargetRotationDegrees(-mc.player.getXRot(), -mc.player.getYRot() + 180);
      }
      pickedItem.tick();
    }
    fadingPickedItems.forEach(HaloPickedItem::tick);

    craftingInterface.tick(bottomIntersect != null ? new Vec2d(bottomIntersect.rotate(Math.toRadians(rotationOffset), new Vector3(0, 1, 0))) : null);

    tick++;
  }

  /**
   * @return the rotation (in radians) needed to reach the end of the item list
   */
  private double getItemListDisplayWidth() {
    int itemCnt = itemList.getAnimatedList().size();
    int cols = itemCnt / itemList.getHeight();
    if (itemCnt % itemList.getHeight() != 0)
      cols++;
    return (cols - 1) * itemRotSpacing;
  }

  private double prevPlayerRot = mc.player.getYRot();
  private double rotationToAdd = 0;
  private double rotationAddingSpeed = 0;
  public void handleRotation() {
    lastRelativeRotation = relativeRotation;

    double rot = mc.player.getYRot();
    double ra = rot - prevPlayerRot;
    double rb = rot - prevPlayerRot;
    if (!anchored) {
      if (Math.abs(ra) < Math.abs(rb)) {
        relativeRotation += ra;
      } else {
        relativeRotation += rb;
      }
    }
    prevPlayerRot = rot;

    double dt = RenderTick.delta;
    rotationAddingSpeed = MathUtil.smoothMovingSpeed(rotationToAdd, 0, rotationAddingSpeed, .3, .7, .01);
    rotationToAdd += rotationAddingSpeed * dt;
    rotationOffset += rotationAddingSpeed * dt;
    relativeRotation -= rotationAddingSpeed * dt;

    if (!anchored) {
      limitPlayerRotation();
    }
  }

  private void limitPlayerRotation() {
    if (isOpenClose()) return;
    if (itemList.getAnimatedList().size() == 0) {
      return;
    }

    if (Math.abs(Math.toRadians(mc.player.getXRot())) > Math.atan(height / radius)) {
      return;
    }

    double excessSpacing = 3;
    double correctionSpd = .1;
    double minSpd = .1;
    double degreeItemRotSpacing = Math.toDegrees(itemRotSpacing);

    double halfListWidth = Math.toDegrees(getItemListDisplayWidth()) / 2.0;
    double start = -(halfListWidth + degreeItemRotSpacing / 2 + excessSpacing);
    double distToStart = relativeRotation - start;
    if (distToStart < 0) {
      mc.player.setYRot((float) (mc.player.getYRot() - (distToStart * correctionSpd - minSpd) * RenderTick.delta));
      spawnParticleLineOnHalo(start);
    }

    double end = halfListWidth + degreeItemRotSpacing / 2 + excessSpacing;
    double distToEnd = relativeRotation - end;
    if (distToEnd > 0) {
      mc.player.setYRot((float) (mc.player.getYRot() - (distToEnd * correctionSpd + minSpd) * RenderTick.delta));
      spawnParticleLineOnHalo(end);
    }
  }

  private float particleSpawnTimer = 0F;
  private void spawnParticleLineOnHalo(double relativeRot) {
    if (mc.level == null || mc.player == null) return;

    Vector3d mid = new Vector3d(radius + .12D, 0, 0);
    mid = mid.rotateYaw((float) Math.toRadians(-rotationOffset - 90 - relativeRot));
    mid = mid.add(getHaloCenter());
    particleSpawnTimer += RenderTick.delta;
    HaloInterfaceStyle style = ItemRequestingHalo.getInterfaceStyle(haloItem);
    float[] tint = ItemRequestingHalo.getHaloTintColor(haloItem);
    int cnt = 0;
    for (int i = 0; i < (int) (particleSpawnTimer / .1); i++) {
      Vector3d pos = mid.add(0, (magicParticleRandom.nextDouble() * 2 - 1) * height, 0);
      float[] color = HaloStylePalette.tint(HaloStylePalette.particle(style, RenderTick.total * .01D + magicParticleRandom.nextDouble()), tint, .82F, .08F);
      float size = style == HaloInterfaceStyle.CLASSIC ? .75F : 1F + magicParticleRandom.nextFloat() * .35F;
      mc.level.addParticle(new DustParticleOptions(new org.joml.Vector3f(color[0], color[1], color[2]), size), pos.x, pos.y, pos.z, 0, .004D + magicParticleRandom.nextDouble() * .004D, 0);
      cnt++;
    }
    particleSpawnTimer -= cnt * .1;
  }

  private float transitionParticleSpawnTimer = 0F;
  private void spawnTransitionParticlesOnHalo(HaloInterfaceStyle style, double progress) {
    if ((!opening && !closing) || progress < .04D || mc.level == null || mc.player == null) {
      transitionParticleSpawnTimer = 0F;
      return;
    }

    float interval = style == HaloInterfaceStyle.BOTANIA ? .035F : .045F;
    transitionParticleSpawnTimer += RenderTick.delta;
    int spawnCount = Math.min((int) (transitionParticleSpawnTimer / interval), style == HaloInterfaceStyle.BOTANIA ? 5 : 4);
    if (spawnCount <= 0) {
      return;
    }

    Vector3d eyePos = new Vector3d(getHaloCenter());
    float[] tint = ItemRequestingHalo.getHaloTintColor(haloItem);
    double panelWidth = progress * (Math.PI * .25D);
    boolean openingNow = opening && !closing;
    for (int index = 0; index < spawnCount; index++) {
      double relativeAngle = (magicParticleRandom.nextDouble() * 2D - 1D) * panelWidth * .88D;
      double vertical = (magicParticleRandom.nextDouble() * 2D - 1D) * height * (.62D + progress * .25D);
      double particleRadius = radius + (openingNow ? -.32D - magicParticleRandom.nextDouble() * .22D : .18D + magicParticleRandom.nextDouble() * .2D);
      Vector3d pos = new Vector3d(particleRadius, vertical, 0D)
          .rotateYaw((float) Math.toRadians(-rotationOffset - 90D - relativeRotation + Math.toDegrees(relativeAngle)))
          .add(eyePos);
      float[] color = HaloStylePalette.tint(HaloStylePalette.particle(style, RenderTick.total * .012D + magicParticleRandom.nextDouble()), tint, .86F, .1F);
      float size = .68F + magicParticleRandom.nextFloat() * (style == HaloInterfaceStyle.BOTANIA ? .62F : .44F);
      double outwardX = pos.x - eyePos.x;
      double outwardZ = pos.z - eyePos.z;
      double outwardLength = Math.sqrt(outwardX * outwardX + outwardZ * outwardZ);
      if (outwardLength > 1.0E-4D) {
        outwardX /= outwardLength;
        outwardZ /= outwardLength;
      }
      double direction = openingNow ? 1D : -1D;
      double swirl = (magicParticleRandom.nextDouble() * 2D - 1D) * .012D;
      double radialSpeed = (.018D + magicParticleRandom.nextDouble() * .018D) * direction;
      double speedX = outwardX * radialSpeed - outwardZ * swirl;
      double speedZ = outwardZ * radialSpeed + outwardX * swirl;
      double speedY = (magicParticleRandom.nextDouble() * 2D - 1D) * .006D + (openingNow ? .004D : -.002D);
      mc.level.addParticle(new DustParticleOptions(new org.joml.Vector3f(color[0], color[1], color[2]), size), pos.x, pos.y, pos.z, speedX, speedY, speedZ);
    }
    transitionParticleSpawnTimer -= spawnCount * interval;
  }

  private Vector3d getHaloCenter() {
    if (anchored && anchoredWorldPos != null) return anchoredWorldPos;
    return new Vector3d(mc.player.getEyePosition((float) RenderTick.pt));
  }

  public boolean isAnchored() {
    return anchored;
  }

  public void toggleAnchor() {
    if (!isModuleInstalled(HaloModule.ANCHOR)) return;
    anchored = !anchored;
    if (anchored) {
      anchoredWorldPos = new Vector3d(mc.player.getEyePosition(1.0f));
      if (ModList.get().isLoaded("sodiumdynamiclights")) {
        DynamicLightsCompat.activateAnchorLight(anchoredWorldPos.x, anchoredWorldPos.y, anchoredWorldPos.z);
      }
    } else {
      anchoredWorldPos = null;
      relativeRotation = mc.player.getYRot() - rotationOffset;
      prevPlayerRot = mc.player.getYRot();
      if (ModList.get().isLoaded("sodiumdynamiclights")) {
        DynamicLightsCompat.deactivateAnchorLight();
      }
    }
    playSound(ModSounds.haloSelect, .5F, anchored ? 0.8F : 1.2F);
  }

  /**
   * Calculates which halo grid cell the player is looking at when the halo is anchored at a
   * fixed world position. Uses ray-cylinder intersection so the player can stand outside the ring.
   */
  private Vec2d calcLookingPosAnchored() {
    if (anchoredWorldPos == null || itemSpacing == 0 || itemRotSpacing == 0) {
      return calcLookingPos(radius, itemSpacing, itemRotSpacing, itemList.getColumnOffset());
    }
    Vector3d eyePos = new Vector3d(mc.player.getEyePosition((float) RenderTick.pt));
    double px = eyePos.x - anchoredWorldPos.x;
    double py = eyePos.y - anchoredWorldPos.y;
    double pz = eyePos.z - anchoredWorldPos.z;

    double pitchRad = Math.toRadians(mc.player.getXRot());
    double yawRad   = Math.toRadians(mc.player.getYRot());
    double dx = -Math.sin(yawRad) * Math.cos(pitchRad);
    double dy = -Math.sin(pitchRad);
    double dz =  Math.cos(yawRad) * Math.cos(pitchRad);

    double a = dx * dx + dz * dz;
    if (a < 1e-10) {
      return calcLookingPos(radius, itemSpacing, itemRotSpacing, itemList.getColumnOffset());
    }
    double b    = 2.0 * (px * dx + pz * dz);
    double cVal = px * px + pz * pz - radius * radius;
    double disc = b * b - 4.0 * a * cVal;
    if (disc < 0) {
      return calcLookingPos(radius, itemSpacing, itemRotSpacing, itemList.getColumnOffset());
    }
    double sqrtDisc = Math.sqrt(disc);
    double t1 = (-b - sqrtDisc) / (2.0 * a);
    double t2 = (-b + sqrtDisc) / (2.0 * a);
    double t;
    if (t1 > 0) {
      t = t1;
    } else if (t2 > 0) {
      t = t2;
    } else {
      return calcLookingPos(radius, itemSpacing, itemRotSpacing, itemList.getColumnOffset());
    }

    double hx = px + t * dx;
    double hy = py + t * dy;
    double hz = pz + t * dz;

    // Map hit angle to column.
    // Items at column c are at world direction yaw = rotationOffset + (c-colOffset)*rotSpacing_deg,
    // which maps to atan2 angle = -toRadians(rotationOffset + (c-colOffset)*rotSpacing_deg).
    // Solving for c: c = colOffset - (hitAngle + toRadians(rotationOffset)) / itemRotSpacing
    double hitAngle = Math.atan2(hx, hz);
    double col = itemList.getColumnOffset() - (hitAngle + Math.toRadians(rotationOffset)) / itemRotSpacing;

    // Map hit height to row: item at row r is at y = -(r - (height-1)/2) * spacing
    double row = (itemList.getHeight() - 1.0) / 2.0 - hy / itemSpacing;

    return new Vec2d(col, row);
  }

  /**
   * @param radius the radius of the halo ring (the distance from player's position to the halo surface)
   * @param rotSpacing the radians between two items
   */
  private Vec2d calcLookingPos(double radius, double spacing, double rotSpacing, double colOffset) {
    return new Vec2d(
        (Math.toRadians(relativeRotation) / rotSpacing) + colOffset,
        (Math.tan(Math.toRadians(mc.player.getXRot())) * radius / spacing) + (itemList.getHeight() - 1) / 2F
    );
  }

  private boolean isLookingAtItem(Vec2d itemPos, Vec2d lookingPos) {
    return Math.abs(lookingPos.x - itemPos.x) < .5F && Math.abs(lookingPos.y - itemPos.y) < .5F;
  }

  /**
   * called for EVERY rendering item
   */
  private boolean updateSelectionBox(AnimatedItemStack stack, Vec2d lookingPos) {
    if (stack.isRemoved()) return false;

    Vec2d itemPos = stack.getPos();
    if (isLookingAtItem(itemPos, lookingPos)) {
      selectionBox.setTarget(stack);
      return true;
    }
    return false;
  }

  private boolean updateOpenClose() {
    double animationSpeed = 10F;
    if (opening) {
      openCloseProgress += RenderTick.delta / animationSpeed;
      if (openCloseProgress >= 1F) {
        openCloseProgress = 1F;
        opening = false;
      }
    } else if (closing) {
      openCloseProgress -= RenderTick.delta / animationSpeed;
      if (openCloseProgress <= 0F) {
        openCloseProgress = 0F;
        closing = false;
        return false;
      }
    }
    return true;
  }

  public void close() {
    if (!isNormalClose) {
      RequestingHaloInterfaceHandler.resetKeyboardListener();
      playSound(ModSounds.haloClose, 1F);
    }
    if (anchored && ModList.get().isLoaded("sodiumdynamiclights")) {
      DynamicLightsCompat.deactivateAnchorLight();
    }
    itemList.removeAll();
  }

  /**
   * Start the close animation
   */
  public void startClose() {
    if (closing) return;
    if (!opening) {
      RequestingHaloInterfaceHandler.resetKeyboardListener();
      closing = true;
      isNormalClose = true;
      playSound(ModSounds.haloClose, 1F);

      if (pickedItem != null) {
        pickedItem.fadeOut();
        fadingPickedItems.add(pickedItem);
        pickedItem = null;
      }
    }
  }

  private static void playSwingAnimation() {
    mc.player.swing(InteractionHand.MAIN_HAND);
  }

  public boolean requestItem() {
    if (isOpenClose()) return false;
    if (!isModuleInstalled(HaloModule.RECEIVE)) return false;
    AnimatedItemStack aniStack = selectionBox.getTarget();
    if (aniStack == null || aniStack.isRemoved()) return false;
    ItemStack stack = aniStack.getStack();
    int requestCnt = 1;
    if (KeyboardHelper.hasShiftDown() && KeyboardHelper.hasControlDown()) {
      requestCnt = stack.getMaxStackSize() / 4;
    } else if (KeyboardHelper.hasControlDown()) {
      requestCnt = stack.getMaxStackSize() / 2;
    } else if (KeyboardHelper.hasShiftDown()) {
      requestCnt = stack.getMaxStackSize();
    }
    ItemStack reqStack = stack.copy();
    reqStack.setCount(requestCnt);
    Player player = mc.player;
    double rot = Math.toRadians(-rotationOffset - relativeRotation);
    Vector3 normal = new Vector3(Math.sin(rot) * itemZOffset * .9, 0, Math.cos(rot) * itemZOffset * .9);
    Vector3 pos = normal.add(player.getX(), player.getEyeY() - Math.tan(Math.toRadians(mc.player.getXRot())) * radius, player.getZ());
    ModPacketHandler.sendToServer(new CPacketRequestItem(slot, reqStack, pos, normal, itemList.onRequest(aniStack)));
    playSwingAnimation();
    selectionBox.playRequestAnimation();
    playSound(pos.x, pos.y, pos.z, ModSounds.haloRequest, 1F);
    return true;
  }

  private boolean drainManaOrClose(int amount) {
    if (isOpenClose()) return true;
    ManaItemHandler manaItemHandler = ManaItemHandler.instance();
    if (!manaItemHandler.requestManaExactForTool(haloItem, mc.player, amount, true)) {
      startClose();
      return false;
    }
    return true;
  }

  private void updateSearch() {
    itemList.setFilter(searchBar.getSearchString());
    itemList.arrange();
    ItemNBTHelper.setString(haloItem, PREFIX_SEARCH_STRING, searchBar.getSearchString());
  }

  private boolean pickItem(boolean unpick) {
    if (unpick) {
      if (pickedItem == null) return false;

      pickedItem.fadeOut();
      fadingPickedItems.add(pickedItem);
      pickedItem = null;
    } else {
      HaloPickedItem newPickedItem = null;
      if (mc.screen == null) {
        if (selectionBox.getTarget() != null) {
          double rot = Math.toRadians(-rotationOffset - relativeRotation);
          double pitch = Math.toRadians(mc.player.getXRot());
          Vector3 pos = new Vector3(
              Math.sin(rot) * itemZOffset,
              -Math.tan(pitch) * radius,
              Math.cos(rot) * itemZOffset
          );
          newPickedItem = new HaloPickedItem(selectionBox.getTarget().getStack(), pos, 0, rot + Math.PI);
          playSwingAnimation();
        } else {
          CraftingInterfaceSlot slot = craftingInterface.getPointingSlot();
          if (slot != null && !slot.getShadowStack().isEmpty() && bottomIntersect != null) {
            newPickedItem = new HaloPickedItem(slot.getShadowStack(), bottomIntersect.add(0, .05, 0), -Math.PI / 4, Math.toRadians(-mc.player.getYRot() + 180));
          }
        }
      } else {
        ItemStack stackUnderMouse = RequestingHaloInterfaceHandler.getUnderMouseItemStack();
        if (!stackUnderMouse.isEmpty()) {
          newPickedItem = new HaloPickedItem(stackUnderMouse, new Vector3(0, 0, 0), Math.toRadians(-mc.player.getXRot()), Math.toRadians(-mc.player.getYRot() + 180));
        }
      }

      if (newPickedItem == null) return false;

      if (pickedItem != null) {
        pickedItem.fadeOut();
        fadingPickedItems.add(pickedItem);
      }
      pickedItem = newPickedItem;
    }
    return true;
  }

  /**
   * @return If the action is consumed
   */
  public boolean onMouseInput(int button, int action, int mods) {
    if (isOpenClose()) return false;

    Vector3 intersectWorldPos = null;
    if (bottomIntersect != null)
      intersectWorldPos = bottomIntersect.add(new Vector3(mc.player.getEyePosition((float) RenderTick.pt)));
    if (action == GLFW_PRESS) {
      switch (button) {
        case GLFW_MOUSE_BUTTON_LEFT:
          if (mc.screen != null) return false;
          if (requestItem())
            return true;
          if (craftingInterface != null) {
            if (craftingInterface.handleSlotInteraction(false, intersectWorldPos, pickedItem)) {
              playSwingAnimation();
              return true;
            }
          }
          break;
        case GLFW_MOUSE_BUTTON_RIGHT:
          if (mc.screen != null) return false;
          if (craftingInterface != null) {
            if (KeyboardHelper.hasShiftDown()) {
              if (craftingInterface != null) {
                craftingInterface.tryOpenJei();
                return true;
              }
            } else {
              if (craftingInterface.handleSlotInteraction(true, intersectWorldPos, pickedItem)) {
                playSwingAnimation();
                return true;
              }
            }
          }
          break;
        case GLFW_MOUSE_BUTTON_MIDDLE:
          if (pickItem(KeyboardHelper.hasShiftDown()))
            return true;
          break;
      }
    } else {
      return false;
    }
    if (craftingInterface.isPointingAtInterface())
      return true;
    return false;
  }

  /**
   * @return If the action is consumed
   */
  public boolean onMouseScroll(double delta, boolean rightDown, boolean midDown, boolean leftDown) {
    if (isOpenClose()) return false;

    if (KeyboardHelper.hasControlDown()) {
      itemList.changeHeight((int) -delta);
      ItemNBTHelper.setInt(haloItem, PREFIX_LIST_HEIGHT, itemList.getHeight());
      itemList.arrange();
      return true;
    }
    if (KeyboardHelper.hasAltDown()) {
      rotationToAdd += delta * Math.toDegrees(itemRotSpacing);
      return true;
    }
    return false;
  }

  public void preKeyEvent(int key, int scanCode, int action, int modifiers) {
    if (isOpenClose()) return;

    if (action == GLFW_PRESS && shouldInsertDroppedItem(key, scanCode)) {
      ModPacketHandler.sendToServer(new CPacketInsertDroppedItem(slot, KeyboardHelper.hasControlDown()));
      playSound(ModSounds.quantumSend, 1F);
      return;
    }

    if (action == GLFW_PRESS || action == GLFW_REPEAT) {
      if (Screen.isCopy(key)) {
        searchBar.copy();
      } else if (Screen.isPaste(key)) {
        searchBar.paste();
      } else if (Screen.isCut(key)) {
        searchBar.cut();
      } else if (Screen.isSelectAll(key)) {
        searchBar.selectAll();
      }
    }
  }

  private static final Int2IntFunction craftingSlotKeyMap = key -> {
    switch (key) {
      case GLFW_KEY_KP_7: return 0;
      case GLFW_KEY_KP_8: return 1;
      case GLFW_KEY_KP_9: return 2;
      case GLFW_KEY_KP_4: return 3;
      case GLFW_KEY_KP_5: return 4;
      case GLFW_KEY_KP_6: return 5;
      case GLFW_KEY_KP_1: return 6;
      case GLFW_KEY_KP_2: return 7;
      case GLFW_KEY_KP_3: return 8;
    }
    return -1;
  };
  public void onKeyEvent(int key, int scanCode, int action, int modifiers) {
    if (isOpenClose()) return;

    if (mc.screen == null) {
    if (isModuleInstalled(HaloModule.SEARCH)) {
      if (KEY_SEARCH.consumeClick()) {
          searchBar.setSearching(!searchBar.isSearching());
          return;
      }
    }
    if (KEY_REQUEST_UPDATE.consumeClick()) {
      itemList.sortByAmount();
      playSound(ModSounds.haloListUpdate, 1F);
      return;
    }
    if (KEY_ANCHOR.consumeClick()) {
      toggleAnchor();
      return;
    }
    if (action == GLFW_PRESS || action == GLFW_REPEAT) {
        if (searchBar.isSearching()) {
          switch (key) {
            case GLFW_KEY_BACKSPACE:
              searchBar.backspace(KeyboardHelper.hasControlDown());
              break;
            case GLFW_KEY_DELETE:
              searchBar.delete();
              break;
            case GLFW_KEY_LEFT:
              searchBar.moveSelectionPos(-1, !KeyboardHelper.hasShiftDown());
              break;
            case GLFW_KEY_RIGHT:
              searchBar.moveSelectionPos(1, !KeyboardHelper.hasShiftDown());
              break;
            case GLFW_KEY_HOME:
              searchBar.moveToStart();
              break;
            case GLFW_KEY_END:
              searchBar.moveToEnd();
              break;
          }
        } else {
          if (key == GLFW_KEY_ENTER) {
            craftingInterface.doCraft();
          }
        }
      }
    } else {
      if (action == GLFW_PRESS) {
        int slot = craftingSlotKeyMap.applyAsInt(key);
        if (slot != -1) {
          ItemStack stackUnderMouse = RequestingHaloInterfaceHandler.getUnderMouseItemStack();
          if (!stackUnderMouse.isEmpty()) {
            craftingInterface.getPointingSlot().setShadowStack(stackUnderMouse);
            return;
          }
        }
      }
    }
  }

  public boolean shouldCancelKeyEvent(int key, int scanCode) {
    if (isOpenClose()) return false;
    if (shouldInsertDroppedItem(key, scanCode)) return true;
    if (!searchBar.isSearching() && RequestingHaloInterfaceHandler.KEY_BINDING.matches(key, scanCode)) return false;
    if (KEY_SEARCH.matches(key, scanCode))
      return false;
    if (KEY_ANCHOR.matches(key, scanCode))
      return false;
    switch (key) {
      case GLFW_KEY_BACKSPACE:
      case GLFW_KEY_DELETE:
      case GLFW_KEY_LEFT:
      case GLFW_KEY_RIGHT:
      case GLFW_KEY_LEFT_SHIFT:
      case GLFW_KEY_RIGHT_SHIFT:
      case GLFW_KEY_HOME:
      case GLFW_KEY_END:
        return false;
    }
    return searchBar.isSearching();
  }

  private boolean shouldInsertDroppedItem(int key, int scanCode) {
    if (mc.player == null || mc.screen != null || searchBar.isSearching()) return false;
    if (!mc.options.keyDrop.matches(key, scanCode)) return false;
    if (!isModuleInstalled(HaloModule.QUANTUM_INSERTER)) return false;
    if (ItemRequestingHalo.getBoundSenderPosition(haloItem) == null) return false;
    ItemStack selected = mc.player.getInventory().getSelected();
    return !selected.isEmpty() && !(selected.getItem() instanceof ItemRequestingHalo);
  }

  public void onCharEvent(int codePoint, int modifiers) {
    if (isOpenClose()) return;
    searchBar.typeChar(codePoint, modifiers);
  }

  public void handleUpdatePacket(List<ItemStack> newList) {
    if (drainManaOrClose(ModConfig.COMMON.requestingHaloUpdateConsumption.get())) {
      itemList.handleUpdatePacket(newList);

      if (!isOpenClose()) {
        if (!ItemRequestingHalo.isModuleInstalled(haloItem, HaloModule.UPDATE)) {
          playSound(ModSounds.haloListUpdate, 1F);
        }
      }
    }
  }

  public void handleRequestResultPacket(int requestId, int successAmount) {
    itemList.handleRequestResultPacket(requestId, successAmount);
  }

  private int lastRequestTick = 0;

  public void requestItemListUpdate() {
    if (tick - lastRequestTick >= 5 || tick == 0) {
      ModPacketHandler.sendToServer(new CPacketRequestItemListUpdate(slot));
      lastRequestTick = tick;
    }
  }

  public boolean isOpenClose() {
    return opening || closing;
  }

  public void playSound(double x, double y, double z, SoundEvent sound, float volume, float pitch) {
    if (mc.level != null) {
      ClientLevel world = mc.level;
      world.playLocalSound(x, y, z, sound, SoundSource.PLAYERS, volume, pitch, false);
    }
  }

  public void playSound(double x, double y, double z, SoundEvent sound, float pitch) {
    playSound(x, y, z, sound, 1F, pitch);
  }

  public void playSound(SoundEvent sound, float volume, float pitch) {
    playSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), sound, volume, pitch);
  }

  public void playSound(SoundEvent sound, float pitch) {
    playSound(sound, 1F, pitch);
  }

  public HaloCraftingInterface getCraftingInterface() {
    return craftingInterface;
  }

  public double getRotationOffset() {
    return rotationOffset;
  }

  public double getRelativeRotation() {
    return relativeRotation;
  }

  public int getListHeight() {
    return itemList.getHeight();
  }

  public String getSearchString() {
    return searchBar.getSearchString();
  }

  public Vec2d getSelectionTargetPos() {
    AnimatedItemStack target = selectionBox.getTarget();
    return target == null ? null : target.getPos();
  }

  public Vector3d getAnchoredWorldPos() {
    return anchoredWorldPos;
  }
}
