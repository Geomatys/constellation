/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.security;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.constellation.map.ws.AbstractWMSWorker;
import org.constellation.query.wms.DescribeLayer;
import org.constellation.query.wms.GetCapabilities;
import org.constellation.query.wms.GetFeatureInfo;
import org.constellation.query.wms.GetLegendGraphic;
import org.constellation.query.wms.GetMap;
import org.constellation.query.wms.WMSQuery;
import org.constellation.wms.AbstractWMSCapabilities;
import org.constellation.wms.v111.WMT_MS_Capabilities;
import org.constellation.wms.v130.WMSCapabilities;
import org.constellation.ws.ExceptionCode;
import org.constellation.ws.WebServiceException;
import org.geotools.display.exception.PortrayalException;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.internal.jaxb.v110.sld.DescribeLayerResponseType;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * A WMS worker for a security Policy Enforcement Point (PEP) gateway following 
 * the OASIS XACML model; this class performs most of the logic of the gateway.
 * <p>
 * This worker takes the request issued by the REST and SOAP server facades, 
 * ensures an Access Control decision is made based on both the security 
 * credentials of the requester and the parameters of the request, and then 
 * either performs the request or denies it depending on the Access Control 
 * decision.
 * </p>
 * <p>
 * <b>WARNING:</b> This class is still experimental and not behaving correctly. 
 * Using it in production is sure to void your warranty, shorten your life, and 
 * increase the likelihood that meteorites will fall on your home.
 *</p>
 *
 * @version $Id$
 * 
 * @author Cédric Briançon (Geomatys)
 * @author Adrian Custer (Geomatys)
 * @since 0.3
 */
public final class WmsSecuredWorker extends AbstractWMSWorker {
	
    /**
     * The default logger.
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.security");
    
    /**
     * The url of the WMS web service, where the request will be sent.
     */
    private final String WMSbaseURL = "http://solardev:8280/constellation/WS/wms";

    /**
     * Defines whether it is a REST or SOAP request.
     */
    private final boolean WMSusesREST = true;

    /**
     * The dispatcher which will receive the request generated.
     */
    private final WmsDispatcher dispatcherWMS;

    /**
     * The marshaller of the result given by the service.
     * <p>
     * NB this is the marshaller for the service presented as a facade, not 
     * necessarily for any of the clients.
     * </p>
     */
    private final Marshaller marshaller;

    /**
     * The unmarshaller of the result given by the service.
     * <p>
     * NB this is the marshaller for the service presented as a facade, not 
     * necessarily for any of the clients.
     * </p>
     */
    private final Unmarshaller unmarshaller;


    /**
     * Builds a {@code GetCapabilities} request.
     *
     * @param marshaller
     * @param unmarshaller
     * @throws IOException if an error occurs at the URL creation.
     */
    public WmsSecuredWorker(final Marshaller marshaller, final Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
        this.marshaller   = marshaller;

        dispatcherWMS     = new WmsDispatcher(WMSbaseURL, WMSusesREST, marshaller, unmarshaller);
        //dispatcherXACML = new 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DescribeLayerResponseType describeLayer(final DescribeLayer descLayer)
            throws WebServiceException
    {
        performAccessControlDecision(descLayer);

        return dispatcherWMS.describeLayer(descLayer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractWMSCapabilities getCapabilities(final GetCapabilities getCapabilities)
            throws WebServiceException {
    	
    	//TODO: getCaps doesn't follow this pattern.
        performAccessControlDecision(getCapabilities);
        
    	AbstractWMSCapabilities response = dispatcherWMS.getCapabilities(getCapabilities);
        response = removeCapabilitiesInfo(response);
        response = addAccessConstraints(response);
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFeatureInfo(GetFeatureInfo getFeatureInfo) throws WebServiceException {
        performAccessControlDecision(getFeatureInfo);

        return dispatcherWMS.getFeatureInfo(getFeatureInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedImage getLegendGraphic(GetLegendGraphic getLegend) throws WebServiceException {
        performAccessControlDecision(getLegend);

        return dispatcherWMS.getLegendGraphic(getLegend);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedImage getMap(GetMap getMap) throws WebServiceException {
    	
    	performAccessControlDecision(getMap);
    	
    	//INFO BLOCK
    	String layerList = "{";
    	for (String layerName : getMap.getLayers() ){
            layerList = new String(layerList+" "+ layerName);
    	}
    	LOGGER.info("WMS-sec: WMS request asks for layers: "+layerList+" }");
    	
    	//Filter block
    	if (true){
    		
    		//TODO: get the source of the clip geometry, using the hard coordinates
	    	
	    	//MAKE A CLIP MASK
    		//Coordinates are in lat/long order!
	    	//final double [] coords = new double[]{0.0,0.0, 20.0,0.0, 30.0,30.0, 20.0,40.0, 0.0,40.0, 0.0,0.0};
            //Coordinates for the OWS-6 Airport scenario
//            final double [] coords = new double[]{29.93,-90.03, 29.94,-90.01, 29.96,-90.01, 29.965,-90.02, 29.96,-90.03, 29.93,-90.03};
            final double [] coords = new double[]{29.900,-90.000, 29.905,-90.012, 29.916,-90.002, 29.924,-89.980, 29.908,-89.978, 29.900,-90.000};
	    	CoordinateReferenceSystem crs = null;
	    	try {
				crs = CRS.decode("EPSG:4236");
			} catch (NoSuchAuthorityCodeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			FeatureCollection<SimpleFeatureType,SimpleFeature> fc = ImageUtilities.createClipFeatureCollection(crs, coords);
			
			BufferedImage mask = null;
			try {
				mask = ImageUtilities.createMask(new ReferencedEnvelope(getMap.getEnvelope()),getMap.getSize(),fc);
			} catch (MismatchedDimensionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PortrayalException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			LOGGER.info("WMS-sec: Made a mask of width: "+mask.getWidth()+" and height: "+mask.getHeight());
			
			
			//DEFINE THE LAYERS TO CLIP
			//TODO: This should be role based representation control
			List<String> layersToClip = new ArrayList<String>();
	    	layersToClip.add("ROAD_C_City");
			
	    	
	    	//GET EACH LAYER, CLIP IF NEEDED, AND COMBINE TO RESULT
	    	BufferedImage result = null;
            getMap = new GetMap(getMap, true);//Add transparency
	    	for (String layerName : getMap.getLayers() ){
	    		
	    		//GET LAYER
	    		BufferedImage bi = dispatcherWMS.getMap(new GetMap(getMap,layerName));
	    		assert (null != bi);//we should have thrown an error.
	    		//CLIP
	    		if (layersToClip.contains(layerName)){
	    			bi = ImageUtilities.applyMask(bi,mask);
	    			LOGGER.info("WMS-sec: Clipped layer: "+layerName+" at "+System.currentTimeMillis() );
	    		}
	    		
	    		//COMBINE
	    		if (null== result){
	    			result = bi;
	    		} else {
	    			result = ImageUtilities.combine(result, bi);
	    		}
	    		LOGGER.info("WMS-sec: Added layer: "+layerName+" at "+System.currentTimeMillis() );
	    	}
			
	    	return result;
    	}
    	
        return dispatcherWMS.getMap(getMap);
    }
    
    
    
    
    /**
     * 
     * @param query
     */
    private void performAccessControlDecision(WMSQuery query){
    	;
    }

    /**
     * Removes the {@code <Capability>} block from a WMS Capability object, such 
     * as the object created by unmarshalling an XML document returned by a 
     * separate WMS server.
     *
     * @param wmsCaps A WMS Capability object conformant to one of the types
     *                  supported by this facade, possibly created from an 
     *                  XML response from a separate service and unmarshalled 
     *                  into a Java WMS Capabilities object.
     * @return The WMS Capabilities object without its {@code <Capability>}
     *           block.
     * @throws WebServiceException if the given WMS Capabilities type does not 
     *                               match the types supported by this facade.
     */
    private AbstractWMSCapabilities removeCapabilitiesInfo(final Object wmsCaps) throws WebServiceException {
        if (wmsCaps instanceof WMT_MS_Capabilities) {
            final WMT_MS_Capabilities cap = (WMT_MS_Capabilities) wmsCaps;
            org.constellation.wms.v111.Capability capability =
                    new org.constellation.wms.v111.Capability(null, null, null, null);
            cap.setCapability(capability);
            return cap;
        }
        if (wmsCaps instanceof WMSCapabilities) {
            final WMSCapabilities cap = (WMSCapabilities) wmsCaps;
            org.constellation.wms.v130.Capability capability =
                    new org.constellation.wms.v130.Capability(null, null, null);
            cap.setCapability(capability);
            return cap;
        }
        throw new WebServiceException("Capabilities response is not valid, because it does not match" +
                " with JAXB classes.", ExceptionCode.NO_APPLICABLE_CODE);
    }

    /**
     * Adds an {@code <AccessConstraints>} block to a WMS Capabilities object to 
     * indicate the Access Control requirements for access to the OGC service 
     * protected by this gateway.
     *
     * @param wmsCaps A WMS Capability object conformant to one of the types
     *                  supported by this facade.
     * @return The WMS Capabilities object with an {@code <AccessConstraints>} 
     *           block appropriate for the OGC service protected by this gateway.
     * @throws WebServiceException if the given WMS Capabilities type does not 
     *                               match the types supported by this facade.
     */
    private AbstractWMSCapabilities addAccessConstraints(final Object wmsCaps) throws WebServiceException {
        if (wmsCaps instanceof WMT_MS_Capabilities) {
            final WMT_MS_Capabilities cap = (WMT_MS_Capabilities) wmsCaps;
            cap.getService().setAccessConstraints("Require an authentication !");
            return cap;
        }
        if (wmsCaps instanceof WMSCapabilities) {
            final WMSCapabilities cap = (WMSCapabilities) wmsCaps;
            cap.getService().setAccessConstraints("Require an authentication !");
            return cap;
        }
        throw new WebServiceException("Capabilities response is not valid, because it does not match" +
                " with JAXB classes.", ExceptionCode.NO_APPLICABLE_CODE);
    }
}

