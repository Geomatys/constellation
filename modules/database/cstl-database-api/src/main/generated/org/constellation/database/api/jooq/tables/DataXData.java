/**
 * This class is generated by jOOQ
 */
package org.constellation.database.api.jooq.tables;

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
public class DataXData extends org.jooq.impl.TableImpl<org.constellation.database.api.jooq.tables.records.DataXDataRecord> {

	private static final long serialVersionUID = -1172504959;

	/**
	 * The reference instance of <code>admin.data_x_data</code>
	 */
	public static final org.constellation.database.api.jooq.tables.DataXData DATA_X_DATA = new org.constellation.database.api.jooq.tables.DataXData();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.constellation.database.api.jooq.tables.records.DataXDataRecord> getRecordType() {
		return org.constellation.database.api.jooq.tables.records.DataXDataRecord.class;
	}

	/**
	 * The column <code>admin.data_x_data.data_id</code>.
	 */
	public final org.jooq.TableField<org.constellation.database.api.jooq.tables.records.DataXDataRecord, java.lang.Integer> DATA_ID = createField("data_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>admin.data_x_data.child_id</code>.
	 */
	public final org.jooq.TableField<org.constellation.database.api.jooq.tables.records.DataXDataRecord, java.lang.Integer> CHILD_ID = createField("child_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * Create a <code>admin.data_x_data</code> table reference
	 */
	public DataXData() {
		this("data_x_data", null);
	}

	/**
	 * Create an aliased <code>admin.data_x_data</code> table reference
	 */
	public DataXData(java.lang.String alias) {
		this(alias, org.constellation.database.api.jooq.tables.DataXData.DATA_X_DATA);
	}

	private DataXData(java.lang.String alias, org.jooq.Table<org.constellation.database.api.jooq.tables.records.DataXDataRecord> aliased) {
		this(alias, aliased, null);
	}

	private DataXData(java.lang.String alias, org.jooq.Table<org.constellation.database.api.jooq.tables.records.DataXDataRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, org.constellation.database.api.jooq.Admin.ADMIN, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<org.constellation.database.api.jooq.tables.records.DataXDataRecord> getPrimaryKey() {
		return org.constellation.database.api.jooq.Keys.DATA_X_DATA_PK;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<org.constellation.database.api.jooq.tables.records.DataXDataRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.constellation.database.api.jooq.tables.records.DataXDataRecord>>asList(org.constellation.database.api.jooq.Keys.DATA_X_DATA_PK);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<org.constellation.database.api.jooq.tables.records.DataXDataRecord, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<org.constellation.database.api.jooq.tables.records.DataXDataRecord, ?>>asList(org.constellation.database.api.jooq.Keys.DATA_X_DATA__DATA_X_DATA_CROSS_ID_FK, org.constellation.database.api.jooq.Keys.DATA_X_DATA__DATA_X_DATA_CROSS_ID_FK2);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.constellation.database.api.jooq.tables.DataXData as(java.lang.String alias) {
		return new org.constellation.database.api.jooq.tables.DataXData(alias, this);
	}

	/**
	 * Rename this table
	 */
	public org.constellation.database.api.jooq.tables.DataXData rename(java.lang.String name) {
		return new org.constellation.database.api.jooq.tables.DataXData(name, null);
	}
}