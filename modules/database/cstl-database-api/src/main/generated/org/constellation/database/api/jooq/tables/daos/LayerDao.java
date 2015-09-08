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
public class LayerDao extends org.jooq.impl.DAOImpl<org.constellation.database.api.jooq.tables.records.LayerRecord, org.constellation.database.api.jooq.tables.pojos.Layer, java.lang.Integer> {

	/**
	 * Create a new LayerDao without any configuration
	 */
	public LayerDao() {
		super(org.constellation.database.api.jooq.tables.Layer.LAYER, org.constellation.database.api.jooq.tables.pojos.Layer.class);
	}

	/**
	 * Create a new LayerDao with an attached configuration
	 */
	public LayerDao(org.jooq.Configuration configuration) {
		super(org.constellation.database.api.jooq.tables.Layer.LAYER, org.constellation.database.api.jooq.tables.pojos.Layer.class, configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected java.lang.Integer getId(org.constellation.database.api.jooq.tables.pojos.Layer object) {
		return object.getId();
	}

	/**
	 * Fetch records that have <code>id IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Layer> fetchById(java.lang.Integer... values) {
		return fetch(org.constellation.database.api.jooq.tables.Layer.LAYER.ID, values);
	}

	/**
	 * Fetch a unique record that has <code>id = value</code>
	 */
	public org.constellation.database.api.jooq.tables.pojos.Layer fetchOneById(java.lang.Integer value) {
		return fetchOne(org.constellation.database.api.jooq.tables.Layer.LAYER.ID, value);
	}

	/**
	 * Fetch records that have <code>name IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Layer> fetchByName(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.Layer.LAYER.NAME, values);
	}

	/**
	 * Fetch records that have <code>namespace IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Layer> fetchByNamespace(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.Layer.LAYER.NAMESPACE, values);
	}

	/**
	 * Fetch records that have <code>alias IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Layer> fetchByAlias(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.Layer.LAYER.ALIAS, values);
	}

	/**
	 * Fetch records that have <code>service IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Layer> fetchByService(java.lang.Integer... values) {
		return fetch(org.constellation.database.api.jooq.tables.Layer.LAYER.SERVICE, values);
	}

	/**
	 * Fetch records that have <code>data IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Layer> fetchByData(java.lang.Integer... values) {
		return fetch(org.constellation.database.api.jooq.tables.Layer.LAYER.DATA, values);
	}

	/**
	 * Fetch records that have <code>date IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Layer> fetchByDate(java.lang.Long... values) {
		return fetch(org.constellation.database.api.jooq.tables.Layer.LAYER.DATE, values);
	}

	/**
	 * Fetch records that have <code>config IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Layer> fetchByConfig(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.Layer.LAYER.CONFIG, values);
	}

	/**
	 * Fetch records that have <code>owner IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Layer> fetchByOwner(java.lang.Integer... values) {
		return fetch(org.constellation.database.api.jooq.tables.Layer.LAYER.OWNER, values);
	}

	/**
	 * Fetch records that have <code>title IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Layer> fetchByTitle(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.Layer.LAYER.TITLE, values);
	}
}
