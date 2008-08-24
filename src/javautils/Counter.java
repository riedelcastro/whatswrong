package javautils;

import java.util.*;
import java.io.*;

/**
 * A Counter counts objects of class T.
 *
 * @author Sebastian Riedel
 */
public class Counter<T> extends HashMap<T, Integer> {

  /**
   * Gets the count of object o.
   *
   * @param o the object to get the count of.
   * @return the count of object o. If no count for o has specified zero is
   *         returned.
   */
  public Integer get(final Object o) {
    Integer original = super.get(o);
    return original == null ? 0 : original;
  }

  /**
   * Increments the count for the given object by <code>howmuch</code>
   *
   * @param value   the object to increment the count for.
   * @param howmuch how much the count should be incremented.
   */
  public void increment(final T value, final int howmuch) {
    Integer old = super.get(value);
    put(value, old == null ? howmuch : old + howmuch);
  }

  /**
   * Loads counts from a column separated file where row looks like "value
   * count".
   *
   * @param file the file to load from.
   * @return the Counter object representing the counts in the file
   * @throws IOException if I/O goes wrong.
   */
  public static Counter<String> loadFromFile(final File file) throws IOException {
    Counter<String> result = new Counter<String>();
    BufferedReader reader = new BufferedReader(new FileReader(file));
    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
      if (!line.trim().equals("")) {
        String[] split = line.split("[ \t]");
        result.increment(split[0], Integer.valueOf(split[1]));
      }
    }
    return result;
  }

  /**
   * Sort map entries by counts.
   *
   * @param descending the list start with the highest or lowest count.
   * @return a list of map entries ordered by count.
   */
  public List<Map.Entry<T, Integer>> sorted(final boolean descending) {
    ArrayList<Map.Entry<T, Integer>> sorted = new ArrayList<Map.Entry<T, Integer>>(entrySet());
    Collections.sort(sorted, new Comparator<Map.Entry<T, Integer>>() {
      @SuppressWarnings({"MissingMethodJavaDoc"})
      public int compare(Map.Entry<T, Integer> o1, Map.Entry<T, Integer> o2) {
        return (descending ? 1 : -1) * (o2.getValue() - o1.getValue());
      }
    });
    return sorted;
  }

  /**
   * Saves the counts to column separated text file with format "value count" in
   * each row.
   *
   * @param outputStream the output stream to print to.
   */
  public void save(final OutputStream outputStream) {
    PrintStream out = new PrintStream(outputStream);
    for (Map.Entry<T, Integer> entry : sorted(true)) {
      out.println(entry.getKey() + "\t" + entry.getValue());
    }
  }

  /**
   * Gets the maximum count of all objects in the counter.
   *
   * @return the maximum count of all objects in the counter.
   */
  public int getMaximum() {
    int max = 0;
    for (Integer value : values())
      if (value > max) max = value;
    return max;
  }

}
