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
public class ChainProcess extends org.jooq.impl.TableImpl<org.constellation.database.api.jooq.tables.records.ChainProcessRecord> {

	private static final long serialVersionUID = -731640922;

	/**
	 * The reference instance of <code>admin.chain_process</code>
	 */
	public static final org.constellation.database.api.jooq.tables.ChainProcess CHAIN_PROCESS = new org.constellation.database.api.jooq.tables.ChainProcess();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.constellation.database.api.jooq.tables.records.ChainProcessRecord> getRecordType() {
		return org.constellation.database.api.jooq.tables.records.ChainProcessRecord.class;
	}

	/**
	 * The column <code>admin.chain_process.id</code>.
	 */
	public final org.jooq.TableField<org.constellation.database.api.jooq.tables.records.ChainProcessRecord, java.lang.Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>admin.chain_process.auth</code>.
	 */
	public final org.jooq.TableField<org.constellation.database.api.jooq.tables.records.ChainProcessRecord, java.lang.String> AUTH = createField("auth", org.jooq.impl.SQLDataType.VARCHAR.length(512), this, "");

	/**
	 * The column <code>admin.chain_process.code</code>.
	 */
	public final org.jooq.TableField<org.constellation.database.api.jooq.tables.records.ChainProcessRecord, java.lang.String> CODE = createField("code", org.jooq.impl.SQLDataType.VARCHAR.length(512), this, "");

	/**
	 * The column <code>admin.chain_process.config</code>.
	 */
	public final org.jooq.TableField<org.constellation.database.api.jooq.tables.records.ChainProcessRecord, java.lang.String> CONFIG = createField("config", org.jooq.impl.SQLDataType.CLOB, this, "");

	/**
	 * Create a <code>admin.chain_process</code> table reference
	 */
	public ChainProcess() {
		this("chain_process", null);
	}

	/**
	 * Create an aliased <code>admin.chain_process</code> table reference
	 */
	public ChainProcess(java.lang.String alias) {
		this(alias, org.constellation.database.api.jooq.tables.ChainProcess.CHAIN_PROCESS);
	}

	private ChainProcess(java.lang.String alias, org.jooq.Table<org.constellation.database.api.jooq.tables.records.ChainProcessRecord> aliased) {
		this(alias, aliased, null);
	}

	private ChainProcess(java.lang.String alias, org.jooq.Table<org.constellation.database.api.jooq.tables.records.ChainProcessRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, org.constellation.database.api.jooq.Admin.ADMIN, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<org.constellation.database.api.jooq.tables.records.ChainProcessRecord, java.lang.Integer> getIdentity() {
		return org.constellation.database.api.jooq.Keys.IDENTITY_CHAIN_PROCESS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<org.constellation.database.api.jooq.tables.records.ChainProcessRecord> getPrimaryKey() {
		return org.constellation.database.api.jooq.Keys.CHAIN_PROCESS_PK;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<org.constellation.database.api.jooq.tables.records.ChainProcessRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.constellation.database.api.jooq.tables.records.ChainProcessRecord>>asList(org.constellation.database.api.jooq.Keys.CHAIN_PROCESS_PK);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.constellation.database.api.jooq.tables.ChainProcess as(java.lang.String alias) {
		return new org.constellation.database.api.jooq.tables.ChainProcess(alias, this);
	}

	/**
	 * Rename this table
	 */
	public org.constellation.database.api.jooq.tables.ChainProcess rename(java.lang.String name) {
		return new org.constellation.database.api.jooq.tables.ChainProcess(name, null);
	}
}
