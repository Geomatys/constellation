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


import org.geotoolkit.cql.CQL;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.constellation.json.util.StyleFactories.FF;
import static org.constellation.json.util.StyleUtilities.*;

/**
 * @author Cédric Briançon (Geomatys)
 */
public class PieSymbolizer implements Symbolizer {
    private String name;
    private Double size;
    private String group;
    private String value;
    private String quarter;
    private List<ColorQuarter> colorQuarters = new ArrayList<>();

    public PieSymbolizer() {}

    public PieSymbolizer(org.geotoolkit.display2d.ext.pie.PieSymbolizer symbolizer) {
        ensureNonNull("symbolizer", symbolizer);
        name = symbolizer.getName();
        if (symbolizer.getSize() != null) {
            size = symbolizer.getSize().evaluate(null, Double.class);
        }
        if (symbolizer.getGroup() != null) {
            group = CQL.write(symbolizer.getGroup());
        }
        if (symbolizer.getValue() != null) {
            value = CQL.write(symbolizer.getValue());
        }
        if (symbolizer.getQuarter() != null) {
            quarter = CQL.write(symbolizer.getQuarter());
        }
        if (symbolizer.getColorQuarters() != null) {
            for (final org.geotoolkit.display2d.ext.pie.PieSymbolizer.ColorQuarter colorQuarter : symbolizer.getColorQuarters()) {
                final ColorQuarter colorQuarterToAdd = new ColorQuarter();
                final Color color = colorQuarter.getColor().evaluate(null, Color.class);
                final String colorHex = String.format("#%06X", (0xFFFFFF & color.getRGB()));
                colorQuarterToAdd.setColor(colorHex);
                colorQuarterToAdd.setQuarter(colorQuarter.getQuarter().evaluate(null, String.class));
                colorQuarters.add(colorQuarterToAdd);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getSize() {
        return size;
    }

    public void setSize(Double size) {
        this.size = size;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    public List<ColorQuarter> getColorQuarters() {
        return colorQuarters;
    }

    public void setColorQuarters(List<ColorQuarter> colorQuarters) {
        this.colorQuarters = colorQuarters;
    }

    @Override
    public org.opengis.style.Symbolizer toType() {
        final org.geotoolkit.display2d.ext.pie.PieSymbolizer symb = new org.geotoolkit.display2d.ext.pie.PieSymbolizer();
        symb.setName(name);
        symb.setSize(FF.literal(size));
        symb.setGroup(FF.property(group));
        symb.setValue(FF.property(value));
        symb.setQuarter(FF.property(quarter));
        for (final ColorQuarter colorQuarter : colorQuarters) {
            final org.geotoolkit.display2d.ext.pie.PieSymbolizer.ColorQuarter colorQuart =
                    new org.geotoolkit.display2d.ext.pie.PieSymbolizer.ColorQuarter();
            colorQuart.setQuarter(FF.literal(colorQuarter.getQuarter()));
            colorQuart.setColor(FF.literal(colorQuarter.getColor()));
            symb.getColorQuarters().add(colorQuart);
        }
        return symb;
    }
}
