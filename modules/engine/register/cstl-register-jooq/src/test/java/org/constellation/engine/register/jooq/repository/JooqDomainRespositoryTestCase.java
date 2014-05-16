package org.constellation.engine.register.jooq.repository;

import static org.constellation.engine.register.jooq.Tables.DATA;
import static org.constellation.engine.register.jooq.Tables.DATA_X_DOMAIN;
import static org.constellation.engine.register.jooq.Tables.PROVIDER;

import java.util.HashSet;
import java.util.Set;

import org.constellation.engine.register.Domain;
import org.constellation.engine.register.jooq.AbstractJooqTestTestCase;
import org.constellation.engine.register.jooq.Tables;
import org.constellation.engine.register.repository.DomainRepository;
import org.jooq.DSLContext;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class JooqDomainRespositoryTestCase extends AbstractJooqTestTestCase {

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private DSLContext dsl;
    
    @Test
    public void all() {
        dump(domainRepository.findAll());
    }

    // @Test
    public void save() {
        Domain domainDTO = new Domain("cadastre", "Domaine du cadastre");
        Domain save = domainRepository.save(domainDTO);
        LOGGER.debug("New domains: " + domainDTO);
    }

    // @Test
    public void update() {
        Domain domainDTO = new Domain(3, "cadastre mec", "Domaine du cadastre");
        domainRepository.update(domainDTO);
        LOGGER.debug("New domains: " + domainDTO);

    }

    // @Test
    public void delete() {
        int n = domainRepository.delete(3);
        LOGGER.debug("Delete " + n + " domains");
    }

    // @Test
    public void testAddUserToDomain() {
        Set<String> roles = new HashSet<String>();
        roles.add("manager");
        domainRepository.addUserToDomain("olivier", 2, roles);
    }

    // @Test
    public void testRemoveUserFromDomain() {
        int removeUserFromDomain = domainRepository.removeUserFromDomain("zozoz", 1);
        LOGGER.debug("Removed: " + removeUserFromDomain);
    }

    @Test
    public void addDataToDomain() {
        int i = domainRepository.addDataToDomain(1, 1);
        LOGGER.debug("Added " + i);
    }

    @Test
    public void removeDataFromDomain() {
        int i = domainRepository.removeDataFromDomain(2, 1);
        LOGGER.debug("Removed " + i);
    }

    @Test
    public void removeAllDataFromDomain() {
        int i = domainRepository.removeAllDataFromDomain(1);
        LOGGER.debug("Removed " + i);
    }

    @Test
    public void addDataFromProviderToDomain() {
        int count = domainRepository.addProviderDataToDomain("bluemarble", 1);
        LOGGER.debug("Added " + count + " data to domain");
    }

    // @Test
    public void findOne() {
        Domain domain = domainRepository.findOne(1);
        dump(domain);
    }
    
    @Test
    public void test() {
        dump(dsl.select().from(Tables.DATA_X_DOMAIN).fetch());
        dump(dsl.select().from(Tables.DATA).fetch());
        
        dump(dsl.select(DATA.ID, PROVIDER.ID)
                        .from(DATA)
                        .join(PROVIDER)
                        .onKey()
                        .where(PROVIDER.IDENTIFIER.eq("generic_shp"))
                        .and(DATA.ID.notIn(dsl.select(DATA_X_DOMAIN.DATA_ID).from(DATA_X_DOMAIN)
                                .where(DATA_X_DOMAIN.DOMAIN_ID.eq(11)))).fetch());
    }

}
