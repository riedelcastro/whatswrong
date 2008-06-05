package whatswrong;


import net.sf.epsgraphics.ColorMode;
import net.sf.epsgraphics.EpsGraphics;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author Sebastian Riedel
 */
public class NLPCanvas extends JPanel {

  private DependencyLayout spanLayout = new SpanLayout();
  private DependencyLayout dependencyLayout = new DependencyLayout();
  private TokenLayout tokenLayout = new TokenLayout();

  private ArrayList<TokenVertex> tokens = new ArrayList<TokenVertex>();
  private LinkedList<Edge> dependencies = new LinkedList<Edge>();

  private BufferedImage tokenImage = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
  private BufferedImage dependencyImage = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
  private BufferedImage spanImage = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);

  private DependencyTypeFilter dependencyTypeFilter = new DependencyTypeFilter();
  private DependencyLabelFilter dependencyLabelFilter = new DependencyLabelFilter();
  private DependencyTokenFilter dependencyTokenFilter = new DependencyTokenFilter();
  private TokenFilter tokenFilter = new TokenFilter();
  private Set<String> usedTypes = new HashSet<String>();
  private Set<TokenProperty> usedProperties = new java.util.HashSet<TokenProperty>();

  private ArrayList<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
  private boolean antiAliasing = true;
  private NLPInstance nlpInstance;

  public interface Listener {
    void instanceChanged();

    void redrawn();
  }

  private ArrayList<Listener> listeners = new ArrayList<Listener>();

  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  public NLPCanvas() {
    setPreferredSize(new Dimension(300, 300));
    setOpaque(false);
    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        Point point = e.getPoint();
        point.translate(0, -(getHeight() - tokenLayout.getHeight() - spanLayout.getHeight()));
        Edge edge = spanLayout.getEdgeAt(point, 5);
        //System.out.println("edge = " + edge);
        if (edge != null) {
          if (e.isMetaDown())
            spanLayout.toggleSelection(edge);
          else
            spanLayout.select(edge);

          updateNLPGraphics();
        }
      }
    });
  }


  public boolean isAntiAliasing() {
    return antiAliasing;
  }

  public void setAntiAliasing(boolean antiAliasing) {
    this.antiAliasing = antiAliasing;
  }

  public void addChangeListenger(ChangeListener changeListener) {
    changeListeners.add(changeListener);
  }

  private void fireChanged() {
    ChangeEvent event = new ChangeEvent(this);
    for (ChangeListener changeListener : changeListeners) {
      changeListener.stateChanged(event);
    }
  }

  private void fireInstanceChanged() {
    for (Listener l : listeners) l.instanceChanged();
  }

  private void fireRedrawn() {
    for (Listener l : listeners) l.redrawn();
  }

  public DependencyTypeFilter getDependencyTypeFilter() {
    return dependencyTypeFilter;
  }


  public DependencyLabelFilter getDependencyLabelFilter() {
    return dependencyLabelFilter;
  }


  public DependencyTokenFilter getDependencyTokenFilter() {
    return dependencyTokenFilter;
  }


  public TokenFilter getTokenFilter() {
    return tokenFilter;
  }


  public void setNLPInstance(NLPInstance nlpInstance) {
    dependencies.clear();
    dependencies.addAll(nlpInstance.getEdges());
    usedTypes.clear();
    for (Edge edge : dependencies)
      usedTypes.add(edge.getType());
    tokens.clear();
    tokens.addAll(nlpInstance.getTokens());
    usedProperties.clear();
    for (TokenVertex token : tokens) {
      usedProperties.addAll(token.getPropertyTypes());
    }
    spanLayout.clearSelection();
    fireInstanceChanged();
    //updateNLPGraphics();
  }

  public Set<TokenProperty> getUsedProperties() {
    return Collections.unmodifiableSet(usedProperties);
  }

  public Set<String> getUsedTypes() {
    return Collections.unmodifiableSet(usedTypes);
  }

  public void setColorForDependencyType(String type, Color color) {
    spanLayout.setColor(type, color);
  }

  private NLPInstance filterInstance() {
    return dependencyTokenFilter.filter(dependencyLabelFilter.filter(dependencyTypeFilter.filter(
      tokenFilter.filter(new NLPInstance(tokens, dependencies)))));
  }

  public void updateNLPGraphics() {
    Graphics2D gTokens = tokenImage.createGraphics();
    NLPInstance filtered = filterInstance();
    Collection<TokenVertex> tokens = new ArrayList<TokenVertex>(filtered.getTokens());

    //layout tokens first    
    tokenLayout.layout(tokens, gTokens);
    tokenImage = new BufferedImage(tokenLayout.getWidth(), tokenLayout.getHeight(),
      BufferedImage.TYPE_4BYTE_ABGR);
    gTokens = tokenImage.createGraphics();
    tokenLayout.layout(tokens, gTokens);

    //layout dependencies
    Collection<Edge> dependencies = new ArrayList<Edge>(filtered.getEdges(Edge.RenderType.dependency));
    Graphics2D gDependencies = dependencyImage.createGraphics();
    dependencyLayout.layout(dependencies, tokenLayout, gDependencies);
    dependencyImage = new BufferedImage(dependencyLayout.getWidth(), dependencyLayout.getHeight(),
      BufferedImage.TYPE_4BYTE_ABGR);
    gDependencies = dependencyImage.createGraphics();
    if (antiAliasing)
      gDependencies.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    dependencyLayout.layout(dependencies, tokenLayout, gDependencies);

    //layout spans
    Collection<Edge> spans = new ArrayList<Edge>(filtered.getEdges(Edge.RenderType.span));
    Graphics2D gSpans = spanImage.createGraphics();
    spanLayout.layout(spans, tokenLayout, gSpans);
    spanImage = new BufferedImage(spanLayout.getWidth(), spanLayout.getHeight(),
      BufferedImage.TYPE_4BYTE_ABGR);
    gSpans= spanImage.createGraphics();
    if (antiAliasing)
      gSpans.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    spanLayout.layout(spans, tokenLayout, gSpans);


    int width = spanLayout.getWidth();
    int height = dependencyLayout.getHeight() + tokenLayout.getHeight() + spanLayout.getHeight();
    setPreferredSize(new Dimension(width, height));
    setMinimumSize(new Dimension(width, height));
    setSize(new Dimension(width, getHeight()));
    repaint();
    invalidate();
    //invalidate();
    fireChanged();
    fireRedrawn();
  }

  private Collection<TokenVertex> filterTokens() {
    return tokenFilter.filterTokens(this.tokens);
  }

  private Collection<Edge> filterDependencies() {
    return dependencyTokenFilter.filterEdges(
      dependencyLabelFilter.filterEdges(
        dependencyTypeFilter.filterEdges(this.dependencies)));
  }


  public TokenLayout getTokenLayout() {
    return tokenLayout;
  }

  public DependencyLayout getSpanLayout() {
    return spanLayout;
  }

  public DependencyLayout getDependencyLayout() {
    return dependencyLayout;
  }

  public void clear() {
    tokens.clear();
    dependencies.clear();
    usedTypes.clear();
  }


  public void paintComponent(Graphics graphics) {
    Graphics2D g2d = (Graphics2D) graphics;
    //g2d.setColor(Color.WHITE);
    //g2d.fillRect(0,0,getWidth(),getHeight());
    int y = getHeight() - dependencyImage.getHeight() - tokenImage.getHeight() - spanImage.getHeight();
    g2d.drawImage(dependencyImage, 0, y, this);
    g2d.drawImage(tokenImage, 0, y + dependencyImage.getHeight(), this);
    g2d.drawImage(spanImage, 0, y + tokenImage.getHeight() + dependencyImage.getHeight(), this);
  }


  public void exportToEPS(File file) throws IOException {

    EpsGraphics dummy = new EpsGraphics("Title", new ByteArrayOutputStream(), 0, 0,
      tokenLayout.getWidth(), spanLayout.getHeight() + tokenLayout.getHeight(), ColorMode.BLACK_AND_WHITE);

    NLPInstance filtered = filterInstance();

    Collection<Edge> edges = filtered.getEdges();
    Collection<TokenVertex> tokens = filtered.getTokens();

    tokenLayout.layout(tokens, dummy);
    spanLayout.layout(edges, tokenLayout, dummy);

    EpsGraphics g = new EpsGraphics("Title", new FileOutputStream(file), 0, 0,
      tokenLayout.getWidth(), spanLayout.getHeight() + tokenLayout.getHeight(), ColorMode.BLACK_AND_WHITE);

    g.translate(0, spanLayout.getHeight());
    tokenLayout.layout(tokens, g);
    g.translate(0, -spanLayout.getHeight());
    spanLayout.layout(edges, tokenLayout, g);
    g.flush();
    g.close();
    //To change body of created methods use File | Settings | File Templates.
  }
}
