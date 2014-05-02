/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

package org.constellation.sos.ws;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.storage.DataStoreException;

// Constellation dependencies
import static org.constellation.sos.ws.SOSUtils.*;

// Geotk dependencies
import org.geotoolkit.swe.xml.AbstractEncoding;
import org.geotoolkit.swe.xml.DataArray;
import org.geotoolkit.swe.xml.TextBlock;
import org.geotoolkit.temporal.object.ISODateParser;
import org.apache.sis.util.logging.Logging;

// GeoAPI dependencies
import org.opengis.filter.Filter;
import org.opengis.filter.temporal.After;
import org.opengis.filter.temporal.Before;
import org.opengis.filter.temporal.During;
import org.opengis.filter.temporal.TEquals;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DatablockParser {

    private static final Logger LOGGER = Logging.getLogger(DatablockParser.class);
    
    
    public static Values getResultValues(final Timestamp tBegin, final Timestamp tEnd, final DataArray array, final List<Filter> eventTimes) throws DataStoreException {
        Values values;

        //for multiple observations we parse the brut values (if we got a time constraint)
        if (tBegin != null && tEnd != null) {

            values = new Values();
            values.values.append(array.getValues());
            values.nbBlock = array.getElementCount().getCount().getValue();

            for (Filter bound: eventTimes) {
                LOGGER.log(Level.FINER, " Values: {0}", values);
                if (bound instanceof TEquals) {
                    final TEquals filter = (TEquals) bound;
                    if (filter.getExpression2() instanceof Instant) {
                        final Instant ti    = (Instant) filter.getExpression2();
                        final Timestamp boundEquals = getTimestampValue(ti.getPosition());

                        LOGGER.finer("TE case 1");
                        //case 1 the periods contains a matching values
                        values = parseDataBlock(values.values.toString(), array.getEncoding(), null, null, boundEquals);
                    }

                } else if (bound instanceof After) {
                    final After filter = (After) bound;
                    final Instant ti   = (Instant) filter.getExpression2();
                    final Timestamp boundBegin = getTimestampValue(ti.getPosition());

                    // case 1 the period overlaps the bound
                    if (tBegin.before(boundBegin) && tEnd.after(boundBegin)) {
                        LOGGER.finer("TA case 1");
                        values = parseDataBlock(values.values.toString(), array.getEncoding(), boundBegin, null, null);
                    }

                } else if (bound instanceof Before) {
                    final Before filter = (Before) bound;
                    final Instant ti    = (Instant) filter.getExpression2();
                    final Timestamp boundEnd = getTimestampValue(ti.getPosition());

                    // case 1 the period overlaps the bound
                    if (tBegin.before(boundEnd) && tEnd.after(boundEnd)) {
                        LOGGER.finer("TB case 1");
                        values = parseDataBlock(values.values.toString(), array.getEncoding(), null, boundEnd, null);
                    }

                } else if (bound instanceof During) {
                    final During filter = (During) bound;
                    final Period tp     = (Period)filter.getExpression2();
                    final Timestamp boundBegin = getTimestampValue(tp.getBeginning().getPosition());
                    final Timestamp boundEnd   = getTimestampValue(tp.getEnding().getPosition());

                    // case 1 the period overlaps the first bound
                    if (tBegin.before(boundBegin) && tEnd.before(boundEnd) && tEnd.after(boundBegin)) {
                        LOGGER.finer("TD case 1");
                        values = parseDataBlock(values.values.toString(), array.getEncoding(), boundBegin, boundEnd, null);

                    // case 2 the period overlaps the second bound
                    } else if (tBegin.after(boundBegin) && tEnd.after(boundEnd) && tBegin.before(boundEnd)) {
                        LOGGER.finer("TD case 2");
                        values = parseDataBlock(values.values.toString(), array.getEncoding(), boundBegin, boundEnd, null);

                    // case 3 the period totaly overlaps the bounds
                    } else if (tBegin.before(boundBegin) && tEnd.after(boundEnd)) {
                        LOGGER.finer("TD case 3");
                        values = parseDataBlock(values.values.toString(), array.getEncoding(), boundBegin, boundEnd, null);
                    }

                }
            }


        //if this is a simple observation, or if there is no time bound
        } else {
            values = new Values();
            values.values.append(array.getValues());
            values.nbBlock = array.getElementCount().getCount().getValue();
        }
        return values;
    }

    /**
     * Parse a data block and return only the values matching the time filter.
     *
     * @param brutValues The data block.
     * @param abstractEncoding The encoding of the data block.
     * @param boundBegin The begin bound of the time filter.
     * @param boundEnd The end bound of the time filter.
     * @param boundEquals An equals time filter (implies boundBegin and boundEnd null).
     *
     * @return a datablock containing only the matching observations.
     */
    private static Values parseDataBlock(final String brutValues, final AbstractEncoding abstractEncoding, final Timestamp boundBegin, final Timestamp boundEnd, final Timestamp boundEquals) {
        final Values values = new Values();
        if (abstractEncoding instanceof TextBlock) {
            final TextBlock encoding        = (TextBlock) abstractEncoding;
            final StringTokenizer tokenizer = new StringTokenizer(brutValues, encoding.getBlockSeparator());
            while (tokenizer.hasMoreTokens()) {
                final String block = tokenizer.nextToken();
                final String samplingTimeValue = block.substring(0, block.indexOf(encoding.getTokenSeparator()));
                Date d = null;
                try {
                    final ISODateParser parser = new ISODateParser();
                    d = parser.parseToDate(samplingTimeValue);
                } catch (NumberFormatException ex) {
                    LOGGER.log(Level.FINER, "unable to parse the value: {0}", samplingTimeValue);
                }
                if (d == null) {
                    LOGGER.log(Level.WARNING, "unable to parse the value: {0}", samplingTimeValue);
                    continue;
                }
                final Timestamp t = new Timestamp(d.getTime());

                // time during case
                if (boundBegin != null && boundEnd != null) {
                    if (t.after(boundBegin) && t.before(boundEnd)) {
                        values.values.append(block).append(encoding.getBlockSeparator());
                        values.nbBlock++;
                    }

                //time after case
                } else if (boundBegin != null && boundEnd == null) {
                    if (t.after(boundBegin)) {
                        values.values.append(block).append(encoding.getBlockSeparator());
                        values.nbBlock++;
                    }

                //time before case
                } else if (boundBegin == null && boundEnd != null) {
                    if (t.before(boundEnd)) {
                        values.values.append(block).append(encoding.getBlockSeparator());
                        values.nbBlock++;
                    }

                //time equals case
                } else if (boundEquals != null) {
                    if (t.equals(boundEquals)) {
                        values.values.append(block).append(encoding.getBlockSeparator());
                        values.nbBlock++;
                    }
                }
            }
        } else {
            LOGGER.severe("unable to parse datablock unknown encoding");
            values.values.append(brutValues);
        }
        return values;
    }
    
    public static class Values {
        public StringBuilder values = new StringBuilder();
        public int nbBlock = 0;
    }
}
