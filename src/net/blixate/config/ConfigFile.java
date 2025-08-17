package net.blixate.config;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import net.blixate.config.parser.ConfigParser;
import net.blixate.config.writer.ConfigWriter;

public class ConfigFile {
  public static final String GLOBAL_SECTION = "Global";
  
  private File file;
  
  private ConfigSection[] sections;
  
  OutputStream dump = null;
  
  public ConfigFile(String fileName) {
    this(new File(fileName));
  }
  
  public ConfigFile(File input) {
    this.file = input;
  }
  
  public ConfigFile(File input, OutputStream dump) {
    this(input);
    this.dump = dump;
  }
  
  public ConfigProperty property(String section, String property) {
    return getSection(section).getProperty(property);
  }
  
  public ConfigProperty globalProperty(String property) {
    if (!hasSection("Global") || (getSection("Global").getProperties()).length == 0)
      return null; 
    return getSection("Global").getProperty(property);
  }
  
  public boolean hasGlobalProperty(String name) {
    return (globalProperty(name) != null);
  }
  
  public boolean hasSection(String name) {
    return (getSection(name) != null);
  }
  
  public ConfigSection getSection(String name) {
    byte b;
    int i;
    ConfigSection[] arrayOfConfigSection;
    for (i = (arrayOfConfigSection = this.sections).length, b = 0; b < i; ) {
      ConfigSection section = arrayOfConfigSection[b];
      if (section.getName().toUpperCase().equals(name.toUpperCase()))
        return section; 
      b++;
    } 
    return null;
  }
  
  public ConfigSection[] getSections() {
    return this.sections;
  }
  
  public static ConfigFile readFile(String name) {
    return readFile(name, null);
  }
  
  public static ConfigFile readFile(String name, OutputStream debugStream) {
    File f = new File(name);
    ConfigFile c = new ConfigFile(f, debugStream);
    try {
      c.read();
    } catch (IOException e) {
      return null;
    } 
    return c;
  }
  
  public ConfigFile read() throws IOException {
    String s = load();
    ConfigParser parser = new ConfigParser(this.dump);
    parser.lex(s);
    parser.parse();
    this.sections = parser.getSections();
    return this;
  }
  
  private String load() throws IOException {
    String content = "";
    for (String line : Files.readAllLines(this.file.toPath()))
      content = String.valueOf(content) + line + "\n"; 
    return content;
  }
  
  public File getFile() {
    return this.file;
  }
  
  public ConfigWriter getWriter() {
    return ConfigWriter.getWriter(this);
  }
}
