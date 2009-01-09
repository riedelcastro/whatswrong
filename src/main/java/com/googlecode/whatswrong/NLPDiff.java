package com.googlecode.whatswrong;

import java.util.HashSet;
import java.util.Set;
import java.util.Collection;

/**
 * An NLPDiff object takes two NLPInstances, a gold and a guess instance, and compares the set of edges that both
 * contain. The result is a new NLP instance that contains <ul> <li>all edges which are in both instances. These will
 * have the type "type:Match" where "type" is the original type of the edges. <li>all edges which are only in the the
 * guess instance. These will have the type "type:FP" <li>all edges which are only in the gold instance. These will have
 * the type "type:FN". </ul>
 *
 * @author Sebastian Riedel
 */
public class NLPDiff {


    /**
     * This class defines the identity of an edge with respect to the diff operation.
     */
    private static class EdgeIdentity {
        private final int from, to;
        private final String type, label;
        public final Edge edge;

        EdgeIdentity(Edge edge) {
            this.edge = edge;
            this.from = edge.getFrom().getIndex();
            this.to = edge.getTo().getIndex();
            this.type = edge.getType();
            this.label = edge.getLabel();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EdgeIdentity that = (EdgeIdentity) o;

            if (from != that.from) return false;
            if (to != that.to) return false;
            if (label != null ? !label.equals(that.label) : that.label != null) return false;
            if (type != null ? !type.equals(that.type) : that.type != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = from;
            result = 31 * result + to;
            result = 31 * result + (type != null ? type.hashCode() : 0);
            result = 31 * result + (label != null ? label.hashCode() : 0);
            return result;
        }
    }

    /**
     * Calculates the difference between two NLP instances in terms of their edges.
     *
     * @param goldInstance  the gold instance
     * @param guessInstance the (system) guess instance.
     * @return An NLPInstance with Matches, False Negatives and False Positives of the difference.
     */
    public NLPInstance diff(NLPInstance goldInstance, NLPInstance guessInstance) {
        NLPInstance diff = new NLPInstance();
        diff.setRenderType(goldInstance.getRenderType());
        for (int splitPoint : goldInstance.getSplitPoints())
            diff.addSplitPoint(splitPoint);
        diff.addTokens(goldInstance.getTokens());
        Set<EdgeIdentity> goldIdentities = createIdentities(goldInstance.getEdges());
        Set<EdgeIdentity> guessIdentities = createIdentities(guessInstance.getEdges());
        Set<EdgeIdentity> fn = new HashSet<EdgeIdentity>(goldIdentities);
        fn.removeAll(guessIdentities);
        Set<EdgeIdentity> fp = new HashSet<EdgeIdentity>(guessIdentities);
        fp.removeAll(goldIdentities);
        Set<EdgeIdentity> matches = new HashSet<EdgeIdentity>(goldIdentities);
        matches.retainAll(guessIdentities);
        for (EdgeIdentity edgeid : fn) {
            Edge edge = edgeid.edge;
            String type = edge.getType() + ":FN";
            diff.addEdge(new Edge(edge.getFrom(), edge.getTo(), edge.getLabel(),
                edge.getNote(), type, edge.getRenderType()));
        }
        for (EdgeIdentity edgeId : fp) {
            Edge edge = edgeId.edge;
            String type = edge.getType() + ":FP";
            diff.addEdge(new Edge(edge.getFrom(), edge.getTo(), edge.getLabel(),
                edge.getNote(), type, edge.getRenderType()));
        }
        for (EdgeIdentity edgeId : matches) {
            Edge edge = edgeId.edge;
            diff.addEdge(new Edge(edge.getFrom(), edge.getTo(), edge.getLabel(),
                edge.getNote(), edge.getType() + ":Match", edge.getRenderType()));
        }
        return diff;

    }

    /**
     * Converts a collection of edges to their diff-based identities.
     *
     * @param edges the input edges
     * @return the identities of the input edges.
     */
    private static Set<EdgeIdentity> createIdentities(Collection<Edge> edges) {
        HashSet<EdgeIdentity> result = new HashSet<EdgeIdentity>();
        for (Edge edge : edges) result.add(new EdgeIdentity(edge));
        return result;
    }

}
