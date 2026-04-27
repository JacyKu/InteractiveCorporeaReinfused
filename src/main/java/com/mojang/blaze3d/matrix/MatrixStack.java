package com.mojang.blaze3d.matrix;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import org.joml.Quaternionfc;

public class MatrixStack extends PoseStack {
	public MatrixStack() {
		super();
	}

	public MatrixStack(PoseStack poseStack) {
		super();
		last().pose().set(poseStack.last().pose());
		last().normal().set(poseStack.last().normal());
	}

	public void push() {
		pushPose();
	}

	public void pop() {
		popPose();
	}

	public void rotate(Quaternionfc quaternion) {
		mulPose(new org.joml.Quaternionf(quaternion));
	}

	public Entry getLast() {
		return new Entry(last());
	}

	public static class Entry {
		private final Pose pose;

		private Entry(Pose pose) {
			this.pose = pose;
		}

		public Matrix4f getMatrix() {
			return new Matrix4f(pose.pose());
		}

		public Matrix3f getNormal() {
			return new Matrix3f(pose.normal());
		}
	}
}
