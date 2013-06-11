/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.gui;

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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.URL;
import java.util.List;

import static junit.framework.Assert.assertEquals;

@RunWith(Arquillian.class)
public class CstlClientTestCase {

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = Helper.createBaseServletDeployment("guice"); // <1> Create the base servlet deployment
        war.addPackages(true, "org.constellation.gui"); // <3> Add the examples.tutorial package
        return war;
    }

    @Drone
    WebDriver driver;

    @ArquillianResource
    URL deploymentURL;


    public URL getApplicationURL(String application) {
        try {
            return deploymentURL.toURI().resolve(application).toURL();
        } catch (Exception e) {
            AssertionFailedError afe = new AssertionFailedError("Could not build URL");
            afe.initCause(e);
            throw afe;
        }
    }


    @Test
    @RunAsClient
    public void testNavBar() {
        driver.get(deploymentURL.toString());
        WebElement nav = driver.findElement(By.className("nav"));
        assertEquals("Homepage Web services Datas Styles Map context Geoviewer Administration", nav.getText());
        List<WebElement> navLinks = nav.findElements(By.tagName("a"));
        for (WebElement navLink : navLinks) {
            String href = navLink.getAttribute("href");
            if(navLink.getText().equals("HomePage")){
                assertEquals(href, deploymentURL.toString());
            }
//            TODO : to continue with all pages
        }
    }
}