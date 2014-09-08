/*
 * Constellation - An open source and standard compliant SDI
 * http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.test.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

import java.net.URI;

/**
 * @author bgarcia
 */
public class IndexPage {

    private final WebDriver drv;

    private final URI siteBase;

    /**
     * @param drv
     *            A web driver.
     * @param siteBase
     *            The root URI of a the expected site.
     * @return Whether or not the driver is at the index page of the site.
     */
    public static boolean isAtIndexPage(WebDriver drv, URI siteBase) {
        return drv.getCurrentUrl().equals(siteBase.toString());
    }


    public IndexPage(WebDriver drv, URI siteBase) {
        if (!isAtIndexPage(drv, siteBase)) { throw new IllegalStateException(); }
        PageFactory.initElements(drv, this);
        this.drv = drv;
        this.siteBase = siteBase;
    }

}
