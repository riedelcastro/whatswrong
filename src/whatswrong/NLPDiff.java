package whatswrong;

import java.util.HashSet;

/**
 * @author Sebastian Riedel
 */
public class NLPDiff {

  public NLPInstance diff(NLPInstance goldInstance, NLPInstance guessInstance){
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
