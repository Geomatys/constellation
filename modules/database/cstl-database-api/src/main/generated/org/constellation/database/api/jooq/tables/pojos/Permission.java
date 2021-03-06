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
public class Permission implements java.io.Serializable {

	private static final long serialVersionUID = 1048679879;

	private java.lang.Integer id;
	private java.lang.String  name;
	private java.lang.String  description;

	public Permission() {}

	public Permission(
		java.lang.Integer id,
		java.lang.String  name,
		java.lang.String  description
	) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	@javax.validation.constraints.NotNull
	public java.lang.Integer getId() {
		return this.id;
	}

	public Permission setId(java.lang.Integer id) {
		this.id = id;
		return this;
	}

	@javax.validation.constraints.NotNull
	@javax.validation.constraints.Size(max = 32)
	public java.lang.String getName() {
		return this.name;
	}

	public Permission setName(java.lang.String name) {
		this.name = name;
		return this;
	}

	@javax.validation.constraints.NotNull
	@javax.validation.constraints.Size(max = 512)
	public java.lang.String getDescription() {
		return this.description;
	}

	public Permission setDescription(java.lang.String description) {
		this.description = description;
		return this;
	}
}
