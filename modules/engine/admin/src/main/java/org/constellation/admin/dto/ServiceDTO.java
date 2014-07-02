package org.constellation.admin.dto;

import java.util.Date;



public class ServiceDTO {
	
	
	private int id;
	private String identifier;
    private String type;
    private Date date;
    private String title;
    private String description;
    private String config;
    private String owner;
    private String status;
    private String metadataIso;
    private String metadataId;
    private String versions;
 
    
    
    public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getConfig() {
		return config;
	}
	public void setConfig(String config) {
		this.config = config;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMetadataIso() {
		return metadataIso;
	}
	public void setMetadataIso(String metadataIso) {
		this.metadataIso = metadataIso;
	}
	public String getMetadataId() {
		return metadataId;
	}
	public void setMetadataId(String metadataId) {
		this.metadataId = metadataId;
	}
    public String getVersions() {
        return versions;
    }

    public void setVersions(String versions) {
        this.versions = versions;
    }
	
	@Override
	public String toString() {
		return "ServiceDTO [id=" + id + ", identifier=" + identifier
				+ ", type=" + type + ", date=" + date + ", title=" + title
				+ ", description=" + description + ", config=" + config
				+ ", owner=" + owner + ", status=" + status + ", metadata="
				+ metadataIso + ", metadataId=" + metadataId + "]";
	}

}
