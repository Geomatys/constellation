/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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

import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.util.Iterator;
import java.util.LinkedList;

import org.geotools.util.CanonicalSet;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GridCoverageFactory;


/**
 * A pool of {@link GridCoverageEntry} instances.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class GridCoveragePool {
    /**
     * The default instance.  A static shared instance for now, but we may revisit in a
     * future version (e.g. we could put that in {@link GridCoverageTable}, but we need
     * to think about {@link GridCoverageEntry} deserialization).
     */
    public static final GridCoveragePool DEFAULT = new GridCoveragePool();

    /**
     * Factory for creating new {@link GridCoverage2D} instances.
     */
    final GridCoverageFactory factory = CoverageFactoryFinder.getGridCoverageFactory(null);

    /**
     * Entries added by {@link GridCoverageEntry#unique} and not yet garbage collected.
     * {@link GridCoverageTable} will try to reuse existing entries as much as possible.
     */
    private final CanonicalSet<GridCoverageEntry> pool = CanonicalSet.newInstance(GridCoverageEntry.class);

    /**
     * Last entries on which {@link GridCoverageEntry#getCoverage} has been invoked. When a new
     * image is read, oldest soft references will be replaced by weak references in order to
     * increase the chance to be garbage-collected.
     */
    private final LinkedList<GridCoverageEntry> lastAllocated = new LinkedList<GridCoverageEntry>();

    /**
     * Maximal amount of memory (in bytes) allowed for all entries enumerated in {@link #lastAllocated}.
     * If more memory is used, then oldest images are removed from {@link #lastAllocated} until memory
     * usage of remaining images fall below this limit.
     */
    private final long memoryUsageThreshold = 128L * 1024 * 1024;

    /**
     * Sum of {@link GridGeometryEntry#getMemoryUsage memory usage} for all coverages in
     * {@link #lastAllocated}.
     */
    private long memoryUsageAllocated;

    /**
     * Creates a new, initially empty, pool.
     */
    private GridCoveragePool() {
    }

    /**
     * Returns a unique instance of the given entry. Entries are hold by weak references.
     */
    public GridCoverageEntry unique(final GridCoverageEntry entry) {
        return pool.unique(entry);
    }

    /**
     * Computes an estimation of memory usage for the given entry. If the sum of memory usage by
     * latest entries is over some threshold, replace oldest soft references by weak references
     * in order to increase the chances to be garbage collected.
     *
     * @param  entry The entry to measure.
     * @param  image The image created for the given entry.
     * @return An estimation of memory usage in bytes.
     */
    public long addMemoryUsage(final GridCoverageEntry entry, final RenderedImage image) {
        long memoryUsage = (long) image.getWidth() * (long) image.getHeight();
        memoryUsage *= DataBuffer.getDataTypeSize(image.getSampleModel().getDataType()) / Byte.SIZE;
        synchronized (lastAllocated) {
            memoryUsageAllocated += memoryUsage;
            /*
             * Iterates over all entries inconditionnaly, even if we are already below the
             * memory threshold, because we want to make sure that the list do not already
             * contains the given entry (we must avoid duplicated values).
             */
            for (final Iterator<GridCoverageEntry> it=lastAllocated.iterator(); it.hasNext();) {
                final GridCoverageEntry previous = it.next();
                if (previous != entry) {
                    if (memoryUsageAllocated <= memoryUsageThreshold) {
                        continue;
                    }
                    previous.clearSoftReference();
                }
                it.remove(); // We are over the memory threshold, or we found a duplicated entry.
                memoryUsageAllocated -= previous.getMemoryUsage();
            }
            lastAllocated.addLast(entry);
        }
        return memoryUsage;
    }
}
