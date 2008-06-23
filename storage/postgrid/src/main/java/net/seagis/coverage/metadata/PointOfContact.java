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
package net.seagis.coverage.metadata;


import net.seagis.catalog.Element;


/**
 * Interface for {@linkplain PointOfContactEntry layer metadata entries}.
 *
 * @author Sam Hiatt
 * @version $Id$
 * 
 * TODO: implement methods for grabbing specific metadats
 *
 */
public interface PointOfContact extends Element {
    /**
     * Returns the metadata for this layer. 
     * 
     * For starters, just dump all the metadata to a string.
     * 
     * @return The metadata for this layer, or {@code null} if unknown.
     * 
     */
    String getMetadata();

}
