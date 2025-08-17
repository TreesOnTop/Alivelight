package net.blixate.config.parser;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.blixate.config.ConfigArray;
import net.blixate.config.ConfigProperty;
import net.blixate.config.ConfigSection;
import net.blixate.config.error.ConfigParsingException;

public class ConfigParser {
  static final Pattern PARENT_SEPERATE_PATTERN = Pattern.compile("\\[([a-zA-Z_]+[a-zA-Z0-9_]*).*?[:].*?([a-zA-Z_]+[a-zA-Z0-9_]*)\\]");
  
  static Pattern pattern;
  
  Token[] tokens;
  
  static {
    StringBuffer tokenPatternsBuffer = new StringBuffer();
    byte b;
    int i;
    TokenType[] arrayOfTokenType;
    for (i = (arrayOfTokenType = TokenType.values()).length, b = 0; b < i; ) {
      TokenType token = arrayOfTokenType[b];
      tokenPatternsBuffer.append("|(?<" + token.name() + ">" + token.regex() + ")");
      b++;
    } 
    String tokenPatternsString = tokenPatternsBuffer.toString().substring(1);
    pattern = Pattern.compile(tokenPatternsString);
  }
  
  OutputStream dump = null;
  
  ArrayList<ConfigSection> sections;
  
  public ConfigParser(OutputStream output) {
    this.dump = output;
    this.sections = new ArrayList<>();
  }
  
  public ConfigSection[] getSections() {
    return this.sections.<ConfigSection>toArray(new ConfigSection[0]);
  }
  
  public void lex(String content) {
    long start = System.currentTimeMillis();
    Matcher matcher = pattern.matcher(content);
    ArrayList<Token> tokens = new ArrayList<>();
    Token tok = null;
    while (matcher.find()) {
      String data = matcher.group();
      byte b;
      int i;
      TokenType[] arrayOfTokenType;
      for (i = (arrayOfTokenType = TokenType.values()).length, b = 0; b < i; ) {
        TokenType t = arrayOfTokenType[b];
        if (data.matches(t.regex())) {
          if (t.equals(TokenType.COMMENT))
            break; 
          tok = new Token(t, data, matcher.start());
          tokens.add(tok);
          debug(new String[] { "[Lexer] " + tok.toString() });
          break;
        } 
        b++;
      } 
    } 
    this.tokens = tokens.<Token>toArray(new Token[0]);
    long end = System.currentTimeMillis();
    debug(new String[] { "Lex Time: " + (end - start) + "ms" });
  }
  
  public void parse() {
    long start = System.currentTimeMillis();
    ConfigSection currentSection = new ConfigSection("Global");
    String property = null;
    Stack<Token> value = new Stack<>();
    boolean readValue = false;
    for (int i = 0; i < this.tokens.length + 1; i++) {
      Token token;
      if (i >= this.tokens.length) {
        token = new Token(TokenType.ENDL, ";", -1);
      } else {
        token = this.tokens[i];
      } 
      if (readValue) {
        if (isEndOfPropertyDefinition(token)) {
          ConfigProperty prop = null;
          if (!readValue) {
            if (value.isEmpty())
              prop = new ConfigProperty(property, "null"); 
          } else {
            if (isInvalidValue(value))
              configError("Invalid value while evaluating property \"" + property + "\"", token); 
            readValue = false;
            prop = evaluate(property, value);
            if (currentSection.hasProperty(prop.getName()))
              debug(new String[] { "[Parser] Property \"" + prop.getName() + "\" overwritten." }); 
          } 
          if (prop != null) {
            currentSection.addProperty(prop.getName(), prop);
            property = null;
            value.clear();
            i--;
            debug(new String[] { "[Parser] Property \"" + prop.getName() + "\" created." });
          } 
        } else {
          value.push(token);
        } 
      } else {
        switch (token.t) {
          case SECTION:
            if (property != null)
              configError("Invalid section start", token); 
            this.sections.add(currentSection);
            debug(new String[] { "[Parser] Section \"" + currentSection.getName() + "\" defined." });
            currentSection = new ConfigSection(token.data().substring(1, token.data().length() - 1));
            break;
          case PROPERTY:
            if (property != null)
              configError("Incorrect property definition", token); 
            property = token.data();
            break;
          case EQUALS:
            if (readValue)
              configError("'" + token.data() + "' is not a value.", token); 
            readValue = true;
            break;
		default:
			break;
        } 
      } 
    } 
    if (currentSection != null) {
      this.sections.add(currentSection);
      debug(new String[] { "[Parser] Section \"" + currentSection.getName() + "\" defined. (EOF-terminated)" });
    } 
    long end = System.currentTimeMillis();
    debug(new String[] { "Parse Time: " + (end - start) + "ms" });
  }
  
  private boolean isEndOfPropertyDefinition(Token token) {
    return !(token.t != TokenType.ENDL && token.t != TokenType.PROPERTY && token.t != TokenType.SECTION);
  }
  
  private ConfigProperty evaluate(String propName, Stack<Token> tokens) {
    if (tokens.size() < 1)
      return null; 
    if (((Token)tokens.firstElement()).t == TokenType.LBRACE)
      return (ConfigProperty)evaluateArray(propName, tokens); 
    return new ConfigProperty(propName, ((Token)tokens.peek()).data());
  }
  
  private boolean isInvalidValue(Stack<Token> value) {
    switch (value.peek().t) {
      case COMMENT:
      case NULL:
      case STRING:
      case LBRACE:
      case RBRACE:
      case NUMBER:
        return false;
	default:
		break;
    } 
    return true;
  }
  
  private ConfigArray evaluateArray(String propName, Stack<Token> tokens) {
    int nestingLevel = 0;
    Stack<Token> elements = new Stack<>();
    ConfigArray array = new ConfigArray(propName, null);
    for (Token t : tokens) {
      switch (t.t) {
        case LBRACE:
          nestingLevel++;
          if (nestingLevel > 1)
            elements.push(t); 
          continue;
        case RBRACE:
          nestingLevel--;
          if (nestingLevel != 0)
            elements.push(t); 
          if (!elements.isEmpty()) {
            array.add(evaluate(propName, elements));
            elements.clear();
          } 
          continue;
        case COMMA:
          if (nestingLevel == 1) {
            if (!elements.isEmpty())
              array.add(evaluate(propName, elements)); 
            elements.clear();
            continue;
          } 
          break;
	default:
		break;
      } 
      elements.push(t);
    } 
    return array;
  }
  
  private void configError(String s, Token t) {
    if (this.dump != null) {
      debug(new String[] { "[Configuration Error] " + s + " (At index " + t.index() + ")" });
    } else {
      throw new ConfigParsingException(String.valueOf(s) + " (At index " + t.index() + ")");
    } 
  }
  
  private void debug(String... strings) {
    if (this.dump == null)
      return; 
    String line = String.valueOf(String.join("", (CharSequence[])strings)) + "\n";
    try {
      this.dump.write(line.getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
}
