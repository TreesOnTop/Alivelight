package net.blixate.config.error;

public class ConfigParsingException extends RuntimeException {
  private static final long serialVersionUID = -8145353177611783807L;
  
  public ConfigParsingException(String string) {
    super(string);
  }
}
