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
import static org.constellation.engine.register.jooq.Tables.PROVIDER;

import java.util.HashSet;
import java.util.Set;

import org.constellation.engine.register.jooq.AbstractJooqTestTestCase;
import org.constellation.engine.register.jooq.Tables;
import org.constellation.engine.register.jooq.TestSamples;
import org.constellation.engine.register.jooq.tables.pojos.CstlUser;
import org.constellation.engine.register.jooq.tables.pojos.Data;
import org.constellation.engine.register.jooq.tables.pojos.Domain;
import org.constellation.engine.register.jooq.tables.pojos.Provider;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.DomainRepository;
import org.constellation.engine.register.repository.ProviderRepository;
import org.constellation.engine.register.repository.UserRepository;
import org.jooq.DSLContext;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class JooqDomainRespositoryTestCase extends AbstractJooqTestTestCase {

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private DataRepository dataRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private DSLContext dsl;

    @Test
    public void all() {
        dump(domainRepository.findAll());
    }

    @Test
    @Transactional()
    public void crude() {
        Domain domainDTO = new Domain(0, "cadastre", "Domaine du cadastre", false);
        Domain saved = domainRepository.save(domainDTO);
        Assert.assertNotNull("Should return saved object", saved);
        String description = "New description";
        saved.setDescription(description);
        Domain update = domainRepository.update(saved);
        Assert.assertEquals("Sould have new decription", description, update.getDescription());
        Assert.assertEquals("Should have deleted 1 record", 1, domainRepository.delete(saved.getId()));
    }

    @Test
    @Transactional()
    public void testAddUserToDomain() {
        CstlUser user = userRepository.insert(TestSamples.newAdminUser(), TestSamples.adminRoles());
        Domain domain = domainRepository.save(TestSamples.newDomain());
        Set<Integer> roles = new HashSet<>();
        roles.add(1);

        int[] added = domainRepository.addUserToDomain(user.getId(), domain.getId(), roles);

        Assert.assertArrayEquals(new int[] { 1 }, added);

        domainRepository.removeUserFromDomain(user.getId(), domain.getId());

        userRepository.delete(user.getId());
    }

    @Test
    @Transactional()
    public void addDataToDomain() {
        Domain domain = domainRepository.save(TestSamples.newDomain());
        CstlUser user = userRepository.insert(TestSamples.newAdminUser(), TestSamples.adminRoles());
        Provider provider = providerRepository.insert(TestSamples.newProvider(user));
        Data data = dataRepository.create(TestSamples.newData(user, provider));

        int i = domainRepository.addDataToDomain(data.getId(), domain.getId());
        Assert.assertEquals("Should have inserted 1 record", 1, i);
        int j = domainRepository.removeDataFromDomain(data.getId(), domain.getId());
        Assert.assertEquals("Should have deleted 1 record", 1, j);
        domainRepository.delete(domain.getId());
        dataRepository.delete(data.getId());
        providerRepository.delete(provider.getId());
        userRepository.delete(user.getId());
    }

    @Test
    @Transactional()
    public void removeDataFromDomain() {
        int i = domainRepository.removeDataFromDomain(2, 1);
        LOGGER.debug("Removed " + i);
    }

    @Test
    @Transactional()
    public void removeAllDataFromDomain() {
        int i = domainRepository.removeAllDataFromDomain(1);
        LOGGER.debug("Removed " + i);
    }

    @Test
    @Transactional()
    public void addDataFromProviderToDomain() {
        int count = domainRepository.addProviderDataToDomain("bluemarble", 1);
        LOGGER.debug("Added " + count + " data to domain");
    }

    // @Test
    public void findOne() {
        Domain domain = domainRepository.findOne(1);
        dump(domain);
    }

    // @Test
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
