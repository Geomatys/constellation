/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.constellation.map.ws;

import org.constellation.ws.MimeType;

/**
 * Holds some extra informations that are not contained in the query nor in the
 * uriInfo, HttpContext or ServletContext.
 *
 * @author Johann Sorel (Geomatys)
 */
public final class QueryContext {

    /**
     * Defines whether the exceptions should be stored and output in an image or not.
     */
    private boolean errorInimage = false;

    private boolean opaque = false;

    /**
     * Defines the image format for the exeception in image.
     */
    private String exceptionImageFormat = MimeType.IMAGE_PNG;

    public boolean isErrorInimage() {
        return errorInimage;
    }

    public void setErrorInimage(boolean errorInimage) {
        this.errorInimage = errorInimage;
    }

    public String getExceptionImageFormat() {
        return exceptionImageFormat;
    }

    public void setExceptionImageFormat(String exceptionImageFormat) {
        this.exceptionImageFormat = exceptionImageFormat;
    }

    public boolean isOpaque() {
        return opaque;
    }

    public void setOpaque(boolean opaque) {
        this.opaque = opaque;
    }

}
