package javautils;

/**
 * A Pair is a typed pair of objects.
 *
 * @author Sebastian Riedel
 */
public class Pair<A1, A2> {

  /**
   * The first argument.
   */
  public final A1 arg1;
  /**
   * The second argument.
   */
  public final A2 arg2;

  /**
   * Creates a pair with the given arguments
   *
   * @param arg1 First argument.
   * @param arg2 Second argument.
   */
  public Pair(final A1 arg1, final A2 arg2) {
    this.arg1 = arg1;
    this.arg2 = arg2;
  }

  /**
   * Checks whether both arguments are equal.
   *
   * @param o the other pair.
   * @return true iff both arguments are equal.
   */
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Pair pair = (Pair) o;

    if (arg1 != null ? !arg1.equals(pair.arg1) : pair.arg1 != null)
      return false;
    if (arg2 != null ? !arg2.equals(pair.arg2) : pair.arg2 != null)
      return false;

    return true;
  }

  /**
   * Returns a hashcode based on both arguments.
   *
   * @return a hashcode based on both arguments.
   */
  public int hashCode() {
    int result;
    result = (arg1 != null ? arg1.hashCode() : 0);
    result = 31 * result + (arg2 != null ? arg2.hashCode() : 0);
    return result;
  }

  /**
   * Returns ([arg1],[arg2]) where [arg1] is replaced by the value of the first
   * argument and [arg2] replaced by the value of the second argument.
   *
   * @return the string "([arg1],[arg2])".
   */
  public String toString() {
    return "(" + arg1 + "," + arg2 + ")";
  }
}
