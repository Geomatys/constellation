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
public class StyleI18n extends org.jooq.impl.TableImpl<org.constellation.database.api.jooq.tables.records.StyleI18nRecord> {

	private static final long serialVersionUID = -143820165;

	/**
	 * The reference instance of <code>admin.style_i18n</code>
	 */
	public static final org.constellation.database.api.jooq.tables.StyleI18n STYLE_I18N = new org.constellation.database.api.jooq.tables.StyleI18n();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.constellation.database.api.jooq.tables.records.StyleI18nRecord> getRecordType() {
		return org.constellation.database.api.jooq.tables.records.StyleI18nRecord.class;
	}

	/**
	 * The column <code>admin.style_i18n.style_id</code>.
	 */
	public final org.jooq.TableField<org.constellation.database.api.jooq.tables.records.StyleI18nRecord, java.lang.Integer> STYLE_ID = createField("style_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>admin.style_i18n.lang</code>.
	 */
	public final org.jooq.TableField<org.constellation.database.api.jooq.tables.records.StyleI18nRecord, java.lang.String> LANG = createField("lang", org.jooq.impl.SQLDataType.CHAR.length(2).nullable(false), this, "");

	/**
	 * The column <code>admin.style_i18n.title</code>.
	 */
	public final org.jooq.TableField<org.constellation.database.api.jooq.tables.records.StyleI18nRecord, java.lang.Integer> TITLE = createField("title", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>admin.style_i18n.description</code>.
	 */
	public final org.jooq.TableField<org.constellation.database.api.jooq.tables.records.StyleI18nRecord, java.lang.Integer> DESCRIPTION = createField("description", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * Create a <code>admin.style_i18n</code> table reference
	 */
	public StyleI18n() {
		this("style_i18n", null);
	}

	/**
	 * Create an aliased <code>admin.style_i18n</code> table reference
	 */
	public StyleI18n(java.lang.String alias) {
		this(alias, org.constellation.database.api.jooq.tables.StyleI18n.STYLE_I18N);
	}

	private StyleI18n(java.lang.String alias, org.jooq.Table<org.constellation.database.api.jooq.tables.records.StyleI18nRecord> aliased) {
		this(alias, aliased, null);
	}

	private StyleI18n(java.lang.String alias, org.jooq.Table<org.constellation.database.api.jooq.tables.records.StyleI18nRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, org.constellation.database.api.jooq.Admin.ADMIN, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<org.constellation.database.api.jooq.tables.records.StyleI18nRecord> getPrimaryKey() {
		return org.constellation.database.api.jooq.Keys.STYLE_I18N_PK;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<org.constellation.database.api.jooq.tables.records.StyleI18nRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.constellation.database.api.jooq.tables.records.StyleI18nRecord>>asList(org.constellation.database.api.jooq.Keys.STYLE_I18N_PK);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<org.constellation.database.api.jooq.tables.records.StyleI18nRecord, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<org.constellation.database.api.jooq.tables.records.StyleI18nRecord, ?>>asList(org.constellation.database.api.jooq.Keys.STYLE_I18N__STYLE_I18N_STYLE_ID_FK);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.constellation.database.api.jooq.tables.StyleI18n as(java.lang.String alias) {
		return new org.constellation.database.api.jooq.tables.StyleI18n(alias, this);
	}

	/**
	 * Rename this table
	 */
	public org.constellation.database.api.jooq.tables.StyleI18n rename(java.lang.String name) {
		return new org.constellation.database.api.jooq.tables.StyleI18n(name, null);
	}
}
