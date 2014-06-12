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

package org.constellation.webservice.map.component;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.dao.DataRecord;
import org.constellation.admin.dao.StyleRecord;
import org.constellation.admin.dao.StyleRecord.StyleType;
import org.constellation.admin.dto.LayerDTO;
import org.constellation.admin.dto.StyleDTO;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.StyleBrief;
import org.constellation.configuration.StyleReport;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.dto.StyleBean;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Layer;
import org.constellation.engine.register.Service;
import org.constellation.engine.register.Style;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.LayerRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.constellation.engine.register.repository.StyleRepository;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviders;
import org.geotoolkit.style.MutableFeatureTypeStyle;
import org.geotoolkit.style.MutableRule;
import org.geotoolkit.style.MutableStyle;
import org.opengis.style.LineSymbolizer;
import org.opengis.style.PointSymbolizer;
import org.opengis.style.PolygonSymbolizer;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.Symbolizer;
import org.opengis.style.TextSymbolizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    /**
     * Logger used for debugging and event notification.
     */
    private static final Logger LOGGER = Logging.getLogger(StyleBusiness.class);

    /**
     * Ensures that a style provider with the specified identifier really
     * exists.
     *
     * @param providerId
     *            the style provider identifier
     * @throws TargetNotFoundException
     *             if the style provider instance can't be found
     */
    private static void ensureExistingProvider(final String providerId) throws TargetNotFoundException {
        if (StyleProviders.getInstance().getProvider(providerId) == null) {
            throw new TargetNotFoundException("Style provider with identifier \"" + providerId + "\" does not exist.");
        }
    }

    /**
     * Ensures that a style with the specified identifier really exists from the
     * style provider with the specified identifier.
     *
     * @param providerId
     *            the style provider identifier
     * @param styleId
     *            the style identifier
     * @throws TargetNotFoundException
     *             if the style instance can't be found
     */
    private static void ensureExistingStyle(final String providerId, final String styleId) throws TargetNotFoundException {
        ensureExistingProvider(providerId);
        if (!StyleProviders.getInstance().getProvider(providerId).contains(styleId)) {
            throw new TargetNotFoundException("Style provider with identifier \"" + providerId
                    + "\" does not contain style named \"" + styleId + "\".");
        }
    }

    /**
     * Builds a {@link StyleBrief} instance from a {@link StyleRecord} instance.
     *
     * @param record
     *            the record to be converted
     * @param locale
     *            the locale for internationalized text
     * @return a {@link StyleBrief} instance
     * @throws SQLException
     *             if a database access error occurs
     */
    private static StyleBrief getBriefFromRecord(final StyleRecord record, final Locale locale) throws SQLException {
        final StyleBrief brief = new StyleBrief();
        brief.setName(record.getName());
        brief.setProvider(record.getProvider().getIdentifier());
        //FIXME I18N
        brief.setTitle("TODO " + locale);
        brief.setDate(record.getDate());
        brief.setType(record.getType().name());
        brief.setOwner(record.getOwnerLogin());
        return brief;
    }

    /**
     * Creates a new style into a style provider instance.
     *
     * @param providerId
     *            the style provider identifier
     * @param style
     *            the style body
     * @throws TargetNotFoundException
     *             if the style with the specified identifier can't be found
     * @throws ConfigurationException
     *             if the operation has failed for any reason
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
        for (final StyleProvider provider : StyleProviders.getInstance().getProviders()) {
            if ("GO2".equals(provider.getId())) {
                continue; // skip "GO2" provider
            }
            try {
                beans.addAll(getAvailableStyles(provider.getId(), category));
            } catch (TargetNotFoundException ignore) { // couldn't happen
            }
        }
        return beans;
    }

    /**
     * Returns the list of available styles as {@link StyleBean} object for the
     * style provider with the specified identifier.
     *
     * @throws TargetNotFoundException
     *             if the style provider does not exist
     * @return a {@link List} of {@link StyleBean} instances
     */
    public List<StyleBrief> getAvailableStyles(final String providerId, final String category)
            throws TargetNotFoundException {
        ensureExistingProvider(providerId);
        final StyleProvider provider = StyleProviders.getInstance().getProvider(providerId);
        final List<StyleBrief> beans = new ArrayList<>();
        for (final String key : provider.getKeys()) {
            StyleRecord record = ConfigurationEngine.getStyle(key, providerId);
            final StyleType typeSearched;
            if (category == null) {
                typeSearched = StyleType.ALL;
            } else {
                typeSearched = StyleType.valueOf(category.toUpperCase());
            }
            if (typeSearched.equals(StyleType.ALL) || record.getType().equals(typeSearched)) {
                final StyleBrief bean = new StyleBrief();
                bean.setName(key);
                bean.setProvider(providerId);
                bean.setType(record.getType().name());
                bean.setOwner(record.getOwnerLogin());
                bean.setDate(record.getDate());
                beans.add(bean);
            }
        }
        return beans;
    }
    
    /**
     * Returns the list of available styles for dataId.
     *
     * @return a {@link List} of {@link StyleDTO} instances
     */
    public List<StyleDTO> findStyleByDataId(final QName dataname, final String providerId){
    	
    	List<StyleDTO> returnlist = new ArrayList<StyleDTO>();
    	Data data = dataRepository.findByNameAndNamespaceAndProviderId(dataname.getLocalPart(), dataname.getNamespaceURI(), providerId);
    	List<Style> styleList = styleRepository.findByData(data);
		for (Style style : styleList) {
			StyleDTO styleDTO = new StyleDTO();
			try {
				BeanUtils.copyProperties(styleDTO, style);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new ConstellationException(e);
			}
			returnlist.add(styleDTO);
		}
		return returnlist;
        
    }
    

    /**
     * Gets and returns the {@link MutableStyle} that matches with the specified
     * identifier.
     *
     * @param providerId
     *            the style provider identifier
     * @param styleId
     *            the style identifier
     * @return the {@link MutableStyle} instance
     * @throws TargetNotFoundException
     *             if the style with the specified identifier can't be found
     */
    public MutableStyle getStyle(final String providerId, final String styleId) throws TargetNotFoundException {
        ensureExistingStyle(providerId, styleId);
        return StyleProviders.getInstance().getProvider(providerId).get(styleId);
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
     * @param providerId
     *            the style provider identifier
     * @param styleId
     *            the style identifier
     * @param locale
     *            the locale for report internationalization
     * @return a {@link StyleReport} instance
     * @throws TargetNotFoundException
     *             if the style with the specified identifier can't be found
     * @throws ConfigurationException
     *             if the operation has failed for any reason
     */
    public StyleReport getStyleReport(final String providerId, final String styleId, final Locale locale)
            throws ConfigurationException {
        final StyleReport report = new StyleReport();

        // Extract information from the administration database.
        try {
            final StyleRecord record = ConfigurationEngine.getStyle(styleId, providerId);
            if (record != null) {
                report.setBrief(getBriefFromRecord(record, locale));
                //FIXME I18N
                report.setDescription("TODO" + locale);
                report.setTargetData(new ArrayList<DataBrief>());
                final List<DataRecord> data = record.getLinkedData();
                for (final DataRecord r : data) {
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
    private static DataBrief getBriefFromRecord(final DataRecord record, final Locale locale) throws SQLException {
        final DataBrief brief = new DataBrief();
        brief.setName(record.getName());
        brief.setNamespace(record.getNamespace());
        brief.setProvider(record.getProvider().getIdentifier());
        //FIXME I18N
        brief.setTitle("TODO " + locale);
        brief.setDate(record.getDate());
        brief.setType(record.getType().name());
        brief.setOwner(record.getOwnerLogin());
        return brief;
    }

    /**
     * Updates an existing from a style provider instance.
     *
     * @param providerId
     *            the style provider identifier
     * @param styleId
     *            the style identifier
     * @param style
     *            the new style body
     * @throws TargetNotFoundException
     *             if the style with the specified identifier can't be found
     * @throws ConfigurationException
     *             if the operation has failed for any reason
     */
    public void setStyle(final String providerId, final String styleId, final MutableStyle style)
            throws ConfigurationException {
        ensureExistingStyle(providerId, styleId);
        createOrUpdateStyle(providerId, styleId, style);
    }

    /**
     * Creates or updates a style into/from a style provider instance.
     *
     * @param providerId
     *            the style provider identifier
     * @param styleId
     *            the style identifier
     * @param style
     *            the new style body
     * @throws TargetNotFoundException
     *             if the style with the specified identifier can't be found
     * @throws ConfigurationException
     *             if the operation has failed for any reason
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
        }else {
            style.setName(styleId);
        }
        // Retrieve or not the provider instance.
        final StyleProvider provider = StyleProviders.getInstance().getProvider(providerId);
        if (provider == null) {
            throw new ConfigurationException("Unable to set the style named \"" + style.getName() + "\". Provider with id \"" + providerId + "\" not found.");
        }

        // Add style into provider.
        provider.set(styleId, style);
    }

    /**
     * Removes a style from a style provider instance.
     *
     * @param providerId
     *            the style provider identifier
     * @param styleId
     *            the style identifier
     * @throws TargetNotFoundException
     *             if the style with the specified identifier can't be found
     * @throws ConfigurationException
     *             if the operation has failed for any reason
     */
    public void deleteStyle(final String providerId, final String styleId) throws ConfigurationException {
        ensureNonNull("providerId", providerId);
        ensureNonNull("styleId", styleId);
        ensureExistingStyle(providerId, styleId);
        final StyleProvider provider = StyleProviders.getInstance().getProvider(providerId);
        provider.remove(styleId);
    }

    /**
     * Links a style resource to an existing data resource.
     *
     * @param styleProvider
     *            the style provider identifier
     * @param styleId
     *            the style identifier
     * @param dataProvider
     *            the data provider identifier
     * @param dataId
     *            the data identifier
     * @throws TargetNotFoundException
     *             if the style with the specified identifier can't be found
     * @throws ConfigurationException
     *             if the operation has failed for any reason
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
     * @param styleProvider
     *            the style provider identifier
     * @param styleId
     *            the style identifier
     * @param dataProvider
     *            the data provider identifier
     * @param dataId
     *            the data identifier
     * @throws TargetNotFoundException
     *             if the style with the specified identifier can't be found
     * @throws ConfigurationException
     *             if the operation has failed for any reason
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

	public void removeStyleFromLayer(String serviceIdentifier,String serviceType, String layerName, String styleProviderId, String styleName) {
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

	
    

    
}
