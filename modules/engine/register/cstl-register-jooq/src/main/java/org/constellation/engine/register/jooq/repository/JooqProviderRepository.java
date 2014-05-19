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
import static org.constellation.engine.register.jooq.Tables.DATA_X_DOMAIN;
import static org.constellation.engine.register.jooq.Tables.LAYER;
import static org.constellation.engine.register.jooq.Tables.PROVIDER;
import static org.constellation.engine.register.jooq.Tables.SERVICE;

import java.util.List;

import org.constellation.engine.register.Provider;
import org.constellation.engine.register.jooq.Tables;
import org.constellation.engine.register.jooq.tables.Data;
import org.constellation.engine.register.jooq.tables.DataXDomain;
import org.constellation.engine.register.jooq.tables.records.ProviderRecord;
import org.constellation.engine.register.repository.ProviderRepository;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.springframework.stereotype.Component;

@Component
public class JooqProviderRepository extends AbstractJooqRespository<ProviderRecord, Provider> implements
        ProviderRepository {

    private org.constellation.engine.register.jooq.tables.Provider provider = PROVIDER.as("p");

    private Data data = DATA.as("d");

    private DataXDomain dXd = DATA_X_DOMAIN.as("dXd");

    public JooqProviderRepository() {
        super(Provider.class, PROVIDER);
    }

    @Override
    public Provider findOne(Integer id) {
        return dsl.select().from(PROVIDER).where(PROVIDER.ID.eq(id)).fetchOne().into(Provider.class);
    }

    @Override
    public List<Provider> findByImpl(String impl) {
        return dsl.select().from(PROVIDER).where(PROVIDER.IMPL.eq(impl)).fetch().into(Provider.class);
    }

    @Override
    public List<String> getProviderIds() {
        return dsl.select(PROVIDER.IDENTIFIER).from(PROVIDER).fetch(PROVIDER.IDENTIFIER);
    }

    @Override
    public Provider findByIdentifie(String identifier) {
        return dsl.select().from(PROVIDER).where(PROVIDER.IDENTIFIER.eq(identifier)).fetchOne().into(Provider.class);
    }

    @Override
    public List<String> getProviderIdsForDomain(int domainId) {

        return dsl.selectDistinct(provider.IDENTIFIER).from(provider).join(data).on(provider.ID.eq(data.PROVIDER))
                .join(dXd).on(data.ID.eq(dXd.DATA_ID)).where(dXd.DOMAIN_ID.eq(domainId)).fetch(provider.IDENTIFIER);
    }

    @Override
    public Provider getProviderParentIdOfLayer(String serviceType, String serviceId, String layerid) {
        return dsl.select(provider.fields()).from(provider).join(data).on(data.PROVIDER.eq(provider.ID)).join(LAYER)
                .on(LAYER.DATA.eq(data.ID)).join(SERVICE).on(SERVICE.ID.eq(LAYER.SERVICE))
                .where(LAYER.NAME.eq(layerid).and(SERVICE.IDENTIFIER.eq(serviceId)).and(SERVICE.TYPE.eq(serviceType)))
                .fetchOneInto(Provider.class);
    }
}
