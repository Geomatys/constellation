package org.constellation.admin.dto.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents a summary of metadata used in Metadata dashboard page.
 *
 * @author Mehdi Sidhoum (Geomatys).
 */
public class MetadataBrief implements Serializable {

    private int id;

    private String fileIdentifier;

    private String title;

    private String resume;

    private String type;

    private User user;

    private Long updateDate;

    private Long creationDate;

    private Integer mdCompletion;

    private String levelCompletion;

    private String parentFileIdentifier;

    private Boolean isValidated;

    private Boolean isPublished;

    private String validationRequired;

    private String comment;

    private List<String> keywords = new ArrayList<>();

    private List<MetadataBrief> linkedMetadata = new ArrayList<>();

    public MetadataBrief() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileIdentifier() {
        return fileIdentifier;
    }

    public void setFileIdentifier(String fileIdentifier) {
        this.fileIdentifier = fileIdentifier;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getMdCompletion() {
        return mdCompletion;
    }

    public void setMdCompletion(Integer mdCompletion) {
        this.mdCompletion = mdCompletion;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<MetadataBrief> getLinkedMetadata() {
        return linkedMetadata;
    }

    public void setLinkedMetadata(List<MetadataBrief> linkedMetadata) {
        this.linkedMetadata = linkedMetadata;
    }

    public Long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Long updateDate) {
        this.updateDate = updateDate;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public String getLevelCompletion() {
        return levelCompletion;
    }

    public void setLevelCompletion(String levelCompletion) {
        this.levelCompletion = levelCompletion;
    }

    public String getParentFileIdentifier() {
        return parentFileIdentifier;
    }

    public void setParentFileIdentifier(String parentFileIdentifier) {
        this.parentFileIdentifier = parentFileIdentifier;
    }

    public Boolean getIsValidated() {
        return isValidated;
    }

    public void setIsValidated(Boolean isValidated) {
        this.isValidated = isValidated;
    }

    public Boolean getIsPublished() {
        return isPublished;
    }

    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }

    public String getValidationRequired() {
        return validationRequired;
    }

    public void setValidationRequired(String validationRequired) {
        this.validationRequired = validationRequired;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
