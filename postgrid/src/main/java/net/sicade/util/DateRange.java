/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.sicade.util;

import java.util.Date;
import javax.media.jai.util.Range;
import javax.units.SI;
import javax.units.Unit;
import javax.units.Converter;
import javax.units.ConversionException;

import org.geotools.util.MeasurementRange;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * A range of dates.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DateRange extends Range {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -6400011350250757942L;

    /**
     * The unit used for time representation in a date.
     */
    private static final Unit MILLISECOND = SI.MILLI(SI.SECOND);

    /**
     * Creates a new date range for the given dates. Start time and end time are inclusive.
     */
    public DateRange(final Date startTime, final Date endTime) {
        super(Date.class, clone(startTime), clone(endTime));
    }

    /**
     * Creates a new date range for the given dates.
     */
    public DateRange(final Date startTime, boolean isMinIncluded,
                     final Date   endTime, boolean isMaxIncluded)
    {
        super(Date.class, clone(startTime), isMinIncluded,
                          clone(  endTime), isMaxIncluded);
    }

    /**
     * Creates a date range from the specified measurement range. Units are converted as needed.
     *
     * @throws ConversionException if the given range doesn't have a
     *         {@linkplain MeasurementRange#getUnits unit} compatible with milliseconds.
     */
    public DateRange(final MeasurementRange range, final Date origin) throws ConversionException {
        this(range, getConverter(range.getUnits()), origin.getTime());
    }

    /**
     * Workaround for RFE #4093999 ("Relax constraint on placement of this()/super()
     * call in constructors").
     */
    private DateRange(final MeasurementRange range, final Converter converter, final long origin)
            throws ConversionException
    {
        super(Date.class,
              new Date(origin + Math.round(converter.convert(range.getMinimum()))), range.isMinIncluded(),
              new Date(origin + Math.round(converter.convert(range.getMaximum()))), range.isMaxIncluded());
    }

    /**
     * Returns a clone of the specified date.
     */
    private static Date clone(final Date date) {
        return (date != null) ? (Date) date.clone() : null;
    }

    /**
     * Workaround for RFE #4093999 ("Relax constraint on placement of this()/super()
     * call in constructors").
     */
    private static Converter getConverter(final Unit source) throws ConversionException {
        if (source == null) {
            throw new ConversionException(Errors.format(ErrorKeys.NO_UNIT));
        }
        return source.getConverterTo(MILLISECOND);
    }

    /**
     * Returns the start time.
     */
    @Override
    public Date getMinValue() {
        return clone((Date) super.getMinValue());
    }

    /**
     * Returns the end time.
     */
    @Override
    public Date getMaxValue() {
        return clone((Date) super.getMaxValue());
    }
}
