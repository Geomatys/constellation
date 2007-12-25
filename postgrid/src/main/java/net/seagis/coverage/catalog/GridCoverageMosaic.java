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
import java.util.ArrayList;
import java.util.Collections;
import java.awt.Rectangle;
import java.net.URI;
import java.io.File;
import java.io.IOException;
import javax.imageio.IIOException;
import javax.imageio.ImageReader;

import org.opengis.metadata.extent.GeographicBoundingBox;
import org.geotools.resources.Utilities;
import org.geotools.image.io.mosaic.Tile;
import org.geotools.image.io.mosaic.TileBuilder;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.coverage.grid.ImageGeometry;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;

import net.seagis.catalog.CatalogException;


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
    private GridCoverageMosaic(final GridCoverageEntry reference, final ImageGeometry geometry,
                               final GeneralEnvelope envelope, final GeographicBoundingBox bbox)
    {
        super(reference, new GridGeometryEntry("Mosaic", geometry, envelope, bbox, reference.geometry));
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
     * Given a list of coverage entries <strong>with the same time range</strong>, checks if some
     * of them are tiles in a mosaic. If such tiles are found, this method assembles them in a
     * bigger {@code GridCoverageEntry}.
     *
     * @param  entries A list of entries at the same time range.
     *         <strong>This list will be cleared for working purpose</strong>.
     * @return A new list with either the same entries, or some bigger entries made of tiles.
     * @throws CatalogException if an error occured while creating the mosaic.
     */
    public static List<GridCoverageEntry> createMosaic(final List<GridCoverageEntry> entries)
            throws CatalogException
    {
        final List<GridCoverageEntry> mosaics = new ArrayList<GridCoverageEntry>(entries.size());
        TileBuilder                   tiles   = null; // 'tiles' and 'readers' will be created
        Map<FormatEntry,ImageReader>  readers = null; // together when first needed (if they are).
        Iterator<GridCoverageEntry>   iterator;
        while ((iterator = entries.iterator()).hasNext()) {
            final GridCoverageEntry reference = iterator.next();
            final GeneralEnvelope envelope = reference.geometry.getEnvelope(); // Need full envelope, not the clipped one.
            final GeographicBoundingBoxImpl bbox = reference.geometry.getGeographicBoundingBox();
            iterator.remove();
            while (iterator.hasNext()) {
                final GridCoverageEntry candidate = iterator.next();
                assert Utilities.equals(reference.getTimeRange(), candidate.getTimeRange());
                if (reference.geometry.canMosaic(candidate.geometry)) {
                    /*
                     * Found at least 2 entries that may be part of a mosaic. Builds required
                     * structures only now (if not yet done). The tiles candidates are put in
                     * the 'tiles' temporary collection, and we keep trace of the ImageReader
                     * created in order to use the same instance for a whole mosaic.
                     */
                    if (tiles == null) {
                        tiles = new TileBuilder();
                        readers = new HashMap<FormatEntry,ImageReader>();
                    }
                    if (tiles.isEmpty()) { // May be true more than once.
                        assert readers.isEmpty();
                        add(tiles, reference, readers);
                    }
                    add(tiles, candidate, readers);
                    assert !readers.isEmpty();
                    candidate.geometry.addTo(envelope, bbox);
                    iterator.remove();
                } else {
                    // Do not remove the entry, since we want to examine it
                    // again in an other pass of the outer 'while' loop.
                }
            }
            /*
             * At this point, either 'tiles' is empty (in which case we must put ourself the
             * examined reference in the list to be returned), or either we have a collection
             * of at least 2 tiles that need further processing. In the later case, all entries
             * should have the same time range and equals CRS (ignoring metadata). Computes the
             * pyramid levels now (this is TileBuilder's job) and stores the result (usually a
             * singleton - but more complex mosaics are allowed) in the list to be returned.
             */
            if (tiles == null || tiles.isEmpty()) {
                mosaics.add(reference);
            } else {
                for (final Map.Entry<ImageGeometry,Tile[]> entry : tiles.tiles().entrySet()) {
                    mosaics.add(new GridCoverageMosaic(reference, entry.getKey(), envelope, bbox));
                }
                tiles.clear();   // Make it ready for building new mosaics.
                readers.clear(); // A different mosaic must use different ImageReader instances.
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
     * @throws CatalogException If an error occured while fetching the input.
     */
    private static void add(final TileBuilder tiles, final GridCoverageEntry entry,
                            final Map<FormatEntry,ImageReader> readers)
            throws CatalogException
    {
        final FormatEntry format = (FormatEntry) entry.getSeries().getFormat();
        ImageReader reader = readers.get(format);
        if (reader == null) {
            try {
                reader = format.createImageReader();
            } catch (IIOException exception) {
                throw new CatalogException(exception);
            }
            readers.put(format, reader);
        }
        // We needs the full bounds, not the clipped ones.
        Rectangle region = entry.geometry.getBounds();
        tiles.setImageReader(reader);
        final Object input;
        try {
            input = entry.getInput();
        } catch (IOException exception) {
            throw new CatalogException(exception);
        }
        tiles.add(input, entry.imageIndex, region, entry.geometry.getGridToCRS2D());
    }
}
