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
public class MetadataBboxDao extends org.jooq.impl.DAOImpl<org.constellation.database.api.jooq.tables.records.MetadataBboxRecord, org.constellation.database.api.jooq.tables.pojos.MetadataBbox, org.jooq.Record5<java.lang.Integer, java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Double>> {

	/**
	 * Create a new MetadataBboxDao without any configuration
	 */
	public MetadataBboxDao() {
		super(org.constellation.database.api.jooq.tables.MetadataBbox.METADATA_BBOX, org.constellation.database.api.jooq.tables.pojos.MetadataBbox.class);
	}

	/**
	 * Create a new MetadataBboxDao with an attached configuration
	 */
	public MetadataBboxDao(org.jooq.Configuration configuration) {
		super(org.constellation.database.api.jooq.tables.MetadataBbox.METADATA_BBOX, org.constellation.database.api.jooq.tables.pojos.MetadataBbox.class, configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected org.jooq.Record5<java.lang.Integer, java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Double> getId(org.constellation.database.api.jooq.tables.pojos.MetadataBbox object) {
		return compositeKeyRecord(object.getMetadataId(), object.getEast(), object.getWest(), object.getNorth(), object.getSouth());
	}

	/**
	 * Fetch records that have <code>metadata_id IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.MetadataBbox> fetchByMetadataId(java.lang.Integer... values) {
		return fetch(org.constellation.database.api.jooq.tables.MetadataBbox.METADATA_BBOX.METADATA_ID, values);
	}

	/**
	 * Fetch records that have <code>east IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.MetadataBbox> fetchByEast(java.lang.Double... values) {
		return fetch(org.constellation.database.api.jooq.tables.MetadataBbox.METADATA_BBOX.EAST, values);
	}

	/**
	 * Fetch records that have <code>west IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.MetadataBbox> fetchByWest(java.lang.Double... values) {
		return fetch(org.constellation.database.api.jooq.tables.MetadataBbox.METADATA_BBOX.WEST, values);
	}

	/**
	 * Fetch records that have <code>north IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.MetadataBbox> fetchByNorth(java.lang.Double... values) {
		return fetch(org.constellation.database.api.jooq.tables.MetadataBbox.METADATA_BBOX.NORTH, values);
	}

	/**
	 * Fetch records that have <code>south IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.MetadataBbox> fetchBySouth(java.lang.Double... values) {
		return fetch(org.constellation.database.api.jooq.tables.MetadataBbox.METADATA_BBOX.SOUTH, values);
	}
}
