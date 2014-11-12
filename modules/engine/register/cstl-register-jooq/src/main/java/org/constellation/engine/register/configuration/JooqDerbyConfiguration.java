package org.constellation.engine.register.configuration;

import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("derby")
@Configuration
public class JooqDerbyConfiguration {

    @Bean(name = "jooq-setting")
    public Settings getSettingsDerby() {
        return new Settings().withRenderNameStyle(RenderNameStyle.QUOTED);
    }

    @Bean(name = "dialect")
    public SQLDialect getDialect() {
        return SQLDialect.DERBY;
    }
}
