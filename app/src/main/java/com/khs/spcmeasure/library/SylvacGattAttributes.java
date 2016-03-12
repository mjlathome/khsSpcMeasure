package com.khs.spcmeasure.library;

import java.util.HashMap;

/**
 * defines Sylvac Bluetooth Low Energy constants i.e. device name and GATT attributes
 */
public class SylvacGattAttributes {
	private static HashMap<String, String> attributes = new HashMap<String, String>();
	
	public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
	
	// Sylvac Ble UUID constants
	public static String SYLVAC_METROLOGY_SERVICE = "c1b25000-caaf-6d0e-4c33-7dae30052840";
	public static String DATA_RECEIVED_FROM_INSTRUMENT = "c1b25010-caaf-6d0e-4c33-7dae30052840";
	public static String DATA_REQUEST_OR_CMD_TO_INSTRUMENT = "c1b25012-caaf-6d0e-4c33-7dae30052840";
	public static String ANSWER_TO_REQUEST_OR_CMD_FROM_INSTRUMENT = "c1b25013-caaf-6d0e-4c33-7dae30052840";

	// build attribute lookup map
	static {
		// add Sylvac Services
		attributes.put(SYLVAC_METROLOGY_SERVICE, "Sylvac Service");

		// add Sylvac Characteristics
		attributes.put(DATA_RECEIVED_FROM_INSTRUMENT, "Data Received from Instrument");
		attributes.put(DATA_REQUEST_OR_CMD_TO_INSTRUMENT, "Data Request or Cmd to Instrument");
		attributes.put(ANSWER_TO_REQUEST_OR_CMD_FROM_INSTRUMENT, "Answer to Request or Cmd from Instrument");
	}

	// lookup - returns attrubute name if uuid found else defaultName
	public static String lookup(String uuid, String defaultName) {
      String name = attributes.get(uuid);
      return name == null ? defaultName : name;
	}
}
