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
public class DataRecord extends org.jooq.impl.UpdatableRecordImpl<org.constellation.database.api.jooq.tables.records.DataRecord> implements org.jooq.Record17<java.lang.Integer, java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.Boolean, java.lang.Long, java.lang.Integer, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.String, java.lang.Boolean> {

	private static final long serialVersionUID = 1245418444;

	/**
	 * Setter for <code>admin.data.id</code>.
	 */
	public DataRecord setId(java.lang.Integer value) {
		setValue(0, value);
		return this;
	}

	/**
	 * Getter for <code>admin.data.id</code>.
	 */
	@javax.validation.constraints.NotNull
	public java.lang.Integer getId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>admin.data.name</code>.
	 */
	public DataRecord setName(java.lang.String value) {
		setValue(1, value);
		return this;
	}

	/**
	 * Getter for <code>admin.data.name</code>.
	 */
	@javax.validation.constraints.NotNull
	@javax.validation.constraints.Size(max = 512)
	public java.lang.String getName() {
		return (java.lang.String) getValue(1);
	}

	/**
	 * Setter for <code>admin.data.namespace</code>.
	 */
	public DataRecord setNamespace(java.lang.String value) {
		setValue(2, value);
		return this;
	}

	/**
	 * Getter for <code>admin.data.namespace</code>.
	 */
	@javax.validation.constraints.NotNull
	@javax.validation.constraints.Size(max = 256)
	public java.lang.String getNamespace() {
		return (java.lang.String) getValue(2);
	}

	/**
	 * Setter for <code>admin.data.provider</code>.
	 */
	public DataRecord setProvider(java.lang.Integer value) {
		setValue(3, value);
		return this;
	}

	/**
	 * Getter for <code>admin.data.provider</code>.
	 */
	@javax.validation.constraints.NotNull
	public java.lang.Integer getProvider() {
		return (java.lang.Integer) getValue(3);
	}

	/**
	 * Setter for <code>admin.data.type</code>.
	 */
	public DataRecord setType(java.lang.String value) {
		setValue(4, value);
		return this;
	}

	/**
	 * Getter for <code>admin.data.type</code>.
	 */
	@javax.validation.constraints.NotNull
	@javax.validation.constraints.Size(max = 32)
	public java.lang.String getType() {
		return (java.lang.String) getValue(4);
	}

	/**
	 * Setter for <code>admin.data.subtype</code>.
	 */
	public DataRecord setSubtype(java.lang.String value) {
		setValue(5, value);
		return this;
	}

	/**
	 * Getter for <code>admin.data.subtype</code>.
	 */
	@javax.validation.constraints.NotNull
	@javax.validation.constraints.Size(max = 32)
	public java.lang.String getSubtype() {
		return (java.lang.String) getValue(5);
	}

	/**
	 * Setter for <code>admin.data.included</code>.
	 */
	public DataRecord setIncluded(java.lang.Boolean value) {
		setValue(6, value);
		return this;
	}

	/**
	 * Getter for <code>admin.data.included</code>.
	 */
	@javax.validation.constraints.NotNull
	public java.lang.Boolean getIncluded() {
		return (java.lang.Boolean) getValue(6);
	}

	/**
	 * Setter for <code>admin.data.sensorable</code>.
	 */
	public DataRecord setSensorable(java.lang.Boolean value) {
		setValue(7, value);
		return this;
	}

	/**
	 * Getter for <code>admin.data.sensorable</code>.
	 */
	@javax.validation.constraints.NotNull
	public java.lang.Boolean getSensorable() {
		return (java.lang.Boolean) getValue(7);
	}

	/**
	 * Setter for <code>admin.data.date</code>.
	 */
	public DataRecord setDate(java.lang.Long value) {
		setValue(8, value);
		return this;
	}

	/**
	 * Getter for <code>admin.data.date</code>.
	 */
	@javax.validation.constraints.NotNull
	public java.lang.Long getDate() {
		return (java.lang.Long) getValue(8);
	}

	/**
	 * Setter for <code>admin.data.owner</code>.
	 */
	public DataRecord setOwner(java.lang.Integer value) {
		setValue(9, value);
		return this;
	}

	/**
	 * Getter for <code>admin.data.owner</code>.
	 */
	public java.lang.Integer getOwner() {
		return (java.lang.Integer) getValue(9);
	}

	/**
	 * Setter for <code>admin.data.metadata</code>.
	 */
	public DataRecord setMetadata(java.lang.String value) {
		setValue(10, value);
		return this;
	}

	/**
	 * Getter for <code>admin.data.metadata</code>.
	 */
	public java.lang.String getMetadata() {
		return (java.lang.String) getValue(10);
	}

	/**
	 * Setter for <code>admin.data.dataset_id</code>.
	 */
	public DataRecord setDatasetId(java.lang.Integer value) {
		setValue(11, value);
		return this;
	}

	/**
	 * Getter for <code>admin.data.dataset_id</code>.
	 */
	public java.lang.Integer getDatasetId() {
		return (java.lang.Integer) getValue(11);
	}

	/**
	 * Setter for <code>admin.data.feature_catalog</code>.
	 */
	public DataRecord setFeatureCatalog(java.lang.String value) {
		setValue(12, value);
		return this;
	}

	/**
	 * Getter for <code>admin.data.feature_catalog</code>.
	 */
	public java.lang.String getFeatureCatalog() {
		return (java.lang.String) getValue(12);
	}

	/**
	 * Setter for <code>admin.data.stats_result</code>.
	 */
	public DataRecord setStatsResult(java.lang.String value) {
		setValue(13, value);
		return this;
	}

	/**
	 * Getter for <code>admin.data.stats_result</code>.
	 */
	public java.lang.String getStatsResult() {
		return (java.lang.String) getValue(13);
	}

	/**
	 * Setter for <code>admin.data.rendered</code>.
	 */
	public DataRecord setRendered(java.lang.Boolean value) {
		setValue(14, value);
		return this;
	}

	/**
	 * Getter for <code>admin.data.rendered</code>.
	 */
	public java.lang.Boolean getRendered() {
		return (java.lang.Boolean) getValue(14);
	}

	/**
	 * Setter for <code>admin.data.stats_state</code>.
	 */
	public DataRecord setStatsState(java.lang.String value) {
		setValue(15, value);
		return this;
	}

	/**
	 * Getter for <code>admin.data.stats_state</code>.
	 */
	public java.lang.String getStatsState() {
		return (java.lang.String) getValue(15);
	}

	/**
	 * Setter for <code>admin.data.hidden</code>.
	 */
	public DataRecord setHidden(java.lang.Boolean value) {
		setValue(16, value);
		return this;
	}

	/**
	 * Getter for <code>admin.data.hidden</code>.
	 */
	@javax.validation.constraints.NotNull
	public java.lang.Boolean getHidden() {
		return (java.lang.Boolean) getValue(16);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Record1<java.lang.Integer> key() {
		return (org.jooq.Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Record17 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row17<java.lang.Integer, java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.Boolean, java.lang.Long, java.lang.Integer, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.String, java.lang.Boolean> fieldsRow() {
		return (org.jooq.Row17) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row17<java.lang.Integer, java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.Boolean, java.lang.Long, java.lang.Integer, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.String, java.lang.Boolean> valuesRow() {
		return (org.jooq.Row17) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return org.constellation.database.api.jooq.tables.Data.DATA.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field2() {
		return org.constellation.database.api.jooq.tables.Data.DATA.NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field3() {
		return org.constellation.database.api.jooq.tables.Data.DATA.NAMESPACE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field4() {
		return org.constellation.database.api.jooq.tables.Data.DATA.PROVIDER;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field5() {
		return org.constellation.database.api.jooq.tables.Data.DATA.TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field6() {
		return org.constellation.database.api.jooq.tables.Data.DATA.SUBTYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Boolean> field7() {
		return org.constellation.database.api.jooq.tables.Data.DATA.INCLUDED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Boolean> field8() {
		return org.constellation.database.api.jooq.tables.Data.DATA.SENSORABLE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Long> field9() {
		return org.constellation.database.api.jooq.tables.Data.DATA.DATE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field10() {
		return org.constellation.database.api.jooq.tables.Data.DATA.OWNER;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field11() {
		return org.constellation.database.api.jooq.tables.Data.DATA.METADATA;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field12() {
		return org.constellation.database.api.jooq.tables.Data.DATA.DATASET_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field13() {
		return org.constellation.database.api.jooq.tables.Data.DATA.FEATURE_CATALOG;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field14() {
		return org.constellation.database.api.jooq.tables.Data.DATA.STATS_RESULT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Boolean> field15() {
		return org.constellation.database.api.jooq.tables.Data.DATA.RENDERED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field16() {
		return org.constellation.database.api.jooq.tables.Data.DATA.STATS_STATE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Boolean> field17() {
		return org.constellation.database.api.jooq.tables.Data.DATA.HIDDEN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value1() {
		return getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value2() {
		return getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value3() {
		return getNamespace();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value4() {
		return getProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value5() {
		return getType();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value6() {
		return getSubtype();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Boolean value7() {
		return getIncluded();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Boolean value8() {
		return getSensorable();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Long value9() {
		return getDate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value10() {
		return getOwner();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value11() {
		return getMetadata();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value12() {
		return getDatasetId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value13() {
		return getFeatureCatalog();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value14() {
		return getStatsResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Boolean value15() {
		return getRendered();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value16() {
		return getStatsState();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Boolean value17() {
		return getHidden();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataRecord value1(java.lang.Integer value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataRecord value2(java.lang.String value) {
		setName(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataRecord value3(java.lang.String value) {
		setNamespace(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataRecord value4(java.lang.Integer value) {
		setProvider(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataRecord value5(java.lang.String value) {
		setType(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataRecord value6(java.lang.String value) {
		setSubtype(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataRecord value7(java.lang.Boolean value) {
		setIncluded(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataRecord value8(java.lang.Boolean value) {
		setSensorable(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataRecord value9(java.lang.Long value) {
		setDate(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataRecord value10(java.lang.Integer value) {
		setOwner(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataRecord value11(java.lang.String value) {
		setMetadata(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataRecord value12(java.lang.Integer value) {
		setDatasetId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataRecord value13(java.lang.String value) {
		setFeatureCatalog(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataRecord value14(java.lang.String value) {
		setStatsResult(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataRecord value15(java.lang.Boolean value) {
		setRendered(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataRecord value16(java.lang.String value) {
		setStatsState(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataRecord value17(java.lang.Boolean value) {
		setHidden(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataRecord values(java.lang.Integer value1, java.lang.String value2, java.lang.String value3, java.lang.Integer value4, java.lang.String value5, java.lang.String value6, java.lang.Boolean value7, java.lang.Boolean value8, java.lang.Long value9, java.lang.Integer value10, java.lang.String value11, java.lang.Integer value12, java.lang.String value13, java.lang.String value14, java.lang.Boolean value15, java.lang.String value16, java.lang.Boolean value17) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached DataRecord
	 */
	public DataRecord() {
		super(org.constellation.database.api.jooq.tables.Data.DATA);
	}

	/**
	 * Create a detached, initialised DataRecord
	 */
	public DataRecord(java.lang.Integer id, java.lang.String name, java.lang.String namespace, java.lang.Integer provider, java.lang.String type, java.lang.String subtype, java.lang.Boolean included, java.lang.Boolean sensorable, java.lang.Long date, java.lang.Integer owner, java.lang.String metadata, java.lang.Integer datasetId, java.lang.String featureCatalog, java.lang.String statsResult, java.lang.Boolean rendered, java.lang.String statsState, java.lang.Boolean hidden) {
		super(org.constellation.database.api.jooq.tables.Data.DATA);

		setValue(0, id);
		setValue(1, name);
		setValue(2, namespace);
		setValue(3, provider);
		setValue(4, type);
		setValue(5, subtype);
		setValue(6, included);
		setValue(7, sensorable);
		setValue(8, date);
		setValue(9, owner);
		setValue(10, metadata);
		setValue(11, datasetId);
		setValue(12, featureCatalog);
		setValue(13, statsResult);
		setValue(14, rendered);
		setValue(15, statsState);
		setValue(16, hidden);
	}
}
