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
public class ServiceExtraConfig implements java.io.Serializable {

	private static final long serialVersionUID = -1906635730;

	private java.lang.Integer id;
	private java.lang.String  filename;
	private java.lang.String  content;

	public ServiceExtraConfig() {}

	public ServiceExtraConfig(
		java.lang.Integer id,
		java.lang.String  filename,
		java.lang.String  content
	) {
		this.id = id;
		this.filename = filename;
		this.content = content;
	}

	@javax.validation.constraints.NotNull
	public java.lang.Integer getId() {
		return this.id;
	}

	public ServiceExtraConfig setId(java.lang.Integer id) {
		this.id = id;
		return this;
	}

	@javax.validation.constraints.NotNull
	@javax.validation.constraints.Size(max = 32)
	public java.lang.String getFilename() {
		return this.filename;
	}

	public ServiceExtraConfig setFilename(java.lang.String filename) {
		this.filename = filename;
		return this;
	}

	public java.lang.String getContent() {
		return this.content;
	}

	public ServiceExtraConfig setContent(java.lang.String content) {
		this.content = content;
		return this;
	}
}
