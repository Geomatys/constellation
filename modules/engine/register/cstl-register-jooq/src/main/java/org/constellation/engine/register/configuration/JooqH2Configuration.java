package org.constellation.engine.register.configuration;

import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("h2")
@Configuration
public class JooqH2Configuration {

    @Bean(name = "jooq-setting")
    public Settings getSettingsH2() {
        return new Settings().withRenderNameStyle(RenderNameStyle.AS_IS);
    }

    @Bean(name = "dialect")
    public SQLDialect getDialect() {
        return SQLDialect.H2;
    }

}
