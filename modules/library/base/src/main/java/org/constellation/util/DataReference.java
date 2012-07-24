
package org.constellation.util;

import org.geotoolkit.feature.DefaultName;
import org.opengis.feature.type.Name;

/**
 * Reference to a provider or service layer relative to dcns server.
 * The reference pattern depend of the type of input data.
 *
 * If DataReference is from a provider layer, the pattern will be like :
 * <code>${providerLayerType:providerId:layerId}</code> for example <code>${providerLayerType:shapeFileProvider:CountiesLayer}</code>
 *
 * If DataReference is from a provider Style, the pattern will be like :
 * <code>${providerStyleType:providerId:layerId}</code> for example <code>${providerStyleType:sldProvider:flashyStyle}</code>
 *
 * If DataReference is from a Service, the pattern will be like :
 * <code>${serviceType:serviceSpec:serviceId:layerId}</code> for example <code>${serviceType:WMS:defaultInstance:CountiesLayer}</code>
 *
 * @author Johann Sorel (Geomatys)
 * @author Quentin Boileau (Geomatys).
 */
public class DataReference implements CharSequence{

    /*
     * Data types
     */
    public static String PROVIDER_LAYER_TYPE = "providerLayerType";
    public static String PROVIDER_STYLE_TYPE = "providerStyleType";
    public static String SERVICE_TYPE        = "serviceType";

    private String reference;
    private String type;
    private String providerId;
    private String serviceSpec;
    private String serviceId;
    private String layerId;

    public DataReference(final String str) {
        this.reference = str;
        computeReferenceParts();
    }

    public DataReference (final String dataType, final String providerId, final String serviceSpec, final String serviceId, final String layerId) {
        if (dataType != null && (dataType.equals(PROVIDER_LAYER_TYPE) || dataType.equals(PROVIDER_STYLE_TYPE) || dataType.equals(SERVICE_TYPE))) {
            this.type       = dataType;
        }

        this.providerId     = providerId;
        this.serviceSpec    = serviceSpec;
        this.serviceId      = serviceId;
        this.layerId        = layerId;
        this.reference = buildRefrenceString();
    }


    /**
     * Create a DataReference from a provider layer or style.
     * @param providerType like providerLayerType or providerStyleType
     * @param providerId provider identifier
     * @param layerId layer identifier
     * @return DataReference
     */
    public static DataReference createProviderDataReference(final String providerType, final String providerId, final String layerId) {
        if (providerType != null && (providerType.equals(PROVIDER_LAYER_TYPE) || providerType.equals(PROVIDER_STYLE_TYPE))) {
            return new DataReference(providerType, providerId, null, null, layerId);
        }
        throw new IllegalArgumentException("Reference should match pattern ${providerLayerType:providerId:layerId} or ${providerStyleType:providerId:layerId} or ${serviceType:serviceSpec:serviceId:layerId}.");
    }

    /**
     * Create a DataReference from a service.
     * @param serviceSpec like WMS, WFS, ...
     * @param serviceId instance identifier of the service
     * @param layerId layer identifier
     * @return DataReference
     */
    public static DataReference createServiceDataReference(final String serviceSpec, final String serviceId, final String layerId) {
        return new DataReference(SERVICE_TYPE, null, serviceSpec, serviceId, layerId);
    }

    public String getReference() {
        return reference;
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

            final String datatype = datas.substring(0, datas.indexOf(":"));
            //get data type
            if (!datatype.isEmpty() && (datatype.equals(PROVIDER_LAYER_TYPE) || datatype.equals(PROVIDER_STYLE_TYPE) )) {
                type = datatype;
            } else if (!datatype.isEmpty() && datatype.equals(SERVICE_TYPE)) {
                type = datatype;
            } else {
                throw new IllegalArgumentException("Reference data should be type of providerLayerType or providerStyleType or serviceType.");
            }

            if (type.equals(PROVIDER_LAYER_TYPE) || type.equals(PROVIDER_STYLE_TYPE)) {

                final String providerDatas = datas.substring(datas.indexOf(":")+1);                     //providerID:layerID
                this.serviceSpec = null;
                this.serviceId = null;
                this.providerId = providerDatas.substring(0, providerDatas.indexOf(":"));               //providerID
                this.layerId = providerDatas.substring(providerDatas.indexOf(":")+1);                   //layerID

            } else if (type.equals(SERVICE_TYPE)) {

                final String serviceDatas = datas.substring(datas.indexOf(":")+1);                      //WMS:serviceID:layerID
                final String serviceIdLayerId = serviceDatas.substring(serviceDatas.indexOf(":")+1);    //sericeID:layerID
                this.providerId = null;
                this.serviceSpec = serviceDatas.substring(0, serviceDatas.indexOf(":"));                //WMS
                this.serviceId = serviceIdLayerId.substring(0, serviceIdLayerId.indexOf(":"));          //serviceID
                this.layerId = serviceIdLayerId.substring(serviceIdLayerId.indexOf(":")+1);             //layerID

            }
        } else {
            throw new IllegalArgumentException("Reference should match pattern ${providerLayerType:providerId:layerId} or ${providerStyleType:providerId:layerId} or ${serviceType:serviceSpec:serviceId:layerId}.");
        }
    }

    /**
     * Make the reference string from parts.
     */
    private String buildRefrenceString() {
        final StringBuffer buffer = new StringBuffer("${");

        if (type.equals(PROVIDER_LAYER_TYPE) || type.equals(PROVIDER_STYLE_TYPE)) {

            buffer.append(getDataType()).append(":");
            buffer.append(providerId).append(":");
            buffer.append(layerId);

        } else if (type.equals(SERVICE_TYPE)) {

            buffer.append(getDataType()).append(":");
            buffer.append(serviceSpec).append(":");
            buffer.append(serviceId).append(":");
            buffer.append(layerId);
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
     * Read the service/provider id part of the data.
     * @return String
     */
    public String getServiceId(){
        if (type.equals(PROVIDER_LAYER_TYPE) || type.equals(PROVIDER_STYLE_TYPE)) {
            return providerId;
        } else if (type.equals(SERVICE_TYPE)) {
            return serviceId;
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
        int hash = 3;
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

}
