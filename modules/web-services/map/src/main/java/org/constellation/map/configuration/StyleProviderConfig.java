/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

package org.constellation.map.configuration;

import org.apache.sis.util.Static;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.EmbeddedDatabase;
import org.constellation.admin.dao.DataRecord;
import org.constellation.admin.dao.Session;
import org.constellation.admin.dao.StyleRecord;
import org.constellation.configuration.ConfigProcessException;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.StyleBrief;
import org.constellation.configuration.StyleReport;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.dto.StyleBean;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.provider.style.DeleteStyleToStyleProviderDescriptor;
import org.constellation.process.provider.style.SetStyleToStyleProviderDescriptor;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviderProxy;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.style.MutableFeatureTypeStyle;
import org.geotoolkit.style.MutableRule;
import org.geotoolkit.style.MutableStyle;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.style.LineSymbolizer;
import org.opengis.style.PointSymbolizer;
import org.opengis.style.PolygonSymbolizer;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.Symbolizer;
import org.opengis.style.TextSymbolizer;
import org.opengis.util.NoSuchIdentifierException;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 * @author Bernard Fabien (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public final class StyleProviderConfig extends Static {

    /**
     * Logger used for debugging and event notification.
     */
    private static final Logger LOGGER = Logging.getLogger(StyleProviderConfig.class);

    /**
     * Ensures that a style provider with the specified identifier really exists.
     *
     * @param providerId the style provider identifier
     * @throws TargetNotFoundException if the style provider instance can't be found
     */
    private static void ensureExistingProvider(final String providerId) throws TargetNotFoundException {
        if (StyleProviderProxy.getInstance().getProvider(providerId) == null) {
            throw new TargetNotFoundException("Style provider with identifier \"" + providerId + "\" does not exist.");
        }
    }

    /**
     * Ensures that a style with the specified identifier really exists from the style
     * provider with the specified identifier.
     *
     * @param providerId the style provider identifier
     * @param styleId    the style identifier
     * @throws TargetNotFoundException if the style instance can't be found
     */
    private static void ensureExistingStyle(final String providerId, final String styleId) throws TargetNotFoundException {
        ensureExistingProvider(providerId);
        if (!StyleProviderProxy.getInstance().getProvider(providerId).contains(styleId)) {
            throw new TargetNotFoundException("Style provider with identifier \"" + providerId + "\" does not contain style named \"" + styleId + "\".");
        }
    }

    /**
     * Builds a {@link StyleBrief} instance from a {@link StyleRecord} instance.
     *
     * @param record the record to be converted
     * @param locale the locale for internationalized text
     * @return a {@link StyleBrief} instance
     * @throws SQLException if a database access error occurs
     */
    public static StyleBrief getBriefFromRecord(final StyleRecord record, final Locale locale) throws SQLException {
        final StyleBrief brief = new StyleBrief();
        brief.setName(record.getName());
        brief.setProvider(record.getProvider().getIdentifier());
        brief.setTitle(record.getTitle(locale));
        brief.setDate(record.getDate());
        brief.setType(record.getType().name());
        brief.setOwner(record.getOwnerLogin());
        return brief;
    }

    /**
     * Creates a new style into a style provider instance.
     *
     * @param providerId the style provider identifier
     * @param style      the style body
     * @throws TargetNotFoundException if the style with the specified identifier can't be found
     * @throws ConfigurationException if the operation has failed for any reason
     */
    public static void createStyle(final String providerId, final MutableStyle style) throws ConfigurationException {
        ensureExistingProvider(providerId);
        createOrUpdateStyle(providerId, style.getName(), style);
    }

    /**
     * Returns the list of available styles as {@link StyleBean} object.
     *
     * @return a {@link List} of {@link StyleBean} instances
     */
    public static List<StyleBean> getAvailableStyles() {
        final List<StyleBean> beans = new ArrayList<>();
        for (final StyleProvider provider : StyleProviderProxy.getInstance().getProviders()) {
            if ("GO2".equals(provider.getId())) {
                continue; // skip "GO2" provider
            }
            try {
                beans.addAll(getAvailableStyles(provider.getId()));
            } catch (TargetNotFoundException ignore) { // couldn't happen
            }
        }
        return beans;
    }

    /**
     * Returns the list of available styles as {@link StyleBean} object for the style
     * provider with the specified identifier.
     *
     * @throws TargetNotFoundException if the style provider does not exist
     * @return a {@link List} of {@link StyleBean} instances
     */
    public static List<StyleBean> getAvailableStyles(final String providerId) throws TargetNotFoundException {
        ensureExistingProvider(providerId);
        final StyleProvider provider = StyleProviderProxy.getInstance().getProvider(providerId);
        final String folder = provider.getSource().groups("sldFolder").get(0).parameter("path").stringValue();

        final List<StyleBean> beans = new ArrayList<>();
        for (final String key : provider.getKeys()) {
            final StyleBean bean = new StyleBean();
            bean.setName(key);
            bean.setProviderId(providerId);
            if (new File(folder, key + ".xml").exists()) {
                bean.setDate(new Date(new File(folder, key + ".xml").lastModified()));
            } else if (new File(folder, key + ".sld").exists()) {
                bean.setDate(new Date(new File(folder, key + ".sld").lastModified()));
            }
            beans.add(bean);
        }
        return beans;
    }

    /**
     * Gets and returns the {@link MutableStyle} that matches with the specified identifier.
     *
     * @param providerId the style provider identifier
     * @param styleId    the style identifier
     * @return the {@link MutableStyle} instance
     * @throws TargetNotFoundException if the style with the specified identifier can't be found
     */
    public static MutableStyle getStyle(final String providerId, final String styleId) throws TargetNotFoundException {
        ensureExistingStyle(providerId, styleId);
        return StyleProviderProxy.getInstance().getProvider(providerId).get(styleId);
    }

    /**
     * Gets and returns a {@link StyleReport} instance that contains several information of an
     * existing style resource.
     * <p>
     * The report contains:
     * <ul>
     *     <li>Style record from administration database (name, provider, date, category).</li>
     *     <li>Style body information (description, symbolizer types).</li>
     *     <li>A list of data records for which the style has been explicitly declared as "applicable".</li>
     * </ul>
     *
     * @param providerId the style provider identifier
     * @param styleId    the style identifier
     * @param locale     the locale for report internationalization
     * @return a {@link StyleReport} instance
     * @throws TargetNotFoundException if the style with the specified identifier can't be found
     * @throws ConfigurationException if the operation has failed for any reason
     */
    public static StyleReport getStyleReport(final String providerId, final String styleId, final Locale locale) throws ConfigurationException {
        final StyleReport report = new StyleReport();

        // Extract information from the administration database.
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final StyleRecord record = session.readStyle(styleId, providerId);
            if (record != null) {
                report.setBrief(getBriefFromRecord(record, locale));
                report.setDescription(record.getDescription(locale));
                report.setTargetData(new ArrayList<DataBrief>());
                final List<DataRecord> data = record.getLinkedData();
                for (final DataRecord r : data) {
                    report.getTargetData().add(DataProviderConfig.getBriefFromRecord(r, locale));
                }
            } else {
                LOGGER.log(Level.WARNING, "Style named \"" + styleId + "\" from provider with id \"" + providerId + "\" can't be found from database.");
            }
        } catch (SQLException ex) {
            throw new ConfigurationException("An error occurred while reading data list for style named \"" + styleId + "\".", ex);
        } finally {
            if (session != null) session.close();
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
     * Updates an existing from a style provider instance.
     *
     * @param providerId the style provider identifier
     * @param styleId    the style identifier
     * @param style      the new style body
     * @throws TargetNotFoundException if the style with the specified identifier can't be found
     * @throws ConfigurationException if the operation has failed for any reason
     */
    public static void setStyle(final String providerId, final String styleId, final MutableStyle style) throws ConfigurationException {
        ensureExistingStyle(providerId, styleId);
        createOrUpdateStyle(providerId, styleId, style);
    }

    /**
     * Creates or updates a style into/from a style provider instance.
     *
     * @param providerId the style provider identifier
     * @param styleId    the style identifier
     * @param style      the new style body
     * @throws TargetNotFoundException if the style with the specified identifier can't be found
     * @throws ConfigurationException if the operation has failed for any reason
     */
    private static void createOrUpdateStyle(final String providerId, final String styleId, final MutableStyle style) throws ConfigurationException {
        final ProcessDescriptor desc = getProcessDescriptor(SetStyleToStyleProviderDescriptor.NAME);
        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
        inputs.parameter(SetStyleToStyleProviderDescriptor.PROVIDER_ID_NAME).setValue(providerId);
        inputs.parameter(SetStyleToStyleProviderDescriptor.STYLE_ID_NAME).setValue(styleId);
        inputs.parameter(SetStyleToStyleProviderDescriptor.STYLE_NAME).setValue(style);
        try {
            desc.createProcess(inputs).call();
        } catch (ProcessException ex) {
            throw new ConfigProcessException("Process to add/set style has reported an error.", ex);
        }
    }

    /**
     * Removes a style from a style provider instance.
     *
     * @param providerId the style provider identifier
     * @param styleId    the style identifier
     * @throws TargetNotFoundException if the style with the specified identifier can't be found
     * @throws ConfigurationException if the operation has failed for any reason
     */
    public static void deleteStyle(final String providerId, final String styleId) throws ConfigurationException {
        ensureExistingStyle(providerId, styleId);
        final ProcessDescriptor desc = getProcessDescriptor(DeleteStyleToStyleProviderDescriptor.NAME);
        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
        inputs.parameter(DeleteStyleToStyleProviderDescriptor.PROVIDER_ID_NAME).setValue(providerId);
        inputs.parameter(DeleteStyleToStyleProviderDescriptor.STYLE_ID_NAME).setValue(styleId);
        try {
            desc.createProcess(inputs).call();
        } catch (ProcessException ex) {
            throw new ConfigProcessException("Process to delete a style has reported an error.", ex);
        }
    }

    /**
     * Links a style resource to an existing data resource.
     *
     * @param styleProvider the style provider identifier
     * @param styleId       the style identifier
     * @param dataProvider  the data provider identifier
     * @param dataId        the data identifier
     * @throws TargetNotFoundException if the style with the specified identifier can't be found
     * @throws ConfigurationException if the operation has failed for any reason
     */
    public static void linkToData(final String styleProvider, final String styleId, final String dataProvider, final String dataId) throws ConfigurationException {
        ensureNonNull("dataProvider", dataProvider);
        ensureNonNull("dataId",       dataId);
        ensureExistingStyle(styleProvider, styleId);

        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final StyleRecord style = session.readStyle(styleId, styleProvider);
            if (style == null) {
                throw new ConfigurationException("Style named \"" + styleId + "\" from provider with id \"" + styleProvider + "\" can't be found from database.");
            }
            final DataRecord data = session.readData(dataId, dataProvider);
            if (data == null) {
                throw new ConfigurationException("Data named \"" + dataId + "\" from provider with id \"" + dataProvider + "\" can't be found from database.");
            }
            final List<StyleRecord> sylesList = session.readStyles(data);
            if(!sylesList.contains(style)){
                session.writeStyledData(style, data);
            }
        } catch (SQLException ex) {
            throw new ConfigurationException("An error occurred while trying to link the style named \"" + styleId + "\" to data named \"" + dataId + "\".", ex);
        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * Unlink a style resource from an existing data resource.
     *
     * @param styleProvider the style provider identifier
     * @param styleId       the style identifier
     * @param dataProvider  the data provider identifier
     * @param dataId        the data identifier
     * @throws TargetNotFoundException if the style with the specified identifier can't be found
     * @throws ConfigurationException if the operation has failed for any reason
     */
    public static void unlinkFromData(final String styleProvider, final String styleId, final String dataProvider, final String dataId) throws ConfigurationException {
        ensureNonNull("dataProvider", dataProvider);
        ensureNonNull("dataId",       dataId);
        ensureExistingStyle(styleProvider, styleId);

        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final StyleRecord style = session.readStyle(styleId, styleProvider);
            if (style == null) {
                throw new ConfigurationException("Style named \"" + styleId + "\" from provider with id \"" + styleProvider + "\" can't be found from database.");
            }
            final DataRecord data = session.readData(dataId, dataProvider);
            if (data == null) {
                throw new ConfigurationException("Data named \"" + dataId + "\" from provider with id \"" + dataProvider + "\" can't be found from database.");
            }
            session.deleteStyledData(style, data);
        } catch (SQLException ex) {
            throw new ConfigurationException("An error occurred while trying to unlink the style named \"" + styleId + "\" from data named \"" + dataId + "\".", ex);
        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * Returns a Constellation {@link ProcessDescriptor} from its name.
     *
     * @param name the process descriptor name
     * @return a {@link ProcessDescriptor} instance
     */
    private static ProcessDescriptor getProcessDescriptor(final String name) {
        try {
            return ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, name);
        } catch (NoSuchIdentifierException ex) { // unexpected
            throw new IllegalStateException("Unexpected error has occurred", ex);
        }
    }
}
