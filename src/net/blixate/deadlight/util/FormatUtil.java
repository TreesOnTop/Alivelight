package net.blixate.deadlight.util;

import java.math.BigInteger;
import java.util.Map.Entry;

import org.bukkit.ChatColor;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;

public class FormatUtil {
	
	private static final NavigableMap<Long, String> suffixes = new TreeMap<> ();
	
	static {
		suffixes.put(1_000L, "k");
		suffixes.put(1_000_000L, "M");
		suffixes.put(1_000_000_000L, "B");
		suffixes.put(1_000_000_000_000L, "T");
		suffixes.put(1_000_000_000_000_000L, "Q");
		suffixes.put(1_000_000_000_000_000_000L, "Qi");
	}

	public static String formatLong(long i) {
		if(i < 1000) {
			return String.valueOf(i); // It won't be changed anyway!
		}
		String number = "" + i;
		int idx = number.length();
		// create reversed character array
		char[] chars = reverseString(number);
		// insert commas
		String s = "";
		idx = 0;
		for(char c : chars) {
			s += c;
			idx++;
			if(idx >= 3) {
				s += ",";
				idx = 0;
			}
		}
		if(s.endsWith(",")) {
			s = s.substring(0, s.length()-1);
		}
		// re-reverse the string
		return String.valueOf(reverseString(s));
	}
	
	public static String formatCompact(BigInteger value) {
		/*if (value.compareTo(BigInteger.ZERO) < 0) return "-" + formatCompact(value.negate());
		if (value.compareTo(BigInteger.valueOf(1000)) < 0) return value.toString(); //deal with easy case

		Entry<Long, String> e = suffixes.floorEntry(value.longValue());
		Long divideBy = e.getKey();
		String suffix = e.getValue();

		BigInteger truncated = value.divide(BigInteger.valueOf(divideBy / 10)); //the number part of the output times 10
		boolean hasDecimal = truncated.compareTo(BigInteger.valueOf(100)) < 0 && (truncated.divide(BigInteger.valueOf(10))) != (truncated.divide(BigInteger.valueOf(10)));
		return hasDecimal ? truncated.divide(BigInteger.valueOf(10))+ suffix : truncated.divide(BigInteger.valueOf(10)) + suffix;
	*/
		return formatCompact(value.longValue());
	}
	
	public static String formatCompact(long value) {
		//Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
		if (value == Long.MIN_VALUE) return formatCompact(Long.MIN_VALUE + 1);
		if (value < 0) return "-" + formatCompact(-value);
		if (value < 1000) return Long.toString(value); //deal with easy case

		Entry<Long, String> e = suffixes.floorEntry(value);
		Long divideBy = e.getKey();
		String suffix = e.getValue();

		long truncated = value / (divideBy / 10); //the number part of the output times 10
		boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
		return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
	}
	
	public static String formatCurrency(BigInteger i) {
		if(i.longValue() < 1000) {
			return i.toString();
		}
		String number = i.toString();
		int idx = number.length();
		// create reversed character array
		char[] chars = reverseString(number);
		// insert commas
		String s = "";
		idx = 0;
		for(char c : chars) {
			s += c;
			idx++;
			if(idx >= 3) {
				s += ",";
				idx = 0;
			}
		}
		if(s.endsWith(",")) {
			s = s.substring(0, s.length()-1);
		}
		// reverse string
		return String.valueOf(reverseString(s));
	}
	
	public static String formatString(String input, String[] replace) {
		String ret = input;
		for(int i = 0; i < replace.length; i++) {
			ret = ret.replace("$" + (i + 1), replace[i]);
		}
		return ret;
	}
	
	public static String formatPercent(double input, boolean floor) {
		if(floor) {
			input = Math.floor(input);
		}else {
			input = (int)(input/100d) * 100d;
		}
		return input + "%";
	}
	
	private static char[] reverseString(String s) {
		int idx = s.length() - 1;
		// create reversed character array
		char[] chars = new char[s.length()];
		for(char c : s.toCharArray()) {
			chars[idx--] = c;
		}
		return chars;
	}
	
	public static UUID getUUIDFromString(String uuidWithoutHyphens) {
		return UUID.fromString(uuidWithoutHyphens
				.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
	}
	
	public static String color(String s) {
		if(s == null) {
			return "null";
		}
		return ChatColor.translateAlternateColorCodes('&', s);
	}
	
	public static String colorOnly(String string) {
		return color(string.replaceAll("&[klmno]", ""));
	}
}
