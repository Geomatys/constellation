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

package org.constellation.json.binding;

import org.geotoolkit.filter.DefaultLiteral;

import java.awt.*;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.constellation.json.util.StyleFactories.SF;
import static org.constellation.json.util.StyleUtilities.literal;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class InterpolationPoint implements StyleElement<org.geotoolkit.style.function.InterpolationPoint> {

	private static final long serialVersionUID = 1L;

	private Number data  = null;
    private String color = "#000000";

    public InterpolationPoint() {
    }

    public InterpolationPoint(final org.geotoolkit.style.function.InterpolationPoint interpolationPoint) {
        ensureNonNull("interpolationPoint", interpolationPoint);
        double value = interpolationPoint.getData().doubleValue();
        if(Double.isNaN(value)){
            data = null;
        }else {
            data = Math.round(value*1000d)/1000d; //FIXME why?
        }
		if(interpolationPoint.getValue() instanceof DefaultLiteral){
			final Object obj = ((DefaultLiteral)interpolationPoint.getValue()).getValue();
			if(obj instanceof Color){
				color = "#"+Integer.toHexString(((Color)obj).getRGB()).substring(2);
			}
		}
    }

    public Number getData() {
        return data;
    }

    public void setData(final Number data) {
        this.data = data;
    }

    public String getColor() {
        return color;
    }

    public void setColor(final String color) {
        this.color = color;
    }

    @Override
    public org.geotoolkit.style.function.InterpolationPoint toType() {
        return SF.interpolationPoint(data, literal(color));
    }
}
