/*
 * Created on 2005-03-12
 */
package dongfang.xsltools.simplification;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dongfang.xsltools.exceptions.XSLToolsException;

/**
 * A resolver that is stuck between a resolver knowing about things bound
 * outside a for-each instructions, and resolvers inside the for-each
 * instruction (a Resolver represents a scope, nested scopes are represented by
 * lists of Resolvers). The effect is to block resolution of variable references
 * inside the for-each, and recording that a resolution was attempted. The
 * recorded names of variables / parameters bound and referred across for-each
 * are used for making parameter forwarding lists when the for-each is desugared
 * into apply-templates and a set of new templates.
 * 
 * @author dongfang
 */
public class ForEachResolverShim extends ResolverBase {
  private Resolver parent;

  public ForEachResolverShim(Resolver parent) {
    this.parent = parent;
  }

  @Override
public void bind(QName name, Binding e) throws XSLToolsException {
    parent.bind(name, e);
  }

  @Override
public Resolver enterScope() {
    return new LexicalScopeResolver(this);
  }

  @Override
public void resolutionDiagnostics(Branch context, DocumentFactory fac) {
    Element e = fac.createElement("ForEachShim");
    context.add(e);
    parent.resolutionDiagnostics(e, fac);
  }

  @Override
public Binding resolve(QName name, short symbolSpace, Resolver base)
      throws XSLToolsException {
    // her skal, på resolve time, findes us af at der er
    // en binding som transgresser en for-each.
    // I så fald skal der returneres en eller anden
    // sjov værdi som indikerer at der ikke skal gøres
    // noget forsøg på at erstatte variabelreferencen
    // (og at den skal kyles på en parameterliste)
    // Måske er det smarteste at ALLE resolvere har en
    // metode til at returnere for-each transgressere:
    // ImportPrecedenceGroup returnerer en tom liste.
    // NoStackingHacking... forwarder til parent
    // Denne klasse returnerer sin liste.
    // HOOOOOOV det er ikke nødvendigt!!! Man kan bare:
    // FØR rekursion HVIS det er en for-each: Smid en shim over
    // resolveren, og husk på den i en kloak variabel.
    // EFTER rekursionen, FØR selve behandlingen af for-each:
    // Se hvad shim'en har fanget.
    // Hint: Måske er det smartere at ombygge ParVarResolutionSimplifier
    // til copying style (og fjerne par fra navnet), hvis den skal til
    // at bryde for-each op. FORRESTEN! Husk lige på om for-each bodyen
    // kommer igennen den rigtige tur??? Nestede for-each, uha uha...
    // Variable def i den yderste og brugt i den inderste skal
    // forwardes igennem den ene template der laves...
    // (det sker vist automagisk, efter denne konstrukt, da begge/alle
    // shims vil registrere den !
    // strengt taget gælder vel det hele også for attribute sets.
    // (pizz .. det gælder altså kun lokale bindinger!)
    // (eller..? Måske er det ok bare at shotgunne!)

    // right now, we will try just to record all transgressers,
    // even if they refer to something defined globally.
    // TODO: Stop this. Could be done by adding a whereResolved
    // method on resolvers that return GLOBAL or LOCAL, and only
    // do the forward thing if LOCAL.

    // if (isLocal .. blah blah) {
    if (symbolSpaces[symbolSpace] == null) {
      symbolSpaces[symbolSpace] = new HashMap();
    }

    Binding probe = parent.resolve(name, symbolSpace, this);

    if (probe == null)
      return null;

    if (probe.getBindingScope() == TOPLEVEL_SCOPE)
      return probe;

    // A hack: We can use resolveLocalScope to get the original,
    // forbidden trans-for-each bindings.
    symbolSpaces[symbolSpace].put(name, probe);
    // }
    // return special unresolvable value:
    // return parent.resolve(name, scope);
    if (symbolSpace == PARAMETER_AND_VARIABLE_SYMBOLSPACE)
      return Binding.SHOULD_NOT_RESOLVE;
    else if (symbolSpace == ATTRIBUTE_SET_SYMBOLSPACE)
      return Binding.SHOULD_NOT_RESOLVE;
    return probe;
  }

  @Override
public Binding resolve(QName name, short symbolSpace)
      throws XSLToolsException {
    return resolve(name, symbolSpace, this);
  }

  /*
   * public Binding resolveLocalScope(QName name, short symbolSpace) { Binding
   * return symbolSpaces[symbolSpace].get(name); }
   */

  public short resolverScope() {
    return parent.resolverScope();
  }

  protected Set<QName> getUnresolvables(short symbolSpaceNumber) {
    Map symbolSpace = symbolSpaces[symbolSpaceNumber];
    if (symbolSpace != null)
      return symbolSpace.keySet();
    return Collections.emptySet();
  }

  Resolver getParent() {
    return parent;
  }

  @Override
protected Binding continueResolution(QName name, short scope, Resolver starter) {
    throw new AssertionError("continueResolution called on ForEachResolverShim");
  }
}
