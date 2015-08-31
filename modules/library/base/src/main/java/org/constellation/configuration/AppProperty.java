package org.constellation.configuration;

/**
 * Gather all application configuration properties keys.
 *
 * @author Quentin Boileau (Geomatys)
 */
public enum AppProperty {
    /**
     * Application database URL in Hiroku like format
     * "protocol://login:password@host:port/instance"
     */
    CSTL_DATABASE_URL("DATABASE_URL"),

    /**
     * EPSG database URL in Hiroku like format
     * "protocol://login:password@host:port/instance"
     */
    EPSG_DATABASE_URL("EPSG_DATABASE_URL"),

    /**
     * Testing database URL in Hiroku like format
     * "protocol://login:password@host:port/instance"
     */
    TEST_DATABASE_URL("TEST_DATABASE_URL"),

    /**
     * Path to application external configuration properties file
     */
    CSTL_CONFIG("cstl.config"),

    /**
     * Constellation application URL
     */
    CSTL_URL("cstl.url"),

    /**
     * Application home directory
     */
    CSTL_HOME("cstl.home"),

    /**
     * Application data directory
     */
    CSTL_DATA("cstl.data"),

    /**
     * Constellation service URL
     */
    CSTL_SERVICE_URL("cstl.service.url"),

    /**
     * Constellation authentication token lifespan in minutes
     */
    CSTL_TOKEN_LIFE("cstl.token.life"),

    /**
     * Seed used to generate token
     */
    CSTL_TOKEN_SECRET("cstl.secret"),

    /**
     * Flag that enable/disable mail service
     */
    CSTL_MAIL_ENABLE("cstl.mail.enabled"),

    CSTL_MAIL_SMTP_FROM("cstl.mail.smtp.from"),
    CSTL_MAIL_SMTP_HOST("cstl.mail.smtp.host"),
    CSTL_MAIL_SMTP_PORT("cstl.mail.smtp.port"),
    CSTL_MAIL_SMTP_USER("cstl.mail.smtp.username"),
    CSTL_MAIL_SMTP_PASSWD("cstl.mail.smtp.password"),
    CSTL_MAIL_SMTP_USE_SSL("cstl.mail.smtp.ssl"),

    /**
     * Flag that enable or disable automatic statistic computing.
     * If disable, may cause errors on style creation dashboard
     */
    DATA_AUTO_ANALYSE("data.auto.analyse");

    private final String key;

    AppProperty(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
