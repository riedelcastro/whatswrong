package whatswrong;

import java.util.HashSet;
import java.util.Set;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * An EdgeLabelFilter filters out edges with a label that contains one of a set
 * of allowed label substrings.
 *
 * <p>Note that if the set of allowed label substrings is empty the filter
 * allows all edges.
 *
 * @author Sebastian Riedel
 */
public class EdgeLabelFilter extends EdgeFilter {

  /**
   * Set of allowed label substrings.
   */
  private HashSet<String> allowedLabels = new HashSet<String>();

  /**
   * Creates a new EdgeLabelFilter that allows the given label substrings.
   *
   * @param allowedLabels var array label substrings that are allowed.
   */
  public EdgeLabelFilter(final String... allowedLabels) {
    this.allowedLabels.addAll(Arrays.asList(allowedLabels));
  }

  /**
   * Adds an allowed label substring.
   *
   * @param label the label that should be allowed.
   */
  public void addAllowedLabel(final String label) {
    allowedLabels.add(label);
  }

  /**
   * Removes an allowed label substring.
   *
   * @param label the label substring to disallow.
   */
  public void removeAllowedLabel(final String label) {
    allowedLabels.remove(label);
  }

  /**
   * Creates a new EdgeLabelFilter that allows the given label substrings.
   *
   * @param allowedLabels a set of label substrings that are allowed.
   */
  public EdgeLabelFilter(final Set<String> allowedLabels) {
    this.allowedLabels.addAll(allowedLabels);
  }

  /**
   * Removes all allowed label substrings. In this state the filter allows all
   * labels.
   */
  public void clear() {
    allowedLabels.clear();
  }

  /**
   * Filters out all edges that don't have a label that contains one of the
   * allowed label substrings. If the set of allowed substrings is empty then
   * the original set of edges is returned as is.
   *
   * @param original the original set of edges.
   * @return a filtered version of the original edge collection.
   *
   * @see EdgeFilter#filterEdges(Collection<Edge>)
   */
  public Collection<Edge> filterEdges(final Collection<Edge> original) {
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

  /**
   * Checks whether the filter allows the given label substring.
   *
   * @param label the label substring we want to check whether the filter allows
   *              it.
   * @return true iff the filter allows the given label substring.
   */
  public boolean allows(final String label) {
    return allowedLabels.contains(label);
  }
}
