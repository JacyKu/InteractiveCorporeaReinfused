package shblock.interactivecorporea.client.render.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.lwjgl.system.MemoryStack;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class RawShaderProgram implements ResourceManagerReloadListener {
  private static final Minecraft mc = Minecraft.getInstance();

  private final ResourceLocation vertexLocation;
  private final ResourceLocation fragmentLocation;
  @Nullable
  private final UnaryOperator<String> vertexTransformer;
  @Nullable
  private final UnaryOperator<String> fragmentTransformer;

  private int vertexShader = 0;
  private int fragmentShader = 0;
  private int program = 0;
  private final Map<String, Integer> uniformLocations = new HashMap<>();

  public RawShaderProgram(ResourceLocation vertexLocation, ResourceLocation fragmentLocation,
      @Nullable UnaryOperator<String> vertexTransformer, @Nullable UnaryOperator<String> fragmentTransformer) {
    this.vertexLocation = vertexLocation;
    this.fragmentLocation = fragmentLocation;
    this.vertexTransformer = vertexTransformer;
    this.fragmentTransformer = fragmentTransformer;

    ResourceManager resourceManager = mc.getResourceManager();
    if (resourceManager instanceof ReloadableResourceManager) {
      ((ReloadableResourceManager) resourceManager).registerReloadListener(this);
    }

    try {
      load(resourceManager);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public boolean isLoaded() {
    return program != 0;
  }

  public void use() {
    glUseProgram(program);
  }

  public void release() {
    glUseProgram(0);
  }

  public int getUniformLocation(String name) {
    return uniformLocations.computeIfAbsent(name, key -> glGetUniformLocation(program, key));
  }

  public void setUniform1f(String name, float value) {
    glUniform1f(getUniformLocation(name), value);
  }

  public void setUniform4f(String name, float x, float y, float z, float w) {
    glUniform4f(getUniformLocation(name), x, y, z, w);
  }

  public void setUniformMatrix4f(String name, org.joml.Matrix4f matrix) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      FloatBuffer buffer = stack.mallocFloat(16);
      matrix.get(buffer);
      glUniformMatrix4fv(getUniformLocation(name), false, buffer);
    }
  }

  @Override
  public void onResourceManagerReload(ResourceManager resourceManager) {
    try {
      load(resourceManager);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void load(ResourceManager resourceManager) throws IOException {
    String vertexSource = readShaderSource(resourceManager, vertexLocation);
    if (vertexTransformer != null) {
      vertexSource = vertexTransformer.apply(vertexSource);
    }

    String fragmentSource = readShaderSource(resourceManager, fragmentLocation);
    if (fragmentTransformer != null) {
      fragmentSource = fragmentTransformer.apply(fragmentSource);
    }

    vertexShader = glCreateShader(GL_VERTEX_SHADER);
    glShaderSource(vertexShader, vertexSource);
    glCompileShader(vertexShader);
    if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE) {
      String info = glGetShaderInfoLog(vertexShader);
      vertexShader = 0;
      throw new IOException("Vertex shader " + vertexLocation + " compile failed:\n" + info);
    }

    fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(fragmentShader, fragmentSource);
    glCompileShader(fragmentShader);
    if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE) {
      String info = glGetShaderInfoLog(fragmentShader);
      fragmentShader = 0;
      throw new IOException("Fragment shader " + fragmentLocation + " compile failed:\n" + info);
    }

    program = glCreateProgram();
    glAttachShader(program, vertexShader);
    glAttachShader(program, fragmentShader);
    glBindAttribLocation(program, 0, "Position");
    glBindAttribLocation(program, 1, "Color");
    glBindAttribLocation(program, 2, "UV0");
    glLinkProgram(program);
    if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
      String info = glGetProgramInfoLog(program);
      program = 0;
      throw new IOException("Shader " + fragmentLocation + " linking failed:\n" + info);
    }

    uniformLocations.clear();
  }

  private static String readShaderSource(ResourceManager resourceManager, ResourceLocation location) throws IOException {
    try (InputStream stream = resourceManager.open(location)) {
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}