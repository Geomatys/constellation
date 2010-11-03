/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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
package org.constellation.bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.servlet.ServletContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.StyleProviderProxy;
import org.constellation.util.UserData;
import org.geotoolkit.gml.xml.v311.CodeListType;
import org.geotoolkit.ows.xml.AbstractAddress;
import org.geotoolkit.ows.xml.AbstractCapabilitiesBase;
import org.geotoolkit.ows.xml.AbstractContact;
import org.geotoolkit.ows.xml.AbstractKeywords;
import org.geotoolkit.ows.xml.AbstractResponsiblePartySubset;
import org.geotoolkit.ows.xml.AbstractServiceIdentification;
import org.geotoolkit.ows.xml.AbstractServiceProvider;
import org.geotoolkit.ows.xml.AbstractTelephone;
import org.geotoolkit.ows.xml.v110.AddressType;
import org.geotoolkit.ows.xml.v110.CapabilitiesBaseType;
import org.geotoolkit.ows.xml.v110.CodeType;
import org.geotoolkit.ows.xml.v110.ContactType;
import org.geotoolkit.ows.xml.v110.KeywordsType;
import org.geotoolkit.ows.xml.v110.LanguageStringType;
import org.geotoolkit.ows.xml.v110.OnlineResourceType;
import org.geotoolkit.ows.xml.v110.ResponsiblePartySubsetType;
import org.geotoolkit.ows.xml.v110.ServiceIdentification;
import org.geotoolkit.ows.xml.v110.ServiceProvider;
import org.geotoolkit.ows.xml.v110.TelephoneType;
import org.geotoolkit.wcs.xml.v100.Keywords;
import org.geotoolkit.wcs.xml.v100.MetadataLinkType;
import org.geotoolkit.wcs.xml.v100.ResponsiblePartyType;
import org.geotoolkit.wcs.xml.v100.ServiceType;
import org.geotoolkit.wcs.xml.v100.WCSCapabilitiesType;
import org.geotoolkit.wcs.xml.v111.Capabilities;
import org.geotoolkit.wms.xml.AbstractService;
import org.geotoolkit.wms.xml.v111.WMT_MS_Capabilities;
import org.geotoolkit.wms.xml.v130.ContactAddress;
import org.geotoolkit.wms.xml.v130.ContactInformation;
import org.geotoolkit.wms.xml.v130.ContactPersonPrimary;
import org.geotoolkit.wms.xml.v130.Keyword;
import org.geotoolkit.wms.xml.v130.KeywordList;
import org.geotoolkit.wms.xml.v130.OnlineResource;
import org.geotoolkit.wms.xml.v130.Service;
import org.geotoolkit.wms.xml.v130.WMSCapabilities;
import org.geotoolkit.xml.MarshallerPool;
import org.opengis.feature.type.Name;

/**
 *
 * @author Guilhem Legal
 * @author Medhi Sidhoum
 */
public final class ServicesBean {

    private static final String WCS = "WCS";
    private static final String WMS = "WMS";
    private static final String SOS = "SOS";
    private static final String CSW = "CSW";
    private static final String FILLFORM = "fillForm";
    /**
     * The service Identification title
     */
    private String title;
    /**
     * The service Identification description
     */
    private String abstractDescription;
    /**
     * The  service Identification List of keywords 
     */
    private List<SelectItem> keywords;
    /**
     * The service Identification type
     */
    private String serviceType;
    /**
     * The  service Identification List of implemented versions. 
     */
    private List<SelectItem> versions;
    /**
     * The service Identification fees
     */
    private String fees;
    /**
     * The service Identification access constraints
     */
    private String accessConstraints;
    /**
     * The service Provider name
     */
    private String providerName;
    /**
     * The service Provider site
     */
    private String providerSite;
    /**
     * The service Provider Contact individual name
     */
    private String individualName;
    /**
     * The service Provider Contact position name
     */
    private String positionName;
    /**
     * The service Provider Contact phone number (voice)
     */
    private String phoneVoice;
    /**
     * The service Provider Contact phone number (facsimile)
     */
    private String phoneFacsimile;
    /**
     * The service Provider Contact delivery point.
     */
    private String deliveryPoint;
    /**
     * The service Provider City
     */
    private String city;
    /**
     * The service Provider administrative area
     */
    private String administrativeArea;
    /**
     * The service Provider Postal Code
     */
    private String postalCode;
    /**
     * The service Provider Country
     */
    private String country;
    /**
     * The service Provider Electronic address
     */
    private String electronicAddress;
    /**
     * The service Provider Role
     */
    private String role;
    /**
     * Specifics term for WMS
     */
    private String addressType;
    private int layerLimit;
    private int maxWidth;
    private int maxHeight;
    /**
     * The capabilities object to update.
     */
    private Object[] capabilities;
    /**
     * The current capabilities file to update
     */
    private File[] capabilitiesFile;
    /**
     * The marshaller/unmarshaller pool to store the updates/ read the bases capabilities files.
     */
    private MarshallerPool marshallerPool;
    /**
     * A servlet context allowing to find the path to deployed file.
     */
    private ServletContext servletContext;
    /**
     * This is an attribute that defines the current selected web service mode.
     * Default is WMS.
     */
    private String webServiceMode = WMS;
    /**
     * 
     * This is the available web services list for the selectOneListbox component.
     */
    private List webServices = new ArrayList();
    /**
     * This object record the user data.
     */
    private UserData userData;
    /**
     * The uploaded File.
     */
    //  private UploadedFile uploadedFile;
    private String urlPreference = "";
    private boolean existPrefrence = false;
    private SERVICESERROR layerProviderError;
    private SERVICESERROR styleProviderError;

    private static enum SERVICESERROR {

        LAYERPROVIDERERROR,
        STYLEPROVIDERERROR,
        NOERROR
    }
    /**
     * Debugging purpose
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.bean");

    public ServicesBean() throws JAXBException, FileNotFoundException {

        userData = new UserData();

        // we get the sevlet context to read the capabilities files in the deployed war
        final FacesContext context = FacesContext.getCurrentInstance();
        servletContext = (ServletContext) context.getExternalContext().getContext();

        //adding items into the webServices list.
        addWebServices();


        //we create the JAXBContext and read the selected file
        marshallerPool = new MarshallerPool(Capabilities.class,
                WMSCapabilities.class,
                WMT_MS_Capabilities.class,
                WCSCapabilitiesType.class,
                org.geotoolkit.csw.xml.v202.Capabilities.class,
                UserData.class,
                org.geotoolkit.sos.xml.v100.Capabilities.class,
                org.geotoolkit.internal.jaxb.geometry.ObjectFactory.class);
    }

    /**
     * fill The formular with OWS 1.1.0 Object
     */
    private void fillFormFromOWS(AbstractCapabilitiesBase cap) {

        //we fill the default value of Service Identification
        final AbstractServiceIdentification si = cap.getServiceIdentification();
        this.title = si.getFirstTitle();
        this.abstractDescription = si.getFirstAbstract();
        if (si.getKeywords().size() > 0) {
            this.keywords = keywordsToSelectItem(si.getKeywords().get(0));
        }
        this.serviceType = si.getServiceType().getValue();
        this.versions = stringToSelectItem(si.getServiceTypeVersion());
        this.fees = si.getFees();
        if (si.getAccessConstraints().size() > 0) {
            this.accessConstraints = si.getAccessConstraints().get(0);
        }

        //we fill the value of ServiceProvider
        final AbstractServiceProvider sp = cap.getServiceProvider();
        final AbstractResponsiblePartySubset sc = sp.getServiceContact();
        final AbstractContact ci = sc.getContactInfo();
        final AbstractTelephone t = ci.getPhone();
        final AbstractAddress a = ci.getAddress();
        this.providerName = sp.getProviderName();
        this.providerSite = sp.getProviderSite().getHref();
        this.individualName = sc.getIndividualName();
        this.positionName = sc.getPositionName();

        // Phone party
        if (t.getVoice().size() > 0) {
            this.phoneVoice = t.getVoice().get(0);
        }
        if (t.getFacsimile().size() > 0) {
            this.phoneFacsimile = t.getFacsimile().get(0);
        }

        //address party
        if (a.getDeliveryPoint().size() > 0) {
            this.deliveryPoint = a.getDeliveryPoint().get(0);
        }
        this.city = a.getCity();
        this.administrativeArea = a.getAdministrativeArea();
        this.postalCode = a.getPostalCode();
        this.country = a.getCountry();
        if (a.getElectronicMailAddress().size() > 0) {
            this.electronicAddress = a.getElectronicMailAddress().get(0);
        }
        if (sc.getRole() != null) {
            this.role = sc.getRole().getValue();
        }
    }

    /**
     * fill The formular with WMS 1.3.0 Object
     */
    private void fillFormFromWMS(WMSCapabilities cap) {

        //we fill the default value of Service Identification
        final Service s = cap.getService();
        this.title = s.getTitle();
        this.abstractDescription = s.getAbstract();
        final KeywordList klist = s.getKeywordList();
        if (klist != null) {
            this.keywords = keywordsToSelectItem(klist);
        }

        this.versions = new ArrayList<SelectItem>();
        this.versions.add(new SelectItem("1.3.0"));
        this.versions.add(new SelectItem("1.1.1"));

        this.fees = s.getFees();
        this.accessConstraints = s.getAccessConstraints();

        //we fill the value of ServiceProvider
        final ContactInformation ci = s.getContactInformation();
        final ContactAddress a = ci.getContactAddress();
        final ContactPersonPrimary cpp = ci.getContactPersonPrimary();
        this.providerName = cpp.getContactOrganization();
        this.providerSite = s.getOnlineResource().getHref();
        this.individualName = cpp.getContactPerson();
        this.positionName = ci.getContactPosition();

        // Phone party
        this.phoneVoice = ci.getContactVoiceTelephone();
        this.phoneFacsimile = ci.getContactFacsimileTelephone();

        //address party
        this.deliveryPoint = a.getAddress();
        this.city = a.getCity();
        this.administrativeArea = a.getStateOrProvince();
        this.postalCode = a.getPostCode();
        this.country = a.getCountry();
        this.electronicAddress = ci.getContactElectronicMailAddress();

        /*
         * The extras attribute for WMS 
         */
        this.addressType = a.getAddressType();
        this.layerLimit = s.getLayerLimit();
        this.maxHeight = s.getMaxHeight();
        this.maxWidth = s.getMaxWidth();
    }

    /**
     * Transform a list of String in a Keyword Object into a list of SelectItem.
     * 
     * @param keywords
     * @return
     */
    private List<SelectItem> keywordsToSelectItem(AbstractKeywords keywords) {
        final List<SelectItem> results = new ArrayList<SelectItem>();

        for (String keyword : keywords.getKeywordList()) {
            results.add(new SelectItem(keyword));
        }

        return results;
    }

    /**
     * Transform a list of String in a Keyword Object into a list of SelectItem.
     * 
     * @param keywords
     * @return
     */
    private List<SelectItem> keywordsToSelectItem(KeywordList keywords) {
        final List<SelectItem> results = new ArrayList<SelectItem>();

        for (Keyword keyword : keywords.getKeyword()) {
            results.add(new SelectItem(keyword.getValue()));
        }

        return results;
    }

    /**
     * Transform a list of string into a list of SelectItem
     * @param list
     * @return
     */
    private List<SelectItem> stringToSelectItem(List<String> list) {
        final List<SelectItem> results = new ArrayList<SelectItem>();

        for (String item : list) {
            results.add(new SelectItem(item));
        }

        return results;
    }

    /**
     * Store the formular in the XML file
     */
    public String storeForm() throws JAXBException, IOException, FileNotFoundException {

        //we signal to the webService to update is capabilities
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            final File f = new File(servletContext.getRealPath("WEB-INF/change.properties"));
            final Properties p = new Properties();
            in = new FileInputStream(f);
            p.load(in);
            in.close();
            p.put("update", "true");
            out = new FileOutputStream(f);
            p.store(out, "updated from JSF interface");
            out.close();
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
        for (Object capa : capabilities) {

            //for OWS 1.1.0
            if (capa instanceof org.geotoolkit.ows.xml.v110.CapabilitiesBaseType) {
                final ServiceIdentification si = getServiceIdentification110();
                final ServiceProvider sp = getServiceProvider110();
                ((CapabilitiesBaseType) capa).setServiceProvider(sp);
                ((CapabilitiesBaseType) capa).setServiceIdentification(si);

                // for OWS 1.0.0
            } else if (capa instanceof org.geotoolkit.ows.xml.v100.CapabilitiesBaseType) {
                final org.geotoolkit.ows.xml.v100.ServiceIdentification si = getServiceIdentification100();
                final org.geotoolkit.ows.xml.v100.ServiceProvider sp = getServiceProvider100();
                ((org.geotoolkit.ows.xml.v100.CapabilitiesBaseType) capa).setServiceProvider(sp);
                ((org.geotoolkit.ows.xml.v100.CapabilitiesBaseType) capa).setServiceIdentification(si);


                // for WCS 1.0.0
            } else if (capa instanceof WCSCapabilitiesType) {
                final ServiceType s = getWCSService();
                ((WCSCapabilitiesType) capa).setService(s);

                // for WMS 1.3.0/1.1.1
            } else if (capa instanceof WMSCapabilities || capa instanceof WMT_MS_Capabilities) {
                final List<AbstractService> s = getWMSService();

                // 1.3.0
                if (capa instanceof WMSCapabilities) {
                    ((WMSCapabilities) capa).setService(s.get(0));
                    LOGGER.info("update WMS version 1.3.0");

                    // 1.1.1
                } else {
                    ((WMT_MS_Capabilities) capa).setService(s.get(1));
                    LOGGER.info("update WMS version 1.1.1");
                }
            }
        }
        storeCapabilitiesFile();
        return "goBack";
    }

    /**
     * Build the Service Identification object of an OWS 1.1 service.
     */
    public ServiceIdentification getServiceIdentification110() {

        final List<LanguageStringType> listKey = new ArrayList<LanguageStringType>();
        for (SelectItem k : keywords) {
            listKey.add(new LanguageStringType((String) k.getValue()));
        }

        final List<String> listVers = new ArrayList<String>();
        for (SelectItem v : versions) {
            listVers.add((String) v.getValue());
        }

        return new ServiceIdentification(new LanguageStringType(title),
                new LanguageStringType(abstractDescription),
                new KeywordsType(listKey, null),
                new CodeType(serviceType),
                listVers,
                fees,
                accessConstraints);
    }

    /**
     * Build the Service Identification object of an OWS 1.0 service.
     */
    public org.geotoolkit.ows.xml.v100.ServiceIdentification getServiceIdentification100() {

        final List<String> listKey = new ArrayList<String>();
        for (SelectItem k : keywords) {
            listKey.add((String) k.getValue());
        }

        final List<String> listVers = new ArrayList<String>();
        for (SelectItem v : versions) {
            listVers.add((String) v.getValue());
        }

        return new org.geotoolkit.ows.xml.v100.ServiceIdentification(title,
                abstractDescription,
                new org.geotoolkit.ows.xml.v100.KeywordsType(listKey, null),
                new org.geotoolkit.ows.xml.v100.CodeType(serviceType),
                listVers,
                fees,
                accessConstraints);
    }

    /**
     * Build a service object for a  WCS 1.0.0 service.
     */
    private ServiceType getWCSService() {

        final List<MetadataLinkType> links = new ArrayList<MetadataLinkType>();
        links.add(new MetadataLinkType(providerSite));

        final List<String> listKey = new ArrayList<String>();
        for (SelectItem k : keywords) {
            listKey.add((String) k.getValue());
        }

        final org.geotoolkit.wcs.xml.v100.TelephoneType tel = new org.geotoolkit.wcs.xml.v100.TelephoneType(phoneVoice, phoneFacsimile);
        final org.geotoolkit.wcs.xml.v100.AddressType adr = new org.geotoolkit.wcs.xml.v100.AddressType(deliveryPoint,
                city,
                administrativeArea,
                postalCode,
                country,
                electronicAddress);

        final org.geotoolkit.wcs.xml.v100.ContactType ci = new org.geotoolkit.wcs.xml.v100.ContactType(tel, adr, null);

        final ResponsiblePartyType resp = new ResponsiblePartyType(individualName,
                positionName,
                providerName,
                ci);

        return new ServiceType(links,
                title,
                title,
                abstractDescription,
                new Keywords(listKey),
                resp,
                new CodeListType(fees),
                new CodeListType(accessConstraints),
                null);
    }

    /**
     * Build a service object for a  WMS 1.3.0/1.1.1 Service.
     */
    private List<AbstractService> getWMSService() {

        final List<AbstractService> result = new ArrayList<AbstractService>();

        // v1.3.0
        final List<Keyword> listKey = new ArrayList<Keyword>();
        for (SelectItem k : keywords) {
            listKey.add(new Keyword((String) k.getValue()));
        }
        final KeywordList keywordList = new KeywordList(listKey);
        final ContactPersonPrimary cpp = new ContactPersonPrimary(individualName, providerName);
        final ContactAddress ca = new ContactAddress(getAddressType(), deliveryPoint, city, administrativeArea,
                postalCode, country);

        final ContactInformation ci = new ContactInformation(cpp, positionName,
                ca, phoneVoice, phoneFacsimile, electronicAddress);

        final Service service130 = new Service(title, title, abstractDescription,
                keywordList,
                new OnlineResource(providerSite),
                ci, fees, accessConstraints, getLayerLimit(),
                getMaxWidth(), getMaxHeight());
        result.add(service130);

        // v1.1.1
        final List<org.geotoolkit.wms.xml.v111.Keyword> listKey111 = new ArrayList<org.geotoolkit.wms.xml.v111.Keyword>();
        for (SelectItem k : keywords) {
            listKey111.add(new org.geotoolkit.wms.xml.v111.Keyword((String) k.getValue()));
        }
        final org.geotoolkit.wms.xml.v111.KeywordList keywordList111 = new org.geotoolkit.wms.xml.v111.KeywordList(listKey111);
        final org.geotoolkit.wms.xml.v111.ContactPersonPrimary cpp111 = new org.geotoolkit.wms.xml.v111.ContactPersonPrimary(individualName, providerName);
        final org.geotoolkit.wms.xml.v111.ContactAddress ca111 = new org.geotoolkit.wms.xml.v111.ContactAddress(
                getAddressType(), deliveryPoint, city, administrativeArea, postalCode, country);

        final org.geotoolkit.wms.xml.v111.ContactInformation ci111 = new org.geotoolkit.wms.xml.v111.ContactInformation(cpp111, positionName,
                ca111, phoneVoice, phoneFacsimile, electronicAddress);

        final org.geotoolkit.wms.xml.v111.Service service111 = new org.geotoolkit.wms.xml.v111.Service(
                title, title, abstractDescription,
                keywordList111,
                new org.geotoolkit.wms.xml.v111.OnlineResource(providerSite),
                ci111, fees, accessConstraints);
        result.add(service111);

        return result;
    }

    /**
     * Build the Service Provider object of an OWS 1.1 service.
     */
    public ServiceProvider getServiceProvider110() {

        final AddressType adr = new AddressType(deliveryPoint,
                city,
                administrativeArea,
                postalCode,
                country,
                electronicAddress);

        final ContactType ci = new ContactType(new TelephoneType(phoneVoice, phoneFacsimile),
                adr,
                null, null, null);

        final ResponsiblePartySubsetType sc = new ResponsiblePartySubsetType(individualName,
                positionName,
                ci,
                new CodeType(role));

        final ServiceProvider sp = new ServiceProvider(providerName,
                new OnlineResourceType(providerSite),
                sc);
        return sp;
    }

    /**
     * Build the Service Provider object of an OWS 1.0 service.
     */
    public org.geotoolkit.ows.xml.v100.ServiceProvider getServiceProvider100() {

        final org.geotoolkit.ows.xml.v100.AddressType adr = new org.geotoolkit.ows.xml.v100.AddressType(deliveryPoint,
                city,
                administrativeArea,
                postalCode,
                country,
                electronicAddress);

        final org.geotoolkit.ows.xml.v100.ContactType ci = new org.geotoolkit.ows.xml.v100.ContactType(new org.geotoolkit.ows.xml.v100.TelephoneType(phoneVoice, phoneFacsimile),
                adr,
                null, null, null);

        final org.geotoolkit.ows.xml.v100.ResponsiblePartySubsetType sc = new org.geotoolkit.ows.xml.v100.ResponsiblePartySubsetType(
                individualName,
                positionName,
                ci,
                new org.geotoolkit.ows.xml.v100.CodeType(role));

        return new org.geotoolkit.ows.xml.v100.ServiceProvider(
                providerName,
                new org.geotoolkit.ows.xml.v100.OnlineResourceType(providerSite),
                sc);
    }

    /**
     * Update the capabilities XML file of the webService and store it in a UserData Object.
     * 
     * @throws javax.xml.bind.JAXBException
     */
    private void storeCapabilitiesFile() throws JAXBException {
        try {
            final Marshaller marshaller = marshallerPool.acquireMarshaller();
            int i = 0;
            for (File f : capabilitiesFile) {
                final OutputStream out = new FileOutputStream(f);
                marshaller.marshal(capabilities[i], out);
                out.close();
                LOGGER.log(Level.INFO, "store {0}", f.getAbsolutePath());
                i++;
            }
            marshallerPool.release(marshaller);

            if (webServiceMode.equals(WMS)) {
                userData.setWMSCapabilities(capabilities);

            } else if (webServiceMode.equals(WCS)) {
                userData.setWCSCapabilities(capabilities);

            } else if (webServiceMode.equals(SOS)) {
                userData.setSOSCapabilities(capabilities);

            } else if (webServiceMode.equals(CSW)) {
                userData.setCSWCapabilities(capabilities);

            }
        } catch (IOException ex) {
            LOGGER.severe("IO Exception while storing capabilities file");
        }
    }

    /**
     * Load the user data from an uploaded File
     * 
     * @param f the uploaded file.
     */
    private void loadUserData(File f) throws FileNotFoundException, IOException {
        try {
            if (f != null) {
                final Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
                userData = (UserData) unmarshaller.unmarshal(f);
                marshallerPool.release(unmarshaller);
            } else {
                LOGGER.severe("File uploaded null");
                return;
            }

            final Marshaller marshaller = marshallerPool.acquireMarshaller();
            // we extract and update WMS user data
            if (userData.getWMSCapabilities() != null) {

                if (userData.getWMSCapabilities().length == 2) {

                    //we begin to write the high lvl document
                    String path = servletContext.getRealPath("WEB-INF/WMSCapabilities1.3.0.xml");
                    File file = new File(path);
                    if (file.exists()) {

                        marshaller.marshal(userData.getWMSCapabilities()[0], (OutputStream) new FileOutputStream(file));

                    } else {
                        LOGGER.log(Level.WARNING, "WMS capabilities file version 1.3.0 not found at :{0}. unable to load WMS Data", path);
                    }

                    // the we add to the list of object to update the other sub version
                    path = servletContext.getRealPath("WEB-INF/WMSCapabilities1.1.1.xml");
                    file = new File(path);
                    if (file.exists()) {

                        marshaller.marshal(userData.getWMSCapabilities()[1], (OutputStream) new FileOutputStream(file));

                    } else {
                        LOGGER.log(Level.WARNING, "WMS capabilities file version 1.1.1 not found at :{0}. unable to load WMS Data", path);
                    }
                } else {
                    // TODO afficher fichier non valide
                    LOGGER.severe("WMS capabilie file uncomplete (!=2)");
                }
            }

            // we extract and update WCS user data
            if (userData.getWCSCapabilities() != null) {

                if (userData.getWCSCapabilities().length == 2) {

                    //we begin to write the high lvl document
                    String path = servletContext.getRealPath("WEB-INF/WCSCapabilities1.1.1.xml");
                    File file = new File(path);
                    if (file.exists()) {

                        marshaller.marshal(userData.getWCSCapabilities()[0], (OutputStream) new FileOutputStream(file));

                    } else {
                        LOGGER.log(Level.WARNING, "WCS capabilities file version 1.1.1 not found at :{0}. unable to load WCS Data", path);
                    }

                    // the we add to the list of object to update the other sub version
                    path = servletContext.getRealPath("WEB-INF/WCSCapabilities1.0.0.xml");
                    file = new File(path);
                    if (file.exists()) {

                        marshaller.marshal(userData.getWCSCapabilities()[1], (OutputStream) new FileOutputStream(file));

                    } else {
                        LOGGER.log(Level.WARNING, "WCS capabilities file version 1.0.0 not found at :{0}. unable to load WCS Data", path);
                    }
                } else {
                    // TODO afficher fichier non valide
                    LOGGER.severe("WCS capabilies file uncomplete (!=2)");
                }
            }

            // we extract and update CSW user data
            if (userData.getCSWCapabilities() != null) {

                if (userData.getCSWCapabilities().length == 1) {

                    //we begin to write the high lvl document
                    final String path = servletContext.getRealPath("WEB-INF/CSWCapabilities2.0.2.xml");
                    final File file = new File(path);
                    if (file.exists()) {

                        marshaller.marshal(userData.getCSWCapabilities()[0], (OutputStream) new FileOutputStream(file));

                    } else {
                        LOGGER.log(Level.WARNING, "CSW capabilities file version 2.0.2 not found at :{0}. unable to load CSW Data", path);
                    }

                } else {
                    // TODO afficher fichier non valide
                    LOGGER.severe("WCS capabilies file uncomplete (!=1)");
                }
            }

            // we extract and update SOS user data
            if (userData.getSOSCapabilities() != null) {

                if (userData.getSOSCapabilities().length == 1) {

                    //we begin to write the high lvl document
                    final String path = servletContext.getRealPath("WEB-INF/SOSCapabilities1.0.0.xml");
                    final File file = new File(path);
                    if (file.exists()) {

                        marshaller.marshal(userData.getSOSCapabilities()[0], (OutputStream) new FileOutputStream(file));

                    } else {
                        LOGGER.log(Level.WARNING, "SOS capabilities file version 1.0.0 not found at :{0}. unable to load SOS Data", path);
                    }

                } else {
                    // TODO afficher fichier non valide
                    LOGGER.severe("SOS capabilies file uncomplete (!=1)");
                }
            }

            //we signal to the webService to update is capabilities
            FileInputStream in = null;
            FileOutputStream out = null;
            try {
                final File change = new File(servletContext.getRealPath("WEB-INF/change.properties"));
                final Properties p = new Properties();
                in = new FileInputStream(change);
                p.load(in);

                p.put("update", "true");
                out = new FileOutputStream(change);
                p.store(out, "updated from JSF interface");
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }

            marshallerPool.release(marshaller);

        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            //TODO afficher quelquechose si le fichier n'est pas valide
        }
    }

    public void storeData() throws JAXBException {
        final String url = servletContext.getRealPath("preference.sml");
        setUrlPreference("preference.sml");
        final File f = new File(url);
        //f.setWritable(true);
        setExistPrefrence(true);
        final Marshaller marshaller = marshallerPool.acquireMarshaller();
        marshaller.marshal(userData, f);
        marshallerPool.release(marshaller);
    }

    public String setWMSMode() throws JAXBException, FileNotFoundException {
        LOGGER.info("set WMS mode");
        webServiceMode = WMS;
        capabilities = new Object[2];
        capabilitiesFile = new File[2];
        final Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();

        //we begin to read the high lvl document
        String path = servletContext.getRealPath("WEB-INF/WMSCapabilities1.3.0.xml");
        capabilitiesFile[0] = new File(path);
        if (capabilitiesFile[0].exists()) {

            capabilities[0] = unmarshaller.unmarshal(new FileReader(capabilitiesFile[0]));
            fillFormFromWMS((WMSCapabilities) capabilities[0]);

        } else {
            LOGGER.log(Level.WARNING, "WMS capabilities file version 1.3.0 not found at :{0}", path);
        }

        // the we add to the list of object to update the other sub version
        path = servletContext.getRealPath("WEB-INF/WMSCapabilities1.1.1.xml");
        capabilitiesFile[1] = new File(path);
        if (capabilitiesFile[1].exists()) {

            capabilities[1] = unmarshaller.unmarshal(new FileReader(capabilitiesFile[1]));

        } else {
            LOGGER.log(Level.WARNING, "WMS capabilities file version 1.1.1 not found at :{0}", path);
        }
        marshallerPool.release(unmarshaller);

        return FILLFORM;

    }

    public String setWCSMode() throws FileNotFoundException, JAXBException {

        webServiceMode = WCS;
        capabilities = new Object[2];
        capabilitiesFile = new File[2];
        final Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();

        //we begin to read the high lvl document
        String path = servletContext.getRealPath("WEB-INF/WCSCapabilities1.1.1.xml");
        capabilitiesFile[0] = new File(path);
        if (capabilitiesFile[0].exists()) {

            capabilities[0] = unmarshaller.unmarshal(new FileReader(capabilitiesFile[0]));
            fillFormFromOWS((Capabilities) capabilities[0]);

        } else {
            LOGGER.log(Level.WARNING, "WCS capabilities file version 1.1.1 not found at :{0}", path);
        }

        // the we add to the list of object to update the other sub version
        path = servletContext.getRealPath("WEB-INF/WCSCapabilities1.0.0.xml");
        capabilitiesFile[1] = new File(path);
        if (capabilitiesFile[1].exists()) {

            capabilities[1] = unmarshaller.unmarshal(new FileReader(capabilitiesFile[1]));

        } else {
            LOGGER.log(Level.WARNING, "WCS capabilities file version 1.0.0 not found at :{0}", path);
        }
        marshallerPool.release(unmarshaller);

        return FILLFORM;

    }

    public String setSOSMode() throws FileNotFoundException, JAXBException {

        webServiceMode = SOS;
        capabilities = new Object[1];
        capabilitiesFile = new File[1];

        //we begin to read the high lvl document
        final String path = servletContext.getRealPath("WEB-INF/SOSCapabilities1.0.0.xml");
        capabilitiesFile[0] = new File(path);
        if (capabilitiesFile[0].exists()) {
            final Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
            capabilities[0] = unmarshaller.unmarshal(new FileReader(capabilitiesFile[0]));
            fillFormFromOWS((CapabilitiesBaseType) capabilities[0]);
            marshallerPool.release(unmarshaller);

        } else {
            LOGGER.log(Level.WARNING, "SOS capabilities file version 1.0.0 not found at :{0}", path);
        }

        return FILLFORM;
    }

    public String setCSWMode() throws FileNotFoundException, JAXBException {

        webServiceMode = CSW;
        capabilities = new Object[1];
        capabilitiesFile = new File[1];

        //we begin to read the high lvl document
        final String path = servletContext.getRealPath("WEB-INF/CSWCapabilities2.0.2.xml");
        capabilitiesFile[0] = new File(path);
        if (capabilitiesFile[0].exists()) {
            final Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
            capabilities[0] = unmarshaller.unmarshal(new FileReader(capabilitiesFile[0]));
            fillFormFromOWS((org.geotoolkit.ows.xml.v100.CapabilitiesBaseType) capabilities[0]);
            marshallerPool.release(unmarshaller);

        } else {
            LOGGER.log(Level.WARNING, "CSW capabilities file version 2.0.2 not found at :{0}", path);
        }

        return FILLFORM;
    }

    /**
     * this method switch to the appropriate mode and returns the outcome string to proceed the jsf navigation.
     * @return
     * @throws java.io.FileNotFoundException
     * @throws javax.xml.bind.JAXBException
     */
    public String switchMode() throws FileNotFoundException, JAXBException {
        if (webServiceMode.equals(WMS)) {
            setWMSMode();
        } else if (webServiceMode.equals(WCS)) {
            setWCSMode();
        } else if (webServiceMode.equals(SOS)) {
            setSOSMode();
        } else if (webServiceMode.equals(CSW)) {
            setCSWMode();
        }
        return FILLFORM;
    }

    /**
     * 
     * This method proceed to validate phase for the selectOneListbox component.
     * @param context
     * @param component
     * @param value
     * @throws ValidatorException
     */
    public void validateWebService(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (!(value instanceof String)) {
            throw new ValidatorException(new FacesMessage("A Validation error was found ! the selected item in selectOneListbox is not a string !!"));
        }
    }

    /**
     * this method adds the available web services into the jsf component.
     */
    public void addWebServices() {
        if (existsCapabilities(CSW)) {
            webServices.add(new SelectItem(CSW, "CSW metadata", null));
        }
        if (existsCapabilities(SOS)) {
            webServices.add(new SelectItem(SOS, "SOS metadata", null));
        }
        if (existsCapabilities(WCS)) {
            webServices.add(new SelectItem(WCS, "WCS metadata", null));
        }
        if (existsCapabilities(WMS)) {
            webServices.add(new SelectItem(WMS, "WMS metadata", null));
        }
    }

    public boolean existsCapabilities(String ws) {
        boolean exist = false;
        final File file;
        final String path;
        if (ws.equals(CSW)) {
            path = servletContext.getRealPath("WEB-INF/CSWCapabilities2.0.2.xml");
            file = new File(path);
            exist = file.exists();
        } else if (ws.equals(SOS)) {
            path = servletContext.getRealPath("WEB-INF/SOSCapabilities1.0.0.xml");
            file = new File(path);
            exist = file.exists();
        } else if (ws.equals(WCS)) {
            path = servletContext.getRealPath("WEB-INF/WCSCapabilities1.0.0.xml");
            file = new File(path);
            exist = file.exists();
        } else if (ws.equals(WMS)) {
            path = servletContext.getRealPath("WEB-INF/WMSCapabilities1.3.0.xml");
            file = new File(path);
            exist = file.exists();
        }
        return exist;
    }

    /**
     * This method proceed to upload and get informations about the uploaded file.
     * @return the content string of the uploaded file
     * @throws java.io.IOException
     */
    public File processSubmitedFile() throws IOException {
        upload();
        final File f = File.createTempFile("userData", "geomatys");
        InputStream inputStream = null;
        InputStreamReader infile = null;
        FileWriter writer = null;
        try {

            //   inputStream                = uploadedFile.getInputStream();
            infile = new InputStreamReader(inputStream);
            final BufferedReader inbuf = new BufferedReader(infile);
            writer = new FileWriter(f);

            String line;
            while ((line = inbuf.readLine()) != null) {
                writer.append(line);
                writer.append('\n');
            }

        } catch (Exception x) {
            final FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_FATAL,
                    x.getClass().getName(), x.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message);
            LOGGER.log(Level.WARNING, "Exception in proccesSubmitFile {0}", x.getMessage());
            return null;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (infile != null) {
                infile.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
        return f;
    }

    public String doUpload() throws IOException {
        final File f = processSubmitedFile();
        if (f == null) {
            LOGGER.severe("[doUpload]process uploaded file null");
        }
        loadUserData(f);
        return "ok";
    }

    /**
     * this method put in the application map some parameters.
     * @throws java.io.IOException
     */
    public void upload() throws IOException {
        /* final FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.getExternalContext().getApplicationMap().put("fileupload_bytes", uploadedFile.getBytes());
        facesContext.getExternalContext().getApplicationMap().put("fileupload_type", uploadedFile.getContentType());
        facesContext.getExternalContext().getApplicationMap().put("fileupload_name", uploadedFile.getName()); */
    }

    /**
     * this method return a flag that indicates if a file was upladed.
     * 
     * @return boolean
     */
    public boolean isUploaded() {
        final FacesContext facesContext = FacesContext.getCurrentInstance();
        return facesContext.getExternalContext().getApplicationMap().get("fileupload_bytes") != null;
    }

    public String goBack() {
        return "goBack";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstract() {
        return abstractDescription;
    }

    public void setAbstract(String abstractt) {
        this.abstractDescription = abstractt;
    }

    public List<SelectItem> getKeywords() {
        if (keywords == null) {
            keywords = new ArrayList<SelectItem>();
        }
        return keywords;
    }

    public void setKeywords(List<SelectItem> keywords) {
        this.keywords = keywords;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public List<SelectItem> getVersions() {
        if (versions == null) {
            versions = new ArrayList<SelectItem>();
        }
        return versions;
    }

    public void setVersions(List<SelectItem> versions) {
        this.versions = versions;
    }

    public String getFees() {
        return fees;
    }

    public void setFees(String fees) {
        this.fees = fees;
    }

    public String getAccessConstraints() {
        return accessConstraints;
    }

    public void setAccessConstraints(String accesConstraints) {
        this.accessConstraints = accesConstraints;
    }

    public String getPhoneVoice() {
        return phoneVoice;
    }

    public void setPhoneVoice(String phoneVoice) {
        this.phoneVoice = phoneVoice;
    }

    public String getPhoneFacsimile() {
        return phoneFacsimile;
    }

    public void setPhoneFacsimile(String phoneFacsimile) {
        this.phoneFacsimile = phoneFacsimile;
    }

    public String getDeliveryPoint() {
        return deliveryPoint;
    }

    public void setDeliveryPoint(String deliveryPoint) {
        this.deliveryPoint = deliveryPoint;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAdministrativeArea() {
        return administrativeArea;
    }

    public void setAdministrativeArea(String administrativeArea) {
        this.administrativeArea = administrativeArea;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getProviderSite() {
        return providerSite;
    }

    public void setProviderSite(String providerSite) {
        this.providerSite = providerSite;
    }

    public String getIndividualName() {
        return individualName;
    }

    public void setIndividualName(String individualName) {
        this.individualName = individualName;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getElectronicAddress() {
        return electronicAddress;
    }

    public void setElectronicAddress(String electronicAddress) {
        this.electronicAddress = electronicAddress;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public int getLayerLimit() {
        return layerLimit;
    }

    public void setLayerLimit(int layerLimit) {
        this.layerLimit = layerLimit;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public String getWebServiceMode() {
        return webServiceMode;
    }

    public void setWebServiceMode(String webServiceMode) {
        this.webServiceMode = webServiceMode;
    }

    public List getWebServices() {
        return webServices;
    }

    public void setWebServices(List webServices) {
        this.webServices = webServices;
    }
    /*
    public UploadedFile getUploadedFile() {
    return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
    this.uploadedFile = uploadedFile;
    } */

    public String getUrlPreference() {
        return urlPreference;
    }

    public void setUrlPreference(String urlPreference) {
        this.urlPreference = urlPreference;
    }

    public boolean isExistPrefrence() {
        return existPrefrence;
    }

    public void setExistPrefrence(boolean existPrefrence) {
        this.existPrefrence = existPrefrence;
    }

    public void reloadLayerProviders() {
        LayerProviderProxy.getInstance().reload();
    }

    public void reloadStyleProviders() {
        StyleProviderProxy.getInstance().reload();
    }

    public List<String> getLayerProviders() {
        final List<String> names = new ArrayList<String>();
        try {
            for (Name n : LayerProviderProxy.getInstance().getKeys()) {
                if (n.getNamespaceURI() != null) {
                    names.add("{" + n.getNamespaceURI() + "}" + n.getLocalPart());
                } else {
                    names.add("{}" + n.getLocalPart());
                }
            }
            Collections.sort(names);
            layerProviderError = SERVICESERROR.NOERROR;
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.SEVERE, "An error occurs while trying to load the layer providers.");
            layerProviderError = SERVICESERROR.LAYERPROVIDERERROR;
        }
        return names;
    }

    public List<String> getStyleProviders() {
        final List<String> names = new ArrayList<String>();
        try {
            names.addAll(StyleProviderProxy.getInstance().getKeys());
            Collections.sort(names);
            styleProviderError = SERVICESERROR.NOERROR;
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.SEVERE, "An error occurs while trying to load the style providers.");
            styleProviderError = SERVICESERROR.STYLEPROVIDERERROR;
        }
        return names;
    }

    public void setLayerProviderError(SERVICESERROR layerProviderError) {
        this.layerProviderError = layerProviderError;
    }

    public SERVICESERROR getLayerProviderError() {
        return layerProviderError;
    }

    public void setStyleProviderError(SERVICESERROR styleProviderError) {
        this.styleProviderError = styleProviderError;
    }

    public SERVICESERROR getStyleProviderError() {
        return styleProviderError;
    }

    /**
     * This method is called from services page via a commandbutton action event
     */
    public void reloadPortrayal(){
        LOGGER.warning("map decoration has been de-activated since there is multi-instance");
        /*
        WMSMapDecoration.reload();
         */
    }

    public List<Entry> getHints() {
        LOGGER.warning("map decoration has been de-activated since there is multi-instance");
        return new ArrayList<Entry>();
        /*
        return new ArrayList<Entry>(WMSMapDecoration.getHints().entrySet());
         */
    }

    public List<Entry> getCompressions(){
        LOGGER.warning("map decoration has been de-activated since there is multi-instance");
        return new ArrayList<Entry>();
        /*return new ArrayList<Entry>(WMSMapDecoration.getCompressions().entrySet());*/
    }


}
