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
package org.constellation.provider.coveragesgroup;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.sis.measure.MeasurementRange;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.ServiceDef.Query;
import org.constellation.business.IStyleBusiness;
import org.constellation.provider.AbstractData;
import org.constellation.provider.coveragesgroup.util.ConvertersJaxbToGeotk;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.feature.type.PropertyDescriptor;
import org.geotoolkit.filter.visitor.DefaultFilterVisitor;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.MutableStyle;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.PropertyName;
import org.opengis.geometry.Envelope;
import org.opengis.util.GenericName;

/**
 *
 * @author Cédric Briançon (Geomatys)
 * @author Quentin Boileau (Geomatys)
 */
public class CoveragesGroupLayerDetails extends AbstractData {
    private static final Logger LOGGER = Logging.getLogger("org.constellation.provider.coveragesgroup");

    private MapContext ctxt;

    private MarshallerPool pool;
    private Unmarshaller unmarshaller;

    public CoveragesGroupLayerDetails(final GenericName name, final File file, final IStyleBusiness styleBusiness) {
        this(name, file, null, null, styleBusiness);
    }

    /**
     * hacked method to pass the login/pass to WebMapServer
     */
    public CoveragesGroupLayerDetails(final GenericName name, final File file, final String login, final String password, final IStyleBusiness styleBusiness) {
        super(name, Collections.EMPTY_LIST);

        // Parsing ctxt : MapBuilder.createContext
        try {
            ctxt = createMapContextForFile(file, login, password, styleBusiness);
        } catch (JAXBException e) {
            LOGGER.log(Level.INFO, "Unable to convert map context file into a valid object", e);
        }
    }

    private MapContext createMapContextForFile(final File file, final String login, final String password, final IStyleBusiness styleBusiness) throws JAXBException {
        pool = new MarshallerPool(JAXBContext.newInstance(org.constellation.provider.coveragesgroup.xml.MapContext.class, org.apache.sis.internal.jaxb.geometry.ObjectFactory.class), null);
        unmarshaller = pool.acquireUnmarshaller();
        final Object result = unmarshaller.unmarshal(file);
        if (!(result instanceof org.constellation.provider.coveragesgroup.xml.MapContext)) {
            throw new JAXBException("Wrong response for the unmarshalling");
        }
        final org.constellation.provider.coveragesgroup.xml.MapContext mapContext = (org.constellation.provider.coveragesgroup.xml.MapContext)result;
        return ConvertersJaxbToGeotk.convertsMapContext(mapContext,login, password, styleBusiness);
    }

    @Override
    public SortedSet<Date> getAvailableTimes() throws DataStoreException {
        return new TreeSet<Date>();
    }

    @Override
    public SortedSet<Number> getAvailableElevations() throws DataStoreException {
        return new TreeSet<Number>();
    }

    @Override
    public GridCoverage2D getCoverage(Envelope envelope, Dimension dimension, Double elevation, Date time) throws DataStoreException, IOException {
        return null;
    }

    @Override
    public Envelope getEnvelope() throws DataStoreException {
        try {
            return ctxt.getBounds();
        } catch (IOException ex) {
            throw new DataStoreException(ex);
        }
    }

    @Override
    public MapItem getMapLayer(MutableStyle style, Map<String, Object> params) throws PortrayalException {
        if (params != null) {
            final Map<String,?> extras = (Map<String, ?>) params.get(KEY_EXTRA_PARAMETERS);
            if (extras != null) {
                Filter filter = null;
                for (String key : extras.keySet()) {
                    if (key.equalsIgnoreCase("cql_filter")) {
                        final String cqlFilter = ((List) extras.get(key)).get(0).toString();
                        if (cqlFilter != null) {
                            filter = buildCQLFilter(cqlFilter, filter);
                        }
                    } else if (key.startsWith("dim_") || key.startsWith("DIM_")) {
                        final String dimValue = ((List) extras.get(key)).get(0).toString();
                        final String dimName = key.substring(4);
                        filter = buildDimFilter(dimName, dimValue, filter);
                    }
                }
                if (filter != null) {
                    setFilter(ctxt, filter);
                }
            }
        }

        setSelectableAndVisible(ctxt);
        return ctxt;
    }


    /**
     * Set selectable and visible to {@code true} recursively for all map items.
     *
     * @param item A {@link MapItem} that could embed others, if it is a {@link MapContext}
     *             for example.
     */
    private static void setSelectableAndVisible(final MapItem item) {
        if (item instanceof MapLayer) {
            ((MapLayer) item).setSelectable(true);
        }
        item.setVisible(true);

        for (MapItem it : item.items()) {
            setSelectableAndVisible(it);
        }
    }

    /**
     * Apply filter for all {@link MapItem} recursively.
     *
     * @param item
     * @param filter
     */
    private void setFilter(final MapItem item, final Filter filter) {
        if (item instanceof FeatureMapLayer) {
            final FeatureMapLayer fml = (FeatureMapLayer) item;
            final PropertyIsEqualToFilterVisitor myVisit = new PropertyIsEqualToFilterVisitor();
            // load PropertyIsEqualTo filters
            filter.accept(myVisit, null);

            // Remove filters that can't apply to this layer.
            for (int i=myVisit.props.size() - 1; i>=0; i--) {
                final PropertyIsEqualTo tempProp = myVisit.props.get(i);
                final Collection<PropertyDescriptor> propsDesc = fml.getCollection().getFeatureType().getDescriptors();
                final String propName = ((PropertyName)(tempProp).getExpression1()).getPropertyName();
                boolean found = false;
                for (PropertyDescriptor prop : propsDesc) {
                    if (prop.getName().tip().toString().equalsIgnoreCase(propName)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    myVisit.props.remove(tempProp);
                }
            }

            final Filter newFilter;
            if (myVisit.props.isEmpty()) {
                newFilter = Filter.INCLUDE;
            } else if (myVisit.props.size() == 1) {
                newFilter = myVisit.props.get(0);
            } else {
                newFilter = GO2Utilities.FILTER_FACTORY.and((List) myVisit.props);
            }

            final QueryBuilder qb = new QueryBuilder(fml.getQuery());
            qb.setFilter(newFilter);
            fml.setQuery(qb.buildQuery());

            return;
        }

        for (final MapItem m : item.items()) {
            setFilter(m, filter);
        }
    }

    @Override
    public MeasurementRange<?>[] getSampleValueRanges() {
        return new MeasurementRange<?>[0];
    }

    @Override
    public boolean isQueryable(Query query) {
        return true;
    }

    @Override
    public TYPE getType() {
        return TYPE.COVERAGE;
    }

    /**
     * Visitor to find {@link PropertyIsEqualTo} filters in a {@link And} filter.
     */
    private class PropertyIsEqualToFilterVisitor extends DefaultFilterVisitor {
        private final List<PropertyIsEqualTo> props = new ArrayList<PropertyIsEqualTo>();

        @Override
        public Object visit(PropertyIsEqualTo filter, Object data) {
            props.add(filter);
            return super.visit(filter, data);
        }

    }
}
