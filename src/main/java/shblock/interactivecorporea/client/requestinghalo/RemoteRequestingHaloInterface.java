package shblock.interactivecorporea.client.requestinghalo;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Camera;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class RemoteRequestingHaloInterface {
	private boolean closing;

	public RemoteRequestingHaloInterface(int playerId, float rotationOffset, int listHeight, boolean sortByAmount, List<ItemStack> itemList) {
	}

	public void startClose() {
		closing = true;
	}

	public void update(float rotationOffset, int listHeight, boolean sortByAmount, List<ItemStack> itemList) {
	}

	public void updateView(float rotationOffset, float relativeRotation, int listHeight, boolean hasSelection, float selectionX, float selectionY, String searchString) {
	}

	public boolean render(MatrixStack poseStack, Camera camera, float partialTicks) {
		return !closing;
	}

	public void tick() {
	}
}