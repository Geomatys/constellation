package org.constellation.coverage;

import org.apache.sis.storage.DataStoreException;
import org.constellation.configuration.ConfigDirectory;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.coverage.CoverageStoreFinder;
import org.geotoolkit.coverage.PyramidCoverageBuilder;
import org.geotoolkit.coverage.filestore.FileCoverageStoreFactory;
import org.geotoolkit.coverage.filestore.XMLCoverageStoreFactory;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.grid.GridGeometry2D;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.coverage.memory.MPCoverageStore;
import org.geotoolkit.coverage.postgresql.PGCoverageStoreFactory;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.image.interpolation.InterpolationCase;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.process.ProcessListener;
import org.geotoolkit.referencing.adapters.NetcdfCRS;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.feature.type.Name;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CancellationException;

/**
 * Helper class to ease the pyramid process.
 *
 * @author olivier.nouguier@geomatys.com
 */
public class PyramidCoverageHelper {

    private CoverageStore store;
    private CoverageStore outputCoverageStore;
    private PyramidCoverageBuilder pyramidCoverageBuilder;
    private String baseCoverageName;
    private CoverageNamer coverageNamer;
    private List<GridCoverage2D> coveragesPyramid;
    private double[] depth;

    public PyramidCoverageHelper(Builder builder) throws DataStoreException {

        this.store = builder.buildInputStore();
        final List<GridCoverage2D> coverages = buildCoverages();

        if (coverages.size() > 0) {
            coveragesPyramid = coverages;
            this.outputCoverageStore = builder.buildOutputStore();

            this.pyramidCoverageBuilder = new PyramidCoverageBuilder(new Dimension(
                    builder.tileWidth, builder.tileHeight), builder.interpolation,
                    1);

            this.coverageNamer = builder.coverageNamer;
            this.baseCoverageName = builder.baseCoverageName;

            this.depth = builder.depth;
        }
    }

    /**
     * Builder factory with a base name for coverage.
     *
     * @param name of coverage
     * @return Builder instance
     */
    public static Builder builder(String name) {
        Builder builder = new Builder(name);
        return builder;
    }

    public CoverageStore getCoverageStore() {
        return outputCoverageStore;
    }

    /**
     * Build coverage list which can be pyramided
     *
     * @return a {@link org.geotoolkit.coverage.grid.GridCoverage2D} {@link java.util.List}
     * @throws CancellationException
     * @throws DataStoreException
     */
    private List<GridCoverage2D> buildCoverages() throws CancellationException, DataStoreException {
        final List<GridCoverage2D> coverages = new ArrayList<>(0);


        for (Name name : store.getNames()) {
            final CoverageReference ref = store.getCoverageReference(name);
            final GridCoverageReader reader = ref.createReader();

            final GridCoverage2D coverage = (GridCoverage2D) reader.read(0, null);
            final GridGeometry2D gridGeometry = (GridGeometry2D) reader.getGridGeometry(ref.getImageIndex());

            if ((gridGeometry.getCoordinateReferenceSystem() instanceof NetcdfCRS)) {
                break;
            }


            final double widthGeometry = gridGeometry.getExtent2D().getWidth();
            final double heightGeometry = gridGeometry.getExtent2D().getHeight();


            double userWidth = 500;
            double userHeight = 500;

            if(ConfigDirectory.CSTL_PROPERTIES!=null){
                String pictureHeight = ConfigDirectory.CSTL_PROPERTIES.getProperty("picture_max_height", "500");
                String pictureWidth = ConfigDirectory.CSTL_PROPERTIES.getProperty("picture_max_width", "500");
                userWidth = Double.parseDouble(pictureWidth);
                userHeight = Double.parseDouble(pictureHeight);
            }

            //If coverage size higher than user selected size else add on an other list to create separate file
            if (widthGeometry > userWidth || heightGeometry > userHeight) {
                coverages.add(coverage);
            }
        }
        return coverages;
    }

    public Map<Envelope, double[]> getResolutionPerEnvelope(
            GridCoverage coverage) {
        Map<Envelope, double[]> map = new HashMap<>();
        map.put(coverage.getEnvelope(), depth);
        return map;
    }

    public PyramidCoverageBuilder getPyramidCoverageBuilder() {
        return pyramidCoverageBuilder;
    }

    public List<GridCoverage2D> getCoveragesPyramid() {
        return coveragesPyramid;
    }

    /**
     * Build pyramid and give a {@link org.geotoolkit.process.ProcessListener} to
     *
     * @param listener
     * @throws DataStoreException
     * @throws TransformException
     * @throws FactoryException
     */
    public void buildPyramid(final ProcessListener listener) throws DataStoreException, TransformException,
            FactoryException {

        List<GridCoverage2D> coverages = getCoveragesPyramid();
        int coverageCount = 1;
        for (GridCoverage coverage : coverages) {
            Map<Envelope, double[]> resolution_Per_Envelope = getResolutionPerEnvelope(coverage);
            pyramidCoverageBuilder.create(coverage, getCoverageStore(),
                    coverageNamer.getName(baseCoverageName, coverageCount),
                    resolution_Per_Envelope, null, listener);
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

        public WithOutput outputFormat(String output) {
            builder.outputFormat = output;
            return this;
        }

        abstract CoverageStore createOutputStore() throws DataStoreException;

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

        public WithInputImpl(Builder builder) {
            this.builder = builder;
        }

        public WithOutput toFileStore(String path) throws MalformedURLException {
            WithFileOutput fileOutput = new WithFileOutput(builder);
            fileOutput.tileFolder = new File(path, "tiles").toURI().toURL();
            builder.output = fileOutput;
            return fileOutput;
        }

        public WithOutput toMemoryStore() {
            WithMemoryOutput memoryOutput = new WithMemoryOutput(builder);
            builder.output = memoryOutput;
            return memoryOutput;
        }

        public WithPGOutput toPostGisStore(String databaseName, String login,
                                           String password) {
            WithPGOutputImpl pgOutput = new WithPGOutputImpl(builder);
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

            protected CoverageStore createOutputStore()
                    throws DataStoreException {

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

        public static class WithPGOutputImpl extends WithOutputImpl implements
                WithPGOutput {
            private String pgPassword;
            private String pgLogin;
            private String pgDatabaseName;
            private String pgHostname = "localhost";
            private int pgPort = 5432;
            private String pgSchema = "pgcoverage";

            public WithPGOutputImpl(Builder builder) {
                super(builder);
            }

            public WithPGOutput withHostname(String hostname) {
                this.pgHostname = hostname;
                return this;
            }

            public WithPGOutput withPgPort(int port) {
                this.pgPort = port;
                return this;
            }

            public WithPGOutput withSchema(String schema) {
                this.pgSchema = schema;
                return this;
            }

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

        public WithFileInput(Builder builder) {
            super(builder);
        }

        protected CoverageStore buildInputStore(String inputFormat)
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
    public static class Builder {

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
        private double[] depth = new double[]{1, 0.5, 0.25, 0.125};
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

        public Builder inputFormat(String inputFormat) {
            this.inputFormat = inputFormat;
            return this;
        }

        public Builder withDeeps(double[] deeps) {
            this.depth = deeps;
            return this;
        }

        public Builder withTile(int width, int height) {
            this.tileWidth = width;
            this.tileHeight = height;
            return this;
        }

        public Builder withInterpolation(InterpolationCase interpolation) {
            this.interpolation = interpolation;
            return this;
        }

        public WithInput fromImage(String path) throws MalformedURLException {
            WithFileInput fileInput = new WithFileInput(this);
            fileInput.imageFile = new File(path).toURI().toURL();
            input = fileInput;
            return input;

        }

        public Builder withBaseCoverageNamer(CoverageNamer coverageNamer) {
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
