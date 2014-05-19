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
