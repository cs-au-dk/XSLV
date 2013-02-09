/*
 * Created on Jun 4, 2005
 */
package dongfang.xsltools.xmlclass.xsd;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;

import dk.brics.misc.Origin;
import dongfang.xsltools.diagnostics.Diagnoseable;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.diagnostics.ParseLocationUtil;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsLoadException;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.exceptions.XSLToolsXPathUnresolvedNamespaceException;
import dongfang.xsltools.model.ElementNamespaceExpander;
import dongfang.xsltools.model.ModelConfiguration;
import dongfang.xsltools.model.NamespaceExpander;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.util.UniqueNameGenerator;

/**
 * @author Soren Kuula
 */
class XSDSchemaDocument extends XSDAbstractDocument implements Diagnoseable {
  private Document document;

  Set<Element> topLevelElementXML = new HashSet<Element>();

  Map<QName, Element> allElementsXMLByRef = new HashMap<QName, Element>();

  Map<QName, Element> allAttributesXMLByRef = new HashMap<QName, Element>();

  Map<QName, Element> allTypedefXMLByRef = new HashMap<QName, Element>();

  Map<QName, Element> attributeGroupDefs = new HashMap<QName, Element>();

  Map<QName, Element> modelGroupDefs = new HashMap<QName, Element>();

  private Namespace targetNamespace;

  /*
   * base URI of this schema document.
   */
  final private String systemId;

  private boolean elementsQualifiedDefault;

  private boolean attributesQualifiedDefault;

  private String blockDefault;

  XSDSchemaDocument(String systemId) {
    super(systemId);
    this.systemId = systemId;
  }

  /*
   * Some simple accessors; they do the obvious
   */
  @Override
String getTargetNamespaceURI() {
    return targetNamespace.getURI();
  }

  @Override
String getTargetNamespacePrefix() {
    return targetNamespace.getPrefix();
  }

  @Override
Namespace getTargetNamespace() {
    return targetNamespace;
  }

  boolean elementsQualifiedDefault() {
    return elementsQualifiedDefault;
  }

  boolean attributesQualifiedDefault() {
    return attributesQualifiedDefault;
  }

  Document getDocument() {
    return document;
  }

  Element getDocumentElement() {
    return document.getRootElement();
  }

  Element getElementDeclDOMRepr(QName name) {
    return allElementsXMLByRef.get(name);
  }

  Element getAttributeDeclDOMRepr(QName name) {
    return allAttributesXMLByRef.get(name);
  }

  Element getTypedefDOMRepr(QName name) {
    return allTypedefXMLByRef.get(name);
  }

  @Override
Element getAttributeGroupDef(QName name) {
    return attributeGroupDefs.get(name);
  }

  @Override
Element getModelGroupDef(QName name) {
    return modelGroupDefs.get(name);
  }

  /*
   * Complain about definitions / declarations that are no longer unique for
   * their name, because on a bad include, or because redefine is not
   * implemented (!)
   */
  void checkDocumentCompatibility(XSDSchemaDocument other) {
    Set<QName> sniff = new HashSet<QName>();

    sniff.addAll(allTypedefXMLByRef.keySet());
    sniff.retainAll(other.allTypedefXMLByRef.keySet());
    if (!sniff.isEmpty()) {
      System.err.println("Documents at " + systemId + " and " + other.systemId
          + ":");
      System.err
          .println("Type conflict at include / import (or unhandled redefine)");
      for (QName name : sniff) {
        System.err.println(Dom4jUtil.clarkName(name));
      }
    }
    sniff.clear();

    sniff.addAll(allElementsXMLByRef.keySet());
    sniff.retainAll(other.allElementsXMLByRef.keySet());
    if (!sniff.isEmpty()) {
      System.err.println("Documents at " + systemId + " and " + other.systemId
          + ":");
      System.err
          .println("Element conflict at include / import (or unhandled redefine)");
      for (QName name : sniff) {
        System.err.println(Dom4jUtil.clarkName(name));
      }
    }
    sniff.clear();

    sniff.addAll(allAttributesXMLByRef.keySet());
    sniff.retainAll(other.allAttributesXMLByRef.keySet());
    if (!sniff.isEmpty()) {
      System.err.println("Documents at " + systemId + " and " + other.systemId
          + ":");
      System.err
          .println("Attribute conflict at include / import (or unhandled redefine)");
      for (QName name : sniff) {
        System.err.println(Dom4jUtil.clarkName(name));
      }
    }
    sniff.clear();
  }

  /**
   * Derive the proper QName of a declaration / definition, by a (typical XSD)
   * complicated process involving the declaration / definition's name, ref and
   * form attributes (whichever are present), the default form, the ,position of
   * the declaration in the schema document (toplevel or otherwise), the target
   * namespace of the document as well as the phase of the moon.
   * 
   * @param element
   * @param xsdTargetNamespaceURI
   * @return
   * @throws XSLToolsXPathUnresolvedNamespaceException
   */
  public QName getDeclaredElementOrAttributeName(Element element,
      boolean defaultQualified)
      throws XSLToolsXPathUnresolvedNamespaceException {

    String nameAttVal = element
        .attributeValue(XSDSchemaConstants.ATTR_NAME_QNAME);
    String refAttVal = element
        .attributeValue(XSDSchemaConstants.ATTR_REF_QNAME);
    String formAttVal = element
        .attributeValue(XSDSchemaConstants.ATTR_FORM_QNAME);

    assert ((nameAttVal != null && refAttVal == null) || (nameAttVal == null && refAttVal != null));

    if (nameAttVal == null) {
      // Ref's are always qualified.
      // for schema components in no namespace land, this might be wrong
      // (as the util will use the default namespace, not no-namespace)
      QName qname = ElementNamespaceExpander.qNameForXSLAttributeValue(
          refAttVal, element,
          NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);
      return qname;
    }

    if (nameAttVal.indexOf(":") < 0) { // should always be true ?!?!?
      boolean localsQualified = defaultQualified;
      if (formAttVal != null) {
        localsQualified = "qualified".equals(formAttVal);
      }

      boolean local = true;
      if (element.getParent() != null
          && element.getParent().getQName().equals(
              XSDSchemaConstants.ELEM_SCHEMA_QNAME))
        local = false;

      if (local && !localsQualified)
        return QName.get(nameAttVal, Namespace.NO_NAMESPACE);

      String prefix = Dom4jUtil.getPrefixFor(element, "temp",
          getTargetNamespaceURI());

      return QName.get(nameAttVal, prefix, getTargetNamespaceURI());
    }
    return ElementNamespaceExpander.qNameForXSLAttributeValue(nameAttVal,
        element, NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);
  }

  QName getDeclaredElementName(Element element)
      throws XSLToolsXPathUnresolvedNamespaceException {
    return getDeclaredElementOrAttributeName(element, elementsQualifiedDefault);
  }

  QName getDeclaredAttributeName(Element element)
      throws XSLToolsXPathUnresolvedNamespaceException {
    return getDeclaredElementOrAttributeName(element,
        attributesQualifiedDefault);
  }

  /*
   * Set the document, init very basic things: Target namespace, elements and
   * attributes qualified defalt. Then, do init().
   */
  void setDocument(Document document, String targetOverride,
      ErrorReporter cesspool) throws XSLToolsXPathUnresolvedNamespaceException,
      XSLToolsException {
    if (!XSDSchemaConstants.NAMESPACE_URI.equals(document.getRootElement()
        .getNamespaceURI()))
      cesspool.reportError(new XSLToolsLoadException(
          "Not a W3C XML Schema document"));

    this.document = document;

    String targetNamespaceURI = getDocumentElement().attributeValue(
        XSDSchemaConstants.ATTR_TARGET_NAMESPACE_QNAME);

    if (targetNamespaceURI == null)
      targetNamespaceURI = "";

    // assert (targetNamespaceURI != null) : "I'm confused about schemas w o
    // target namespace";

    String prefix = Dom4jUtil.getPrefixFor(getDocumentElement(), "target",
        targetNamespaceURI);

    if (targetOverride == null) {
      targetNamespace = Namespace.get(prefix, targetNamespaceURI);
    } else
      targetNamespace = Namespace.get(targetOverride);

    String s = getDocumentElement().attributeValue(
        XSDSchemaConstants.ATTR_ELEMENT_FORM_DEFAULT_QNAME);
    elementsQualifiedDefault = "qualified".equals(s);

    s = getDocumentElement().attributeValue(
        XSDSchemaConstants.ATTR_ATTRIBUTE_FORM_DEFAULT_QNAME);
    attributesQualifiedDefault = "qualified".equals(s);

    blockDefault = getDocumentElement().attributeValue(
        XSDSchemaConstants.ATTR_BLOCK_DEFAULT_QNAME);

    init();
  }

  /*
   * Record all top level element declaration (just in DOM form), all top level
   * attribute declarations (just in DOM form), all top level type definitions
   * (just in DOM form), and all top level (and only, right?) model and
   * attribute group definitions.
   */
  void init() throws XSLToolsSchemaException {
    Element root = getDocumentElement();

    topLevelElementXML.clear();
    allElementsXMLByRef.clear();
    allAttributesXMLByRef.clear();
    attributeGroupDefs.clear();
    modelGroupDefs.clear();

    for (Iterator toplevelIterator = root.elementIterator(); toplevelIterator
        .hasNext();) {
      Element toplevelElement = (Element) toplevelIterator.next();
      init(toplevelElement);
    }
  }

  private void init(Element toplevelElement) throws XSLToolsSchemaException {
    if (toplevelElement.getNamespaceURI().equals(
        XSDSchemaConstants.NAMESPACE_URI)) {

      String nameAttVal = toplevelElement
          .attributeValue(XSDSchemaConstants.ATTR_NAME_QNAME);

      QName qname = nameAttVal == null ? null : QName.get(nameAttVal,
          targetNamespace);

      if (toplevelElement.getName().equals(XSDSchemaConstants.ELEM_ELEMENT)) {
        if (nameAttVal != null) {
          topLevelElementXML.add(toplevelElement);
          allElementsXMLByRef.put(qname, toplevelElement);
        } else
          throw new XSLToolsSchemaException(
              "Toplevel element declaration without a name!");
      }

      else if (toplevelElement.getName().equals(
          XSDSchemaConstants.ELEM_ATTRIBUTE)) {
        if (nameAttVal != null) {
          allAttributesXMLByRef.put(qname, toplevelElement);
        } else
          throw new XSLToolsSchemaException(
              "Toplevel attribute declaration without a name!");
      }

      else if (toplevelElement.getName().equals(
          XSDSchemaConstants.ELEM_COMPLEX_TYPE)
          || toplevelElement.getName().equals(
              XSDSchemaConstants.ELEM_SIMPLE_TYPE)) {
        if (nameAttVal != null) {
          // Dom4jUtil.transferAttributeValue(qname,
          // XMLConstants.XSD_ATTR_REFNAME, toplevelElement);
          // toplevelElement.addAttribute("typeNSURI", qname.getNamespaceURI());
          allTypedefXMLByRef.put(qname, toplevelElement);
        } else
          throw new XSLToolsSchemaException("Toplevel typedef without a name!");
      }

      else if (toplevelElement.getName().equals(
          XSDSchemaConstants.ELEM_ATTRIBUTE_GROUP)) {
        if (nameAttVal != null) {
          // Dom4jUtil.transferAttributeValue(qname,
          // XMLConstants.XSD_ATTR_REFNAME, toplevelElement);
          attributeGroupDefs.put(qname, toplevelElement);
        } else
          throw new XSLToolsSchemaException("Attribute group without a name!");
      }

      else if (toplevelElement.getName().equals(XSDSchemaConstants.ELEM_GROUP)) {
        if (nameAttVal != null) {
          // Dom4jUtil.transferAttributeValue(qname,
          // XMLConstants.XSD_ATTR_REFNAME, toplevelElement);
          modelGroupDefs.put(qname, toplevelElement);
        } else
          throw new AssertionError("HUH? Group without a name");
      }

      else if (toplevelElement.getName()
          .equals(XSDSchemaConstants.ELEM_INCLUDE)) {
        // ignore
      }
    }
  }

  /*
   * Fetch the set of URI's (in string form) that are imported from in this
   * module.
   */
  Set<XSDSchemaFactory.LoadingSchema> resolveImportReferences(
      String baseURIString) throws URISyntaxException {
    Set<XSDSchemaFactory.LoadingSchema> result = new HashSet<XSDSchemaFactory.LoadingSchema>();
    Element root = getDocumentElement();
    for (Iterator iter = root.elements().iterator(); iter.hasNext();) {
      Element element = (Element) iter.next();
      if (element.getQName().equals(XSDSchemaConstants.ELEM_IMPORT_QNAME)) {
        String attVal = element.attributeValue(QName
            .get(XSDSchemaConstants.ATTR_SCHEMA_LOCATION));
        // must find a way to react to missing location...
        URI importURI = new URI(attVal);
        URI baseURI = new URI(baseURIString);
        URI total = baseURI.resolve(importURI);
        String namespace = element
            .attributeValue(XSDSchemaConstants.ATTR_NAMESPACE_QNAME);
        result.add(new XSDSchemaFactory.LoadingSchema(total.toString(),
            namespace));
      }
    }
    return result;
  }

  /*
   * Fetch the set of URI's (in string form) that are imported from in this
   * module.
   */
  Set<XSDSchemaFactory.LoadingSchema> resolveIncludeReferences(
      String baseURIString) throws URISyntaxException {
    Set<XSDSchemaFactory.LoadingSchema> result = new HashSet<XSDSchemaFactory.LoadingSchema>();
    Element root = getDocumentElement();
    for (Iterator iter = root.elements().iterator(); iter.hasNext();) {
      Element element = (Element) iter.next();
      if (element.getQName().equals(XSDSchemaConstants.ELEM_INCLUDE_QNAME)) {
        String attVal = element.attributeValue(QName
            .get(XSDSchemaConstants.ATTR_SCHEMA_LOCATION));
        // must find a way to react to missing location...
        URI importURI = new URI(attVal);
        URI baseURI = new URI(baseURIString);
        URI total = baseURI.resolve(importURI);
        String pladderslam = null;
        if (!targetNamespace.getURI().equals(Namespace.NO_NAMESPACE.getURI()))
          pladderslam = targetNamespace.getURI();
        result.add(new XSDSchemaFactory.LoadingSchema(total.toString(),
            pladderslam));
      }
    }
    return result;
  }

  /*
   * See next method.
   */
  @Override
void prepareDOM4Flattening(XSDSchema schema)
      throws XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException {
    prepareDOM4Flattening(document.getRootElement(),
        new HashMap<String, String>(), new HashSet<String>(), schema);
  }

  /*
   * Make copies of namespace nodes on every element in their scope (in
   * preparation for disassembly of the tree), and remove PIs, comments,
   * whitespace text nodes, and annotation elements.
   */
  private void prepareDOM4Flattening(Element e, Map<String, String> nss,
      Set<String> alreadyOn, XSDSchema schema)
      throws XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException {
    // this is a temp. set
    alreadyOn.clear();

    // first, sniff up all local namespace bindings on the element
    for (Iterator ni = e.content().iterator(); ni.hasNext();) {
      Node n = (Node) ni.next();
      if (n.getNodeType() == Node.NAMESPACE_NODE) {
        Namespace ns = (Namespace) n;
        alreadyOn.add(ns.getPrefix());
        nss.put(ns.getPrefix(), ns.getURI());
      }
    }

    // Then, add all those not there already
    for (Map.Entry<String, String> nse : nss.entrySet()) {
      if (!alreadyOn.contains(nse.getKey())) {
        e.add(Namespace.get(nse.getKey(), nse.getValue()));
      }
    }

    for (ListIterator ni = e.content().listIterator(); ni.hasNext();) {
      Node n = (Node) ni.next();
      switch (n.getNodeType()) {
      case Node.COMMENT_NODE:
        // Death on comment nodes
        ni.remove();
        break;
      case Node.PROCESSING_INSTRUCTION_NODE:
        // Death on PI nodes
        ni.remove();
        break;
      case Node.TEXT_NODE: {
        // Death on whitespace text nodes (could kill em all??)
        String s = ((org.dom4j.Text) n).getText();
        if (s.trim().equals(""))
          ni.remove();
        break;
      }
      case Node.ELEMENT_NODE: {
        Element e2 = (Element) n;
        if (e2.getQName().equals(XSDSchemaConstants.ELEM_ANNOTATION_QNAME)) {
          // Death on annotation subtrees
          ni.remove();
          break;
        }

        if (e2.getQName().equals(XSDSchemaConstants.ELEM_GROUP_QNAME)) {
          String ref = e2.attributeValue(XSDSchemaConstants.ATTR_REF_QNAME);
          if (ref == null) {
            // it's a definition, just leave it in place
          } else {
            // resolve model group!
            QName groupName = ElementNamespaceExpander
                .qNameForXSLAttributeValue(ref, e2,
                    NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);

            Element group = schema.getModelGroupDef(groupName);

            if (group == null)
              throw new XSLToolsSchemaException("Model group did not resolve: "
                  + Dom4jUtil.clarkName(groupName));

            Element copy = (Element) group.clone();
            prepareDOM4Flattening(copy, new HashMap<String, String>(nss),
                new HashSet<String>(), schema);

            // Remove the reference
            ni.remove();
            // replace it by what it referred to
            int noSubstitutions = 0;
            for (Iterator copyi = copy.elementIterator(); copyi.hasNext();) {
              Element copy2 = (Element) ((Element) copyi.next()).clone();

              String minOccurs = e2
                  .attributeValue(XSDSchemaConstants.ATTR_MIN_OCCURS_QNAME);
              if (minOccurs != null)
                copy2.addAttribute(XSDSchemaConstants.ATTR_MIN_OCCURS,
                    minOccurs);

              String maxOccurs = e2
                  .attributeValue(XSDSchemaConstants.ATTR_MAX_OCCURS_QNAME);
              if (maxOccurs != null)
                copy2.addAttribute(XSDSchemaConstants.ATTR_MAX_OCCURS,
                    maxOccurs);

              // stunt: Clean up annotations that survived because they
              // are defined in a part of the schema not yet prepared for
              // flattening,
              // as well as nested model groups / attribute groups,
              // if such a thing exists...
              if (!copy.getQName().equals(
                  XSDSchemaConstants.ELEM_ANNOTATION_QNAME)) {
                ni.add(copy2);
                noSubstitutions++;
              }
            } // Iterator copyi
            if (noSubstitutions > 1)
              throw new XSLToolsSchemaException("Model group "
                  + Dom4jUtil.clarkName(groupName) + " had more than one child");
          } // a group def
        }

        /*
         * resolve attribute groups. DONE: Attribute group references in
         * attribute group definitions are not handled.
         */
        if (e2.getQName().equals(XSDSchemaConstants.ELEM_ATTRIBUTE_GROUP_QNAME)) {
          String ref = e2.attributeValue(XSDSchemaConstants.ATTR_REF_QNAME);
          if (ref == null) {
            // it's a definition, just leave it in place
          } else {
            QName groupName = ElementNamespaceExpander
                .qNameForXSLAttributeValue(ref, e2,
                    NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);
            Element group = schema.getAttributeGroupDef(groupName);

            if (group == null)
              throw new XSLToolsSchemaException(
                  "Attribute group did not resolve: "
                      + groupName.getQualifiedName() + " in document "
                      + systemId);

            Element copy = (Element) group.clone();
            prepareDOM4Flattening(copy, new HashMap<String, String>(nss),
                new HashSet<String>(), schema);
            ni.remove();
            for (Iterator copyi = copy.elementIterator(); copyi.hasNext();) {
              Element copy2 = (Element) ((Element) copyi.next()).clone();
              // stunt: Clean up annotations that survived until here.
              if (!copy.getQName().equals(
                  XSDSchemaConstants.ELEM_ANNOTATION_QNAME))
                ni.add(copy2);
            }
          }
        }

        // recurse, with a COPY of the binding map.
        prepareDOM4Flattening((Element) n, new HashMap<String, String>(nss),
            alreadyOn, schema);
        break;
      }
      }
    }
  }

  @Override
void killGroupDefs() {
    for (Iterator killer = getDocumentElement().elementIterator(); killer
        .hasNext();) {
      Element e = (Element) killer.next();
      if (XSDSchemaConstants.ELEM_GROUP_QNAME.equals(e.getQName())
          || XSDSchemaConstants.ELEM_ATTRIBUTE_GROUP_QNAME.equals(e.getQName()))
        killer.remove();
    }
    // System.out.println("nach");
    // Dom4jUtil.debugPrettyPrint(document);
  }

  /*
   * Flatten content! Every type definition within a content model is removed,
   * stored in the global type definition map under a fresh name, and replaced
   * by a type reference to the fresh name. Every element / attribute
   * declaration within a content model is removed, stored in the global type
   * definition map under a fresh name, and replaced by a an element / attribute
   * reference to the fresh name. After flattening, there are no element
   * attribute declarations in type definitions (only references), and no type
   * definitions in element / attribute declarations (only references). However,
   * the names used for referencing are not necessarily the names of the
   * elements / attributes / types referenced (so this is not generally
   * representable in legal XSD). No ElementDecl or anything else is
   * constructed; it's merely about moving around stuff in DOM trees and storing /
   * refering to it under names.
   */
  @Override
void flatten(UniqueNameGenerator names)
      throws XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException {
    DocumentFactory dfac = ModelConfiguration.current.getDocumentFactory();
    ListIterator it = document.getRootElement().elements().listIterator();
    while (it.hasNext())
      flattenToplevel((Element) it.next(), names, dfac);
  }

  void flattenToplevel(Element e, UniqueNameGenerator names,
      DocumentFactory dfac) throws XSLToolsXPathUnresolvedNamespaceException,
      XSLToolsSchemaException {
    String currentName = null;
    if (e.getQName().equals(XSDSchemaConstants.ELEM_ELEMENT_QNAME)
        || e.getQName().equals(XSDSchemaConstants.ELEM_ATTRIBUTE_QNAME))
      currentName = e.attributeValue(XSDSchemaConstants.ATTR_NAME_QNAME);
    if (currentName != null) {
      int colonIdx;
      if ((colonIdx = currentName.indexOf(':')) > 0) {
        currentName = currentName.substring(colonIdx + 1);
      }
    }
    ListIterator it = e.elements().listIterator();
    flatten(it, names, dfac, currentName);
  }

  /*
   * Ref everything ref-able, except model / attr groups. That is, with every
   * non toplevel named element: - Replace it by an element ref=xxx, and put a
   * binding xxx->the original element in the element decl map - The same for
   * attributes - For every non toplevel type: - Replace it by a type ref=xxx,
   * and put a binding xxx->the original type in the type def map
   * 
   * After this, we have only elements with a typeref, a type child with nothing
   * but a typeref, or no type at all, and types where all elements have a ref,
   * and don't contain anything.
   * 
   * CurrentName is just a feature for making a fresh type name a little bit
   * clearer -- it will become typeof-a-xx, for a typedef that used to be nested
   * inside an element named a. It could be removed w/ no change in semantics.
   */
  void flatten(ListIterator iter, UniqueNameGenerator names,
      DocumentFactory dfac, String currentName)
      throws XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException {
    while (iter.hasNext()) {
      // replace everything with a name by a ref to a fresh definition /
      // declaration.
      Element e = (Element) iter.next();

      if (e.getName().equals("annotation")) {
        throw new AssertionError("Die-hard annotation: " + e.getText());
      }

      if (e.getName().equals(XSDSchemaConstants.ELEM_ELEMENT)
          || e.getName().equals(XSDSchemaConstants.ELEM_ATTRIBUTE)) {

        String ref = e.attributeValue(XSDSchemaConstants.ATTR_REF_QNAME);

        if (ref == null) {
          // it's not a ref (refs we just leave)
          Attribute nameAtt = e.attribute(XSDSchemaConstants.ATTR_NAME_QNAME);
          if (nameAtt != null) {
            Element replacement = dfac.createElement(e.getQName());
            // clone attributes
            for (int i = 0; i < e.attributeCount(); i++) {
              Attribute a = e.attribute(i);
              if (!a.getQName().equals(XSDSchemaConstants.ATTR_NAME_QNAME)
                  && !a.getQName().equals(XSDSchemaConstants.ATTR_TYPE_QNAME))
                replacement.addAttribute(a.getQName(), a.getValue());
            }

            QName refname = null;

            do {
              // Make sure to pick a non colliding name
              refname = QName.get(names.getFreshId("ref-%1S"), targetNamespace);
            } while (allElementsXMLByRef.get(refname) != null
                || allAttributesXMLByRef.get(refname) != null);

            Dom4jUtil.transferAttributeValue(refname,
                XSDSchemaConstants.ATTR_REF, replacement);

            // about here, e should be detached.
            if (e.getName().equals(XSDSchemaConstants.ELEM_ELEMENT)) {
              QName realName = getDeclaredElementOrAttributeName(e,
                  elementsQualifiedDefault);
              // if (realName.getName().equals("space"))
              // System.err.println(realName);
              Dom4jUtil.transferAttributeValue(realName,
                  XSDSchemaConstants.ATTR_NAME, e);
              // Dom4jUtil.transferAttributeValue(realName,
              // XSDSchemaConstants.ATTR_NAME, replacement);

              /*
               * String test =
               * e.attributeValue(XSDSchemaConstants.ATTR_NAME_QNAME); QName
               * testq =
               * ElementNamespaceExpander.qNameForXSLAttributeValue(test, e,
               * NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);
               * 
               * if (!realName.equals(testq)) {
               * System.err.println(realName.getQualifiedName());
               * System.err.println(testq.getQualifiedName()); }
               */

              allElementsXMLByRef.put(refname, e);
              currentName = realName.getName();
            } else {
              QName realName = getDeclaredElementOrAttributeName(e,
                  attributesQualifiedDefault);
              Dom4jUtil.transferAttributeValue(realName,
                  XSDSchemaConstants.ATTR_NAME, e);
              // Dom4jUtil.transferAttributeValue(realName,
              // XSDSchemaConstants.ATTR_NAME, replacement);
              allAttributesXMLByRef.put(refname, e);
              currentName = realName.getName();
            }

            // flatten contents of element before we detach it
            ListIterator iter2 = e.elements().listIterator();
            flatten(iter2, names, dfac, currentName);
            iter.set(replacement);

          } else {
            throw new XSLToolsSchemaException(
                "Element or attribute without name attribute and without ref attribute encountered!");
          }
          // QName refq =
          // ElementNamespaceExpander.qNameForXSLAttributeValue(ref,
          // e, NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);
          // Dom4jUtil.transferAttributeValue(refq,
          // XSDSchemaConstants.ATTR_REF, e);
          // e.addAttribute("nameNSURI", refq.getNamespaceURI());
        }
      } else
      // unnamed, unrefd type definition
      if (e.getName().equals(XSDSchemaConstants.ELEM_COMPLEX_TYPE)
          || e.getName().equals(XSDSchemaConstants.ELEM_SIMPLE_TYPE)) {
        String ref = e.attributeValue(XSDSchemaConstants.ATTR_REF_QNAME);
        if (ref == null) {
          Element replacement = dfac.createElement(e.getQName());

          // Make sure to pick a non colliding name
          QName refname = null;
          do {
            String prefix;
            if (currentName == null || "".equals(currentName))
              prefix = "ref";
            else
              prefix = "typeof-" + currentName;
            refname = QName.get(names.getFreshId(prefix + "-%1S"),
                targetNamespace);
          } while (allTypedefXMLByRef.get(refname) != null);

          Dom4jUtil.transferAttributeValue(refname,
              XSDSchemaConstants.ATTR_REF, replacement);
          // flatten contents of element before we detach it
          ListIterator iter2 = e.elements().listIterator();
          flatten(iter2, names, dfac, currentName);

          iter.set(replacement);

          // about here, e should be detached.
          QName realName = refname;
          Dom4jUtil.transferAttributeValue(realName,
              XSDSchemaConstants.ATTR_NAME, e);
          // replacement.addAttribute("typeNSURI", realName.getNamespaceURI());
          allTypedefXMLByRef.put(refname, e);
        } else {
          // type is already ref's to; nothing to split off.
        }
      } else if (e.getName().equals(XSDSchemaConstants.ELEM_ANY)
          || e.getName().equals(XSDSchemaConstants.ELEM_ANY_ATTRIBUTE)) {
        e.addAttribute(XSDSchemaConstants.ATTR_TARGET_NAMESPACE_QNAME,
            targetNamespace.getURI());
      } else {
        ListIterator iter2 = e.elements().listIterator();
        flatten(iter2, names, dfac, currentName);
      }
    }
  }

  /*
   * This was in some respect the opposite of flattening: Type references were
   * resolved and the contents nested into elements. KILLED.
   */
  /*
   * void resolveAllTypes(XSDSchema schema) throws
   * XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException { for
   * (Map.Entry<QName, Element> e : allElementDecls.entrySet()) { Element el =
   * e.getValue(); if (el.elements().size() > 0) { Element typeRef = (Element)
   * el.elements().get(0); String refValue = typeRef
   * .attributeValue(XSDSchemaConstants.ATTR_REF_QNAME); QName refName =
   * ElementNamespaceExpander.qNameForXSLAttributeValue(refValue, typeRef,
   * NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE); Element typeDef =
   * schema.getTypeDef(refName); if (typeDef == null) throw new
   * XSLToolsSchemaException( "A reference in the schema does not resolve: " +
   * refName.getQualifiedName()); typeRef.detach(); el.add((Element)
   * typeDef.clone()); } } }
   */

  /*
   * Post flattening: All elements and attribute decls should either have no
   * type at all, a type ref attribute, or a simpleType / complexType child with
   * a ref attribute at this stage. In the latter case, the child is dropped and
   * its ref attribute is moved up on the declaration. Background: A simpleType
   * of complexType with a ref attribute on it conveys no information really,
   * and it is a simplification to kill it. - The information whether it is
   * simple or complex should be easy to determine from the type referenced. -
   * If there is a ref attribute then there is no content anyway. -- So just
   * move the ref att up on the element, as a type att. All right, if there is a
   * type att already on the element, we do nada. If there is no type ref on the
   * element, and no nested simpleType / complexType, then we make explicit
   * ANY_TYPE.
   */
  @Override
void prepareDecls() throws XSLToolsXPathUnresolvedNamespaceException {
    for (Element e : allElementsXMLByRef.values()) {
      String qualtypeName = e
          .attributeValue(XSDSchemaConstants.ATTR_TYPE_QNAME);
      if (qualtypeName != null) {
        // fine, just leave it on, then.
      } else {
        if (e.elements().isEmpty()) {
          // String name = e.attributeValue(XSDSchemaConstants.ATTR_NAME_QNAME);
          // System.out.println("Element " + name + " has no type at all
          // (!!??)");
          // whack on an anyType then...
          // This should be removed, as there is some funny
          // feature defaulting the type to that of the head of
          // the substitution group (affiliation), it that is present.
          // That is too complicated to handle here.
          /*
           * if
           * (e.attributeValue(XSDSchemaConstants.ATTR_SUBSTITUTION_GROUP_QNAME)!=null)
           * System.err.println("Possible type problem here: A substitution
           * group was" + " referenced but not a type. I should have taken the
           * type from the head"+ " of the subst group, but I don't know yet
           * what that is...");
           */
          Dom4jUtil.transferAttributeValue(
              XSDSchemaConstants.UNSPECIFIED_TYPE_QNAME,
              XSDSchemaConstants.ATTR_TYPE, e);
        } else {
          Element e2 = (Element) e.elements().get(0);
          qualtypeName = e2.attributeValue(XSDSchemaConstants.ATTR_REF_QNAME);
          if (qualtypeName != null) {
            // System.out.println("a nested type that refers to: " + typerefs);
            Dom4jUtil.transferAttributeValue(qualtypeName, e2,
                XSDSchemaConstants.ATTR_TYPE, e,
                NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);
            e2.detach();
          } else
            throw new AssertionError(
                "Element has a nested type type without a ref (flattening should have put a type ref on it)");
        }
      }
    }

    for (Element e : allAttributesXMLByRef.values()) {
      String typerefs = e.attributeValue(XSDSchemaConstants.ATTR_TYPE_QNAME);
      if (typerefs != null) {
        // fine, the attribute decl has a type ref already.
      } else {
        if (e.elements().isEmpty()) {
          // whack on an anySimpleType then...
          // This type defaulting should be removed because the
          // type when nothing it made explicit depends on
          // substitution groups and all kinds of other funny
          // stuff...
          Dom4jUtil.transferAttributeValue(
              XSDSchemaConstants.UNSPECIFIED_SIMPLETYPE_QNAME,
              XSDSchemaConstants.ATTR_TYPE, e);
          /*
           * if
           * (e.attributeValue(XSDSchemaConstants.ATTR_SUBSTITUTION_GROUP_QNAME)!=null)
           * System.err.println("Possible type problem here: A substitution
           * group was" + " referenced but not a type. I should have taken the
           * type from the head"+ " of the subst group, but I don't know yet
           * what that is...");
           */
        } else {
          // there is some content, see if it (is a simpleType and) has a ref
          // attribute.
          // If it does, normalize by moving the attribute up to the attribute
          // decl,
          // and delete the simpleType. Because of the flattening, there should
          // never
          // be a nested simpleType without a ref attribute.
          Element e2 = (Element) e.elements().get(0);
          typerefs = e2.attributeValue(XSDSchemaConstants.ATTR_REF_QNAME);
          if (typerefs != null) {
            // System.out.println("a nested type that refers to: " + typerefs);
            Dom4jUtil.transferAttributeValue(typerefs, e2,
                XSDSchemaConstants.ATTR_TYPE, e,
                NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);
            e2.detach();
          } else
            throw new AssertionError(
                "Attribute has a nested type without a ref (flattening should have put a type ref on it)");
        }
      }
    }
  }

  /*
   * Helper for 2 below methods
   */
  private QName getTypeName(Element decl) throws XSLToolsSchemaException,
      XSLToolsXPathUnresolvedNamespaceException {
    /*
     * String sqname = decl.attributeValue(XSDSchemaConstants.ATTR_NAME_QNAME);
     * QName qname = getDeclaredElementName(decl);
     * 
     * if (!qname.equals(altname)) { System.err.println("Whoops! Element Naming
     * conflict Old method :" + Dom4jUtil.clarkName(altname) + ", new: " +
     * Dom4jUtil.clarkName(qname)); }
     */
    /*
     * System.err.println("Creating element decl for: " + '{' +
     * qname.getNamespaceURI() + '}' + qname.getName());
     */

    // Pick up the declared type name
    String qualtypeName = decl
        .attributeValue(XSDSchemaConstants.ATTR_TYPE_QNAME);

    if (qualtypeName == null)
      return null;

    QName typeqname = ElementNamespaceExpander.qNameForXSLAttributeValue(
        qualtypeName, decl,
        NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);

    return typeqname;
  }

  /*
   * Make XSDElementDecls out of DOM element decls.
   */
  XSDElementDecl mkElementDecl(Element elementDeclXML, XSDSchema schema)
      throws XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException {

    QName declElementName = getDeclaredElementName(elementDeclXML);

   // if (declElementName.getQualifiedName().contains("definitions"))
   //   System.out.println();
    
    // find the XSDType object representing the declared type
    QName typeName = getTypeName(elementDeclXML);
    XSDAbstractType declaredType = null;

    if (typeName != null
        && !typeName.equals(XSDSchemaConstants.UNSPECIFIED_SIMPLETYPE_QNAME)
        && !typeName.equals(XSDSchemaConstants.UNSPECIFIED_TYPE_QNAME)) {

      declaredType = (XSDAbstractType) schema.getTypedef(typeName);

      if (declaredType == null)
        throw new XSLToolsSchemaException("Could not resolve type "
            + Dom4jUtil.clarkName(typeName) + " used as type of element "
            + Dom4jUtil.clarkName(declElementName));
    } else {
      // it is allowed to have an unspecified type, there is a subst grp head
      // to such it off from; otherwise default to anyType.
      // System.err.println("Null typename ??");
    }

    Origin o = ParseLocationUtil.getOrigin(systemId, elementDeclXML);

    return new XSDElementDecl(declElementName, typeName, declaredType,
        elementDeclXML, blockDefault, o);
  }

  /*
   * Make XSDAttributsDecl's out of DOM attribute decls.
   */

  XSDAttributeDecl mkAttributeDecl(Element attributeDeclXML, XSDSchema schema)
      throws XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException {

    // find the XSDType object representing the declared type
    QName typeName = getTypeName(attributeDeclXML);

    if (typeName.equals(XSDSchemaConstants.UNSPECIFIED_SIMPLETYPE_QNAME))
      typeName = XSDSchemaConstants.ANYSIMPLETYPE_QNAME;

    XSDType type = schema.getTypedef(typeName);

    QName declAttributeName = getDeclaredAttributeName(attributeDeclXML);

    if (type == null)
      throw new XSLToolsSchemaException("Could not resolve type "
          + Dom4jUtil.clarkName(typeName) + " used as type of attribute "
          + Dom4jUtil.clarkName(declAttributeName));

    if (type instanceof XSDComplexType)
      throw new XSLToolsSchemaException("Type " + Dom4jUtil.clarkName(typeName)
          + " used as type of attribute "
          + Dom4jUtil.clarkName(declAttributeName) + " is not simple");

    Origin o = ParseLocationUtil.getOrigin(systemId, attributeDeclXML);

    return new XSDAttributeDecl(declAttributeName, typeName,
        (XSDSimpleType) type, o);
  }

  /*
   * Make the XSDElementDecl, XSDAttributeDecl and XSDType objects. Nothing is
   * done in order to establish any sort of type hierarchy (too soon for that).
   */
  @Override
void makeTypeDefs(XSDSchema schema)
      throws XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException {
    for (Map.Entry<QName, Element> e : allTypedefXMLByRef.entrySet()) {

      Element el = e.getValue();
      XSDAbstractType type;
      // e is a simpleType or a complexType.
      // if complexType, children may be:
      // (simpleContent | complexContent |
      // ( (group | all | choice | sequence)?,
      // ( (attribute | attributeGroup)*, anyAttribute?) )
      // )
      //
      // In case of simpleContent, there is a base name, and a restriction child
      // or an extension child.
      // We make the new type a restriction / an extension of the type of that
      // name.
      //
      // In case of complexContent, there is a base name, and a restriction
      // child or an extension child.
      // We make the new type a restriction / an extension of the type of that
      // name.
      //
      String block = el.attributeValue(XSDSchemaConstants.ATTR_BLOCK_QNAME);
      if (block == null)
        block = blockDefault;
      if (el.getQName().equals(XSDSchemaConstants.ELEM_COMPLEX_TYPE_QNAME)) {
        if (el.elements().isEmpty()) {
          // no derivation constructor, and no model .. there is nothing here at
          // all..
          // make an empty-model no-attribute type, deriving by restriction from
          // anyType.
          Origin origin = ParseLocationUtil.getOrigin(systemId, el);
          type = new XSDComplexContentType(e.getKey(),
              XSDSchemaConstants.ANYTYPE_QNAME, Collections.EMPTY_LIST,
              XSDType.DER_RESTRICTION, block, el
                  .attributeValue(XSDSchemaConstants.ATTR_MIXED_QNAME), origin);
        } else {
          Element first = (Element) el.elements().get(0);
          if (first.getQName().equals(
              XSDSchemaConstants.ELEM_SIMPLE_CONTENT_QNAME)) {
            // the base attribute may refer to some other simple content complex
            // type,
            // or to a purely simple type. And at this time, that type may not
            // even have
            // been instantiated. We jut have to instantiate ourselves, and then
            // delay
            // the terrible simple content derivation till when all types have
            // been
            // instantiated.

            if (el.elements().size() != 1)
              throw new XSLToolsSchemaException(
                  "simpleContent element with other siblings than annotation (it never should have!), in the document at: "
                      + systemId);

            if (first.elements().isEmpty())
              throw new XSLToolsSchemaException(
                  "simpleContent element without child elements, in the document at: "
                      + systemId);

            if (first.elements().size() != 1)
              throw new XSLToolsSchemaException(
                  "simpleContent with more than one child element, in the document at: "
                      + systemId);

            Element deriver = (Element) first.elements().get(0);
            String sbase = deriver
                .attributeValue(XSDSchemaConstants.ATTR_BASE_QNAME);

            if (sbase == null)
              throw new XSLToolsSchemaException("Derivation without base");
            QName base = ElementNamespaceExpander.qNameForXSLAttributeValue(
                sbase, deriver,
                NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);
            short derkind;

            if (deriver.getQName().equals(
                XSDSchemaConstants.ELEM_RESTRICTION_QNAME))
              derkind = XSDType.DER_RESTRICTION;
            else if (deriver.getQName().equals(
                XSDSchemaConstants.ELEM_EXTENSION_QNAME))
              derkind = XSDType.DER_EXTENSION;
            else
              throw new XSLToolsSchemaException(
                  "simpleContent without extension or restriction child, in document at: "
                      + systemId + ", child name was: "
                      + deriver.getQualifiedName());

            Origin origin = ParseLocationUtil.getOrigin("todo", el);
            type = new XSDSimpleContentType(e.getKey(), base, derkind, deriver
                .elements(), block, origin); // the
            // simpl
          } else if (first.getQName().equals(
              XSDSchemaConstants.ELEM_COMPLEX_CONTENT_QNAME)) {

            if (el.elements().size() != 1)
              throw new XSLToolsSchemaException(
                  "complexContent element with other siblings than annotation (it never should have!), in the document at: "
                      + systemId);

            boolean hasGoodDeriver = true;
            List deriverElements;
            short derkind;
            QName base = XSDSchemaConstants.ANYTYPE_QNAME;

            if (first.elements().isEmpty()) {
              System.err
                  .println("complexContent without child element in the document at: "
                      + systemId
                      + " , autopatching to an empty restriction of anyType");
              hasGoodDeriver = false;
              derkind = XSDType.DER_RESTRICTION;
              deriverElements = Collections.EMPTY_LIST;
            } else if (first.elements().size() != 1) {
              throw new XSLToolsSchemaException(
                  "complexContent with more than one child element, in the document at: "
                      + systemId);
            } else { // meaning: There is one child
              Element deriver = (Element) first.elements().get(0);
              if (XSDSchemaConstants.ELEM_RESTRICTION_QNAME.equals(deriver
                  .getQName())) {
                derkind = XSDType.DER_RESTRICTION;
                deriverElements = deriver.elements();
              } else if (XSDSchemaConstants.ELEM_EXTENSION_QNAME.equals(deriver
                  .getQName())) {
                derkind = XSDType.DER_EXTENSION;
                deriverElements = deriver.elements();
              } else {
                System.err
                    .println("complexContent some bad (not extension or restriction) child (was: "
                        + Dom4jUtil.clarkName(deriver.getQName()) + ")");
                derkind = XSDType.DER_RESTRICTION;
                // emergency hack: somebody has content in place of restriction
                // or extension. Pick it up.
                deriverElements = first.elements();
                hasGoodDeriver = false;
              }

              // now, derkind, deriverElements are inited... still have base
              // name to go...

              if (hasGoodDeriver) { // meaning: There is an extension or
                // restricion child
                String sbase = deriver
                    .attributeValue(XSDSchemaConstants.ATTR_BASE_QNAME);
                if (sbase == null)
                  throw new XSLToolsSchemaException("Derivation without base");
                base = ElementNamespaceExpander.qNameForXSLAttributeValue(
                    sbase, deriver,
                    NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);
              }
            }

            // mixed attribute on complexContent has, if present, precedence
            // over that on complexType.
            String mixed = first
                .attributeValue(XSDSchemaConstants.ATTR_MIXED_QNAME);
            if (mixed == null)
              mixed = el.attributeValue(XSDSchemaConstants.ATTR_MIXED_QNAME);

            Origin origin = ParseLocationUtil.getOrigin(systemId, el);

            type = new XSDComplexContentType(e.getKey(), base, deriverElements,
                derkind, block, mixed, origin); // the
            // complexContent
          } else {
            Origin origin = ParseLocationUtil.getOrigin(systemId, el);

            type = new XSDComplexContentType(e.getKey(), el.elements(), block,
                el.attributeValue(XSDSchemaConstants.ATTR_MIXED_QNAME), origin);
            // a sequence of regexp
            // constructors followed by
            // attribute stuff
          }
        }
      } else if (el.getQName()
          .equals(XSDSchemaConstants.ELEM_SIMPLE_TYPE_QNAME)) {
        // if should have one child, which is restriction, list or union.
        if (el.elements().size() != 1) {
          throw new XSLToolsSchemaException(
              "simpleType did not have exactly one child (apart from any annotations)");
        }
        Element child = (Element) el.elements().get(0);
        if (child.getQName().equals(XSDSchemaConstants.ELEM_RESTRICTION_QNAME)) {
          // restriction element has this content model:
          // ((simpleType?,
          // (minExclusive | minInclusive | maxExclusive | maxInclusive |
          // totalDigits
          // | fractionDigits | length | minLength | maxLength | enumeration
          // | whiteSpace | pattern)*))
          // and its base attribute is OPTIONAL.
          // In our case, any nested simpleType should have been cut and refd
          // in the flattening.
          // Approach must be:
          // 1) Resolve name of restricted type, either as base (low precedence)
          // or as
          // nested type (high precedence) or as anySimpleType (if nothing else
          // present).
          // 2) Suck up facet list
          // 3) go!
          QName derivedTypeName = XSDSchemaConstants.ANYSIMPLETYPE_QNAME;

          String sbase = child
              .attributeValue(XSDSchemaConstants.ATTR_BASE_QNAME);
          if (sbase != null) {
            derivedTypeName = ElementNamespaceExpander
                .qNameForXSLAttributeValue(sbase, child,
                    NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);
          }

          if (!child.elements().isEmpty()) {
            Element maybeSimpleType = (Element) child.elements().get(0);
            if (maybeSimpleType.getQName().equals(
                XSDSchemaConstants.ELEM_SIMPLE_TYPE_QNAME)) {
              String sref = maybeSimpleType
                  .attributeValue(XSDSchemaConstants.ATTR_REF_QNAME);
              assert (sref != null) : "Contained typedecl not refd -- flattener failed to do it?";
              derivedTypeName = ElementNamespaceExpander
                  .qNameForXSLAttributeValue(sref, maybeSimpleType,
                      NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);
            }
          }

          Origin origin = ParseLocationUtil.getOrigin("todo", el);
          type = new XSDRestrictionSimpleType(e.getKey(), derivedTypeName,
              XSDType.DER_RESTRICTION, child.elements(), origin);
        } else if (child.getQName().equals(XSDSchemaConstants.ELEM_LIST_QNAME)) {
          Origin origin = ParseLocationUtil.getOrigin("todo", el);

          String sitemType = child
              .attributeValue(XSDSchemaConstants.ATTR_ITEM_TYPE_QNAME);
          QName itemType = null;

          if (sitemType != null)
            itemType = ElementNamespaceExpander.qNameForXSLAttributeValue(
                sitemType, child,
                NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);

          if (itemType == null) {
            // we have to find it among the children then...
            for (Iterator it = child.elements().iterator(); it.hasNext();) {
              Element test = (Element) it.next();
              if (test.getQName().equals(
                  XSDSchemaConstants.ELEM_SIMPLE_TYPE_QNAME)) {
                sitemType = test
                    .attributeValue(XSDSchemaConstants.ATTR_REF_QNAME);
                if (sitemType == null)
                  throw new XSLToolsSchemaException(
                      "SimpleType (int list) without ref attribute after flattening!");
                itemType = ElementNamespaceExpander.qNameForXSLAttributeValue(
                    sitemType, test,
                    NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);
                break;
              }
            }
          }

          if (itemType == null)
            throw new XSLToolsSchemaException(
                "Could not find any item type of the list simple type named "
                    + Dom4jUtil.clarkName(e.getKey()));

          type = new XSDListSimpleType(e.getKey(), itemType, child.elements(),
              origin);
        } else if (child.getQName().equals(XSDSchemaConstants.ELEM_UNION_QNAME)) {
          Origin origin = ParseLocationUtil.getOrigin("todo", el);

          // DONE: not right??? IS right, base type of union is anySimpleType.
          QName base = XSDSchemaConstants.ANYSIMPLETYPE_QNAME;

          Set<QName> memberTypes = new HashSet<QName>();

          String memberTypeList = child
              .attributeValue(XSDSchemaConstants.ATTR_MEMBER_TYPES_QNAME);
          if (memberTypeList != null) {
            StringTokenizer parserInParser = new StringTokenizer(
                memberTypeList, " \n\t");
            while (parserInParser.hasMoreElements()) {
              String smemberQName = parserInParser.nextToken();
              QName memberQName = ElementNamespaceExpander
                  .qNameForXSLAttributeValue(smemberQName, child,
                      NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);
              memberTypes.add(memberQName);
            }
          }

          type = new XSDUnionSimpleType(e.getKey(), base, child.elements(),
              memberTypes, origin);
          // TODO : Patter og numse

        } else
          throw new XSLToolsSchemaException("simpleType " + e.getKey()
              + " did not have a restriction, list or union child (had: "
              + Dom4jUtil.clarkName(child.getQName()) + ")");
      } else {
        throw new AssertionError(
            "makeDecls sucked in something that was not a simpleType or complexType");
      }
      localTypedefsByRef.put(e.getKey(), type);
    }
  }

  /*
   * Create all element and attribute declarations from their DOM/XML
   * representation.
   */
  @Override
void makeDecls(XSDSchema schema)
      throws XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException {

    for (Map.Entry<QName, Element> e : allElementsXMLByRef.entrySet()) {
      Element el = e.getValue();

      XSDElementDecl myDecl = mkElementDecl(el, schema);
      schema.addElementDecl(myDecl);

      localElementDeclsByRef.put(e.getKey(), myDecl);

      if (topLevelElementXML.contains(el)) {
        QName name = myDecl.getQName();
        assert (name.equals(e.getKey()));
        // if (!localToplevelElements.get(e.getKey()).isEmpty()) {
        // System.err.println("Fims ! Samme toplevelnavn fundet 2*!");
        // }
        localToplevelElements.put(e.getKey(), myDecl);
      }
    }

    for (Map.Entry<QName, Element> e : allAttributesXMLByRef.entrySet()) {
      Element el = e.getValue();
      XSDAttributeDecl myDecl = mkAttributeDecl(el, schema);
      schema.addAttributeDecl(myDecl);
      localAttributeDeclsByRef.put(e.getKey(), myDecl);
    }
  }

  @Override
void cookCM(XSDSchema schema)
      throws XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException {
    for (XSDType t : localTypedefsByRef.values()) {
      ((XSDAbstractType) t).cookCM(schema);
    }
  }

  public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    Element me = fac.createElement("schema-document");
    parent.add(me);
    me.addAttribute("targetNamespace", targetNamespace.getURI());

    /*
     * Element doc = fac.createElement("det-afpillede-skrog"); me.add(doc);
     * doc.add((Element) document.getRootElement().clone());
     * 
     * Dom4jUtil.collectionDiagnostics(me, typeDefs, "typeDefs", fac);
     * Dom4jUtil.collectionDiagnostics(me, topLevelElementXML,
     * "topLevelElementXML", fac); Dom4jUtil.collectionDiagnostics(me,
     * allElementsXMLByRef, "allElementDecls", fac);
     * Dom4jUtil.collectionDiagnostics(me, allAttributesXMLByRef,
     * 
     * "topLevelAttributeDecls", fac); Dom4jUtil.collectionDiagnostics(me,
     * attributeGroupDefs, "attributeGroupDefs", fac);
     * Dom4jUtil.collectionDiagnostics(me, modelGroupDefs, "modelGroupDefs",
     * fac);
     */
  }

  @Override
public String toString() {
    return "<schemaDocument systemId=\"" + systemId + "\" numElementXML=\""
        + allElementsXMLByRef.size() + "\" numAttributeXML=\""
        + allAttributesXMLByRef.size() + "\" numTypeDefsXML=\""
        + allTypedefXMLByRef.size() + "\" targetNamespaceURI=\""
        + targetNamespace.getURI() + "\"/>";
  }
}
