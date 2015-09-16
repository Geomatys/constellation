/**
 * This class is generated by jOOQ
 */
package org.constellation.database.api.jooq.tables.records;

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
public class DataXDataRecord extends org.jooq.impl.UpdatableRecordImpl<org.constellation.database.api.jooq.tables.records.DataXDataRecord> implements org.jooq.Record2<java.lang.Integer, java.lang.Integer> {

	private static final long serialVersionUID = -1636563042;

	/**
	 * Setter for <code>admin.data_x_data.data_id</code>.
	 */
	public DataXDataRecord setDataId(java.lang.Integer value) {
		setValue(0, value);
		return this;
	}

	/**
	 * Getter for <code>admin.data_x_data.data_id</code>.
	 */
	@javax.validation.constraints.NotNull
	public java.lang.Integer getDataId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>admin.data_x_data.child_id</code>.
	 */
	public DataXDataRecord setChildId(java.lang.Integer value) {
		setValue(1, value);
		return this;
	}

	/**
	 * Getter for <code>admin.data_x_data.child_id</code>.
	 */
	@javax.validation.constraints.NotNull
	public java.lang.Integer getChildId() {
		return (java.lang.Integer) getValue(1);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Record2<java.lang.Integer, java.lang.Integer> key() {
		return (org.jooq.Record2) super.key();
	}

	// -------------------------------------------------------------------------
	// Record2 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row2<java.lang.Integer, java.lang.Integer> fieldsRow() {
		return (org.jooq.Row2) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row2<java.lang.Integer, java.lang.Integer> valuesRow() {
		return (org.jooq.Row2) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return org.constellation.database.api.jooq.tables.DataXData.DATA_X_DATA.DATA_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field2() {
		return org.constellation.database.api.jooq.tables.DataXData.DATA_X_DATA.CHILD_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value1() {
		return getDataId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value2() {
		return getChildId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataXDataRecord value1(java.lang.Integer value) {
		setDataId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataXDataRecord value2(java.lang.Integer value) {
		setChildId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataXDataRecord values(java.lang.Integer value1, java.lang.Integer value2) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached DataXDataRecord
	 */
	public DataXDataRecord() {
		super(org.constellation.database.api.jooq.tables.DataXData.DATA_X_DATA);
	}

	/**
	 * Create a detached, initialised DataXDataRecord
	 */
	public DataXDataRecord(java.lang.Integer dataId, java.lang.Integer childId) {
		super(org.constellation.database.api.jooq.tables.DataXData.DATA_X_DATA);

		setValue(0, dataId);
		setValue(1, childId);
	}
}