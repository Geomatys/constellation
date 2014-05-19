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
package org.constellation.query.wms;

import javax.ws.rs.core.MultivaluedMap;
import net.jcip.annotations.Immutable;
import org.constellation.query.Query;
import org.constellation.query.DefaultQueryRequest;
import org.constellation.query.QueryRequest;
import org.apache.sis.util.Version;


/**
 * Handle the service type and the version of a WMS query.
 * Contains constants for WMS requests in version 1.1.1 and 1.3.0
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
@Immutable
public abstract class WMSQuery implements Query {

    /**
     * Request parameters.
     */
    public static final String GETMAP           = "GetMap";
    public static final String MAP              = "Map";
    public static final String GETFEATUREINFO   = "GetFeatureInfo";
    public static final String GETCAPABILITIES  = "GetCapabilities";
    public static final String DESCRIBELAYER    = "DescribeLayer";
    public static final String GETLEGENDGRAPHIC = "GetLegendGraphic";
    public static final String GETORIGFILE      = "GetOrigFile";

    /**
     * WMS Query service
     */
    public static final String WMS_SERVICE = "WMS";

    /**
     * Key for the {@code DescribeLayer} request.
     */
    public static final QueryRequest DESCRIBE_LAYER = new DefaultQueryRequest(DESCRIBELAYER);

    /**
     * Key for the {@code GetCapabilities} request.
     */
    public static final QueryRequest GET_CAPABILITIES = new DefaultQueryRequest(GETCAPABILITIES);

    /**
     * Key for the {@code GetFeatureInfo} request.
     */
    public static final QueryRequest GET_FEATURE_INFO = new DefaultQueryRequest(GETFEATUREINFO);

    /**
     * Key for the {@code GetLegendGraphic} request.
     */
    public static final QueryRequest GET_LEGEND_GRAPHIC = new DefaultQueryRequest(GETLEGENDGRAPHIC);

    /**
     * Key for the {@code GetMap} request.
     */
    public static final QueryRequest GET_MAP = new DefaultQueryRequest(GETMAP);

    /**
     * Key for the {@code GetOrigFile} request.
     */
    public static final QueryRequest GET_ORIG_FILE = new DefaultQueryRequest(GETORIGFILE);

    /**
     * For backward compatibility with WMS 1.0.0, the request can be done with
     * a value {@code capabilities}.
     */
    public static final String CAPABILITIES     = "Capabilities";

    /** Parameter used in getMap, getLegendGraphic, getCapabilities */
    public static final String KEY_FORMAT = "FORMAT";
    /** Parameter used in getMap, describeLayer */
    public static final String KEY_LAYERS = "LAYERS";
    /** Parameter used in getOrigFile, getLegendGraphic */
    public static final String KEY_LAYER = "LAYER";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_QUERY_LAYERS = "QUERY_LAYERS";
    /** Parameter used in getMap, getFeatureInfo */
    public static final String KEY_CRS_V111 = "SRS";
    /** Parameter used in getMap, getFeatureInfo */
    public static final String KEY_CRS_V130 = "CRS";
    /** Parameter used in getMap, getFeatureInfo */
    public static final String KEY_BBOX = "BBOX";
    /** Parameter used in getMap, getFeatureInfo */
    public static final String KEY_ELEVATION = "ELEVATION";
    /** Parameter used in getMap, getOrigFile, getFeatureInfo */
    public static final String KEY_TIME = "TIME";
    /** Parameter used in getMap, getFeatureInfo, getLegendGraphic */
    public static final String KEY_WIDTH = "WIDTH";
    /** Parameter used in getMap, getFeatureInfo, getLegendGraphic */
    public static final String KEY_HEIGHT = "HEIGHT";
    /** Parameter used in getMap */
    public static final String KEY_BGCOLOR = "BGCOLOR";
    /** Parameter used in getMap */
    public static final String KEY_TRANSPARENT = "TRANSPARENT";
    /** Parameter used in getMap */
    public static final String KEY_STYLES = "STYLES";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_STYLE = "STYLE";
    /** Parameter used in getMap,getLegendGraphic */
    public static final String KEY_SLD = "SLD";
    /** Parameter used in getMap, getLegendGraphic */
    public static final String KEY_SLD_VERSION = "SLD_VERSION";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_FEATURETYPE = "FEATURETYPE";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_COVERAGE = "COVERAGE";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_RULE = "RULE";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_SCALE = "SCALE";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_SLD_BODY = "SLD_BODY";
    /** Parameter used in getMap,getLegendGraphic */
    public static final String KEY_REMOTE_OWS_TYPE = "REMOTE_OWS_TYPE";
    /** Parameter used in getMap,getLegendGraphic */
    public static final String KEY_REMOTE_OWS_URL = "REMOTE_OWS_URL";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_I_V130 = "I";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_J_V130 = "J";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_I_V111 = "X";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_J_V111 = "Y";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_INFO_FORMAT= "INFO_FORMAT";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_FEATURE_COUNT = "FEATURE_COUNT";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_GETMETADATA = "GetMetadata";
    /** Parameter used in getMap */
    public static final String KEY_AZIMUTH = "AZIMUTH";
    /** Parameter used in GetCapabilities, for backward compatibility with WMS 1.0.0 */
    public static final String KEY_WMTVER = "WMTVER";
    /** Parameter used to store additional parameters from the query, the value object is a MultiValueMap */
    public static final String KEY_EXTRA_PARAMETERS = "EXTRA";
    /** Parameter INSPIRE used to choose the language of the capabilities document */
    public static final String KEY_LANGUAGE = "LANGUAGE";

    private final Version version;
    /**
     * All query parameters, this might hold additional parameters that providers
     * or renderers may understand.
     */
    protected final MultivaluedMap<String,String> parameters;


    protected WMSQuery(final Version version, MultivaluedMap<String,String> parameters) {
        if (version == null) {
            throw new IllegalArgumentException("Version should not be null !");
        }
        this.version = version;
        this.parameters = parameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getService() {
        return WMS_SERVICE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Version getVersion() {
        return version;
    }

    public MultivaluedMap<String, String> getParameters() {
        return parameters;
    }

    /**
     * Returns a string representation of the parameters stored in WMS requests, as
     * a Key Value Pair list separated by the character {@code &}.
     */
    public abstract String toKvp();
}
