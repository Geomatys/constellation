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

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.constellation.json.util.StyleFactories.SF;
import static org.constellation.json.util.StyleUtilities.literal;
import static org.constellation.json.util.StyleUtilities.type;

/**
 * pojo binding for Halo used for text symbolizer.
 *
 * @author Mehdi Sidhoum (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public class Halo implements StyleElement<org.opengis.style.Halo>{
    private double radius = 1.0;
    private Fill fill = new Fill();

    public Halo(){}

    public Halo(final org.opengis.style.Halo halo){
        ensureNonNull("halo", halo);
        try{
            radius = Double.parseDouble(halo.getRadius().evaluate(null, String.class));
        }catch(Exception ex){
            //do nothing
        }
        if(halo.getFill() != null){
            fill = new Fill(halo.getFill());
        }
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        try{
            this.radius = Double.parseDouble(radius);
        }catch(Exception ex){
            //do nothing
        }
    }

    public Fill getFill() {
        return fill;
    }

    public void setFill(Fill fill) {
        this.fill = fill;
    }

    @Override
    public org.opengis.style.Halo toType() {
        return SF.halo(type(fill), literal(radius));
    }

}
