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
public class StyledData extends org.jooq.impl.TableImpl<org.constellation.database.api.jooq.tables.records.StyledDataRecord> {

	private static final long serialVersionUID = 829365;

	/**
	 * The reference instance of <code>admin.styled_data</code>
	 */
	public static final org.constellation.database.api.jooq.tables.StyledData STYLED_DATA = new org.constellation.database.api.jooq.tables.StyledData();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.constellation.database.api.jooq.tables.records.StyledDataRecord> getRecordType() {
		return org.constellation.database.api.jooq.tables.records.StyledDataRecord.class;
	}

	/**
	 * The column <code>admin.styled_data.style</code>.
	 */
	public final org.jooq.TableField<org.constellation.database.api.jooq.tables.records.StyledDataRecord, java.lang.Integer> STYLE = createField("style", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>admin.styled_data.data</code>.
	 */
	public final org.jooq.TableField<org.constellation.database.api.jooq.tables.records.StyledDataRecord, java.lang.Integer> DATA = createField("data", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * Create a <code>admin.styled_data</code> table reference
	 */
	public StyledData() {
		this("styled_data", null);
	}

	/**
	 * Create an aliased <code>admin.styled_data</code> table reference
	 */
	public StyledData(java.lang.String alias) {
		this(alias, org.constellation.database.api.jooq.tables.StyledData.STYLED_DATA);
	}

	private StyledData(java.lang.String alias, org.jooq.Table<org.constellation.database.api.jooq.tables.records.StyledDataRecord> aliased) {
		this(alias, aliased, null);
	}

	private StyledData(java.lang.String alias, org.jooq.Table<org.constellation.database.api.jooq.tables.records.StyledDataRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, org.constellation.database.api.jooq.Admin.ADMIN, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<org.constellation.database.api.jooq.tables.records.StyledDataRecord> getPrimaryKey() {
		return org.constellation.database.api.jooq.Keys.STYLED_DATA_PK;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<org.constellation.database.api.jooq.tables.records.StyledDataRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.constellation.database.api.jooq.tables.records.StyledDataRecord>>asList(org.constellation.database.api.jooq.Keys.STYLED_DATA_PK);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<org.constellation.database.api.jooq.tables.records.StyledDataRecord, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<org.constellation.database.api.jooq.tables.records.StyledDataRecord, ?>>asList(org.constellation.database.api.jooq.Keys.STYLED_DATA__STYLED_DATA_STYLE_FK, org.constellation.database.api.jooq.Keys.STYLED_DATA__STYLED_DATA_DATA_FK);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.constellation.database.api.jooq.tables.StyledData as(java.lang.String alias) {
		return new org.constellation.database.api.jooq.tables.StyledData(alias, this);
	}

	/**
	 * Rename this table
	 */
	public org.constellation.database.api.jooq.tables.StyledData rename(java.lang.String name) {
		return new org.constellation.database.api.jooq.tables.StyledData(name, null);
	}
}
