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
public class ServiceDetailsDao extends org.jooq.impl.DAOImpl<org.constellation.database.api.jooq.tables.records.ServiceDetailsRecord, org.constellation.database.api.jooq.tables.pojos.ServiceDetails, org.jooq.Record2<java.lang.Integer, java.lang.String>> {

	/**
	 * Create a new ServiceDetailsDao without any configuration
	 */
	public ServiceDetailsDao() {
		super(org.constellation.database.api.jooq.tables.ServiceDetails.SERVICE_DETAILS, org.constellation.database.api.jooq.tables.pojos.ServiceDetails.class);
	}

	/**
	 * Create a new ServiceDetailsDao with an attached configuration
	 */
	public ServiceDetailsDao(org.jooq.Configuration configuration) {
		super(org.constellation.database.api.jooq.tables.ServiceDetails.SERVICE_DETAILS, org.constellation.database.api.jooq.tables.pojos.ServiceDetails.class, configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected org.jooq.Record2<java.lang.Integer, java.lang.String> getId(org.constellation.database.api.jooq.tables.pojos.ServiceDetails object) {
		return compositeKeyRecord(object.getId(), object.getLang());
	}

	/**
	 * Fetch records that have <code>id IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.ServiceDetails> fetchById(java.lang.Integer... values) {
		return fetch(org.constellation.database.api.jooq.tables.ServiceDetails.SERVICE_DETAILS.ID, values);
	}

	/**
	 * Fetch records that have <code>lang IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.ServiceDetails> fetchByLang(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.ServiceDetails.SERVICE_DETAILS.LANG, values);
	}

	/**
	 * Fetch records that have <code>content IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.ServiceDetails> fetchByContent(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.ServiceDetails.SERVICE_DETAILS.CONTENT, values);
	}

	/**
	 * Fetch records that have <code>default_lang IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.ServiceDetails> fetchByDefaultLang(java.lang.Boolean... values) {
		return fetch(org.constellation.database.api.jooq.tables.ServiceDetails.SERVICE_DETAILS.DEFAULT_LANG, values);
	}
}
