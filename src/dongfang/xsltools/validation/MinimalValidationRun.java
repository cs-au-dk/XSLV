package dongfang.xsltools.validation;


public class MinimalValidationRun extends NullResultListener implements ValidationRun {
  short progress;

  ValidationResult result;

  public boolean relaxateProgress(short progress) {
    this.progress = (short) Math.max(progress, this.progress);
    return true;
  }

  public short getProgress() {
    return progress;
  }

  public ValidationResult getValidationResult() {
    return result;
  }

  @Override
  public void setValidationResult(ValidationResult result) {
    this.result = result;
  }
}
