package net.blixate.config.parser;

import java.util.regex.Pattern;

enum TokenType {
  COMMENT("\\#.*"),
  NULL("null"),
  BOOLEAN("(true|false)"),
  SECTION("\\[[a-zA-Z_]+[a-zA-Z0-9_\\.]*\\]"),
  PROPERTY("[a-zA-Z_][a-zA-Z0-9_\\.]*"),
  STRING(new String[] { "\"(\\.|[^\"])*\"", "'(\\.|[^'])*'" }),
  LBRACE("\\{"),
  RBRACE("\\}"),
  COMMA(","),
  EQUALS("[=:]"),
  NUMBER(new String[] { "[+-]?0x[0-9A-Fa-f]+", "[+-]?0b[01]", "[+-]?0o[0-7]", "[+-]?[0-9]*[.]?[0-9]+" }),
  LPAREN("\\("),
  RPAREN("\\)"),
  ENDL(";");
  
  private Pattern regex;
  
  TokenType(String regex) {
    this.regex = Pattern.compile(regex);
  }
  
  TokenType(String... regexes) {
    String[] s = new String[regexes.length];
    for (int i = 0; i < regexes.length; ) {
      s[i] = "(" + regexes[i] + ")";
      i++;
    } 
    this.regex = Pattern.compile(String.join("|", (CharSequence[])s));
  }
  
  public String regex() {
    return this.regex.pattern();
  }
  
  public boolean match(String test) {
    return this.regex.matcher(test).matches();
  }
}
