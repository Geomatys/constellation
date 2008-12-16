package org.constellation.security.wms;

import java.awt.image.BufferedImage;

import org.constellation.query.wms.DescribeLayer;
import org.constellation.query.wms.GetCapabilities;
import org.constellation.query.wms.GetFeatureInfo;
import org.constellation.query.wms.GetLegendGraphic;
import org.constellation.query.wms.GetMap;
import org.constellation.wms.AbstractWMSCapabilities;
import org.constellation.ws.WebServiceException;
import org.geotools.internal.jaxb.v110.sld.DescribeLayerResponseType;

public interface WmsClient {

    public DescribeLayerResponseType describeLayer(DescribeLayer descLayer) throws WebServiceException;
	
	public AbstractWMSCapabilities getCapabilities( GetCapabilities getCapabilities) throws WebServiceException ;
	
	public String getFeatureInfo(GetFeatureInfo getFeatureInfo) throws WebServiceException ;
	
	public BufferedImage getLegendGraphic(GetLegendGraphic getLegend) throws WebServiceException ;
	
	public BufferedImage getMap(GetMap getMap) throws WebServiceException ;
}
