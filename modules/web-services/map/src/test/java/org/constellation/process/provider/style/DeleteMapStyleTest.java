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
package org.constellation.process.provider.style;

import java.io.File;
import java.net.MalformedURLException;
import org.constellation.configuration.ConfigurationException;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.provider.*;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.style.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public class DeleteMapStyleTest extends AbstractMapStyleTest {

    public DeleteMapStyleTest() {
        super(DeleteStyleToStyleProviderDescriptor.NAME);
    }

    @Test
    public void testDeleteStyle() throws ProcessException, NoSuchIdentifierException, MalformedURLException, ConfigurationException {

        addProvider("deleteStyleProvider1",buildProvider("deleteStyleProvider1", true));

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteStyleToStyleProviderDescriptor.NAME);

        final MutableStyleFactory msf = new DefaultStyleFactory();
        final MutableStyle style = msf.style(StyleConstants.DEFAULT_LINE_SYMBOLIZER);

        for (StyleProvider p : StyleProviders.getInstance().getProviders()) {
            if (p.getId().equals("deleteStyleProvider1")) {
                p.set("styleToDelete", style);
            }
        }

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(DeleteStyleToStyleProviderDescriptor.PROVIDER_ID_NAME).setValue("deleteStyleProvider1");
        in.parameter(DeleteStyleToStyleProviderDescriptor.STYLE_ID_NAME).setValue("styleToDelete");

        desc.createProcess(in).call();

        Provider provider = null;
        for (StyleProvider p : StyleProviders.getInstance().getProviders()) {
            if ("deleteStyleProvider1".equals(p.getId())){
                provider = p;
            }
        }
        assertNotNull(provider);
        final File styleFile = new File(configDirectory.getAbsolutePath()+"/sldDir/");
        assertTrue(styleFile.list().length == 0);
        assertFalse(provider.contains("styleToDelete"));

        removeProvider("deleteStyleProvider1");
    }

    /**
     * Provider doesn't exist.
     */
    @Test
    public void testFailDeleteStyle1() throws ProcessException, NoSuchIdentifierException, MalformedURLException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteStyleToStyleProviderDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(DeleteStyleToStyleProviderDescriptor.PROVIDER_ID_NAME).setValue("deleteStyleProvider2");
        in.parameter(DeleteStyleToStyleProviderDescriptor.STYLE_ID_NAME).setValue("myStyle");

        try {
            desc.createProcess(in).call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }

    /**
     * Empty provider name
     */
    @Test
    public void testFailDeleteStyle2() throws ProcessException, NoSuchIdentifierException, MalformedURLException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteStyleToStyleProviderDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(DeleteStyleToStyleProviderDescriptor.PROVIDER_ID_NAME).setValue("");
        in.parameter(DeleteStyleToStyleProviderDescriptor.STYLE_ID_NAME).setValue("myStyle");

        try {
            desc.createProcess(in).call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }

    /**
     * Empty style name
     */
    @Test
    public void testFailDeleteStyle3() throws ProcessException, NoSuchIdentifierException, MalformedURLException, ConfigurationException {

        addProvider("deleteStyleProvider3",buildProvider("deleteStyleProvider3", true));

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteStyleToStyleProviderDescriptor.NAME);


        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(DeleteStyleToStyleProviderDescriptor.PROVIDER_ID_NAME).setValue("deleteStyleProvider3");
        in.parameter(DeleteStyleToStyleProviderDescriptor.STYLE_ID_NAME).setValue("");

        try {
            desc.createProcess(in).call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        } finally {
            removeProvider("deleteStyleProvider3");
        }
    }

}
