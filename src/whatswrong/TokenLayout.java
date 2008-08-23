package whatswrong;

import javautils.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sebastian Riedel
 */
public class TokenLayout {

  private HashMap<Pair<Token, Integer>, TextLayout>
          textLayouts = new HashMap<Pair<Token, Integer>, TextLayout>();

  private HashMap<Token, Rectangle2D> bounds = new HashMap<Token, Rectangle2D>();

  private int rowHeight = 14;
  private int baseline = 0;
  private int margin = 20;
  private int width;
  private int height;


  public void setRowHeight(int rowHeight) {
    this.rowHeight = rowHeight;
  }

  public void setBaseline(int baseline) {
    this.baseline = baseline;
  }

  public void setMargin(int margin) {
    this.margin = margin;
  }


  public int getRowHeight() {
    return rowHeight;
  }

  public int getBaseline() {
    return baseline;
  }

  public int getMargin() {
    return margin;
  }

  public void layout(Collection<Token> tokens, Map<Token,Integer> tokenWidths, Graphics2D g2d) {
    if (tokens.size() == 0){
      height = 1;
      width = 1;
      return;
    }
    textLayouts.clear();
    int lastx = 0;
    height = 0;

    g2d.setColor(Color.BLACK);

    for (Token token : tokens) {
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
        textLayouts.put(new Pair<Token, Integer>(token, index++),layout);
      }
      Integer requiredWidth = tokenWidths.get(token);
      if (requiredWidth != null && maxX < requiredWidth) maxX = requiredWidth;
      bounds.put(token, new Rectangle(lastx,baseline,maxX, lasty - baseline));
      lastx+= maxX + margin;
      if (lasty - rowHeight > height) height = lasty - rowHeight;
    }
    width = lastx - margin;
  }

  public TextLayout getProperty(Token vertex, int index) {
    return textLayouts.get(new Pair<Token,Integer>(vertex,index));
  }

  public Rectangle2D getBounds(Token vertex){
    return bounds.get(vertex);
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height + 4;
  }
}
