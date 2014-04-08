package org.constellation.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.metadata.iso.DefaultIdentifier;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.metadata.iso.citation.DefaultAddress;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.citation.DefaultCitationDate;
import org.apache.sis.metadata.iso.citation.DefaultContact;
import org.apache.sis.metadata.iso.citation.DefaultOnlineResource;
import org.apache.sis.metadata.iso.citation.DefaultResponsibleParty;
import org.apache.sis.metadata.iso.citation.DefaultTelephone;
import org.apache.sis.metadata.iso.constraint.DefaultLegalConstraints;
import org.apache.sis.metadata.iso.distribution.DefaultDigitalTransferOptions;
import org.apache.sis.metadata.iso.distribution.DefaultDistribution;
import org.apache.sis.metadata.iso.identification.AbstractIdentification;
import org.apache.sis.metadata.iso.identification.DefaultDataIdentification;
import org.apache.sis.metadata.iso.identification.DefaultKeywords;
import org.apache.sis.util.iso.DefaultInternationalString;
import org.apache.sis.util.iso.DefaultNameFactory;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.apache.sis.util.logging.Logging;
import org.constellation.dto.AccessConstraint;
import org.constellation.dto.Contact;
import org.constellation.dto.DataMetadata;
import org.constellation.dto.Service;
import org.geotoolkit.service.OperationMetadataImpl;
import org.geotoolkit.service.ServiceIdentificationImpl;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.citation.CitationDate;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.citation.OnlineResource;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.constraint.LegalConstraints;
import org.opengis.metadata.distribution.Distribution;
import org.opengis.metadata.identification.Identification;
import org.opengis.metadata.identification.Keywords;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.service.CouplingType;
import org.opengis.service.DCPList;
import org.opengis.service.OperationMetadata;
import org.opengis.util.InternationalString;
import org.opengis.util.NameFactory;

/**
 * Class which add some part on a metadata using {@link org.constellation.dto.DataMetadata}.
 *
 * @author bgarcia
 * @version 0.9
 * @since 0.9
 */
public class MetadataFeeder {

    private static final Logger LOGGER = Logging.getLogger(MetadataFeeder.class);
    
    /**
     * metadata target
     */
    private final DefaultMetadata eater;

    /**
     * Constructor
     *
     * @param eater {@link org.apache.sis.metadata.iso.DefaultMetadata} which contain new informations.
     */
    public MetadataFeeder(final DefaultMetadata eater) {
        this.eater = eater;
    }

    /**
     * merge {@link org.constellation.dto.DataMetadata} feeded on {@link org.apache.sis.metadata.iso.DefaultMetadata} eater
     *
     * @param feeded : {@link org.constellation.dto.DataMetadata} need to be merge on metadata
     */
    public void feed(final DataMetadata feeded) {
        addDateStamp(new Date());

        final Locale metadataLocale;
        final String localeMd = feeded.getLocaleMetadata();
        if (localeMd != null) {
            final String[] localeAndCountry = localeMd.split("_");
            if (localeAndCountry.length == 2) {
                metadataLocale = new Locale(localeAndCountry[0], localeAndCountry[1]);
            }else{
                metadataLocale = new Locale(localeAndCountry[0]);
            }
            addMetadataLocale(metadataLocale);
        }

        setTitle(feeded.getTitle());
        setAbstract(feeded.getAnAbstract());
        addContact(feeded.getUsername(), feeded.getOrganisationName(), feeded.getRole());

        setIdentifier(CstlMetadatas.getMetadataIdForData(feeded.getDataName()));

        final String localeData = feeded.getLocaleData();
        if (localeData != null) {
            final String[] localeAndCountry = localeData.split("_");
            Locale dataLocale;
            if(localeAndCountry.length==2){
                dataLocale = new Locale(localeAndCountry[0], localeAndCountry[1]);
            }else{
                dataLocale = new Locale(localeAndCountry[0]);
            }

            addDataLanguage(dataLocale);
        }

        addKeywords(feeded.getKeywords());
        addTopicCategory(feeded.getTopicCategory());
    }
    
    public void feedService(Service serviceInfo) {
        setAbstract(serviceInfo.getDescription());
        setTitle(serviceInfo.getName());
        addKeywords(serviceInfo.getKeywords());
        feedServiceContraint(serviceInfo.getServiceConstraints());
        feedServiceContact(serviceInfo.getServiceContact());
        
    }

    private void feedServiceContraint(final AccessConstraint constraint) {
        if (constraint != null) {
            final AbstractIdentification identification = (AbstractIdentification) getIdentification(eater);
            final DefaultLegalConstraints legalConstraint = new DefaultLegalConstraints();
            if (constraint.getAccessConstraint() != null && !constraint.getAccessConstraint().isEmpty()) {
                final SimpleInternationalString useLim = new SimpleInternationalString(constraint.getAccessConstraint());
                legalConstraint.setUseLimitations(Arrays.asList(useLim));
            }
            if (constraint.getFees() != null && !constraint.getFees().isEmpty()) {
                final SimpleInternationalString fees = new SimpleInternationalString("Fees:" + constraint.getFees());
                legalConstraint.setOtherConstraints(Arrays.asList(fees));
            }
            identification.setResourceConstraints(Arrays.asList(legalConstraint));
        }
    }
    
    private void feedServiceContact(final Contact contact) {
        if (contact != null) {
            final AbstractIdentification identification = (AbstractIdentification) getIdentification(eater);
            final DefaultResponsibleParty ct = new DefaultResponsibleParty(Role.POINT_OF_CONTACT);
            final DefaultContact cont = new DefaultContact();
            
            boolean hasAddress = false;
            final DefaultAddress adr = new DefaultAddress();
            if (contact.getAddress() != null && !contact.getAddress().isEmpty()) {
                adr.setDeliveryPoints(Arrays.asList(contact.getAddress()));
                hasAddress = true;
            }
            if (contact.getCity()!= null && !contact.getCity().isEmpty()) {
                adr.setCity(new SimpleInternationalString(contact.getCity()));
                hasAddress = true;
            }
            if (contact.getCountry()!= null && !contact.getCountry().isEmpty()) {
                adr.setCountry(new SimpleInternationalString(contact.getCountry()));
                hasAddress = true;
            }
            if (contact.getState()!= null && !contact.getState().isEmpty()) {
                adr.setAdministrativeArea(new SimpleInternationalString(contact.getState()));
                hasAddress = true;
            }
            if (contact.getZipCode()!= null && !contact.getZipCode().isEmpty()) {
                adr.setPostalCode(contact.getZipCode());
                hasAddress = true;
            }
            if (contact.getEmail()!= null && !contact.getEmail().isEmpty()) {
                adr.setElectronicMailAddresses(Arrays.asList(contact.getEmail()));
                hasAddress = true;
            }
            if (hasAddress) {
                cont.setAddress(adr);
            }
            
            if (contact.getContactInstructions() != null && !contact.getContactInstructions().isEmpty()) {
                cont.setContactInstructions(new SimpleInternationalString(contact.getContactInstructions()));
            }
            if (contact.getHoursOfService() != null && !contact.getHoursOfService().isEmpty()) {
                cont.setHoursOfService(new SimpleInternationalString(contact.getHoursOfService()));
            }
            
            final DefaultTelephone phone = new DefaultTelephone();
            boolean hasPhone = false;
            if (contact.getPhone() != null && !contact.getPhone().isEmpty()) {
                phone.setVoices(Arrays.asList(contact.getPhone()));
                hasPhone = true;
            }
            if (contact.getFax() != null && !contact.getFax().isEmpty()) {
                phone.setFacsimiles(Arrays.asList(contact.getFax()));
                hasPhone = true;
            }
            if (hasPhone) {
                cont.setPhone(phone);
            }
            
            if (contact.getUrl() != null && !contact.getUrl().isEmpty()) {
                try {
                    final DefaultOnlineResource or = new DefaultOnlineResource(new URI(contact.getUrl()));
                    cont.setOnlineResource(or);
                } catch (URISyntaxException ex) {
                    LOGGER.log(Level.WARNING, "unvalid URL in service contact", ex);
                }
            }
            ct.setContactInfo(cont);
            String fullName = "";
            if (contact.getFirstname()!= null && !contact.getFirstname().isEmpty()) {
                fullName = contact.getFirstname();
            }
            
            if (contact.getLastname()!= null && !contact.getLastname().isEmpty()) {
                fullName = fullName + " " + contact.getLastname();
            }
            
            if (!fullName.isEmpty()) {
                ct.setIndividualName(fullName);
            }
            
            if (contact.getOrganisation()!= null && !contact.getOrganisation().isEmpty()) {
                ct.setOrganisationName(new SimpleInternationalString(contact.getOrganisation()));
            }
            if (contact.getPosition()!= null && !contact.getPosition().isEmpty()) {
                ct.setPositionName(new SimpleInternationalString(contact.getPosition()));
            }
            
            identification.setPointOfContacts(Arrays.asList(ct));
        }
    }

    /**
     * Get IdentifiationInformation from metadata
     *
     * @param metadata {@link org.apache.sis.metadata.iso.DefaultMetadata} where we can found Identification
     * @return an {@link org.opengis.metadata.identification.Identification}
     */
    private Identification getIdentification(DefaultMetadata metadata) {
        if (metadata.getIdentificationInfo() == null || metadata.getIdentificationInfo().isEmpty()) {
            metadata.getIdentificationInfo().add(new DefaultDataIdentification());
        }

        return metadata.getIdentificationInfo().iterator().next();
    }
    
     private Identification getServiceIdentification(DefaultMetadata metadata) {
        if (metadata.getIdentificationInfo().isEmpty()) {
            metadata.getIdentificationInfo().add(new ServiceIdentificationImpl());
        }

        return metadata.getIdentificationInfo().iterator().next();
    }
     
    public Identification getServiceIdentification() {
        return getServiceIdentification(eater);
    }

    /**
     * Add title on metadata
     *
     * @param title title  we want add
     */
    public void setTitle(final String title) {
        final AbstractIdentification identification = (AbstractIdentification) getIdentification(eater);
        final InternationalString internationalizeTitle = new DefaultInternationalString(title);
        if (identification.getCitation() == null) {
            final DefaultCitation citation = new DefaultCitation();
            citation.setTitle(internationalizeTitle);
            identification.setCitation(citation);
        } else {
            final DefaultCitation citation = (DefaultCitation) identification.getCitation();
            citation.setTitle(internationalizeTitle);
        }
    }
    
    /**
     * Add data date on metadata
     *
     * @param date     {@link java.util.Date} need to be inserted
     * @param dateType {@link org.opengis.metadata.citation.DateType} to define the type of the date inserted
     */
    private void setCitationDate(final Date date, final DateType dateType) {
        final AbstractIdentification identification = (AbstractIdentification) getIdentification(eater);
        final DefaultCitationDate citDate = new DefaultCitationDate(date, dateType);
        if (identification.getCitation() == null) {
            final DefaultCitation citation = new DefaultCitation();
            citation.setDates(Collections.singletonList(citDate));
            identification.setCitation(citation);
        }

        final DefaultCitation citation = (DefaultCitation) identification.getCitation();
        final List<CitationDate> dates = new ArrayList<>(0);
        for (CitationDate d : citation.getDates()) {
            dates.add(d);
        }
        dates.add(citDate);
        citation.setDates(dates);
    }
    
    public void setCreationDate(final Date date) {
        final DefaultCitationDate creationDate = new DefaultCitationDate(date, DateType.CREATION);
        final AbstractIdentification ident = (AbstractIdentification)getIdentification(eater);
        DefaultCitation citation = (DefaultCitation) ident.getCitation();
        if (citation == null) {
            citation = new DefaultCitation();
            citation.setDates(Collections.singletonList(creationDate));
            ident.setCitation(citation);
            return;
        }
        //remove old creationDate
        final List<CitationDate> dates = new ArrayList<>();
        for (CitationDate cd : citation.getDates()) {
            if (DateType.CREATION.equals(cd.getDateType())) {
                dates.add(cd);
            }
        }
        citation.getDates().removeAll(dates);
        //add the new creation date
        citation.getDates().add(creationDate);
    }
    
    public void setCitationIdentifier(final String fileIdentifier) {
        final Identification id = getIdentification(eater);
        DefaultCitation citation = (DefaultCitation) id.getCitation();
        if (citation == null) {
            citation = new DefaultCitation();
            citation.setIdentifiers(Collections.singleton(new DefaultIdentifier(fileIdentifier)));
            return;
        }
        citation.setIdentifiers(Collections.singleton(new DefaultIdentifier(fileIdentifier)));
    }

    /**
     * Add abstract on metadata
     *
     * @param _abstract abstract we want add.
     */
    private void setAbstract(String _abstract) {
        final AbstractIdentification identification = (AbstractIdentification) getIdentification(eater);
        final InternationalString internationalizeAbstract;
        if (_abstract != null) {
            internationalizeAbstract = new DefaultInternationalString(_abstract);
        } else {
            internationalizeAbstract = null;
        }
        identification.setAbstract(internationalizeAbstract);
    }

    /**
     * Add keywords on metadata
     *
     * @param keywords a Keyword {@link java.util.List}
     */
    public void addKeywords(final List<String> keywords) {
        if (keywords == null) {
            return;
        }
        final List<InternationalString> kw = new ArrayList<>();
        for (String k : keywords) {
            kw.add(new SimpleInternationalString(k));
        }
        final AbstractIdentification ident = (AbstractIdentification) getIdentification(eater);
        List<Keywords> toRemove = new ArrayList<>();
        for (Keywords descKeywords : ident.getDescriptiveKeywords()) {
            if (descKeywords.getType() == null) {
                toRemove.add(descKeywords);
            }
        }
        ident.getDescriptiveKeywords().removeAll(toRemove);
        final DefaultKeywords keywordsNoType = new DefaultKeywords();
        keywordsNoType.setKeywords(kw);
        ident.getDescriptiveKeywords().add(keywordsNoType);
    }


    /**
     * Add data locale on metadata
     *
     * @param dataLocale {@link java.util.Locale} for data locale
     */
    public void addDataLanguage(final Locale dataLocale) {
        if (dataLocale == null) {
            return;
        }
        final DefaultDataIdentification identification = (DefaultDataIdentification) getIdentification(eater);
        if (identification.getLanguages() == null) {
            identification.setLanguages(Collections.singletonList(dataLocale));
        } else {
            identification.getLanguages().add(dataLocale);
        }
    }

    /**
     * Add a topicCategory on metadata
     *
     * @param topicCategoryName topic code to found right {@link org.opengis.metadata.identification.TopicCategory}
     */
    public void addTopicCategory(final String topicCategoryName) {
        if (topicCategoryName == null) {
            return;
        }
        final TopicCategory topic = TopicCategory.valueOf(topicCategoryName);
        final DefaultDataIdentification identification = (DefaultDataIdentification) getIdentification(eater);
        if (identification.getTopicCategories() == null) {
            identification.setTopicCategories(Collections.singletonList(topic));
        } else {
            identification.getTopicCategories().add(topic);
        }
    }

    /**
     * Add fileIdentifier on metadata
     *
     * @param identifier the fileIdentifier
     */
    private void setIdentifier(final String identifier) {
        eater.setFileIdentifier(identifier);
    }
    
    public String getIdentifier() {
        return eater.getFileIdentifier();
    }

    /**
     * Add dateStamp on metadata
     *
     * @param dateStamp
     */
    private void addDateStamp(final Date dateStamp) {
        eater.setDateStamp(dateStamp);
    }

    /**
     * Add locale on metadata
     *
     * @param metadataLocale
     */
    private void addMetadataLocale(final Locale metadataLocale) {
        Collection<Locale> locales = new ArrayList<>(0);
        locales.add(metadataLocale);
        eater.setLocales(locales);
    }

    /**
     * Add a contact on metadata
     *
     * @param individualName   data user name
     * @param organisationName data organisation name
     * @param userRole         user role
     */
    private void addContact(final String individualName, final String organisationName, final String userRole) {
        DefaultResponsibleParty newContact = new DefaultResponsibleParty();
        newContact.setIndividualName(individualName);
        final InternationalString internationalizeOrganisation = new DefaultInternationalString(organisationName);
        newContact.setOrganisationName(internationalizeOrganisation);
        Role currentRole = Role.valueOf(userRole);
        newContact.setRole(currentRole);
        eater.getContacts().add(newContact);
    }
    
    public String getServiceType() {
        final Collection<Identification> idents = eater.getIdentificationInfo();
        ServiceIdentificationImpl servIdent = null;
        for (Identification ident : idents) {
            if (ident instanceof ServiceIdentificationImpl) {
                servIdent = (ServiceIdentificationImpl) ident;
            }
        }
        if (servIdent != null && servIdent.getServiceType() != null) {
            return servIdent.getServiceType().toString();
        }
        return null;
    }

    public String getServiceInstanceName() {
        final Identification servIdent = getIdentification(eater);
        if (servIdent != null) {
            final Citation cit = servIdent.getCitation();
            if (cit != null && cit.getOtherCitationDetails() != null) {
                return cit.getOtherCitationDetails().toString();
            }
        }
        return null;
    }

    public void setServiceInstanceName(final String serviceInstance) {
        final Identification servIdent = getServiceIdentification();
        if (servIdent != null) {
           DefaultCitation cit = (DefaultCitation) servIdent.getCitation();
           if (cit != null) {
               cit.setOtherCitationDetails(new SimpleInternationalString(serviceInstance));
           } else {
               cit = new DefaultCitation();
               cit.setOtherCitationDetails(new SimpleInternationalString(serviceInstance));
               ((AbstractIdentification)servIdent).setCitation(cit);
           }
        } else {
            final ServiceIdentificationImpl ident = new ServiceIdentificationImpl();
            final DefaultCitation cit = new DefaultCitation();
            cit.setOtherCitationDetails(new SimpleInternationalString(serviceInstance));
            ident.setCitation(cit);
            eater.setIdentificationInfo(Collections.singletonList(ident));
        }

    }

    public void addServiceInformation(final String serviceType, final String url) {
        final Collection<Identification> idents = eater.getIdentificationInfo();
        ServiceIdentificationImpl servIdent = null;
        for (Identification ident : idents) {
            if (ident instanceof ServiceIdentificationImpl) {
                servIdent = (ServiceIdentificationImpl) ident;
            }
        }
        if (servIdent == null) {
            servIdent = new ServiceIdentificationImpl();
            eater.getIdentificationInfo().add(servIdent);
        }
        final NameFactory nameFacto = new DefaultNameFactory();
        servIdent.setCouplingType(CouplingType.LOOSE);
        servIdent.setServiceType(nameFacto.createLocalName(null, serviceType));
        servIdent.setContainsOperations(getOperation(serviceType, url));
        
        try {
            Distribution dist = eater.getDistributionInfo();
            if (dist == null) {
                dist = new DefaultDistribution();
                eater.setDistributionInfo(dist);
            }

            DefaultDigitalTransferOptions dto = new DefaultDigitalTransferOptions();
            dto.setOnLines(Collections.singleton(new DefaultOnlineResource(new URI(url))));
            addWithoutDoublon(dist.getTransferOptions(), Collections.singleton(dto));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
        }
    }
    
    private List<OperationMetadata> getOperation(final String serviceType, final String url) {
        final List<OperationMetadata> operations = new ArrayList<>();
        operations.add(buildOperation("GetCapabilities", url));

        switch (serviceType) {
            case "WMS":
                operations.add(buildOperation("GetMap", url));
                operations.add(buildOperation("GetFeatureInfo", url));
                operations.add(buildOperation("DescribeLayer", url));
                operations.add(buildOperation("GetLegendGraphic", url));
                break;
            case "WFS":
                operations.add(buildOperation("GetFeature", url));
                operations.add(buildOperation("DescribeFeatureType", url));
                operations.add(buildOperation("Transaction", url));
                break;
            case "WCS":
                operations.add(buildOperation("DescribeCoverage", url));
                operations.add(buildOperation("GetCoverage", url));
                break;
            case "WMTS":
                operations.add(buildOperation("GetTile", url));
                operations.add(buildOperation("GetFeatureInfo", url));
                break;
            case "CSW":
                operations.add(buildOperation("GetRecords", url));
                operations.add(buildOperation("GetRecordById", url));
                operations.add(buildOperation("DescribeRecord", url));
                operations.add(buildOperation("GetDomain", url));
                operations.add(buildOperation("Transaction", url));
                operations.add(buildOperation("Harvest", url));
                break;
            case "SOS":
                operations.add(buildOperation("GetObservation", url));
                operations.add(buildOperation("GetObservationById", url));
                operations.add(buildOperation("DescribeSensor", url));
                operations.add(buildOperation("GetFeatureOfInterest", url));
                operations.add(buildOperation("InsertObservation", url));
                operations.add(buildOperation("InsertSensor", url));
                operations.add(buildOperation("DeleteSensor", url));
                operations.add(buildOperation("InsertResult", url));
                operations.add(buildOperation("InsertResultTemplate", url));
                operations.add(buildOperation("GetResultTemplate", url));
                operations.add(buildOperation("GetFeatureOfInterestTime", url));
                break;
        }
        // TODO other service
        return operations;
    }
    
    private OperationMetadata buildOperation(final String operationName, final String url) {
        final OperationMetadataImpl op = new OperationMetadataImpl(operationName);
        op.setDCP(DCPList.WEBSERVICES);
        try {
            op.setConnectPoint(Arrays.asList((OnlineResource)new DefaultOnlineResource(new URI(url))));
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.WARNING, "unvalid URL:" + url, ex);
        }
        return op;
    }
    
    /**
     * Copy the elements of a source into a destination collection, without adding the elements
     * which are already present in the destination collection.
     * @param destination The collection to copy data to.
     * @param source The collection to get data from.
     */
    public static void addWithoutDoublon(Collection destination, Collection source) {
        if (source == null || source.isEmpty()) {
            return;
        }
       
        if (destination.isEmpty()) {
            destination.addAll(source);
        } else {
            for (Object object : source) {
                if (!destination.contains(object)) {
                    destination.add(object);
                }
            }
        }
    }
}
