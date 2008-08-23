package whatswrong;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A FilterPipeline filters an NLPInstance by iteratively calling a sequence of
 * delegate filters.
 *
 * @author Sebastian Riedel
 */
public class FilterPipeline implements NLPInstanceFilter {

  /**
   * The list of filters.
   */
  private ArrayList<NLPInstanceFilter>
    filters = new ArrayList<NLPInstanceFilter>();

  /**
   * Creates a new filter pipeline with the given filters.
   *
   * @param filters the filters of the pipeline. The first filter will be
   *                applied first, the last filter last.
   */
  public FilterPipeline(final NLPInstanceFilter... filters) {
    this.filters.addAll(Arrays.asList(filters));
  }


  /**
   * Applies the 1st filter to the original instance, the 2nd filter to the
   * result of the 1st filter, and so on.
   *
   * @param original the original instance.
   * @return the result of the last filter applied to the previous result.
   *
   * @see NLPInstanceFilter#filter(NLPInstance)
   */
  public NLPInstance filter(final NLPInstance original) {
    NLPInstance instance = original;
    for (NLPInstanceFilter filter : filters)
      instance = filter.filter(instance);
    return instance;
  }
}
