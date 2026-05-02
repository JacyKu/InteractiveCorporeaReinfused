package shblock.interactivecorporea.client.requestinghalo;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import shblock.interactivecorporea.client.util.RenderTick;
import shblock.interactivecorporea.common.item.HaloInterfaceStyle;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import shblock.interactivecorporea.common.util.MathUtil;
import shblock.interactivecorporea.common.util.Vec2d;

import java.util.List;

public class RemoteRequestingHaloInterface {
	private static final Minecraft mc = Minecraft.getInstance();
	private static final MultiBufferSource.BufferSource TEXT_BUFFERS = MultiBufferSource.immediate(new BufferBuilder(64));

	private final int playerId;
	private final AnimatedCorporeaItemList itemList;
	private final AnimatedItemSelectionBox selectionBox = new AnimatedItemSelectionBox(() -> {});
	private final HaloSearchBar searchBar = new HaloSearchBar();

	private boolean opening = true;
	private boolean closing;
	private double openCloseProgress;

	private double rotationOffset;
	private double relativeRotation;
	private boolean hasSelection;
	private Vec2d selectionPos = new Vec2d();
	private HaloInterfaceStyle interfaceStyle;
  private int haloTint;
	private final double radius = 2F;
	private final double height = 1F;
	private double itemSpacing;
	private double itemRotSpacing;
	private double itemZOffset;

public RemoteRequestingHaloInterface(int playerId, float rotationOffset, int listHeight, boolean sortByAmount, List<ItemStack> itemList, HaloInterfaceStyle interfaceStyle, int haloTint) {
    this.playerId = playerId;
    this.rotationOffset = rotationOffset;
    this.relativeRotation = 36F;
    this.interfaceStyle = interfaceStyle;
    this.haloTint = haloTint;
    this.itemList = new AnimatedCorporeaItemList(clampListHeight(listHeight));
    this.searchBar.setSearching(false);
    update(rotationOffset, listHeight, sortByAmount, itemList, interfaceStyle, haloTint);
	}

	public void startClose() {
		if (closing) return;
		opening = false;
		closing = true;
	}

	public void update(float rotationOffset, int listHeight, boolean sortByAmount, List<ItemStack> itemList, HaloInterfaceStyle interfaceStyle, int haloTint) {
		if (closing) {
			closing = false;
			opening = true;
		}
		this.rotationOffset = rotationOffset;
		this.interfaceStyle = interfaceStyle;
		this.haloTint = haloTint;
		updateListHeight(listHeight);
		if (sortByAmount) {
			this.itemList.setSortMode(SortMode.AMOUNT);
		}
		this.itemList.handleUpdatePacket(itemList);
		this.searchBar.setHasMatches(this.itemList.hasItemsMatchingFilter());
	}

	public void updateView(float rotationOffset, float relativeRotation, int listHeight, boolean hasSelection, float selectionX, float selectionY, String searchString) {
		this.rotationOffset = rotationOffset;
		this.relativeRotation = relativeRotation;
		this.hasSelection = hasSelection;
		this.selectionPos.set(selectionX, selectionY);
		updateListHeight(listHeight);
		updateSearch(searchString == null ? "" : searchString);
	}

	public boolean render(MatrixStack poseStack, Camera camera, float partialTicks) {
		if (!updateOpenClose()) {
			itemList.removeAll();
			return false;
		}

		Player player = getPlayer();
		if (player == null) {
			return false;
		}

		Vec3 cameraPos = camera.getPosition();
		Vec3 eyePos = player.getEyePosition(partialTicks);

		poseStack.push();
		poseStack.translate(eyePos.x - cameraPos.x, eyePos.y - cameraPos.y, eyePos.z - cameraPos.z);

		renderHaloBody(poseStack);
		renderItems(poseStack);

		if (!searchBar.getSearchString().isEmpty()) {
			renderSearchBar(poseStack);
		}

		poseStack.pop();

		return true;
	}

	public void tick() {
		itemList.tick();
	}

	private Player getPlayer() {
		if (mc.level == null) {
			return null;
		}
		Entity entity = mc.level.getEntity(playerId);
		return entity instanceof Player player ? player : null;
	}

	private void renderHaloBody(MatrixStack poseStack) {
		poseStack.push();
		poseStack.rotate(new Quaternion(Vector3f.YP, (float) (-rotationOffset - relativeRotation), true));
		double progress = Math.sin((Math.PI / 2F) * openCloseProgress);
		HaloInterfaceBackground.render(poseStack, radius, height, progress, interfaceStyle, ItemRequestingHalo.unpackTint(haloTint), rotationOffset + relativeRotation);
		poseStack.pop();
	}

	private void renderItems(MatrixStack poseStack) {
		double fadeWidth = .3;
		double fadeDegrees = Math.toDegrees(fadeWidth);
		double widthDegrees = Math.toDegrees(Math.sin((Math.PI / 2F) * openCloseProgress) * (Math.PI * .25F));

		itemList.update(RenderTick.delta);
		poseStack.push();
		poseStack.rotate(new Quaternion(Vector3f.YP, (float) -rotationOffset, true));
		double scale = 1D / itemList.getHeight() * height * 2D;
		itemSpacing = 1D / itemList.getHeight() * 2D * height;
		itemRotSpacing = MathUtil.calcRadiansFromChord(radius, itemSpacing);
		itemZOffset = MathUtil.calcChordCenterDistance(radius, itemSpacing);

		double colOffset = itemList.getColumnOffset();

		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		boolean haveSelectedItem = false;
		for (AnimatedItemStack animatedStack : itemList.getAnimatedList()) {
			if (updateSelectionBox(animatedStack)) {
				haveSelectedItem = true;
			}

			Vec2d pos = animatedStack.getPos();
			float rot = (float) ((pos.x - colOffset) * itemRotSpacing);
			double degreeDiff = Math.abs(relativeRotation - Math.toDegrees(rot));
			if (degreeDiff >= widthDegrees) {
				continue;
			}

			float currentScale = (float) (scale * Math.sin(MathHelper.clamp(widthDegrees - degreeDiff, 0F, fadeDegrees) / fadeDegrees * Math.PI * .5F));
			poseStack.push();
			poseStack.rotate(new Quaternion(Vector3f.YP, -rot, false));
			poseStack.translate(0F, -(pos.y - (itemList.getHeight() - 1D) / 2D) * itemSpacing, itemZOffset);
			poseStack.scale(currentScale, currentScale, currentScale);

			double perlinPos = RenderTick.total * .025;
			double motionScale = 1 / currentScale * .0375;
			poseStack.translate(
					animatedStack.noise.perlin(perlinPos, 0, 0) * motionScale,
					animatedStack.noise.perlin(0, perlinPos, 0) * motionScale,
					0F
			);

			animatedStack.renderItem(poseStack);

			poseStack.push();
			float textScale = 1F / 24F;
			poseStack.scale(textScale, textScale, textScale);
			poseStack.translate(-itemSpacing - 10D, -itemSpacing - 4D, -0.02);
			animatedStack.renderAmount(poseStack, 0x00FFFFFF | 0xFF << 24, TEXT_BUFFERS);
			poseStack.pop();

			poseStack.push();
			textScale = 1F / 18F;
			poseStack.scale(textScale, textScale, textScale);
			poseStack.translate(0F, 0F, -0.05);
			animatedStack.renderRequestResultAnimations(poseStack, buffers);
			poseStack.pop();

			poseStack.pop();
		}
		if (!haveSelectedItem || closing) {
			selectionBox.setTarget(null);
		}
		poseStack.pop();
		TEXT_BUFFERS.endBatch();

		selectionBox.update();

		poseStack.push();
		poseStack.rotate(new Quaternion(Vector3f.YP, (float) -rotationOffset, true));
		Vec2d selectionBoxPos = selectionBox.getPos();
		poseStack.rotate(Vector3f.YP.rotation((float) (-(selectionBoxPos.x - colOffset) * itemRotSpacing)));
		poseStack.translate(0F, -(selectionBoxPos.y - (itemList.getHeight() - 1) / 2F) * itemSpacing, itemZOffset);
		float selectionScale = (float) scale;
		poseStack.scale(selectionScale, selectionScale, selectionScale);
		selectionBox.render(poseStack);
		poseStack.pop();
	}

	private void renderSearchBar(MatrixStack poseStack) {
		poseStack.push();
		poseStack.rotate(new Quaternion(Vector3f.YP, (float) (-rotationOffset - relativeRotation), true));
		poseStack.translate(0, 0, radius);
		poseStack.scale((float) Math.sin(openCloseProgress * Math.PI * .5), 1, (float) Math.max(Math.sin((openCloseProgress - .5) * Math.PI), .01));
		poseStack.translate(0, 0, -radius);
		searchBar.render(poseStack, radius, height);
		poseStack.pop();
	}

	private boolean updateSelectionBox(AnimatedItemStack animatedStack) {
		if (!hasSelection || animatedStack.isRemoved()) {
			return false;
		}

		Vec2d itemPos = animatedStack.getPos();
		if (Math.abs(itemPos.x - selectionPos.x) < .5F && Math.abs(itemPos.y - selectionPos.y) < .5F) {
			selectionBox.setTarget(animatedStack);
			return true;
		}
		return false;
	}

	private void updateSearch(String searchString) {
		if (searchString.equals(searchBar.getSearchString())) {
			searchBar.setHasMatches(itemList.hasItemsMatchingFilter());
			return;
		}
		searchBar.setSearchString(searchString);
		searchBar.setSearching(!searchString.isEmpty());
		itemList.setFilter(searchString);
		itemList.arrange();
		searchBar.setHasMatches(itemList.hasItemsMatchingFilter());
	}

	private void updateListHeight(int listHeight) {
		int clampedHeight = clampListHeight(listHeight);
		if (clampedHeight != itemList.getHeight()) {
			itemList.setHeight(clampedHeight);
			itemList.arrange();
		}
	}

	private static int clampListHeight(int listHeight) {
		return Math.max(1, Math.min(16, listHeight));
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
}
