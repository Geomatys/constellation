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

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.filter.DefaultLiteral;
import org.geotoolkit.style.StyleConstants;
import org.geotoolkit.style.function.ThreshholdsBelongTo;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

/**
 * @author Benjamin Garcia (Geomatys)
 *
 */
public class Categorize implements Function{

	private static final long serialVersionUID = 1L;

	private List<InterpolationPoint> points = new ArrayList<InterpolationPoint>();
	
	public Categorize() {
	}
	
	@SuppressWarnings("rawtypes")
	public Categorize(final org.geotoolkit.style.function.Categorize categorize){
		ensureNonNull("categorize", categorize);
		for (Expression expression : categorize.getThresholds().keySet()) {
			InterpolationPoint ip = new InterpolationPoint();
			Expression colorHex = categorize.getThresholds().get(expression);
			if(colorHex instanceof Color){
				ip.setColor(Integer.toHexString(((Color)colorHex).getRGB()).substring(2));
			}
			
			if(expression instanceof DefaultLiteral){
				Object obj = ((DefaultLiteral)expression).getValue();
				if(obj instanceof Double){
					ip.setData((Number)obj);
				}
			}
		}
	}


	public List<InterpolationPoint> getPoints() {
		return points;
	}




	public void setPoints(List<InterpolationPoint> points) {
		this.points = points;
	}




	@Override
	public org.opengis.filter.expression.Function toType() {
		
		Map<Expression, Expression> values = new HashMap<>();
        final FilterFactory ff = FactoryFinder.getFilterFactory(null);
		values.put(ff.literal(StyleConstants.CATEGORIZE_LESS_INFINITY), ff.literal(Color.GRAY));
		for (InterpolationPoint interpolationPoint : points) {
			Color c = Color.decode(interpolationPoint.getColor());
			values.put(new DefaultLiteral<Double>(interpolationPoint.getData().doubleValue()), new DefaultLiteral<Color>(c));
		}
		return SF.categorizeFunction(null, values, ThreshholdsBelongTo.PRECEDING, null);
	}
}
