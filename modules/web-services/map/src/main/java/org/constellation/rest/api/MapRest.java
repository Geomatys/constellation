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

package org.constellation.rest.api;

import org.apache.sis.util.logging.Logging;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.business.IDataBusiness;
import org.constellation.business.ILayerBusiness;
import org.constellation.business.IStyleBusiness;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerList;
import org.constellation.configuration.LayerSummary;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.dto.AddLayer;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.SimpleValue;
import org.constellation.security.SecurityManager;
import org.geotoolkit.ows.xml.v110.BoundingBoxType;
import org.geotoolkit.ows.xml.v110.WGS84BoundingBoxType;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.wmts.WMTSUtilities;
import org.geotoolkit.wmts.xml.v100.*;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.constellation.utils.RESTfulUtilities.ok;

/**
 * RESTful API for map services configuration.
 *
 * @author Bernard Fabien (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@Path("/1/MAP/{spec}")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class MapRest {
    private static final Logger LOGGER = Logging.getLogger(MapRest.class);

    @Inject
    private IStyleBusiness styleBusiness;
    
    @Inject
    private ILayerBusiness layerBusiness;
    
    @Inject
    private SecurityManager securityManager;

    @Inject
    private IDataBusiness dataBusiness;

    /**
     * Extracts and returns the list of {@link Layer}s available on a "map" service.
     *
     * @param spec the service type
     * @param id the service identifier
     * @return the {@link Layer} list
     * @throws TargetNotFoundException if the service with specified identifier does not exist
     * @throws ConfigurationException if the operation has failed for any reason
     */
    @GET
    @Path("{id}/layer/all")
    public Response getLayers(final @PathParam("spec") String spec, final @PathParam("id") String id) throws ConfigurationException {
        return ok(new LayerList(layerBusiness.getLayers(spec, id, securityManager.getCurrentUserLogin())));
    }

    @GET
    @Path("{id}/layersummary/all")
    public Response getLayersSummary(final @PathParam("spec") String spec, final @PathParam("id") String id) throws ConfigurationException {
        final List<Layer> layers = layerBusiness.getLayers(spec, id, securityManager.getCurrentUserLogin());
        
        final List<LayerSummary> sumLayers = new ArrayList<>();
        for (final Layer lay : layers) {
            final DataBrief db = dataBusiness.getDataBrief(lay.getName(), lay.getProviderID());
            sumLayers.add(new LayerSummary(lay,db));
        }
        return ok(sumLayers);
    }

    /**
     * Adds a new layer to a "map" service instance.
     *
     * @param layer the layer to be added
     * @throws TargetNotFoundException if the service with specified identifier does not exist
     * @throws ConfigurationException if the operation has failed for any reason
     */
    @PUT
    @Path("{id}/layer")
    public Response addLayer(final @PathParam("spec") String spec, final @PathParam("id") String id, final AddLayer layer) throws ConfigurationException {
        layerBusiness.add(layer);
        return ok(AcknowlegementType.success("Layer \"" + layer.getLayerId() + "\" successfully added to " + spec + " service \"" + id + "\"."));
    }

    /**
     * Update an existing layer title to a "map" service instance.
     *
     * @param layer the layer to be added
     * @throws TargetNotFoundException if the service with specified identifier does not exist
     * @throws ConfigurationException if the operation has failed for any reason
     */
    @POST
    @Path("{id}/layer/update/title")
    public Response updateLayerTitle(final @PathParam("spec") String spec, final @PathParam("id") String id, final LayerSummary layer) throws ConfigurationException {
        layerBusiness.updateLayerTitle(layer);
        return ok(AcknowlegementType.success("Layer \"" + layer.getName() + "\" successfully added to " + spec + " service \"" + id + "\"."));
    }

    /**
     * Remove a layer from a service.
     *
     * @param spec service type.
     * @param serviceId the service identifier
     * @param layerId the layer to remove
     * @throws ConfigurationException
     */
    @POST
    @Path("{id}/delete/{layerid}")
    public Response deleteLayer(final @PathParam("spec") String spec, final @PathParam("id") String serviceId, final @PathParam("layerid") String layerId, final SimpleValue layernmsp) throws ConfigurationException {
        String namespace = null;
        if (layernmsp != null && !layernmsp.getValue().isEmpty()) {
            namespace = layernmsp.getValue();
        }
        layerBusiness.remove(spec, serviceId, layerId, namespace);
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @POST
    @Path("{id}/updatestyle")
    public Response updateLayerStyleForService(final @PathParam("spec") String serviceType, final @PathParam("id") String serviceIdentifier, final ParameterValues params) throws ConfigurationException {
        styleBusiness.createOrUpdateStyleFromLayer(serviceType, serviceIdentifier, params.get("layerId"), params.get("spId"), params.get("styleName"));
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @POST
    @Path("{id}/removestyle")
    public Response removeLayerStyleForService(final @PathParam("spec") String serviceType, final @PathParam("id") String serviceIdentifier,
        final ParameterValues params) throws ConfigurationException {
        styleBusiness.removeStyleFromLayer(serviceIdentifier, serviceType, params.get("layerId"), params.get("spId"), params.get("styleName"));
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @POST
    @Path("{id}/extractLayerInfo/{layerName}/{crs}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML})
    public Response extractWMTSLayerInfo(final @PathParam("spec") String serviceType,
                                         final @PathParam("id") String serviceIdentifier,
                                         final @PathParam("layerName") String layerName,
                                         final @PathParam("crs") String crs,
                                         final Capabilities capabilities) throws ConfigurationException {
        final Map<String,Object> map = new HashMap<>();
        if(capabilities != null){
            final ContentsType contents = capabilities.getContents();
            if(contents != null){
                final List<LayerType> layertypeList = contents.getLayers();
                LayerType layerType = null;
                for(final LayerType lt : layertypeList){
                    if(layerName.equals(lt.getIdentifier().getValue())){
                        layerType = lt;
                        break;
                    }
                }
                if(layerType == null) {
                    throw new ConstellationException("There is no layer in capabilities with name "+layerName);
                }
                final List<TileMatrixSetLink> tmslList = layerType.getTileMatrixSetLink();
                if(tmslList!=null && !tmslList.isEmpty()){
                    final TileMatrixSetLink tmsl = tmslList.get(0); //the layer contains only one tilematrixSetLink
                    if(tmsl != null){
                        final String matrixSetId = tmsl.getTileMatrixSet();
                        map.put("matrixSet",matrixSetId);

                        final TileMatrixSet matrixSet = contents.getTileMatrixSetByIdentifier(matrixSetId);
                        if(matrixSet!=null){
                            final List<TileMatrix> tileMatrixList = matrixSet.getTileMatrix();
                            if(tileMatrixList != null){
                                final List<String> matrixIds = new ArrayList<>();
                                final List<Double> resolutions = new ArrayList<>();
                                for(int i=tileMatrixList.size()-1;i>=0;i--){
                                    final TileMatrix tm = tileMatrixList.get(i);
                                    matrixIds.add(tm.getIdentifier().getValue());

                                    try{
                                        final CoordinateReferenceSystem crsObj = CRS.decode(matrixSet.getSupportedCRS());
                                        final double scale = WMTSUtilities.unitsByPixel(matrixSet, crsObj, tm);
                                        resolutions.add(scale);
                                    }catch(Exception ex){
                                        LOGGER.log(Level.WARNING,ex.getLocalizedMessage(),ex);
                                    }
                                }
                                map.put("matrixIds",matrixIds.toArray());
                                map.put("resolutions",resolutions.toArray());
                            }
                        }
                    }
                }
                final List<Style> styleList = layerType.getStyle();
                if(styleList != null && !styleList.isEmpty()){
                    final Style style = styleList.get(0);
                    map.put("style",style.getIdentifier().getValue());
                }
                final List<WGS84BoundingBoxType> bboxList = layerType.getWGS84BoundingBox();
                if (bboxList != null && ! bboxList.isEmpty()) {
                    final WGS84BoundingBoxType bbt = bboxList.get(0);
                    map.put("dataExtent", Arrays.asList(bbt.getLowerCorner().get(0),
                            bbt.getLowerCorner().get(1),
                            bbt.getUpperCorner().get(0),
                            bbt.getUpperCorner().get(1)).toArray());
                }else {
                    final List<BoundingBoxType> bboxList2 = layerType.getBoundingBox();
                    if (bboxList2 != null && ! bboxList2.isEmpty()) {
                        final BoundingBoxType bbt = bboxList2.get(0);
                        map.put("dataExtent", Arrays.asList(bbt.getLowerCorner().get(0),
                                bbt.getLowerCorner().get(1),
                                bbt.getUpperCorner().get(0),
                                bbt.getUpperCorner().get(1)).toArray());
                    }
                }
            }
        }
        return Response.ok(map).build();
    }

}
