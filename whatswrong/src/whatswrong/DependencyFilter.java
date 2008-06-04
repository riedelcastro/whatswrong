package whatswrong;

import java.util.Collection;

/**
 * @author Sebastian Riedel
 */
public abstract class DependencyFilter implements NLPInstanceFilter {

  public abstract Collection<DependencyEdge> filterEdges(Collection<DependencyEdge> original);

  public NLPInstance filter(NLPInstance original) {
    return new NLPInstance(original.getTokens(), filterEdges(original.getDependencies()));
  }
}
