
package net.sicade.observation;

import java.sql.Timestamp;
import java.util.StringTokenizer;


/**
 *  Cette classe remplace le type primitif SQL timestamp pour remplir les contrainte impose par JaxB
 *  c'est a dire au moins un constructeur vide. 
 *
 * @author legal
 */
public class TimestampEntry  extends java.util.Date {
    
    /**
     * Constructeur vide exige par JAXB d'ou la creation de cette classe.
     */
    public TimestampEntry() {}
    
    /**
     * un constructeur qui prend un timestamp pour en faire un timestampEntry
     */
     public TimestampEntry(Timestamp t) {
         super((t.getTime()/1000)*1000);
	 nanos = (int)((t.getTime()%1000) * 1000000);
	 if (nanos < 0) {
	    nanos = 1000000000 + nanos;	    
	    super.setTime(((t.getTime()/1000)-1)*1000);
	}
     }
     
    /**
     * Constructs a <code>Timestamp</code> object initialized
     * with the given values.
     *
     * @param year the year minus 1900
     * @param month 0 to 11 
     * @param date 1 to 31
     * @param hour 0 to 23
     * @param minute 0 to 59
     * @param second 0 to 59
     * @param nano 0 to 999,999,999
     * @deprecated instead use the constructor <code>Timestamp(long millis)</code>
     * @exception IllegalArgumentException if the nano argument is out of bounds
     */
    @Deprecated
    public TimestampEntry(int year, int month, int date, 
		     int hour, int minute, int second, int nano) {
	super(year, month, date, hour, minute, second);
	if (nano > 999999999 || nano < 0) {
	    throw new IllegalArgumentException("nanos > 999999999 or < 0");
	}
	nanos = nano;
    }

    /**
     * Constructs a <code>Timestamp</code> object 
     * using a milliseconds time value. The
     * integral seconds are stored in the underlying date value; the
     * fractional seconds are stored in the <code>nanos</code> field of
     * the <code>Timestamp</code> object.
     *
     * @param time milliseconds since January 1, 1970, 00:00:00 GMT.
     *        A negative number is the number of milliseconds before
     *         January 1, 1970, 00:00:00 GMT.
     * @see java.util.Calendar 
     */
    public TimestampEntry(long time) {
	super((time/1000)*1000);
	nanos = (int)((time%1000) * 1000000);
	if (nanos < 0) {
	    nanos = 1000000000 + nanos;	    
	    super.setTime(((time/1000)-1)*1000);
	}
    }

    /**
     * Sets this <code>Timestamp</code> object to represent a point in time that is 
     * <tt>time</tt> milliseconds after January 1, 1970 00:00:00 GMT. 
     *
     * @param time   the number of milliseconds.
     * @see #getTime
     * @see #Timestamp(long time)
     * @see java.util.Calendar
     */
    @Override
    public void setTime(long time) {
	super.setTime((time/1000)*1000);
	nanos = (int)((time%1000) * 1000000);
	if (nanos < 0) {
	    nanos = 1000000000 + nanos;	    
	    super.setTime(((time/1000)-1)*1000);
	}
    }

    /**
     * Returns the number of milliseconds since January 1, 1970, 00:00:00 GMT
     * represented by this <code>Timestamp</code> object.
     *
     * @return  the number of milliseconds since January 1, 1970, 00:00:00 GMT
     *          represented by this date.
     * @see #setTime
     */
    @Override
    public long getTime() {
        long time = super.getTime();
        return (time + (nanos / 1000000));
    }
            

    /**
     * @serial
     */
    private int nanos;

    /**
     * Converts a <code>String</code> object in JDBC timestamp escape format to a
     * <code>Timestamp</code> value.
     *
     * @param s timestamp in format <code>yyyy-mm-dd hh:mm:ss[.f...]</code>.  The
     * fractional seconds may be omitted.
     * @return corresponding <code>Timestamp</code> value
     * @exception java.lang.IllegalArgumentException if the given argument
     * does not have the format <code>yyyy-mm-dd hh:mm:ss[.f...]</code>
     */
    public static TimestampEntry valueOf(String s) {
	String date_s;
	String time_s;
	String nanos_s;
	int year;
	int month;
	int day;
	int hour;
	int minute;
	int second;
	int a_nanos = 0;
	int firstDash;
	int secondDash;
	int dividingSpace;
	int firstColon = 0;
	int secondColon = 0;
	int period = 0;
	String formatError = "Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]";
	String zeros = "000000000";
	String delimiterDate = "-";
	String delimiterTime = ":";
	StringTokenizer stringTokeninzerDate;
	StringTokenizer stringTokeninzerTime;

	if (s == null) throw new java.lang.IllegalArgumentException("null string");

	int counterD = 0;
	int intDate[] = {4,2,2};

	int counterT = 0;
	int intTime[] = {2,2,12};

	// Split the string into date and time components
	s = s.trim();
	dividingSpace = s.indexOf(' ');
	if (dividingSpace > 0) {
	    date_s = s.substring(0,dividingSpace);
	    time_s = s.substring(dividingSpace+1);
	} else {
	    throw new java.lang.IllegalArgumentException(formatError);
	}

	stringTokeninzerTime = new StringTokenizer(time_s, delimiterTime);
	stringTokeninzerDate = new StringTokenizer(date_s, delimiterDate);
	
	while(stringTokeninzerDate.hasMoreTokens()) {
	     String tokenDate = stringTokeninzerDate.nextToken();
	     if(tokenDate.length() != intDate[counterD] ) {
		throw new java.lang.IllegalArgumentException(formatError);
	     }
	     counterD++;	
	}
	
	/*
         //Commenting this portion out for checking of time

	while(stringTokeninzerTime.hasMoreTokens()) {
	     String tokenTime = stringTokeninzerTime.nextToken();
	
	     if	(counterT < 2 && tokenTime.length() != intTime[counterT]  ) {
		throw new java.lang.IllegalArgumentException(formatError);
	     }
	     counterT++;	
	}	     
	*/	  
  
	// Parse the date
	firstDash = date_s.indexOf('-');
	secondDash = date_s.indexOf('-', firstDash+1);

	// Parse the time
	if (time_s == null) 
	    throw new java.lang.IllegalArgumentException(formatError);
	firstColon = time_s.indexOf(':');
	secondColon = time_s.indexOf(':', firstColon+1);
	period = time_s.indexOf('.', secondColon+1);

	// Convert the date
	if ((firstDash > 0) && (secondDash > 0) && 
	    (secondDash < date_s.length()-1)) {
	    year = Integer.parseInt(date_s.substring(0, firstDash)) - 1900;
	    month = 
		Integer.parseInt(date_s.substring
				 (firstDash+1, secondDash)) - 1;
	    day = Integer.parseInt(date_s.substring(secondDash+1));
	} else {		
	    throw new java.lang.IllegalArgumentException(formatError);
	}

	// Convert the time; default missing nanos
	if ((firstColon > 0) & (secondColon > 0) & 
	    (secondColon < time_s.length()-1)) {
	    hour = Integer.parseInt(time_s.substring(0, firstColon));
	    minute = 
		Integer.parseInt(time_s.substring(firstColon+1, secondColon));
	    if ((period > 0) & (period < time_s.length()-1)) {
		second = 
		    Integer.parseInt(time_s.substring(secondColon+1, period));
		nanos_s = time_s.substring(period+1);
		if (nanos_s.length() > 9) 
		    throw new java.lang.IllegalArgumentException(formatError);
		if (!Character.isDigit(nanos_s.charAt(0)))
		    throw new java.lang.IllegalArgumentException(formatError);
		nanos_s = nanos_s + zeros.substring(0,9-nanos_s.length());
		a_nanos = Integer.parseInt(nanos_s);
	    } else if (period > 0) {
		throw new java.lang.IllegalArgumentException(formatError);
	    } else {
		second = Integer.parseInt(time_s.substring(secondColon+1));
	    }
	} else {
	    throw new java.lang.IllegalArgumentException();
	}

	return new TimestampEntry(year, month, day, hour, minute, second, a_nanos);
    }

    /**
     * Formats a timestamp in JDBC timestamp escape format.
     *         <code>yyyy-mm-dd hh:mm:ss.fffffffff</code>,
     * where <code>ffffffffff</code> indicates nanoseconds.
     * <P>
     * @return a <code>String</code> object in
     *           <code>yyyy-mm-dd hh:mm:ss.fffffffff</code> format
     */
    public String toString () {

	int year = super.getYear() + 1900;
	int month = super.getMonth() + 1;
	int day = super.getDate();
	int hour = super.getHours();
	int minute = super.getMinutes();
	int second = super.getSeconds();
	String yearString;
	String monthString;
	String dayString;
	String hourString;
	String minuteString;
	String secondString;
	String nanosString;
	String zeros = "000000000";
	String yearZeros = "0000";
	StringBuffer timestampBuf;

	if (year < 1000) {
	    // Add leading zeros 
	    yearString = "" + year;
	    yearString = yearZeros.substring(0, (4-yearString.length())) + 
	   	yearString;
	} else {
	    yearString = "" + year;
	}
	if (month < 10) {
	    monthString = "0" + month;
	} else {
	    monthString = Integer.toString(month);
	} 
	if (day < 10) {
	    dayString = "0" + day;
	} else {
	    dayString = Integer.toString(day);
	}
	if (hour < 10) {
	    hourString = "0" + hour;
	} else {
	    hourString = Integer.toString(hour);
	}
	if (minute < 10) {
	    minuteString = "0" + minute;
	} else {
	    minuteString = Integer.toString(minute);
	}
	if (second < 10) {
	    secondString = "0" + second;
	} else {
	    secondString = Integer.toString(second);
	}
	if (nanos == 0) {
	    nanosString = "0";
	} else {
	    nanosString = Integer.toString(nanos);

	    // Add leading zeros
	    nanosString = zeros.substring(0, (9-nanosString.length())) +
		nanosString; 

	    // Truncate trailing zeros
	    char[] nanosChar = new char[nanosString.length()];
	    nanosString.getChars(0, nanosString.length(), nanosChar, 0);
	    int truncIndex = 8;
	    while (nanosChar[truncIndex] == '0') {
		truncIndex--;
	    }
	
	    nanosString = new String(nanosChar, 0, truncIndex + 1);
	}

	// do a string buffer here instead.
	timestampBuf = new StringBuffer(20+nanosString.length());
	timestampBuf.append(yearString);
	timestampBuf.append("-");
	timestampBuf.append(monthString);
	timestampBuf.append("-");
	timestampBuf.append(dayString);
	timestampBuf.append(" ");
	timestampBuf.append(hourString);
	timestampBuf.append(":");
	timestampBuf.append(minuteString);
	timestampBuf.append(":");
	timestampBuf.append(secondString);
	timestampBuf.append(".");
	timestampBuf.append(nanosString);
	
	return (timestampBuf.toString());
    }

    /**
     * Gets this <code>Timestamp</code> object's <code>nanos</code> value.
     *
     * @return this <code>Timestamp</code> object's fractional seconds component
     * @see #setNanos
     */
    public int getNanos() {
	return nanos;
    }

    /**
     * Sets this <code>Timestamp</code> object's <code>nanos</code> field
     * to the given value.
     *
     * @param n the new fractional seconds component
     * @exception java.lang.IllegalArgumentException if the given argument
     *            is greater than 999999999 or less than 0
     * @see #getNanos
     */
    public void setNanos(int n) {
	if (n > 999999999 || n < 0) {
	    throw new IllegalArgumentException("nanos > 999999999 or < 0");
	}
	nanos = n;
    }

    /**
     * Tests to see if this <code>Timestamp</code> object is
     * equal to the given <code>Timestamp</code> object.
     *
     * @param ts the <code>Timestamp</code> value to compare with
     * @return <code>true</code> if the given <code>Timestamp</code>
     *         object is equal to this <code>Timestamp</code> object;
     *         <code>false</code> otherwise
     */
    public boolean equals(TimestampEntry ts) {
	if (super.equals(ts)) {
	    if  (nanos == ts.nanos) {
		return true;
	    } else {
		return false;
	    }
	} else {
	    return false;
	}
    }

    /**
     * Tests to see if this <code>Timestamp</code> object is
     * equal to the given object.
     *
     * This version of the method <code>equals</code> has been added
     * to fix the incorrect 
     * signature of <code>Timestamp.equals(Timestamp)</code> and to preserve backward 
     * compatibility with existing class files.
     *
     * Note: This method is not symmetric with respect to the 
     * <code>equals(Object)</code> method in the base class.
     *
     * @param ts the <code>Object</code> value to compare with
     * @return <code>true</code> if the given <code>Object</code> is an instance
     *         of a <code>Timestamp</code> that
     *         is equal to this <code>Timestamp</code> object;
     *         <code>false</code> otherwise
     */
    @Override
    public boolean equals(java.lang.Object ts) {
      if (ts instanceof TimestampEntry) {
	return this.equals((TimestampEntry)ts);
      } else {
	return false;
      }
    }

    /**
     * Indicates whether this <code>Timestamp</code> object is
     * earlier than the given <code>Timestamp</code> object.
     *
     * @param ts the <code>Timestamp</code> value to compare with
     * @return <code>true</code> if this <code>Timestamp</code> object is earlier;
     *        <code>false</code> otherwise
     */
    public boolean before(TimestampEntry ts) {
	return compareTo(ts) < 0;
    }

    /**
     * Indicates whether this <code>Timestamp</code> object is
     * later than the given <code>Timestamp</code> object.
     *
     * @param ts the <code>Timestamp</code> value to compare with
     * @return <code>true</code> if this <code>Timestamp</code> object is later;
     *        <code>false</code> otherwise
     */
    public boolean after(TimestampEntry ts) {
	return compareTo(ts) > 0;
    }

    /**
     * Compares this <code>Timestamp</code> object to the given 
     * <code>Timestamp</code> object.
     *
     * @param   ts   the <code>Timestamp</code> object to be compared to
     *                this <code>Timestamp</code> object
     * @return  the value <code>0</code> if the two <code>Timestamp</code>
     *          objects are equal; a value less than <code>0</code> if this 
     *          <code>Timestamp</code> object is before the given argument;
     *          and a value greater than <code>0</code> if this 
     *          <code>Timestamp</code> object is after the given argument.
     * @since   1.4
     */
    public int compareTo(TimestampEntry ts) {
        int i = super.compareTo(ts);
        if (i == 0) {
            if (nanos > ts.nanos) {
		    return 1;
            } else if (nanos < ts.nanos) {
                return -1;
            }
        }
        return i;

    }

    /**
     * Compares this <code>Timestamp</code> object to the given 
     * <code>Date</code>, which must be a <code>Timestamp</code>
     * object. If the argument is not a <code>Timestamp</code> object,
     * this method throws a <code>ClassCastException</code> object.
     * (<code>Timestamp</code> objects are 
     * comparable only to other <code>Timestamp</code> objects.)
     *
     * @param o the <code>Date</code> to be compared, which must be a
     *        <code>Timestamp</code> object
     * @return  the value <code>0</code> if this <code>Timestamp</code> object
     *          and the given object are equal; a value less than <code>0</code> 
     *          if this  <code>Timestamp</code> object is before the given argument;
     *          and a value greater than <code>0</code> if this 
     *          <code>Timestamp</code> object is after the given argument.
     *
     * @since	1.5
     */
    @Override
    public int compareTo(java.util.Date o) {
       if(o instanceof TimestampEntry) {
            // When Timestamp instance compare it with a Timestamp
            // Hence it is basically calling this.compareTo((Timestamp))o);
            // Note typecasting is safe because o is instance of Timestamp
           return compareTo((TimestampEntry)o);
      } else {
            // When Date doing a o.compareTo(this)
            // will give wrong results.
          TimestampEntry ts = new TimestampEntry(o.getTime());
          return this.compareTo(ts);
      }
    } 
            
    static final long serialVersionUID = 2745179027874758501L;

}
