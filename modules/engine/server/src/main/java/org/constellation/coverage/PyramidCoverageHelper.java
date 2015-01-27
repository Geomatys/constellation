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
package org.constellation.coverage;

import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.ArraysExt;
import org.geotoolkit.coverage.*;
import org.geotoolkit.coverage.filestore.FileCoverageStoreFactory;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.grid.GridGeometry2D;
import org.geotoolkit.coverage.io.GridCoverageReadParam;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.coverage.memory.MPCoverageStore;
import org.geotoolkit.coverage.postgresql.PGCoverageStoreFactory;
import org.geotoolkit.coverage.xmlstore.XMLCoverageStoreFactory;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.image.interpolation.InterpolationCase;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.process.ProcessListener;
import org.geotoolkit.referencing.adapters.NetcdfCRS;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import org.constellation.admin.SpringHelper;
import org.constellation.engine.register.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Helper class to ease the pyramid process.
 *
 * @author olivier.nouguier@geomatys.com
 */
public class PyramidCoverageHelper {

    public interface IBuilder {

        WithInput fromImage(String string) throws MalformedURLException;

        IBuilder withInterpolation(InterpolationCase bilinear);

        IBuilder inputFormat(String inputFormat);

        IBuilder withDeeps(double[] deeps);

        IBuilder withEnvelope(Envelope deeps);

        IBuilder withTile(int width, int height);

        IBuilder withBaseCoverageNamer(CoverageNamer coverageNamer);

    }

    private final CoverageStore store;
    private CoverageStore outputCoverageStore;
    private PyramidCoverageBuilder pyramidCoverageBuilder;
    private String baseCoverageName;
    private CoverageNamer coverageNamer;
    private List<GridCoverage2D> coveragesPyramid;
    private double[] depth;
    private Envelope envelope;
    
    @Autowired
    private PropertyRepository propertyRepository;

    public PyramidCoverageHelper(Builder builder) throws DataStoreException {
        SpringHelper.injectDependencies(this);
        this.store = builder.buildInputStore();
        final List<GridCoverage2D> coverages = buildCoverages();

        if (coverages.size() > 0) {
            coveragesPyramid = coverages;
            this.outputCoverageStore = builder.buildOutputStore();

            this.pyramidCoverageBuilder = new PyramidCoverageBuilder(
                    new Dimension(builder.tileWidth, builder.tileHeight),
                    builder.interpolation, 1);

            this.coverageNamer = builder.coverageNamer;
            this.baseCoverageName = builder.baseCoverageName;

            this.depth = builder.depth;
            this.envelope = builder.envelope;
        }
    }

    /**
     * Builder factory with a base name for coverage.
     *
     * @param name
     *            of coverage
     * @return Builder instance
     */
    public static IBuilder builder(String name) {
        Builder builder = new Builder(name);
        return builder;
    }

    public CoverageStore getCoverageStore() {
        return outputCoverageStore;
    }

    /**
     * Build coverage list which can be pyramided
     *
     * @return a {@link org.geotoolkit.coverage.grid.GridCoverage2D}
     *         {@link java.util.List}
     * @throws CancellationException
     * @throws DataStoreException
     */
    private List<GridCoverage2D> buildCoverages() throws CancellationException,
            DataStoreException {
        final List<GridCoverage2D> coverages = new ArrayList<>(0);

        for (Name name : store.getNames()) {
            final CoverageReference ref = store.getCoverageReference(name);
            final GridCoverageReader reader = ref.acquireReader();

            final GridCoverageReadParam param = new GridCoverageReadParam();
            param.setDeferred(true);
            final GridCoverage coverage = reader.read(ref.getImageIndex(), param);
            ref.recycle(reader);

            if (coverage instanceof GridCoverageStack) {
                // TODO handle stack
            } else {
                final GridCoverage2D coverage2D = (GridCoverage2D) coverage;
                final GridGeometry2D gridGeometry = coverage2D.getGridGeometry();

                if ((gridGeometry.getCoordinateReferenceSystem() instanceof NetcdfCRS)) {
                    //FIXME normalize CRS ?
                    break;
                }

                // Always pyramid coverage for WMTS
                coverages.add(coverage2D);
            }
        }
        return coverages;
    }

    public Map<Envelope, double[]> getResolutionPerEnvelope() {
        Map<Envelope, double[]> map = new HashMap<>();
        if (envelope != null) {
            map.put(envelope, depth);
        }
        return map;
    }

    public PyramidCoverageBuilder getPyramidCoverageBuilder() {
        return pyramidCoverageBuilder;
    }

    public List<GridCoverage2D> getCoveragesPyramid() {
        return coveragesPyramid;
    }

    /**
     * Build pyramid and give a {@link org.geotoolkit.process.ProcessListener}
     * to
     *
     * @param listener
     * @throws DataStoreException
     * @throws TransformException
     * @throws FactoryException
     */
    public void buildPyramid(final ProcessListener listener)
            throws DataStoreException, TransformException, FactoryException, IOException {

        List<GridCoverage2D> coverages = getCoveragesPyramid();
        int coverageCount = 1;
        for (GridCoverage2D coverage : coverages) {

            final Map<Envelope, double[]> resolution_Per_Envelope = getResolutionPerEnvelope();
            if (resolution_Per_Envelope.isEmpty()) {
                final Envelope coverageEnv = coverage.getEnvelope();
                final GridGeometry2D gg = coverage.getGridGeometry();
                int gridspan = gg.getExtent2D().getSpan(0);

                //calculate scales
                final double spanX = coverageEnv.getSpan(0);
                final double baseScale = spanX / gridspan;
                double scale = spanX / 256;
                double[] scales = new double[0];
                while (true) {
                    if (scale <= baseScale) {
                        //fit to exact match to preserve base quality.
                        scale = baseScale;
                    }
                    scales = ArraysExt.insert(scales, scales.length, 1);
                    scales[scales.length - 1] = scale;

                    if (scale <= baseScale) {
                        break;
                    }
                    scale = scale / 2;
                }
                resolution_Per_Envelope.put(coverageEnv, scales);
            }

            pyramidCoverageBuilder.create(coverage, getCoverageStore(),
                    coverageNamer.getName(baseCoverageName, coverageCount),
                    resolution_Per_Envelope, null, listener, null);
            coverageCount++;
        }
    }

    /**
     * Command that produce coverage names during the pyramid generation.
     *
     * @author olivier.nouguier@geomatys.com
     */
    public interface CoverageNamer {
        /**
         * @param baseName
         * @param n
         * @return
         */
        DefaultName getName(String baseName, int n);
    }

    /**
     * Exposed builder when output is set.
     *
     * @author olivier.nouguier@geomatys.com
     */
    public static interface WithOutput {

        WithOutput outputFormat(String output);

        PyramidCoverageHelper build() throws DataStoreException;
    }

    /**
     * Exposed when PGStorage is set.
     *
     * @author olivier.nouguier@geomatys.com
     */
    public interface WithPGOutput extends WithOutput {
        WithPGOutput withHostname(String hostname);

        WithPGOutput withPgPort(int port);

        WithPGOutput withSchema(String schema);

    }

    public static interface WithInput {

        WithOutput toFileStore(String path) throws MalformedURLException;

        WithOutput toMemoryStore();

        WithPGOutput toPostGisStore(String databaseName, String login,
                String password);
    }

    /**
     * Non exposed builder.
     *
     * @author olivier.nouguier@geomatys.com
     */
    private static abstract class WithOutputImpl implements WithOutput {
        Builder builder;

        public WithOutputImpl(Builder builder) {
            this.builder = builder;
        }

        @Override
        public WithOutput outputFormat(String output) {
            builder.outputFormat = output;
            return this;
        }

        abstract CoverageStore createOutputStore() throws DataStoreException;

        @Override
        public PyramidCoverageHelper build() throws DataStoreException {
            PyramidCoverageHelper helper = new PyramidCoverageHelper(builder);

            return helper;
        }

    }

    /**
     * Only exposed to this classes.
     *
     * @author olivier.nouguier@geomatys.com
     */
    public static abstract class WithInputImpl implements WithInput {

        Builder builder;

        public WithInputImpl(final Builder builder) {
            this.builder = builder;
        }

        @Override
        public WithOutput toFileStore(final String path) throws MalformedURLException {
            final WithFileOutput fileOutput = new WithFileOutput(builder);
            fileOutput.tileFolder = new File(path, "tiles").toURI().toURL();
            builder.output = fileOutput;
            return fileOutput;
        }

        @Override
        public WithOutput toMemoryStore() {
            final WithMemoryOutput memoryOutput = new WithMemoryOutput(builder);
            builder.output = memoryOutput;
            return memoryOutput;
        }

        @Override
        public WithPGOutput toPostGisStore(String databaseName, String login,
                                           String password) {
            final WithPGOutputImpl pgOutput = new WithPGOutputImpl(builder);
            pgOutput.pgDatabaseName = databaseName;
            pgOutput.pgLogin = login;
            pgOutput.pgPassword = password;

            builder.output = pgOutput;

            return pgOutput;

        }

        protected abstract CoverageStore buildInputStore(String inputFormat)
                throws DataStoreException;

        /**
         * Inner builder when output is set to file.
         *
         * @author olivier.nouguier@geomatys.com
         */
        private static class WithFileOutput extends WithOutputImpl {
            public URL tileFolder;

            public WithFileOutput(Builder builder) {
                super(builder);
            }

            @Override
            protected CoverageStore createOutputStore() throws DataStoreException {

                final XMLCoverageStoreFactory factory = new XMLCoverageStoreFactory();
                Map<String, Serializable> parameters = new HashMap<>();
                parameters.put("path", tileFolder);

                parameters.put("type", builder.outputFormat);

                return factory.create(parameters);

            }

        }

        private static class WithMemoryOutput extends WithOutputImpl {

            public WithMemoryOutput(Builder builder) {
                super(builder);
            }

            @Override
            public CoverageStore createOutputStore() {
                return new MPCoverageStore();
            }

        }

        public static class WithPGOutputImpl extends WithOutputImpl implements WithPGOutput {
            private String pgPassword;
            private String pgLogin;
            private String pgDatabaseName;
            private String pgHostname = "localhost";
            private int pgPort = 5432;
            private String pgSchema = "pgcoverage";

            public WithPGOutputImpl(Builder builder) {
                super(builder);
            }

            @Override
            public WithPGOutput withHostname(String hostname) {
                this.pgHostname = hostname;
                return this;
            }

            @Override
            public WithPGOutput withPgPort(int port) {
                this.pgPort = port;
                return this;
            }

            @Override
            public WithPGOutput withSchema(String schema) {
                this.pgSchema = schema;
                return this;
            }

            @Override
            protected CoverageStore createOutputStore()
                    throws DataStoreException {

                PGCoverageStoreFactory coverageStoreFactory = new PGCoverageStoreFactory();
                ParameterDescriptorGroup parametersDescriptor = coverageStoreFactory
                        .getParametersDescriptor();
                ParameterValueGroup params = parametersDescriptor.createValue();
                params.parameter("host").setValue(pgHostname);
                params.parameter("port").setValue(pgPort);
                params.parameter("database").setValue(pgDatabaseName);
                params.parameter("user").setValue(pgLogin);
                params.parameter("password").setValue(pgPassword);
                params.parameter("schema").setValue(pgSchema);

                return coverageStoreFactory.create(params);

            }

        }

    }

    /**
     * Inner builder when input is set.
     *
     * @author olivier.nouguier@geomatys.com
     */

    private static class WithFileInput extends WithInputImpl {

        public URL imageFile;

        public WithFileInput(final Builder builder) {
            super(builder);
        }

        @Override
        protected CoverageStore buildInputStore(final String inputFormat)
                throws DataStoreException {

            final ParameterValueGroup params = FileCoverageStoreFactory.PARAMETERS_DESCRIPTOR
                    .createValue();
            Parameters.getOrCreate(FileCoverageStoreFactory.PATH, params)
                    .setValue(imageFile);
            Parameters.getOrCreate(FileCoverageStoreFactory.TYPE, params)
                    .setValue(inputFormat);

            return CoverageStoreFinder.open(params);
        }

    }

    /**
     * External builder.
     *
     * @author Olivier NOUGUIER
     */
    public static class Builder implements IBuilder {

        /**
         * Mandatory property, it is the base name used to define coverage(s)
         * name: eg: name="forest_" then coverages names are "forest_1",
         * "forest_2"
         */
        // General inputs.
        final private String baseCoverageName;
        private int tileWidth = 256;
        private int tileHeight = 256;
        private String outputFormat = "PNG";
        private String inputFormat = "geotiff";
        private double[] depth = new double[] { 1, 0.5, 0.25, 0.125 };
        private Envelope envelope = null;
        private InterpolationCase interpolation = InterpolationCase.BILINEAR;
        private CoverageNamer coverageNamer = new CoverageNamer() {

            @Override
            public DefaultName getName(String baseName, int n) {
                return new DefaultName(baseName + n);
            }
        };
        private WithInputImpl input;
        private WithOutputImpl output;

        private Builder(String baseCoverageName) {
            this.baseCoverageName = baseCoverageName;
        }

        @Override
        public IBuilder inputFormat(String inputFormat) {
            this.inputFormat = inputFormat;
            return this;
        }

        @Override
        public IBuilder withDeeps(double[] deeps) {
            this.depth = deeps;
            return this;
        }

        @Override
        public IBuilder withEnvelope(Envelope envelope) {
            this.envelope = envelope;
            return this;
        }

        @Override
        public IBuilder withTile(int width, int height) {
            this.tileWidth = width;
            this.tileHeight = height;
            return this;
        }

        @Override
        public IBuilder withInterpolation(InterpolationCase interpolation) {
            this.interpolation = interpolation;
            return this;
        }

        @Override
        public WithInput fromImage(String path) throws MalformedURLException {
            WithFileInput fileInput = new WithFileInput(this);
            fileInput.imageFile = new File(path).toURI().toURL();
            input = fileInput;
            return input;

        }

        @Override
        public IBuilder withBaseCoverageNamer(CoverageNamer coverageNamer) {
            this.coverageNamer = coverageNamer;
            return this;
        }

        public CoverageStore buildOutputStore() throws DataStoreException {
            return output.createOutputStore();
        }

        private CoverageStore buildInputStore() throws DataStoreException {
            return input.buildInputStore(inputFormat);
        }

    }

}
