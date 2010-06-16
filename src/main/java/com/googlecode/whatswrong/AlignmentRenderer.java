package com.googlecode.whatswrong;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.Map;

/**
 * A SingleSentenceRenderer renders an NLPInstance as a single sentence with spans drawn below the tokens, and
 * dependencies above the tokens.
 *
 * @author Sebastian Riedel
 */
public class AlignmentRenderer implements NLPCanvasRenderer {

    /**
     * The layout object for tokens.
     */
    private TokenLayout tokenLayout1 = new TokenLayout();

    /**
     * The layout object for tokens.
     */
    private TokenLayout tokenLayout2 = new TokenLayout();


    /**
     * Should lines be drawn using antialiasing.
     */
    private boolean antiAliasing = true;

    private int heightFactor = 100;
    private boolean isCurved = true;


    public AlignmentRenderer() {
        tokenLayout1.setToSplitPoint(0);
        tokenLayout2.setFromSplitPoint(0);
    }

    /**
     * Renders the given instance as a single sentence with spans drawn below tokens, and dependencies above tokens.
     *
     * @param instance   the instance to render
     * @param graphics2D the graphics object to draw upon
     * @return the width and height of the drawn object.
     * @see com.googlecode.whatswrong.NLPCanvasRenderer#render(com.googlecode.whatswrong.NLPInstance,
     *      java.awt.Graphics2D)
     */
    public Dimension render(NLPInstance instance, Graphics2D graphics2D) {

        //find token bounds
        Map<Token, Bounds1D> tokenXBounds1 =
            tokenLayout1.estimateTokenBounds(instance,
                Collections.<Token, Integer>emptyMap(), graphics2D);

        Map<Token, Bounds1D> tokenXBounds2 =
            tokenLayout2.estimateTokenBounds(instance,
                Collections.<Token, Integer>emptyMap(), graphics2D);

        if (antiAliasing) {
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        }


        int width = 0;
        int height = 0;
        Dimension dim;

        //place dependencies on top
        dim = tokenLayout1.layout(instance, Collections.<Token, Integer>emptyMap(),
            graphics2D);
        height += dim.height;
        width = dim.width > width ? dim.width : width;

        for (Edge edge : instance.getEdges(Edge.RenderType.dependency)) {
            if ("FP".equals(edge.getTypePostfix()))
                graphics2D.setColor(Color.RED);
            else if ("FN".equals(edge.getTypePostfix()))
                graphics2D.setColor(Color.BLUE);
            else
                graphics2D.setColor(Color.BLACK);
            Bounds1D bound1 = tokenXBounds1.get(edge.getFrom());
            Bounds1D bound2 = tokenXBounds2.get(edge.getTo());
            if (isCurved) {
                GeneralPath shape = new GeneralPath();
                shape.moveTo(bound1.getMiddle(), height);
                shape.curveTo(bound1.getMiddle(), height + heightFactor / 2,
                    bound2.getMiddle(), height + heightFactor / 2,
                    bound2.getMiddle(), height + heightFactor);
                graphics2D.draw(shape);
            } else {
                graphics2D.drawLine(bound1.getMiddle(), height,
                    bound2.getMiddle(), height + heightFactor);
            }
        }

        //add spans
        graphics2D.translate(0, dim.height + heightFactor);
        dim = tokenLayout2.layout(instance, Collections.<Token, Integer>emptyMap(),
            graphics2D);
        height += dim.height + heightFactor;
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
        tokenLayout1.setMargin(margin);
        tokenLayout2.setMargin(margin);
    }


    /**
     * Returns the margin between tokens.
     *
     * @return the margin between tokens.
     */
    public int getMargin() {
        return tokenLayout1.getMargin();
    }

    public Edge getEdgeAt(Point2D p, int radius) {
        return null;  
    }

    /**
     * Controls the height of the graph.
     *
     * @param heightFactor an integer that indicates how high the graph should be.
     */
    public void setHeightFactor(int heightFactor) {
        this.heightFactor = heightFactor * 4;
    }

    /**
     * Returns an integer that reflects the height of the graph.
     *
     * @return an integer that reflects the height of the graph. The higher this value, the higher the graph.
     */
    public int getHeightFactor() {
        return heightFactor / 4;
    }

    /**
     * Controls whether the graph should be curved or rectangular. If curved the dependencies are drawn as curves instead
     * of rectangular lines, and spans are drawn as rounded rectangles.
     *
     * @param isCurved should the graph be more curved.
     * @see com.googlecode.whatswrong.NLPCanvasRenderer#setCurved(boolean)
     */
    public void setCurved(boolean isCurved) {
        this.isCurved = isCurved;
    }

    /**
     * Returns whether the renderer draws a more curved graph or not.
     *
     * @return true iff the renderer draws a more curved graph.
     */
    public boolean isCurved() {
        return isCurved;
    }

    /**
     * Set the color for edges of a certain type.
     *
     * @param edgeType the type of the edges we want to change the color for.
     * @param color    the color of the edges of the given type.
     */
    public void setEdgeTypeColor(String edgeType, Color color) {
    }

    /**
     * Sets the order/vertical layer in which the area of a certain type should be drawn.
     *
     * @param edgeType the type we want to change the order for.
     * @param order    the order/vertical layer in which the area of the given type should be drawn.
     */
    public void setEdgeTypeOrder(String edgeType, int order) {
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