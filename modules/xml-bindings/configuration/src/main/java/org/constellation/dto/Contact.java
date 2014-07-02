/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.dto;

//import juzu.Mapped;

/**
 * Contact part on getCapabilities service section.
 * It's a DTO used from Juzu to constellation server side. It's {@link org.constellation.dto.Service} part.
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 *
 */
//@Mapped
public class Contact {

    private String firstname;

    private String lastname;

    private String fullname;

    private String organisation;

    private String position;

    private String phone;

    private String fax;

    private String email;

    private String address;

    private String city;

    private String state;

    private String zipCode;

    private String country;

    private String url;

    private String hoursOfService;

    private String contactInstructions;

    public Contact() {
    }

    public Contact(final String firstname, final String lastname, final String organisation, final String position,
            final String phone, final String fax, final String email, final String address,
            final String city, final String state, final String zipCode, final String country,
            final String url, final String hoursOfService, final String contactInstructions) {
        this.address = address;
        this.city = city;
        this.country = country;
        this.email = email;
        this.fax = fax;
        this.firstname = firstname;
        this.lastname = lastname;
        this.fullname = firstname+ " "+ lastname;
        this.organisation = organisation;
        this.phone = phone;
        this.position = position;
        this.state = state;
        this.zipCode = zipCode;
        this.url = url;
        this.hoursOfService = hoursOfService;
        this.contactInstructions = contactInstructions;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(final String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(final String lastname) {
        this.lastname = lastname;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setFullname() {
        this.fullname = firstname+ " "+lastname;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHoursOfService() {
        return hoursOfService;
    }

    public void setHoursOfService(String hoursOfService) {
        this.hoursOfService = hoursOfService;
    }

    public String getContactInstructions() {
        return contactInstructions;
    }

    public void setContactInstructions(String contactInstructions) {
        this.contactInstructions = contactInstructions;
    }
}
