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
package org.constellation.process.style;

import org.constellation.process.style.DeleteStyleProviderDescriptor;
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
public class DeleteMapStyleTest extends AbstractMapStyleTest {

    public DeleteMapStyleTest() {
        super(DeleteStyleProviderDescriptor.NAME);
    }

    @Test
    public void testDeleteStyle() throws ProcessException, NoSuchIdentifierException, MalformedURLException {

        addProvider(buildProvider("deleteStyleProvider1", true));

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteStyleProviderDescriptor.NAME);

        final MutableStyleFactory msf = new DefaultStyleFactory();
        final MutableStyle style = msf.style(StyleConstants.DEFAULT_LINE_SYMBOLIZER);

        for (StyleProvider p : StyleProviderProxy.getInstance().getProviders()) {
            if (p.getId().equals("deleteStyleProvider1")) {
                p.set("styleToDelete", style);
            }
        }

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(DeleteStyleProviderDescriptor.PROVIDER_ID_NAME).setValue("deleteStyleProvider1");
        in.parameter(DeleteStyleProviderDescriptor.STYLE_ID_NAME).setValue("styleToDelete");

        desc.createProcess(in).call();

        Provider provider = null;
        for (StyleProvider p : StyleProviderProxy.getInstance().getProviders()) {
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

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteStyleProviderDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(DeleteStyleProviderDescriptor.PROVIDER_ID_NAME).setValue("deleteStyleProvider2");
        in.parameter(DeleteStyleProviderDescriptor.STYLE_ID_NAME).setValue("myStyle");

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

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteStyleProviderDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(DeleteStyleProviderDescriptor.PROVIDER_ID_NAME).setValue("");
        in.parameter(DeleteStyleProviderDescriptor.STYLE_ID_NAME).setValue("myStyle");

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
    public void testFailDeleteStyle3() throws ProcessException, NoSuchIdentifierException, MalformedURLException {

        addProvider(buildProvider("deleteStyleProvider3", true));

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteStyleProviderDescriptor.NAME);


        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(DeleteStyleProviderDescriptor.PROVIDER_ID_NAME).setValue("deleteStyleProvider3");
        in.parameter(DeleteStyleProviderDescriptor.STYLE_ID_NAME).setValue("");

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
