package whatswrong;

/**
 * A TokenProperty represents a property of a token, such as the 'Word' or
 * "PoS-Tag' property. A {@link Token} then maps such properties to property
 * values, such as "house" or "NN". Each TokenProperty has a name (say 'Word')
 * and an integer 'level' that can be used to define an order on properties.
 * This order is for example used when the properties of a token are stacked
 * under each other in the graphical representation of a token.
 *
 * @author Sebastian Riedel
 */
public class TokenProperty implements Comparable<TokenProperty> {
  /**
   * The name of the property.
   */
  private final String name;
  /**
   * The level of the property.
   */
  private final int level;

  /**
   * Create new property with given name and level.
   *
   * @param name  the name of the property.
   * @param level the level of the property.
   */
  public TokenProperty(final String name, final int level) {
    this.name = name;
    this.level = level;
  }


  /**
   * Creates a property with the given name and level 0.
   *
   * @param name the name of the property.
   */
  public TokenProperty(String name) {
    this(name, 0);
  }


  /**
   * Returns the name of the property.
   *
   * @return the name of the property.
   */
  public String toString() {
    return name;
  }

  /**
   * Creates a property with the given level and the name 'Property [level]'
   * where [level] will be replaced with the given level.
   *
   * @param level the level of the property.
   */
  public TokenProperty(final int level) {
    this("Property " + level, level);
  }

  /**
   * Returns the name of the property.
   *
   * @return the name of the property.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the level of the property.
   *
   * @return the level of the property.
   */
  public int getLevel() {
    return level;
  }


  /**
   * Two TokenProperty objects are equal iff their names match.
   *
   * @param o the other property.
   * @return true iff the property names match.
   */
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TokenProperty that = (TokenProperty) o;

    if (name != null ? !name.equals(that.name) : that.name != null)
      return false;

    return true;
  }

  /**
   * Calculates a hashcode based on the property name.
   *
   * @return a hashcode based on the property name.
   */
  public int hashCode() {
    return (name != null ? name.hashCode() : 0);
  }

  /**
   * First compares the level of the two properties and if these are equal the
   * property names are compared.
   *
   * @param o the other property.
   * @return a value larger than 0 if this level is larger than the other level
   *         or the levels equal and this name is lexicographically larger than
   *         the other. A value smaller than 0 is returned if this level is
   *         smaller than the other level or the levels equal and this name is
   *         lexicographically smaller than the other. Otherwise 0 is returned.
   */
  public int compareTo(TokenProperty o) {
    return level != o.level ? level - o.level : name.compareTo(o.name);
  }
}
