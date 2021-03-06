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
public class TaskRecord extends org.jooq.impl.UpdatableRecordImpl<org.constellation.database.api.jooq.tables.records.TaskRecord> implements org.jooq.Record10<java.lang.String, java.lang.String, java.lang.String, java.lang.Long, java.lang.Long, java.lang.Integer, java.lang.String, java.lang.Integer, java.lang.Double, java.lang.String> {

	private static final long serialVersionUID = -602727465;

	/**
	 * Setter for <code>admin.task.identifier</code>.
	 */
	public TaskRecord setIdentifier(java.lang.String value) {
		setValue(0, value);
		return this;
	}

	/**
	 * Getter for <code>admin.task.identifier</code>.
	 */
	@javax.validation.constraints.NotNull
	@javax.validation.constraints.Size(max = 512)
	public java.lang.String getIdentifier() {
		return (java.lang.String) getValue(0);
	}

	/**
	 * Setter for <code>admin.task.state</code>.
	 */
	public TaskRecord setState(java.lang.String value) {
		setValue(1, value);
		return this;
	}

	/**
	 * Getter for <code>admin.task.state</code>.
	 */
	@javax.validation.constraints.NotNull
	@javax.validation.constraints.Size(max = 32)
	public java.lang.String getState() {
		return (java.lang.String) getValue(1);
	}

	/**
	 * Setter for <code>admin.task.type</code>.
	 */
	public TaskRecord setType(java.lang.String value) {
		setValue(2, value);
		return this;
	}

	/**
	 * Getter for <code>admin.task.type</code>.
	 */
	@javax.validation.constraints.NotNull
	@javax.validation.constraints.Size(max = 32)
	public java.lang.String getType() {
		return (java.lang.String) getValue(2);
	}

	/**
	 * Setter for <code>admin.task.date_start</code>.
	 */
	public TaskRecord setDateStart(java.lang.Long value) {
		setValue(3, value);
		return this;
	}

	/**
	 * Getter for <code>admin.task.date_start</code>.
	 */
	@javax.validation.constraints.NotNull
	public java.lang.Long getDateStart() {
		return (java.lang.Long) getValue(3);
	}

	/**
	 * Setter for <code>admin.task.date_end</code>.
	 */
	public TaskRecord setDateEnd(java.lang.Long value) {
		setValue(4, value);
		return this;
	}

	/**
	 * Getter for <code>admin.task.date_end</code>.
	 */
	public java.lang.Long getDateEnd() {
		return (java.lang.Long) getValue(4);
	}

	/**
	 * Setter for <code>admin.task.owner</code>.
	 */
	public TaskRecord setOwner(java.lang.Integer value) {
		setValue(5, value);
		return this;
	}

	/**
	 * Getter for <code>admin.task.owner</code>.
	 */
	public java.lang.Integer getOwner() {
		return (java.lang.Integer) getValue(5);
	}

	/**
	 * Setter for <code>admin.task.message</code>.
	 */
	public TaskRecord setMessage(java.lang.String value) {
		setValue(6, value);
		return this;
	}

	/**
	 * Getter for <code>admin.task.message</code>.
	 */
	public java.lang.String getMessage() {
		return (java.lang.String) getValue(6);
	}

	/**
	 * Setter for <code>admin.task.task_parameter_id</code>.
	 */
	public TaskRecord setTaskParameterId(java.lang.Integer value) {
		setValue(7, value);
		return this;
	}

	/**
	 * Getter for <code>admin.task.task_parameter_id</code>.
	 */
	public java.lang.Integer getTaskParameterId() {
		return (java.lang.Integer) getValue(7);
	}

	/**
	 * Setter for <code>admin.task.progress</code>.
	 */
	public TaskRecord setProgress(java.lang.Double value) {
		setValue(8, value);
		return this;
	}

	/**
	 * Getter for <code>admin.task.progress</code>.
	 */
	public java.lang.Double getProgress() {
		return (java.lang.Double) getValue(8);
	}

	/**
	 * Setter for <code>admin.task.task_output</code>.
	 */
	public TaskRecord setTaskOutput(java.lang.String value) {
		setValue(9, value);
		return this;
	}

	/**
	 * Getter for <code>admin.task.task_output</code>.
	 */
	public java.lang.String getTaskOutput() {
		return (java.lang.String) getValue(9);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Record1<java.lang.String> key() {
		return (org.jooq.Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Record10 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row10<java.lang.String, java.lang.String, java.lang.String, java.lang.Long, java.lang.Long, java.lang.Integer, java.lang.String, java.lang.Integer, java.lang.Double, java.lang.String> fieldsRow() {
		return (org.jooq.Row10) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row10<java.lang.String, java.lang.String, java.lang.String, java.lang.Long, java.lang.Long, java.lang.Integer, java.lang.String, java.lang.Integer, java.lang.Double, java.lang.String> valuesRow() {
		return (org.jooq.Row10) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field1() {
		return org.constellation.database.api.jooq.tables.Task.TASK.IDENTIFIER;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field2() {
		return org.constellation.database.api.jooq.tables.Task.TASK.STATE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field3() {
		return org.constellation.database.api.jooq.tables.Task.TASK.TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Long> field4() {
		return org.constellation.database.api.jooq.tables.Task.TASK.DATE_START;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Long> field5() {
		return org.constellation.database.api.jooq.tables.Task.TASK.DATE_END;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field6() {
		return org.constellation.database.api.jooq.tables.Task.TASK.OWNER;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field7() {
		return org.constellation.database.api.jooq.tables.Task.TASK.MESSAGE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field8() {
		return org.constellation.database.api.jooq.tables.Task.TASK.TASK_PARAMETER_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Double> field9() {
		return org.constellation.database.api.jooq.tables.Task.TASK.PROGRESS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field10() {
		return org.constellation.database.api.jooq.tables.Task.TASK.TASK_OUTPUT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value1() {
		return getIdentifier();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value2() {
		return getState();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value3() {
		return getType();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Long value4() {
		return getDateStart();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Long value5() {
		return getDateEnd();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value6() {
		return getOwner();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value7() {
		return getMessage();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value8() {
		return getTaskParameterId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Double value9() {
		return getProgress();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value10() {
		return getTaskOutput();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaskRecord value1(java.lang.String value) {
		setIdentifier(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaskRecord value2(java.lang.String value) {
		setState(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaskRecord value3(java.lang.String value) {
		setType(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaskRecord value4(java.lang.Long value) {
		setDateStart(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaskRecord value5(java.lang.Long value) {
		setDateEnd(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaskRecord value6(java.lang.Integer value) {
		setOwner(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaskRecord value7(java.lang.String value) {
		setMessage(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaskRecord value8(java.lang.Integer value) {
		setTaskParameterId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaskRecord value9(java.lang.Double value) {
		setProgress(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaskRecord value10(java.lang.String value) {
		setTaskOutput(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaskRecord values(java.lang.String value1, java.lang.String value2, java.lang.String value3, java.lang.Long value4, java.lang.Long value5, java.lang.Integer value6, java.lang.String value7, java.lang.Integer value8, java.lang.Double value9, java.lang.String value10) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached TaskRecord
	 */
	public TaskRecord() {
		super(org.constellation.database.api.jooq.tables.Task.TASK);
	}

	/**
	 * Create a detached, initialised TaskRecord
	 */
	public TaskRecord(java.lang.String identifier, java.lang.String state, java.lang.String type, java.lang.Long dateStart, java.lang.Long dateEnd, java.lang.Integer owner, java.lang.String message, java.lang.Integer taskParameterId, java.lang.Double progress, java.lang.String taskOutput) {
		super(org.constellation.database.api.jooq.tables.Task.TASK);

		setValue(0, identifier);
		setValue(1, state);
		setValue(2, type);
		setValue(3, dateStart);
		setValue(4, dateEnd);
		setValue(5, owner);
		setValue(6, message);
		setValue(7, taskParameterId);
		setValue(8, progress);
		setValue(9, taskOutput);
	}
}
