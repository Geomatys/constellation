package org.constellation.engine.register.jooq;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.constellation.engine.register.jooq.tables.pojos.CstlUser;
import org.constellation.engine.register.jooq.tables.pojos.Data;
import org.constellation.engine.register.jooq.tables.pojos.Provider;

public class TestSamples {

    public static CstlUser newAdminUser() {
        CstlUser user = new CstlUser();
        user.setFirstname("olivier");
        user.setLastname("Nouguier");
        user.setLogin("olivier");
        user.setEmail("olvier.nouguier@gmail.com");
        user.setPassword("zozozozo");
        user.setFirstname("olivier");
        user.setActive(Boolean.TRUE);
        user.setLocale("fr");

        return user;
    }
    
    public static Data newData(CstlUser owner, Provider provider) {
        Data data = new Data();
        data.setDate(new Date().getTime());
        data.setName("testdata");
        data.setNamespace("");
        data.setOwner(owner.getId());
        data.setType("type");
        data.setProvider(provider.getId());
        return data;
    }

    public static Provider newProvider(CstlUser owner) {
        Provider provider = new Provider();
        provider.setIdentifier("test");
        provider.setImpl("immmmp");
        provider.setOwner(owner.getId());
        provider.setType("coverage");
        provider.setParent("");
        provider.setConfig("<root />");
        return provider;
    }

    public static List<String> adminRoles() {
        return Collections.singletonList("cstl-admin");
    }

}
