package org.constellation.engine.template;

import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.CoreMatchers.*;

/**
 * Created by christophem on 02/04/14.
 */
public class TemplateEngineTest  {


    @Test
    public void testGroovyTemplateEngine() throws TemplateEngineException {

        TemplateEngine templateEngine = TemplateEngineFactory.getInstance(TemplateEngineFactory.GROOVY_TEMPLATE_ENGINE);
        templateEngine.printTest("pattern");
    }

    @Test
    public void testGroovyTemplateEngineException()  {

        try {
            TemplateEngine ifc = TemplateEngineFactory.getInstance("unknown");
            fail("Should have raised a TemplateEngineException");
            ifc.printTest("nothing");
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
        System.out.println(templateApplied);
        assertThat(templateApplied, containsString("testParentId"));
        assertThat(templateApplied, containsString("toto"));
    }

}
