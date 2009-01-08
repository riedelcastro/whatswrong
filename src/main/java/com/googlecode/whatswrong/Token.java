package com.googlecode.whatswrong;

import java.util.*;

/**
 * A Token represents a word in an utterance. It consists of an index and a set of properties with name and value.
 *
 * @author Sebastian Riedel
 */
public class Token implements Comparable<Token> {

    /**
     * The index of the token.
     */
    private int index;
    /**
     * A mapping from properties to values.
     */
    private HashMap<TokenProperty, String>
        tokenProperties = new HashMap<TokenProperty, String>();


    /**
     * Creates a new token with the given index.
     *
     * @param index the index of the token.
     */
    public Token(final int index) {
        this.index = index;
    }

    /**
     * Returns the index of the token.
     *
     * @return the index of the token.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Return all token properties (the property names). To get the value of a property use {@link
     * Token#getProperty(TokenProperty)}.
     *
     * @return a collection with token properties.
     */
    public Collection<TokenProperty> getPropertyTypes() {
        return Collections.unmodifiableCollection(tokenProperties.keySet());
    }

    /**
     * Get the value of the given property.
     *
     * @param property the property to get the value for.
     * @return the value of the given property.
     */
    public String getProperty(TokenProperty property) {
        return tokenProperties.get(property);
    }

    /**
     * Remove the property value with given index.
     *
     * @param index the index of the property to remove.
     */
    public void removeProperty(int index) {
        tokenProperties.remove(new TokenProperty(index));
    }

    /**
     * Remove the property value with the given name.
     *
     * @param name the name of the property to remove.
     */
    public void removeProperty(String name) {
        tokenProperties.remove(new TokenProperty(name));
    }

    /**
     * Add a property with the given name and value.
     *
     * @param name  the name of the property.
     * @param value the value of the property.
     * @return a pointer to this token.
     */
    public Token addProperty(String name, String value) {
        tokenProperties.put(new TokenProperty(name, tokenProperties.size()), value);
        return this;
    }

    /**
     * Add the property with name "Property [index]" and the given value.
     *
     * @param index    the index of the property
     * @param property the value of the property.
     */
    public void addProperty(int index, String property) {
        tokenProperties.put(new TokenProperty(index), property);
    }

    /**
     * Add a property with given value.
     *
     * @param property the property to add
     * @param value    the value of the property
     * @return this token.
     */
    public Token addProperty(TokenProperty property, String value) {
        tokenProperties.put(property, value);
        return this;
    }

    /**
     * Adds a property with the given value. The property name will be "Property i" where i this the current number of
     * properties.
     *
     * @param value the value of the property.
     */
    public void addProperty(String value) {
        addProperty(tokenProperties.size(), value);
    }

    /**
     * Sorts the properties by property level and name.
     *
     * @return a list of sorted token properties.
     */
    public List<TokenProperty> getSortedProperties() {
        ArrayList<TokenProperty>
            sorted = new ArrayList<TokenProperty>(tokenProperties.keySet());
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * Returns a collection of all property values.
     *
     * @return a collection of all property values.
     */
    public Collection<String> getPropertyValues() {
        return Collections.unmodifiableCollection(tokenProperties.values());
    }

    /**
     * Check whether any of the property values contains the given string.
     *
     * @param substring the string to check whether it is contained in any property value of this token.
     * @return true iff there exists on property of this token for which <code>substring</code> is a substring of the
     *         corresponding property value.
     */
    public boolean propertiesContain(String substring) {
        for (String property : tokenProperties.values())
            if (property.contains(substring)) return true;
        return false;
    }

    /**
     * Check whether any of the property values of this token contains any of the strings in the given set of strings.
     *
     * @param substrings set of strings to check
     * @param wholeWord  should we check for complete words of is it enough for the given strings to be substrings of
     *                   the token value.
     * @return true iff a) if there is a property value equal to one of the strings in <code>substrings</code>
     *         (wholeword=true) or b) if there is a property value that contains one of the strings in
     *         <code>substrings</code> (wholeword=false).
     */
    public boolean propertiesContain(Collection<String> substrings, boolean wholeWord) {
        for (String property : tokenProperties.values())
            for (String substring : substrings)
                if (substring.matches("\\d+-\\d+")) {
                    String[] split = substring.split("[-]");
                    int from = Integer.parseInt(split[0]);
                    int to = Integer.parseInt(split[1]);
                    for (int i = from; i <= to; ++i)
                        if (property.equals(String.valueOf(i))) return true;
                } else if (wholeWord ? property.equals(substring) : property.contains(substring))
                    return true;
        return false;
    }

    /**
     * Checks whether the two tokens have the same index. (Hence equality is only defined through the position of the
     * token in the sentence.
     *
     * @param o the other token.
     * @return <code>index==((Token)o).index</code>
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Token that = (Token) o;

        return index == that.index;

    }

    /**
     * Returns the index of the token.
     *
     * @return the index of the token.
     */
    public int hashCode() {
        return index;
    }

    /**
     * Inserts all properties and values of the other token into this token. In case of clashes the value of the other
     * token is taken.
     *
     * @param token the token to merge with.
     */
    public void merge(Token token) {
        tokenProperties.putAll(token.tokenProperties);
    }

    /**
     * Compares the indices of both tokens.
     *
     * @param o the other token.
     * @return <code>index - o.getIndex()</code>
     */
    public int compareTo(Token o) {
        return index - o.getIndex();
    }

    /**
     * Returns a string representation of this token containing token index and properties.
     *
     * @return a string representation of this token.
     */
    public String toString() {
        return index + ":" + tokenProperties;
    }
}
