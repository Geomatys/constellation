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

import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.iso.DefaultInternationalString;
import org.constellation.admin.StyleBusiness;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.StyleListBrief;
import org.constellation.json.binding.Style;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.rest.dto.AutoIntervalValuesDTO;
import org.constellation.rest.dto.AutoUniqueValuesDTO;
import org.constellation.rest.dto.WrapperIntervalDTO;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.coverage.io.GridCoverageReadParam;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.process.coverage.statistics.ImageStatistics;
import org.geotoolkit.style.DefaultDescription;
import org.geotoolkit.style.DefaultLineSymbolizer;
import org.geotoolkit.style.DefaultMutableStyle;
import org.geotoolkit.style.DefaultPointSymbolizer;
import org.geotoolkit.style.DefaultPolygonSymbolizer;
import org.geotoolkit.style.MutableRule;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.geotoolkit.style.StyleConstants;
import org.geotoolkit.style.function.Categorize;
import org.geotoolkit.style.function.Interpolate;
import org.geotoolkit.style.interval.DefaultIntervalPalette;
import org.geotoolkit.style.interval.IntervalPalette;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.PropertyName;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.style.Fill;
import org.opengis.style.Graphic;
import org.opengis.style.GraphicalSymbol;
import org.opengis.style.LineSymbolizer;
import org.opengis.style.Mark;
import org.opengis.style.PointSymbolizer;
import org.opengis.style.PolygonSymbolizer;
import org.opengis.style.Stroke;
import org.opengis.style.Symbolizer;
import org.opengis.util.NoSuchIdentifierException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.constellation.utils.RESTfulUtilities.ok;
import static org.geotoolkit.style.StyleConstants.DEFAULT_DESCRIPTION;
import static org.geotoolkit.style.StyleConstants.DEFAULT_DISPLACEMENT;
import static org.geotoolkit.style.StyleConstants.DEFAULT_UOM;

/**
 * RESTful API for style providers configuration.
 *
 * @author Bernard Fabien (Geomatys)
 * @author Mehdi Sidhoum (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@Path("/1/SP")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public final class StyleRest {
    
    @Inject
    private StyleBusiness styleBusiness;

    /**
     * @see StyleBusiness#createStyle(String, MutableStyle)
     */
    @PUT
    @Path("{id}/style")
    public Response createStyle(final @PathParam("id") String id, final DefaultMutableStyle style) throws Exception {
        styleBusiness.createStyle(id, style);
        return ok(AcknowlegementType.success("Style named \"" + style.getName() + "\" successfully added to provider with id \"" + id + "\"."));
    }

    /**
     * @see StyleBusiness#createStyle(String, MutableStyle)
     */
    @PUT
    @Path("{id}/style/create")
    public Response createStyleJson(final @PathParam("id") String id, final Style style) throws Exception {
        styleBusiness.createStyle(id, style.toType());
        return ok(AcknowlegementType.success("Style named \"" + style.getName() + "\" successfully added to provider with id \"" + id + "\"."));
    }

    /**
     * @see StyleBusiness#getStyle(String, String)
     */
    @GET
    @Path("{id}/style/{styleId}")
    public Response getStyle(final @PathParam("id") String id, final @PathParam("styleId") String styleId) throws Exception {
    	return ok(new Style(styleBusiness.getStyle(id, styleId)));
    }

    /**
     * @see StyleBusiness#getFunctionColorMap(String, String, String)
     */
    @GET
    @Path("{id}/style/{styleId}/{ruleName}")
    public Response getPaletteStyle(@PathParam("id") String id, @PathParam("styleId") String styleId, @PathParam("ruleName") String ruleName) throws Exception{
    	Function function = styleBusiness.getFunctionColorMap(id, styleId, ruleName);
    	if(function instanceof Categorize){
    		return ok(new org.constellation.json.binding.Categorize((Categorize)function).getPoints());
    	}else if(function instanceof Interpolate){
    		return ok(new org.constellation.json.binding.Interpolate((Interpolate)function).getPoints());
    	}
    	return ok(new AcknowlegementType("Failure", "function unknow"));
    }

    /**
     * Creates a style and calculate the rules as palette defined as interval set.
     * Returns the new style object as json.
     * @param id the style provider identifier.
     * @param wrapper object that contains the style and the config parameter to generate the palette rules.
     * @return the style as json.
     * @throws Exception
     */
    @POST
    @Path("{id}/style/generateAutoInterval")
    public Response generateAutoIntervalStyle(final @PathParam("id") String id, final WrapperIntervalDTO wrapper) throws Exception {
        //get style and interval params
        final Style style = wrapper.getStyle();
        final AutoIntervalValuesDTO intervalValues = wrapper.getIntervalValues();

        final String dataProviderId = wrapper.getDataProvider();
        final String layerName = wrapper.getLayerName();

        final String attribute = intervalValues.getAttr();
        if(attribute ==null || attribute.trim().isEmpty()){
            return ok(AcknowlegementType.failure("Attribute field should not be empty!"));
        }

        final String method = intervalValues.getMethod();
        final int intervals = intervalValues.getNbIntervals();

        final String symbolizerType = intervalValues.getSymbol();
        final List<String> colorsList = intervalValues.getColors();

        //rules that will be added to the style
        final List<MutableRule> newRules = new ArrayList<MutableRule>();

        /*
         * I - Get feature type and feature data.
         */
        final DataProvider dataprovider = DataProviders.getInstance().getProvider(dataProviderId);
        final DataStore dataStore = dataprovider.getMainStore();
        final Name typeName = new DefaultName(layerName);
        final QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.setTypeName(typeName);
        queryBuilder.setProperties(new String[]{attribute});
        if(dataStore instanceof FeatureStore) {
            final FeatureStore fstore = (FeatureStore) dataStore;
            final FeatureCollection featureCollection = fstore.createSession(false).getFeatureCollection(queryBuilder.buildQuery());

            /*
            * II - Search extreme values.
            */
            final Set<Double> values = new HashSet<Double>();
            double minimum = Double.POSITIVE_INFINITY;
            double maximum = Double.NEGATIVE_INFINITY;

            final MutableStyleFactory SF = (MutableStyleFactory) FactoryFinder.getStyleFactory(
                    new Hints(Hints.STYLE_FACTORY, MutableStyleFactory.class));
            final FilterFactory2 FF = (FilterFactory2) FactoryFinder.getFilterFactory(
                    new Hints(Hints.FILTER_FACTORY, FilterFactory2.class));

            final PropertyName property = FF.property(attribute);

            final FeatureIterator it = featureCollection.iterator();
            while(it.hasNext()){
                final Feature feature = it.next();
                final Number number = property.evaluate(feature, Number.class);
                final Double value = number.doubleValue();
                values.add(value);
                if (value < minimum) {
                    minimum = value;
                }
                if (value > maximum) {
                    maximum = value;
                }
            }
            it.close();

            /*
            * III - Analyze values.
            */
            final Double[] allValues = values.toArray(new Double[values.size()]);
            double[] interValues = new double[0];
            if ("equidistant".equals(method)) {
                interValues = new double[intervals + 1];
                for (int i = 0; i < interValues.length; i++) {
                    interValues[i] = minimum + (float) i / (interValues.length - 1) * (maximum - minimum);
                }
            } else if ("mediane".equals(method)) {
                interValues = new double[intervals + 1];
                for (int i = 0; i < interValues.length; i++) {
                    interValues[i] = allValues[i * (allValues.length - 1) / (interValues.length - 1)];
                }
            } else {
                if (interValues.length != intervals + 1) {
                    interValues = Arrays.copyOf(interValues, intervals + 1);
                }
            }

            /*
            * IV - Generate rules deriving symbolizer with given colors.
            */
            final Symbolizer symbolizer = createSymbolizer(symbolizerType, SF, FF);
            final Color[] colors = new Color[colorsList.size()];
            int loop = 0;
            for(final String c : colorsList){
                colors[loop] = Color.decode(c);
                loop++;
            }
            final IntervalPalette palette = new DefaultIntervalPalette(colors);
            int count = 0;

            /*
             * Create one rule for each interval.
             */
            for (int i = 1; i < interValues.length; i++) {
                final double step = (double) (i - 1) / (interValues.length - 2); // derivation step
                double start = interValues[i - 1];
                double end = interValues[i];
                /*
                * Create the interval filter.
                */
                final Filter above = FF.greaterOrEqual(property, FF.literal(start));
                final Filter under;
                if (i == interValues.length - 1) {
                    under = FF.lessOrEqual(property, FF.literal(end));
                } else {
                    under = FF.less(property, FF.literal(end));
                }
                final Filter interval = FF.and(above, under);
                /*
                * Create new rule deriving the base symbolizer.
                */
                final MutableRule rule = SF.rule();
                rule.setName((count++)+" - AutoInterval - " + property.getPropertyName());
                rule.setDescription(new DefaultDescription(new DefaultInternationalString(property.getPropertyName()+" "+start+" - "+end),null));
                rule.setFilter(interval);
                rule.symbolizers().add(derivateSymbolizer(symbolizer, palette.interpolate(step),SF,FF));
                newRules.add(rule);
            }
        }

        //add rules to the style
        final MutableStyle mutableStyle = style.toType();
        //remove all auto intervals rules if exists before adding the new list.
        final List<MutableRule> backupRules = new ArrayList<MutableRule>(mutableStyle.featureTypeStyles().get(0).rules());
        final List<MutableRule> rulesToRemove = new ArrayList<MutableRule>();
        for(final MutableRule r : backupRules){
            if(r.getName().contains("AutoInterval")){
                rulesToRemove.add(r);
            }
        }
        backupRules.removeAll(rulesToRemove);
        mutableStyle.featureTypeStyles().get(0).rules().clear();
        mutableStyle.featureTypeStyles().get(0).rules().addAll(backupRules);
        mutableStyle.featureTypeStyles().get(0).rules().addAll(newRules);

        //create the style in server
        styleBusiness.createStyle(id, mutableStyle);

        return ok(new Style(mutableStyle));
    }

    private Symbolizer createSymbolizer(final String symbolizerType, final MutableStyleFactory SF, final FilterFactory2 FF) {
        final Symbolizer symbolizer;
        if ("polygon".equals(symbolizerType)) {
            final Stroke stroke = SF.stroke(Color.BLACK, 1);
            final Fill fill = SF.fill(Color.BLUE);
            symbolizer = new DefaultPolygonSymbolizer(
                    stroke,
                    fill,
                    DEFAULT_DISPLACEMENT,
                    FF.literal(0),
                    DEFAULT_UOM,
                    null,
                    "polygon",
                    DEFAULT_DESCRIPTION);
        } else if ("line".equals(symbolizerType)) {
            final Stroke stroke = SF.stroke(Color.BLUE, 2);
            symbolizer = new DefaultLineSymbolizer(
                    stroke,
                    FF.literal(0),
                    DEFAULT_UOM,
                    null,
                    "line",
                    DEFAULT_DESCRIPTION);
        } else {
            final Stroke stroke = SF.stroke(Color.BLACK, 1);
            final Fill fill = SF.fill(Color.BLUE);
            final List<GraphicalSymbol> symbols = new ArrayList<GraphicalSymbol>();
            symbols.add(SF.mark(StyleConstants.MARK_CIRCLE, fill, stroke));
            final Graphic gra = SF.graphic(symbols, FF.literal(1), FF.literal(12), FF.literal(0), SF.anchorPoint(), SF.displacement());
            symbolizer = new DefaultPointSymbolizer(
                    gra,
                    DEFAULT_UOM,
                    null,
                    "point",
                    DEFAULT_DESCRIPTION);
        }
        return symbolizer;
    }

    private Symbolizer derivateSymbolizer(final Symbolizer symbol, final Color color, final MutableStyleFactory SF, final FilterFactory2 FF) {
        if (symbol instanceof PolygonSymbolizer) {
            final PolygonSymbolizer ps = (PolygonSymbolizer) symbol;
            final Fill fill = SF.fill(SF.literal(color), ps.getFill().getOpacity());
            return SF.polygonSymbolizer(ps.getName(), ps.getGeometryPropertyName(),
                    ps.getDescription(), ps.getUnitOfMeasure(), ps.getStroke(),
                    fill, ps.getDisplacement(), ps.getPerpendicularOffset());
        } else if (symbol instanceof LineSymbolizer) {
            final LineSymbolizer ls = (LineSymbolizer) symbol;
            final Stroke oldStroke = ls.getStroke();
            final Stroke stroke = SF.stroke(SF.literal(color), oldStroke.getOpacity(), oldStroke.getWidth(),
                    oldStroke.getLineJoin(), oldStroke.getLineCap(), oldStroke.getDashArray(), oldStroke.getDashOffset());
            return SF.lineSymbolizer(ls.getName(), ls.getGeometryPropertyName(),
                    ls.getDescription(), ls.getUnitOfMeasure(), stroke, ls.getPerpendicularOffset());
        } else if (symbol instanceof PointSymbolizer) {
            final PointSymbolizer ps = (PointSymbolizer) symbol;
            final Graphic oldGraphic = ps.getGraphic();
            final Mark oldMark = (Mark) oldGraphic.graphicalSymbols().get(0);
            final Fill fill = SF.fill(SF.literal(color), oldMark.getFill().getOpacity());
            final List<GraphicalSymbol> symbols = new ArrayList<GraphicalSymbol>();
            symbols.add(SF.mark(oldMark.getWellKnownName(), fill, oldMark.getStroke()));
            final Graphic graphic = SF.graphic(symbols, oldGraphic.getOpacity(), oldGraphic.getSize(),
                    oldGraphic.getRotation(), oldGraphic.getAnchorPoint(), oldGraphic.getDisplacement());
            return SF.pointSymbolizer(graphic, ps.getGeometryPropertyName());
        } else {
            throw new IllegalArgumentException("Unexpected symbolizer type: " + symbol);
        }
    }

    /**
     * Creates a style and calculate the rules as palette defined as unique values set.
     * Returns the new style object as json.
     * @param id style provider identifier
     * @param wrapper object that contains the style and the config parameter to generate the palette rules.
     * @return new style as json
     * @throws Exception
     */
    @POST
    @Path("{id}/style/generateAutoUnique")
    public Response generateAutoUniqueStyle(final @PathParam("id") String id, final WrapperIntervalDTO wrapper) throws Exception {
        //get style and interval params
        final Style style = wrapper.getStyle();
        final AutoUniqueValuesDTO autoUniqueValues = wrapper.getUniqueValues();

        final String dataProviderId = wrapper.getDataProvider();
        final String layerName = wrapper.getLayerName();

        final String attribute = autoUniqueValues.getAttr();
        if(attribute ==null || attribute.trim().isEmpty()){
            return ok(AcknowlegementType.failure("Attribute field should not be empty!"));
        }

        final String symbolizerType = autoUniqueValues.getSymbol();
        final List<String> colorsList = autoUniqueValues.getColors();

        //rules that will be added to the style
        final List<MutableRule> newRules = new ArrayList<MutableRule>();

        /*
         * I - Get feature type and feature data.
         */
        final DataProvider dataprovider = DataProviders.getInstance().getProvider(dataProviderId);
        final DataStore dataStore = dataprovider.getMainStore();
        final Name typeName = new DefaultName(layerName);
        final QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.setTypeName(typeName);
        queryBuilder.setProperties(new String[]{attribute});
        if(dataStore instanceof FeatureStore) {
            final FeatureStore fstore = (FeatureStore) dataStore;
            final FeatureCollection featureCollection = fstore.createSession(false).getFeatureCollection(queryBuilder.buildQuery());
            /*
            * II - Extract all different values.
            */
            final MutableStyleFactory SF = (MutableStyleFactory) FactoryFinder.getStyleFactory(
                    new Hints(Hints.STYLE_FACTORY, MutableStyleFactory.class));
            final FilterFactory2 FF = (FilterFactory2) FactoryFinder.getFilterFactory(
                    new Hints(Hints.FILTER_FACTORY, FilterFactory2.class));
            final PropertyName property = FF.property(attribute);
            final List<Object> differentValues = new ArrayList<Object>();

            final FeatureIterator it = featureCollection.iterator();
            while(it.hasNext()){
                final Feature feature = it.next();
                final Object value = property.evaluate(feature);
                if (!differentValues.contains(value)) {
                    differentValues.add(value);
                }
            }
            it.close();
            /*
            * III - Generate rules deriving symbolizer with colors array.
            */
            final Symbolizer symbolizer = createSymbolizer(symbolizerType, SF, FF);
            final Color[] colors = new Color[colorsList.size()];
            int loop = 0;
            for(final String c : colorsList){
                colors[loop] = Color.decode(c);
                loop++;
            }
            final IntervalPalette palette = new DefaultIntervalPalette(colors);
            int count = 0;
            /*
            * Create one rule for each different value.
            */
            for (int i = 0; i < differentValues.size(); i++) {
                final double step = ((double) i) / (differentValues.size() - 1); // derivation step
                final Object value = differentValues.get(i);
                /*
                 * Create the unique value filter.
                 */
                final Filter filter;
                if(value instanceof String && !value.toString().isEmpty() && value.toString().contains("'")){
                    final String val = ((String) value).replaceAll("'","\\"+"'");
                    filter = FF.like(property,FF.literal(val).toString(),"*","?","\\",true);
                }else {
                    filter = FF.equals(property, FF.literal(value));
                }

                /*
                 * Create new rule derivating the base symbolizer.
                 */
                final MutableRule rule = SF.rule(derivateSymbolizer(symbolizer, palette.interpolate(step),SF,FF));
                rule.setName((count++)+" - AutoUnique - " + property.getPropertyName());
                final Object valStr = value instanceof String && ((String) value).isEmpty() ? "''":value;
                rule.setDescription(new DefaultDescription(new DefaultInternationalString(property.getPropertyName()+" = "+valStr),null));
                rule.setFilter(filter);
                newRules.add(rule);
            }
        }

        //add rules to the style
        final MutableStyle mutableStyle = style.toType();
        //remove all auto unique values rules if exists before adding the new list.
        final List<MutableRule> backupRules = new ArrayList<MutableRule>(mutableStyle.featureTypeStyles().get(0).rules());
        final List<MutableRule> rulesToRemove = new ArrayList<MutableRule>();
        for(final MutableRule r : backupRules){
            if(r.getName().contains("AutoUnique")){
                rulesToRemove.add(r);
            }
        }
        backupRules.removeAll(rulesToRemove);
        mutableStyle.featureTypeStyles().get(0).rules().clear();
        mutableStyle.featureTypeStyles().get(0).rules().addAll(backupRules);
        mutableStyle.featureTypeStyles().get(0).rules().addAll(newRules);

        //create the style in server
        styleBusiness.createStyle(id, mutableStyle);
        return ok(new Style(mutableStyle));
    }

    
    /**
     * @see StyleBusiness#getAvailableStyles(String)
     */
    @GET
    @Path("{id}/style/available")
    public Response getAvailableStyles(final @PathParam("id") String id) throws Exception {
        return ok(new StyleListBrief(styleBusiness.getAvailableStyles(id, null)));
    }

    /**
     * @see StyleBusiness#getAvailableStyles(String)
     */
    @GET
    @Path("all/style/available")
    public Response getAvailableStyles() throws Exception {
        return ok(new StyleListBrief(styleBusiness.getAvailableStyles("ALL")));
    }

    /**
     * @see StyleBusiness#getAvailableStyles(String)
     */
    @GET
    @Path("all/style/available/{category}")
    public Response getCategoryAvailableStyles(@PathParam("category") String category) throws Exception {
        return ok(new StyleListBrief(styleBusiness.getAvailableStyles(category)));
    }

    /**
     * @see StyleBusiness#setStyle(String, String, MutableStyle)
     */
    @POST
    @Path("{id}/style/{styleId}")
    public Response updateStyle(final @PathParam("id") String id, final @PathParam("styleId") String styleId, final MutableStyle style) throws Exception {
        styleBusiness.setStyle(id, styleId, style);
        return ok(AcknowlegementType.success("Style named \"" + styleId + "\" successfully updated."));
    }

    /**
     * @see StyleBusiness#createStyle(String, MutableStyle)
     */
    @PUT
    @Path("{id}/style/{styleId}/update")
    public Response updateStyleJson(final @PathParam("id") String id, final @PathParam("styleId") String styleId, final Style style) throws Exception {
        styleBusiness.setStyle(id, styleId, style.toType());
        return ok(AcknowlegementType.success("Style named \"" + style.getName() + "\" successfully updated to provider with id \"" + id + "\"."));
    }

    /**
     * @see StyleBusiness#deleteStyle(String, String)
     */
    @DELETE
    @Path("{id}/style/{styleId}")
    public Response deleteStyle(final @PathParam("id") String id, final @PathParam("styleId") String styleId) throws Exception {
        styleBusiness.deleteStyle(id, styleId);
        return ok(AcknowlegementType.success("Style named \"" + styleId + "\" successfully removed from provider with id \"" + id + "\"."));
    }

    /**
     * @see StyleBusiness#getStyleReport(String, String,Locale)
     */
    @GET
    @Path("{id}/style/{styleId}/report")
    public Response getStyleReport(final @Context HttpServletRequest request, final @PathParam("id") String id, final @PathParam("styleId") String styleId) throws Exception {
        return ok(styleBusiness.getStyleReport(id, styleId, request.getLocale()));
    }

    /**
     * @see StyleBusiness#linkToData(String, String, String, String)
     */
    @POST
    @Path("{id}/style/{styleId}/linkData")
    public Response linkToData(final @PathParam("id") String id, final @PathParam("styleId") String styleId, final ParameterValues values) throws Exception {
        styleBusiness.linkToData(id, styleId, values.get("dataProvider"), new QName(values.get("dataNamespace"), values.get("dataId")));
        return ok(AcknowlegementType.success("Style named \"" + styleId + "\" successfully linked to data named \"" + values.get("dataId") + "\"."));
    }

    /**
     * @see StyleBusiness#unlinkFromData(String, String, String, String)
     */
    @POST
    @Path("{id}/style/{styleId}/unlinkData")
    public Response unlinkFromData(final @PathParam("id") String id, final @PathParam("styleId") String styleId, final ParameterValues values) throws Exception {
        styleBusiness.unlinkFromData(id, styleId, values.get("dataProvider"), new QName(values.get("dataNamespace"), values.get("dataId")));
        return ok(AcknowlegementType.success("Style named \"" + styleId + "\" successfully unlinked from data named \"" + values.get("dataId") + "\"."));
    }
    
    @GET
    @Path("restart")
    public Response restartStyleProviders() throws Exception {
        org.constellation.provider.StyleProviders.getInstance().reload();
        return ok(new AcknowlegementType("Success", "All style providers have been restarted."));
    }

   @POST
   @Path("statistics")
   public Response getHistogram(final ParameterValues values) throws NoSuchIdentifierException, ProcessException, DataStoreException {
       final DataProvider provider = DataProviders.getInstance().getProvider(values.get("dataProvider"));
       final CoverageReference coverageReference = ((CoverageStore) provider.getMainStore()).getCoverageReference(new DefaultName(values.get("dataId")));
       GridCoverageReadParam params = new GridCoverageReadParam();
       params.setDeferred(true);
       final GridCoverage coverage = coverageReference.acquireReader().read(coverageReference.getImageIndex(), params);

       //call process to get Histograms
       final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor("coverage", "statistic");
       final ParameterValueGroup procparams = desc.getInputDescriptor().createValue();
       procparams.parameter("inCoverage").setValue(coverage);
       final org.geotoolkit.process.Process process = desc.createProcess(procparams);
       final ParameterValueGroup result = process.call();
       ImageStatistics statistics = (ImageStatistics) result.parameter("outStatistic").getValue();
       return ok(statistics);
   }

}
