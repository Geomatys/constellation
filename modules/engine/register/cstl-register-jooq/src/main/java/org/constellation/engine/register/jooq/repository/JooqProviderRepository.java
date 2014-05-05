package org.constellation.engine.register.jooq.repository;

import static org.constellation.engine.register.jooq.Tables.PROVIDER;

import java.util.List;

import org.constellation.engine.register.Provider;
import org.constellation.engine.register.jooq.tables.records.ProviderRecord;
import org.constellation.engine.register.repository.ProviderRepository;
import org.springframework.stereotype.Component;

@Component
public class JooqProviderRepository extends AbstractJooqRespository<ProviderRecord, Provider> implements ProviderRepository {

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

}
