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
public class DatasetDao extends org.jooq.impl.DAOImpl<org.constellation.database.api.jooq.tables.records.DatasetRecord, org.constellation.database.api.jooq.tables.pojos.Dataset, java.lang.Integer> {

	/**
	 * Create a new DatasetDao without any configuration
	 */
	public DatasetDao() {
		super(org.constellation.database.api.jooq.tables.Dataset.DATASET, org.constellation.database.api.jooq.tables.pojos.Dataset.class);
	}

	/**
	 * Create a new DatasetDao with an attached configuration
	 */
	public DatasetDao(org.jooq.Configuration configuration) {
		super(org.constellation.database.api.jooq.tables.Dataset.DATASET, org.constellation.database.api.jooq.tables.pojos.Dataset.class, configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected java.lang.Integer getId(org.constellation.database.api.jooq.tables.pojos.Dataset object) {
		return object.getId();
	}

	/**
	 * Fetch records that have <code>id IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Dataset> fetchById(java.lang.Integer... values) {
		return fetch(org.constellation.database.api.jooq.tables.Dataset.DATASET.ID, values);
	}

	/**
	 * Fetch a unique record that has <code>id = value</code>
	 */
	public org.constellation.database.api.jooq.tables.pojos.Dataset fetchOneById(java.lang.Integer value) {
		return fetchOne(org.constellation.database.api.jooq.tables.Dataset.DATASET.ID, value);
	}

	/**
	 * Fetch records that have <code>identifier IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Dataset> fetchByIdentifier(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.Dataset.DATASET.IDENTIFIER, values);
	}

	/**
	 * Fetch records that have <code>owner IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Dataset> fetchByOwner(java.lang.Integer... values) {
		return fetch(org.constellation.database.api.jooq.tables.Dataset.DATASET.OWNER, values);
	}

	/**
	 * Fetch records that have <code>date IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Dataset> fetchByDate(java.lang.Long... values) {
		return fetch(org.constellation.database.api.jooq.tables.Dataset.DATASET.DATE, values);
	}

	/**
	 * Fetch records that have <code>feature_catalog IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Dataset> fetchByFeatureCatalog(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.Dataset.DATASET.FEATURE_CATALOG, values);
	}
}
