package in.co.rays.proj4.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DataUtility provides common helper methods for converting and parsing
 * primitive types and date/time related utilities used across the application.
 *
 * <p>
 * This class contains methods to:
 * <ul>
 *   <li>convert strings to primitives (int, long)</li>
 *   <li>convert strings to {@link Date} and {@link Timestamp}</li>
 *   <li>format Date / Timestamp values</li>
 *   <li>provide the current timestamp</li>
 * </ul>
 * </p>
 *
 * @author Chaitanya Bhatt
 * @version 1.0
 */
public class DataUtility {

    /** Application date format used for parsing and formatting dates. */
    public static final String APP_DATE_FORMAT = "dd-MM-yyyy";

    /** Application time format used for parsing and formatting timestamps. */
    public static final String APP_TIME_FORMAT = "dd-MM-yyyy HH:mm:ss";

    private static final SimpleDateFormat formatter = new SimpleDateFormat(APP_DATE_FORMAT);

    private static final SimpleDateFormat timeFormatter = new SimpleDateFormat(APP_TIME_FORMAT);

    /**
     * Returns trimmed version of the input string when it is non-null and not empty.
     *
     * @param val input string
     * @return trimmed string if input is not null, otherwise null
     */
    public static String getString(String val) {
        if (DataValidator.isNotNull(val)) {
            return val.trim();
        } else {
            return val;
        }
    }

    /**
     * Converts an object to its string representation.
     *
     * @param val input object
     * @return {@code val.toString()} when val is non-null, otherwise empty string
     */
    public static String getStringData(Object val) {
        if (val != null) {
            return val.toString();
        } else {
            return "";
        }
    }

    /**
     * Converts a numeric string to int.
     *
     * @param val numeric string
     * @return parsed int if valid integer, otherwise 0
     */
    public static int getInt(String val) {
        if (DataValidator.isInteger(val)) {
            return Integer.parseInt(val);
        } else {
            return 0;
        }
    }

    /**
     * Converts a numeric string to long.
     *
     * @param val numeric string
     * @return parsed long if valid long, otherwise 0
     */
    public static long getLong(String val) {
        if (DataValidator.isLong(val)) {
            return Long.parseLong(val);
        } else {
            return 0;
        }
    }

    /**
     * Parses a date string using the application date format.
     *
     * @param val date string in {@value #APP_DATE_FORMAT} format
     * @return parsed {@link Date} or {@code null} on parse failure
     */
    public static Date getDate(String val) {
        Date date = null;
        try {
            date = formatter.parse(val);
        } catch (Exception e) {

        }
        return date;
    }

    /**
     * Formats a {@link Date} using the application date format.
     *
     * @param date the date to format
     * @return formatted date string or empty string on failure
     */
    public static String getDateString(Date date) {
        try {
            return formatter.format(date);
        } catch (Exception e) {
        }
        return "";
    }

    /**
     * Parses a timestamp string using the application time format.
     *
     * @param val timestamp string in {@value #APP_TIME_FORMAT} format
     * @return parsed {@link Timestamp} or {@code null} on parse failure
     */
    public static Timestamp getTimestamp(String val) {
        Timestamp timeStamp = null;
        try {
            timeStamp = new Timestamp((timeFormatter.parse(val)).getTime());
        } catch (Exception e) {
            return null;
        }
        return timeStamp;
    }

    /**
     * Creates a {@link Timestamp} from milliseconds since epoch.
     *
     * @param l milliseconds since epoch
     * @return {@link Timestamp} or {@code null} on failure
     */
    public static Timestamp getTimestamp(long l) {
        Timestamp timeStamp = null;
        try {
            timeStamp = new Timestamp(l);
        } catch (Exception e) {
            return null;
        }
        return timeStamp;
    }

    /**
     * Returns the current timestamp.
     *
     * @return current {@link Timestamp}
     */
    public static Timestamp getCurrentTimestamp() {
        Timestamp timeStamp = null;
        try {
            timeStamp = new Timestamp(new Date().getTime());
        } catch (Exception e) {
        }
        return timeStamp;

    }

    /**
     * Returns milliseconds value of the provided {@link Timestamp}.
     *
     * @param tm timestamp
     * @return milliseconds since epoch or 0 on failure
     */
    public static long getTimestamp(Timestamp tm) {
        try {
            return tm.getTime();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * A simple test driver for DataUtility methods.
     *
     * <p>
     * Note: the test strings used here (e.g. "10/15/2024") do not match
     * {@value #APP_DATE_FORMAT}. They are left as-is to preserve your original
     * test code.
     * </p>
     *
     * @param args command line args (not used)
     */
    public static void main(String[] args) {
        // Test getString
        System.out.println("getString Test:");
        System.out.println("Original: '  Hello World  ' -> Trimmed: '" + getString("  Hello World  ") + "'");
        System.out.println("Null input: " + getString(null));

        // Test getStringData
        System.out.println("\ngetStringData Test:");
        System.out.println("Object to String: " + getStringData(1234));
        System.out.println("Null Object: '" + getStringData(null) + "'");

        // Test getInt
        System.out.println("\ngetInt Test:");
        System.out.println("Valid Integer String: '124' -> " + getInt("124"));
        System.out.println("Invalid Integer String: 'abc' -> " + getInt("abc"));
        System.out.println("Null String: -> " + getInt(null));

        // Test getLong
        System.out.println("\ngetLong Test:");
        System.out.println("Valid Long String: '123456789' -> " + getLong("123456789"));
        System.out.println("Invalid Long String: 'abc' -> " + getLong("abc"));

        // Test getDate
        System.out.println("\ngetDate Test:");
        String dateStr = "10/15/2024";
        Date date = getDate(dateStr);
        System.out.println("String to Date: '" + dateStr + "' -> " + date);

        // Test getDateString
        System.out.println("\ngetDateString Test:");
        System.out.println("Date to String: '" + getDateString(new Date()) + "'");

        // Test getTimestamp (String)
        System.out.println("\ngetTimestamp(String) Test:");
        String timestampStr = "10/15/2024 10:30:45";
        Timestamp timestamp = getTimestamp(timestampStr);
        System.out.println("String to Timestamp: '" + timestampStr + "' -> " + timestamp);

        // Test getTimestamp (long)
        System.out.println("\ngetTimestamp(long) Test:");
        long currentTimeMillis = System.currentTimeMillis();
        Timestamp ts = getTimestamp(currentTimeMillis);
        System.out.println("Current Time Millis to Timestamp: '" + currentTimeMillis + "' -> " + ts);

        // Test getCurrentTimestamp
        System.out.println("\ngetCurrentTimestamp Test:");
        Timestamp currentTimestamp = getCurrentTimestamp();
        System.out.println("Current Timestamp: " + currentTimestamp);

        // Test getTimestamp(Timestamp)
        System.out.println("\ngetTimestamp(Timestamp) Test:");
        System.out.println("Timestamp to long: " + getTimestamp(currentTimestamp));
    }
}
