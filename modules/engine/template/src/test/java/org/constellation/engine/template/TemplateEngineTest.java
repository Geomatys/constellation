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

package org.constellation.engine.template;

import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
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
        prop.put("keywords", Arrays.asList("kw1","kw2"));
        String templateApplied = templateEngine.apply(new File(templateUrl.toURI()), prop);
        assertThat(templateApplied, containsString("testParentId"));
        assertThat(templateApplied, containsString("toto"));
    }

}
