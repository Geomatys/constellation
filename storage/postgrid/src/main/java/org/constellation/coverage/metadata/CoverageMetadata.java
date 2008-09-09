/*
 * Ecocast - NASA Ames Research Center
 * (C) 2008, Ecocast
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
package org.constellation.coverage.metadata;


import org.constellation.catalog.Element;


/**
 * Interface for {@linkplain CoverageMetadataEntry coverage metadata entries}.
 *
 * @author Sam Hiatt
 * @version $Id$
 * 
 * TODO: implement methods for grabbing specific metadats
 *
 */
public interface CoverageMetadata extends Element {
    /**
     * Returns the metadata for this coverage. 
     * 
     * For starters, just dump all the metadata to a string.
     * 
     * @return The metadata for this layer, or {@code null} if unknown.
     * 
     */
    String getMetadata();

}
