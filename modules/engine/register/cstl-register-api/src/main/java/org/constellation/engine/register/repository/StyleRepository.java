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
package org.constellation.engine.register.repository;

import java.util.List;

import org.constellation.engine.register.i18n.StyleWithI18N;
import org.constellation.engine.register.jooq.tables.pojos.Data;
import org.constellation.engine.register.jooq.tables.pojos.Layer;
import org.constellation.engine.register.jooq.tables.pojos.Style;

public interface StyleRepository {

    int create(Style style);
    
    List<Style> findAll();
    
    List<Style> findByType(final String type);
    
    List<Style> findByTypeAndProvider(final int providerId, final String type);
    
    List<Style> findByProvider(final int providerId);

    Style findByNameAndProvider(final int providerId, String name);
    
    Style findById(int id);

    List<Style> findByName(final String name);

    List<Style> findByData(Data data);
    
    List<Style> findByLayer(Layer layer);
    
    List<Data> getLinkedData(int styleId);
    
    void linkStyleToData(int styleId, int dataid);

    void unlinkStyleToData(int styleId, int dataid);
    
    void linkStyleToLayer(int styleId, int layerid);

    void unlinkStyleToLayer(int styleId, int layerId);

    List<Integer> getStyleIdsForData(int id);
    
    void deleteStyle(int providerId, String name);

    Style save(Style s);
    
    StyleWithI18N getStyleWithI18Ns(Style style);
}
