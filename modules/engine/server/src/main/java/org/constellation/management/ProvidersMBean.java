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

package org.constellation.management;

import java.util.List;

/**
 * Constellation providers management bean.
 * Can be used to reload providers.
 * TODO : make a complete controle bean.
 *
 * @author Johann Sorel (Geomatys)
 */
public interface ProvidersMBean {

    public static final String OBJECT_NAME = "org.constellation:type=Manager,name=Providers";

    /**
     * @return list of all layers
     */
    List<String> getLayerList();

    /**
     * @return list of all styles
     */
    List<String> getStyleList();

    /**
     * Force reloading layer providers.
     */
    void reloadLayerProviders();

    /**
     * Force reloading style providers.
     */
    void reloadStyleProviders();

}
