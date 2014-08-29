package org.constellation.provider;

/**
 * List all {@link DataProviderFactory} names
 * @author Quentin Boileau (Geomatys)
 */
public enum ProviderFactoryType {
    FEATURE_STORE("feature-store"),
    COVERAGE_STORE("coverage-store"),
    COVERAGES_GROUP("coverages-group"),
    COVERAGE_SQL("coverage-sql"),
    OBSERVATION_STORE("observation-store"),
    SERVER_STORE("server-store");

    String type;
    ProviderFactoryType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
