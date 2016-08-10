package won.matcher.solr.query;

/**
 * Created by hfriedrich on 01.08.2016.
 */
public class MatchFieldQuery extends SolrQueryFactory
{
  private String fieldName;
  private String value;

  public MatchFieldQuery(String fieldName, String value) {
    this.value = value;
    this.fieldName = fieldName;
  }

  @Override
  protected String makeQueryString() {
    return String.join("", fieldName, " : ", value);
  }
}
