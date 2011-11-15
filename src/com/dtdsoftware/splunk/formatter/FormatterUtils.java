package com.dtdsoftware.splunk.formatter;

public abstract class FormatterUtils {

	
public static String stripNewlines(String input){
		
		if (input == null){
			return "";
		}
		char [] chars = input.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if(Character.isWhitespace(chars[i])){
				chars[i]=' ';
			}
		}
		
		return new String(chars);
	}

public static String trimQuotes(String quotedString) {
	
	if(quotedString.startsWith("\"")){
		quotedString=quotedString.substring(1);
	}
	if(quotedString.endsWith("\"")){
		quotedString=quotedString.substring(0, quotedString.length()-1);
	}
	
	
	return quotedString;
}

}
