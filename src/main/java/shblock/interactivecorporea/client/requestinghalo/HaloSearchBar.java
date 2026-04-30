package shblock.interactivecorporea.client.requestinghalo;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import shblock.interactivecorporea.client.render.RenderUtil;
import shblock.interactivecorporea.client.util.RenderTick;
import shblock.interactivecorporea.common.util.MathUtil;

public class HaloSearchBar {
  private static final Minecraft mc = Minecraft.getInstance();
  private static final int DEFAULT_TEXT_COLOR = 0xFFFFFFFF;
  private static final int PREFIX_TEXT_COLOR = 0xFFFFFF00;
  private static final int CREATIVE_TAB_PREFIX_TEXT_COLOR = 0xFFFF00FF;

  private boolean searching;
  private String searchString = "";
  private int selectionStart;
  private int selectionEnd;
  private boolean hasMatches = true;

  private Runnable updateCallback;

  public void render(MatrixStack ms, double radius, double haloHeight) {
    ms.push();
    float textScale = .02F;
    double barHeight = mc.font.lineHeight * textScale / 2F + .025;
    ms.translate(0, haloHeight + barHeight + .1, 0);
    float alpha = .6F;
    float[] color;
    if (searching) {
      color = hasMatches || searchString.isEmpty()
          ? new float[]{0F, .5F, 1F, alpha}
          : new float[]{1F, 0F, 0F, alpha};
    } else {
      color = new float[]{.5F, .5F, .5F, alpha};
    }
    float[] revCol = MathUtil.revertColor(color);
    double rot = RenderUtil.calcTextOnHaloRadians(mc.font, searchString, textScale, radius - .01);
    RenderUtil.renderPartialHalo(
      ms,
      radius,
      Math.max(rot / 2, .1F),
      barHeight,
      .1,
      color[0], color[1], color[2], color[3]
    );
    RenderUtil.renderTextOnHaloCentered(
        ms, mc.font, searchString, radius - .01, textScale,
        this::getSearchTextColor,
        charIndex -> {
          if (searching) {
            if (charIndex >= Math.min(selectionStart, selectionEnd) && charIndex < Math.max(selectionStart, selectionEnd))
              return MathUtil.colorToInt(revCol[0], revCol[1], revCol[2], alpha);
          }
          return 0;
        },
        charIndex -> {
          if (searching) {
            if (charIndex == selectionEnd)
              if ((RenderTick.total / 20) % 1 < .5) {
                return charIndex == searchString.length() ? '_' : '|';
              }
          }
          return (char) 0;
        }
    );
    ms.pop();
  }

  private int getSearchTextColor(int charIndex) {
    if (charIndex < 0 || charIndex >= searchString.length()) {
      return DEFAULT_TEXT_COLOR;
    }

    int segmentStart = charIndex;
    while (segmentStart > 0 && !Character.isWhitespace(searchString.charAt(segmentStart - 1))) {
      segmentStart--;
    }

    char prefix = searchString.charAt(segmentStart);
    if (prefix == '%') {
      return CREATIVE_TAB_PREFIX_TEXT_COLOR;
    }
    if (prefix == '@' || prefix == '$' || prefix == '&') {
      return PREFIX_TEXT_COLOR;
    }
    return DEFAULT_TEXT_COLOR;
  }

  private void fixSelection() {
    if (selectionStart < 0)
      selectionStart = 0;
    if (selectionEnd < 0)
      selectionEnd = 0;
    if (selectionStart > searchString.length())
      selectionStart = searchString.length();
    if (selectionEnd > searchString.length())
      selectionEnd = searchString.length();
  }

  public void backspace() {
    backspace(false);
  }

  public void backspace(boolean deleteWord) {
    if (!isSearching()) return;

    if (selectionStart != selectionEnd) {
      if (deleteSelectedRegion()) {
        updateSearch();
      }
      return;
    }

    if (selectionEnd <= searchString.length() && !searchString.isEmpty() && selectionEnd != 0) {
      int start = deleteWord ? getPreviousWordStart(selectionEnd) : selectionEnd - 1;
      if (deleteRegion(start, selectionEnd)) {
        selectionEnd = start;
        selectionStart = selectionEnd;
        fixSelection();
        updateSearch();
      }
    }
  }

  private int getPreviousWordStart(int pos) {
    int index = MathHelper.clamp(pos, 0, searchString.length());
    while (index > 0 && Character.isWhitespace(searchString.charAt(index - 1))) {
      index--;
    }
    while (index > 0 && !Character.isWhitespace(searchString.charAt(index - 1))) {
      index--;
    }
    return index;
  }

  /**
   * Delete the part of string between start and end (start does not have to be below end)
   * @return if the search string got changed
   */
  private boolean deleteRegion(int start, int end) {
    if (start == end) return false;
    int a = Math.min(start, end);
    int b = Math.max(start, end);
    if (searchString.isEmpty()) return false;
    if (a >= 0 && b <= searchString.length()) {
      searchString = searchString.substring(0, a) + searchString.substring(b);
      return true;
    }
    return false;
  }

  private boolean deleteSelectedRegion() {
    boolean did = deleteRegion(selectionStart, selectionEnd);
    if (did) {
      selectionStart = Math.min(selectionStart, selectionEnd);
      selectionEnd = selectionStart;
      fixSelection();
      return true;
    }
    return false;
  }

  public void delete() {
    if (!isSearching()) return;

    if (selectionStart == selectionEnd) {
      if (selectionStart < searchString.length()) {
        searchString = searchString.substring(0, selectionEnd) + searchString.substring(selectionEnd + 1);
        updateSearch();
      }
    } else {
      if (deleteSelectedRegion()) {
        updateSearch();
      }
    }
  }

  private void insertString(int pos, String insert) {
    searchString = searchString.substring(0, pos) + insert + searchString.substring(pos);
  }

  private void insertStringToSelectionPos(String insert) {
    insertString(selectionEnd, insert);
    selectionEnd += insert.length();
    selectionStart = selectionEnd;
    fixSelection();
  }

  public void inputString(String addString) {
    if (addString.isEmpty()) return;
    if (!isSearching()) return;

    if (selectionStart != selectionEnd) {
      deleteSelectedRegion();
    }
    insertStringToSelectionPos(addString);

    updateSearch();
  }

  public void typeChar(int codePoint, int modifiers) {
    if (!isSearching()) return;

    String addString = "";
    for (char c : Character.toChars(codePoint)) {
      addString += c;
    }

    inputString(addString);
  }

  public void moveSelectionPos(int move, boolean moveStartPos) {
    selectionEnd += move;
    selectionEnd = MathHelper.clamp(selectionEnd, 0, searchString.length());

    if (moveStartPos) {
      selectionStart = selectionEnd;
    }
    fixSelection();
  }

  public void moveToStart() {
    selectionStart = 0;
    selectionEnd = selectionStart;
  }

  public void moveToEnd() {
    selectionStart = searchString.length();
    selectionEnd = selectionStart;
  }

  public void selectAll() {
    selectionStart = 0;
    selectionEnd = searchString.length();
  }

  private void updateSearch() {
    if (updateCallback != null) {
      updateCallback.run();
    }
  }

  public void copy() {
    if (selectionStart != selectionEnd) {
      mc.keyboardHandler.setClipboard(searchString.substring(Math.min(selectionStart, selectionEnd), Math.max(selectionStart, selectionEnd)));
    }
  }

  public void paste() {
    inputString(mc.keyboardHandler.getClipboard());
  }

  public void cut() {
    copy();
    deleteSelectedRegion();
    updateSearch();
  }

  public String getSearchString() {
    return searchString;
  }

  public void setSearchString(String str) {
    this.searchString = str;
    fixSelection();
  }

  public void setSearching(boolean searching) {
    this.searching = searching;
  }

  public void setHasMatches(boolean hasMatches) {
    this.hasMatches = hasMatches;
  }

  public boolean isSearching() {
    return searching;
  }

  public void setUpdateCallback(Runnable callback) {
    this.updateCallback = callback;
  }
}
