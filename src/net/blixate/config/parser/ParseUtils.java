package net.blixate.config.parser;

public class ParseUtils {
  public static boolean matches(String tokenName, String test) {
    return (test != null && TokenType.valueOf(tokenName).match(test));
  }
}
