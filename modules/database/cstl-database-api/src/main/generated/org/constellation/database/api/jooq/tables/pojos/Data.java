/**
 * This class is generated by jOOQ
 */
package org.constellation.database.api.jooq.tables.pojos;

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
public class Data implements java.io.Serializable {

	private static final long serialVersionUID = 2054899077;

	private java.lang.Integer id;
	private java.lang.String  name;
	private java.lang.String  namespace;
	private java.lang.Integer provider;
	private java.lang.String  type;
	private java.lang.String  subtype;
	private java.lang.Boolean included;
	private java.lang.Boolean sensorable;
	private java.lang.Long    date;
	private java.lang.Integer owner;
	private java.lang.String  metadata;
	private java.lang.Integer datasetId;
	private java.lang.String  featureCatalog;
	private java.lang.String  statsResult;
	private java.lang.Boolean rendered;
	private java.lang.String  statsState;
	private java.lang.Boolean hidden;

	public Data() {}

	public Data(
		java.lang.Integer id,
		java.lang.String  name,
		java.lang.String  namespace,
		java.lang.Integer provider,
		java.lang.String  type,
		java.lang.String  subtype,
		java.lang.Boolean included,
		java.lang.Boolean sensorable,
		java.lang.Long    date,
		java.lang.Integer owner,
		java.lang.String  metadata,
		java.lang.Integer datasetId,
		java.lang.String  featureCatalog,
		java.lang.String  statsResult,
		java.lang.Boolean rendered,
		java.lang.String  statsState,
		java.lang.Boolean hidden
	) {
		this.id = id;
		this.name = name;
		this.namespace = namespace;
		this.provider = provider;
		this.type = type;
		this.subtype = subtype;
		this.included = included;
		this.sensorable = sensorable;
		this.date = date;
		this.owner = owner;
		this.metadata = metadata;
		this.datasetId = datasetId;
		this.featureCatalog = featureCatalog;
		this.statsResult = statsResult;
		this.rendered = rendered;
		this.statsState = statsState;
		this.hidden = hidden;
	}

	@javax.validation.constraints.NotNull
	public java.lang.Integer getId() {
		return this.id;
	}

	public Data setId(java.lang.Integer id) {
		this.id = id;
		return this;
	}

	@javax.validation.constraints.NotNull
	@javax.validation.constraints.Size(max = 512)
	public java.lang.String getName() {
		return this.name;
	}

	public Data setName(java.lang.String name) {
		this.name = name;
		return this;
	}

	@javax.validation.constraints.NotNull
	@javax.validation.constraints.Size(max = 256)
	public java.lang.String getNamespace() {
		return this.namespace;
	}

	public Data setNamespace(java.lang.String namespace) {
		this.namespace = namespace;
		return this;
	}

	@javax.validation.constraints.NotNull
	public java.lang.Integer getProvider() {
		return this.provider;
	}

	public Data setProvider(java.lang.Integer provider) {
		this.provider = provider;
		return this;
	}

	@javax.validation.constraints.NotNull
	@javax.validation.constraints.Size(max = 32)
	public java.lang.String getType() {
		return this.type;
	}

	public Data setType(java.lang.String type) {
		this.type = type;
		return this;
	}

	@javax.validation.constraints.NotNull
	@javax.validation.constraints.Size(max = 32)
	public java.lang.String getSubtype() {
		return this.subtype;
	}

	public Data setSubtype(java.lang.String subtype) {
		this.subtype = subtype;
		return this;
	}

	@javax.validation.constraints.NotNull
	public java.lang.Boolean getIncluded() {
		return this.included;
	}

	public Data setIncluded(java.lang.Boolean included) {
		this.included = included;
		return this;
	}

	@javax.validation.constraints.NotNull
	public java.lang.Boolean getSensorable() {
		return this.sensorable;
	}

	public Data setSensorable(java.lang.Boolean sensorable) {
		this.sensorable = sensorable;
		return this;
	}

	@javax.validation.constraints.NotNull
	public java.lang.Long getDate() {
		return this.date;
	}

	public Data setDate(java.lang.Long date) {
		this.date = date;
		return this;
	}

	public java.lang.Integer getOwner() {
		return this.owner;
	}

	public Data setOwner(java.lang.Integer owner) {
		this.owner = owner;
		return this;
	}

	public java.lang.String getMetadata() {
		return this.metadata;
	}

	public Data setMetadata(java.lang.String metadata) {
		this.metadata = metadata;
		return this;
	}

	public java.lang.Integer getDatasetId() {
		return this.datasetId;
	}

	public Data setDatasetId(java.lang.Integer datasetId) {
		this.datasetId = datasetId;
		return this;
	}

	public java.lang.String getFeatureCatalog() {
		return this.featureCatalog;
	}

	public Data setFeatureCatalog(java.lang.String featureCatalog) {
		this.featureCatalog = featureCatalog;
		return this;
	}

	public java.lang.String getStatsResult() {
		return this.statsResult;
	}

	public Data setStatsResult(java.lang.String statsResult) {
		this.statsResult = statsResult;
		return this;
	}

	public java.lang.Boolean getRendered() {
		return this.rendered;
	}

	public Data setRendered(java.lang.Boolean rendered) {
		this.rendered = rendered;
		return this;
	}

	public java.lang.String getStatsState() {
		return this.statsState;
	}

	public Data setStatsState(java.lang.String statsState) {
		this.statsState = statsState;
		return this;
	}

	@javax.validation.constraints.NotNull
	public java.lang.Boolean getHidden() {
		return this.hidden;
	}

	public Data setHidden(java.lang.Boolean hidden) {
		this.hidden = hidden;
		return this;
	}
}
