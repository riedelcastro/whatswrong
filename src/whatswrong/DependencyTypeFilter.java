package whatswrong;

import java.util.*;

/**
 * @author Sebastian Riedel
 */
public class DependencyTypeFilter implements DependencyFilter {

  private HashSet<String> allowedTypes = new HashSet<String>();

  public interface Listener {
    void changed(String type);
  }

  private ArrayList<Listener> listeners = new ArrayList<Listener>();

  public void addListener(Listener listener) {
    listeners.add(listener);
  }


  public Set<String> getAllowedTypes() {
    return Collections.unmodifiableSet(allowedTypes);
  }

  public DependencyTypeFilter(String... allowedTypes) {
    for (String type : allowedTypes) this.allowedTypes.add(type);
  }

  private void fireChanged(String type) {
    for (Listener l : listeners) l.changed(type);
  }

  public void addAllowedType(String type) {
    allowedTypes.add(type);
    fireChanged(type);
  }

  public void removeAllowedType(String type) {
    allowedTypes.remove(type);
    fireChanged(type);
  }

  public DependencyTypeFilter(Set<String> allowedTypes) {
    this.allowedTypes.addAll(allowedTypes);
  }

  public Collection<DependencyEdge> filter(Collection<DependencyEdge> original) {
    if (allowedTypes.size() == 0) return original;
    ArrayList<DependencyEdge> result = new ArrayList<DependencyEdge>(original.size());
    for (DependencyEdge edge : original) {
      for (String type : allowedTypes)
        if (edge.getTypePrefix().contains(type) || edge.getTypePostfix().contains(type))
          result.add(edge);
    }
    return result;

  }

  public boolean allows(String type) {
    return allowedTypes.isEmpty() || allowedTypes.contains(type);
  }
}
