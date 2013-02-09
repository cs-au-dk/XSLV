package dongfang.xsltools.xmlclass.xslside;

import org.dom4j.QName;

/*
 * The function mu
 */
public interface CharNameResolver {
  char getCharForElementName(QName name);

  char getCharForAttributeName(QName name);
}
