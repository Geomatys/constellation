/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.seagis.bean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// JSF dependencies
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
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
import net.seagis.wms.ContactAddress;
import net.seagis.wms.ContactInformation;
import net.seagis.wms.ContactPersonPrimary;
import net.seagis.wms.Keyword;
import net.seagis.wms.KeywordList;
import net.seagis.wms.OnlineResource;
import net.seagis.wms.Service;
import net.seagis.wms.WMSCapabilities;
import net.seagis.wms.WMT_MS_Capabilities;

/**
 *
 * @author guilhem
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
     * 
     */
    private Logger logger = Logger.getLogger("net.seagis.bean");
    
    public ServicesBean() throws JAXBException, FileNotFoundException {
        
        // we get the sevlet context to read the capabilities files in the deployed war
        FacesContext context = FacesContext.getCurrentInstance();
        servletContext       = (ServletContext) context.getExternalContext().getContext();
        
        //we create the JAXBContext and read the selected file 
        JAXBContext JBcontext = JAXBContext.newInstance(Capabilities.class, WMSCapabilities.class,
                                                        WMT_MS_Capabilities.class, WCSCapabilitiesType.class,
                                                        net.seagis.cat.csw.Capabilities.class);
                                                        
        unmarshaller          = JBcontext.createUnmarshaller();
        marshaller            = JBcontext.createMarshaller();   
        
          
    }
    
    /**
     * fill The formular with OWS 1.1.0 Object
     */
    private void fillFormFromOWS110(CapabilitiesBaseType cap) {
        
        //we fill the default value of Service Identification
        ServiceIdentification SI = cap.getServiceIdentification(); 
        if (SI.getTitle().size() > 0)
            this.title           = SI.getTitle().get(0).getValue();
        if (SI.getAbstract().size() > 0)
            this._abstract       = SI.getAbstract().get(0).getValue();
        if (SI.getKeywords().size() > 0)
            this.keywords        = keywordsToSelectItem(SI.getKeywords().get(0));  
        this.serviceType         = SI.getServiceType().getValue();
        this.versions            = stringToSelectItem(SI.getServiceTypeVersion());
        this.fees                = SI.getFees();
        if (SI.getAccessConstraints().size() > 0)
            this.accessConstraints    = SI.getAccessConstraints().get(0);
        
        //we fill the value of ServiceProvider
        ServiceProvider SP       = cap.getServiceProvider();
        ResponsiblePartySubsetType SC = SP.getServiceContact();
        ContactType CI           = SC.getContactInfo();
        TelephoneType T          = CI.getPhone();
        AddressType A            = CI.getAddress();
        this.providerName        = SP.getProviderName();
        this.providerSite        = SP.getProviderSite().getHref();
        this.individualName      = SC.getIndividualName();
        this.positionName        = SC.getPositionName();
        
        // Phone party
        if (T.getVoice().size() > 0)
            this.phoneVoice      = T.getVoice().get(0);
        if (T.getFacsimile().size() > 0)
            this.phoneFacsimile  = T.getFacsimile().get(0);
        
         //address party
         if (A.getDeliveryPoint().size() > 0)
            this.deliveryPoint   = A.getDeliveryPoint().get(0);
         this.city                = A.getCity();
         this.administrativeArea  = A.getAdministrativeArea();
         this.postalCode          = A.getPostalCode();
         this.country             = A.getCountry();
         if (A.getElectronicMailAddress().size() > 0)
            this.electronicAddress   = A.getElectronicMailAddress().get(0);
         if (SC.getRole() != null)
            this.role            = SC.getRole().getValue(); 
    }
    
     /**
     * fill The formular with OWS 1.0.0 Object
     */
    private void fillFormFromOWS100(net.seagis.ows.v100.CapabilitiesBaseType cap) {
        
        //we fill the default value of Service Identification
        net.seagis.ows.v100.ServiceIdentification SI = cap.getServiceIdentification(); 
        this.title               = SI.getTitle();
        this._abstract           = SI.getAbstract();
        if (SI.getKeywords().size() > 0)
            this.keywords        = keywordsToSelectItem(SI.getKeywords().get(0));  
        
        this.serviceType         = SI.getServiceType().getValue();
        this.versions            = stringToSelectItem(SI.getServiceTypeVersion());
        this.fees                = SI.getFees();
        if (SI.getAccessConstraints().size() > 0)
            this.accessConstraints    = SI.getAccessConstraints().get(0);
        
        //we fill the value of ServiceProvider
        net.seagis.ows.v100.ServiceProvider            SP = cap.getServiceProvider();
        net.seagis.ows.v100.ResponsiblePartySubsetType SC = SP.getServiceContact();
        net.seagis.ows.v100.ContactType                CI = SC.getContactInfo();
        net.seagis.ows.v100.TelephoneType              T  = CI.getPhone();
        net.seagis.ows.v100.AddressType                A  = CI.getAddress();
        this.providerName        = SP.getProviderName();
        this.providerSite        = SP.getProviderSite().getHref();
        this.individualName      = SC.getIndividualName();
        this.positionName        = SC.getPositionName();
        
        // Phone party
        if (T.getVoice().size() > 0)
            this.phoneVoice      = T.getVoice().get(0);
        if (T.getFacsimile().size() > 0)
            this.phoneFacsimile  = T.getFacsimile().get(0);
        
         //address party
         if (A.getDeliveryPoint().size() > 0)
            this.deliveryPoint   = A.getDeliveryPoint().get(0);
         this.city                = A.getCity();
         this.administrativeArea  = A.getAdministrativeArea();
         this.postalCode          = A.getPostalCode();
         this.country             = A.getCountry();
         if (A.getElectronicMailAddress().size() > 0)
            this.electronicAddress   = A.getElectronicMailAddress().get(0);
         if (SC.getRole() != null)
            this.role            = SC.getRole().getValue(); 
    }
    
    /**
     * fill The formular with WMS 1.3.0 Object
     */
    private void fillFormFromWMS(WMSCapabilities cap) {
        
        //we fill the default value of Service Identification
        Service S         = cap.getService();
        this.title        = S.getTitle();
        this._abstract    = S.getAbstract();
        KeywordList klist = S.getKeywordList();
        if (klist != null)
            this.keywords        = keywordsToSelectItem(klist);
        
        this.versions            = new ArrayList<SelectItem>();
        this.versions.add(new SelectItem("1.3.0"));
        this.versions.add(new SelectItem("1.1.1"));
        
        this.fees                = S.getFees();
        this.accessConstraints   = S.getAccessConstraints();
        
        //we fill the value of ServiceProvider
        ContactInformation    CI = S.getContactInformation();
        ContactAddress        A  = CI.getContactAddress();
        ContactPersonPrimary CPP = CI.getContactPersonPrimary();
        this.providerName        = CPP.getContactOrganization();
        this.providerSite        = S.getOnlineResource().getHref();
        this.individualName      = CPP.getContactPerson();
        this.positionName        = CI.getContactPosition();
        
        // Phone party
        this.phoneVoice          = CI.getContactVoiceTelephone();
        this.phoneFacsimile      = CI.getContactFacsimileTelephone();
        
         //address party
         this.deliveryPoint       = A.getAddress();
         this.city                = A.getCity();
         this.administrativeArea  = A.getStateOrProvince();
         this.postalCode          = A.getPostCode();
         this.country             = A.getCountry();
         this.electronicAddress   = CI.getContactElectronicMailAddress();
         
         /*
          * The extras attribute for WMS 
          */
         this.addressType         = A.getAddressType();
         this.layerLimit          = S.getLayerLimit();
         this.maxHeight           = S.getMaxHeight();
         this.maxWidth            = S.getMaxWidth();
    }
    
    /**
     * Transform a list of languageString in a Keyword Object into a list of SelectItem.
     * 
     * @param keywords
     * @return
     */
    private List<SelectItem> keywordsToSelectItem(KeywordsType keywords) {
        List<SelectItem> results = new ArrayList<SelectItem>();
        
        for (LanguageStringType keyword:keywords.getKeyword()) {
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
        
        for (String keyword:keywords.getKeyword()) {
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
        
        for (Keyword keyword:keywords.getKeyword()) {
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
        
        for (String item:list) {
            results.add(new SelectItem(item));
        }
        
        return results;
    }
    
    /**
     * Store the formular int he XML file
     */
    public void storeForm() throws JAXBException {
        
        for (Object capa: capabilities) {
            
            //for OWS 1.1.0
            if (capa instanceof net.seagis.ows.v110.CapabilitiesBaseType) {
                ServiceIdentification SI = getServiceIdentification110();
                ServiceProvider       SP = getServiceProvider110();
                ((CapabilitiesBaseType)capa).setServiceProvider(SP);
                ((CapabilitiesBaseType)capa).setServiceIdentification(SI);
        
            // for OWS 1.0.0
            } else if (capa instanceof net.seagis.ows.v100.CapabilitiesBaseType) {
                net.seagis.ows.v100.ServiceIdentification SI = getServiceIdentification100();
                net.seagis.ows.v100.ServiceProvider       SP = getServiceProvider100();
                ((net.seagis.ows.v100.CapabilitiesBaseType)capa).setServiceProvider(SP);
                ((net.seagis.ows.v100.CapabilitiesBaseType)capa).setServiceIdentification(SI);
            
                    
            // for WCS 1.0.0
            } else if (capa instanceof WCSCapabilitiesType) {
                ServiceType S = getWCSService();
                ((WCSCapabilitiesType)capa).setService(S);
        
            // for WMS 1.3.0/1.1.1
            } else if (capa instanceof WMSCapabilities) {
                Service S = getWMSService();
                ((WMSCapabilities)capa).setService(S);
            }
        
        }
        storeCapabilitiesFile();
            
    }
    
    /**
     * Build the Service Identification object of an OWS 1.1 service.
     */
    public ServiceIdentification getServiceIdentification110()  {
        
        List<LanguageStringType> listKey = new ArrayList<LanguageStringType>();
        for (SelectItem k: keywords) {
            listKey.add(new LanguageStringType((String)k.getValue()));  
        }
        
        List<String> listVers = new ArrayList<String>();
        for (SelectItem v: versions) {
            listVers.add((String)v.getValue());  
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
    public net.seagis.ows.v100.ServiceIdentification getServiceIdentification100()  {
        
        List<String> listKey = new ArrayList<String>();
        for (SelectItem k: keywords) {
            listKey.add((String)k.getValue());  
        }
        
        List<String> listVers = new ArrayList<String>();
        for (SelectItem v: versions) {
            listVers.add((String)v.getValue());  
        }

        net.seagis.ows.v100.ServiceIdentification SI 
                = new net.seagis.ows.v100.ServiceIdentification(title,
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
         for (SelectItem k: keywords) {
            listKey.add((String)k.getValue());  
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
        for (SelectItem k: keywords) {
            listKey.add(new Keyword((String)k.getValue()));  
        }
        KeywordList keywordList  = new KeywordList(listKey);
        ContactPersonPrimary CPP = new ContactPersonPrimary(individualName, providerName);
        ContactAddress       CA  = new  ContactAddress(getAddressType(),deliveryPoint, city, administrativeArea,
                                                       postalCode, country); 
        
        ContactInformation CI = new ContactInformation( CPP, positionName,
                                                        CA,  phoneVoice,  phoneFacsimile, electronicAddress);
        
        Service service = new Service(title, title, _abstract,
                                      keywordList, 
                                      new OnlineResource(providerSite), 
                                      CI, fees, accessConstraints,getLayerLimit(),
                                      getMaxWidth(),getMaxHeight());
        return service;
    }
    
    /**
     * Build the Service Provider object of an OWS 1.1 service.
     */
    public ServiceProvider getServiceProvider110()  {
        
        AddressType adr = new AddressType(deliveryPoint,
                                          city,
                                          administrativeArea,
                                          postalCode,
                                          country,
                                         electronicAddress);
        
        ContactType CI = new ContactType( new TelephoneType(phoneVoice, phoneFacsimile),
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
    public net.seagis.ows.v100.ServiceProvider getServiceProvider100()  {
        
        net.seagis.ows.v100.AddressType adr = new net.seagis.ows.v100.AddressType(deliveryPoint,
                                          city,
                                          administrativeArea,
                                          postalCode,
                                          country,
                                          electronicAddress);
        
        net.seagis.ows.v100.ContactType CI 
                = new net.seagis.ows.v100.ContactType( new net.seagis.ows.v100.TelephoneType(phoneVoice, phoneFacsimile),
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
    
    private void storeCapabilitiesFile() throws JAXBException {
        int i = 0;
        for (File f:capabilitiesFile)
            marshaller.marshal(capabilities[i], f);
            i++;
    }
    
    public String setWMSMode() throws JAXBException, FileNotFoundException {
        
        capabilities     = new Object[2];
        capabilitiesFile = new File[2];
        
        //we begin to read the high lvl document
        String path          = servletContext.getRealPath("WEB-INF/WMSCapabilities1.3.0.xml");
        capabilitiesFile[0]  = new File(path);
        if (capabilitiesFile[0].exists()) {
                    
            capabilities[0] =  unmarshaller.unmarshal(new FileReader(capabilitiesFile[0]));
            fillFormFromWMS((WMSCapabilities)capabilities[0]);
              
        } else {
            logger.severe("WMS capabilities file version 1.3.0 not found at :" + path);
        }
        
        // the we add to the list of object to update the other sub version
        path                 = servletContext.getRealPath("WEB-INF/WMSCapabilities1.1.1.xml");
        capabilitiesFile[1]  = new File(path);
        if (capabilitiesFile[1].exists()) {
                    
            capabilities[1] =  unmarshaller.unmarshal(new FileReader(capabilitiesFile[1]));
              
        } else {
            logger.severe("WMS capabilities file version 1.1.1 not found at :" + path);
        }
        
        return "fillForm";
        
    }
    
    public String setWCSMode() throws FileNotFoundException, JAXBException {
        
        capabilities     = new Object[2];
        capabilitiesFile = new File[2];
        
        //we begin to read the high lvl document
        String path          = servletContext.getRealPath("WEB-INF/WCSCapabilities1.1.1.xml");
        capabilitiesFile[0]  = new File(path);
        if (capabilitiesFile[0].exists()) {
                    
            capabilities[0] =  unmarshaller.unmarshal(new FileReader(capabilitiesFile[0]));
            fillFormFromOWS110((Capabilities)capabilities[0]);
              
        } else {
            logger.severe("WCS capabilities file version 1.1.1 not found at :" + path);
        }
        
        // the we add to the list of object to update the other sub version
        path                 = servletContext.getRealPath("WEB-INF/WCSCapabilities1.0.0.xml");
        capabilitiesFile[1]  = new File(path);
        if (capabilitiesFile[1].exists()) {
                    
            capabilities[1] =  unmarshaller.unmarshal(new FileReader(capabilitiesFile[1]));
              
        } else {
            logger.severe("WCS capabilities file version 1.0.0 not found at :" + path);
        }
        
        return "fillForm";
        
    }
    
    public String setSOSMode() throws FileNotFoundException, JAXBException {
        
        capabilities     = new Object[1];
        capabilitiesFile = new File[1];
        
        //we begin to read the high lvl document
        String path          = servletContext.getRealPath("WEB-INF/SOSCapabilities1.0.0.xml");
        capabilitiesFile[0]     = new File(path);
        if (capabilitiesFile[0].exists()) {
                    
            capabilities[0] =  unmarshaller.unmarshal(new FileReader(capabilitiesFile[0]));
            fillFormFromOWS110((CapabilitiesBaseType)capabilities[0]);
              
        } else {
            logger.severe("SOS capabilities file version 1.0.0 not found at :" + path);
        }
        
        return "fillForm";
    }
    
    public String setCSWMode() throws FileNotFoundException, JAXBException {
        
        capabilities     = new Object[1];
        capabilitiesFile = new File[1];
        
        //we begin to read the high lvl document
        String path          = servletContext.getRealPath("WEB-INF/CSWCapabilities2.0.2.xml");
        capabilitiesFile[0]     = new File(path);
        if (capabilitiesFile[0].exists()) {
                    
            capabilities[0] =  unmarshaller.unmarshal(new FileReader(capabilitiesFile[0]));
            fillFormFromOWS100((net.seagis.ows.v100.CapabilitiesBaseType)capabilities[0]);
              
        } else {
            logger.severe("SOS capabilities file version 1.0.0 not found at :" + path);
        }
        
        return "fillForm"; 
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
    
}
