/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2008, Geomatys
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
package net.seagis.coverage.web;


/**
 * The type of an image.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
enum ImageType {
    /**
     * Image is a coverage, i.e. sample values have a geophysics meaning.
     */
    COVERAGE,

    /**
     * Image is designed for viewing only, i.e. sample values don't need to have a geophysics meaning.
     */
    IMAGE,

    /**
     * Image is a legend.
     */
    LEGEND;
}
