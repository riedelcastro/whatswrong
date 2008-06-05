package whatswrong;

/**
 * @author Sebastian Riedel
 */
public class Edge {

  public enum RenderType {
    span, dependency
  }

  private TokenVertex from, to;
  private String label;
  private String type;
  private RenderType renderType = RenderType.dependency;


  public Edge(TokenVertex from, TokenVertex to, String label, String type, RenderType renderType) {
    this.from = from;
    this.to = to;
    this.label = label;
    this.type = type;
    this.renderType = renderType;
  }

  public Edge(TokenVertex from, TokenVertex to, String label, String type) {
    this.from = from;
    this.to = to;
    this.label = label;
    this.type = type;
  }

  public String getTypePrefix() {
    int index = type.indexOf(':');
    return index == -1 ? type : type.substring(0, index);
  }

  public String getTypePostfix() {
    int index = type.indexOf(':');
    return index == -1 ? "" : type.substring(index + 1);
  }

  public int getMinIndex() {
    return from.getIndex() < to.getIndex() ? from.getIndex() : to.getIndex();
  }

  public int getMaxIndex() {
    return from.getIndex() > to.getIndex() ? from.getIndex() : to.getIndex();
  }

  public RenderType getRenderType() {
    return renderType;
  }

  public void setRenderType(RenderType renderType) {
    this.renderType = renderType;
  }

  public TokenVertex getFrom() {
    return from;
  }

  public TokenVertex getTo() {
    return to;
  }

  public String getLabel() {
    return label;
  }

  public String getType() {
    return type;
  }

  public int lexicographicOrder(Edge edge) {
    int result = type.compareTo(edge.type);
    return result == 0 ? -label.compareTo(edge.label) : -result;
  }

  public boolean leftOf(TokenVertex token) {
    return from.getIndex() <= token.getIndex() && to.getIndex() <= token.getIndex();
  }

  public boolean rightOf(TokenVertex token) {
    return from.getIndex() >= token.getIndex() && to.getIndex() >= token.getIndex();
  }

  public int getLength() {
    return Math.abs(from.getIndex() - to.getIndex());
  }

  public boolean covers(Edge edge) {
    return getMinIndex() < edge.getMinIndex() && getMaxIndex() > edge.getMaxIndex();
  }

  public boolean coversExactly(Edge edge) {
    return getMinIndex() == edge.getMinIndex() && getMaxIndex() == edge.getMaxIndex();
  }

  public boolean coversSemi(Edge edge) {
    return
      getMinIndex() < edge.getMinIndex() && getMaxIndex() == edge.getMaxIndex() ||
      getMinIndex() == edge.getMinIndex() && getMaxIndex() > edge.getMaxIndex();
  }

  public boolean overlaps(Edge edge) {
    return getMinIndex() <= edge.getMinIndex() &&
      getMaxIndex() <= edge.getMaxIndex() &&
      getMaxIndex() >= edge.getMinIndex() ||
      getMinIndex() >= edge.getMinIndex() &&
      getMaxIndex() <= edge.getMaxIndex() &&
      getMaxIndex() <= edge.getMinIndex();
  }


  public boolean strictlyCovers(Edge edge) {
    return getMinIndex() < edge.getMinIndex() && getMaxIndex() >= edge.getMaxIndex() ||
      getMinIndex() <= edge.getMinIndex() && getMaxIndex() > edge.getMaxIndex();
  }


  public String toString() {
    return from.getIndex() + "-" + label + "->" + to.getIndex() + "(" + type + ")";
  }

  public boolean crosses(Edge edge) {
    return getMinIndex() > edge.getMinIndex()
      && getMinIndex() < edge.getMaxIndex()
      && getMaxIndex() > edge.getMaxIndex() ||
      edge.getMinIndex() > getMinIndex()
        && edge.getMinIndex() < getMaxIndex()
        && edge.getMaxIndex() > getMaxIndex();

  }


  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Edge that = (Edge) o;

    if (from != null ? !from.equals(that.from) : that.from != null) return false;
    if (label != null ? !label.equals(that.label) : that.label != null) return false;
    if (to != null ? !to.equals(that.to) : that.to != null) return false;
    //noinspection RedundantIfStatement
    if (type != null ? !type.equals(that.type) : that.type != null) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (from != null ? from.hashCode() : 0);
    result = 31 * result + (to != null ? to.hashCode() : 0);
    result = 31 * result + (label != null ? label.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    return result;
  }
}
