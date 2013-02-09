/*
 * dongfang M. Sc. Thesis
 * Created on 2005-05-09
 */
package dongfang.xsltools.resolver;

/**
 * @author dongfang
 */
public class ReadableRequestBean {
  String systemId;
  String humanReadableType;
  String explanation = "[no explanation for this]";
  String none = "Leave blank";
  
  public ReadableRequestBean(String systemId, String humanReadableType) {
    this.systemId = systemId;
    this.humanReadableType = humanReadableType;
  }

  public ReadableRequestBean(String systemId, String humanReadableType, String user, String none) {
      this (systemId, humanReadableType);
    this.explanation = user;
    this.none = none;
  }

  public String getSystemId() {
    return systemId;
  }

  public String getHumanReadable() {
    return humanReadableType;
  }

  public String getExplanation() {
    return explanation;
  }

  @Override
public boolean equals(Object o) {
    if (!(o instanceof ReadableRequestBean))
      return false;
    return ((ReadableRequestBean) o).getSystemId().equals(getSystemId());
  }

  @Override
public int hashCode() {
    return systemId.hashCode();
  }
}
