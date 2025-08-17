package net.blixate.config;

import java.util.HashMap;

public class ConfigSection {
  String name;
  
  HashMap<String, ConfigProperty> properties;
  
  public ConfigSection(String name) {
    this.name = name;
    this.properties = new HashMap<>();
  }
  
  public void addProperty(String name, ConfigProperty value) {
    this.properties.put(name, value);
  }
  
  public void addProperty(ConfigProperty value) {
    this.properties.put(value.getName(), value);
  }
  
  public boolean hasProperty(String name) {
    return this.properties.containsKey(name);
  }
  
  public ConfigProperty getProperty(String name) {
    return hasProperty(name) ? this.properties.get(name) : new ConfigNull(name);
  }
  
  public ConfigProperty[] getProperties() {
    return (ConfigProperty[])this.properties.values().toArray((Object[])new ConfigProperty[0]);
  }
  
  public String getName() {
    return this.name;
  }
  
  public String toString() {
    return "[" + this.name + ", propertyCount=" + this.properties.size() + "]";
  }
  
  public int getInt(String property) {
    return getProperty(property).getAsInt();
  }
  
  public float getFloat(String property) {
    return getProperty(property).getAsFloat();
  }
  
  public String getString(String property) {
    return getProperty(property).getAsString();
  }
  
  public double getDouble(String property) {
    return getProperty(property).getAsDouble();
  }
  
  public long getLong(String property) {
    return getProperty(property).getAsLong();
  }
  
  public ConfigArray getArray(String property) {
    return getProperty(property).asArray();
  }
  
  public boolean getBoolean(String property) {
    return getProperty(property).getAsBoolean();
  }
}
