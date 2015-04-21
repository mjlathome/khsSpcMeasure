/**
 * 
 */
package com.khs.spcmeasure.library;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Mark
 *
 */
public class DateTimeUtils {
	public static String SQLITE_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static String CHART_DATETIME_FORMAT = "yy-MM-dd HH:mm";

    // convert Date to SQLite date time string
    public static String getDateTimeStr(Date date) {
    	SimpleDateFormat sdf = new SimpleDateFormat(SQLITE_DATETIME_FORMAT, Locale.getDefault());    	
    	return sdf.format(date);
    }

    // convert Date to date time string according to the format provided
    public static String getDateTimeStr(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(date);
    }


    // convert SQLite date time string to Date 
    public static Date getDate(String dateTimeStr) {
		SimpleDateFormat sdf = new SimpleDateFormat(SQLITE_DATETIME_FORMAT, Locale.getDefault());    	
		try {
			return sdf.parse(dateTimeStr);
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }    
}
