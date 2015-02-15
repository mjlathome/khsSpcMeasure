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
public enum CollectStatus {	
	OPEN("O"), CLOSED("C"), HISTORY("H");
	
	// value to be stored
	private final String value;

	// declare value lookup map 
	private static final Map<String, CollectStatus> map = new HashMap<String, CollectStatus>();
	
	// populate the value lookup map
	static {
		for (CollectStatus collStat : CollectStatus.values()) {
			map.put(collStat.value, collStat);
		}
	}
	
	// constructor
	private CollectStatus(String value) {
		this.value = value;
	}
	
	// convert key into enumurated type
	public static CollectStatus fromValue(String key) {
		CollectStatus collStat = null;
				
		if (map.containsKey(key)) {
			return map.get(key);
		}
		
		return collStat;
	}

	// extracts the enumerators value 
	public String getValue() {		
		return this.value;
	}			
}
