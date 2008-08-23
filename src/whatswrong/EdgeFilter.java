package whatswrong;

import java.util.Collection;

/**
 * A DependencyFilter is a NLPInstanceFilter that only filters out edges.
 *
 * @author Sebastian Riedel
 */
public abstract class EdgeFilter implements NLPInstanceFilter {

  /**
   * Take a set of edges and return a subset of them.
   *
   * @param original the original set of edges.
   * @return the filtered set of edges.
   */
  public abstract Collection<Edge> filterEdges(Collection<Edge> original);


  /**
   * @see NLPInstanceFilter#filter(NLPInstance)
   */
  public NLPInstance filter(NLPInstance original) {
    return new NLPInstance(original.getTokens(), filterEdges(original.getEdges()));
  }
}
