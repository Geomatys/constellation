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
public class SensoredDataDao extends org.jooq.impl.DAOImpl<org.constellation.database.api.jooq.tables.records.SensoredDataRecord, org.constellation.database.api.jooq.tables.pojos.SensoredData, org.jooq.Record2<java.lang.Integer, java.lang.Integer>> {

	/**
	 * Create a new SensoredDataDao without any configuration
	 */
	public SensoredDataDao() {
		super(org.constellation.database.api.jooq.tables.SensoredData.SENSORED_DATA, org.constellation.database.api.jooq.tables.pojos.SensoredData.class);
	}

	/**
	 * Create a new SensoredDataDao with an attached configuration
	 */
	public SensoredDataDao(org.jooq.Configuration configuration) {
		super(org.constellation.database.api.jooq.tables.SensoredData.SENSORED_DATA, org.constellation.database.api.jooq.tables.pojos.SensoredData.class, configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected org.jooq.Record2<java.lang.Integer, java.lang.Integer> getId(org.constellation.database.api.jooq.tables.pojos.SensoredData object) {
		return compositeKeyRecord(object.getSensor(), object.getData());
	}

	/**
	 * Fetch records that have <code>sensor IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.SensoredData> fetchBySensor(java.lang.Integer... values) {
		return fetch(org.constellation.database.api.jooq.tables.SensoredData.SENSORED_DATA.SENSOR, values);
	}

	/**
	 * Fetch records that have <code>data IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.SensoredData> fetchByData(java.lang.Integer... values) {
		return fetch(org.constellation.database.api.jooq.tables.SensoredData.SENSORED_DATA.DATA, values);
	}
}
