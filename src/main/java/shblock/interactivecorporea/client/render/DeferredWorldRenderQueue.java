package shblock.interactivecorporea.client.render;

import java.util.ArrayList;
import java.util.List;

public final class DeferredWorldRenderQueue {
  private static final List<Runnable> DRAWS = new ArrayList<>();

  private DeferredWorldRenderQueue() {
  }

  public static void enqueue(Runnable draw) {
    DRAWS.add(draw);
  }

  public static void flush() {
    if (DRAWS.isEmpty()) {
      return;
    }
    try {
      for (Runnable draw : DRAWS) {
        draw.run();
      }
    } finally {
      DRAWS.clear();
    }
  }
}