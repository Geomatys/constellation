/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.util.Date;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * 
 * 
 * @author Guilhem Legal
 * @author Mehdi Sidhoum
 */
public class PeriodUtilities {
    
    /**
     * The number of millisecond in one year.
     */
    private static final long YEAR_MS = 31536000000L;
    
    /**
     * The number of millisecond in one month.
     */
    private static final long MONTH_MS = 2628000000L;
    
    /**
     * The number of millisecond in one week.
     */
    private static final long WEEK_MS = 604800000L;
    
    /**
     * The number of millisecond in one day.
     */
    private static final long DAY_MS = 86400000L;
    
    /**
     * The number of millisecond in one hour.
     */
    private static final long HOUR_MS = 3600000L;
    
    /**
     * The number of millisecond in one minute.
     */
    private static final long MIN_MS = 60000;
    
    /**
     * The number of millisecond in one second.
     */
    private static final long SECOND_MS = 1000;
        
    /**
     * The format of the dates. 
     */
    private DateFormat dateFormat;
    
    /**
     * Build a new period worker with the specified DateFormat 
     */
    public PeriodUtilities(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }
    
    /**
     * Evaluate the periodical gap between the different available time.
     * Return a String concatening periods and isolated date.
     *
     * @param dates a sorted set of date (ordered by time).
     */
    public String getDatesRespresentation(SortedSet<Date> dates) {
        if (dates.comparator() != null) {
            throw new IllegalArgumentException();
        }
        
        final StringBuffer response = new StringBuffer();
       /* Iterator<Date> it = dates.iterator();
        if (!it.hasNext()) {
            return "";
        }
        Date first = it.next();
        Date previous = first;
        long previousGap = 0;
        long gap;
        while (it.hasNext()) {
            Date next = it.next();
            gap = next.getTime() - previous.getTime();
            if (gap == 0) {
                continue;
            }
            if (gap < 0) {
                throw new IllegalArgumentException();
            }
            if (previousGap == 0) {
                previousGap = gap;
            } else if (previousGap != gap) {
                response.append(getPeriodDescription(first, previous, gap));
                first = previous;
            }
            previous = next;
        }
        response.append(getPeriodDescription(first, previous, gap));*/
            
        

        if (dates.isEmpty()) {
            return "";
        }
        
        Date first          = dates.first();
        Date previousDate   = first;
        long previousGap    = 0;
        long gap            = 0;
        int nbDataInGap     = 0;

        for (Date d : dates) {
            previousGap = gap;
            gap = d.getTime() - previousDate.getTime();

            if (previousGap != gap) {
                if (nbDataInGap >= 2) {
                    final String firstDate = dateFormat.format(first) + "";
                    if (response.indexOf(firstDate + ',') != -1) {
                        final int pos = response.indexOf(firstDate);
                        response.delete(pos, pos + firstDate.length() + 1);
                    } 
                    response.append(getPeriodDescription(dates.subSet(first, d), previousGap)).append(',');
                    nbDataInGap = 1;
                } else {
                    if (nbDataInGap > 0) {
                        dateFormat.format(previousDate, response, new FieldPosition(0)).append(',');
                        nbDataInGap = 1;
                    }
                }
                first       = previousDate;

            } else {
                nbDataInGap++;
            }

            previousDate = d;
        }

        if (nbDataInGap > 0) {
            if (nbDataInGap >= 2) {
                final String firstDate = dateFormat.format(first) + "";
                if (response.indexOf(firstDate + ',') != -1) {
                    final int pos = response.indexOf(firstDate);
                    response.delete(pos, pos + firstDate.length() + 1);
                } 
                response.append(getPeriodDescription(dates.tailSet(first), gap));
                nbDataInGap = 1;
            } else {
                if (nbDataInGap > 0) {
                    dateFormat.format(previousDate, response, new FieldPosition(0));
                    nbDataInGap = 1;
                }
           }
        }

        return response.toString();
    }

    /**
     * Return a String for a range of date (or just one)
     * 
     * @param first
     * @param last
     * @param gap
     * @return
     */
    public String getPeriodDescription(SortedSet<Date> dates, long gap) {
        final StringBuffer response = new StringBuffer();
        dateFormat.format(dates.first(), response, new FieldPosition(0));
        response.append('/');
        
        dateFormat.format(dates.last(), response, new FieldPosition(0));
        response.append("/P");

        //we look if the gap is more than one year (31536000000 ms)
        long temp = gap / YEAR_MS;
        if (temp > 1) {
            response.append(temp).append("Y");
            gap -= temp * YEAR_MS;
        }

        //we look if the gap is more than one month (2628000000 ms)
        temp = gap / MONTH_MS;
        if (temp >= 1) {
            response.append(temp).append("M");
            gap -= temp * MONTH_MS;
        }
        //we look if the gap is more than one week (604800000 ms)
        temp = gap / WEEK_MS;
        if (temp >= 1) {
            response.append(temp).append("W");
            gap -= temp * WEEK_MS;
        }

        //we look if the gap is more than one day (86400000 ms)
        temp = gap / DAY_MS;
        if (temp >= 1) {
            response.append(temp).append("D");
            gap -= temp * DAY_MS;
        }

        //if the gap is not over we pass to the hours by adding 'T'
        if (gap != 0) {
            response.append('T');
        }

        //we look if the gap is more than one hour (3600000 ms)
        temp = gap / HOUR_MS;
        if (temp >= 1) {
            response.append(temp).append("H");
            gap -= temp * HOUR_MS;
        }

        //we look if the gap is more than one min (60000 ms)
        temp = gap / MIN_MS;
        if (temp >= 1) {
            response.append(temp).append("M");
            gap -= temp * MIN_MS;
        }

        //we look if the gap is more than one week (1000 ms)
        temp = gap / SECOND_MS;
        if (temp >= 1) {
            response.append(temp).append("S");
            gap -= temp * SECOND_MS;
        }
        if (gap != 0) {
            throw new IllegalArgumentException("TimePeriod can't be found a the Millisecond precision");
        }
        return response.toString();
    }

    /**
     * Return a sorted set from a string description.
     * 
     * @param periods
     * @param df
     * @return
     * @throws java.text.ParseException
     */
    public SortedSet<Date> getDatesFromPeriodDescription(String periods) throws ParseException {
        final SortedSet<Date> response = new TreeSet<Date>();
        final StringTokenizer tokens = new StringTokenizer(periods, ",");
        while (tokens.hasMoreTokens()) {
            String dates = tokens.nextToken().trim();

            if (dates.indexOf('/') == -1) {
                response.add(dateFormat.parse(dates));

            } else {

                //we get the begin position
                final String begin = dates.substring(0, dates.indexOf('/'));
                final Date first = dateFormat.parse(begin);
                dates = dates.substring(dates.indexOf('/') + 1);

                //we get the end position
                final String end = dates.substring(0, dates.indexOf('/'));
                final Date last = dateFormat.parse(end);
                dates = dates.substring(dates.indexOf('/') + 1);

                //then we get the period Description
                final long gap = getTimeFromPeriodDescription(dates);

                Date currentDate = first;
                while (!currentDate.equals(last)) {
                    response.add(currentDate);
                    currentDate = new Date(currentDate.getTime() + gap);
                }
                response.add(last);
            }
        }
        return response;
    }

    /**
     * Return a Date (long time) from a String description
     * 
     * @param periodDescription
     * @return
     */
    public long getTimeFromPeriodDescription(String periodDescription) {

        long time = 0;
        //we remove the 'P'
        periodDescription = periodDescription.substring(1);

        //we look if the period contains years (31536000000 ms)
        if (periodDescription.indexOf('Y') != -1) {
            final int nbYear = Integer.parseInt(periodDescription.substring(0, periodDescription.indexOf('Y')));
            time += nbYear * YEAR_MS;
            periodDescription = periodDescription.substring(periodDescription.indexOf('Y') + 1);
        }

        //we look if the period contains months (2628000000 ms)
        if (    periodDescription.indexOf('M') != -1 && 
                (periodDescription.indexOf('T') == -1 || periodDescription.indexOf('T') > periodDescription.indexOf('M')) ) {
            final int nbMonth = Integer.parseInt(periodDescription.substring(0, periodDescription.indexOf('M')));
            time += nbMonth * MONTH_MS;
            periodDescription = periodDescription.substring(periodDescription.indexOf('M') + 1);
        }
        
        //we look if the period contains weeks (604800000 ms)
        if (periodDescription.indexOf('W') != -1) {
            final int nbWeek = Integer.parseInt(periodDescription.substring(0, periodDescription.indexOf('W')));
            time += nbWeek * WEEK_MS;
            periodDescription = periodDescription.substring(periodDescription.indexOf('W') + 1);
        }

        //we look if the period contains days (86400000 ms)
        if (periodDescription.indexOf('D') != -1) {
            final int nbDay = Integer.parseInt(periodDescription.substring(0, periodDescription.indexOf('D')));
            time += nbDay * DAY_MS;
            periodDescription = periodDescription.substring(periodDescription.indexOf('D') + 1);
        }

        //if the periodDescription is not over we pass to the hours by removing 'T'
        if (periodDescription.indexOf('T') != -1) {
            periodDescription = periodDescription.substring(1);
        }

        //we look if the period contains hours (3600000 ms)
        if (periodDescription.indexOf('H') != -1) {
            final int nbHour = Integer.parseInt(periodDescription.substring(0, periodDescription.indexOf('H')));
            time += nbHour * HOUR_MS;
            periodDescription = periodDescription.substring(periodDescription.indexOf('H') + 1);
        }

        //we look if the period contains minutes (60000 ms)
        if (periodDescription.indexOf('M') != -1) {
            final int nbMin = Integer.parseInt(periodDescription.substring(0, periodDescription.indexOf('M')));
            time += nbMin * MIN_MS;
            periodDescription = periodDescription.substring(periodDescription.indexOf('M') + 1);
        }

        //we look if the period contains seconds (1000 ms)
        if (periodDescription.indexOf('S') != -1) {
            final int nbSec = Integer.parseInt(periodDescription.substring(0, periodDescription.indexOf('S')));
            time += nbSec * SECOND_MS;
            periodDescription = periodDescription.substring(periodDescription.indexOf('S') + 1);
        }

        if (periodDescription.length() != 0) {
            throw new IllegalArgumentException("The period descritpion is malformed");
        }
        return time;
    }
}
