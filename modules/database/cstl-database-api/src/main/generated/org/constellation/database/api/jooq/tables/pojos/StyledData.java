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
public class StyledData implements java.io.Serializable {

	private static final long serialVersionUID = 1187227486;

	private java.lang.Integer style;
	private java.lang.Integer data;

	public StyledData() {}

	public StyledData(
		java.lang.Integer style,
		java.lang.Integer data
	) {
		this.style = style;
		this.data = data;
	}

	@javax.validation.constraints.NotNull
	public java.lang.Integer getStyle() {
		return this.style;
	}

	public StyledData setStyle(java.lang.Integer style) {
		this.style = style;
		return this;
	}

	@javax.validation.constraints.NotNull
	public java.lang.Integer getData() {
		return this.data;
	}

	public StyledData setData(java.lang.Integer data) {
		this.data = data;
		return this;
	}
}
