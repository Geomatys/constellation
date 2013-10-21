package org.constellation.coverage;

import junit.framework.Assert;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.lang.Setup;
import org.geotoolkit.process.ProcessListener;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PyramidCoverageHelperTestCase {

    private static final Logger LOGGER = Logging.getLogger(PyramidCoverageHelperTestCase.class);

    @Before
    public void beforeTest() throws IOException {
        File propertiesFile = new File("/Users/bgarcia/projet/oss/constellation/modules/cstl-services/src/main/webapp/WEB-INF/constellation.properties");
        FileInputStream fis = new FileInputStream(propertiesFile);
        Properties properties = new Properties();
        properties.load(fis);
    }

    @Test
    public void testNetCDF() {
        ImageIO.scanForPlugins();
        Setup.initialize(null);

        try {
            PyramidCoverageHelper helper = PyramidCoverageHelper.builder("F1_resu_mask").
                    inputFormat("AUTO").withDeeps(new double[]{1}).
                    fromImage("/Users/bgarcia/Documents/donnees/netcdf/F1_resu_mask.nc").toFileStore("/Users/bgarcia/.constellation-data/pyramid").build();
            Assert.assertNotNull(helper);
            Assert.assertNull(helper.getCoveragesPyramid());
            if (helper.getCoveragesPyramid() != null) {
                ProcessListener listener = new PyramidCoverageProcessListener();
                helper.buildPyramid(listener);
            }
        } catch (DataStoreException | MalformedURLException | TransformException | FactoryException e) {
            LOGGER.log(Level.WARNING, "error on pyramidal", e);
            Assert.fail();
        }
        Assert.assertTrue("success", true);

    }

    @Test
    public void testGeotiff() {
        ImageIO.scanForPlugins();
        Setup.initialize(null);

        try {
            PyramidCoverageHelper helper = PyramidCoverageHelper.builder("SP27GTIF").
                    inputFormat("AUTO").withDeeps(new double[]{1}).
                    fromImage("/Users/bgarcia/Documents/donnees/geotif/chicago/SP27GTIF.TIF").toFileStore("/Users/bgarcia/.constellation-data/pyramid").build();
            Assert.assertNotNull(helper);
            Assert.assertNotNull(helper.getCoveragesPyramid());
            if (helper.getCoveragesPyramid() != null) {
                ProcessListener listener = new PyramidCoverageProcessListener();
                helper.buildPyramid(listener);
            }
        } catch (DataStoreException | MalformedURLException | TransformException | FactoryException e) {
            LOGGER.log(Level.WARNING, "error on pyramidal", e);
            Assert.fail();
        }
        Assert.assertTrue("success", true);
    }
}
