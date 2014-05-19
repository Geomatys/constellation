/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
package org.constellation.engine.register.jooq.repository;

import static org.constellation.engine.register.jooq.Tables.DATA;

import org.constellation.engine.register.Data;
import org.constellation.engine.register.jooq.Tables;
import org.constellation.engine.register.jooq.tables.records.DataRecord;
import org.constellation.engine.register.repository.DataRepository;
import org.springframework.stereotype.Component;

@Component
public class JooqDataRepository extends AbstractJooqRespository<DataRecord, Data> implements DataRepository {

    public JooqDataRepository() {
        super(Data.class, DATA);
    }

    @Override
    public Data findByNameAndNamespaceAndProviderId(String name, String namespace, String providerIdentifier) {
        dsl.insertInto(DATA).select(dsl.select().from(DATA).where(DATA.VISIBLE.eq(true))).execute();
        return null;
    }

    @Override
    public Data fromLayer(String layerAlias, String providerId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Data save(Data data) {
        return null;
    }

}
