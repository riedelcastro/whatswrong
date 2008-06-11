package whatswrong;

import java.util.*;

/**
 * @author Sebastian Riedel
 */
public class NLPInstance {

  private List<Edge> edges = new ArrayList<Edge>();
  private List<TokenVertex> tokens = new ArrayList<TokenVertex>();
  private HashMap<Integer, TokenVertex> map = new HashMap<Integer, TokenVertex>();

  public NLPInstance() {
  }

  public NLPInstance(Collection<TokenVertex> tokens, Collection<Edge> edges) {
    this.tokens.addAll(tokens);
    for (TokenVertex t : tokens) map.put(t.getIndex(), t);
    this.edges.addAll(edges);
  }

  public void addEdge(int from, int to, String label, String type) {
    edges.add(new Edge(map.get(from), map.get(to), label, type));
  }

  public void addEdge(int from, int to, String label, String type, Edge.RenderType renderType) {
    edges.add(new Edge(map.get(from), map.get(to), label, type, renderType));
  }

  public void addSpan(int from, int to, String label, String type) {
    edges.add(new Edge(map.get(from), map.get(to), label, type, Edge.RenderType.span));
  }

  public void addDependency(int from, int to, String label, String type) {
    edges.add(new Edge(map.get(from), map.get(to), label, type, Edge.RenderType.dependency));
  }

  public void addEdge(TokenVertex from, TokenVertex to, String label, String type) {
    addEdge(from.getIndex(), to.getIndex(), label, type);
  }

  public void addEdge(TokenVertex from, TokenVertex to, String label, String type, Edge.RenderType renderType) {
    addEdge(from.getIndex(), to.getIndex(), label, type, renderType);
  }

  public void addTokens(Collection<TokenVertex> tokens) {
    this.tokens.addAll(tokens);
    for (TokenVertex t : tokens) map.put(t.getIndex(), t);
  }


  public void addEdges(Collection<Edge> dependencies) {
    this.edges.addAll(dependencies);
  }

  public void merge(NLPInstance nlp) {
    for (int i = 0; i < Math.min(tokens.size(), nlp.tokens.size()); ++i) {
      tokens.get(i).merge(nlp.tokens.get(i));
    }
    for (Edge edge : nlp.edges) {
      addEdge(edge.getFrom().getIndex(), edge.getTo().getIndex(),
        edge.getLabel(), edge.getType(), edge.getRenderType());
    }
  }

  public void addTokenWithProperties(String... properties) {
    TokenVertex token = new TokenVertex(tokens.size());
    for (String property : properties) token.addProperty(property);
    tokens.add(token);
    map.put(token.getIndex(), token);
  }

  public TokenVertex addToken() {
    TokenVertex vertex = new TokenVertex(tokens.size());
    tokens.add(vertex);
    map.put(vertex.getIndex(), vertex);
    return vertex;
  }

  public TokenVertex addToken(int index) {

    TokenVertex vertex = map.get(index);
    if (vertex == null) {
      vertex = new TokenVertex(index);
      map.put(index, vertex);
    }
    return vertex;
  }

  public void consistify() {
    tokens.addAll(map.values());
    Collections.sort(tokens);
  }


  public List<Edge> getEdges() {
    return Collections.unmodifiableList(edges);
  }

  public List<Edge> getEdges(Edge.RenderType renderType) {
    ArrayList<Edge> result = new ArrayList<Edge>(edges.size());
    for (Edge e : edges) if (e.getRenderType() == renderType) result.add(e);
    return result;
  }

  public TokenVertex getToken(int index) {
    return map.get(index);
  }

  public List<TokenVertex> getTokens() {
    return Collections.unmodifiableList(tokens);
  }

  public String toString() {
    return tokens + "\n" + map + "\n" + edges;
  }
}
