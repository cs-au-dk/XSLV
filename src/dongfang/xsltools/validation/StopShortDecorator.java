package dongfang.xsltools.validation;

public class StopShortDecorator extends ValidationRunDecoratorBase {
  int lastStep;

  public StopShortDecorator(ValidationRun decorated, int lastStep) {
    super(decorated);
    this.lastStep = lastStep;
  }

  @Override
public boolean relaxateProgress(short progress) {
    decorated.relaxateProgress(progress);
    return getProgress() < lastStep;
  }
}
