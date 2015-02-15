/**
 * 
 */
package com.khs.spcmeasure;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Mark
 *
 */
public enum ConnectionState {
	DISCONNECTED("Disconnected"), CONNECTING("Connecting"), CONNECTED("Connected");
	
	// value to be stored
	private final String value;

	// declare value lookup map 
	private static final Map<String, ConnectionState> map = new HashMap<String, ConnectionState>();
	
	// populate the value lookup map
	static {
		for (ConnectionState connState : ConnectionState.values()) {
			map.put(connState.value, connState);
		}
	}
	
	// constructor
	private ConnectionState(String value) {
		this.value = value;
	}
	
	// converts case insensitive value to the enumeration
	public static ConnectionState fromValue(String val) {
		ConnectionState connState = null;
		String key = val.toUpperCase(Locale.ENGLISH);
				
		if (map.containsKey(key)) {
			return map.get(key);
		}
		
		return connState;
	}

	// extracts the enumerators value 
	public String getValue() {		
		return this.value;
	}	
}
