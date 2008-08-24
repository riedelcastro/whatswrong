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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * An NLPCanvas is responsible for drawing the tokens and edges of an
 * NLPInstance using different edge and token layouts. In order to draw an
 * NLPInstance clients have to first set the instance to draw by calling {@link
 * whatswrong.NLPCanvas#setNLPInstance(NLPInstance)} and then update the
 * graphical representation by calling {@link NLPCanvas#updateNLPGraphics()}.
 * The latter method should also be called whenever changes are made to the
 * layout configuration (curved edges vs straight edges, antialiasing etc.).
 *
 * @author Sebastian Riedel
 * @see whatswrong.EdgeLayout
 * @see whatswrong.TokenLayout
 */
public class NLPCanvas extends JPanel {

  /**
   * The layout object for spans.
   */
  private SpanLayout spanLayout = new SpanLayout();
  /**
   * The layout object for dependencies.
   */
  private DependencyLayout dependencyLayout = new DependencyLayout();
  /**
   * The layout object for tokens.
   */
  private TokenLayout tokenLayout = new TokenLayout();

  /**
   * All tokens.
   */
  private ArrayList<Token> tokens = new ArrayList<Token>();
  /**
   * All edges.
   */
  private LinkedList<Edge> dependencies = new LinkedList<Edge>();

  /**
   * The image we write the token layout to.
   */
  private BufferedImage
    tokenImage = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
  /**
   * The image we write the dependency layout to.
   */
  private BufferedImage
    dependencyImage = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
  /**
   * The image we write the span layout to.
   */
  private BufferedImage
    spanImage = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);

  /**
   * A collection of all edge types used in the current nlp instance.
   */
  private Set<String> usedTypes = new HashSet<String>();
  /**
   * A collection of all token properties used in the current nlp instance.
   */
  private Set<TokenProperty>
    usedProperties = new java.util.HashSet<TokenProperty>();

  /**
   * The list of change listeners of this canvas.
   */
  private ArrayList<ChangeListener>
    changeListeners = new ArrayList<ChangeListener>();

  /**
   * Should lines and fonts should be drawn using anti-aliasing.
   */
  private boolean antiAliasing = true;

  /**
   * The filter that processes the current instance before it is drawn.
   */
  private NLPInstanceFilter filter;

  /**
   * A NLPCanvas.Listener is notified whenever the canvas is redrawn or when a
   * new instance has been set.
   */
  public interface Listener {
    /**
     * Called whenever the {@link NLPCanvas#setNLPInstance(NLPInstance)} method
     * is called.
     */
    void instanceChanged();

    /**
     * Called whenever the {@link NLPCanvas#updateNLPGraphics()} method is
     * called.
     */
    void redrawn();
  }

  /**
   * The set of listeners of this canvas.
   */
  private ArrayList<Listener> listeners = new ArrayList<Listener>();

  /**
   * Adds a new listener.
   *
   * @param listener the listener to add.
   */
  public void addListener(final Listener listener) {
    listeners.add(listener);
  }

  /**
   * Creates a new canvas with default size.
   */
  public NLPCanvas() {
    setPreferredSize(new Dimension(300, 300));
    setOpaque(false);
    addMouseListener(new MouseAdapter() {
      /**
       * Selects edges in the dependency layout.
       * @param e the event.
       */
      public void mousePressed(MouseEvent e) {
        Point point = e.getPoint();
        point.translate(0, -(getHeight() - tokenLayout.getHeight() -
          dependencyLayout.getHeight() - spanLayout.getHeight()));
        Edge edge = dependencyLayout.getEdgeAt(point, 5);
        //System.out.println("edge = " + edge);
        if (edge != null) {
          if (e.isMetaDown())
            dependencyLayout.toggleSelection(edge);
          else
            dependencyLayout.select(edge);

          updateNLPGraphics();
        }
      }
    });
  }


  /**
   * Should anti-aliasing be used when drawing the graph.
   *
   * @return true iff anti-aliasing is used when drawing the graph.
   */
  public boolean isAntiAliasing() {
    return antiAliasing;
  }

  /**
   * Should anti-aliasing be used when drawing the graph.
   *
   * @param antiAliasing rue iff anti-aliasing should be used when drawing the
   *                     graph.
   */
  public void setAntiAliasing(boolean antiAliasing) {
    this.antiAliasing = antiAliasing;
  }

  /**
   * Adds a change listener to this canvas.
   *
   * @param changeListener the listener to add.
   */
  public void addChangeListenger(ChangeListener changeListener) {
    changeListeners.add(changeListener);
  }

  /**
   * Fired whenever this canvas is changed.
   */
  private void fireChanged() {
    ChangeEvent event = new ChangeEvent(this);
    for (ChangeListener changeListener : changeListeners) {
      changeListener.stateChanged(event);
    }
  }

  /**
   * Notifies all listeners about an instance change event.
   */
  private void fireInstanceChanged() {
    for (Listener l : listeners) l.instanceChanged();
  }

  /**
   * Notifies all listeners about a redraw/update event.
   */
  private void fireRedrawn() {
    for (Listener l : listeners) l.redrawn();
  }

  /**
   * Sets the current NLP instance to draw. Note that this does not cause to
   * canvas to be immediately updated. For this {@link NLPCanvas#updateNLPGraphics()}
   * needs to be called.
   *
   * @param nlpInstance the new NLP instance.
   */
  public void setNLPInstance(final NLPInstance nlpInstance) {
    dependencies.clear();
    dependencies.addAll(nlpInstance.getEdges());
    usedTypes.clear();
    for (Edge edge : dependencies)
      usedTypes.add(edge.getType());
    tokens.clear();
    tokens.addAll(nlpInstance.getTokens());
    usedProperties.clear();
    for (Token token : tokens) {
      usedProperties.addAll(token.getPropertyTypes());
    }
    spanLayout.clearSelection();
    fireInstanceChanged();
    //updateNLPGraphics();
  }

  /**
   * Returns the set of all token properties in the current nlp instance.
   *
   * @return the set of all token properties in the current nlp instance.
   */
  public Set<TokenProperty> getUsedProperties() {
    return Collections.unmodifiableSet(usedProperties);
  }

  /**
   * Returns the set of all edge types in the current nlp instance.
   *
   * @return the set of all edge types in the current nlp instance.
   */
  public Set<String> getUsedTypes() {
    return Collections.unmodifiableSet(usedTypes);
  }


  /**
   * Returns the filter this canvas is applying to the nlp instance before it is
   * drawn.
   *
   * @return the filter of this canvas.
   */
  public NLPInstanceFilter getFilter() {
    return filter;
  }

  /**
   * Sets the filter this canvas should apply to the nlp instance before it is
   * drawn.
   *
   * @param filter the filter to use.
   */
  public void setFilter(NLPInstanceFilter filter) {
    this.filter = filter;
  }

  /**
   * Just calls the filter on the current instance.
   *
   * @return the filtered instance.
   */
  private NLPInstance filterInstance() {
    return filter.filter(new NLPInstance(tokens, dependencies));
  }

  /**
   * Updates the current graph. This takes into account all changes to the
   * filter, NLP instance and drawing parameters.
   */
  public void updateNLPGraphics() {
    NLPInstance filtered = filterInstance();

    //get edges and tokens
    Collection<Token> tokens =
      new ArrayList<Token>(filtered.getTokens());
    Collection<Edge> dependencies =
      new ArrayList<Edge>(filtered.getEdges(Edge.RenderType.dependency));
    Collection<Edge> spans =
      new ArrayList<Edge>(filtered.getEdges(Edge.RenderType.span));

    //create dummy graphics objects to estimate dimensions    
    Graphics2D gTokens = tokenImage.createGraphics();
    Graphics2D gDependencies = dependencyImage.createGraphics();
    Graphics2D gSpans = spanImage.createGraphics();

    //get span required token widths
    Map<Token, Integer> widths =
      spanLayout.estimateRequiredTokenWidths(spans, gSpans);

    //test layout to estimate sizes
    tokenLayout.layout(tokens, widths, gTokens);
    dependencyLayout.layout(dependencies, tokenLayout, gDependencies);
    spanLayout.layout(spans, tokenLayout, gSpans);

    //create a new images with right dimensions
    tokenImage = new BufferedImage(tokenLayout.getWidth(),
      tokenLayout.getHeight(),
      BufferedImage.TYPE_4BYTE_ABGR);
    dependencyImage = new BufferedImage(dependencyLayout.getWidth(),
      dependencyLayout.getHeight(),
      BufferedImage.TYPE_4BYTE_ABGR);
    spanImage = new BufferedImage(spanLayout.getWidth(),
      spanLayout.getHeight(),
      BufferedImage.TYPE_4BYTE_ABGR);

    //create the real graphics objects
    gTokens = tokenImage.createGraphics();
    gDependencies = dependencyImage.createGraphics();
    gSpans = spanImage.createGraphics();

    //specify rendering hints
    if (antiAliasing) {
      gDependencies.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
      gSpans.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    }

    //do the layout
    tokenLayout.layout(tokens, widths, gTokens);
    dependencyLayout.layout(dependencies, tokenLayout, gDependencies);
    spanLayout.layout(spans, tokenLayout, gSpans);


    int width = spanLayout.getWidth();
    int height = dependencyLayout.getHeight() + tokenLayout.getHeight()
      + spanLayout.getHeight();
    setPreferredSize(new Dimension(width, height));
    setMinimumSize(new Dimension(width, height));
    setSize(new Dimension(width, getHeight()));
    repaint();
    invalidate();
    //invalidate();
    fireChanged();
    fireRedrawn();
  }

  /**
   * Returns the token layout of this canvas.
   *
   * @return the token layout of this canvas.
   */
  public TokenLayout getTokenLayout() {
    return tokenLayout;
  }

  /**
   * Returns the span layout of this canvas.
   *
   * @return the span layout of this canvas.
   */
  public SpanLayout getSpanLayout() {
    return spanLayout;
  }

  /**
   * Returns the dependency layout of this canvas.
   *
   * @return the dependency layout of this canvas.
   */
  public DependencyLayout getDependencyLayout() {
    return dependencyLayout;
  }

  /**
   * Clears the current instance.
   */
  public void clear() {
    tokens.clear();
    dependencies.clear();
    usedTypes.clear();
    usedProperties.clear();
  }


  /**
   * Paint the canvas to the graphics object.
   *
   * @param graphics the graphics object to draw to.
   */
  public void paintComponent(Graphics graphics) {
    Graphics2D g2d = (Graphics2D) graphics;
    //g2d.setColor(Color.WHITE);
    //g2d.fillRect(0,0,getWidth(),getHeight());
    int y = getHeight() - dependencyImage.getHeight() - tokenImage.getHeight() - spanImage.getHeight();
    g2d.drawImage(dependencyImage, 0, y, this);
    g2d.drawImage(tokenImage, 0, y + dependencyImage.getHeight(), this);
    g2d.drawImage(spanImage, 0, y + tokenImage.getHeight() + dependencyImage.getHeight(), this);
  }


  /**
   * Exports the current graph to EPS.
   *
   * @param file the eps file to export to.
   * @throws IOException if IO goes wrong.
   */
  public void exportToEPS(File file) throws IOException {

    EpsGraphics dummy = new EpsGraphics("Title", new ByteArrayOutputStream(),
      0, 0, tokenLayout.getWidth(), spanLayout.getHeight()
      + tokenLayout.getHeight(), ColorMode.BLACK_AND_WHITE);

    NLPInstance filtered = filterInstance();

    //get edges and tokens
    Collection<Token> tokens =
      new ArrayList<Token>(filtered.getTokens());
    Collection<Edge> dependencies =
      new ArrayList<Edge>(filtered.getEdges(Edge.RenderType.dependency));
    Collection<Edge> spans =
      new ArrayList<Edge>(filtered.getEdges(Edge.RenderType.span));

    //create dummy graphics objects to estimate dimensions

    //get span required token widths
    Map<Token, Integer> widths =
      spanLayout.estimateRequiredTokenWidths(spans, dummy);

    //test layout to estimate sizes
    tokenLayout.layout(tokens, widths, dummy);
    dependencyLayout.layout(dependencies, tokenLayout, dummy);
    spanLayout.layout(spans, tokenLayout, dummy);

    //create actual EPS graphics object
    EpsGraphics g = new EpsGraphics("Title", new FileOutputStream(file), 0, 0,
      tokenLayout.getWidth() + 2,
      spanLayout.getHeight() + tokenLayout.getHeight()
        + dependencyLayout.getHeight(),
      ColorMode.COLOR_RGB);

    // do eps rendering
    dependencyLayout.layout(dependencies, tokenLayout, g);
    g.translate(0, dependencyLayout.getHeight());
    tokenLayout.layout(tokens, widths, g);
    g.translate(0, tokenLayout.getHeight());
    spanLayout.layout(spans, tokenLayout, g);

    g.flush();
    g.close();
  }
}
