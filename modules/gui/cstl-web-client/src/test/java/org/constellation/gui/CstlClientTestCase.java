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

package org.constellation.gui;

import com.google.common.base.Function;
import junit.framework.AssertionFailedError;
import juzu.arquillian.Helper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Main junit test case with arquillian and selenium
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
//@RunWith(Arquillian.class)
public class CstlClientTestCase {

    /**
     * Initialise war deployment for test case
     *
     * @return a war file object
     */
    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        WebArchive war = Helper.createBaseServletDeployment();
        war.addAsWebInfResource(new File("src/test/resources/spring.xml"));
        war.addPackages(true, "org.constellation.gui");
        return war;
    }

    /**
     * driver to find object on web page
     */
    @Drone
    WebDriver driver;

    /**
     * root url deployment
     */
    @ArquillianResource
    URL deploymentURL;


    /**
     * Method to find application {@link URL}
     *
     * @param application application name
     * @return application {@link URL}
     */
    public URL getApplicationURL(String application) {
        try {
            return deploymentURL.toURI().resolve(application).toURL();
        } catch (Exception e) {
            AssertionFailedError afe = new AssertionFailedError("Could not build URL");
            afe.initCause(e);
            throw afe;
        }
    }


    /**
     * Main menu bar test case. Use to verify all links in menu
     */
//    @Test
//    @RunAsClient
    public void testNavBar() {
        driver.get(deploymentURL.toString());
        WebElement nav = driver.findElement(By.className("nav"));
//        assertEquals("Homepage Web services Datas Styles Map context Geoviewer Administration", nav.getText());
        List<WebElement> navLinks = nav.findElements(By.tagName("a"));
        for (WebElement navLink : navLinks) {
            String href = navLink.getAttribute("href");

            if (navLink.getText().equals("HomePage")) {
                assertEquals(href, deploymentURL.toString());
            }
            if (navLink.getText().equals("Web services")) {
                assertEquals(href, deploymentURL.toString() + "webservices");
            }
//            TODO : to continue with all pages
        }
    }

    /**
     * Test Homepage links with found a {@link WebElement} an next navigation page.
     */
//    @Test
//    @RunAsClient
    public void testHomePageLinks() {
        driver.get(deploymentURL.toString());
        WebElement serviceBtn = driver.findElement(By.id("servicebtn"));
        serviceBtn.click();
        WebElement createService = driver.findElement(By.id("createservice"));

        //if not null => button exist we are go on an other page...
        assertNotNull(createService);
    }

    /**
     * test wms creation page navigation
     */
//    @Test
//    @RunAsClient
    public void testCreateWMS() {
        driver.get(deploymentURL.toString() + "webservices");
        WebElement createService = driver.findElement(By.id("createservice"));
        assertNotNull(createService);

        //wms button test visibility
        WebElement wmschoice = driver.findElement(By.id("wmschoice"));
        assertFalse(wmschoice.isDisplayed());
        createService.click();
        assertTrue(wmschoice.isDisplayed());

        //go to form first page
        wmschoice.click();

        //Test visibility parts.
        WebElement description = driver.findElement(By.id("description"));
        final WebElement metadata = driver.findElement(By.id("metadata"));
        Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
                .withTimeout(30, TimeUnit.SECONDS)
                .pollingEvery(5, TimeUnit.SECONDS)
                .ignoring(NoSuchElementException.class);

        wait.until(new Function<WebDriver, Boolean>() {
            @Override
            public Boolean apply(org.openqa.selenium.WebDriver webDriver) {
                return metadata.isDisplayed()==false;
            }
        });

        assertTrue(description.isDisplayed());
        assertFalse(metadata.isDisplayed());

        //add forms data
        WebElement createtionWmsForm = driver.findElement(By.tagName("form"));
        createtionWmsForm.findElement(By.id("name")).sendKeys("serviceName");
        createtionWmsForm.findElement(By.id("identifier")).sendKeys("serviceIdentifier");
        createtionWmsForm.findElement(By.id("keywords")).sendKeys("service keywords");
        createtionWmsForm.findElement(By.id("inputDescription")).sendKeys("service Description");
        createtionWmsForm.findElement(By.id("inputDescription")).sendKeys("service Description");
        createtionWmsForm.findElement(By.id("v130")).click();

        driver.findElement(By.id("nextButton")).click();

        new WebDriverWait(driver, 20).until(new ExpectedCondition<Object>() {
            @Override
            public Object apply(org.openqa.selenium.WebDriver webDriver) {
                if(webDriver.findElement(By.id("contactName")).isDisplayed())
                    return webDriver.findElement(By.id("contactName"));
                else return null;
            }
        });
        // contact information & address
        createtionWmsForm.findElement(By.id("contactName")).sendKeys("contact Name");
        createtionWmsForm.findElement(By.id("contactOrganisation")).sendKeys("contact Organisation");
        createtionWmsForm.findElement(By.id("contactPosition")).sendKeys("contact position");
        createtionWmsForm.findElement(By.id("contactPhone")).sendKeys("contact Phone");
        createtionWmsForm.findElement(By.id("contactFax")).sendKeys("contact Fax");
        createtionWmsForm.findElement(By.id("contactEmail")).sendKeys("contact eMail");
        createtionWmsForm.findElement(By.id("contactAddress")).sendKeys("contact Adress");
        createtionWmsForm.findElement(By.id("contactCity")).sendKeys("contact City");
        createtionWmsForm.findElement(By.id("contactState")).sendKeys("contact State");
        createtionWmsForm.findElement(By.id("contactPostcode")).sendKeys("contact PostCode");
        createtionWmsForm.findElement(By.id("contactCountry")).sendKeys("contact Country");

        //constraint
        createtionWmsForm.findElement(By.id("fees")).sendKeys("fees");
        createtionWmsForm.findElement(By.id("accessConstraints")).sendKeys("access Constraint");
        createtionWmsForm.findElement(By.id("layerLimit")).sendKeys("layer Limit");
        createtionWmsForm.findElement(By.id("maxWidth")).sendKeys("max Width");
        createtionWmsForm.findElement(By.id("maxHeight")).sendKeys("max Height");
        createtionWmsForm.submit();

    }
}