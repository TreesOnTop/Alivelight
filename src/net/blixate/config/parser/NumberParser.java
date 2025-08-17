package net.blixate.config.parser;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberParser {
  public static final String[] prefixes = new String[] { "0x", "0b", "0o" };
  
  public static final int[] prefixRadix = new int[] { 16, 2, 8 };
  
  public static String getParsable(String value) {
    int offset = (value.charAt(0) == '-') ? 1 : 0;
    boolean isNegative = (offset == 1);
    if (value.length() > offset + 1 && 
      isPrefix(value.substring(offset, offset + 2)))
      offset += 2; 
    String pValue = value.substring(offset);
    return String.valueOf(isNegative ? "-" : "") + pValue;
  }
  
  public static int getRadix(String value) {
    int offset = (value.charAt(0) == '-') ? 1 : 0;
    if (value.length() > offset + 1) {
      int pIdx = prefixIdx(value.substring(offset, offset + 2));
      if (pIdx >= 0)
        return prefixRadix[pIdx]; 
    } 
    return 10;
  }
  
  private static boolean isPrefix(String prefix) {
    return (prefixIdx(prefix) != -1);
  }
  
  private static int prefixIdx(String prefix) {
    for (int i = 0; i < prefixes.length; i++) {
      if (prefix.equals(prefixes[i]))
        return i; 
    } 
    return -1;
  }
  
  public static int parseInt(String value) {
    if (value == null)
      return -1; 
    int radix = getRadix(value);
    String parseString = getParsable(value);
    return Integer.parseInt(parseString, radix);
  }
  
  public BigInteger parseBigInt(String value) {
    return new BigInteger(value);
  }
  
  public BigDecimal parseBigNumber(String value) {
    return new BigDecimal(value);
  }
}
