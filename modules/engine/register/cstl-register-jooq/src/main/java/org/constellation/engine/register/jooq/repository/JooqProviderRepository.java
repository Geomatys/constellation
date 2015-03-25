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
package org.constellation.engine.register.jooq.repository;

import static org.constellation.engine.register.jooq.Tables.DATA;
import static org.constellation.engine.register.jooq.Tables.DATA_X_DOMAIN;
import static org.constellation.engine.register.jooq.Tables.LAYER;
import static org.constellation.engine.register.jooq.Tables.PROVIDER;
import static org.constellation.engine.register.jooq.Tables.SERVICE;
import static org.constellation.engine.register.jooq.Tables.STYLE;

import java.util.List;

import org.constellation.engine.register.jooq.tables.Data;
import org.constellation.engine.register.jooq.tables.DataXDomain;
import org.constellation.engine.register.jooq.tables.pojos.Provider;
import org.constellation.engine.register.jooq.tables.pojos.Style;
import org.constellation.engine.register.jooq.tables.records.ProviderRecord;
import org.constellation.engine.register.repository.ProviderRepository;
import org.jooq.UpdateConditionStep;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
        return dsl.select().from(PROVIDER).where(PROVIDER.ID.eq(id)).fetchOneInto(Provider.class);
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
    public Provider findByIdentifier(String identifier) {
        return dsl.select().from(PROVIDER).where(PROVIDER.IDENTIFIER.eq(identifier)).fetchOneInto(Provider.class);
    }
    
    @Override
    public Provider findByIdentifierAndType(String providerIdentifier, String type) {
        return dsl.select().from(PROVIDER).where(PROVIDER.IDENTIFIER.eq(providerIdentifier)).and(PROVIDER.TYPE.eq(type)).fetchOneInto(Provider.class);
    }

    @Override
    public List<Integer> getProviderIdsForDomain(int domainId) {
        return dsl.selectDistinct(provider.ID).from(provider).join(data).on(provider.ID.eq(data.PROVIDER))
                .join(dXd).on(data.ID.eq(dXd.DATA_ID)).where(dXd.DOMAIN_ID.eq(domainId)).fetch(provider.ID);
    }

    @Override
    public Provider getProviderParentIdOfLayer(String serviceType, String serviceId, String layerid) {
        return dsl.select(provider.fields()).from(provider).join(data).on(data.PROVIDER.eq(provider.ID)).join(LAYER)
                .on(LAYER.DATA.eq(data.ID)).join(SERVICE).on(SERVICE.ID.eq(LAYER.SERVICE))
                .where(LAYER.NAME.eq(layerid).and(SERVICE.IDENTIFIER.eq(serviceId)).and(SERVICE.TYPE.eq(serviceType)))
                .fetchOneInto(Provider.class);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Provider insert(Provider provider) {
        ProviderRecord newRecord = dsl.newRecord(PROVIDER);
        newRecord.setConfig(provider.getConfig());
        newRecord.setIdentifier(provider.getIdentifier());
        newRecord.setImpl(provider.getImpl());
        newRecord.setOwner(provider.getOwner());
        newRecord.setType(provider.getType());
        newRecord.setParent(provider.getParent());
        newRecord.store();
        return newRecord.into(Provider.class);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public int delete(int id) {
        return dsl.delete(PROVIDER).where(PROVIDER.ID.eq(id)).execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public int deleteByIdentifier(String providerID) {
        return dsl.delete(PROVIDER).where(PROVIDER.IDENTIFIER.eq(providerID)).execute();
    }

    @Override
    public List<Provider> findChildren(String id) {
        return dsl.select().from(PROVIDER).where(PROVIDER.PARENT.eq(id)).fetchInto(Provider.class);
    }

    @Override
    public List<org.constellation.engine.register.jooq.tables.pojos.Data> findDatasByProviderId(Integer id) {
        return dsl.select(DATA.fields()).from(DATA).join(PROVIDER).on(DATA.PROVIDER.eq(PROVIDER.ID))
                .where(PROVIDER.ID.eq(id)).fetchInto(org.constellation.engine.register.jooq.tables.pojos.Data.class);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public int update(Provider provider) {
        ProviderRecord providerRecord = new ProviderRecord();
        providerRecord.from(provider);
        UpdateConditionStep<ProviderRecord> set = dsl.update(PROVIDER)
                .set(PROVIDER.CONFIG, provider.getConfig())
                .set(PROVIDER.IDENTIFIER, provider.getIdentifier())
                .set(PROVIDER.IMPL, provider.getImpl())
                .set(PROVIDER.OWNER, provider.getOwner())
                .set(PROVIDER.PARENT, provider.getParent())
                .set(PROVIDER.TYPE, provider.getType())
                .where(PROVIDER.ID.eq(provider.getId()));

        return set.execute();

    }

    @Override
    public List<Style> findStylesByProviderId(Integer providerId) {
        return dsl.select().from(STYLE).join(PROVIDER).on(STYLE.PROVIDER.eq(PROVIDER.ID))
                .where(PROVIDER.ID.eq(providerId)).fetchInto(Style.class);
    }

    @Override
    public Provider findByIdentifierAndDomainId(String providerIdentifier, Integer domainId) {
        // @FIXME binding domainId
        return dsl.select().from(PROVIDER).where(PROVIDER.IDENTIFIER.eq(providerIdentifier)).fetchOneInto(Provider.class);

    }
}
