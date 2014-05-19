/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
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
