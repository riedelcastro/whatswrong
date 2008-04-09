package whatswrong;

import java.util.*;

/**
 * @author Sebastian Riedel
 */
public class DependencyTypeFilter implements DependencyFilter {

  private HashSet<String> forbiddenTypes = new HashSet<String>();

  public interface Listener {
    void changed(String type);
  }

  private ArrayList<Listener> listeners = new ArrayList<Listener>();

  public void addListener(Listener listener){
    listeners.add(listener);
  }


  public Set<String> getForbiddenTypes() {
    return Collections.unmodifiableSet(forbiddenTypes);
  }

  public DependencyTypeFilter(String ... forbiddenTypes){
    for (String type : forbiddenTypes) this.forbiddenTypes.add(type);
  }

  private void fireChanged(String type){
    for (Listener l : listeners) l.changed(type);
  }

  public void addForbiddenType(String type){
    forbiddenTypes.add(type);
    fireChanged(type);
  }

  public void removeForbiddenType(String type){
    forbiddenTypes.remove(type);
    fireChanged(type);
  }

  public DependencyTypeFilter(Set<String> forbiddenTypes) {
    this.forbiddenTypes.addAll(forbiddenTypes);
  }

  public Collection<DependencyEdge> filter(Collection<DependencyEdge> original) {
    ArrayList<DependencyEdge> result = new ArrayList<DependencyEdge>(original.size());
    main:
    for (DependencyEdge edge : original){
      for (String type : forbiddenTypes)
        if (edge.getType().contains(type)) continue main;
      result.add(edge);
    }
    return result;

  }

  public boolean forbids(String type) {
    return forbiddenTypes.contains(type);
  }
}
