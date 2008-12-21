package com.googlecode.whatswrong;

import com.googlecode.whatswrong.javautils.Pair;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A TokenLayout object lays out a collection of tokens in sequence by placing a
 * stack of property values of each token at a position corresponding to the
 * index of the token. The order in which the property values are stacked
 * depends on the level of each corresponding property. The first property (with
 * highest level) is rendered in black while the remaining property values are
 * rendered in gray.
 *
 * <p>Note that the TokenLayout remembers the bounds of each token property
 * stack and the text layout of each property value. This can be handy when
 * other layouts (e.g., {@link com.googlecode.whatswrong.DependencyLayout}) want
 * to connect the tokens.
 *
 * @author Sebastian Riedel
 */
public class TokenLayout {

  /**
   * Mapping from token and property index to the text layout of the
   * corresponding property value.
   */
  private HashMap<Pair<Token, Integer>, TextLayout>
    textLayouts = new HashMap<Pair<Token, Integer>, TextLayout>();

  /**
   * Mapping from token to its bounding box.
   */
  private HashMap<Token, Rectangle2D> bounds = new HashMap<Token, Rectangle2D>();

  /**
   * The height of each property value row in the stack.
   */
  private int rowHeight = 14;
  /**
   * Where should we start to draw the stacks.
   */
  private int baseline = 0;
  /**
   * The margin between tokens (i.e., their stacks).
   */
  private int margin = 20;

  private int fromSplitPoint = -1;
  private int toSplitPoint = -1;

  /**
   * the total width of the graph that consists of all token stacks next to each
   * other.
   */
  private int width;

  /**
   * the total height of the graph that consists of all token stacks next to
   * each other.
   */
  private int height;


  /**
   * Sets the height of each property value row in the stack.
   *
   * @param rowHeight the height of each property value row in the stack.
   */
  public void setRowHeight(final int rowHeight) {
    this.rowHeight = rowHeight;
  }

  /**
   * Sets the y value at which the token layout should start.
   *
   * @param baseline the y value at which the token layout should start.
   */
  public void setBaseline(final int baseline) {
    this.baseline = baseline;
  }

  public int getFromSplitPoint() {
    return fromSplitPoint;
  }

  public void setFromSplitPoint(int fromSplitPoint) {
    this.fromSplitPoint = fromSplitPoint;
  }

  public int getToSplitPoint() {
    return toSplitPoint;
  }

  public void setToSplitPoint(int toSplitPoint) {
    this.toSplitPoint = toSplitPoint;
  }

  /**
   * Sets the margin between token stacks.
   *
   * @param margin the margin between token stacks.
   */
  public void setMargin(final int margin) {
    this.margin = margin;
  }


  /**
   * Gets the height of each property value row in the stack.
   *
   * @return the height of each property value row in the stack.
   */
  public int getRowHeight() {
    return rowHeight;
  }

  /**
   * Gets the y value at which the token layout should start.
   *
   * @return the y value at which the token layout should start.
   */
  public int getBaseline() {
    return baseline;
  }

  /**
   * Returns the margin between token stacks.
   *
   * @return the margin between token stacks.
   */
  public int getMargin() {
    return margin;
  }


  public Map<Token, Bounds1D> estimateTokenBounds(
    final NLPInstance instance,
    final Map<Token, Integer> tokenWidths,
    final Graphics2D g2d) {

    HashMap<Token, Bounds1D>
      result = new HashMap<Token, Bounds1D>();
    height = 0;

    List<Token> tokens = instance.getTokens();

    if (tokens.size() == 0) {
      return result;
    }
    int lastx = 0;

    int fromToken = fromSplitPoint == -1 ? 0 :
      instance.getSplitPoints().get(fromSplitPoint);
    int toToken = toSplitPoint == -1 ? tokens.size() :
      instance.getSplitPoints().get(toSplitPoint);

    for (int tokenIndex = fromToken; tokenIndex < toToken; ++tokenIndex) {
      Token token = tokens.get(tokenIndex);
      Font font = g2d.getFont();//Font.getFont("Helvetica-bold-italic");
      FontRenderContext frc = g2d.getFontRenderContext();
      int maxX = 0;
      int lasty = baseline + rowHeight;
      for (TokenProperty p : token.getSortedProperties()) {
        String property = token.getProperty(p);
        TextLayout layout = new TextLayout(property, font, frc);
        lasty += rowHeight;
        if (layout.getBounds().getMaxX() > maxX)
          maxX = (int) layout.getBounds().getMaxX();

      }
      Integer requiredWidth = tokenWidths.get(token);
      if (requiredWidth != null && maxX < requiredWidth) maxX = requiredWidth;
      result.put(token, new Bounds1D(lastx, lastx + maxX));
      lastx += maxX + margin;
      if (lasty - rowHeight > height) height = lasty - rowHeight;
    }
    return result;
  }


  /**
   * Lays out all tokens in the given collection as stacks of property values
   * that are placed next to each other according the order of the tokens (as
   * indicated by their indices).
   *
   * <p>After this method has been called the properties of the layout (height,
   * width, bounding boxes of token stacks and text layouts of each property
   * value) can be queried by calling the appropriate get methods.
   *
   * @param instance
   * @param tokenWidths if some tokens need extra space (for example because
   *                    they have self loops in a {@link com.googlecode.whatswrong.DependencyLayout})
   *                    the space they need can be provided through this map.
   * @param g2d         the graphics object to draw to.
   * @return the dimension of the drawn graph.
   */
  public Dimension layout(final NLPInstance instance,
                          final Map<Token, Integer> tokenWidths,
                          final Graphics2D g2d) {
    List<Token> tokens = instance.getTokens();
    if (tokens.size() == 0) {
      height = 1;
      width = 1;
      return new Dimension(width, height);
    }
    textLayouts.clear();
    int lastx = 0;
    height = 0;

    g2d.setColor(Color.BLACK);

    int fromToken = fromSplitPoint == -1 ? 0 :
      instance.getSplitPoints().get(fromSplitPoint);
    int toToken = toSplitPoint == -1 ? tokens.size() :
      instance.getSplitPoints().get(toSplitPoint);

    for (int tokenIndex = fromToken; tokenIndex < toToken; ++tokenIndex) {
      Token token = tokens.get(tokenIndex);
      Font font = g2d.getFont();//Font.getFont("Helvetica-bold-italic");
      FontRenderContext frc = g2d.getFontRenderContext();
      int index = 0;
      int lasty = baseline + rowHeight;
      int maxX = 0;
      for (TokenProperty p : token.getSortedProperties()) {
        String property = token.getProperty(p);
        g2d.setColor(index == 0 ? Color.BLACK : Color.GRAY);
        TextLayout layout = new TextLayout(property, font, frc);
        layout.draw(g2d, lastx, lasty);
        lasty += rowHeight;
        if (layout.getBounds().getMaxX() > maxX)
          maxX = (int) layout.getBounds().getMaxX();
        textLayouts.put(new Pair<Token, Integer>(token, index++), layout);
      }
      Integer requiredWidth = tokenWidths.get(token);
      if (requiredWidth != null && maxX < requiredWidth) maxX = requiredWidth;
      bounds.put(token, new Rectangle(lastx, baseline, maxX, lasty - baseline));
      lastx += maxX + margin;
      if (lasty - rowHeight > height) height = lasty - rowHeight;
    }
    width = lastx - margin;
    return new Dimension(width, height + 1);
  }

  /**
   * Returns the text layout for a given property and property index in the
   * stack.
   *
   * @param vertex the token for which we want the text layout of a propery of
   *               it.
   * @param index  the index of the property in the stack.
   * @return the text layout of the property value at index <code>index</code>
   *         of the stack for the token <code>vertex</code>
   */
  public TextLayout getPropertyTextLayout(final Token vertex, final int index) {
    return textLayouts.get(new Pair<Token, Integer>(vertex, index));
  }

  /**
   * Gets the bounds of the property value stack of the given token.
   *
   * @param vertex the token for which to get the bounds for.
   * @return a bounding box around the stack of property values for the given
   *         token.
   */
  public Rectangle2D getBounds(Token vertex) {
    return bounds.get(vertex);
  }

  /**
   * Gets the total width of this TokenLayout (covering all token stacks).
   *
   * @return the total width of this TokenLayout.
   */
  public int getWidth() {
    return width;
  }

  /**
   * Gets the total height of this TokenLayout (covering all token stacks).
   *
   * @return the total width of this TokenLayout.
   */
  public int getHeight() {
    return height + 4;
  }
}
