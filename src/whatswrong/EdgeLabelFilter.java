package whatswrong;

import java.util.HashSet;
import java.util.Set;
import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Sebastian Riedel
 */
public class EdgeLabelFilter extends EdgeFilter {

  private HashSet<String> allowedLabels = new HashSet<String>();

  public EdgeLabelFilter(String ... allowedLabels){
    for (String type : allowedLabels) this.allowedLabels.add(type);
  }

  public void addAllowedLabel(String type){
    allowedLabels.add(type);
  }

  public void removeAllowedLabel(String type){
    allowedLabels.remove(type);
  }

  public EdgeLabelFilter(Set<String> forbiddenTypes) {
    this.allowedLabels.addAll(forbiddenTypes);
  }

  public void clear(){
    allowedLabels.clear();
  }

  public Collection<Edge> filterEdges(Collection<Edge> original) {
    if (allowedLabels.size() == 0) return original;
    ArrayList<Edge> result = new ArrayList<Edge>(original.size());
    for (Edge edge : original)
      for (String allowed : allowedLabels)
        if (edge.getLabel().contains(allowed)) {
          result.add(edge);
          break;
        }
    return result;

  }

  public boolean allows(String type) {
    return allowedLabels.contains(type);
  }
}
