/*
 * Constellation - An open source and standard compliant SDI
 * http://www.constellation-sdi.org
 *
 * Copyright 2015 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import org.geotoolkit.display2d.ext.dynamicrange.DynamicRangeSymbolizer;

import java.io.Serializable;

/**
 * Created by bgarcia on 27/02/15.
 */
public class DynamicRangeBounds implements Serializable {

    private String value;

    public DynamicRangeBounds() {
    }

    public DynamicRangeBounds(DynamicRangeSymbolizer.DRBound lower) {
        value = CQL.write(lower.getValue());
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public DynamicRangeSymbolizer.DRBound toType(){
        DynamicRangeSymbolizer.DRBound bound = new DynamicRangeSymbolizer.DRBound();
        bound.setMode(DynamicRangeSymbolizer.DRBound.MODE_EXPRESSION);
        bound.setValue(StyleUtilities.parseExpression(value));
        return bound;
    }
}
