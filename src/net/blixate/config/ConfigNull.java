package net.blixate.config;

public class ConfigNull extends ConfigProperty {
  public ConfigNull(String name) {
    super(name, null);
  }
  
  public boolean isNull() {
    return true;
  }
}
