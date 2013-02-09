/*
 * dongfang M. Sc. Thesis
 * Created on 2005-02-26
 */
package dongfang.xsltools.exceptions;

import org.dom4j.Element;

import dongfang.xsltools.diagnostics.ParseLocation;
import dongfang.xsltools.diagnostics.ParseLocationBean;
import dongfang.xsltools.diagnostics.ParseLocationUtil;

/**
 * @author dongfang
 */
public class XSLToolsLocatableException extends XSLToolsException implements
    Locatable {
  /**
	 * 
	 */
	private static final long serialVersionUID = -6471059330193673724L;

// find out some repr. for this!
  // Em sucker should also be able to refer to the StylesheetModule that
  // contains the error.
  ParseLocation.Extent extent;

  // StylesheetModule module;
  // Element originalElement;

  String originId;

  ParseLocation location;

  /*
   * public XSLToolsLocatableException(StylesheetModule module, Element element,
   * int place, String message) { super(message); init(module, element, place); }
   * 
   * private void init(StylesheetModule module, Element element, int place) {
   * this.module = module; this.place = place; Attribute elementIdAttr =
   * element.attribute(XMLConstants.ELEMENT_ID_QNAME); try { String elementId =
   * elementIdAttr.getValue(); originalElement =
   * module.getElementById(elementId, StylesheetModule.ORIGINAL); } catch
   * (Exception ex) { System.err .println("oops, small problem with a missing
   * element IDs .. should not happen. Element is: " + element + " exception was " +
   * ex.getMessage() + ")"); originalElement = new
   * DocumentFactory().createElement("oops.missing"); } }
   */

  public XSLToolsLocatableException(String originId, Element element,
      ParseLocation.Extent extent, Throwable cause) {
    super(cause);
    init(originId, element, extent);
  }

  public XSLToolsLocatableException(String originId, Element element,
      ParseLocation.Extent extent, String message) {
    super(message);
    init(originId, element, extent);
  }

  private void init(String systemId, Element element,
      ParseLocation.Extent extent) {
    this.originId = systemId;
    this.extent = extent;
    try {
      location = ParseLocationUtil.getParseLocation(element);
    } catch (Exception ex) {
      System.err
          .println("oops, small problem with a missing element IDs .. should not happen. Element is: "
              + element + " exception was " + ex.getMessage() + ")");
      originId = "oops.missing";
    }
  }

  public XSLToolsLocatableException(String originId, int line, int col,
      Throwable cause) {
    super(cause);
    init(originId, line, col);
  }

  public XSLToolsLocatableException(String origonId, int line, int col,
      String message) {
    super(message);
    init(originId, line, col);
  }

  private void init(String originId, int line, int col) {
    this.originId = originId;
    this.extent = ParseLocation.Extent.POINT;
    location = new ParseLocationBean(line, col);
  }

  public String getOriginId() {
    return originId;
  }

  public ParseLocation.Extent getExtent() {
    return extent;
  }

  public String getParsePositionAsReadableString() {
    if (location == null)
      return "No parse position available";
    return location.toReadableString(extent);
  }

  public ParseLocation getParseLocation() {
    return location;
  }

  @Override
  public String getMessage() {
    return super.getMessage();// + module;
  }
}
