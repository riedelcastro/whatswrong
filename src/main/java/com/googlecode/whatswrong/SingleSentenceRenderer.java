package com.googlecode.whatswrong;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A SingleSentenceRenderer renders an NLPInstance as a single sentence with spans drawn below the tokens, and
 * dependencies above the tokens.
 *
 * @author Sebastian Riedel
 */
public class SingleSentenceRenderer implements NLPCanvasRenderer {

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
     * Should lines be drawn using antialiasing.
     */
    private boolean antiAliasing = true;

    /**
     * Y coordinates where token layout starts
     */
    private int startOfTokens = 0;

    /**
     * Y coordinate where span layout starts
     */
    private int startOfSpans = 0;


    /**
     * Renders the given instance as a single sentence with spans drawn below tokens, and dependencies above tokens.
     *
     * @param instance   the instance to render
     * @param graphics2D the graphics object to draw upon
     * @return the width and height of the drawn object.
     * @see NLPCanvasRenderer#render(NLPInstance, Graphics2D)
     */
    public Dimension render(NLPInstance instance, Graphics2D graphics2D) {
        List<Token> tokens =
                new ArrayList<Token>(instance.getTokens());
        Collection<Edge> dependencies =
                new ArrayList<Edge>(instance.getEdges(Edge.RenderType.dependency));
        Collection<Edge> spans =
                new ArrayList<Edge>(instance.getEdges(Edge.RenderType.span));

        //get span required token widths
        Map<Token, Integer> widths =
                spanLayout.estimateRequiredTokenWidths(spans, graphics2D);

        //find token bounds
        Map<Token, Bounds1D> tokenXBounds =
                tokenLayout.estimateTokenBounds(instance, widths, graphics2D);

        if (antiAliasing) {
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }


        int width = 0;
        int height = 0;
        Dimension dim;

        //place dependencies on top
        dim = dependencyLayout.layoutEdges(dependencies, tokenXBounds, graphics2D);
        height += dim.height;
        startOfTokens = height;
        width = dim.width > width ? dim.width : width;

        //add tokens
        graphics2D.translate(0, dim.height);
        dim = tokenLayout.layout(instance, widths, graphics2D);
        height += dim.height;
        startOfSpans = height;
        width = dim.width > width ? dim.width : width;

        //add spans
        graphics2D.translate(0, dim.height);
        dim = spanLayout.layoutEdges(spans, tokenXBounds, graphics2D);
        height += dim.height;
        width = dim.width > width ? dim.width : width;

        return new Dimension(width, height + 1);
    }

    /**
     * Should anti-aliasing be used when drawing the graph.
     *
     * @param antiAliasing rue iff anti-aliasing should be used when drawing the graph.
     */
    public void setAntiAliasing(boolean antiAliasing) {
        this.antiAliasing = antiAliasing;
    }

    /**
     * Sets the margin between tokens.
     *
     * @param margin the margin between tokens.
     */
    public void setMargin(int margin) {
        tokenLayout.setMargin(margin);
    }


    /**
     * Returns the margin between tokens.
     *
     * @return the margin between tokens.
     */
    public int getMargin() {
        return tokenLayout.getMargin();
    }


    /**
     * @inheritDoc
     */
    public Edge getEdgeAt(Point2D p, int radius) {
        System.out.println("dependencyLayout height = " + dependencyLayout.getHeight());    
        if (p.getY() < startOfTokens) {
            return dependencyLayout.getEdgeAt(p, radius);
        }
        else {
            Point2D shifted = new Point2D.Double(p.getX(),
                    p.getY() - startOfSpans);
            return spanLayout.getEdgeAt(shifted,radius);
        }
    }

    /**
     * Controls the height of the graph.
     *
     * @param heightFactor an integer that indicates how high the graph should be.
     */
    public void setHeightFactor(int heightFactor) {
        dependencyLayout.setHeightPerLevel(heightFactor);
        spanLayout.setHeightPerLevel(heightFactor);
    }

    /**
     * Returns an integer that reflects the height of the graph.
     *
     * @return an integer that reflects the height of the graph. The higher this value, the higher the graph.
     */
    public int getHeightFactor() {
        return dependencyLayout.getHeightPerLevel();
    }

    /**
     * Controls whether the graph should be curved or rectangular. If curved the dependencies are drawn as curves instead
     * of rectangular lines, and spans are drawn as rounded rectangles.
     *
     * @param isCurved should the graph be more curved.
     * @see NLPCanvasRenderer#setCurved(boolean)
     */
    public void setCurved(boolean isCurved) {
        dependencyLayout.setCurve(isCurved);
        spanLayout.setCurve(isCurved);
    }

    /**
     * Returns whether the renderer draws a more curved graph or not.
     *
     * @return true iff the renderer draws a more curved graph.
     */
    public boolean isCurved() {
        return dependencyLayout.isCurve();
    }

    /**
     * Set the color for edges of a certain type.
     *
     * @param edgeType the type of the edges we want to change the color for.
     * @param color    the color of the edges of the given type.
     */
    public void setEdgeTypeColor(String edgeType, Color color) {
        dependencyLayout.setColor(edgeType, color);
        spanLayout.setColor(edgeType, color);
    }

    /**
     * Sets the order/vertical layer in which the area of a certain type should be drawn.
     *
     * @param edgeType the type we want to change the order for.
     * @param order    the order/vertical layer in which the area of the given type should be drawn.
     */
    public void setEdgeTypeOrder(String edgeType, int order) {
        spanLayout.setTypeOrder(edgeType, order);
    }

    /**
     * Should anti-aliasing be used when drawing the graph.
     *
     * @return true iff anti-aliasing should be used when drawing the graph.
     */
    public boolean isAntiAliasing() {
        return antiAliasing;
    }


}
