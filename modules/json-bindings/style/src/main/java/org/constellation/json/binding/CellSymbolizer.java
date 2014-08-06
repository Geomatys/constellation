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

import org.geotoolkit.style.MutableRule;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.constellation.json.util.StyleUtilities.type;

/**
 * This is the json binding class for CellSymbolizer.
 *
 * @author Mehdi Sidhoum (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public class CellSymbolizer implements Symbolizer {

    private static final long serialVersionUID = 1L;

    private String name;

    private int cellSize = 20;

    private Rule rule;

    public CellSymbolizer(){}

    public CellSymbolizer(final org.geotoolkit.display2d.ext.cellular.CellSymbolizer symbolizer){
        ensureNonNull("symbolizer", symbolizer);

        name = symbolizer.getName();
        cellSize = symbolizer.getCellSize();
        final MutableRule mutableRule = (MutableRule)symbolizer.getRule();
        rule = new Rule(mutableRule);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCellSize() {
        return cellSize;
    }

    public void setCellSize(int cellSize) {
        this.cellSize = cellSize;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    @Override
    public org.opengis.style.Symbolizer toType() {
        return new org.geotoolkit.display2d.ext.cellular.CellSymbolizer(cellSize,type(rule));
    }
}
