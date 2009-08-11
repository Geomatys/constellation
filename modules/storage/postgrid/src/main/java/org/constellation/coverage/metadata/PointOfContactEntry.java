/*
 * Ecocast - NASA Ames Research Center
 * (C) 2008, Ecocast
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.coverage.metadata;


import org.constellation.catalog.Entry;


/**
 * Implementation of a {@linkplain PointOfContact layer metadata entry}.
 *
 * @author Sam Hiatt
 * @version $Id$
 */
final class PointOfContactEntry extends Entry implements PointOfContact {

    private String pocId;
    private String lastName;
    private String firstName;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String country;
    private String zip;
    private String phone;
    private String email;
    private String org;
    private String orgAddress1;
    private String orgAddress2;
    private String orgCity;
    private String orgState;
    private String orgZip;
    private String orgCountry;
    private String orgContact;

    protected PointOfContactEntry(
        final String pocId,
        final String lastName,
        final String firstName,
        final String address1,
        final String address2,
        final String city,
        final String state,
        final String country,
        final String zip,
        final String phone,
        final String email,
        final String org,
        final String orgAddress1,
        final String orgAddress2,
        final String orgCity,
        final String orgState,
        final String orgZip,
        final String orgCountry,
        final String orgContact                                                                                                                                )

    {
        super(pocId);
        this.pocId = pocId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.address1 = address1;
        this.address2 = address2;
        this.city = city;
        this.state = state;
        this.country = country;
        this.zip = zip;
        this.phone = phone;
        this.email = email;
        this.org = org;
        this.orgAddress1 = orgAddress1;
        this.orgAddress2 = orgAddress2;
        this.orgCity = orgCity;
        this.orgState = orgState;
        this.orgZip = orgZip;
        this.orgCountry = orgCountry;
        this.orgContact = orgContact;
    }    

    public String getMetadata() {
        final String closeRow = "</td></tr>";
        String res = "<table border=1>" +
                //"<tr><td>poc id:</td><td>           " + pocId + closeRow +
                "<th colspan=2>Point Of Contact</th>" +
                "<tr><td>Last Name:</td><td>        " + lastName  + closeRow +
                "<tr><td>First Name:</td><td>       " + firstName + closeRow +
                "<tr><td>Address Line 1:</td><td>   " + address1  + closeRow +
                "<tr><td>Address Line 2:</td><td>   " + address2  + closeRow +
                "<tr><td>City:</td><td>             " + city      + closeRow +
                "<tr><td>State:</td><td>            " + state     + closeRow +
                "<tr><td>Country:</td><td>          " + country   + closeRow +
                "<tr><td>Zip:</td><td>              " + zip       + closeRow +
                "<tr><td>Phone:</td><td>            " + phone     + closeRow +
                "<tr><td>Email:</td><td>            " + email     + closeRow +
                "</table>" +
                "<table border=1>" +
                //"<tr><td>Organization:</td><td>     " + org + closeRow +
                "<th colspan=2>" + org + "</th>" +
                "<tr><td>Address Line 1:</td><td>   " + orgAddress1 + closeRow +
                "<tr><td>Address Line 2:</td><td>   " + orgAddress2 + closeRow +
                "<tr><td>City:</td><td>             " + orgCity     + closeRow +
                "<tr><td>State:</td><td>            " + orgState    + closeRow +
                "<tr><td>Country:</td><td>          " + orgCountry  + closeRow +
                "<tr><td>Zip:</td><td>              " + orgZip      + closeRow +
                "<tr><td>Contact:</td><td>          " + orgContact  + closeRow +
                "</table>";
                
        return res;
    }
        public String getName() {
        return firstName + " " + lastName;
    }
    public String getOrg() {
        return org;
    }
    public String getEmail() {
        return email;
    }
}
