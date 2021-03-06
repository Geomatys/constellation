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
public class StyledLayer extends org.jooq.impl.TableImpl<org.constellation.database.api.jooq.tables.records.StyledLayerRecord> {

	private static final long serialVersionUID = 1191049248;

	/**
	 * The reference instance of <code>admin.styled_layer</code>
	 */
	public static final org.constellation.database.api.jooq.tables.StyledLayer STYLED_LAYER = new org.constellation.database.api.jooq.tables.StyledLayer();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.constellation.database.api.jooq.tables.records.StyledLayerRecord> getRecordType() {
		return org.constellation.database.api.jooq.tables.records.StyledLayerRecord.class;
	}

	/**
	 * The column <code>admin.styled_layer.style</code>.
	 */
	public final org.jooq.TableField<org.constellation.database.api.jooq.tables.records.StyledLayerRecord, java.lang.Integer> STYLE = createField("style", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>admin.styled_layer.layer</code>.
	 */
	public final org.jooq.TableField<org.constellation.database.api.jooq.tables.records.StyledLayerRecord, java.lang.Integer> LAYER = createField("layer", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>admin.styled_layer.is_default</code>.
	 */
	public final org.jooq.TableField<org.constellation.database.api.jooq.tables.records.StyledLayerRecord, java.lang.Boolean> IS_DEFAULT = createField("is_default", org.jooq.impl.SQLDataType.BOOLEAN, this, "");

	/**
	 * Create a <code>admin.styled_layer</code> table reference
	 */
	public StyledLayer() {
		this("styled_layer", null);
	}

	/**
	 * Create an aliased <code>admin.styled_layer</code> table reference
	 */
	public StyledLayer(java.lang.String alias) {
		this(alias, org.constellation.database.api.jooq.tables.StyledLayer.STYLED_LAYER);
	}

	private StyledLayer(java.lang.String alias, org.jooq.Table<org.constellation.database.api.jooq.tables.records.StyledLayerRecord> aliased) {
		this(alias, aliased, null);
	}

	private StyledLayer(java.lang.String alias, org.jooq.Table<org.constellation.database.api.jooq.tables.records.StyledLayerRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, org.constellation.database.api.jooq.Admin.ADMIN, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<org.constellation.database.api.jooq.tables.records.StyledLayerRecord> getPrimaryKey() {
		return org.constellation.database.api.jooq.Keys.STYLED_LAYER_PK;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<org.constellation.database.api.jooq.tables.records.StyledLayerRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.constellation.database.api.jooq.tables.records.StyledLayerRecord>>asList(org.constellation.database.api.jooq.Keys.STYLED_LAYER_PK);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<org.constellation.database.api.jooq.tables.records.StyledLayerRecord, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<org.constellation.database.api.jooq.tables.records.StyledLayerRecord, ?>>asList(org.constellation.database.api.jooq.Keys.STYLED_LAYER__STYLED_LAYER_STYLE_FK, org.constellation.database.api.jooq.Keys.STYLED_LAYER__STYLED_LAYER_LAYER_FK);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.constellation.database.api.jooq.tables.StyledLayer as(java.lang.String alias) {
		return new org.constellation.database.api.jooq.tables.StyledLayer(alias, this);
	}

	/**
	 * Rename this table
	 */
	public org.constellation.database.api.jooq.tables.StyledLayer rename(java.lang.String name) {
		return new org.constellation.database.api.jooq.tables.StyledLayer(name, null);
	}
}
