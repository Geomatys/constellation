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
package org.constellation.map.featureinfo;

import org.apache.sis.geometry.GeneralDirectPosition;
import org.apache.sis.util.ArraysExt;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.util.ArgumentChecks;

import org.constellation.configuration.*;

import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReadParam;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.display2d.canvas.AbstractGraphicVisitor;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.lang.Static;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.crs.DefaultCompoundCRS;

import org.opengis.coverage.CannotEvaluateException;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.operation.TransformException;

import javax.imageio.spi.ServiceRegistry;
import javax.measure.converter.ConversionException;
import javax.measure.unit.NonSI;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.logging.Level;

/**
 * Set of utilities methods for FeatureInfoFormat and GetFeatureInfoCfg manipulation.
 *
 * @author Quentin Boileau (Geomatys)
 */
public final class FeatureInfoUtilities extends Static {

    /**
     * Get all declared in resources/META-INF/service/org.constellation.map.featureinfo.FeatureInfoFormat file
     * FeatureInfoFormat.
     * @return an array of FeatureInfoFormat instances.
     */
    public static FeatureInfoFormat[] getAllFeatureInfoFormat() {

        final Set<FeatureInfoFormat> infoFormats = new HashSet<FeatureInfoFormat>();
        final Iterator<FeatureInfoFormat> ite = ServiceRegistry.lookupProviders(FeatureInfoFormat.class);
        while (ite.hasNext()) {
            infoFormats.add(ite.next());
        }
        return infoFormats.toArray(new FeatureInfoFormat[infoFormats.size()]);
    }

    /**
     * Search a specific instance of {@link FeatureInfoFormat} in layer (if not null) then in service configuration using
     * mimeType.
     *
     * @param serviceConf service configuration (can't be null)
     * @param layerConf layer configuration. Can be null. If not null, search in layer configuration first.
     * @param mimeType searched mimeType (can't be null)
     * @return found FeatureInfoFormat of <code>null</code> if not found.
     * @throws ClassNotFoundException if a {@link org.constellation.configuration.GetFeatureInfoCfg} binding class is not in classpath
     * @throws ConfigurationException if binding class is not an {@link FeatureInfoFormat} instance
     * or declared {@link org.constellation.configuration.GetFeatureInfoCfg} MimeType is not supported by the {@link FeatureInfoFormat} implementation.
     */
    public static FeatureInfoFormat getFeatureInfoFormat (final LayerContext serviceConf, final Layer layerConf, final String mimeType)
            throws ClassNotFoundException, ConfigurationException {

        ArgumentChecks.ensureNonNull("serviceConf", serviceConf);
        ArgumentChecks.ensureNonNull("mimeType", mimeType);

        FeatureInfoFormat featureInfo = null;

        if (layerConf != null) {
            final List<GetFeatureInfoCfg> infos = layerConf.getGetFeatureInfoCfgs();
            if (infos != null && infos.size() > 0) {
                for (GetFeatureInfoCfg infoCfg : infos) {
                    if (infoCfg.getMimeType().equals(mimeType)) {
                        featureInfo = FeatureInfoUtilities.getFeatureInfoFormatFromConf(infoCfg);
                    } else if (infoCfg.getMimeType() == null || infoCfg.getMimeType().isEmpty()) {

                        //Find supported mimetypes in FeatureInfoFormat
                        final FeatureInfoFormat tmpFormat = FeatureInfoUtilities.getFeatureInfoFormatFromConf(infoCfg);

                        final List<String> supportedMime = tmpFormat.getSupportedMimeTypes();
                        if (!(supportedMime.isEmpty()) && supportedMime.contains(mimeType)) {
                            featureInfo = tmpFormat;
                        }
                    }
                }
            }
        }

        //try generics
        if (featureInfo == null) {
            final Set<GetFeatureInfoCfg> generics = FeatureInfoUtilities.getGenericFeatureInfos(serviceConf);
            for (GetFeatureInfoCfg infoCfg : generics) {
                if (infoCfg.getMimeType().equals(mimeType)) {
                    featureInfo = FeatureInfoUtilities.getFeatureInfoFormatFromConf(infoCfg);
                }
            }
        }

        return featureInfo;

    }

    /**
     * Find {@link FeatureInfoFormat} from a given {@link org.constellation.configuration.GetFeatureInfoCfg}.
     * Also check if {@link org.constellation.configuration.GetFeatureInfoCfg} mimeType is supported by {@link FeatureInfoFormat} found.
     *
     * @param infoConf {@link org.constellation.configuration.GetFeatureInfoCfg} input
     * @return a {@link FeatureInfoFormat} or null if not found
     * @throws ClassNotFoundException if a {@link org.constellation.configuration.GetFeatureInfoCfg} binding class is not in classpath
     * @throws ConfigurationException if binding class is not an {@link FeatureInfoFormat} instance
     * or declared {@link org.constellation.configuration.GetFeatureInfoCfg} MimeType is not supported by the {@link FeatureInfoFormat} implementation.
     */
    public static FeatureInfoFormat getFeatureInfoFormatFromConf (final GetFeatureInfoCfg infoConf) throws ClassNotFoundException, ConfigurationException {
        final String mime = infoConf.getMimeType();
        final String binding = infoConf.getBinding();

        final FeatureInfoFormat featureInfo = getFeatureInfoFormatFromBinding(binding);
        if (featureInfo != null) {
            featureInfo.setConfiguration(infoConf);//give his configuration

            if (mime == null || mime.isEmpty()) {
                return featureInfo; // empty config mime type -> no need to check
            } else {
                if (featureInfo.getSupportedMimeTypes().contains(mime)) {
                    return featureInfo;
                } else {
                    throw new ConfigurationException("MimeType "+mime+" not supported by FeatureInfo "+binding+
                            ". Supported output MimeTypes are "+ featureInfo.getSupportedMimeTypes());
                }
            }
        }
        return null;
    }

    /**
     *  Find {@link FeatureInfoFormat} from a given canonical class name.
     *
     * @param binding canonical class name String
     * @return {@link FeatureInfoFormat} or null if binding class is not an instance of {@link FeatureInfoFormat}.
     * @throws ConfigurationException if binding class is not an {@link FeatureInfoFormat} instance
     */
    private static FeatureInfoFormat getFeatureInfoFormatFromBinding (final String binding) throws ClassNotFoundException {
        ArgumentChecks.ensureNonNull("binding", binding);

        final Class clazz = Class.forName(binding);
        final FeatureInfoFormat[] FIs = getAllFeatureInfoFormat();

        for (FeatureInfoFormat fi : FIs) {
            if (clazz.isInstance(fi)) {
                return fi;
            }
        }
        return null;
    }

    /**
     * Check {@link org.constellation.configuration.GetFeatureInfoCfg} configuration in {@link LayerContext}.
     *
     * @param config service configuration
     * @throws ConfigurationException if binding class is not an {@link FeatureInfoFormat} instance
     * or declared MimeType is not supported by the {@link FeatureInfoFormat} implementation.
     * @throws ClassNotFoundException if binding class is not in classpath
     */
    public static void checkConfiguration(final LayerContext config) throws ConfigurationException, ClassNotFoundException {
        if (config != null) {
            final Set<GetFeatureInfoCfg> generics = getGenericFeatureInfos(config);

            FeatureInfoFormat featureinfo;
            for (final GetFeatureInfoCfg infoConf : generics) {
                featureinfo = getFeatureInfoFormatFromConf(infoConf);
                if (featureinfo == null) {
                    throw new ConfigurationException("Unknown generic FeatureInfo configuration binding "+infoConf.getBinding());
                }
            }

            for (Source source : config.getLayers()) {
                if (source != null) {
                    for (Layer layer : source.getInclude()) {
                        if (layer != null && layer.getGetFeatureInfoCfgs() != null) {
                            for (GetFeatureInfoCfg infoConf : layer.getGetFeatureInfoCfgs()) {
                                featureinfo = getFeatureInfoFormatFromConf(infoConf);
                                if (featureinfo == null) {
                                    throw new ConfigurationException("Unknown FeatureInfo configuration binding "+infoConf.getBinding()+
                                    " for layer "+layer.getName().getLocalPart());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get all configured mimeTypes from a service {@link LayerContext}.
     * @param config service configuration
     * @return a Set of all MimeType from generic list and from layers config without duplicates.
     */
    public static Set<String> allSupportedMimeTypes (final LayerContext config) throws ConfigurationException, ClassNotFoundException {
        final Set<String> mimes = new HashSet<>();
        if (config != null) {
            final Set<GetFeatureInfoCfg> generics = getGenericFeatureInfos(config);
            for (GetFeatureInfoCfg infoConf : generics) {
                if (infoConf.getMimeType() != null && infoConf.getBinding() != null) {
                    mimes.add(infoConf.getMimeType());
                } else {
                    throw new ConfigurationException("Binding or MimeType not define for GetFeatureInfoCfg "+infoConf);
                }
            }

            for (Source source : config.getLayers()) {
                if (source != null) {
                    for (Layer layer : source.getInclude()) {
                        if (layer != null && layer.getGetFeatureInfoCfgs() != null) {
                            for (GetFeatureInfoCfg infoConf : layer.getGetFeatureInfoCfgs()) {

                                if (infoConf.getMimeType() == null || infoConf.getMimeType().isEmpty()) {
                                    //Empty mimeType -> Find supported mimetypes in format
                                    final FeatureInfoFormat tmpFormat = FeatureInfoUtilities.getFeatureInfoFormatFromConf(infoConf);
                                    tmpFormat.setConfiguration(infoConf); //give his configuration
                                    final List<String> supportedMime = tmpFormat.getSupportedMimeTypes();
                                    mimes.addAll(supportedMime);
                                } else {
                                    mimes.add(infoConf.getMimeType());
                                }
                            }
                        }
                    }
                }
            }
        }
        return mimes;
    }

    /**
     * Extract generic {@link org.constellation.configuration.GetFeatureInfoCfg} configurations from {@link LayerContext} base.
     *
     * @param config service configuration
     * @return a Set of GetFeatureInfoCfg
     */
    public static Set<GetFeatureInfoCfg> getGenericFeatureInfos (final LayerContext config) {
        final Set<GetFeatureInfoCfg> fis = new HashSet<>();
        if (config != null) {
            final List<GetFeatureInfoCfg> globalFI = config.getGetFeatureInfoCfgs();
            if (globalFI != null && !(globalFI.isEmpty())) {
                for (GetFeatureInfoCfg infoConf : globalFI) {
                    fis.add(infoConf);
                }
            }
        }
        return fis;
    }

    /**
     * Create the default {@link GetFeatureInfoCfg} list to configure a LayerContext.
     * This list is build from generic {@link FeatureInfoFormat} and there supported mimetype.
     * HTMLFeatureInfoFormat, CSVFeatureInfoFormat, GMLFeatureInfoFormat
     *
     * @return a list of {@link GetFeatureInfoCfg}
     */
    public static List<GetFeatureInfoCfg> createGenericConfiguration () {
        //Default featureInfo configuration
        final List<GetFeatureInfoCfg> featureInfos = new ArrayList<>();

        //HTML
        FeatureInfoFormat infoFormat = new HTMLFeatureInfoFormat();
        for (String mime : infoFormat.getSupportedMimeTypes()) {
            featureInfos.add(new GetFeatureInfoCfg(mime, infoFormat.getClass().getCanonicalName()));
        }

        //CSV
        infoFormat = new CSVFeatureInfoFormat();
        for (String mime : infoFormat.getSupportedMimeTypes()) {
            featureInfos.add(new GetFeatureInfoCfg(mime, infoFormat.getClass().getCanonicalName()));
        }

        //GML
        infoFormat = new GMLFeatureInfoFormat();
        for (String mime : infoFormat.getSupportedMimeTypes()) {
            featureInfos.add(new GetFeatureInfoCfg(mime, infoFormat.getClass().getCanonicalName()));
        }

        //XML
        infoFormat = new XMLFeatureInfoFormat();
        for (String mime : infoFormat.getSupportedMimeTypes()) {
            featureInfos.add(new GetFeatureInfoCfg(mime, infoFormat.getClass().getCanonicalName()));
        }
        return featureInfos;
    }


    /**
     * Returns the data values of the given coverage, or {@code null} if the
     * values can not be obtained.
     *
     * @return list : each entry contain a gridsampledimension and value associated.
     */
    public static List<Map.Entry<GridSampleDimension,Object>> getCoverageValues(final ProjectedCoverage gra,
                                                                                   final RenderingContext2D context,
                                                                                   final SearchAreaJ2D queryArea){

        final CoverageMapLayer layer = gra.getLayer();
        Envelope objBounds = context.getCanvasObjectiveBounds();
        CoordinateReferenceSystem objCRS = objBounds.getCoordinateReferenceSystem();
        TemporalCRS temporalCRS = CRS.getTemporalCRS(objCRS);
        if (temporalCRS == null) {
            /*
             * If there is no temporal range, arbitrarily select the latest date.
             * This is necessary otherwise the call to reader.read(...) will scan
             * every records in the GridCoverages table for the layer.
             */
            Envelope timeRange = layer.getBounds();
            if (timeRange != null) {
                temporalCRS = CRS.getTemporalCRS(timeRange.getCoordinateReferenceSystem());
                if (temporalCRS != null) {
                    try {
                        timeRange = CRS.transform(timeRange, temporalCRS);
                    } catch (TransformException e) {
                        // Should never happen since temporalCRS is a component of layer CRS.
                        Logging.unexpectedException(AbstractGraphicVisitor.class, "getCoverageValues", e);
                        return null;
                    }
                    final double lastTime = timeRange.getMaximum(0);
                    double day;
                    try {
                        // Arbitrarily use a time range of 1 day, to be converted in units of the temporal CRS.
                        day = NonSI.DAY.getConverterToAny(temporalCRS.getCoordinateSystem().getAxis(0).getUnit()).convert(1);
                    } catch (ConversionException e) {
                        // Should never happen since TemporalCRS use time units. But if it happen
                        // anyway, use a time range of 1 of whatever units the temporal CRS use.
                        Logging.unexpectedException(AbstractGraphicVisitor.class, "getCoverageValues", e);
                        day = 1;
                    }
                    objCRS = new DefaultCompoundCRS(objCRS.getName().getCode() + " + time", objCRS, temporalCRS);
                    final GeneralEnvelope merged = new GeneralEnvelope(objCRS);
                    GeneralEnvelope subEnv = merged.subEnvelope(0, objBounds.getDimension());
                    subEnv.setEnvelope(objBounds);
                    merged.setRange(objBounds.getDimension(), lastTime - day, lastTime);
                    objBounds = merged;
                }
            }
        }
        double[] resolution = context.getResolution();
        resolution = ArraysExt.resize(resolution, objCRS.getCoordinateSystem().getDimension());

        final GridCoverageReadParam param = new GridCoverageReadParam();
        param.setEnvelope(objBounds);
        param.setResolution(resolution);

        final CoverageReference ref = layer.getCoverageReference();
        GridCoverageReader reader = null;
        final GridCoverage2D coverage;
        try {
            reader = ref.acquireReader();
            coverage = (GridCoverage2D) reader.read(ref.getImageIndex(),param);
        } catch (CoverageStoreException ex) {
            context.getMonitor().exceptionOccured(ex, Level.INFO);
            return null;
        } finally {
            if (reader!= null) {
                try {
                    reader.dispose();
                } catch (CoverageStoreException e) {
                    context.getMonitor().exceptionOccured(e, Level.INFO);
                }
            }
        }

        if (coverage == null) {
            //no coverage for this BBOX
            return null;
        }

        final GeneralDirectPosition dp = new GeneralDirectPosition(objCRS);
        final Rectangle2D bounds2D = queryArea.getObjectiveShape().getBounds2D();
        dp.setOrdinate(0, bounds2D.getCenterX());
        dp.setOrdinate(1, bounds2D.getCenterY());

        float[] values = null;

        try{
            values = coverage.evaluate(dp, values);
        }catch(CannotEvaluateException ex){
            context.getMonitor().exceptionOccured(ex, Level.INFO);
            values = new float[coverage.getSampleDimensions().length];
            Arrays.fill(values, Float.NaN);
        }

        final List<Map.Entry<GridSampleDimension,Object>> results = new ArrayList<>();
        for (int i=0; i<values.length; i++){
            final GridSampleDimension sample = coverage.getSampleDimension(i);
            results.add(new AbstractMap.SimpleImmutableEntry<GridSampleDimension, Object>(sample, values[i]));
        }
        return results;
    }
}
