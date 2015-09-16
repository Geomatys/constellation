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
public class DataI18nDao extends org.jooq.impl.DAOImpl<org.constellation.database.api.jooq.tables.records.DataI18nRecord, org.constellation.database.api.jooq.tables.pojos.DataI18n, org.jooq.Record2<java.lang.Integer, java.lang.String>> {

	/**
	 * Create a new DataI18nDao without any configuration
	 */
	public DataI18nDao() {
		super(org.constellation.database.api.jooq.tables.DataI18n.DATA_I18N, org.constellation.database.api.jooq.tables.pojos.DataI18n.class);
	}

	/**
	 * Create a new DataI18nDao with an attached configuration
	 */
	public DataI18nDao(org.jooq.Configuration configuration) {
		super(org.constellation.database.api.jooq.tables.DataI18n.DATA_I18N, org.constellation.database.api.jooq.tables.pojos.DataI18n.class, configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected org.jooq.Record2<java.lang.Integer, java.lang.String> getId(org.constellation.database.api.jooq.tables.pojos.DataI18n object) {
		return compositeKeyRecord(object.getDataId(), object.getLang());
	}

	/**
	 * Fetch records that have <code>data_id IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.DataI18n> fetchByDataId(java.lang.Integer... values) {
		return fetch(org.constellation.database.api.jooq.tables.DataI18n.DATA_I18N.DATA_ID, values);
	}

	/**
	 * Fetch records that have <code>lang IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.DataI18n> fetchByLang(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.DataI18n.DATA_I18N.LANG, values);
	}

	/**
	 * Fetch records that have <code>title IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.DataI18n> fetchByTitle(java.lang.Integer... values) {
		return fetch(org.constellation.database.api.jooq.tables.DataI18n.DATA_I18N.TITLE, values);
	}

	/**
	 * Fetch records that have <code>description IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.DataI18n> fetchByDescription(java.lang.Integer... values) {
		return fetch(org.constellation.database.api.jooq.tables.DataI18n.DATA_I18N.DESCRIPTION, values);
	}
}