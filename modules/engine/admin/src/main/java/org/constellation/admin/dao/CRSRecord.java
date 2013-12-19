package org.constellation.admin.dao;

public class CRSRecord implements Record{

    private String dataid;

    private String crscode;

    public String getCrscode() {
        return crscode;
    }

    public void setCrscode(final String crscode) {
        this.crscode = crscode;
    }

    public String getDataid() {
        return dataid;
    }

    public void setDataid(final String dataid) {
        this.dataid = dataid;
    }
}
