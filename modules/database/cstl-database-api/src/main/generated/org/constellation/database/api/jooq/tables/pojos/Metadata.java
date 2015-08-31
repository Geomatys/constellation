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
public class Metadata implements java.io.Serializable {

	private static final long serialVersionUID = -1016849909;

	private java.lang.Integer id;
	private java.lang.String  metadataId;
	private java.lang.String  metadataIso;
	private java.lang.Integer dataId;
	private java.lang.Integer datasetId;
	private java.lang.Integer serviceId;
	private java.lang.Integer mdCompletion;
	private java.lang.Integer owner;
	private java.lang.Long    datestamp;
	private java.lang.Long    dateCreation;
	private java.lang.String  title;
	private java.lang.String  profile;
	private java.lang.Integer parentIdentifier;
	private java.lang.Boolean isValidated;
	private java.lang.Boolean isPublished;
	private java.lang.String  level;
	private java.lang.String  resume;
	private java.lang.String  validationRequired;
	private java.lang.String  validatedState;
	private java.lang.String  comment;

	public Metadata() {}

	public Metadata(
		java.lang.Integer id,
		java.lang.String  metadataId,
		java.lang.String  metadataIso,
		java.lang.Integer dataId,
		java.lang.Integer datasetId,
		java.lang.Integer serviceId,
		java.lang.Integer mdCompletion,
		java.lang.Integer owner,
		java.lang.Long    datestamp,
		java.lang.Long    dateCreation,
		java.lang.String  title,
		java.lang.String  profile,
		java.lang.Integer parentIdentifier,
		java.lang.Boolean isValidated,
		java.lang.Boolean isPublished,
		java.lang.String  level,
		java.lang.String  resume,
		java.lang.String  validationRequired,
		java.lang.String  validatedState,
		java.lang.String  comment
	) {
		this.id = id;
		this.metadataId = metadataId;
		this.metadataIso = metadataIso;
		this.dataId = dataId;
		this.datasetId = datasetId;
		this.serviceId = serviceId;
		this.mdCompletion = mdCompletion;
		this.owner = owner;
		this.datestamp = datestamp;
		this.dateCreation = dateCreation;
		this.title = title;
		this.profile = profile;
		this.parentIdentifier = parentIdentifier;
		this.isValidated = isValidated;
		this.isPublished = isPublished;
		this.level = level;
		this.resume = resume;
		this.validationRequired = validationRequired;
		this.validatedState = validatedState;
		this.comment = comment;
	}

	@javax.validation.constraints.NotNull
	public java.lang.Integer getId() {
		return this.id;
	}

	public Metadata setId(java.lang.Integer id) {
		this.id = id;
		return this;
	}

	@javax.validation.constraints.NotNull
	@javax.validation.constraints.Size(max = 100)
	public java.lang.String getMetadataId() {
		return this.metadataId;
	}

	public Metadata setMetadataId(java.lang.String metadataId) {
		this.metadataId = metadataId;
		return this;
	}

	@javax.validation.constraints.NotNull
	public java.lang.String getMetadataIso() {
		return this.metadataIso;
	}

	public Metadata setMetadataIso(java.lang.String metadataIso) {
		this.metadataIso = metadataIso;
		return this;
	}

	public java.lang.Integer getDataId() {
		return this.dataId;
	}

	public Metadata setDataId(java.lang.Integer dataId) {
		this.dataId = dataId;
		return this;
	}

	public java.lang.Integer getDatasetId() {
		return this.datasetId;
	}

	public Metadata setDatasetId(java.lang.Integer datasetId) {
		this.datasetId = datasetId;
		return this;
	}

	public java.lang.Integer getServiceId() {
		return this.serviceId;
	}

	public Metadata setServiceId(java.lang.Integer serviceId) {
		this.serviceId = serviceId;
		return this;
	}

	public java.lang.Integer getMdCompletion() {
		return this.mdCompletion;
	}

	public Metadata setMdCompletion(java.lang.Integer mdCompletion) {
		this.mdCompletion = mdCompletion;
		return this;
	}

	public java.lang.Integer getOwner() {
		return this.owner;
	}

	public Metadata setOwner(java.lang.Integer owner) {
		this.owner = owner;
		return this;
	}

	public java.lang.Long getDatestamp() {
		return this.datestamp;
	}

	public Metadata setDatestamp(java.lang.Long datestamp) {
		this.datestamp = datestamp;
		return this;
	}

	public java.lang.Long getDateCreation() {
		return this.dateCreation;
	}

	public Metadata setDateCreation(java.lang.Long dateCreation) {
		this.dateCreation = dateCreation;
		return this;
	}

	@javax.validation.constraints.Size(max = 500)
	public java.lang.String getTitle() {
		return this.title;
	}

	public Metadata setTitle(java.lang.String title) {
		this.title = title;
		return this;
	}

	@javax.validation.constraints.Size(max = 255)
	public java.lang.String getProfile() {
		return this.profile;
	}

	public Metadata setProfile(java.lang.String profile) {
		this.profile = profile;
		return this;
	}

	public java.lang.Integer getParentIdentifier() {
		return this.parentIdentifier;
	}

	public Metadata setParentIdentifier(java.lang.Integer parentIdentifier) {
		this.parentIdentifier = parentIdentifier;
		return this;
	}

	@javax.validation.constraints.NotNull
	public java.lang.Boolean getIsValidated() {
		return this.isValidated;
	}

	public Metadata setIsValidated(java.lang.Boolean isValidated) {
		this.isValidated = isValidated;
		return this;
	}

	@javax.validation.constraints.NotNull
	public java.lang.Boolean getIsPublished() {
		return this.isPublished;
	}

	public Metadata setIsPublished(java.lang.Boolean isPublished) {
		this.isPublished = isPublished;
		return this;
	}

	@javax.validation.constraints.NotNull
	@javax.validation.constraints.Size(max = 50)
	public java.lang.String getLevel() {
		return this.level;
	}

	public Metadata setLevel(java.lang.String level) {
		this.level = level;
		return this;
	}

	@javax.validation.constraints.Size(max = 5000)
	public java.lang.String getResume() {
		return this.resume;
	}

	public Metadata setResume(java.lang.String resume) {
		this.resume = resume;
		return this;
	}

	@javax.validation.constraints.NotNull
	@javax.validation.constraints.Size(max = 10)
	public java.lang.String getValidationRequired() {
		return this.validationRequired;
	}

	public Metadata setValidationRequired(java.lang.String validationRequired) {
		this.validationRequired = validationRequired;
		return this;
	}

	public java.lang.String getValidatedState() {
		return this.validatedState;
	}

	public Metadata setValidatedState(java.lang.String validatedState) {
		this.validatedState = validatedState;
		return this;
	}

	public java.lang.String getComment() {
		return this.comment;
	}

	public Metadata setComment(java.lang.String comment) {
		this.comment = comment;
		return this;
	}
}
