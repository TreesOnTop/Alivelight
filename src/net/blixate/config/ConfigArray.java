package net.blixate.config;

import java.util.ArrayList;
import net.blixate.config.util.Converter;
import net.blixate.config.writer.ConfigSerializable;

public class ConfigArray extends ConfigProperty implements ConfigSerializable {
  ConfigProperty[] value;
  
  public ConfigArray(String name, ConfigProperty[] value) {
    super(name, "");
    this.value = value;
  }
  
  public void add(ConfigProperty value) {
    if (this.value == null)
      this.value = new ConfigProperty[0]; 
    ConfigProperty[] name = new ConfigProperty[this.value.length + 1];
    System.arraycopy(this.value, 0, name, 0, this.value.length);
    name[this.value.length] = value;
    this.value = name;
  }
  
  public ConfigProperty get(int index) {
    if (this.value == null)
      throw new RuntimeException("Array not initalized!"); 
    if (index >= this.value.length)
      throw new ArrayIndexOutOfBoundsException("Array does not have property at index " + index); 
    return this.value[index];
  }
  
  public int size() {
    return this.value.length;
  }
  
  public ConfigProperty[] values() {
    return this.value;
  }
  
  public String toString() {
    if (this.value == null || this.value.length == 0)
      return String.valueOf(this.name) + "={}"; 
    StringBuilder sb = new StringBuilder("{");
    byte b;
    int i;
    ConfigProperty[] arrayOfConfigProperty;
    for (i = (arrayOfConfigProperty = this.value).length, b = 0; b < i; ) {
      ConfigProperty val = arrayOfConfigProperty[b];
      sb.append(val.value);
      sb.append(", ");
      b++;
    } 
    return String.valueOf(this.name) + "=" + sb.toString().substring(0, sb.length() - 2) + "}";
  }
  
  public String value() {
    String valueString = "{";
    byte b;
    int i;
    ConfigProperty[] arrayOfConfigProperty;
    for (i = (arrayOfConfigProperty = this.value).length, b = 0; b < i; ) {
      ConfigProperty v = arrayOfConfigProperty[b];
      if (v == null) {
        valueString = String.valueOf(valueString) + "null,";
      } else if (v instanceof ConfigSerializable) {
        valueString = String.valueOf(valueString) + v.value();
      } else {
        valueString = String.valueOf(valueString) + v;
      } 
      valueString = String.valueOf(valueString) + ", ";
      b++;
    } 
    valueString = String.valueOf((valueString.length() > 2) ? valueString.substring(0, valueString.length() - 2) : "{") + "}";
    return valueString;
  }
  
  public boolean isArray() {
    return true;
  }
  
  public String[] toStringArray() {
    return toArray(new String[0], new Converter<ConfigProperty, String>() {
          public String convert(ConfigProperty input) {
            return input.getAsString();
          }
        });
  }
  
  public Integer[] toIntegerArray() {
    return toArray(new Integer[0], new Converter<ConfigProperty, Integer>() {
          public Integer convert(ConfigProperty input) {
            return Integer.valueOf(input.getAsInt());
          }
        });
  }
  
  public Float[] toFloatArray() {
    return toArray(new Float[0], new Converter<ConfigProperty, Float>() {
          public Float convert(ConfigProperty input) {
            return Float.valueOf(input.getAsFloat());
          }
        });
  }
  
  public <T> T[] toArray(Object[] array, Converter<ConfigProperty, T> converter) {
    ArrayList<T> obj = new ArrayList<>();
    if (isNull())
      return (T[])array; 
    byte b;
    int i;
    ConfigProperty[] arrayOfConfigProperty;
    for (i = (arrayOfConfigProperty = this.value).length, b = 0; b < i; ) {
      ConfigProperty prop = arrayOfConfigProperty[b];
      obj.add((T)converter.convert(prop));
      b++;
    } 
    return obj.toArray((T[])array);
  }
  
  public static <T> ConfigProperty toConfigArray(String name, Object[] objs) {
    ArrayList<ConfigProperty> prop = new ArrayList<>();
    byte b;
    int i;
    Object[] arrayOfObject;
    for (i = (arrayOfObject = objs).length, b = 0; b < i; ) {
      T obj = (T)arrayOfObject[b];
      if (obj instanceof ConfigSerializable) {
        prop.add(new ConfigProperty(((ConfigSerializable)obj).value()));
      } else {
        prop.add(new ConfigProperty(String.valueOf(obj)));
      } 
      b++;
    } 
    return new ConfigArray(name, prop.<ConfigProperty>toArray(new ConfigProperty[0]));
  }
}
