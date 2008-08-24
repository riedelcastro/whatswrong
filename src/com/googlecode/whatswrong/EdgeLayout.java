package com.googlecode.whatswrong;

import java.util.Collection;
import java.awt.*;

/**
 * An EdgeLayout draws a set of edges onto a Graphics2D object.
 *
 * @author Sebastian Riedel
 */
public interface EdgeLayout {
  /**
   * Draws the edges onto the graphics object. In order to draw edges the method
   * need to know the layout of the tokens (i.e. the position of the tokens).
   *
   * @param edges       a set of edges.
   * @param tokenLayout a token layout.
   * @param g2d         the graphics object to draw to.
   */
  void layout(Collection<Edge> edges, TokenLayout tokenLayout, Graphics2D g2d);

  /**
   * The height of the layout for the last call to {@link
   * EdgeLayout#layout(java.util.Collection, TokenLayout,
   * java.awt.Graphics2D)}.
   *
   * @return the height in pixels.
   */
  int getHeight();

  /**
   * The width of the layout for the last call to {@link
   * EdgeLayout#layout(java.util.Collection, TokenLayout,
   * java.awt.Graphics2D)}.
   *
   * @return the height in pixels.
   */
  int getWidth();
}
