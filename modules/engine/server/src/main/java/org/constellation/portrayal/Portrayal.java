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

package org.constellation.portrayal;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.constellation.provider.LayerDetails;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.style.MutableStyle;


/**
 * Data structures used to simply method calls in the portrayal system.
 * 
 * @author Adrian Custer (Geomatys)
 * @since 0.3
 *
 */
public final class Portrayal {
	
	/**
	 * Data structure to define the parameters of a 'view'.
	 * <p>
	 * Constellation currently only handles two dimensional portrayal so this 
	 * data structure is limited to the needs of such a portrayal system. The 
	 * view definition contains:
	 * <ol>
	 *   <li><b>Envelope:</b> The extent of the scene.</li>
	 *   <li><b>Azimuth :</b> The angle from true North for the scene.</li>
	 * </ol>
	 * </p>
	 * <p>
	 * The view can be passed to the portrayal system for a portrayal request.
	 * </p>
	 * 
	 * @author Adrian Custer (Geomatys)
	 * @since 0.3
	 *
	 */
	public final static class ViewDef{
		
		/** The extent of interest. */
		public final ReferencedEnvelope envelope;
		/** The azimuth. TODO: what is this really?*/
		public final double azimuth;
		
		
		public ViewDef(ReferencedEnvelope env,
				       double azimuth){
			
			assert( null != env);
			//TODO: validate value of azimuth.
			
			this.envelope = env;
			this.azimuth  = azimuth;
		}

        @Override
        public String toString() {
            return "ViewDef[envelope=" + envelope +", azimuth=" + azimuth + "]";
        }
	}
	
	
	public final static class CanvasDef{
		public final Dimension dimension;
		public final Color background;
		public CanvasDef(Dimension dimension, Color background){
			assert( null != dimension );
			this.dimension = dimension;
			this.background = background;
		}

        @Override
        public String toString() {
            return "CanvasDef[dimension=" + dimension + ", background=" + background +"]";
        }
	}
	
	
	public final static class SceneDef{
		
		public final List<LayerDetails> layerRefs = new ArrayList<LayerDetails>();
		public final List<MutableStyle> styleRefs = new ArrayList<MutableStyle>();
        //Hold some extra parameters, like dimensions and DIM_RANGE styles.
		public final Map<String, Object> renderingParameters = new HashMap<String, Object>();
		
		public SceneDef( List<LayerDetails> layerRefs, 
				         List<MutableStyle> styleRefs,
				         Map<String, Object> renderingParameters){
			if(layerRefs.size() == 0){
                throw new IllegalArgumentException("A scene definition must have at least one layer.");
            }

            this.layerRefs.addAll(layerRefs);
            this.styleRefs.addAll(styleRefs);
            this.renderingParameters.putAll(renderingParameters);
		}

		public SceneDef( LayerDetails layerRef, 
				         MutableStyle styleRef,
				         Map<String, Object> renderingParameters){
			if(layerRef == null){
                throw new NullPointerException("The scene's layer is not defined.");
            }
			
			this.layerRefs.add(layerRef);
			this.styleRefs.add(styleRef);
            this.renderingParameters.putAll(renderingParameters);
		}

	}

}
