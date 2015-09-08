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
public class ChainProcessDao extends org.jooq.impl.DAOImpl<org.constellation.database.api.jooq.tables.records.ChainProcessRecord, org.constellation.database.api.jooq.tables.pojos.ChainProcess, java.lang.Integer> {

	/**
	 * Create a new ChainProcessDao without any configuration
	 */
	public ChainProcessDao() {
		super(org.constellation.database.api.jooq.tables.ChainProcess.CHAIN_PROCESS, org.constellation.database.api.jooq.tables.pojos.ChainProcess.class);
	}

	/**
	 * Create a new ChainProcessDao with an attached configuration
	 */
	public ChainProcessDao(org.jooq.Configuration configuration) {
		super(org.constellation.database.api.jooq.tables.ChainProcess.CHAIN_PROCESS, org.constellation.database.api.jooq.tables.pojos.ChainProcess.class, configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected java.lang.Integer getId(org.constellation.database.api.jooq.tables.pojos.ChainProcess object) {
		return object.getId();
	}

	/**
	 * Fetch records that have <code>id IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.ChainProcess> fetchById(java.lang.Integer... values) {
		return fetch(org.constellation.database.api.jooq.tables.ChainProcess.CHAIN_PROCESS.ID, values);
	}

	/**
	 * Fetch a unique record that has <code>id = value</code>
	 */
	public org.constellation.database.api.jooq.tables.pojos.ChainProcess fetchOneById(java.lang.Integer value) {
		return fetchOne(org.constellation.database.api.jooq.tables.ChainProcess.CHAIN_PROCESS.ID, value);
	}

	/**
	 * Fetch records that have <code>auth IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.ChainProcess> fetchByAuth(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.ChainProcess.CHAIN_PROCESS.AUTH, values);
	}

	/**
	 * Fetch records that have <code>code IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.ChainProcess> fetchByCode(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.ChainProcess.CHAIN_PROCESS.CODE, values);
	}

	/**
	 * Fetch records that have <code>config IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.ChainProcess> fetchByConfig(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.ChainProcess.CHAIN_PROCESS.CONFIG, values);
	}
}
