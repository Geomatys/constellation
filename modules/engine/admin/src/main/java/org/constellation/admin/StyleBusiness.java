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
package org.constellation.admin;

import org.apache.sis.util.logging.Logging;
import org.constellation.admin.dao.DataRecord;
import org.constellation.admin.dao.StyleRecord;
import org.constellation.admin.util.IOUtilities;
import org.constellation.configuration.*;
import org.constellation.dto.StyleBean;
import org.constellation.engine.register.*;
import org.constellation.engine.register.Layer;
import org.constellation.engine.register.Style;
import org.constellation.engine.register.repository.*;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.sld.*;
import org.geotoolkit.sld.xml.Specification;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.geotoolkit.style.MutableFeatureTypeStyle;
import org.geotoolkit.style.MutableRule;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.opengis.style.*;
import org.opengis.util.FactoryException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 * @author Bernard Fabien (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@Component
public final class StyleBusiness {

    @Inject
    StyleRepository styleRepository;

    @Inject
    DataRepository dataRepository;

    @Inject
    LayerRepository layerRepository;

    @Inject
    ServiceRepository serviceRepository;

    @Inject
    ProviderRepository providerRepository;
    
    @Inject
    private org.constellation.security.SecurityManager securityManager;
    
    private final StyleXmlIO sldParser = new StyleXmlIO();

    private static final MutableStyleFactory SF = (MutableStyleFactory)FactoryFinder.getStyleFactory(
                            new Hints(Hints.STYLE_FACTORY, MutableStyleFactory.class));
    /**
     * Logger used for debugging and event notification.
     */
    private static final Logger LOGGER = Logging.getLogger(StyleBusiness.class);

    /**
     * Ensures that a style provider with the specified identifier really
     * exists.
     *
     * @param providerId the style provider identifier
     * @throws TargetNotFoundException if the style provider instance can't be
     * found
     */
    private Provider ensureExistingProvider(final String providerId) throws TargetNotFoundException {
        final Provider provider = providerRepository.findByIdentifierAndType(providerId, "STYLE");
        if (provider == null) {
            throw new TargetNotFoundException("Style provider with identifier \"" + providerId + "\" does not exist.");
        }
        return provider;
    }

    /**
     * Ensures that a style with the specified identifier really exists from the
     * style provider with the specified identifier.
     *
     * @param providerId the style provider identifier
     * @param styleId the style identifier
     * @throws TargetNotFoundException if the style instance can't be found
     */
    private Style ensureExistingStyle(final String providerId, final String styleId) throws TargetNotFoundException {
        final Provider provider = ensureExistingProvider(providerId);
        final Style style = styleRepository.findByNameAndProvider(provider.getId(), styleId);
        if (style == null) {
            throw new TargetNotFoundException("Style provider with identifier \"" + providerId
                    + "\" does not contain style named \"" + styleId + "\".");
        }
        return style;
    }

    /**
     * Builds a {@link StyleBrief} instance from a {@link StyleRecord} instance.
     *
     * @param record the record to be converted
     * @param locale the locale for internationalized text
     * @return a {@link StyleBrief} instance
     * @throws SQLException if a database access error occurs
     */
    private StyleBrief getBriefFromRecord(final Style record, final Locale locale) throws SQLException {
        final Provider provider = providerRepository.findOne(record.getProvider());
        final StyleBrief brief = new StyleBrief();
        brief.setName(record.getName());
        brief.setProvider(provider.getIdentifier());
        //FIXME I18N
        brief.setTitle("TODO " + locale);
        brief.setDate(new Date(record.getDate()));
        brief.setType(record.getType());
        brief.setOwner(record.getOwner());
        return brief;
    }

    /**
     * Creates a new style into a style provider instance.
     *
     * @param providerId the style provider identifier
     * @param style the style body
     * @throws TargetNotFoundException if the style with the specified
     * identifier can't be found
     * @throws ConfigurationException if the operation has failed for any reason
     */
    public void createStyle(final String providerId, final MutableStyle style) throws ConfigurationException {
        ensureExistingProvider(providerId);
        createOrUpdateStyle(providerId, style.getName(), style);
    }

    /**
     * Returns the list of available styles as {@link StyleBean} object.
     *
     * @return a {@link List} of {@link StyleBean} instances
     */
    public List<StyleBrief> getAvailableStyles(final String category) {
        final List<StyleBrief> beans = new ArrayList<>();
        final List<Style> styles;
        if (category == null) {
            styles = styleRepository.findAll();
        } else {
            styles = styleRepository.findByType(category);
        }
        for (final Style style : styles) {
            final Provider provider = providerRepository.findOne(style.getId());
            if ("GO2".equals(provider.getIdentifier())) {
                continue; // skip "GO2" provider
            }
            final StyleBrief bean = new StyleBrief();
            bean.setName(style.getName());
            bean.setProvider(provider.getIdentifier());
            bean.setType(style.getType());
            bean.setOwner(style.getOwner());
            bean.setDate(new Date(style.getDate()));
            beans.add(bean);
        }
        return beans;
    }

    /**
     * Returns the list of available styles as {@link StyleBean} object for the
     * style provider with the specified identifier.
     *
     * @throws TargetNotFoundException if the style provider does not exist
     * @return a {@link List} of {@link StyleBean} instances
     */
    public List<StyleBrief> getAvailableStyles(final String providerId, final String category) throws TargetNotFoundException {
        final Provider provider = ensureExistingProvider(providerId);

        final List<Style> styles;
        if (category == null) {
            styles = styleRepository.findByProvider(provider.getId());
        } else {
            styles = styleRepository.findByTypeAndProvider(provider.getId(), category);
        }

        final List<StyleBrief> beans = new ArrayList<>();
        for (final Style style : styles) {
            final StyleBrief bean = new StyleBrief();
            bean.setName(style.getName());
            bean.setProvider(providerId);
            bean.setType(style.getType());
            bean.setOwner(style.getOwner());
            bean.setDate(new Date(style.getDate()));
            beans.add(bean);
        }
        return beans;
    }

//    /**
//     * Returns the list of available styles for dataId.
//     *
//     * @return a {@link List} of {@link StyleDTO} instances
//     */
//    public List<StyleDTO> findStyleByDataId(final QName dataname, final String providerId) {
//
//        List<StyleDTO> returnlist = new ArrayList<>();
//        Data data = dataRepository.findByNameAndNamespaceAndProviderIdentifier(dataname.getLocalPart(), dataname.getNamespaceURI(), providerId);
//        List<Style> styleList = styleRepository.findByData(data);
//        for (Style style : styleList) {
//            StyleDTO styleDTO = new StyleDTO();
//            try {
//                BeanUtils.copyProperties(styleDTO, style);
//            } catch (IllegalAccessException | InvocationTargetException e) {
//                throw new ConstellationException(e);
//            }
//            returnlist.add(styleDTO);
//        }
//        return returnlist;
//
//    }

    /**
     * Gets and returns the {@link MutableStyle} that matches with the specified
     * identifier.
     *
     * @param providerId the style provider identifier
     * @param styleId the style identifier
     * @return the {@link MutableStyle} instance
     * @throws TargetNotFoundException if the style with the specified
     * identifier can't be found
     */
    public MutableStyle getStyle(final String providerId, final String styleId) throws TargetNotFoundException {
        final Style style = ensureExistingStyle(providerId, styleId);
        return parseStyle(style.getName(), style.getBody());
    }

    /**
     * Gets and returns a {@link StyleReport} instance that contains several
     * information of an existing style resource.
     * <p>
     * The report contains:
     * <ul>
     * <li>Style record from administration database (name, provider, date,
     * category).</li>
     * <li>Style body information (description, symbolizer types).</li>
     * <li>A list of data records for which the style has been explicitly
     * declared as "applicable".</li>
     * </ul>
     *
     * @param providerId the style provider identifier
     * @param styleId the style identifier
     * @param locale the locale for report internationalization
     * @return a {@link StyleReport} instance
     * @throws TargetNotFoundException if the style with the specified
     * identifier can't be found
     * @throws ConfigurationException if the operation has failed for any reason
     */
    public StyleReport getStyleReport(final String providerId, final String styleId, final Locale locale) throws ConfigurationException {
        final StyleReport report = new StyleReport();

        final Provider provider = ensureExistingProvider(providerId);

        // Extract information from the administration database.
        try {
            final Style record = styleRepository.findByNameAndProvider(provider.getId(), styleId);
            if (record != null) {
                report.setBrief(getBriefFromRecord(record, locale));
                //FIXME I18N
                report.setDescription("TODO" + locale);
                report.setTargetData(new ArrayList<DataBrief>());
                final List<Data> data = styleRepository.getLinkedData(record.getId());
                for (final Data r : data) {
                    report.getTargetData().add(getBriefFromRecord(r, locale));
                }
            } else {
                LOGGER.log(Level.WARNING, "Style named \"" + styleId + "\" from provider with id \"" + providerId
                        + "\" can't be found from database.");
            }
        } catch (SQLException ex) {
            throw new ConfigurationException("An error occurred while reading data list for style named \"" + styleId + "\".", ex);
        }

        // Extract additional information from the style body.
        final MutableStyle style = getStyle(providerId, styleId);
        for (final MutableFeatureTypeStyle fts : style.featureTypeStyles()) {
            for (final MutableRule rule : fts.rules()) {
                for (final Symbolizer symbolizer : rule.symbolizers()) {
                    if (symbolizer instanceof PointSymbolizer && !report.getSymbolizerTypes().contains("point")) {
                        report.getSymbolizerTypes().add("point");
                    } else if (symbolizer instanceof LineSymbolizer && !report.getSymbolizerTypes().contains("line")) {
                        report.getSymbolizerTypes().add("line");
                    } else if (symbolizer instanceof PolygonSymbolizer && !report.getSymbolizerTypes().contains("polygon")) {
                        report.getSymbolizerTypes().add("polygon");
                    } else if (symbolizer instanceof TextSymbolizer && !report.getSymbolizerTypes().contains("text")) {
                        report.getSymbolizerTypes().add("text");
                    } else if (symbolizer instanceof RasterSymbolizer && !report.getSymbolizerTypes().contains("raster")) {
                        report.getSymbolizerTypes().add("raster");
                    }
                }
            }
        }

        return report;
    }

    /**
     * Builds a {@link DataBrief} instance from a {@link DataRecord} instance.
     *
     * @param record the record to be converted
     * @param locale the locale for internationalized text
     * @return a {@link DataBrief} instance
     * @throws SQLException if a database access error occurs
     */
    private DataBrief getBriefFromRecord(final Data record, final Locale locale) throws SQLException {
        final Provider provider = providerRepository.findOne(record.getId());
        final DataBrief brief = new DataBrief();
        brief.setName(record.getName());
        brief.setNamespace(record.getNamespace());
        brief.setProvider(provider.getIdentifier());
        //FIXME I18N
        brief.setTitle("TODO " + locale);
        brief.setDate(new Date(record.getDate()));
        brief.setType(record.getType());
        brief.setOwner(record.getOwner());
        return brief;
    }

    /**
     * Updates an existing from a style provider instance.
     *
     * @param providerId the style provider identifier
     * @param styleId the style identifier
     * @param style the new style body
     * @throws TargetNotFoundException if the style with the specified
     * identifier can't be found
     * @throws ConfigurationException if the operation has failed for any reason
     */
    public void setStyle(final String providerId, final String styleId, final MutableStyle style)
            throws ConfigurationException {
        ensureExistingStyle(providerId, styleId);
        createOrUpdateStyle(providerId, styleId, style);
    }

    /**
     * Creates or updates a style into/from a style provider instance.
     *
     * @param providerId the style provider identifier
     * @param styleId the style identifier
     * @param style the new style body
     * @throws TargetNotFoundException if the style with the specified
     * identifier can't be found
     * @throws ConfigurationException if the operation has failed for any reason
     */
    private void createOrUpdateStyle(final String providerId, String styleId, final MutableStyle style)
            throws ConfigurationException {

        // Proceed style name.
        if (isBlank(styleId)) {
            if (isBlank(style.getName())) {
                throw new ConfigurationException("Unable to create/update the style. No specified style name.");
            } else {
                styleId = style.getName();
            }
        } else {
            style.setName(styleId);
        }
        
        // Retrieve or not the provider instance.
        final Provider provider = providerRepository.findByIdentifier(providerId);
        if (provider == null) {
            throw new ConfigurationException("Unable to set the style named \"" + style.getName() + "\". Provider with id \"" + providerId + "\" not found.");
        }
        
        final StringWriter sw = new StringWriter();
        final StyleXmlIO util = new StyleXmlIO();
        try {
            util.writeStyle(sw, style, Specification.StyledLayerDescriptor.V_1_1_0);
        } catch (JAXBException ex) {
            throw new ConfigurationException(ex);
        }
        
        final Style s = styleRepository.findByNameAndProvider(provider.getId(), styleId);
        if (s != null) {
            s.setBody(sw.toString());
            styleRepository.save(s);
        } else {
            final Style newStyle = new Style(styleId, provider.getId(), getTypeFromMutableStyle(style), new Date().getTime(), sw.toString(), securityManager.getCurrentUserLogin());
            styleRepository.create(newStyle);
        }
        
    }

    /**
     * Removes a style from a style provider instance.
     *
     * @param providerId the style provider identifier
     * @param styleId the style identifier
     * @throws TargetNotFoundException if the style with the specified
     * identifier can't be found
     * @throws ConfigurationException if the operation has failed for any reason
     */
    public void deleteStyle(final String providerId, final String styleId) throws ConfigurationException {
        ensureNonNull("providerId", providerId);
        ensureNonNull("styleId", styleId);
        final Provider provider = ensureExistingProvider(providerId);
        ensureExistingStyle(providerId, styleId);

        styleRepository.deleteStyle(provider.getId(), styleId);
    }

    /**
     * Links a style resource to an existing data resource.
     *
     * @param styleProvider the style provider identifier
     * @param styleId the style identifier
     * @param dataProvider the data provider identifier
     * @param dataId the data identifier
     * @throws TargetNotFoundException if the style with the specified
     * identifier can't be found
     * @throws ConfigurationException if the operation has failed for any reason
     */
    public void linkToData(final String styleProvider, final String styleId, final String dataProvider, final QName dataId)
            throws ConfigurationException {
        ensureNonNull("dataProvider", dataProvider);
        ensureNonNull("dataId", dataId);
        ensureExistingStyle(styleProvider, styleId);
        final Style style = styleRepository.findByName(styleId);
        if (style == null) {
            throw new ConfigurationException("Style named \"" + styleId + "\" can't be found from database.");
        }
        Data data = dataRepository.findDataFromProvider(dataId.getNamespaceURI(), dataId.getLocalPart(), dataProvider);
        if (data == null) {
            throw new ConfigurationException("Data named \"" + dataId + "\" from provider with id \"" + dataProvider
                    + "\" can't be found from database.");
        }
        styleRepository.linkStyleToData(style.getId(), data.getId());

    }

    /**
     * Unlink a style resource from an existing data resource.
     *
     * @param styleProvider the style provider identifier
     * @param styleId the style identifier
     * @param dataProvider the data provider identifier
     * @param dataId the data identifier
     * @throws TargetNotFoundException if the style with the specified
     * identifier can't be found
     * @throws ConfigurationException if the operation has failed for any reason
     */
    public void unlinkFromData(final String styleProvider, final String styleId, final String dataProvider,
            final QName dataId) throws ConfigurationException {
        ensureNonNull("dataProvider", dataProvider);
        ensureNonNull("dataId", dataId);
        ensureExistingStyle(styleProvider, styleId);
        final Style style = styleRepository.findByName(styleId);
        if (style == null) {
            throw new ConfigurationException("Style named \"" + styleId + "\" can't be found from database.");
        }
        final Data data = dataRepository.findDataFromProvider(dataId.getNamespaceURI(), dataId.getLocalPart(), dataProvider);
        if (data == null) {
            throw new ConfigurationException("Data named \"" + dataId + "\" from provider with id \"" + dataProvider
                    + "\" can't be found from database.");
        }
        styleRepository.unlinkStyleToData(style.getId(), data.getId());
    }

    public void removeStyleFromLayer(String serviceIdentifier, String serviceType, String layerName, String styleProviderId, String styleName) {
        Service service = serviceRepository.findByIdentifierAndType(serviceIdentifier, serviceType);
        Layer layer = layerRepository.findByServiceIdAndLayerName(service.getId(), layerName);
        Style style = styleRepository.findByName(styleName);
        styleRepository.unlinkStyleToLayer(style.getId(), layer.getId());

    }

    public void createOrUpdateStyleFromLayer(String serviceType, String serviceIdentifier, String layerName, String styleProviderId, String styleName) {
        Service service = serviceRepository.findByIdentifierAndType(serviceIdentifier, serviceType);
        Layer layer = layerRepository.findByServiceIdAndLayerName(service.getId(), layerName);
        Style style = styleRepository.findByName(styleName);
        styleRepository.linkStyleToLayer(style.getId(), layer.getId());
    }

    private MutableStyle parseStyle(final String name, final String xml) {
        MutableStyle value = null;
        final StringReader sr = new StringReader(xml);
        final String baseErrorMsg = "SLD Style ";
        
        //try SLD 1.1
        try {
            final MutableStyledLayerDescriptor sld = sldParser.readSLD(sr, Specification.StyledLayerDescriptor.V_1_1_0);
            value = getFirstStyle(sld);
            if (value != null) {
                value.setName(name);
                LOGGER.log(Level.FINE, "{0}{1} is an SLD 1.1.0", new Object[]{baseErrorMsg, name});
                return value;
            }
        } catch (JAXBException | FactoryException ex) { /* dont log*/ }

        //try SLD 1.0
        try {
            final MutableStyledLayerDescriptor sld = sldParser.readSLD(sr, Specification.StyledLayerDescriptor.V_1_0_0);
            value = getFirstStyle(sld);
            if (value != null) {
                value.setName(name);
                LOGGER.log(Level.FINE, "{0}{1} is an SLD 1.0.0", new Object[]{baseErrorMsg, name});
                return value;
            }
        } catch (JAXBException | FactoryException ex) { /* dont log*/ }

        //try UserStyle SLD 1.1
        try {
            value = sldParser.readStyle(sr, Specification.SymbologyEncoding.V_1_1_0);
            if (value != null) {
                value.setName(name);
                LOGGER.log(Level.FINE, "{0}{1} is a UserStyle SLD 1.1.0", new Object[]{baseErrorMsg, name});
                return value;
            }
        } catch (JAXBException | FactoryException ex) { /* dont log*/ }

        //try UserStyle SLD 1.0
        try {
            value = sldParser.readStyle(sr, Specification.SymbologyEncoding.SLD_1_0_0);
            if (value != null) {
                value.setName(name);
                LOGGER.log(Level.FINE, "{0}{1} is a UserStyle SLD 1.0.0", new Object[]{baseErrorMsg, name});
                return value;
            }
        } catch (JAXBException | FactoryException ex) { /* dont log*/ }

        //try FeatureTypeStyle SE 1.1
        try {
            final MutableFeatureTypeStyle fts = sldParser.readFeatureTypeStyle(sr, Specification.SymbologyEncoding.V_1_1_0);
            value = SF.style();
            value.featureTypeStyles().add(fts);
            value.setName(name);
            LOGGER.log(Level.FINE, "{0}{1} is FeatureTypeStyle SE 1.1", new Object[]{baseErrorMsg, name});
            return value;
            
        } catch (JAXBException |FactoryException ex) { /* dont log*/ }

        //try FeatureTypeStyle SLD 1.0
        try {
            final MutableFeatureTypeStyle fts = sldParser.readFeatureTypeStyle(sr, Specification.SymbologyEncoding.SLD_1_0_0);
            value = SF.style();
            value.featureTypeStyles().add(fts);
            value.setName(name);
            LOGGER.log(Level.FINE, "{0}{1} is an FeatureTypeStyle SLD 1.0", new Object[]{baseErrorMsg, name});
            return value;
            
        } catch (JAXBException  | FactoryException ex) { /* dont log*/ }
        
        return value;
    }
    
    private static MutableStyle getFirstStyle(final MutableStyledLayerDescriptor sld){
        if(sld == null) return null;
        for(final MutableLayer layer : sld.layers()){
            if(layer instanceof MutableNamedLayer){
                final MutableNamedLayer mnl = (MutableNamedLayer) layer;
                for(final MutableLayerStyle stl : mnl.styles()){
                    if(stl instanceof MutableStyle){
                        return (MutableStyle) stl;
                    }
                }
            }else if(layer instanceof MutableUserLayer){
                final MutableUserLayer mnl = (MutableUserLayer) layer;
                for(final MutableStyle stl : mnl.styles()){
                    return stl;
                }
            }
        }
        return null;
    }
    
    private static String getTypeFromMutableStyle(final MutableStyle style) {
        for (final MutableFeatureTypeStyle fts : style.featureTypeStyles()) {
            for (final MutableRule rule : fts.rules()) {
                for (final Symbolizer symbolizer : rule.symbolizers()) {
                    if (symbolizer instanceof RasterSymbolizer) {
                        return "COVERAGE";
                    }
                }
            }
        }
        return "VECTOR";
    }


    public void writeStyle(final String name, final Integer providerId, final StyleRecord.StyleType type, final MutableStyle body) throws IOException {
        final String login = securityManager.getCurrentUserLogin();
        Style style = new Style();
        style.setBody(IOUtilities.writeStyle(body));
        style.setDate(System.currentTimeMillis());
        style.setName(name);
        style.setOwner(login);
        style.setProvider(providerId);
        style.setType(type.name());
        styleRepository.create(style);
    }


}
