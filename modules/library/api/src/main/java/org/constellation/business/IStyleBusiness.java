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
package org.constellation.business;

import org.constellation.api.StyleType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.StyleBrief;
import org.constellation.configuration.StyleReport;
import org.constellation.configuration.TargetNotFoundException;
import org.geotoolkit.style.MutableStyle;
import org.opengis.filter.expression.Function;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * @author Cédric Briançon (Geomatys)
 */
public interface IStyleBusiness {
    void deleteStyle(String id, String name) throws ConfigurationException;

    void writeStyle(String key, Integer id, StyleType styleType, MutableStyle style) throws IOException;

    MutableStyle getStyle(String providerID, String styleName) throws TargetNotFoundException;

    MutableStyle getStyle(int styleId) throws TargetNotFoundException;

    void createOrUpdateStyleFromLayer(String serviceType, String serviceIdentifier, String layerName, String styleProviderId,
                                      String styleName) throws TargetNotFoundException;

    void removeStyleFromLayer(String serviceIdentifier, String serviceType, String layerName, String styleProviderId,
                              String styleName) throws TargetNotFoundException;

    void createStyle(String sld, MutableStyle style) throws ConfigurationException;

    Function getFunctionColorMap(String id, String styleId, String ruleName) throws TargetNotFoundException;

    List<StyleBrief> getAvailableStyles(String category);

    List<StyleBrief> getAvailableStyles(String providerId, String category) throws TargetNotFoundException;

    void setStyle(String id, String styleId, MutableStyle style) throws ConfigurationException;

    StyleReport getStyleReport(String providerId, String styleName, Locale locale) throws ConfigurationException;

    void linkToData(String styleProvider, String styleName, String dataProvider, QName dataId) throws ConfigurationException;

    void unlinkFromData(String styleProvider, String styleName, String dataProvider, QName dataId) throws ConfigurationException;
}
