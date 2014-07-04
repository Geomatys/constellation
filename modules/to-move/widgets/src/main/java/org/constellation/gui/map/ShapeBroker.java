/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.gui.map;

// Geometry

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.IllegalPathStateException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


/**
 * A path iterator iterating only over a single polygon in a shape (i.e. all
 * segments until the next {@link #SEG_CLOSE} instruction). This iterator also
 * implements the {@link Shape} interface in order to allows it to be passed
 * as argument to {@link java.awt.Graphics2D#fill}.
 * <p>
 * Note: this class make some assumption about {@link java.awt.Graphics2D#fill}
 *       implementation.   We assume that {@code draw/fill} method invokes
 *       {@code getPathIterator(...)} only once,  and that the same flavor
 *       of {@code getPathIterator(...)} is invoked each time. If one of ours
 *       assumptions is false, {@link IllegalPathStateException} will be thrown.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class ShapeBroker implements Shape, PathIterator {
    /**
     * The underlying shape.
     */
    private final Shape shape;

    /**
     * {@code true} if the iterator is iterating through a flatenned fiew of the shape.
     */
    private boolean flat;

    /**
     * Tells if a {@link #SEG_CLOSE} instruction has been encounter during
     * iteration. In this case, iteration is considered finished.   Only a
     * portion of the underlying shape will have been drawn.
     */
    private boolean closed = true;

    /**
     * The iterator for the underlying shape. This iterator will
     * be constructed only the first time {@link #getPathIterator}
     * is invoked.
     */
    private PathIterator iterator;

    /**
     * Construct a {@code ShapeBroker} for the specified shape.
     */
    public ShapeBroker(final Shape shape) {
        this.shape = shape;
    }

    /**
     * Returns the shape's bounding box. This method
     * just forward the call to the underlying shape.
     */
    public Rectangle getBounds() {
        return shape.getBounds();
    }

    /**
     * Returns the shape's bounding box. This method
     * just forward the call to the underlying shape.
     */
    public Rectangle2D getBounds2D() {
        return shape.getBounds2D();
    }

    /**
     * Check the specified point for inclusion. This method
     * just forward the call to the underlying shape.
     */
    public boolean contains(double x, double y) {
        return shape.contains(x,y);
    }

    /**
     * Check the specified point for inclusion. This method
     * just forward the call to the underlying shape.
     */
    public boolean contains(Point2D point) {
        return shape.contains(point);
    }

    /**
     * Check the specified retangle for intersection. This
     * method just forward the call to the underlying shape.
     */
    public boolean intersects(double x, double y, double w, double h) {
        return shape.intersects(x,y,w,h);
    }

    /**
     * Check the specified retangle for intersection. This
     * method just forward the call to the underlying shape.
     */
    public boolean intersects(Rectangle2D rect) {
        return shape.intersects(rect);
    }

    /**
     * Check the specified retangle for inclusion. This
     * method just forward the call to the underlying shape.
     */
    public boolean contains(double x, double y, double w, double h) {
        return shape.contains(x,y,w,h);
    }

    /**
     * Check the specified retangle for inclusion. This
     * method just forward the call to the underlying shape.
     */
    public boolean contains(Rectangle2D rect) {
        return shape.contains(rect);
    }

    /**
     * Returns the winding rule. This method just
     * forward the call to the underlying iterator.
     */
    public int getWindingRule() {
        return iterator.getWindingRule();
    }

    /**
     * Test if there is no more polygon to drawn.
     */
    public boolean finished() {
        return iterator.isDone();
    }

    /**
     * Test if the iteration is completed. The iteration is considered completed when
     * a {@link #SEG_CLOSE} instruction is meets, event if the underlying shape still
     * has more points.   In this case, field {@link #closed} will have to bet set to
     * {@code false} before iteration can continue.
     */
    public boolean isDone() {
        return closed || iterator.isDone();
    }

    /**
     * Move to the next segment.
     */
    public void next() {
        if (!closed) {
            iterator.next();
        }
    }

    /**
     * Get coordinate points for the current segments. If this method returns
     * {@link #SEG_CLOSE}, then the next call to {@link #isDone()} will returns
     * {@code true} even if the underlying shape still has more points.
     */
    public int currentSegment(final float[] coords) {
        ensure(!closed);
        final int code=iterator.currentSegment(coords);
        closed = (code==SEG_CLOSE);
        return code;
    }

    /**
     * Get coordinate points for the current segments. If this method returns
     * {@link #SEG_CLOSE}, then the next call to {@link #isDone()} will returns
     * {@code true} even if the underlying shape still has more points.
     */
    public int currentSegment(final double[] coords) {
        ensure(!closed);
        final int code=iterator.currentSegment(coords);
        closed = (code==SEG_CLOSE);
        return code;
    }

    /**
     * Returns a path iterator for this shape. This method can be
     * invoked only once until the next {@link #SEG_CLOSE}.
     */
    public PathIterator getPathIterator(final AffineTransform at) {
        return getPathIterator(at, 0);
    }

    /**
     * Returns a path iterator for this shape. This method can be
     * invoked only once until the next {@link #SEG_CLOSE}.
     */
    public PathIterator getPathIterator(final AffineTransform at, final double flatness) {
        ensure(closed);
        final boolean isFlatnessSet = (flatness > 0);
        if (iterator == null) {
            iterator = (isFlatnessSet) ? shape.getPathIterator(at, flatness)
                                       : shape.getPathIterator(at);
            flat = isFlatnessSet;
        } else {
            ensure(flat == isFlatnessSet);
            iterator.next();
        }
        closed = false;
        return this;
    }

    /**
     * Make sure the specified condition is true.
     * Otherwise, throws a {@link IllegalPathStateException}.
     */
    private static void ensure(final boolean check) {
        if (!check) {
            throw new IllegalPathStateException();
        }
    }
}
