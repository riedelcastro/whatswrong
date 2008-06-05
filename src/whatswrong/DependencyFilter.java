package whatswrong;

import java.util.Collection;

/**
 * @author Sebastian Riedel
 */
public abstract class DependencyFilter implements NLPInstanceFilter {

  public abstract Collection<Edge> filterEdges(Collection<Edge> original);

  public NLPInstance filter(NLPInstance original) {
    return new NLPInstance(original.getTokens(), filterEdges(original.getEdges()));
  }
}
