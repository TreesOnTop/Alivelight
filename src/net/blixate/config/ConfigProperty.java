package net.blixate.config;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import net.blixate.config.error.ConfigTypeException;
import net.blixate.config.parser.NumberParser;
import net.blixate.config.parser.ParseUtils;
import net.blixate.config.parser.StringHelper;
import net.blixate.config.writer.ConfigSerializable;

public class ConfigProperty implements ConfigSerializable {
  String name;
  
  String value;
  
  public ConfigProperty(String name, String value) {
    this.name = name;
    this.value = value;
  }
  
  public ConfigProperty(Object value) {
    this.value = value.toString();
  }
  
  public ConfigProperty(ConfigSerializable value) {
    this.value = value.value();
  }
  
  public String getName() {
    return this.name;
  }
  
  public String toString() {
    return String.valueOf(this.name) + "=" + this.value;
  }
  
  public int getAsInt() {
    try {
      return NumberParser.parseInt(this.value);
    } catch (NumberFormatException e) {
      e.printStackTrace();
      throw new ConfigTypeException("Invalid integer type '" + this.value + "'");
    } 
  }
  
  public BigInteger getAsBigInteger() {
    return new BigInteger(this.value);
  }
  
  public float getAsFloat() {
    try {
      return (float)getAsDouble();
    } catch (NumberFormatException e) {
      e.printStackTrace();
      throw new ConfigTypeException("Invalid float type '" + this.value + "'");
    } 
  }
  
  public long getAsLong() {
    try {
      return Long.parseLong(this.value);
    } catch (NumberFormatException e) {
      throw new ConfigTypeException("Invalid long type '" + this.value + "'");
    } 
  }
  
  public char getAsChar() {
    if (isNumber())
      return (char)getAsInt(); 
    return getAsString().charAt(0);
  }
  
  public double getAsDouble() {
    try {
      return (new BigDecimal(this.value)).doubleValue();
    } catch (NumberFormatException e) {
      throw new ConfigTypeException("Invalid double type '" + this.value + "'");
    } 
  }
  
  public char[] getAsCharArray() {
    if (isArray()) {
      ArrayList<Character> c = new ArrayList<>();
      byte b;
      int j;
      ConfigProperty[] arrayOfConfigProperty;
      for (j = (arrayOfConfigProperty = asArray().values()).length, b = 0; b < j; ) {
        ConfigProperty prop = arrayOfConfigProperty[b];
        c.add(Character.valueOf(prop.getAsChar()));
        b++;
      } 
      char[] chars = new char[c.size()];
      int i = 0;
      for (Character ch : c) {
        chars[i] = ch.charValue();
        i++;
      } 
      return chars;
    } 
    return getAsString().toCharArray();
  }
  
  public String getAsString() {
    if (isNull())
      return null; 
    if (isString())
      return StringHelper.escape(this.value.substring(1, this.value.length() - 1)); 
    throw new ConfigTypeException("Invalid string type '" + this.value + "'");
  }
  
  public boolean isString() {
    return ParseUtils.matches("STRING", this.value);
  }
  
  public boolean isNumber() {
    return ParseUtils.matches("NUMBER", this.value);
  }
  
  public boolean isNull() {
    return !(!ParseUtils.matches("NULL", this.value) && this.value != null);
  }
  
  public boolean isArray() {
    return this instanceof ConfigArray;
  }
  
  public ConfigArray asArray() {
    if (isNull())
      return null; 
    return (ConfigArray)this;
  }
  
  public boolean getAsBoolean() {
    if (this.value.equalsIgnoreCase("true") || this.value.equalsIgnoreCase("false"))
      return this.value.equalsIgnoreCase("true"); 
    throw new ConfigTypeException("Invalid boolean type '" + this.value + "'");
  }
  
  public String value() {
    if (isString())
      return "\"" + getAsString() + "\""; 
    if (isArray())
      return asArray().value(); 
    return this.value;
  }
}
