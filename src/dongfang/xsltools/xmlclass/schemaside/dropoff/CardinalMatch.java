package dongfang.xsltools.xmlclass.schemaside.dropoff;


public class CardinalMatch {

  DynamicRedeclaration alternative1;
  DynamicRedeclaration alternative2;

  private CardinalMatch() {}
  
  public static final CardinalMatch NEVER_ARG_TYPE = new CardinalMatch();
  public static final CardinalMatch MAYBE_ARG_TYPE = new CardinalMatch();
  public static final CardinalMatch ALWAYS_ARG_TYPE = new CardinalMatch();
  
  CardinalMatch(DynamicRedeclaration alternative1, DynamicRedeclaration alternative2) {
    this.alternative1 = alternative1;
    this.alternative2 = alternative2;
  }

  public DynamicRedeclaration getPassAlternative() {
    return alternative1;
  }

  public DynamicRedeclaration getFailAlternative() {
    return alternative2;
  }
  
  public boolean isSplit() {
    return alternative1 != null || alternative2 != null;
  }
  
  public boolean argTypeNeverPasses() {
    return this == NEVER_ARG_TYPE;
  }
  
  public boolean cannotDetermine() {
    return this == MAYBE_ARG_TYPE;
  }
  
  public boolean argTypeAlwaysPasses() {
    return this == ALWAYS_ARG_TYPE;
  }
}
