/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
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
package net.seagis.bean;

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
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

// JSF dependencies
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.servlet.ServletContext;

// JAXB dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

// SeaGIS dependencies
import net.seagis.gml.CodeListType;
import net.seagis.ows.v110.AddressType;
import net.seagis.ows.v110.CapabilitiesBaseType;
import net.seagis.ows.v110.CodeType;
import net.seagis.ows.v110.ContactType;
import net.seagis.ows.v110.KeywordsType;
import net.seagis.ows.v110.LanguageStringType;
import net.seagis.ows.v110.OnlineResourceType;
import net.seagis.ows.v110.ResponsiblePartySubsetType;
import net.seagis.ows.v110.ServiceIdentification;
import net.seagis.ows.v110.ServiceProvider;
import net.seagis.ows.v110.TelephoneType;
import net.seagis.wcs.v100.Keywords;
import net.seagis.wcs.v100.MetadataLinkType;
import net.seagis.wcs.v100.ResponsiblePartyType;
import net.seagis.wcs.v100.ServiceType;
import net.seagis.wcs.v100.WCSCapabilitiesType;
import net.seagis.wcs.v111.Capabilities;
import net.seagis.webservice.UserData;
import net.seagis.wms.ContactAddress;
import net.seagis.wms.ContactInformation;
import net.seagis.wms.ContactPersonPrimary;
import net.seagis.wms.Keyword;
import net.seagis.wms.KeywordList;
import net.seagis.wms.OnlineResource;
import net.seagis.wms.Service;
import net.seagis.wms.WMSCapabilities;
import net.seagis.wms.WMT_MS_Capabilities;
import org.apache.myfaces.custom.fileupload.UploadedFile;

/**
 *
 * @author Guilhem Legal
 * @author Medhi Sidhoum
 */
public class ServicesBean {

    /**
     * The service Identification title
     */
    private String title;
    /**
     * The service Identification description
     */
    private String _abstract;
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
     * The marshaller to store the updates
     */
    private Marshaller marshaller;
    /**
     * The a unmarshaller to read the bases capabilities files.
     */
    private Unmarshaller unmarshaller;
    /**
     * A servlet context allowing to find the path to deployed file.
     */
    private ServletContext servletContext;
    /**
     * This is an attribute that defines the current selected web service mode.
     * Default is WMS.
     */
    private String webServiceMode = "WMS";
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
     *  A logger (debugging purpose)
     * The uploaded File.
     */
    private UploadedFile uploadedFile;
    
    private String urlPreference = "";
    
    private boolean existPrefrence = false;
    
    /**
     * 
     */
    private Logger logger = Logger.getLogger("net.seagis.bean");

    public ServicesBean() throws JAXBException, FileNotFoundException {
        
        userData = new UserData();

        // we get the sevlet context to read the capabilities files in the deployed war
        FacesContext context = FacesContext.getCurrentInstance();
        servletContext = (ServletContext) context.getExternalContext().getContext();

        //adding items into the webServices list.
        addWebServices();


        //we create the JAXBContext and read the selected file 
        JAXBContext JBcontext = JAXBContext.newInstance(Capabilities.class, WMSCapabilities.class,
                WMT_MS_Capabilities.class, WCSCapabilitiesType.class,
                net.seagis.cat.csw.Capabilities.class, UserData.class);

        unmarshaller = JBcontext.createUnmarshaller();
        marshaller = JBcontext.createMarshaller();


    }

    /**
     * fill The formular with OWS 1.1.0 Object
     */
    private void fillFormFromOWS110(CapabilitiesBaseType cap) {

        //we fill the default value of Service Identification
        ServiceIdentification SI = cap.getServiceIdentification();
        if (SI.getTitle().size() > 0) {
            this.title = SI.getTitle().get(0).getValue();
        }
        if (SI.getAbstract().size() > 0) {
            this._abstract = SI.getAbstract().get(0).getValue();
        }
        if (SI.getKeywords().size() > 0) {
            this.keywords = keywordsToSelectItem(SI.getKeywords().get(0));
        }
        this.serviceType = SI.getServiceType().getValue();
        this.versions = stringToSelectItem(SI.getServiceTypeVersion());
        this.fees = SI.getFees();
        if (SI.getAccessConstraints().size() > 0) {
            this.accessConstraints = SI.getAccessConstraints().get(0);
        }

        //we fill the value of ServiceProvider
        ServiceProvider SP = cap.getServiceProvider();
        ResponsiblePartySubsetType SC = SP.getServiceContact();
        ContactType CI = SC.getContactInfo();
        TelephoneType T = CI.getPhone();
        AddressType A = CI.getAddress();
        this.providerName = SP.getProviderName();
        this.providerSite = SP.getProviderSite().getHref();
        this.individualName = SC.getIndividualName();
        this.positionName = SC.getPositionName();

        // Phone party
        if (T.getVoice().size() > 0) {
            this.phoneVoice = T.getVoice().get(0);
        }
        if (T.getFacsimile().size() > 0) {
            this.phoneFacsimile = T.getFacsimile().get(0);
        }

        //address party
        if (A.getDeliveryPoint().size() > 0) {
            this.deliveryPoint = A.getDeliveryPoint().get(0);
        }
        this.city = A.getCity();
        this.administrativeArea = A.getAdministrativeArea();
        this.postalCode = A.getPostalCode();
        this.country = A.getCountry();
        if (A.getElectronicMailAddress().size() > 0) {
            this.electronicAddress = A.getElectronicMailAddress().get(0);
        }
        if (SC.getRole() != null) {
            this.role = SC.getRole().getValue();
        }
    }

    /**
     * fill The formular with OWS 1.0.0 Object
     */
    private void fillFormFromOWS100(net.seagis.ows.v100.CapabilitiesBaseType cap) {

        //we fill the default value of Service Identification
        net.seagis.ows.v100.ServiceIdentification SI = cap.getServiceIdentification();
        this.title = SI.getTitle();
        this._abstract = SI.getAbstract();
        if (SI.getKeywords().size() > 0) {
            this.keywords = keywordsToSelectItem(SI.getKeywords().get(0));
        }

        this.serviceType = SI.getServiceType().getValue();
        this.versions = stringToSelectItem(SI.getServiceTypeVersion());
        this.fees = SI.getFees();
        if (SI.getAccessConstraints().size() > 0) {
            this.accessConstraints = SI.getAccessConstraints().get(0);
        }

        //we fill the value of ServiceProvider
        net.seagis.ows.v100.ServiceProvider SP = cap.getServiceProvider();
        net.seagis.ows.v100.ResponsiblePartySubsetType SC = SP.getServiceContact();
        net.seagis.ows.v100.ContactType CI = SC.getContactInfo();
        net.seagis.ows.v100.TelephoneType T = CI.getPhone();
        net.seagis.ows.v100.AddressType A = CI.getAddress();
        this.providerName = SP.getProviderName();
        this.providerSite = SP.getProviderSite().getHref();
        this.individualName = SC.getIndividualName();
        this.positionName = SC.getPositionName();

        // Phone party
        if (T.getVoice().size() > 0) {
            this.phoneVoice = T.getVoice().get(0);
        }
        if (T.getFacsimile().size() > 0) {
            this.phoneFacsimile = T.getFacsimile().get(0);
        }

        //address party
        if (A.getDeliveryPoint().size() > 0) {
            this.deliveryPoint = A.getDeliveryPoint().get(0);
        }
        this.city = A.getCity();
        this.administrativeArea = A.getAdministrativeArea();
        this.postalCode = A.getPostalCode();
        this.country = A.getCountry();
        if (A.getElectronicMailAddress().size() > 0) {
            this.electronicAddress = A.getElectronicMailAddress().get(0);
        }
        if (SC.getRole() != null) {
            this.role = SC.getRole().getValue();
        }
    }

    /**
     * fill The formular with WMS 1.3.0 Object
     */
    private void fillFormFromWMS(WMSCapabilities cap) {

        //we fill the default value of Service Identification
        Service S = cap.getService();
        this.title = S.getTitle();
        this._abstract = S.getAbstract();
        KeywordList klist = S.getKeywordList();
        if (klist != null) {
            this.keywords = keywordsToSelectItem(klist);
        }

        this.versions = new ArrayList<SelectItem>();
        this.versions.add(new SelectItem("1.3.0"));
        this.versions.add(new SelectItem("1.1.1"));

        this.fees = S.getFees();
        this.accessConstraints = S.getAccessConstraints();

        //we fill the value of ServiceProvider
        ContactInformation CI = S.getContactInformation();
        ContactAddress A = CI.getContactAddress();
        ContactPersonPrimary CPP = CI.getContactPersonPrimary();
        this.providerName = CPP.getContactOrganization();
        this.providerSite = S.getOnlineResource().getHref();
        this.individualName = CPP.getContactPerson();
        this.positionName = CI.getContactPosition();

        // Phone party
        this.phoneVoice = CI.getContactVoiceTelephone();
        this.phoneFacsimile = CI.getContactFacsimileTelephone();

        //address party
        this.deliveryPoint = A.getAddress();
        this.city = A.getCity();
        this.administrativeArea = A.getStateOrProvince();
        this.postalCode = A.getPostCode();
        this.country = A.getCountry();
        this.electronicAddress = CI.getContactElectronicMailAddress();

        /*
         * The extras attribute for WMS 
         */
        this.addressType = A.getAddressType();
        this.layerLimit = S.getLayerLimit();
        this.maxHeight = S.getMaxHeight();
        this.maxWidth = S.getMaxWidth();
    }

    /**
     * Transform a list of languageString in a Keyword Object into a list of SelectItem.
     * 
     * @param keywords
     * @return
     */
    private List<SelectItem> keywordsToSelectItem(KeywordsType keywords) {
        List<SelectItem> results = new ArrayList<SelectItem>();

        for (LanguageStringType keyword : keywords.getKeyword()) {
            results.add(new SelectItem(keyword.getValue()));
        }

        return results;
    }

    /**
     * Transform a list of String in a Keyword Object into a list of SelectItem.
     * 
     * @param keywords
     * @return
     */
    private List<SelectItem> keywordsToSelectItem(net.seagis.ows.v100.KeywordsType keywords) {
        List<SelectItem> results = new ArrayList<SelectItem>();

        for (String keyword : keywords.getKeyword()) {
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
        List<SelectItem> results = new ArrayList<SelectItem>();

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
        List<SelectItem> results = new ArrayList<SelectItem>();

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
        File f = new File(servletContext.getRealPath("WEB-INF/change.properties"));
        Properties p = new Properties();
        FileInputStream in = new FileInputStream(f);
        p.load(in);
        in.close();
        p.put("update", "true");
        FileOutputStream out = new FileOutputStream(f);
        p.store(out, "updated from JSF interface");
        out.close();
        int i = 0;
        for (Object capa : capabilities) {

            //for OWS 1.1.0
            if (capa instanceof net.seagis.ows.v110.CapabilitiesBaseType) {
                ServiceIdentification SI = getServiceIdentification110();
                ServiceProvider SP = getServiceProvider110();
                ((CapabilitiesBaseType) capa).setServiceProvider(SP);
                ((CapabilitiesBaseType) capa).setServiceIdentification(SI);

            // for OWS 1.0.0
            } else if (capa instanceof net.seagis.ows.v100.CapabilitiesBaseType) {
                net.seagis.ows.v100.ServiceIdentification SI = getServiceIdentification100();
                net.seagis.ows.v100.ServiceProvider SP = getServiceProvider100();
                ((net.seagis.ows.v100.CapabilitiesBaseType) capa).setServiceProvider(SP);
                ((net.seagis.ows.v100.CapabilitiesBaseType) capa).setServiceIdentification(SI);


            // for WCS 1.0.0
            } else if (capa instanceof WCSCapabilitiesType) {
                ServiceType S = getWCSService();
                ((WCSCapabilitiesType) capa).setService(S);

            // for WMS 1.3.0/1.1.1
            } else if (capa instanceof WMSCapabilities || capa instanceof WMT_MS_Capabilities) {
                Service S = getWMSService();
                ((WMSCapabilities) capa).setService(S);
                logger.info("update WMS version" + i);
            }
            i++;
        }
        storeCapabilitiesFile();
        return "goBack";
    }

    /**
     * Build the Service Identification object of an OWS 1.1 service.
     */
    public ServiceIdentification getServiceIdentification110() {

        List<LanguageStringType> listKey = new ArrayList<LanguageStringType>();
        for (SelectItem k : keywords) {
            listKey.add(new LanguageStringType((String) k.getValue()));
        }

        List<String> listVers = new ArrayList<String>();
        for (SelectItem v : versions) {
            listVers.add((String) v.getValue());
        }

        ServiceIdentification SI = new ServiceIdentification(new LanguageStringType(title),
                new LanguageStringType(_abstract),
                new KeywordsType(listKey, null),
                new CodeType(serviceType),
                listVers,
                fees,
                accessConstraints);
        return SI;
    }

    /**
     * Build the Service Identification object of an OWS 1.0 service.
     */
    public net.seagis.ows.v100.ServiceIdentification getServiceIdentification100() {

        List<String> listKey = new ArrayList<String>();
        for (SelectItem k : keywords) {
            listKey.add((String) k.getValue());
        }

        List<String> listVers = new ArrayList<String>();
        for (SelectItem v : versions) {
            listVers.add((String) v.getValue());
        }

        net.seagis.ows.v100.ServiceIdentification SI = new net.seagis.ows.v100.ServiceIdentification(title,
                _abstract,
                new net.seagis.ows.v100.KeywordsType(listKey, null),
                new net.seagis.ows.v100.CodeType(serviceType),
                listVers,
                fees,
                accessConstraints);
        return SI;
    }

    /**
     * Build a service object for a  WCS 1.0.0 service.
     */
    private ServiceType getWCSService() {

        List<MetadataLinkType> links = new ArrayList<MetadataLinkType>();
        links.add(new MetadataLinkType(providerSite));

        List<String> listKey = new ArrayList<String>();
        for (SelectItem k : keywords) {
            listKey.add((String) k.getValue());
        }

        net.seagis.wcs.v100.TelephoneType tel = new net.seagis.wcs.v100.TelephoneType(phoneVoice, phoneFacsimile);

        net.seagis.wcs.v100.AddressType adr = new net.seagis.wcs.v100.AddressType(deliveryPoint,
                city,
                administrativeArea,
                postalCode,
                country,
                electronicAddress);

        net.seagis.wcs.v100.ContactType CI = new net.seagis.wcs.v100.ContactType(tel, adr, null);

        ResponsiblePartyType resp = new ResponsiblePartyType(individualName,
                positionName,
                providerName,
                CI);

        ServiceType service = new ServiceType(links,
                title,
                title,
                _abstract,
                new Keywords(listKey),
                resp,
                new CodeListType(fees),
                new CodeListType(accessConstraints),
                null);
        return service;
    }

    /**
     * Build a service object for a  WMS 1.3.0/1.1.1 Service.
     */
    private Service getWMSService() {

        List<Keyword> listKey = new ArrayList<Keyword>();
        for (SelectItem k : keywords) {
            listKey.add(new Keyword((String) k.getValue()));
        }
        KeywordList keywordList = new KeywordList(listKey);
        ContactPersonPrimary CPP = new ContactPersonPrimary(individualName, providerName);
        ContactAddress CA = new ContactAddress(getAddressType(), deliveryPoint, city, administrativeArea,
                postalCode, country);

        ContactInformation CI = new ContactInformation(CPP, positionName,
                CA, phoneVoice, phoneFacsimile, electronicAddress);

        Service service = new Service(title, title, _abstract,
                keywordList,
                new OnlineResource(providerSite),
                CI, fees, accessConstraints, getLayerLimit(),
                getMaxWidth(), getMaxHeight());
        return service;
    }

    /**
     * Build the Service Provider object of an OWS 1.1 service.
     */
    public ServiceProvider getServiceProvider110() {

        AddressType adr = new AddressType(deliveryPoint,
                city,
                administrativeArea,
                postalCode,
                country,
                electronicAddress);

        ContactType CI = new ContactType(new TelephoneType(phoneVoice, phoneFacsimile),
                adr,
                null, null, null);

        ResponsiblePartySubsetType SC = new ResponsiblePartySubsetType(individualName,
                positionName,
                CI,
                new CodeType(role));

        ServiceProvider SP = new ServiceProvider(providerName,
                new OnlineResourceType(providerSite),
                SC);
        return SP;
    }

    /**
     * Build the Service Provider object of an OWS 1.0 service.
     */
    public net.seagis.ows.v100.ServiceProvider getServiceProvider100() {

        net.seagis.ows.v100.AddressType adr = new net.seagis.ows.v100.AddressType(deliveryPoint,
                city,
                administrativeArea,
                postalCode,
                country,
                electronicAddress);

        net.seagis.ows.v100.ContactType CI = new net.seagis.ows.v100.ContactType(new net.seagis.ows.v100.TelephoneType(phoneVoice, phoneFacsimile),
                adr,
                null, null, null);

        net.seagis.ows.v100.ResponsiblePartySubsetType SC = new net.seagis.ows.v100.ResponsiblePartySubsetType(
                individualName,
                positionName,
                CI,
                new net.seagis.ows.v100.CodeType(role));

        net.seagis.ows.v100.ServiceProvider SP = new net.seagis.ows.v100.ServiceProvider(
                providerName,
                new net.seagis.ows.v100.OnlineResourceType(providerSite),
                SC);
        return SP;
    }

    /**
     * Update the capabilities XML file of the webService and store it in a UserData Object.
     * 
     * @throws javax.xml.bind.JAXBException
     */
    private void storeCapabilitiesFile() throws JAXBException {
        try {
            int i = 0;
            for (File f : capabilitiesFile) {
                OutputStream out = new FileOutputStream(f);
                marshaller.marshal(capabilities[i],out);
                out.close();
                logger.info("store " + f.getAbsolutePath());
                i++;
            }
            
            if (webServiceMode.equals("WMS")) {
                userData.setWMSCapabilities(capabilities);
            
            } else if (webServiceMode.equals("WCS")) {
                userData.setWCSCapabilities(capabilities);
                
            } else if (webServiceMode.equals("SOS")) {
                userData.setSOSCapabilities(capabilities);
                
            } else if (webServiceMode.equals("CSW")) {
                userData.setCSWCapabilities(capabilities);
                
            }
        } catch (IOException ex) {
            Logger.getLogger(ServicesBean.class.getName()).log(Level.SEVERE, null, ex);
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
            	userData = (UserData) unmarshaller.unmarshal(f);
            } else {
		logger.severe("File uploaded null");
	        return;
	    }
            userData = (UserData) unmarshaller.unmarshal(f);
            
            // we extract and update WMS user data
            if (userData.getWMSCapabilities() != null) {
                
                if (userData.getWMSCapabilities().length == 2) {
                    
                    //we begin to write the high lvl document
                    String path = servletContext.getRealPath("WEB-INF/WMSCapabilities1.3.0.xml");
                    File file   = new File(path);
                    if (file.exists()) {
                    
                        marshaller.marshal(userData.getWMSCapabilities()[0], (OutputStream) new FileOutputStream(file));
              
                    } else {
                        logger.severe("WMS capabilities file version 1.3.0 not found at :" + path + ". unable to load WMS Data");
                    }
        
                    // the we add to the list of object to update the other sub version
                    path  = servletContext.getRealPath("WEB-INF/WMSCapabilities1.1.1.xml");
                    file  = new File(path);
                    if (file.exists()) {
                    
                        marshaller.marshal(userData.getWMSCapabilities()[1], (OutputStream) new FileOutputStream(file));
              
                    } else {
                        logger.severe("WMS capabilities file version 1.1.1 not found at :" + path + ". unable to load WMS Data");
                    }
                } else {
                    // TODO afficher fichier non valide
                    logger.severe("WMS capabilie file uncomplete (!=2)");               
                }
            }
            
            // we extract and update WCS user data
            if (userData.getWCSCapabilities() != null) {
                
                if (userData.getWCSCapabilities().length == 2) {
                    
                    //we begin to write the high lvl document
                    String path = servletContext.getRealPath("WEB-INF/WCSCapabilities1.1.1.xml");
                    File file   = new File(path);
                    if (file.exists()) {
                    
                        marshaller.marshal(userData.getWCSCapabilities()[0], (OutputStream) new FileOutputStream(file));
              
                    } else {
                        logger.severe("WCS capabilities file version 1.1.1 not found at :" + path + ". unable to load WCS Data");
                    }
        
                    // the we add to the list of object to update the other sub version
                    path  = servletContext.getRealPath("WEB-INF/WCSCapabilities1.0.0.xml");
                    file  = new File(path);
                    if (file.exists()) {
                    
                        marshaller.marshal(userData.getWCSCapabilities()[1], (OutputStream) new FileOutputStream(file));
              
                    } else {
                        logger.severe("WCS capabilities file version 1.0.0 not found at :" + path + ". unable to load WCS Data");
                    }
                } else {
                    // TODO afficher fichier non valide
                    logger.severe("WCS capabilies file uncomplete (!=2)");               
                }
            }
            
             // we extract and update CSW user data
            if (userData.getCSWCapabilities() != null) {
                
                if (userData.getCSWCapabilities().length == 1) {
                    
                    //we begin to write the high lvl document
                    String path = servletContext.getRealPath("WEB-INF/CSWCapabilities2.0.2.xml");
                    File file   = new File(path);
                    if (file.exists()) {
                    
                        marshaller.marshal(userData.getCSWCapabilities()[0], (OutputStream) new FileOutputStream(file));
              
                    } else {
                        logger.severe("CSW capabilities file version 2.0.2 not found at :" + path + ". unable to load CSW Data");
                    }
                    
                } else {
                    // TODO afficher fichier non valide
                    logger.severe("WCS capabilies file uncomplete (!=1)");               
                }
            }
            
             // we extract and update SOS user data
            if (userData.getSOSCapabilities() != null) {
                
                if (userData.getSOSCapabilities().length == 1) {
                    
                    //we begin to write the high lvl document
                    String path = servletContext.getRealPath("WEB-INF/SOSCapabilities1.0.0.xml");
                    File file   = new File(path);
                    if (file.exists()) {
                    
                        marshaller.marshal(userData.getSOSCapabilities()[0], (OutputStream) new FileOutputStream(file));
              
                    } else {
                        logger.severe("SOS capabilities file version 1.0.0 not found at :" + path + ". unable to load SOS Data");
                    }
                    
                } else {
                    // TODO afficher fichier non valide
                    logger.severe("SOS capabilies file uncomplete (!=1)");               
                }
            }
            
            //we signal to the webService to update is capabilities
            File change        = new File(servletContext.getRealPath("WEB-INF/change.properties"));
            Properties p       = new Properties();
            FileInputStream in = new FileInputStream(change);
            p.load(in);
            in.close();
            p.put("update", "true");
            FileOutputStream out = new FileOutputStream(change);
            p.store(out, "updated from JSF interface");
            out.close();
            
        } catch (JAXBException ex) {
            Logger.getLogger(ServicesBean.class.getName()).log(Level.SEVERE, null, ex);
            //TODO afficher quelquechose si le fichier n'est pas valide
        }
    }
    
    public void storeData() throws JAXBException {
        String url = servletContext.getRealPath("preference.sml");
        setUrlPreference("preference.sml");
        File f = new File(url);
        //f.setWritable(true);
        setExistPrefrence(true);
       marshaller.marshal(userData, f);
    }
    
    
    public String setWMSMode() throws JAXBException, FileNotFoundException {
        logger.info("set WMS mode");
        webServiceMode    = "WMS";
        capabilities     = new Object[2];
        capabilitiesFile = new File[2];

        //we begin to read the high lvl document
        String path = servletContext.getRealPath("WEB-INF/WMSCapabilities1.3.0.xml");
        capabilitiesFile[0] = new File(path);
        if (capabilitiesFile[0].exists()) {

            capabilities[0] = unmarshaller.unmarshal(new FileReader(capabilitiesFile[0]));
            fillFormFromWMS((WMSCapabilities) capabilities[0]);

        } else {
            logger.severe("WMS capabilities file version 1.3.0 not found at :" + path);
        }

        // the we add to the list of object to update the other sub version
        path = servletContext.getRealPath("WEB-INF/WMSCapabilities1.1.1.xml");
        capabilitiesFile[1] = new File(path);
        if (capabilitiesFile[1].exists()) {

            capabilities[1] = unmarshaller.unmarshal(new FileReader(capabilitiesFile[1]));

        } else {
            logger.severe("WMS capabilities file version 1.1.1 not found at :" + path);
        }

        return "fillForm";

    }

    public String setWCSMode() throws FileNotFoundException, JAXBException {

        webServiceMode = "WCS";
        capabilities = new Object[2];
        capabilitiesFile = new File[2];

        //we begin to read the high lvl document
        String path = servletContext.getRealPath("WEB-INF/WCSCapabilities1.1.1.xml");
        capabilitiesFile[0] = new File(path);
        if (capabilitiesFile[0].exists()) {

            capabilities[0] = unmarshaller.unmarshal(new FileReader(capabilitiesFile[0]));
            fillFormFromOWS110((Capabilities) capabilities[0]);

        } else {
            logger.severe("WCS capabilities file version 1.1.1 not found at :" + path);
        }

        // the we add to the list of object to update the other sub version
        path = servletContext.getRealPath("WEB-INF/WCSCapabilities1.0.0.xml");
        capabilitiesFile[1] = new File(path);
        if (capabilitiesFile[1].exists()) {

            capabilities[1] = unmarshaller.unmarshal(new FileReader(capabilitiesFile[1]));

        } else {
            logger.severe("WCS capabilities file version 1.0.0 not found at :" + path);
        }

        return "fillForm";

    }

    public String setSOSMode() throws FileNotFoundException, JAXBException {

        webServiceMode = "SOS";
        capabilities = new Object[1];
        capabilitiesFile = new File[1];

        //we begin to read the high lvl document
        String path = servletContext.getRealPath("WEB-INF/SOSCapabilities1.0.0.xml");
        capabilitiesFile[0] = new File(path);
        if (capabilitiesFile[0].exists()) {

            capabilities[0] = unmarshaller.unmarshal(new FileReader(capabilitiesFile[0]));
            fillFormFromOWS110((CapabilitiesBaseType) capabilities[0]);

        } else {
            logger.severe("SOS capabilities file version 1.0.0 not found at :" + path);
        }

        return "fillForm";
    }

    public String setCSWMode() throws FileNotFoundException, JAXBException {

        webServiceMode = "CSW";
        capabilities = new Object[1];
        capabilitiesFile = new File[1];

        //we begin to read the high lvl document
        String path = servletContext.getRealPath("WEB-INF/CSWCapabilities2.0.2.xml");
        capabilitiesFile[0] = new File(path);
        if (capabilitiesFile[0].exists()) {

            capabilities[0] = unmarshaller.unmarshal(new FileReader(capabilitiesFile[0]));
            fillFormFromOWS100((net.seagis.ows.v100.CapabilitiesBaseType) capabilities[0]);

        } else {
            logger.severe("CSW capabilities file version 2.0.2 not found at :" + path);
        }

        return "fillForm";
    }

    /**
     * this method switch to the appropriate mode and returns the outcome string to proceed the jsf navigation.
     * @return
     * @throws java.io.FileNotFoundException
     * @throws javax.xml.bind.JAXBException
     */
    public String switchMode() throws FileNotFoundException, JAXBException {
        if (webServiceMode.equals("WMS")) {
            setWMSMode();
        } else if (webServiceMode.equals("WCS")) {
            setWCSMode();
        } else if (webServiceMode.equals("SOS")) {
            setSOSMode();
        } else if (webServiceMode.equals("CSW")) {
            setCSWMode();
        }
        return "fillForm";
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
        if (existsCapabilities("CSW")) {
            webServices.add(new SelectItem("CSW", "CSW metadata", null));
        }
        if (existsCapabilities("SOS")) {
            webServices.add(new SelectItem("SOS", "SOS metadata", null));
        }
        if (existsCapabilities("WCS")) {
            webServices.add(new SelectItem("WCS", "WCS metadata", null));
        }
        if (existsCapabilities("WMS")) {
            webServices.add(new SelectItem("WMS", "WMS metadata", null));
        }
    }

    public boolean existsCapabilities(String ws) {
        boolean exist = false;
        File file;
        String path;
        if (ws.equals("CSW")) {
            path = servletContext.getRealPath("WEB-INF/CSWCapabilities2.0.2.xml");
            file = new File(path);
            exist = file.exists();
        } else if (ws.equals("SOS")) {
            path = servletContext.getRealPath("WEB-INF/SOSCapabilities1.0.0.xml");
            file = new File(path);
            exist = file.exists();
        } else if (ws.equals("WCS")) {
            path = servletContext.getRealPath("WEB-INF/WCSCapabilities1.0.0.xml");
            file = new File(path);
            exist = file.exists();
        } else if (ws.equals("WMS")) {
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
        File f = File.createTempFile("userData", "geomatys");
        try {
           
            InputStream inputStream = uploadedFile.getInputStream();
            InputStreamReader infile = new InputStreamReader(inputStream);
            BufferedReader inbuf = new BufferedReader(infile);
            FileWriter writer = new FileWriter(f);

            String line;
            while ((line = inbuf.readLine()) != null) {
                writer.append(line);
                writer.append('\n');
            }
            inputStream.close();
            infile.close();
            writer.close();
            
        } catch (Exception x) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_FATAL,
                    x.getClass().getName(), x.getMessage());
            FacesContext.getCurrentInstance().addMessage(
                    null, message);
            logger.severe("Exception in proccesSubmitFile " + x.getMessage());
            return null;
        }
        if (f == null) {
            logger.severe("process uploaded file null");
        }
        return f;
    }
    
    public String doUpload() throws IOException{
        File f = processSubmitedFile();
        if (f == null) {
            logger.severe("[doUpload]process uploaded file null");
        }
        loadUserData(f);
        return "ok";
    }

    /**
     * this method put in the application map some parameters.
     * @throws java.io.IOException
     */
    public void upload() throws IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.getExternalContext().getApplicationMap().put("fileupload_bytes", uploadedFile.getBytes());
        facesContext.getExternalContext().getApplicationMap().put("fileupload_type", uploadedFile.getContentType());
        facesContext.getExternalContext().getApplicationMap().put("fileupload_name", uploadedFile.getName());
    }

    /**
     * this method return a flag that indicates if a file was upladed.
     * 
     * @return boolean
     */
    public boolean isUploaded() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        return facesContext.getExternalContext().getApplicationMap().get("fileupload_bytes") != null;
    }

    public String goBack() {
        return "goBack";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String Title) {
        this.title = Title;
    }

    public String getAbstract() {
        return _abstract;
    }

    public void setAbstract(String _abstract) {
        this._abstract = _abstract;
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

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

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
}
