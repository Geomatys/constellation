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
import static org.constellation.json.util.StyleUtilities.type;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class PointSymbolizer implements Symbolizer {
    private String name;

    private Graphic graphic = new Graphic();

    public PointSymbolizer() {
    }

    public PointSymbolizer(org.opengis.style.PointSymbolizer symbolizer) {
        ensureNonNull("symbolizer", symbolizer);
        name = symbolizer.getName();
        if (symbolizer.getGraphic() != null) {
            graphic = new Graphic(symbolizer.getGraphic());
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Graphic getGraphic() {
        return graphic;
    }

    public void setGraphic(final Graphic graphic) {
        this.graphic = graphic;
    }

    @Override
    public org.opengis.style.Symbolizer toType() {
        return SF.pointSymbolizer(name, (String)null, null, null, type(graphic));
    }
}
