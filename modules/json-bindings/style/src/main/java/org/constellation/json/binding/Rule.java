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

import org.constellation.json.util.StyleUtilities;
import org.geotoolkit.cql.CQL;
import org.geotoolkit.style.MutableRule;
import org.opengis.filter.PropertyIsLike;

import java.util.ArrayList;
import java.util.List;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.constellation.json.util.StyleFactories.SF;
import static org.constellation.json.util.StyleUtilities.filter;
import static org.constellation.json.util.StyleUtilities.listType;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class Rule implements StyleElement<MutableRule> {

    private String name                  = "Change me!";
    private String title                 = "";
    private String description           = "";
    private double minScale              = 0.0;
    private double maxScale              = Double.MAX_VALUE;
    private List<Symbolizer> symbolizers = new ArrayList<Symbolizer>(0);
    private String filter                = null;

    public Rule() {
    }

    public Rule(final MutableRule rule) {
        ensureNonNull("rule", rule);
        this.name = rule.getName();
        if (rule.getDescription() != null) {
            if (rule.getDescription().getTitle() != null) {
                this.title = rule.getDescription().getTitle().toString();
            }
            if (rule.getDescription().getAbstract() != null) {
                this.description = rule.getDescription().getAbstract().toString();
            }
        }
        this.minScale = rule.getMinScaleDenominator();
        this.maxScale = rule.getMaxScaleDenominator();
        for (final org.opengis.style.Symbolizer symbolizer : rule.symbolizers()) {
            if (symbolizer instanceof org.opengis.style.PointSymbolizer) {
                symbolizers.add(new PointSymbolizer((org.opengis.style.PointSymbolizer) symbolizer));
            } else if (symbolizer instanceof org.opengis.style.LineSymbolizer) {
                symbolizers.add(new LineSymbolizer((org.opengis.style.LineSymbolizer) symbolizer));
            } else if (symbolizer instanceof org.opengis.style.PolygonSymbolizer) {
                symbolizers.add(new PolygonSymbolizer((org.opengis.style.PolygonSymbolizer) symbolizer));
            } else if (symbolizer instanceof org.opengis.style.TextSymbolizer) {
                symbolizers.add(new TextSymbolizer((org.opengis.style.TextSymbolizer) symbolizer));
            } else if (symbolizer instanceof org.opengis.style.RasterSymbolizer) {
                symbolizers.add(new RasterSymbolizer((org.opengis.style.RasterSymbolizer) symbolizer));
            } else if (symbolizer instanceof org.geotoolkit.display2d.ext.cellular.CellSymbolizer) {
                symbolizers.add(new CellSymbolizer((org.geotoolkit.display2d.ext.cellular.CellSymbolizer) symbolizer));
            } else if (symbolizer instanceof org.geotoolkit.display2d.ext.pie.PieSymbolizer) {
                symbolizers.add(new PieSymbolizer((org.geotoolkit.display2d.ext.pie.PieSymbolizer)symbolizer));
            } else if (symbolizer instanceof org.geotoolkit.display2d.ext.dynamicrange.DynamicRangeSymbolizer){
                symbolizers.add(new DynamicRangeSymbolizer((org.geotoolkit.display2d.ext.dynamicrange.DynamicRangeSymbolizer) symbolizer));
            }
        }
        if (rule.getFilter() != null) {
            filter = StyleUtilities.toCQL(rule.getFilter());

            //for generated rules auto unique values we need to escape quotes.
            if(filter.contains("''") && !filter.endsWith("''") && rule.getFilter() instanceof PropertyIsLike){
                filter = filter.replaceAll("''","\\\\'");
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public double getMinScale() {
        return minScale;
    }

    public void setMinScale(final double minScale) {
        this.minScale = minScale;
    }

    public double getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(final double maxScale) {
        this.maxScale = maxScale;
    }

    public List<Symbolizer> getSymbolizers() {
        return symbolizers;
    }

    public void setSymbolizers(final List<Symbolizer> symbolizers) {
        this.symbolizers = symbolizers;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(final String filter) {
        this.filter = filter;
    }

    @Override
    public MutableRule toType() {
        return SF.rule(
            name,
            SF.description(title != null ? title : "", description != null ? description : ""),
            null,
            minScale,
            maxScale,
            listType(symbolizers),
            filter(filter));
    }
}
