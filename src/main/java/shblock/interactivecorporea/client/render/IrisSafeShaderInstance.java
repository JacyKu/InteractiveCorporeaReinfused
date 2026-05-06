package shblock.interactivecorporea.client.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;

public class IrisSafeShaderInstance extends ShaderInstance {
  public IrisSafeShaderInstance(ResourceProvider resourceProvider, ResourceLocation resourceLocation, VertexFormat vertexFormat) throws IOException {
    super(resourceProvider, resourceLocation, vertexFormat);
  }

  @Override
  public void apply() {
    super.apply();
    OculusCompat.unlockDepthColor();
  }
}