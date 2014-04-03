/*
 * Constellation - An open source and standard compliant SDI
 * http://www.constellation-sdi.org
 *
 * (C) 2011, Geomatys
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package org.constellation.engine.template;

import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.CoreMatchers.*;

/** Test Class GroovyTemplateEngine
 * Created by christophe mourette on 02/04/14 for Geomatys.
 */
public class TemplateEngineTest  {


    @Test
    public void testGroovyTemplateEngineException() throws URISyntaxException {

        try {
            TemplateEngine ifc = TemplateEngineFactory.getInstance("unknown");
            URL templateUrl = TemplateEngineFactory.class.getResource("/org/constellation/engine/template/mdTemplDataset.xml");
            Properties prop = new Properties();
            prop.put("parentId", "testParentId");
            prop.put("srs", "toto");
            fail("Should have raised a TemplateEngineException");
            ifc.apply(new File(templateUrl.toURI()), prop);
        } catch (TemplateEngineException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testGroovyTemplateEngineFile() throws TemplateEngineException, URISyntaxException {

        TemplateEngine templateEngine = TemplateEngineFactory.getInstance(TemplateEngineFactory.GROOVY_TEMPLATE_ENGINE);
        URL templateUrl = TemplateEngineFactory.class.getResource("/org/constellation/engine/template/mdTemplDataset.xml");
        Properties prop = new Properties();
        prop.put("parentId", "testParentId");
        prop.put("srs", "toto");
        String templateApplied = templateEngine.apply(new File(templateUrl.toURI()), prop);
        assertThat(templateApplied, containsString("testParentId"));
        assertThat(templateApplied, containsString("toto"));
    }

}
