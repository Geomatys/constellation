/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.lucene.filter;

/**
 *
 * @author guilhem
 */
public enum SpatialFilterType {

    CONTAINS,
    INTERSECTS,
    EQUALS,
    DISJOINT,
    BBOX,
    BEYOND,
    CROSSES,
    DWITHIN,
    WITHIN,
    TOUCHES,
    OVERLAPS,
}
