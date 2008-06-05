package whatswrong;

import java.util.*;

/**
 * @author Sebastian Riedel
 */
public class TokenFilter implements NLPInstanceFilter {
  private HashSet<TokenProperty> forbiddenProperties = new HashSet<TokenProperty>();

  private HashSet<String> allowedStrings = new HashSet<String>();

  private boolean wholeWord = false;

  public TokenFilter() {
  }

  public boolean isWholeWord() {
    return wholeWord;
  }

  public void setWholeWord(boolean wholeWord) {
    this.wholeWord = wholeWord;
  }

  public void addAllowedString(String string) {
    allowedStrings.add(string);
  }

  public void clearAllowedStrings() {
    allowedStrings.clear();
  }

  public void addForbiddenProperty(String name) {
    forbiddenProperties.add(new TokenProperty(name));
  }

  public void removeForbiddenProperty(String name) {
    forbiddenProperties.remove(new TokenProperty(name));
  }

  public Set<TokenProperty> getForbiddenTokenProperties() {
    return Collections.unmodifiableSet(forbiddenProperties);
  }

  public List<TokenVertex> filterTokens(Collection<TokenVertex> original) {
    ArrayList<TokenVertex> result = new ArrayList<TokenVertex>(original.size());
    for (TokenVertex vertex : original) {
      TokenVertex copy = new TokenVertex(vertex.getIndex());
      for (TokenProperty property : vertex.getPropertyTypes()) {
        if (!forbiddenProperties.contains(property))
          copy.addProperty(property, vertex.getProperty(property));
      }
      result.add(copy);
    }
    return result;
  }

  public NLPInstance filter(NLPInstance original) {

    if (allowedStrings.size() > 0) {
      //first filter out tokens not containing allowed strings
      HashMap<TokenVertex, TokenVertex> old2new = new HashMap<TokenVertex, TokenVertex>();
      ArrayList<TokenVertex> tokens = new ArrayList<TokenVertex>();
      main:
      for (TokenVertex t : original.getTokens()) {
        for (String prop : t.getProperties())
          for (String allowed : allowedStrings)
            if (wholeWord ? prop.equals(allowed) : prop.contains(allowed)) {
              TokenVertex newVertex = new TokenVertex(tokens.size());
              newVertex.merge(t);
              tokens.add(newVertex);
              old2new.put(t, newVertex);
              continue main;
            }
      }
      //update edges and remove those that have vertices not in the new vertex set
      ArrayList<Edge> edges = new ArrayList<Edge>();
      for (Edge e : original.getEdges()) {
        TokenVertex newFrom = old2new.get(e.getFrom());
        TokenVertex newTo = old2new.get(e.getTo());
        if (newFrom == null || newTo == null) continue;
        edges.add(new Edge(newFrom, newTo, e.getLabel(), e.getType(),e.getRenderType()));
      }
      return new NLPInstance(filterTokens(tokens), edges);

    } else {
      List<TokenVertex> filteredTokens = filterTokens(original.getTokens());
      return new NLPInstance(filteredTokens, original.getEdges());
    }
  }

  private Collection<Edge> updateVertices(Collection<Edge> edges, List<TokenVertex> tokens){
    HashSet<Edge> result = new HashSet<Edge>();
    for (Edge e: edges)
      result.add(new Edge(tokens.get(e.getFrom().getIndex()),
        tokens.get(e.getTo().getIndex()),e.getLabel(), e.getType()));

    return result;
  }

}
