package org.constellation.admin.dao;

import java.sql.SQLException;

public class CRSRecord extends Record{

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

    /**
     * Do nothing in this implementation.
     */
    @Override
    protected void ensureConnectionNotClosed() throws SQLException {}
}
