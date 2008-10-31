/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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

package org.constellation.generic.edmo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Organisation")
public class Organisation {
    
    private String n_code;
    private Boolean active;
    private Integer update_count;
    private String input_date;
    private String last_update;
    private String revision_date;
    private String currency_date;
    private String date_created;
    private Boolean organisation_exists;
    private String organisation_changed_into;
    private String organisation_changed_year;
    private String selected;
    private Boolean cdi_active;
    private Boolean simorc_active;
    private Double Longitude;
    private Double Latitude;
    private Integer centre_id_edmed;
    private Integer centre_id_edmerp;
    private Integer centre_id_edios;
    private Integer centre_id_cdi;
    private Integer centre_id_simorc;
    private Integer centre_id_csr;
    private Integer owner_old;
    private Integer collate_id;
    private Boolean national_collator;
    private String logo;
    private String name;
    private String native_name;
    private String address;
    private String zipcode;
    private String city;
    private String state;
    private String country;
    private String email;
    private String phone;
    private String fax;
    private String website;
    private String description_edmerp;
    private String description;
    private String c_country;
    private String n_collate_id;

    public String getN_code() {
        return n_code;
    }

    public void setN_code(String n_code) {
        this.n_code = n_code;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getUpdate_count() {
        return update_count;
    }

    public void setUpdate_count(Integer update_count) {
        this.update_count = update_count;
    }

    public String getInput_date() {
        return input_date;
    }

    public void setInput_date(String input_date) {
        this.input_date = input_date;
    }

    public String getLast_update() {
        return last_update;
    }

    public void setLast_update(String last_update) {
        this.last_update = last_update;
    }

    public String getRevision_date() {
        return revision_date;
    }

    public void setRevision_date(String revision_date) {
        this.revision_date = revision_date;
    }

    public String getCurrency_date() {
        return currency_date;
    }

    public void setCurrency_date(String currency_date) {
        this.currency_date = currency_date;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public Boolean getOrganisation_exists() {
        return organisation_exists;
    }

    public void setOrganisation_exists(Boolean organisation_exists) {
        this.organisation_exists = organisation_exists;
    }

    public String getOrganisation_changed_into() {
        return organisation_changed_into;
    }

    public void setOrganisation_changed_into(String organisation_changed_into) {
        this.organisation_changed_into = organisation_changed_into;
    }

    public String getOrganisation_changed_year() {
        return organisation_changed_year;
    }

    public void setOrganisation_changed_year(String organisation_changed_year) {
        this.organisation_changed_year = organisation_changed_year;
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    public Boolean getCdi_active() {
        return cdi_active;
    }

    public void setCdi_active(Boolean cdi_active) {
        this.cdi_active = cdi_active;
    }

    public Boolean getSimorc_active() {
        return simorc_active;
    }

    public void setSimorc_active(Boolean simorc_active) {
        this.simorc_active = simorc_active;
    }

    public Double getLongitude() {
        return Longitude;
    }

    public void setLongitude(Double Longitude) {
        this.Longitude = Longitude;
    }

    public Double getLatitude() {
        return Latitude;
    }

    public void setLatitude(Double Latitude) {
        this.Latitude = Latitude;
    }

    public Integer getCentre_id_edmed() {
        return centre_id_edmed;
    }

    public void setCentre_id_edmed(Integer centre_id_edmed) {
        this.centre_id_edmed = centre_id_edmed;
    }

    public Integer getCentre_id_edmerp() {
        return centre_id_edmerp;
    }

    public void setCentre_id_edmerp(Integer centre_id_edmerp) {
        this.centre_id_edmerp = centre_id_edmerp;
    }

    public Integer getCentre_id_edios() {
        return centre_id_edios;
    }

    public void setCentre_id_edios(Integer centre_id_edios) {
        this.centre_id_edios = centre_id_edios;
    }

    public Integer getCentre_id_cdi() {
        return centre_id_cdi;
    }

    public void setCentre_id_cdi(Integer centre_id_cdi) {
        this.centre_id_cdi = centre_id_cdi;
    }

    public Integer getCentre_id_simorc() {
        return centre_id_simorc;
    }

    public void setCentre_id_simorc(Integer centre_id_simorc) {
        this.centre_id_simorc = centre_id_simorc;
    }

    public Integer getCentre_id_csr() {
        return centre_id_csr;
    }

    public void setCentre_id_csr(Integer centre_id_csr) {
        this.centre_id_csr = centre_id_csr;
    }

    public Integer getOwner_old() {
        return owner_old;
    }

    public void setOwner_old(Integer owner_old) {
        this.owner_old = owner_old;
    }

    public Integer getCollate_id() {
        return collate_id;
    }

    public void setCollate_id(Integer collate_id) {
        this.collate_id = collate_id;
    }

    public Boolean getNational_collator() {
        return national_collator;
    }

    public void setNational_collator(Boolean national_collator) {
        this.national_collator = national_collator;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNative_name() {
        return native_name;
    }

    public void setNative_name(String native_name) {
        this.native_name = native_name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDescription_edmerp() {
        return description_edmerp;
    }

    public void setDescription_edmerp(String description_edmerp) {
        this.description_edmerp = description_edmerp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getC_country() {
        return c_country;
    }

    public void setC_country(String c_country) {
        this.c_country = c_country;
    }

    public String getN_collate_id() {
        return n_collate_id;
    }

    public void setN_collate_id(String n_collate_id) {
        this.n_collate_id = n_collate_id;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[Organisation]").append('\n');
        if (n_code != null) {
            s.append("n_code: ").append(n_code).append('\n');
        }
        if (active != null) {
            s.append("active: ").append(active).append('\n');
        }
        if (update_count != null) {
            s.append("update_count: ").append(update_count).append('\n');
        }
        if (input_date != null) {
            s.append("input_date: ").append(input_date).append('\n');
        }
        if (last_update != null) {
            s.append("last_update: ").append(last_update).append('\n');
        }
        if (revision_date != null) {             
            s.append("revision_date: ").append(revision_date).append('\n');
        }
        if (currency_date != null) {
            s.append("currency_date: ").append(currency_date).append('\n');
        }
        if (date_created != null) {
            s.append("date_created: ").append(date_created).append('\n');
        }
        if (organisation_exists != null) {
            s.append("organisation_exists: ").append(organisation_exists).append('\n');
        }
        if (organisation_changed_into != null) {
            s.append("organisation_changed_into: ").append(organisation_changed_into).append('\n');
        }
        if (organisation_changed_year != null) {
            s.append("organisation_changed_year: ").append(organisation_changed_year).append('\n');
        }
        if (selected != null) {            
            s.append("selected: ").append(selected).append('\n');
        }
        if (cdi_active != null) {
             s.append("cdi_active: ").append(cdi_active).append('\n');
        }
        if (cdi_active != null) {
             s.append("simorc_active: ").append(simorc_active).append('\n');
        }
        if (Longitude != null) {
             s.append("Longitude: ").append(Longitude).append('\n');
        }
        if (Latitude != null) {
            s.append("Latitude").append(Latitude).append('\n');
        }
        if (logo != null) {
            s.append("logo: ").append(logo).append('\n');
        }
        if (name != null) {
            s.append("name: ").append(name).append('\n');
        }
        if (native_name != null) {
            s.append("native_name: ").append(native_name).append('\n');
        }
        if (address != null) {
            s.append("address: ").append(address).append('\n');
        }
        if (zipcode != null) {
            s.append("zipcode: ").append(zipcode).append('\n');
        }
        if (city != null) {
            s.append("city: ").append(city).append('\n');
        }
        if (state != null) {
            s.append("state: ").append(state).append('\n');
        }
        if (country != null) {
            s.append("country: ").append(country).append('\n');
        }
        if (email != null) {
            s.append("email: ").append(email).append('\n');
        }
        if (phone != null) {
            s.append("phone: ").append(phone).append('\n');
        }
        if (fax != null) {
            s.append("fax: ").append(fax).append('\n');
        }
        if (website != null) {
            s.append("website: ").append(website).append('\n');
        }
        if (description_edmerp != null) {
            s.append("desc edmerp: ").append(description_edmerp).append('\n');
        }
        if (description != null) {
            s.append("description: ").append(description).append('\n');
        }
        if (c_country != null) {
            s.append("c_country: ").append(c_country).append('\n');
        }
        if (n_collate_id != null) {
            s.append("n_collate_id: ").append(n_collate_id).append('\n');
        }
        if (centre_id_edmed != null) {
            s.append("centre_id_edmed: ").append(centre_id_edmed).append('\n');
        }
        if (centre_id_edmerp != null) {
            s.append("centre_id_edmerp: ").append(centre_id_edmerp).append('\n');
        }
        if (centre_id_edios != null) {
            s.append("centre_id_edios: ").append(centre_id_edios).append('\n');
        }
        if (centre_id_cdi != null) {
            s.append("centre_id_cdi: ").append(centre_id_cdi).append('\n');
        }
        if (centre_id_simorc != null) {
            s.append("centre_id_simorc: ").append(centre_id_simorc).append('\n');
        }
        if (centre_id_csr != null) {
            s.append("centre_id_csr: ").append(centre_id_csr).append('\n');
        }
        if (owner_old != null) {
            s.append("owner_old: ").append(owner_old).append('\n');
        }
        if (collate_id != null) {
            s.append("collate_id: ").append(collate_id).append('\n');
        }
        if (national_collator != null) {
            s.append("national_collator: ").append(national_collator).append('\n');
        }
        return s.toString();
    }
}
