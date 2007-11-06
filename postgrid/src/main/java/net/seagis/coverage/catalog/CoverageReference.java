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
package net.seagis.coverage.catalog;

import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import javax.imageio.IIOException;

import org.opengis.coverage.SampleDimension;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.geotools.image.io.IIOListeners;
import org.geotools.coverage.CoverageStack;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.util.NumberRange;

import net.seagis.catalog.Element;
import net.seagis.util.DateRange;


/**
 * Reference to a {@linkplain Coverage coverage}. This object holds some metadata about the coverage
 * ({@linkplain #getTimeRange time range}, {@linkplain #getGeographicBoundingBox geographic bounding
 * box}, <cite>etc.</cite>) without the need to load the coverage itself. Coverage loading will
 * occurs only when {@link #getCoverage} is invoked for the first time.
 * <p>
 * {@code CoverageReference} instances are immutable and thread-safe.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface CoverageReference extends Element, CoverageStack.Element {
    /**
     * Key to use for storing a {@code CoverageReference} as {@link GridCoverage2D} property.
     * Users can obtain a {@code CoverageReference} from a {@code GridCoverage2D} as below:
     *
     * <blockquote><pre>
     * CoverageReference reference = ...
     * GridCoverage2D    coverage  = reference.{@linkplain #getCoverage getCoverage}(null);
     * CoverageReference source    = (CoverageReference) coverage.getProperty(CoverageReference.REFERENCE_KEY);
     * assert source == reference;
     * </pre></blockquote>
     */
    String REFERENCE_KEY = "net.seagis.observation.CoverageReference";

    /**
     * Returns the series of this coverage reference.
     */
    Series getSeries();

    /**
     * Returns the path to the image file, or {@code null} if the file is not accessible
     * on the local machine. In the later case, {@link #getURL} should be used instead.
     */
    File getFile();

    /**
     * Returns the URL to the image data, or {@code null} if none. The data may or may not
     * be a file hosted on the local machine.
     */
    URL getURL();

    /**
     * Returns the coordinate reference system for the {@linkplain #getCoverage coverage}.
     * This is also the CRS for the {@linkplain #getEnvelope coverage envelope}.
     */
    CoordinateReferenceSystem getCoordinateReferenceSystem();

    /**
     * Returns the spatio-temporal envelope for the {@linkplain #getCoverage coverage}.
     */
    @Override
    Envelope getEnvelope();

    /**
     * Returns the temporal part of the {@linkplain #getEnvelope coverage envelope}.
     * Invoking this method is equivalent to extracting the temporal component of the
     * envelope and transform the coordinates if needed.
     */
    DateRange getTimeRange();

    /**
     * Returns the geographic bounding box of the {@linkplain #getEnvelope coverage envelope}.
     * Invoking this method is equivalent to extracting the horizontal component of the envelope
     * and transform the coordinates if needed.
     */
    GeographicBoundingBox getGeographicBoundingBox();

    /**
     * For {@link org.geotools.coverage.CoverageStack.Element} implementation only.
     */
    @Override
    NumberRange getZRange();

    /**
     * Returns the {@linkplain #getCoverage coverage} grid geometry.
     */
    @Override
    GridGeometry2D getGridGeometry();

    /**
     * Returns the {@linkplain #getCoverage coverage} sample dimensions. This method returns
     * always the <cite>geophysics</cite> version of sample dimensions
     * (<code>{@linkplain GridSampleDimension#geophysics geophysics}(true)</code>), which is
     * consistent with the coverage returned by {@link #getCoverage getCoverage(...)}.
     */
    @Override
    SampleDimension[] getSampleDimensions();

    /**
     * Loads the data if needed and returns the coverage. This method returns always the geophysics
     * version of data (<code>{@linkplain GridCoverage2D#geophysics geophysics}(true)</code>).
     * <p>
     * If the coverage has already been read previously and has not yet been reclaimed by the
     * garbage collector, then the existing coverage is returned immediately.
     * <p>
     * Some implementation may use RMI (<cite>Remote Method Invocation</cite>). In such cases, this
     * method will clip the coverage and apply operation (if any) on the remove machine before to
     * send the results through the network. The image quality may be degraded for more compact
     * transmission over the network.
     *
     * @param  listeners Objects to inform about progress, or {@code null} if none.
     * @return The coverage, {@code null} if the user {@linkplain #abort aborted} the process.
     * @throws IOException if an error occured while reading the image.
     * @throws IIOException if there is no reader for the coverage, of if the file is not valid
     *         for the expected format.
     * @throws RemoteException if an error occured while communicating with a remote server.
     *
     * @todo Should probable thrown an exception instead of returning null when the reading is aborted.
     */
    @Override
    GridCoverage2D getCoverage(IIOListeners listeners) throws IOException;

    /**
     * Abort the image reading. This method can be invoked from any thread. If {@link #getCoverage
     * getCoverage(...)} was in progress at the time this method is invoked, then it will stop and
     * returns {@code null}.
     */
    void abort();


    /**
     * A coverage reference that delegate its work to an other instance of {@link CoverageReference}.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static class Proxy extends net.seagis.catalog.Proxy implements CoverageReference {
        /**
         * For cross-version compatibility.
         */
        private static final long serialVersionUID = 1679051552440633120L;

        /**
         * The backing reference on which to delegate the work.
         */
        private final CoverageReference ref;

        /**
         * Creates a proxy which will delegates its work to the specified object.
         */
        protected Proxy(final CoverageReference ref) {
            this.ref = ref;
            if (ref == null) {
                throw new NullPointerException();
            }
        }

        public @Override CoverageReference         getBackingElement()            {return ref;}
        public @Override Series                    getSeries()                    {return ref.getSeries();}
        public @Override File                      getFile()                      {return ref.getFile();}
        public @Override URL                       getURL()                       {return ref.getURL();}
        public @Override GridGeometry2D            getGridGeometry()              {return ref.getGridGeometry();}
        public @Override CoordinateReferenceSystem getCoordinateReferenceSystem() {return ref.getCoordinateReferenceSystem();}
        public @Override Envelope                  getEnvelope()                  {return ref.getEnvelope();}
        public @Override NumberRange               getZRange()                    {return ref.getZRange();}
        public @Override DateRange                 getTimeRange()                 {return ref.getTimeRange();}
        public @Override GeographicBoundingBox     getGeographicBoundingBox()     {return ref.getGeographicBoundingBox();}
        public @Override SampleDimension[]         getSampleDimensions()          {return ref.getSampleDimensions();}
        public @Override void                      abort()                        {       ref.abort();}
        public @Override GridCoverage2D getCoverage(final IIOListeners listeners) throws IOException {
            return ref.getCoverage(listeners);
        }
    }
}
