package com.googlecode.whatswrong;

import java.util.*;

/**
 * An EdgeTypeFilter filters out edges that do not have certain (prefix or postfix) types.
 *
 * @author Sebastian Riedel
 */
public class EdgeTypeFilter extends EdgeFilter {

    /**
     * If an edge has a prefix-type in this set it can pass.
     */
    private HashSet<String> allowedPrefixTypes = new HashSet<String>();

    /**
     * If an edge has a postfix-type in this set it can pass.
     */
    private HashSet<String> allowedPostfixTypes = new HashSet<String>();

    /**
     * Am EdgeTypeFilter.Listener is notified of changes to the set of allowed edge type strings.
     */
    public interface Listener {
        /**
         * Called when a type string was added or removed from the filter.
         *
         * @param type the type string that was added or removed from the filter.
         */
        void changed(String type);
    }

    /**
     * The list of listeners of this filter.
     */
    private ArrayList<Listener> listeners = new ArrayList<Listener>();

    /**
     * Adds a listener.
     *
     * @param listener the listener to add.
     */
    public void addListener(Listener listener) {
        listeners.add(listener);
    }


    /**
     * Returns the set of allowed prefix types for edges.
     *
     * @return an unmodifiable set of allowed prefix types for edges.
     */
    public Set<String> getAllowedPrefixTypes() {
        return Collections.unmodifiableSet(allowedPrefixTypes);
    }

    /**
     * Returns the set of allowed postfix types for edges.
     *
     * @return an unmodifiable set of allowed postfix types for edges.
     */
    public Set<String> getAllowedPostfixTypes() {
        return Collections.unmodifiableSet(allowedPostfixTypes);
    }

    /**
     * Creates a new EdgeTypeFilter with the given allowed edge prefix types.
     *
     * @param allowedPrefixTypes the allowed prefix types.
     */
    public EdgeTypeFilter(final String... allowedPrefixTypes) {
        this.allowedPrefixTypes.addAll(Arrays.asList(allowedPrefixTypes));
    }

    /**
     * Notifies every listener that the allow/disallow state of a type has changed.
     *
     * @param type the type which allow/disallow state has changed.
     */
    private void fireChanged(final String type) {
        for (Listener l : listeners) l.changed(type);
    }

    /**
     * Adds an allowed prefix type. This causes the filter to accept edges with the given prefix type.
     *
     * @param type the allowed prefix type.
     */
    public void addAllowedPrefixType(final String type) {
        allowedPrefixTypes.add(type);
        fireChanged(type);
    }

    /**
     * Adds an allowed prefix type. This causes the filter to accept edges with the given postfix type.
     *
     * @param type the allowed postfix type.
     */
    public void addAllowedPostfixType(final String type) {
        allowedPostfixTypes.add(type);
        fireChanged(type);
    }

    /**
     * Disallows the given prefix type. This causes the filter to stop accepting edges with the given prefix type.
     *
     * @param type the prefix type to disallow.
     */
    public void removeAllowedPrefixType(String type) {
        allowedPrefixTypes.remove(type);
        fireChanged(type);
    }

    /**
     * Disallows the given postfix type. This causes the filter to stop accepting edges with the given postfix type.
     *
     * @param type the postfix type to disallow.
     */
    public void removeAllowedPostfixType(String type) {
        allowedPostfixTypes.remove(type);
        fireChanged(type);
    }

    /**
     * Creates a new EdgeTypeFilter with the given allowed edge prefix types.
     *
     * @param allowedPrefixTypes the allowed prefix types.
     */
    public EdgeTypeFilter(final Set<String> allowedPrefixTypes) {
        this.allowedPrefixTypes.addAll(allowedPrefixTypes);
    }

    /**
     * Filters out all edges that don't have an allowed prefix and postfix type.
     *
     * @param original the original set of edges.
     * @return the filtered set of edges.
     * @see EdgeFilter#filterEdges(Collection<Edge>)
     */
    public Collection<Edge> filterEdges(Collection<Edge> original) {
        ArrayList<Edge> result = new ArrayList<Edge>(original.size());
        for (Edge edge : original) {
            boolean prefixAllowed = edge.getTypePrefix().equals("") ||
                allowedPrefixTypes.contains(edge.getTypePrefix());
            boolean postfixAllowed = edge.getTypePostfix().equals("") ||
                allowedPostfixTypes.contains(edge.getTypePostfix());
            if (prefixAllowed && postfixAllowed)
                result.add(edge);
        }
        return result;

    }

    /**
     * Does the filter allow the given prefix.
     *
     * @param type the type to check whether it is allowed as prefix.
     * @return true iff the given type is allowed as prefix.
     */
    public boolean allowsPrefix(String type) {
        return allowedPrefixTypes.contains(type);
    }

    /**
     * Does the filter allow the given postfix.
     *
     * @param type the type to check whether it is allowed as postfix.
     * @return true iff the given type is allowed as postfix.
     */
    public boolean allowsPostfix(String type) {
        return allowedPostfixTypes.contains(type);
    }
}
