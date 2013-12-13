package org.constellation.utils;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.citation.DefaultCitationDate;
import org.apache.sis.metadata.iso.citation.DefaultResponsibleParty;
import org.apache.sis.metadata.iso.identification.AbstractIdentification;
import org.apache.sis.metadata.iso.identification.DefaultDataIdentification;
import org.apache.sis.metadata.iso.identification.DefaultKeywords;
import org.apache.sis.util.iso.DefaultInternationalString;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.constellation.dto.DataMetadata;
import org.opengis.metadata.citation.CitationDate;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.identification.Identification;
import org.opengis.metadata.identification.Keywords;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.util.InternationalString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Class which add some part on a metadata using {@link org.constellation.dto.DataMetadata}.
 *
 * @author bgarcia
 * @version 0.9
 * @since 0.9
 */
public class MetadataFeeder {

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

        String[] localeAndCountry = feeded.getLocaleMetadata().split("_");
        final Locale metadataLocale = new Locale(localeAndCountry[0], localeAndCountry[1]);
        addMetadataLocale(metadataLocale);

        addTitle(feeded.getTitle());
        addAbstract(feeded.getAnAbstract());
        addContact(feeded.getUsername(), feeded.getOrganisationName(), feeded.getRole());

        String uuid = UUID.randomUUID().toString();
        addFileIdentifier(uuid);

        localeAndCountry = feeded.getLocaleData().split("_");
        Locale dataLocale;
        if(localeAndCountry.length==2){
            dataLocale = new Locale(localeAndCountry[0], localeAndCountry[1]);
        }else{
            dataLocale = new Locale(localeAndCountry[0]);
        }

        addDataLanguage(dataLocale);
        addKeywords(feeded.getKeywords());
        addTopicCategory(feeded.getTopicCategory());


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

    /**
     * Add title on metadata
     *
     * @param title title  we want add
     */
    private void addTitle(final String title) {
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
    private void addCitationDate(final Date date, final DateType dateType) {
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

    /**
     * Add abstract on metadata
     *
     * @param _abstract abstract we want add.
     */
    private void addAbstract(String _abstract) {
        final AbstractIdentification identification = (AbstractIdentification) getIdentification(eater);
        final InternationalString internationalizeAbstract = new DefaultInternationalString(_abstract);
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
    private void addFileIdentifier(final String identifier) {
        eater.setFileIdentifier(identifier);
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

}
