package whatswrong;

import java.util.*;

/**
 * @author Sebastian Riedel
 */
public class TokenPropertyFilter implements NLPInstanceFilter {

  private HashSet<TokenProperty> forbiddenProperties = new HashSet<TokenProperty>();


  public TokenPropertyFilter() {
  }

  public void addForbiddenProperty(String name){
    forbiddenProperties.add(new TokenProperty(name));
  }

  public void removeForbiddenProperty(String name){
    forbiddenProperties.remove(new TokenProperty(name));
  }

  public Set<TokenProperty> getForbiddenTokenProperties(){
    return Collections.unmodifiableSet(forbiddenProperties);
  }

  public List<TokenVertex> filterTokens(Collection<TokenVertex> original) {
    ArrayList<TokenVertex> result = new ArrayList<TokenVertex>(original.size());
    for (TokenVertex vertex : original) {
      TokenVertex copy = new TokenVertex(vertex.getIndex());
      for (TokenProperty property : vertex.getPropertyTypes()) {
        if (!forbiddenProperties.contains(property))
          copy.addProperty(property,vertex.getProperty(property));
      }
      result.add(copy);
    }
    return result;
  }

  public NLPInstance filter(NLPInstance original) {
    if (forbiddenProperties.size() == 0) return original;
    List<TokenVertex> tokens = filterTokens(original.getTokens());
    HashSet<DependencyEdge> edges = new HashSet<DependencyEdge>();
    for (DependencyEdge e: original.getDependencies())
      edges.add(new DependencyEdge(tokens.get(e.getFrom().getIndex()),
        tokens.get(e.getTo().getIndex()),e.getLabel(), e.getType()));
    return new NLPInstance(tokens, edges);
  }
}
