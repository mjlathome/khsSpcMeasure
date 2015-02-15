/**
 * 
 */
package com.khs.spcmeasure.library;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Mark
 *
 */
public enum LimitType {
	CONTROL("CL"), SIGNIFICANT_CONTROL("SCL"), ENGINEERING("ENG");
	
	// value to be stored
	private final String value;

	// declare value lookup map 
	private static final Map<String, LimitType> map = new HashMap<String, LimitType>();
	
	// populate the value lookup map
	static {
		for (LimitType limType : LimitType.values()) {
			map.put(limType.value, limType);
		}
	}
	
	// constructor
	private LimitType(String value) {
		this.value = value;
	}
	
	// converts case insensitive value to the enumeration
	public static LimitType fromValue(String val) {
		LimitType limType = null;
		String key = val.toUpperCase(Locale.ENGLISH);
				
		if (map.containsKey(key)) {
			return map.get(key);
		}
		
		return limType;
	}

	// extracts the enumerators value 
	public String getValue() {		
		return this.value;
	}	
}
