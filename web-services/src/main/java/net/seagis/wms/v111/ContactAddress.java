package net.seagis.wms.v111;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * 
 *  @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "addressType",
    "address",
    "city",
    "stateOrProvince",
    "postCode",
    "country"
})
@XmlRootElement(name = "ContactAddress")
public class ContactAddress {

    @XmlElement(name = "AddressType", required = true)
    private String addressType;
    @XmlElement(name = "Address", required = true)
    private String address;
    @XmlElement(name = "City", required = true)
    private String city;
    @XmlElement(name = "StateOrProvince", required = true)
    private String stateOrProvince;
    @XmlElement(name = "PostCode", required = true)
    private String postCode;
    @XmlElement(name = "Country", required = true)
    private String country;

    /**
     * An empty constructor used by JAXB.
     */
     ContactAddress() {
     }

    /**
     * Build a new Contact adress object
     */
    public ContactAddress(final String addressType, final String address, final String city,
            final String stateOrProvince, final String postCode, final String country) {
        this.address         = address;
        this.addressType     = addressType;
        this.city            = city;
        this.country         = country;
        this.postCode        = postCode;
        this.stateOrProvince = stateOrProvince;
    }
    
    /**
     * Gets the value of the addressType property.
     */
    public String getAddressType() {
        return addressType;
    }

    /**
     * Gets the value of the address property.
     */
    public String getAddress() {
        return address;
    }

   /**
     * Gets the value of the city property.
     * 
     */
    public String getCity() {
        return city;
    }

    /**
     * Gets the value of the stateOrProvince property.
     */
    public String getStateOrProvince() {
        return stateOrProvince;
    }

    /**
     * Gets the value of the postCode property.
     * 
     */
    public String getPostCode() {
        return postCode;
    }

    /**
     * Gets the value of the country property.
     * 
     */
    public String getCountry() {
        return country;
    }
}
