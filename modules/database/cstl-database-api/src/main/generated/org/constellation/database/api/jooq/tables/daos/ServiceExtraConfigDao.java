/**
 * This class is generated by jOOQ
 */
package org.constellation.database.api.jooq.tables.daos;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.5.3"
	},
	comments = "This class is generated by jOOQ"
)
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ServiceExtraConfigDao extends org.jooq.impl.DAOImpl<org.constellation.database.api.jooq.tables.records.ServiceExtraConfigRecord, org.constellation.database.api.jooq.tables.pojos.ServiceExtraConfig, org.jooq.Record2<java.lang.Integer, java.lang.String>> {

	/**
	 * Create a new ServiceExtraConfigDao without any configuration
	 */
	public ServiceExtraConfigDao() {
		super(org.constellation.database.api.jooq.tables.ServiceExtraConfig.SERVICE_EXTRA_CONFIG, org.constellation.database.api.jooq.tables.pojos.ServiceExtraConfig.class);
	}

	/**
	 * Create a new ServiceExtraConfigDao with an attached configuration
	 */
	public ServiceExtraConfigDao(org.jooq.Configuration configuration) {
		super(org.constellation.database.api.jooq.tables.ServiceExtraConfig.SERVICE_EXTRA_CONFIG, org.constellation.database.api.jooq.tables.pojos.ServiceExtraConfig.class, configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected org.jooq.Record2<java.lang.Integer, java.lang.String> getId(org.constellation.database.api.jooq.tables.pojos.ServiceExtraConfig object) {
		return compositeKeyRecord(object.getId(), object.getFilename());
	}

	/**
	 * Fetch records that have <code>id IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.ServiceExtraConfig> fetchById(java.lang.Integer... values) {
		return fetch(org.constellation.database.api.jooq.tables.ServiceExtraConfig.SERVICE_EXTRA_CONFIG.ID, values);
	}

	/**
	 * Fetch records that have <code>filename IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.ServiceExtraConfig> fetchByFilename(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.ServiceExtraConfig.SERVICE_EXTRA_CONFIG.FILENAME, values);
	}

	/**
	 * Fetch records that have <code>content IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.ServiceExtraConfig> fetchByContent(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.ServiceExtraConfig.SERVICE_EXTRA_CONFIG.CONTENT, values);
	}
}
