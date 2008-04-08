package whatswrong;

import java.util.HashSet;

/**
 * @author Sebastian Riedel
 */
public class NLPDiff {

  public NLPInstance diff(NLPInstance goldInstance, NLPInstance guessInstance){
    NLPInstance diff = new NLPInstance();
    diff.addTokens(goldInstance.getTokens());
    HashSet<DependencyEdge> fn = new HashSet<DependencyEdge>(goldInstance.getDependencies());
    fn.removeAll(guessInstance.getDependencies());
    HashSet<DependencyEdge> fp = new HashSet<DependencyEdge>(guessInstance.getDependencies());
    fp.removeAll(goldInstance.getDependencies());
    HashSet<DependencyEdge> matches = new HashSet<DependencyEdge>(goldInstance.getDependencies());
    matches.retainAll(guessInstance.getDependencies());
    for (DependencyEdge edge : fn) {
      String type = edge.getType() + ":FN";
      diff.addDependency(edge.getFrom(), edge.getTo(), edge.getLabel(), type);
    }
    for (DependencyEdge edge : fp) {
      String type = edge.getType() + ":FP";
      diff.addDependency(edge.getFrom(), edge.getTo(), edge.getLabel(), type);
    }
    for (DependencyEdge edge : matches)
      diff.addDependency(edge.getFrom(), edge.getTo(), edge.getLabel(), edge.getType() + ":Match");
    return diff;

  }

}
