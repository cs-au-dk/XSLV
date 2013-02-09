/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.simplification;

import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetLevel;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.util.UniqueNameGenerator;

/**
 * Put unique df:cid on each element. Supposed to be run on core XSLT documents
 * only (possibly before approximative simplification)
 * 
 * @author dongfang
 */
public class IdToElementMapUpdater implements StylesheetProcessor {

  static IdToElementMapUpdater getInstance(ErrorReporter cesspool,
      UniqueNameGenerator names) {
    return new IdToElementMapUpdater();
  }

  public IdToElementMapUpdater() {
  }

  void update(final StylesheetModule module) {
    module.rebuildIdToElementMaps();
  }

  public void process(StylesheetLevel group) {
    for (StylesheetModule module : group.contents()) {
      update(module);
    }
    for (StylesheetLevel imported : group.imports()) {
      process(imported);
    }
  }

  public void process(Stylesheet stylesheet) {
    process(stylesheet.getPrincipalLevel());
    update(stylesheet);
  }
}
