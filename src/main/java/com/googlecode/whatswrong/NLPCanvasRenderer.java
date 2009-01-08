package com.googlecode.whatswrong;

import java.awt.*;

/**
 * An NLPCanvasRenderer renders a given NLPInstance to a Graphics object and returns the dimension of the created
 * image.
 *
 * @author Sebastian Riedel
 */
public interface NLPCanvasRenderer {


    /**
     * Renders the given instance to the given Graphics object and returns the dimension of the rendered image
     *
     * @param instance   the instance to render
     * @param graphics2D the graphics object to draw upon
     * @return the width and height of the drawn object.
     */
    Dimension render(NLPInstance instance, Graphics2D graphics2D);

    /**
     * Should anti-aliasing be used when drawing the graph.
     *
     * @param antiAliasing rue iff anti-aliasing should be used when drawing the graph.
     */
    void setAntiAliasing(boolean antiAliasing);

    /**
     * Sets the margin between tokens.
     *
     * @param margin the margin between tokens.
     */
    public void setMargin(int margin);

    /**
     * Returns the margin between tokens.
     *
     * @return the margin between tokens.
     */
    public int getMargin();

    /**
     * Controls the height of the graph.
     *
     * @param heightFactor an integer that indicates how high the graph should be.
     */
    public void setHeightFactor(int heightFactor);


    /**
     * Returns an integer that reflects the height of the graph.
     *
     * @return an integer that reflects the height of the graph. The higher this value, the higher the graph.
     */
    public int getHeightFactor();

    /**
     * Controls whether the graph should be curved or rectangular. This switch may have slightly different meanings for
     * different renderers. For example, a dependency graph might contain curved edges, a span type graph might contain
     * curved blocks.
     *
     * @param isCurved should the graph be more curved.
     */
    public void setCurved(boolean isCurved);

    /**
     * Returns whether the renderer draws a more curved graph or not.
     *
     * @return true iff the renderer draws a more curved graph.
     */
    public boolean isCurved();

    /**
     * Set the color for edges of a certain type.
     *
     * @param edgeType the type of the edges we want to change the color for.
     * @param color    the color of the edges of the given type.
     */
    public void setEdgeTypeColor(String edgeType, Color color);

    /**
     * Sets the order/vertical layer in which the area of a certain type should be drawn.
     *
     * @param edgeType the type we want to change the order for.
     * @param order    the order/vertical layer in which the area of the given type should be drawn.
     */
    public void setEdgeTypeOrder(String edgeType, int order);


    /**
     * Should anti-aliasing be used when drawing the graph.
     *
     * @return true iff anti-aliasing should be used when drawing the graph.
     */
    boolean isAntiAliasing();
}
