/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.process.style.create;

import java.io.File;
import java.net.MalformedURLException;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.style.AbstractMapStyleTest;
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
public class CreateMapStyleTest extends AbstractMapStyleTest {

    public CreateMapStyleTest() {
        super(CreateMapStyleDescriptor.NAME);
    }

    @Test
    public void testCreateStyle() throws ProcessException, NoSuchIdentifierException, MalformedURLException {

        addProvider(buildProvider("createStyleProvider1", true));

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateMapStyleDescriptor.NAME);

        final MutableStyleFactory msf = new DefaultStyleFactory();
        final MutableStyle style = msf.style(StyleConstants.DEFAULT_LINE_SYMBOLIZER);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(CreateMapStyleDescriptor.PROVIDER_ID_NAME).setValue("createStyleProvider1");
        in.parameter(CreateMapStyleDescriptor.STYLE_ID_NAME).setValue("myStyle");
        in.parameter(CreateMapStyleDescriptor.STYLE_NAME).setValue(style);

        desc.createProcess(in).call();

        Provider provider = null;
        for (StyleProvider p : StyleProviderProxy.getInstance().getProviders()) {
            if ("createStyleProvider1".equals(p.getId())){
                provider = p;
            }
        }
        assertNotNull(provider);
        final File styleFile = new File(configDirectory.getAbsolutePath()+"/sldDir/", "myStyle.xml");
        assertTrue(styleFile.exists());

        removeProvider("createStyleProvider1");
    }

    /**
     * Provider doesn't exist.
     */
    @Test
    public void testFailCreateStyle1() throws ProcessException, NoSuchIdentifierException, MalformedURLException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateMapStyleDescriptor.NAME);

        final MutableStyleFactory msf = new DefaultStyleFactory();
        final MutableStyle style = msf.style(StyleConstants.DEFAULT_LINE_SYMBOLIZER);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(CreateMapStyleDescriptor.PROVIDER_ID_NAME).setValue("createStyleProvider2");
        in.parameter(CreateMapStyleDescriptor.STYLE_ID_NAME).setValue("myStyle");
        in.parameter(CreateMapStyleDescriptor.STYLE_NAME).setValue(style);

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

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateMapStyleDescriptor.NAME);

        final MutableStyleFactory msf = new DefaultStyleFactory();
        final MutableStyle style = msf.style(StyleConstants.DEFAULT_LINE_SYMBOLIZER);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(CreateMapStyleDescriptor.PROVIDER_ID_NAME).setValue("");
        in.parameter(CreateMapStyleDescriptor.STYLE_ID_NAME).setValue("myStyle");
        in.parameter(CreateMapStyleDescriptor.STYLE_NAME).setValue(style);

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
    public void testFailCreateStyle3() throws ProcessException, NoSuchIdentifierException, MalformedURLException {

        addProvider(buildProvider("createStyleProvider3", true));

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateMapStyleDescriptor.NAME);

        final MutableStyleFactory msf = new DefaultStyleFactory();
        final MutableStyle style = msf.style(StyleConstants.DEFAULT_LINE_SYMBOLIZER);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(CreateMapStyleDescriptor.PROVIDER_ID_NAME).setValue("createStyleProvider3");
        in.parameter(CreateMapStyleDescriptor.STYLE_ID_NAME).setValue("");
        in.parameter(CreateMapStyleDescriptor.STYLE_NAME).setValue(style);

        try {
            desc.createProcess(in).call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }

        removeProvider("createStyleProvider3");
    }
}
