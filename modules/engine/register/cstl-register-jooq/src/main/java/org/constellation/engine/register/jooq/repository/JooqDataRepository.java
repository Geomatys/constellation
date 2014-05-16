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
