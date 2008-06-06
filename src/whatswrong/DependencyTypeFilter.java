package whatswrong;

import java.util.*;

/**
 * @author Sebastian Riedel
 */
public class DependencyTypeFilter extends DependencyFilter {

  private HashSet<String> allowedPrefixTypes = new HashSet<String>();
  private HashSet<String> allowedPostfixTypes = new HashSet<String>();

  public interface Listener {
    void changed(String type);
  }

  private ArrayList<Listener> listeners = new ArrayList<Listener>();

  public void addListener(Listener listener) {
    listeners.add(listener);
  }


  public Set<String> getAllowedPrefixTypes() {
    return Collections.unmodifiableSet(allowedPrefixTypes);
  }

  public Set<String> getAllowedPostfixTypes() {
    return Collections.unmodifiableSet(allowedPostfixTypes);
  }

  public DependencyTypeFilter(String... allowedPrefixTypes) {
    for (String type : allowedPrefixTypes) this.allowedPrefixTypes.add(type);
  }

  private void fireChanged(String type) {
    for (Listener l : listeners) l.changed(type);
  }

  public void addAllowedPrefixType(String type) {
    allowedPrefixTypes.add(type);
    fireChanged(type);
  }

  public void addAllowedPostfixType(String type) {
    allowedPostfixTypes.add(type);
    fireChanged(type);
  }

  public void removeAllowedPrefixType(String type) {
    allowedPrefixTypes.remove(type);
    fireChanged(type);
  }

  public void removeAllowedPostfixType(String type) {
    allowedPostfixTypes.remove(type);
    fireChanged(type);
  }

  public DependencyTypeFilter(Set<String> allowedPrefixTypes) {
    this.allowedPrefixTypes.addAll(allowedPrefixTypes);
  }

  public Collection<Edge> filterEdges(Collection<Edge> original) {
    ArrayList<Edge> result = new ArrayList<Edge>(original.size());
    for (Edge edge : original) {
      boolean prefixAllowed = edge.getTypePrefix().equals("") ||
        allowedPrefixTypes.contains(edge.getTypePrefix());
      boolean postfixAllowed = edge.getTypePostfix().equals("") ||
        allowedPostfixTypes.contains(edge.getTypePostfix());
      if (prefixAllowed && postfixAllowed)
        result.add(edge);
    }
    return result;

  }

  public boolean allowsPrefix(String type) {
    return allowedPrefixTypes.contains(type);
  }

  public boolean allowsPostfix(String type) {
    return allowedPostfixTypes.contains(type);
  }
}
