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
package org.constellation.util;

import javax.xml.bind.annotation.XmlValue;
import org.geotoolkit.feature.DefaultName;
import org.opengis.feature.type.Name;

import java.util.Date;

/**
 * Reference to a provider or service layer.
 * The reference pattern depend of the type of input data.
 *
 * If DataReference is from a provider layer, the pattern will be like :
 * <code>${providerLayerType|providerId|layerId}</code> for example <code>${providerLayerType|shapeFileProvider|CountiesLayer}</code>
 * use {@link #createProviderDataReference(String, String, String, java.util.Date)} with {@link #PROVIDER_LAYER_TYPE}
 *
 * If DataReference is from a provider Style, the pattern will be like :
 * <code>${providerStyleType|providerId|layerId}</code> for example <code>${providerStyleType|sldProvider|flashyStyle}</code>
 * use {@link #createProviderDataReference(String, String, String, java.util.Date)} with {@link #PROVIDER_STYLE_TYPE}
 *
 * If DataReference is from a Service, the pattern will be like :
 * <code>${serviceType|serviceURL|serviceSpec|serviceId|layerId}</code> for example <code>${serviceType|http://localhost:8080/cstl/WS/wms/defaultInstance|WMS|defaultInstance|CountiesLayer}</code>
 * use {@link #createServiceDataReference(String, String, String, String, java.util.Date)}
 *
 * Parameter <code>dataVersion</code> add the version used to extract data only if reference data support versionning.
 *
 * layer identifier should be with namespace like "{http://geotoolkit.org}myLayer".
 *
 * @author Johann Sorel (Geomatys)
 * @author Quentin Boileau (Geomatys).
 */
public class DataReference implements CharSequence, Comparable<DataReference>{

    private static final String SEPARATOR = "|";
    /*
     * Data types
     */
    public static String PROVIDER_LAYER_TYPE = "providerLayerType";
    public static String PROVIDER_STYLE_TYPE = "providerStyleType";
    public static String SERVICE_TYPE        = "serviceType";

    protected String reference;

    /**
     * Reference type can be  {@link #PROVIDER_LAYER_TYPE} or {@link #PROVIDER_STYLE_TYPE} or {@link #SERVICE_TYPE}.
     */
    protected String type;
    protected String providerId;
    protected String serviceURL;
    protected String serviceSpec;
    protected String serviceId;
    protected String layerId;

    /**
     * Date version of targed data. In timestamp.
     */
    protected String dataVersion;

    public DataReference() {
        this.reference = null;
    }

    public DataReference(final String str) {
        this.reference = str;
        computeReferenceParts();
    }

    /**
     * DataReference constructor without layer version date.
     * @param dataType {@link #PROVIDER_LAYER_TYPE} {@link #PROVIDER_STYLE_TYPE} {@link #SERVICE_TYPE}
     * @param providerId
     * @param serviceURL
     * @param serviceSpec
     * @param serviceId
     * @param layerId layer identifier with namespace like "{http://geotoolkit.org}myLayer"
     */
    public DataReference (final String dataType, final String providerId, final String serviceURL, final String serviceSpec, final String serviceId, final String layerId) {
        this(dataType, providerId, serviceURL, serviceSpec, serviceId, layerId, null);
    }

    /**
     * DataReference constructor WITH layer version date.
     * @param dataType {@link #PROVIDER_LAYER_TYPE} {@link #PROVIDER_STYLE_TYPE} {@link #SERVICE_TYPE}
     * @param providerId
     * @param serviceURL
     * @param serviceSpec
     * @param serviceId
     * @param layerId layer identifier with namespace like "{http://geotoolkit.org}myLayer"
     * @param dataVersion date of date version. (can be used only if targeted data is versioned).
     */
    public DataReference (final String dataType, final String providerId, final String serviceURL, final String serviceSpec, final String serviceId, final String layerId,
                          final Date dataVersion) {
        if (dataType != null && (dataType.equals(PROVIDER_LAYER_TYPE) || dataType.equals(PROVIDER_STYLE_TYPE) || dataType.equals(SERVICE_TYPE))) {
            this.type       = dataType;
        } else {
            throw new IllegalArgumentException("Reference should match pattern ${providerLayerType|providerId|layerId} or ${providerStyleType|providerId|layerId} or ${serviceType|serviceURL|serviceSpec|serviceId|layerId}.");
        }
        this.serviceURL     = serviceURL;
        this.providerId     = providerId;
        this.serviceSpec    = serviceSpec;
        this.serviceId      = serviceId;
        this.layerId        = layerId;
        this.reference      = buildRefrenceString();
        this.dataVersion = dataVersion != null ? Long.valueOf(dataVersion.getTime()).toString() : null;
    }

    /**
     * Create a DataReference from a provider layer or style.
     * @param providerType should be  {@link #PROVIDER_LAYER_TYPE} or {@link #PROVIDER_STYLE_TYPE}
     * @param providerId provider identifier
     * @param layerId layer identifier with namespace like "{http://geotoolkit.org}myLayer"
     * @return DataReference
     */
    public static DataReference createProviderDataReference(final String providerType, final String providerId, final String layerId) {
        return createProviderDataReference(providerType, providerId, layerId, null);
    }

    /**
     * Create a DataReference from a provider layer or style.
     * @param providerType should be  {@link #PROVIDER_LAYER_TYPE} or {@link #PROVIDER_STYLE_TYPE}
     * @param providerId provider identifier
     * @param layerId layer identifier with namespace like "{http://geotoolkit.org}myLayer"
     * @param dataVersion Date of data version.
     * @return DataReference
     */
    public static DataReference createProviderDataReference(final String providerType, final String providerId, final String layerId, final Date dataVersion) {
        if (providerType != null && (providerType.equals(PROVIDER_LAYER_TYPE) || providerType.equals(PROVIDER_STYLE_TYPE))) {
            return new DataReference(providerType, providerId, null, null, null, layerId, dataVersion);
        }
        throw new IllegalArgumentException("Reference should match pattern ${providerLayerType|providerId|layerId} or ${providerStyleType|providerId|layerId}.");
    }

    /**
     * Create a DataReference from a service.
     * @param serviceSpec like WMS, WFS, ...
     * @param serviceId instance identifier of the service
     * @param layerId layer identifier with namespace like "{http://geotoolkit.org}myLayer"
     * @return DataReference
     */
    public static DataReference createServiceDataReference(final String serviceURL, final String serviceSpec, final String serviceId, final String layerId) {
        return createServiceDataReference(serviceURL, serviceSpec, serviceId, layerId, null);
    }

    /**
     * Create a DataReference from a service.
     * @param serviceSpec like WMS, WFS, ...
     * @param serviceId instance identifier of the service
     * @param layerId layer identifier with namespace like "{http://geotoolkit.org}myLayer"
     * @param dataVersion Date of data version.
     * @return DataReference
     */
    public static DataReference createServiceDataReference(final String serviceURL, final String serviceSpec, final String serviceId, final String layerId, final Date dataVersion) {
        return new DataReference(SERVICE_TYPE, null,serviceURL, serviceSpec, serviceId, layerId, dataVersion);
    }

    @XmlValue
    public String getReference() {
        return buildRefrenceString();
    }

    public void setReference(final String reference) {
        this.reference = reference;
        computeReferenceParts();
    }

    /**
     * Split reference string.
     */
    private void computeReferenceParts () {
        if (reference != null && reference.startsWith("${") && reference.endsWith("}")) {
            final String datas = reference.substring(2,reference.length()-1);

            final String[] dataSplit = datas.split("\\"+SEPARATOR);
            final int groupCount = dataSplit.length;

            final String datatype = dataSplit[0];
            //get data type
            if (!datatype.isEmpty() && (datatype.equals(PROVIDER_LAYER_TYPE) || datatype.equals(PROVIDER_STYLE_TYPE) ) && (groupCount == 3 || groupCount == 4)) {
                type = datatype;
            } else if (!datatype.isEmpty() && datatype.equals(SERVICE_TYPE) && (groupCount == 5 || groupCount == 6)) {
                type = datatype;
            } else {
                throw new IllegalArgumentException("Reference data should be type of providerLayerType or providerStyleType or serviceType.");
            }

            this.dataVersion = null;
            if (type.equals(PROVIDER_LAYER_TYPE) || type.equals(PROVIDER_STYLE_TYPE)) {

                this.serviceSpec = null;
                this.serviceId = null;
                this.providerId = dataSplit[1];     //providerID
                this.layerId = dataSplit[2];        //layerID
                if (groupCount == 4) {
                    this.dataVersion = dataSplit[3]; //dataVersion
                }

            } else if (type.equals(SERVICE_TYPE)) {

                this.providerId = null;
                this.serviceURL = dataSplit[1];     //http://localhost:8080/cstl/WS/wms/serviceID
                this.serviceSpec = dataSplit[2];    //WMS
                this.serviceId = dataSplit[3];      //serviceID
                this.layerId = dataSplit[4];        //layerID
                if (groupCount == 6) {
                    this.dataVersion = dataSplit[5]; //dataVersion
                }

            }
        } else {
            throw new IllegalArgumentException("Reference should match pattern ${providerLayerType|providerId|layerId} or ${providerStyleType|providerId|layerId} or ${serviceType|serviceURL|serviceSpec|serviceId|layerId}.");
        }
    }

    /**
     * Make the reference string from parts.
     */
    private String buildRefrenceString() {
        final StringBuffer buffer = new StringBuffer("${");
        buffer.append(getDataType()).append(SEPARATOR);

        if (type.equals(PROVIDER_LAYER_TYPE) || type.equals(PROVIDER_STYLE_TYPE)) {

            buffer.append(providerId).append(SEPARATOR);

        } else if (type.equals(SERVICE_TYPE)) {

            buffer.append(serviceURL).append(SEPARATOR);
            buffer.append(serviceSpec).append(SEPARATOR);
            buffer.append(serviceId).append(SEPARATOR);
        }

        buffer.append(layerId);
        if (dataVersion != null) {
            buffer.append(SEPARATOR).append(dataVersion);
        }

        buffer.append("}");
        return buffer.toString();
    }

    /**
     * Return the type of dataRefrence, <code>providerLayerType</code> or <code>providerStyleType</code> or <code>serviceType</code>.
     * @return String or null if type is undefined.
     */
    public String getDataType() {
        return type;
    }

    /**
     * The service specification part of the data.
     * @return String
     */
    public String getServiceSpec(){
        return serviceSpec;
    }

    /**
     * The service server URL part of the data.
     * @return String
     */
    public String getServiceURL(){
        return serviceURL;
    }

    /**
     * Read the service/provider id part of the data.
     * @return String
     */
    public String getProviderOrServiceId(){
        if (type.equals(PROVIDER_LAYER_TYPE) || type.equals(PROVIDER_STYLE_TYPE)) {
            return providerId;
        } else if (type.equals(SERVICE_TYPE)) {
            return serviceId;
        }
        return null;
    }

    /**
     * Read the service id part of the data.
     * @return String if DataReference is a Service type or null
     */
    public String getServiceId(){
        if (type.equals(SERVICE_TYPE)) {
            return serviceId;
        }
        return null;
    }

    /**
     * Read the provider id part of the data.
     * @return String provider id if DataReference is a Style or layer provider type or null
     */
    public String getProviderId(){
        if (type.equals(PROVIDER_LAYER_TYPE) || type.equals(PROVIDER_STYLE_TYPE)) {
            return providerId;
        }
        return null;
    }

    /**
     * Read the layer id part of the data.
     * @return Name
     */
    public Name getLayerId(){
        return DefaultName.valueOf(layerId);
    }

    /**
     * The dataVersion if not null.
     * @return Date or null if dataVersion not specified.
     */
    public Date getDataVersion() {

        if (dataVersion != null) {
            Long time = Long.valueOf(dataVersion);
            return new Date(time);
        }
        return null;
    }


    @Override
    public int length() {
        return reference.length();
    }

    @Override
    public char charAt(int index) {
        return reference.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return reference.subSequence(start, end);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.reference != null ? this.reference.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DataReference other = (DataReference) obj;
        if ((this.reference == null) ? (other.reference != null) : !this.reference.equals(other.reference)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[DataReference]");
        sb.append("reference:\n").append(reference).append('\n');
        sb.append("type:\n").append(type).append('\n');
        sb.append("providerId:\n").append(providerId).append('\n');
        sb.append("serviceURL:\n").append(serviceURL).append('\n');
        sb.append("serviceSpec:\n").append(serviceSpec).append('\n');
        sb.append("serviceId:\n").append(serviceId).append('\n');
        sb.append("layerId:\n").append(layerId).append('\n');
        sb.append("dataVersion:\n").append(dataVersion).append('\n');
        return sb.toString();
    }

	@Override
	public int compareTo(DataReference o) {
	    if(o==null)
	        return -1;
		return getReference().compareTo(o.getReference());
	}

}
