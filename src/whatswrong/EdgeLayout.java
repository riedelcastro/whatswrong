package whatswrong;

import java.util.Collection;
import java.awt.*;

/**
 * @author Sebastian Riedel
 */
public interface EdgeLayout {
  void layout(Collection<Edge> edges, TokenLayout tokenLayout, Graphics2D g2d);

  int getHeight();

  int getWidth();
}
