package javautils;

/**
 * Created by IntelliJ IDEA. User: srriedel Date: Sep 21, 2006 Time: 11:56:32 PM
 */
public class Pair<A1, A2>  {
  public final A1 arg1;
  public final A2 arg2;


  public Pair(A1 arg1, A2 arg2) {
    this.arg1 = arg1;
    this.arg2 = arg2;
  }


  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Pair pair = (Pair) o;

    if (arg1 != null ? !arg1.equals(pair.arg1) : pair.arg1 != null) return false;
    if (arg2 != null ? !arg2.equals(pair.arg2) : pair.arg2 != null) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (arg1 != null ? arg1.hashCode() : 0);
    result = 31 * result + (arg2 != null ? arg2.hashCode() : 0);
    return result;
  }


  public String toString() {
    return "(" + arg1 + "," + arg2 + ")";
  }
}
