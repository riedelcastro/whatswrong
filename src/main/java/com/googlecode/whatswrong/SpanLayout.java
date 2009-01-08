package com.googlecode.whatswrong;


import com.googlecode.whatswrong.javautils.Counter;
import com.googlecode.whatswrong.javautils.HashMultiMapLinkedList;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A SpanLayouy lays out edges as rectangular blocks under or above the tokens that the edge covers. The label is
 * written into these blocks. If there are multiple edge types then all spans of the same type appear in the same
 * contiguous vertical area.
 *
 * @author Sebastian Riedel
 */
public class SpanLayout extends AbstractEdgeLayout {

    /**
     * Should the graph be upside-down reverted.
     */
    private boolean revert = true;

    /**
     * Should we draw separation lines between the areas for different span types.
     */
    private boolean separationLines = true;

    /**
     * The order/vertical layer in which the area of a certain type should be drawn.
     */
    private HashMap<String, Integer> orders = new HashMap<String, Integer>();

    /**
     * How much space should at least be between the label of a span and the right and left edges of the span.
     */
    private double totalTextMargin = 6.0;

    /**
     * Creates a new SpanLayout.
     */
    public SpanLayout() {
        baseline = 1;
    }

    /**
     * Sets the order/vertical layer in which the area of a certain type should be drawn.
     *
     * @param type  the type we want to change the order for.
     * @param order the order/vertical layer in which the area of the given type should be drawn.
     */
    public void setTypeOrder(final String type, final int order) {
        orders.put(type, order);
    }

    /**
     * Returns the order/vertical layer in which the area of a certain type should be drawn.
     *
     * @param type the type we want to get the order for.
     * @return the order/vertical layer in which the area of the given type should be drawn.
     */
    public int getOrder(String type) {
        Integer order = orders.get(type);
        return order == null ? Integer.MIN_VALUE : order;
    }

    /**
     * Should we draw separation lines between the areas for different span types.
     *
     * @return true iff separation lines should be drawn.
     */
    public boolean isSeparationLines() {
        return separationLines;
    }

    /**
     * Should we draw separation lines between the areas for different span types.
     *
     * @param separationLines true iff separation lines should be drawn.
     */
    public void setSeparationLines(final boolean separationLines) {
        this.separationLines = separationLines;
    }


    /**
     * For each token that has a self-loop we need the token to be wide enough. This method calculates the needed token
     * width for a given set of edges. That is, for all self-loops in the set of edges we calculate how wide the
     * corresponding token need to be.
     *
     * @param edges the set of edges that can contain self-loops.
     * @param g2d   the graphics object needed to find out the actual width of text.
     * @return A mapping from tokens with self-loops to pixel widths.
     */
    public Map<Token, Integer> estimateRequiredTokenWidths(
        final Collection<Edge> edges, final Graphics2D g2d) {

        HashMap<Token, Integer> result = new HashMap<Token, Integer>();
        for (Edge edge : edges) {
            if (edge.getFrom() == edge.getTo()) {
                Font font = new Font(g2d.getFont().getName(), Font.PLAIN, 8);
                FontRenderContext frc = g2d.getFontRenderContext();
                TextLayout layout = new TextLayout(edge.getLabel(), font, frc);
                Integer oldWidth = result.get(edge.getFrom());
                int width = oldWidth == null ? (int) layout.getBounds().getWidth() :
                    (int) Math.max(layout.getBounds().getWidth(), oldWidth);
                result.put(edge.getFrom(), (int) (width + totalTextMargin));
            }
        }


        return result;
    }

    /**
     * Lays out the edges as spans (blocks) under or above the tokens they contain.
     *
     * @param edges  the edges to layout.
     * @param bounds the bounds of the tokens the spans connect.
     * @param g2d    the graphics object to draw on.
     * @return the dimensions of the drawn graph.
     */
    public Dimension layoutEdges(Collection<Edge> edges,
                                 Map<Token, Bounds1D> bounds,
                                 Graphics2D g2d) {
        if (visible.size() > 0) {
            edges = new HashSet<Edge>(edges);
            edges.retainAll(visible);
        }

        //find out height of each edge
        shapes.clear();

        Counter<Edge> depth = new Counter<Edge>();
        Counter<Edge> offset = new Counter<Edge>();
        HashMultiMapLinkedList<Edge, Edge>
            dominates = new HashMultiMapLinkedList<Edge, Edge>();


        for (Edge over : edges)
            for (Edge under : edges) {
                int orderOver = getOrder(over.getTypePrefix());
                int orderUnder = getOrder(under.getTypePrefix());
                if (orderOver > orderUnder ||
                    orderOver == orderUnder && (
                        over.covers(under) ||
                            over.coversSemi(under) ||
                            over.coversExactly(under) && over.lexicographicOrder(under) > 0 ||
                            over.overlaps(under) && over.getMinIndex() < under.getMinIndex()
                    ))
                    dominates.add(over, under);

            }

        for (Edge edge : edges)
            calculateDepth(dominates, depth, edge);

        //calculate maxHeight and maxWidth
        int maxDepth = depth.getMaximum();
        int maxHeight = edges.size() > 0 ? (maxDepth + 1) * heightPerLevel + 3 : 1;
        //in case there are no edges that cover other edges (depth == 0) we need
        //to increase the height slightly because loops on the same token
        //have height of 1.5 levels

        //build map from vertex to incoming/outgoing edges
        HashMultiMapLinkedList<Token, Edge> vertex2edges = new HashMultiMapLinkedList<Token, Edge>();
        for (Edge edge : edges) {
            vertex2edges.add(edge.getFrom(), edge);
            vertex2edges.add(edge.getTo(), edge);
        }
        //assign starting and end points of edges by sorting the edges per vertex
        from = new HashMap<Edge, Point>();
        to = new HashMap<Edge, Point>();

        int maxWidth = 0;

        //draw each edge
        for (Edge edge : edges) {
            //set Color and remember old color
            Color old = g2d.getColor();
            g2d.setColor(getColor(edge.getType()));

            //prepare label (will be needed for spacing)
            Font font = new Font(g2d.getFont().getName(), Font.PLAIN, 8);
            FontRenderContext frc = g2d.getFontRenderContext();
            TextLayout layout = new TextLayout(edge.getLabel(), font, frc);

            //draw lines
            Integer spanLevel = revert ? maxDepth - depth.get(edge) : depth.get(edge);
            int height = baseline + maxHeight - (spanLevel + 1) * heightPerLevel + offset.get(edge);
            g2d.setStroke(getStroke(edge));

            int buffer = 2;

            Bounds1D fromBounds = bounds.get(edge.getFrom());
            Bounds1D toBounds = bounds.get(edge.getTo());
            int minX = Math.min(fromBounds.from, toBounds.from);
            int maxX = Math.max(fromBounds.to, toBounds.to);

            if (maxX > maxWidth) maxWidth = maxX + 1;

            if (maxX - minX < layout.getBounds().getWidth() + totalTextMargin) {
                double middle = minX + (maxX - minX) / 2.0;
                double textWidth = layout.getBounds().getWidth() + totalTextMargin;
                minX = (int) (middle - textWidth / 2.0);
                maxX = (int) (middle + textWidth / 2.0);
            }

            //connection
            //GeneralPath shape = createRectArrow(p1, p2, p3, p4);
            Shape shape = curve ?
                new RoundRectangle2D.Double(minX, height - buffer, maxX - minX, heightPerLevel - 2 * buffer, 8, 8) :
                new Rectangle2D.Double(minX, height - buffer, maxX - minX, heightPerLevel - 2 * buffer);

            //GeneralPath shape = createCurveArrow(curveLength, p1, p2, p3, p4);
            g2d.draw(shape);

            //write label in the middle under
            int labelx = minX + (maxX - minX) / 2 - (int) layout.getBounds().getWidth() / 2;
            int labely = height + heightPerLevel / 2;
            layout.draw(g2d, labelx, labely);
            g2d.setColor(old);
            shapes.put(shape, edge);

        }

        //int maxWidth = 0;
        for (Bounds1D bound1D : bounds.values())
            if (bound1D.to > maxWidth) maxWidth = bound1D.to;

        if (separationLines) {
            //find largest depth for each prefix type
            HashMap<String, Integer> minDepths = new HashMap<String, Integer>();
            for (Edge e : edges) {
                int edgeDepth = depth.get(e);
                Integer typeDepth = minDepths.get(e.getTypePrefix());
                if (typeDepth == null || typeDepth > edgeDepth) {
                    typeDepth = edgeDepth;
                    minDepths.put(e.getTypePrefix(), typeDepth);
                }
            }
            for (Integer d : minDepths.values()) {
                double height = baseline - 1 + ((!revert ? maxDepth - d : d) * heightPerLevel);
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.drawLine(0, (int) height, maxWidth, (int) height);

            }
        }


        return new Dimension(maxWidth, maxHeight);

    }


}