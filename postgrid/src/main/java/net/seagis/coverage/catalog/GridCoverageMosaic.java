/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
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

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.net.URI;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageReader;

import org.opengis.coverage.grid.GridRange;
import org.geotools.resources.Utilities;
import org.geotools.image.io.mosaic.Tile;
import org.geotools.image.io.mosaic.TileBuilder;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.coverage.grid.ImageGeometry;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;


/**
 * A grid coverage entry made from a mosaic of other entries.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class GridCoverageMosaic extends GridCoverageEntry {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 2950751064046936487L;

    /**
     * Creates a new grid coverage mosaic from the specified entries.
     */
    protected GridCoverageMosaic(final GridCoverageEntry reference, final GridGeometryEntry geometry) {
        super(reference, geometry);
    }

    /**
     * Returns the input to be given to the image reader.
     */
    @Override
    protected Tile[] getInput() {
        return null;
    }

    /**
     * Returns {@code null} since the image is not accessible through a single file.
     */
    @Override
    public File getFile() {
        return null;
    }

    /**
     * Returns {@code null} since the image is not accessible through a single URI.
     */
    @Override
    public URI getURI() {
        return null;
    }

    /**
     *
     */
    private static List<GridCoverageEntry> createMosaic(final List<GridCoverageEntry> entries)
            throws IOException
    {
        TileBuilder                  tiles   = null;
        List<GridCoverageEntry>      mosaics = null;
        Map<FormatEntry,ImageReader> readers = null;
        Iterator<GridCoverageEntry>  iterator;
        while ((iterator = entries.iterator()).hasNext()) {
            final GridCoverageEntry reference = iterator.next();
            final GeneralEnvelope envelope = reference.geometry.getEnvelope(); // Need full envelope, not the clipped one.
            final GeographicBoundingBoxImpl bbox = new GeographicBoundingBoxImpl(reference.geometry.geographicEnvelope);
            iterator.remove();
            while (iterator.hasNext()) {
                final GridCoverageEntry candidate = iterator.next();
                assert Utilities.equals(reference.getTimeRange(), candidate.getTimeRange());
                if (reference.geometry.canMosaic(candidate.geometry)) {
                    if (tiles == null) {
                        tiles = new TileBuilder();
                    }
                    if (tiles.isEmpty()) {
                        if (readers == null) {
                            readers = new HashMap<FormatEntry,ImageReader>();
                        }
                        add(tiles, reference, readers);
                    }
                    add(tiles, candidate, readers);
                    envelope.add(candidate.geometry.envelope);
                    bbox.add(new GeographicBoundingBoxImpl(candidate.geometry.geographicEnvelope));
                    iterator.remove();
                }
            }
            /*
             * At this point, we have a collection of entries at the same time range and
             * using compatible CRS. Computes the pyramid levels now.
             */
            if (tiles != null && !tiles.isEmpty()) {
                for (final Map.Entry<ImageGeometry,Tile[]> entry : tiles.tiles().entrySet()) {
                    final ImageGeometry   geometry  = entry.getKey();
                    final AffineTransform gridToCRS = geometry.getGridToCRS();
                    final GridRange       gridRange = geometry.getGridRange();
                    mosaics.add(new GridCoverageMosaic(reference, new GridGeometryEntry(
                            "Mosaic", gridToCRS, gridRange, envelope, bbox, null)));
                }
                tiles.clear();
            } else {
                mosaics.add(reference);
            }
            if (readers != null) {
                readers.clear();
            }
        }
        return mosaics;
    }

    /**
     * Adds an entry to a tile collection.
     *
     * @param  tiles   The tile collection to add to.
     * @param  entry   The grid coverage entry to add.
     * @param  readers A pool of pre-allocated readers.
     * @throws IOException If an error occured while fetching the input.
     */
    private static void add(final TileBuilder tiles, final GridCoverageEntry entry,
                            final Map<FormatEntry,ImageReader> readers)
            throws IOException
    {
        final FormatEntry format = (FormatEntry) entry.getSeries().getFormat();
        ImageReader reader = readers.get(format);
        if (reader == null) {
            reader = format.createImageReader();
            readers.put(format, reader);
        }
        // We needs the full bounds, not the clipped ones.
        Rectangle region = entry.geometry.getBounds();
        tiles.setImageReader(reader);
        tiles.add(entry.getInput(), entry.imageIndex, region, entry.geometry.getGridToCRS2D());
    }
}
