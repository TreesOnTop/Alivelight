package net.blixate.config.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import net.blixate.config.ConfigArray;
import net.blixate.config.ConfigFile;
import net.blixate.config.ConfigNull;
import net.blixate.config.ConfigProperty;
import net.blixate.config.ConfigSection;

public class ConfigWriter {
  ArrayList<ConfigSection> sections;
  
  ConfigFile file;
  
  public static ConfigWriter getWriter(ConfigFile file) {
    ConfigWriter writer = new ConfigWriter(file);
    writer.load();
    return writer;
  }
  
  private ConfigWriter(ConfigFile file) {
    this.file = file;
    this.sections = new ArrayList<>();
  }
  
  public void save(OutputStream stream) throws IOException {
    for (ConfigSection section : this.sections) {
      if ((section.getProperties()).length == 0)
        continue; 
      if (!section.getName().equals("Global"))
        stream.write(writeSection(section).getBytes()); 
      byte b;
      int i;
      ConfigProperty[] arrayOfConfigProperty;
      for (i = (arrayOfConfigProperty = section.getProperties()).length, b = 0; b < i; ) {
        ConfigProperty prop = arrayOfConfigProperty[b];
        stream.write(writeProperty(prop).getBytes());
        b++;
      } 
    } 
    this.file.read();
  }
  
  private void load() {
    byte b;
    int i;
    ConfigSection[] arrayOfConfigSection;
    for (i = (arrayOfConfigSection = this.file.getSections()).length, b = 0; b < i; ) {
      ConfigSection section = arrayOfConfigSection[b];
      this.sections.add(section);
      b++;
    } 
  }
  
  public String writeSection(ConfigSection section) {
    return "[" + section.getName() + "]\n";
  }
  
  public String writeProperty(ConfigProperty prop) {
    return String.valueOf(prop.getName()) + " = " + prop.value() + "\n";
  }
  
  public <T> void set(String section, String propertyName, Object[] value) {
    if (value == null)
      set(section, (ConfigProperty)new ConfigNull(propertyName)); 
    set(section, ConfigArray.toConfigArray(propertyName, value));
  }
  
  public void set(String section, String propertyName, boolean value) {
    set(section, new ConfigProperty(propertyName, ""+value));
  }
  
  public void set(String section, String propertyName, float value) {
    set(section, new ConfigProperty(propertyName, ""+value));
  }
  
  public void set(String section, String propertyName, double value) {
    set(section, new ConfigProperty(propertyName, ""+value));
  }
  
  public void set(String section, String propertyName, long value) {
    set(section, new ConfigProperty(propertyName, ""+value));
  }
  
  public void set(String section, String propertyName, byte value) {
    set(section, new ConfigProperty(propertyName, ""+value));
  }
  
  public void set(String section, String propertyName, short value) {
    set(section, new ConfigProperty(propertyName, ""+value));
  }
  
  public void set(String section, String propertyName, char value) {
    set(section, new ConfigProperty(propertyName, "\"" + value + "\""));
  }
  
  public void set(String section, String propertyName, String value) {
    set(section, new ConfigProperty(propertyName, "\"" + value + "\""));
  }
  
  public void set(String section, String propertyName, ConfigSerializable value) {
    set(section, new ConfigProperty(propertyName, value.value()));
  }
  
  private void set(String section, ConfigProperty prop) {
    ConfigSection sec = getSection(section);
    if (sec == null) {
      sec = new ConfigSection(section);
      this.sections.add(sec);
    } 
    sec.addProperty(prop);
  }
  
  private ConfigSection getSection(String name) {
    for (int i = 0; i < this.sections.size(); i++) {
      if (((ConfigSection)this.sections.get(i)).getName().equals(name))
        return this.sections.get(i); 
    } 
    return null;
  }
  
  public OutputStream getFileStream() throws IOException {
    return Files.newOutputStream(this.file.getFile().toPath(), new java.nio.file.OpenOption[0]);
  }
}
