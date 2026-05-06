package shblock.interactivecorporea.client.render.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import shblock.interactivecorporea.IC;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class SimpleShaderProgram implements ResourceManagerReloadListener {
  private static final Minecraft mc = Minecraft.getInstance();
  private static final String PREFIX = "shaders/";

  private final ResourceLocation vertexLocation;
  private final ResourceLocation fragmentLocation;
  private final boolean hasVert;

  private int vert = 0;
  private int frag = 0;
  private int program = 0;

  private final Consumer<SimpleShaderProgram> reloadCallback;

  public SimpleShaderProgram(@Nullable String vertLoc, String fragLoc, @Nullable Consumer<SimpleShaderProgram> reloadCallback) {
    if (vertLoc != null) {
      vertexLocation = new ResourceLocation(IC.MODID, PREFIX + vertLoc + ".vert");
      hasVert = true;
    } else {
      vertexLocation = null;
      hasVert = false;
    }
    fragmentLocation = new ResourceLocation(IC.MODID, PREFIX + fragLoc + ".frag");
    this.reloadCallback = reloadCallback;
    ResourceManager resourceManager = mc.getResourceManager();
    if (resourceManager instanceof ReloadableResourceManager) {
      ((ReloadableResourceManager) resourceManager).registerReloadListener(this);
    }

    try {
      load(mc.getResourceManager());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public SimpleShaderProgram(String fragLoc, @Nullable Consumer<SimpleShaderProgram> reloadCallback) {
    this(null, fragLoc, reloadCallback);
  }

  private void load(ResourceManager resourceManager) throws IOException {
    if (hasVert) {
      vert = glCreateShader(GL_VERTEX_SHADER);
      glShaderSource(vert, readShaderSource(resourceManager, vertexLocation));
      glCompileShader(vert);
      if (glGetShaderi(vert, GL_COMPILE_STATUS) == GL_FALSE) {
        String info = glGetShaderInfoLog(vert);
        vert = 0;
        throw new IOException("Vertex shader " + vertexLocation + " compile failed:\n" + info);
      }
    }

    frag = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(frag, readShaderSource(resourceManager, fragmentLocation));
    glCompileShader(frag);
    if (glGetShaderi(frag, GL_COMPILE_STATUS) == GL_FALSE) {
      String info = glGetShaderInfoLog(frag);
      frag = 0;
      throw new IOException("Fragment shader " + fragmentLocation + " compile failed:\n" + info);
    }

    program = glCreateProgram();
    if (hasVert) {
      glAttachShader(program, vert);
    }
    glAttachShader(program, frag);
    glLinkProgram(program);
    if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
      String info = glGetProgramInfoLog(program);
      program = 0;
      throw new IOException("Shader " + fragmentLocation + " linking failed:\n" + info);
    }

    if (reloadCallback != null) {
      reloadCallback.accept(this);
    }
  }

  private static String readShaderSource(ResourceManager resourceManager, ResourceLocation location) throws IOException {
    try (InputStream stream = resourceManager.open(location)) {
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  public int getUniformLocation(String name) {
    return glGetUniformLocation(program, name);
  }

  public void use() {
    glUseProgram(program);
  }

  public void release() {
    glUseProgram(0);
  }

  @Override
  public void onResourceManagerReload(ResourceManager resourceManager) {
    try {
      load(resourceManager);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
