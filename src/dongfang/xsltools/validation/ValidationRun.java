package dongfang.xsltools.validation;


/**
 * A callback class for validation runs: Implementations can decide what they want to do 
 * with the various temporary data structures, reports etc. that validation generates.
 * 
 * Implementations can also decide to abort the process (the relaxateProgress method).
 * @author dongfang
 */
public interface ValidationRun extends ResultListener {

  short STYLESHEET_LOADED = 1;
  short XCFG_CONSTRUCTED = 2;
  short SG_CONSTRUCTED = 3;
  short VALIDATED = 4;

  /**
   * Return false here to abort.
   * @param progress
   * @return
   */
  boolean relaxateProgress(short progress);

  short getProgress();

  ValidationResult getValidationResult();
}