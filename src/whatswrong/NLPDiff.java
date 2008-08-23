package whatswrong;

import java.util.HashSet;

/**
 * An NLPDiff object takes two NLPInstances, a gold and a guess instance, and
 * compares the set of edges that both contain. The result is a new NLP instance
 * that contains <ul> <li>all edges which are in both instances. These will have
 * the type "type:Match" where "type" is the original type of the edges. <li>all
 * edges which are only in the the guess instance. These will have the type
 * "type:FP" <li>all edges which are only in the gold instance. These will have
 * the type "type:FN". </ul>
 *
 * @author Sebastian Riedel
 */
public class NLPDiff {

  /**
   * Calculates the difference between two NLP instances in terms of their
   * edges.
   *
   * @param goldInstance  the gold instance
   * @param guessInstance the (system) guess instance.
   * @return An NLPInstance with Matches, False Negatives and False Positives of
   *         the difference.
   */
  public NLPInstance diff(NLPInstance goldInstance, NLPInstance guessInstance) {
    NLPInstance diff = new NLPInstance();
    diff.addTokens(goldInstance.getTokens());
    HashSet<Edge> fn = new HashSet<Edge>(goldInstance.getEdges());
    fn.removeAll(guessInstance.getEdges());
    HashSet<Edge> fp = new HashSet<Edge>(guessInstance.getEdges());
    fp.removeAll(goldInstance.getEdges());
    HashSet<Edge> matches = new HashSet<Edge>(goldInstance.getEdges());
    matches.retainAll(guessInstance.getEdges());
    for (Edge edge : fn) {
      String type = edge.getType() + ":FN";
      diff.addEdge(edge.getFrom(), edge.getTo(), edge.getLabel(), type, edge.getRenderType());
    }
    for (Edge edge : fp) {
      String type = edge.getType() + ":FP";
      diff.addEdge(edge.getFrom(), edge.getTo(), edge.getLabel(), type, edge.getRenderType());
    }
    for (Edge edge : matches)
      diff.addEdge(edge.getFrom(), edge.getTo(), edge.getLabel(), edge.getType() + ":Match",
        edge.getRenderType());
    return diff;

  }

}
