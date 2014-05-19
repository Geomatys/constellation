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
public class SetMapStyleTest extends AbstractMapStyleTest {

    public SetMapStyleTest() {
        super(SetStyleToStyleProviderDescriptor.NAME);
    }

    @Test
    public void testCreateStyle() throws ProcessException, NoSuchIdentifierException, MalformedURLException, ConfigurationException {

        addProvider("createStyleProvider1",buildProvider("createStyleProvider1", true));

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, SetStyleToStyleProviderDescriptor.NAME);

        final MutableStyleFactory msf = new DefaultStyleFactory();
        final MutableStyle style = msf.style(StyleConstants.DEFAULT_LINE_SYMBOLIZER);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(SetStyleToStyleProviderDescriptor.PROVIDER_ID_NAME).setValue("createStyleProvider1");
        in.parameter(SetStyleToStyleProviderDescriptor.STYLE_ID_NAME).setValue("myStyle");
        in.parameter(SetStyleToStyleProviderDescriptor.STYLE_NAME).setValue(style);

        desc.createProcess(in).call();

        Provider provider = null;
        for (StyleProvider p : StyleProviders.getInstance().getProviders()) {
            if ("createStyleProvider1".equals(p.getId())){
                provider = p;
            }
        }
        assertNotNull(provider);
        final File styleFile = new File(configDirectory.getAbsolutePath()+"/sldDir/", "myStyle.xml");
        assertTrue(styleFile.exists());
        assertTrue(provider.contains("myStyle"));
        
        removeProvider("createStyleProvider1");
        styleFile.delete();
    }

    /**
     * Empty style name
     */
    @Test
    public void testCreateStyle2() throws ProcessException, NoSuchIdentifierException, MalformedURLException, ConfigurationException {

        addProvider("createStyleProvider3",buildProvider("createStyleProvider3", true));

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, SetStyleToStyleProviderDescriptor.NAME);

        final MutableStyleFactory msf = new DefaultStyleFactory();
        final MutableStyle style = msf.style(StyleConstants.DEFAULT_LINE_SYMBOLIZER);
        style.setName("styleName");

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(SetStyleToStyleProviderDescriptor.PROVIDER_ID_NAME).setValue("createStyleProvider3");
        in.parameter(SetStyleToStyleProviderDescriptor.STYLE_NAME).setValue(style);

        desc.createProcess(in).call();
        
        Provider provider = null;
        for (StyleProvider p : StyleProviders.getInstance().getProviders()) {
            if ("createStyleProvider3".equals(p.getId())){
                provider = p;
            }
        }
        assertNotNull(provider);
        final File styleFile = new File(configDirectory.getAbsolutePath()+"/sldDir/", "styleName.xml");
        assertTrue(styleFile.exists());
        assertTrue(provider.contains("styleName"));

        removeProvider("createStyleProvider3");
    }
    
    /**
     * Provider doesn't exist.
     */
    @Test
    public void testFailCreateStyle1() throws ProcessException, NoSuchIdentifierException, MalformedURLException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, SetStyleToStyleProviderDescriptor.NAME);

        final MutableStyleFactory msf = new DefaultStyleFactory();
        final MutableStyle style = msf.style(StyleConstants.DEFAULT_LINE_SYMBOLIZER);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(SetStyleToStyleProviderDescriptor.PROVIDER_ID_NAME).setValue("createStyleProvider2");
        in.parameter(SetStyleToStyleProviderDescriptor.STYLE_ID_NAME).setValue("myStyle");
        in.parameter(SetStyleToStyleProviderDescriptor.STYLE_NAME).setValue(style);

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
    public void testFailCreateStyle2() throws ProcessException, NoSuchIdentifierException, MalformedURLException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, SetStyleToStyleProviderDescriptor.NAME);

        final MutableStyleFactory msf = new DefaultStyleFactory();
        final MutableStyle style = msf.style(StyleConstants.DEFAULT_LINE_SYMBOLIZER);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(SetStyleToStyleProviderDescriptor.PROVIDER_ID_NAME).setValue("");
        in.parameter(SetStyleToStyleProviderDescriptor.STYLE_ID_NAME).setValue("myStyle");
        in.parameter(SetStyleToStyleProviderDescriptor.STYLE_NAME).setValue(style);

        try {
            desc.createProcess(in).call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }
    
}
