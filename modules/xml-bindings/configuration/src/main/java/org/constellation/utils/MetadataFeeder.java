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
package org.constellation.utils;

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
import org.apache.sis.metadata.iso.identification.DefaultOperationMetadata;
import org.apache.sis.util.iso.DefaultInternationalString;
import org.apache.sis.util.iso.DefaultNameFactory;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.IdentifierSpace;
import org.constellation.dto.AccessConstraint;
import org.constellation.dto.Contact;
import org.constellation.dto.DataMetadata;
import org.constellation.dto.Details;
import org.apache.sis.metadata.iso.identification.DefaultServiceIdentification;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.citation.CitationDate;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.citation.OnlineResource;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.content.AttributeGroup;
import org.opengis.metadata.content.ContentInformation;
import org.opengis.metadata.content.CoverageDescription;
import org.opengis.metadata.content.RangeDimension;
import org.opengis.metadata.distribution.DigitalTransferOptions;
import org.opengis.metadata.distribution.Distribution;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicDescription;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.identification.DataIdentification;
import org.opengis.metadata.identification.Identification;
import org.opengis.metadata.identification.Keywords;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.metadata.identification.CouplingType;
import org.opengis.metadata.identification.DistributedComputingPlatform;
import org.opengis.metadata.identification.OperationMetadata;
import org.opengis.util.InternationalString;
import org.opengis.util.NameFactory;

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


/**
 * Class which add some part on a metadata using {@link org.constellation.dto.DataMetadata}.
 *
 * @author bgarcia
 * @author Cédric Briançon (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class MetadataFeeder {

    private static final Logger LOGGER = Logging.getLogger(MetadataFeeder.class);

    /**
     * metadata target
     */
    protected final DefaultMetadata eater;

    /**
     * Constructor
     *
     * @param eater {@link org.apache.sis.metadata.iso.DefaultMetadata} which contain new informations.
     */
    public MetadataFeeder(final DefaultMetadata eater) {
        this.eater = eater;
    }

    public void feedService(Details serviceInfo) {
        setAbstract(serviceInfo.getDescription());
        setTitle(serviceInfo.getName());
        setKeywordsNoType(serviceInfo.getKeywords());
        feedServiceContraint(serviceInfo.getServiceConstraints());
        feedServiceContact(serviceInfo.getServiceContact());

    }

    protected void feedServiceContraint(final AccessConstraint constraint) {
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

    protected void feedServiceContact(final Contact contact) {
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
     * merge {@link org.constellation.dto.DataMetadata} feeded on {@link org.apache.sis.metadata.iso.DefaultMetadata} eater
     *
     * @param feeded : {@link org.constellation.dto.DataMetadata} need to be merge on metadata
     */
    public void feed(final DataMetadata feeded) {

        final Locale metadataLocale;
        final String localeMd = feeded.getLocaleMetadata();
        if (localeMd != null) {
            final String[] localeAndCountry = localeMd.split("_");
            if (localeAndCountry.length == 2) {
                metadataLocale = new Locale(localeAndCountry[0], localeAndCountry[1]);
            }else{
                metadataLocale = new Locale(localeAndCountry[0]);
            }
            setMetadataLocale(metadataLocale);
        }

        if (feeded.getDate() != null && feeded.getDateType() != null) {
            final DateType dt = DateType.valueOf(feeded.getDateType());
            setCitationDate(feeded.getDate(), dt);
        }

        setTitle(feeded.getTitle());
        setAbstract(feeded.getAnAbstract());
        setContact(feeded.getUsername(), feeded.getOrganisationName(), feeded.getRole());

        final String localeData = feeded.getLocaleData();
        if (localeData != null) {
            final String[] localeAndCountry = localeData.split("_");
            Locale dataLocale;
            if(localeAndCountry.length==2){
                dataLocale = new Locale(localeAndCountry[0], localeAndCountry[1]);
            }else{
                dataLocale = new Locale(localeAndCountry[0]);
            }

            setDataLanguage(dataLocale);
        }

        setKeywordsNoType(feeded.getKeywords());
        setTopicCategory(feeded.getTopicCategory());
    }

    /**
     * Get IdentifiationInformation from metadata
     *
     * @param metadata {@link org.apache.sis.metadata.iso.DefaultMetadata} where we can found Identification
     * @return an {@link org.opengis.metadata.identification.Identification}
     */
    protected Identification getIdentification(DefaultMetadata metadata) {
        if (metadata.getIdentificationInfo() == null || metadata.getIdentificationInfo().isEmpty()) {
            metadata.getIdentificationInfo().add(new DefaultDataIdentification());
        }

        return metadata.getIdentificationInfo().iterator().next();
    }

     protected Identification getServiceIdentification(DefaultMetadata metadata) {
        if (metadata.getIdentificationInfo().isEmpty()) {
            metadata.getIdentificationInfo().add(new DefaultServiceIdentification());
        }

        return metadata.getIdentificationInfo().iterator().next();
    }

    public Identification getServiceIdentification() {
        return getServiceIdentification(eater);
    }

    public String getTitle() {
        final AbstractIdentification identification = (AbstractIdentification) getIdentification(eater);
        if (identification != null && identification.getCitation() != null) {
            final Citation citation = identification.getCitation();
            if (citation != null) {
                final InternationalString is = citation.getTitle();
                if (is != null) {
                    return is.toString();
                }
            }
        }
        return null;
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

    protected CitationDate getCitationDate() {
        final AbstractIdentification identification = (AbstractIdentification) getIdentification(eater);
        if (identification.getCitation() != null) {
            final Citation citation = identification.getCitation();
            if (!citation.getDates().isEmpty()) {
                return citation.getDates().iterator().next();
            }
        }
        return null;
    }

    /**
     * Add data date on metadata
     *
     * @param date     {@link java.util.Date} need to be inserted
     * @param dateType {@link org.opengis.metadata.citation.DateType} to define the type of the date inserted
     */
    protected void setCitationDate(final Date date, final DateType dateType) {
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
        final DefaultDataIdentification id = (DefaultDataIdentification) getIdentification(eater);
        DefaultCitation citation = (DefaultCitation) id.getCitation();
        if (citation == null) {
            citation = new DefaultCitation();
            citation.setIdentifiers(Collections.singleton(new DefaultIdentifier(fileIdentifier)));
            id.setCitation(citation);
            return;
        }
        citation.setIdentifiers(Collections.singleton(new DefaultIdentifier(fileIdentifier)));
    }

    public String getAbstract() {
        final AbstractIdentification identification = (AbstractIdentification) getIdentification(eater);
        final InternationalString internationalizeAbstract = identification.getAbstract();
        if (internationalizeAbstract != null) {
            return internationalizeAbstract.toString();
        }
        return null;
    }

    /**
     * Add abstract on metadata
     *
     * @param _abstract abstract we want add.
     */
    protected void setAbstract(String _abstract) {
        final AbstractIdentification identification = (AbstractIdentification) getIdentification(eater);
        final InternationalString internationalizeAbstract;
        if (_abstract != null) {
            internationalizeAbstract = new DefaultInternationalString(_abstract);
        } else {
            internationalizeAbstract = null;
        }
        identification.setAbstract(internationalizeAbstract);
    }

    public final List<String> getKeywordsNoType() {
        final List<String> keywords = new ArrayList<>();
        final AbstractIdentification ident = (AbstractIdentification) getIdentification(eater);
        for (Keywords descKeywords : ident.getDescriptiveKeywords()) {
            if (descKeywords.getType() == null) {
                for (InternationalString is : descKeywords.getKeywords()) {
                    keywords.add(is.toString());
                }
            }
        }
        return keywords;
    }

    /**
     * Add keywords on metadata
     *
     * @param keywords a Keyword {@link java.util.List}
     */
    public void setKeywordsNoType(final List<String> keywords) {
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

    protected String getDataLanguage() {
        final DefaultDataIdentification identification = (DefaultDataIdentification) getIdentification(eater);
        if (!identification.getLanguages().isEmpty()) {
            return identification.getLanguages().iterator().next().getLanguage();
        }
        return null;
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

    public void setDataLanguage(final Locale dataLocale) {
        if (dataLocale == null) {
            return;
        }
        final DefaultDataIdentification identification = (DefaultDataIdentification) getIdentification(eater);
        if (identification.getLanguages() == null) {
            identification.setLanguages(Collections.singletonList(dataLocale));
        } else {
            identification.getLanguages().clear();
            identification.getLanguages().add(dataLocale);
        }
    }

    public String getTopicCategory() {
        final DefaultDataIdentification identification = (DefaultDataIdentification) getIdentification(eater);
        if (!identification.getTopicCategories().isEmpty()) {
            return identification.getTopicCategories().iterator().next().identifier();
        }
        return null;
    }

    public List<String> getAllTopicCategory() {
        List<String> result = new ArrayList<>();
        final DefaultDataIdentification identification = (DefaultDataIdentification) getIdentification(eater);
        if (!identification.getTopicCategories().isEmpty()) {
            final Collection<TopicCategory> topicCategories = identification.getTopicCategories();//.iterator().next().identifier();
            for (TopicCategory topicCategory : topicCategories){
                result.add(topicCategory.identifier());
            }
        }

        return result;
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

    public void setTopicCategory(final String topicCategoryName) {
        if (topicCategoryName == null) {
            return;
        }
        final TopicCategory topic = TopicCategory.valueOf(topicCategoryName);
        final DefaultDataIdentification identification = (DefaultDataIdentification) getIdentification(eater);
        if (identification.getTopicCategories() == null) {
            identification.setTopicCategories(Collections.singletonList(topic));
        } else {
            identification.getTopicCategories().clear();
            identification.getTopicCategories().add(topic);
        }
    }

    /**
     * Add fileIdentifier on metadata
     *
     * @param identifier the fileIdentifier
     */
    protected void setIdentifier(final String identifier) {
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
    protected void addDateStamp(final Date dateStamp) {
        eater.setDateStamp(dateStamp);
    }

    protected String getMetadataLocale() {
        if (!eater.getLocales().isEmpty()) {
            return eater.getLocales().iterator().next().getLanguage();
        }
        return null;
    }

    /**
     * Add locale on metadata
     *
     * @param metadataLocale
     */
    protected void setMetadataLocale(final Locale metadataLocale) {
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
    protected void addContact(final String individualName, final String organisationName, final String userRole) {
        DefaultResponsibleParty newContact = new DefaultResponsibleParty();
        newContact.setIndividualName(individualName);
        final InternationalString internationalizeOrganisation = new DefaultInternationalString(organisationName);
        newContact.setOrganisationName(internationalizeOrganisation);
        Role currentRole = Role.valueOf(userRole);
        newContact.setRole(currentRole);
        eater.getContacts().add(newContact);
    }

    protected void setContact(final String individualName, final String organisationName, final String userRole) {
        DefaultResponsibleParty newContact = new DefaultResponsibleParty();
        newContact.setIndividualName(individualName);
        final InternationalString internationalizeOrganisation = new DefaultInternationalString(organisationName);
        newContact.setOrganisationName(internationalizeOrganisation);
        Role currentRole = Role.valueOf(userRole);
        newContact.setRole(currentRole);
        eater.getContacts().clear();
        eater.getContacts().add(newContact);
    }

    protected String getOrganisationName() {
        if (!eater.getContacts().isEmpty()) {
            final InternationalString is = eater.getContacts().iterator().next().getOrganisationName();
            if (is != null) {
                return is.toString();
            }
        }
        return null;
    }

    protected String getIndividualName() {
        if (!eater.getContacts().isEmpty()) {
            return eater.getContacts().iterator().next().getIndividualName();
        }
        return null;
    }

    protected String getRole() {
        if (!eater.getContacts().isEmpty()) {
            final Role is = eater.getContacts().iterator().next().getRole();
            if (is != null) {
                return is.name();
            }
        }
        return null;
    }

    public String getServiceType() {
        final Collection<Identification> idents = eater.getIdentificationInfo();
        DefaultServiceIdentification servIdent = null;
        for (Identification ident : idents) {
            if (ident instanceof DefaultServiceIdentification) {
                servIdent = (DefaultServiceIdentification) ident;
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
               cit.setOtherCitationDetails(Collections.singleton(new SimpleInternationalString(serviceInstance)));
           } else {
               cit = new DefaultCitation();
               cit.setOtherCitationDetails(Collections.singleton(new SimpleInternationalString(serviceInstance)));
               ((AbstractIdentification)servIdent).setCitation(cit);
           }
        } else {
            final DefaultServiceIdentification ident = new DefaultServiceIdentification();
            final DefaultCitation cit = new DefaultCitation();
            cit.setOtherCitationDetails(Collections.singleton(new SimpleInternationalString(serviceInstance)));
            ident.setCitation(cit);
            eater.setIdentificationInfo(Collections.singletonList(ident));
        }

    }

    public void addServiceInformation(final String serviceType, final String url) {
        final Collection<Identification> idents = eater.getIdentificationInfo();
        DefaultServiceIdentification servIdent = null;
        for (Identification ident : idents) {
            if (ident instanceof DefaultServiceIdentification) {
                servIdent = (DefaultServiceIdentification) ident;
            }
        }
        if (servIdent == null) {
            servIdent = new DefaultServiceIdentification();
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
        } catch (URISyntaxException e) {
            LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
        }
    }

    public void updateServiceURL(final String url) {
        final Collection<Identification> idents = eater.getIdentificationInfo();
        DefaultServiceIdentification servIdent = null;
        for (Identification ident : idents) {
            if (ident instanceof DefaultServiceIdentification) {
                servIdent = (DefaultServiceIdentification) ident;
            }
        }
        if (servIdent != null) {
            for (OperationMetadata om : servIdent.getContainsOperations()) {
                for (OnlineResource or : om.getConnectPoints()) {
                    final DefaultOnlineResource resource = (DefaultOnlineResource) or;
                    try {
                        resource.setLinkage(new URI(url));
                    } catch (URISyntaxException ex) {
                        LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                    }
                }
            }
        }
        Distribution dist = eater.getDistributionInfo();
        if (dist != null) {
            for (DigitalTransferOptions dto : dist.getTransferOptions()) {
                for (OnlineResource or : dto.getOnLines()) {
                    final DefaultOnlineResource resource = (DefaultOnlineResource) or;
                    try {
                        resource.setLinkage(new URI(url));
                    } catch (URISyntaxException ex) {
                        LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                    }
                }
            }
        }
    }

    protected List<OperationMetadata> getOperation(final String serviceType, final String url) {
        final List<OperationMetadata> operations = new ArrayList<>();
        OnlineResource resource;
        try {
            resource = new DefaultOnlineResource(new URI(url));
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.WARNING, "unvalid URL:" + url, ex);  // TODO: NON!!!!!
            resource = null;
        }
        operations.add(buildOperation("GetCapabilities", resource));

        switch (serviceType) {
            case "WMS":
                operations.add(buildOperation("GetMap", resource));
                operations.add(buildOperation("GetFeatureInfo", resource));
                operations.add(buildOperation("DescribeLayer", resource));
                operations.add(buildOperation("GetLegendGraphic", resource));
                break;
            case "WFS":
                operations.add(buildOperation("GetFeature", resource));
                operations.add(buildOperation("DescribeFeatureType", resource));
                operations.add(buildOperation("Transaction", resource));
                break;
            case "WCS":
                operations.add(buildOperation("DescribeCoverage", resource));
                operations.add(buildOperation("GetCoverage", resource));
                break;
            case "WMTS":
                operations.add(buildOperation("GetTile", resource));
                operations.add(buildOperation("GetFeatureInfo", resource));
                break;
            case "CSW":
                operations.add(buildOperation("GetRecords", resource));
                operations.add(buildOperation("GetRecordById", resource));
                operations.add(buildOperation("DescribeRecord", resource));
                operations.add(buildOperation("GetDomain", resource));
                operations.add(buildOperation("Transaction", resource));
                operations.add(buildOperation("Harvest", resource));
                break;
            case "SOS":
                operations.add(buildOperation("GetObservation", resource));
                operations.add(buildOperation("GetObservationById", resource));
                operations.add(buildOperation("DescribeSensor", resource));
                operations.add(buildOperation("GetFeatureOfInterest", resource));
                operations.add(buildOperation("InsertObservation", resource));
                operations.add(buildOperation("InsertSensor", resource));
                operations.add(buildOperation("DeleteSensor", resource));
                operations.add(buildOperation("InsertResult", resource));
                operations.add(buildOperation("InsertResultTemplate", resource));
                operations.add(buildOperation("GetResultTemplate", resource));
                operations.add(buildOperation("GetFeatureOfInterestTime", resource));
                break;
        }
        // TODO other service
        return operations;
    }

    protected OperationMetadata buildOperation(final String operationName, final OnlineResource url) {
        return new DefaultOperationMetadata(operationName, DistributedComputingPlatform.WEB_SERVICES, url);
    }

    public void setServiceMetadataIdForData(final List<String> layerIds) {
        final Collection<Identification> idents = eater.getIdentificationInfo();
        DefaultServiceIdentification servIdent = null;
        for (Identification ident : idents) {
            if (ident instanceof DefaultServiceIdentification) {
                servIdent = (DefaultServiceIdentification) ident;
            }
        }
        if (servIdent == null) {
            servIdent = new DefaultServiceIdentification();
            eater.getIdentificationInfo().add(servIdent);
        }

        final List<DataIdentification> resources = new ArrayList<>();
        for (String layerId : layerIds) {
            final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
            dataIdent.getIdentifierMap().put(IdentifierSpace.HREF, layerId);
            resources.add(dataIdent);
        }
        servIdent.setOperatesOn(resources);
    }

    public void addServiceMetadataIdForData(final String layerId) {
        final Collection<Identification> idents = eater.getIdentificationInfo();
        DefaultServiceIdentification servIdent = null;
        for (Identification ident : idents) {
            if (ident instanceof DefaultServiceIdentification) {
                servIdent = (DefaultServiceIdentification) ident;
            }
        }
        if (servIdent == null) {
            servIdent = new DefaultServiceIdentification();
            eater.getIdentificationInfo().add(servIdent);
        }

        final Collection<DataIdentification> resources = servIdent.getOperatesOn();
        for (DataIdentification did : resources) {
            final DefaultDataIdentification dataIdent = (DefaultDataIdentification) did;
            if (dataIdent.getIdentifierMap().getSpecialized(IdentifierSpace.HREF).equals(layerId)) {
                return;
            }
        }
        // add new resouce
        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        dataIdent.getIdentifierMap().put(IdentifierSpace.HREF, layerId);
        servIdent.getOperatesOn().add(dataIdent);
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

    public DataMetadata extractDataMetadata() {
        final DataMetadata result = new DataMetadata();

        result.setAnAbstract(getAbstract());

        final CitationDate date = getCitationDate();
        if (date != null) {
            result.setDate(date.getDate());
            result.setDateType(date.getDateType().name());
        }

        result.setKeywords(getKeywordsNoType());
        result.setLocaleData(getDataLanguage());
        result.setLocaleMetadata(getMetadataLocale());
        result.setOrganisationName(getOrganisationName());
        result.setRole(getRole());
        result.setTitle(getTitle());
        result.setTopicCategory(getTopicCategory());
        result.setUsername(getIndividualName());

        return result;
    }


    public List<String> getAllSequenceIdentifier() {
        List<String> result = new ArrayList<>();
        for (ContentInformation contentInformation : eater.getContentInfo()){
            if (contentInformation != null && contentInformation instanceof  CoverageDescription){
                for (AttributeGroup attributeGroup : ((CoverageDescription) contentInformation).getAttributeGroups()){
                    if (attributeGroup != null) {
                        for (RangeDimension rangeDimension : attributeGroup.getAttributes()) {
                            if (rangeDimension != null && rangeDimension.getSequenceIdentifier() != null) {
                                result.add(rangeDimension.getSequenceIdentifier().toString());
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public String getProcessingLevel() {
        final Identification identification = getIdentification(eater);
        if (identification !=null && identification.getProcessingLevel() != null) {
            return identification.getProcessingLevel().toString();
        }
        return null;
    }

    public List<String> getAllGeographicIdentifier() {
        List<String> result = new ArrayList<>();
        final Identification identification = getIdentification(eater);
        if (identification != null) {
            for (Extent extent : identification.getExtents()) {
                if (extent != null) {
                    for (GeographicExtent geographicExtent : extent.getGeographicElements()) {
                        if (geographicExtent != null && geographicExtent instanceof GeographicDescription &&
                                ((GeographicDescription) geographicExtent).getGeographicIdentifier() != null) {
                            result.add(((GeographicDescription) geographicExtent).getGeographicIdentifier().toString());
                        }
                    }
                }
            }
            return result;
        } else {
            return null;
        }
    }
}
