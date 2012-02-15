package com.dtdsoftware.splunk.formatter;

/**
 * <pre>
 * Utility methods for formatting
 * </pre>
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public abstract class FormatterUtils {

	/**
	 * Strip all whitespace chars and replace with a single space char
	 * 
	 * @param input
	 * @return
	 */
	public static String stripNewlines(String input) {

		if (input == null) {
			return "";
		}
		char[] chars = input.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (Character.isWhitespace(chars[i])) {
				chars[i] = ' ';
			}
		}

		return new String(chars);
	}

	/**
	 * Remove surrounding quotes from a string
	 * 
	 * @param quotedString
	 * @return
	 */
	public static String trimQuotes(String quotedString) {

		if (quotedString.startsWith("\"")) {
			quotedString = quotedString.substring(1);
		}
		if (quotedString.endsWith("\"")) {
			quotedString = quotedString.substring(0, quotedString.length() - 1);
		}

		return quotedString;
	}

}
