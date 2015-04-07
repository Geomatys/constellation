package org.constellation.rest.api;

import com.google.common.base.Optional;
import org.apache.sis.util.logging.Logging;
import org.constellation.business.IMetadataBusiness;
import org.constellation.engine.register.MetadataComplete;
import org.constellation.engine.register.jooq.tables.pojos.CstlUser;
import org.constellation.engine.register.jooq.tables.pojos.Metadata;
import org.constellation.engine.register.jooq.tables.pojos.MetadataBbox;
import org.constellation.engine.register.repository.MetadataRepository;
import org.constellation.engine.register.repository.UserRepository;
import org.constellation.model.metadata.Filter;
import org.constellation.model.metadata.MetadataBrief;
import org.constellation.model.metadata.MetadataLightBrief;
import org.constellation.model.metadata.Page;
import org.constellation.model.metadata.PagedSearch;
import org.constellation.model.metadata.Profile;
import org.constellation.model.metadata.Sort;
import org.geotoolkit.util.FileUtilities;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.ZipOutputStream;

import static org.apache.commons.lang3.StringUtils.isBlank;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.constellation.configuration.ConfigurationException;
import org.constellation.json.metadata.v2.Template;

/**
 * @author Mehdi Sidhoum (Geomatys).
 */
@Component
@Path("/1/metadata")
@Consumes("application/json")
@Produces("application/json")
public class MetadataRest {

    private static final Logger LOGGER = Logging.getLogger(MetadataRest.class);

    /**
     * Inject metadata repository
     */
    @Inject
    private MetadataRepository metadataRepository;

    /**
     * Inject metadata business
     */
    @Inject
    private IMetadataBusiness metadataBusiness;

    /**
     * Injected user repository.
     */
    @Inject
    private UserRepository userRepository;

    public MetadataRest() {

    }

    @GET
    @Path("/profiles")
    public List<Profile> getProfilesList() {
        final List<Profile> result = new ArrayList<>();
        final Map<String,Integer> map = metadataRepository.getProfilesCount();
        if(map!=null){
            for(final Map.Entry<String,Integer> entry : map.entrySet()){
                result.add(new Profile(entry.getKey(),entry.getValue()));
            }
        }
        return result;
    }

    /**
     * TODO to be removed, used only to fill metadata table to simulate several rows for dashboard page.
     * @param count number of mocked metadata to create
     */
    @GET
    @Path("/mockup/{count}")
    public Response mockup(@PathParam("count") final Integer count) {
        final List<String> profiles = new ArrayList<>();
        profiles.add("AccessProgram");
        profiles.add("Guideline");
        profiles.add("MarketingToolkit");
        profiles.add("Organisation");
        profiles.add("OtherCBResource");
        profiles.add("OtherDocument");
        profiles.add("QuickWin");
        profiles.add("RawDataCatalog");
        profiles.add("RawEOProduct");
        profiles.add("Software");
        profiles.add("ScientificPublication");
        for(int i=0;i<count;i++) {
            final Metadata metadata = new Metadata();
            metadata.setMetadataId(UUID.randomUUID().toString());
            metadata.setMetadataIso("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:fra=\"http://www.cnig.gouv.fr/2005/fra\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:srv=\"http://www.isotc211.org/2005/srv\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:gts=\"http://www.isotc211.org/2005/gts\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:gmi=\"http://www.isotc211.org/2005/gmi\" xmlns:gmx=\"http://www.isotc211.org/2005/gmx\"><gmd:fileIdentifier><gco:CharacterString>" + UUID.randomUUID().toString() + "</gco:CharacterString></gmd:fileIdentifier><gmd:language><gmd:LanguageCode codeList=\"http://schemas.opengis.net/iso/19139/20070417/resources/Codelist/gmxCodelists.xml#LanguageCode\" codeListValue=\"eng\" codeSpace=\"eng\">English</gmd:LanguageCode></gmd:language><gmd:characterSet><gmd:MD_CharacterSetCode codeList=\"http://schemas.opengis.net/iso/19139/20070417/resources/Codelist/gmxCodelists.xml#MD_CharacterSetCode\" codeListValue=\"utf8\">UTF-8</gmd:MD_CharacterSetCode></gmd:characterSet><gmd:contact><gmd:CI_ResponsibleParty><gmd:individualName><gco:CharacterString>Geomatys User</gco:CharacterString></gmd:individualName><gmd:contactInfo><gmd:CI_Contact><gmd:address><gmd:CI_Address><gmd:electronicMailAddress><gco:CharacterString>test@test.com</gco:CharacterString></gmd:electronicMailAddress></gmd:CI_Address></gmd:address></gmd:CI_Contact></gmd:contactInfo></gmd:CI_ResponsibleParty></gmd:contact><gmd:dateStamp><gco:DateTime>2015-03-26T11:26:00+01:00</gco:DateTime></gmd:dateStamp><gmd:metadataStandardName><gco:CharacterString>ISO19115</gco:CharacterString></gmd:metadataStandardName><gmd:metadataStandardVersion><gco:CharacterString>2003/Cor.1:2006</gco:CharacterString></gmd:metadataStandardVersion><gmd:identificationInfo><gmd:MD_DataIdentification><gmd:citation><gmd:CI_Citation><gmd:title><gco:CharacterString>holuhraun_oli_2014249_653_geo</gco:CharacterString></gmd:title></gmd:CI_Citation></gmd:citation><gmd:extent><gmd:EX_Extent><gmd:geographicElement><gmd:EX_GeographicBoundingBox><gmd:extentTypeCode><gco:Boolean>true</gco:Boolean></gmd:extentTypeCode><gmd:westBoundLongitude><gco:Decimal>-17.46653783459498</gco:Decimal></gmd:westBoundLongitude><gmd:eastBoundLongitude><gco:Decimal>-15.920336221182728</gco:Decimal></gmd:eastBoundLongitude><gmd:southBoundLatitude><gco:Decimal>64.56546890901124</gco:Decimal></gmd:southBoundLatitude><gmd:northBoundLatitude><gco:Decimal>65.06696897623314</gco:Decimal></gmd:northBoundLatitude></gmd:EX_GeographicBoundingBox></gmd:geographicElement></gmd:EX_Extent></gmd:extent></gmd:MD_DataIdentification></gmd:identificationInfo></gmd:MD_Metadata>");
            metadata.setDataId(null);
            metadata.setMdCompletion((int) (Math.random() * 100));
            metadata.setOwner(Math.random() < 0.5 ? 1 : 2);
            metadata.setDatestamp(System.currentTimeMillis());
            metadata.setDateCreation(System.currentTimeMillis() - (1000 * 60 * 60 * 24));
            metadata.setTitle("mocked_" + UUID.randomUUID().toString());
            metadata.setResume("Le Lorem Ipsum est simplement du faux texte employé dans la composition et la mise en page avant impression. Le Lorem Ipsum est le faux texte standard de l'imprimerie depuis les années 1500, quand un peintre anonyme assembla ensemble des morceaux de texte pour réaliser un livre spécimen de polices de texte. Il n'a pas fait que survivre cinq siècles, mais s'est aussi adapté à la bureautique informatique, sans que son contenu n'en soit modifié. Il a été popularisé dans les années 1960 grâce à la vente de feuilles Letraset contenant des passages du Lorem Ipsum, et, plus récemment, par son inclusion dans des applications de mise en page de texte, comme Aldus PageMaker.");
            metadata.setProfile(profiles.get( (int)(Math.random() * 10)  ));
            metadata.setParentIdentifier(null);
            metadata.setLevel(Math.random() < 0.5 ? "NONE" : "ELEMENTARY");
            metadata.setIsValidated(Math.random() < 0.5);
            metadata.setIsPublished(metadata.getIsValidated() && (Math.random() < 0.5));
            metadataRepository.create(new MetadataComplete(metadata, new ArrayList<MetadataBbox>()));
        }
        return Response.ok("Mockup metadata successfully!").build();
    }

    @POST
    @Path("/search")
    public Page<MetadataBrief> search(final PagedSearch pagedSearch,@Context HttpServletRequest req) {
        List<MetadataBrief> results = new ArrayList<>();

        //filters
        final Map<String,Object> filterMap = prepareFilters(pagedSearch,req);

        //sorting
        final Sort sort = pagedSearch.getSort();
        Map.Entry<String,String> sortEntry = null;
        if (sort != null) {
            sortEntry = new AbstractMap.SimpleEntry<>(sort.getField(),sort.getOrder().toString());
        }

        //pagination
        final int pageNumber = pagedSearch.getPage();
        final int rowsPerPage = pagedSearch.getSize();

        final Map<Integer,List> result = metadataRepository.filterAndGet(filterMap,sortEntry,pageNumber,rowsPerPage);
        final Map.Entry<Integer,List> entry = result.entrySet().iterator().next();
        final int total = entry.getKey();

        final List metadataList = entry.getValue();
        if(metadataList != null) {
            for(final Object candidat : metadataList) {
                if(candidat instanceof Metadata) {
                    final Metadata md = (Metadata) candidat;
                    final MetadataBrief mdb = convertToMetadataBrief(md);
                    results.add(mdb);
                }
            }
        }

        // Build and return the content list of page.
        return new Page<MetadataBrief>()
                .setNumber(pageNumber)
                .setSize(rowsPerPage)
                .setContent(results)
                .setTotal(total);

    }

    private Map<String,Object> prepareFilters(final PagedSearch pagedSearch, final HttpServletRequest req) {
        List<Filter> filters = pagedSearch.getFilters();
        final String searchTerm = pagedSearch.getText();
        if(!isBlank(searchTerm)){
            final Filter f = new Filter("term",searchTerm);
            if(filters != null){
                filters.add(f);
            }else {
                filters = Arrays.asList(f);
            }
        }
        final Map<String,Object> filterMap = new HashMap<>();
        if(filters != null) {
            for(final Filter f : filters) {
                if("owner".equals(f.getField())) {
                    String login = f.getValue();
                    if("_all".equals(login)) {
                        continue; //no need to filter on owner field if we ask all users
                    }
                    if("_me".equals(login)) {
                        //get user login
                        login = req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : null;
                    }
                    final Optional<CstlUser> optUser = userRepository.findOne(login);
                    if(optUser!=null && optUser.isPresent()){
                        final CstlUser user = optUser.get();
                        if(user != null){
                            filterMap.put(f.getField(),user.getId());
                        }
                    }
                }else if("profile".equals(f.getField())) {
                    if(! "_all".equals(f.getValue())){
                        filterMap.put(f.getField(),f.getValue());
                    }
                } else if("level".equals(f.getField())) {
                    final String value = f.getValue();
                    if("_all".equals(value)){
                        continue; //no need to filter on published if we ask all
                    }
                    filterMap.put(f.getField(),f.getValue());

                } else if("validated".equals(f.getField())) {
                    final String value = f.getValue();
                    if("_all".equals(value)){
                        continue; //no need to filter on validity if we ask all
                    }
                    if("true".equals(value)) {
                        filterMap.put(f.getField(),Boolean.TRUE);
                    }else {
                        filterMap.put(f.getField(),Boolean.FALSE);
                    }
                }else if("published".equals(f.getField())) {
                    final String value = f.getValue();
                    if("_all".equals(value)){
                        continue; //no need to filter on published if we ask all
                    }
                    if("true".equals(value)) {
                        filterMap.put(f.getField(),Boolean.TRUE);
                    }else {
                        filterMap.put(f.getField(),Boolean.FALSE);
                    }
                } else {
                    filterMap.put(f.getField(),f.getValue());
                }
            }
        }
        return filterMap;
    }

    @POST
    @Path("/searchIds")
    public Map searchIds(final PagedSearch pagedSearch,@Context HttpServletRequest req) {
        final List<MetadataLightBrief> list = new ArrayList<>();
        final Map<String,Object> filterMap = prepareFilters(pagedSearch,req);
        final Map<Integer,String> map = metadataRepository.filterAndGetWithoutPagination(filterMap);
        if(map!=null){
            for(final Map.Entry<Integer,String> entry : map.entrySet()){
                list.add(new MetadataLightBrief(entry.getKey(),entry.getValue()));
            }
        }
        final Map<String,Object> result = new HashMap<>();
        result.put("total",list.size());
        result.put("list",list);
        return result;
    }

    private MetadataBrief convertToMetadataBrief(final Metadata md) {
        final MetadataBrief mdb = new MetadataBrief();
        mdb.setId(md.getId());
        mdb.setFileIdentifier(md.getMetadataId());
        mdb.setTitle(md.getTitle());
        mdb.setType(md.getProfile());

        final Optional<CstlUser> optUser = userRepository.findById(md.getOwner());
        String owner = null;
        if(optUser!=null && optUser.isPresent()){
            final CstlUser user = optUser.get();
            if(user != null){
                owner = user.getLogin();
            }
        }
        mdb.setOwner(owner);
        mdb.setUpdateDate(md.getDatestamp());
        mdb.setCreationDate(md.getDateCreation());
        mdb.setMdCompletion(md.getMdCompletion());
        mdb.setLevelCompletion(md.getLevel());
        mdb.setIsValidated(md.getIsValidated());
        mdb.setIsPublished(md.getIsPublished());
        mdb.setResume(md.getResume());
        return mdb;
    }

    @GET
    @Path("/{id}")
    public MetadataBrief get(@PathParam("id") final Integer metadataId) {
        final Metadata candidat = metadataBusiness.getMetadataById(metadataId);
        if(candidat != null) {
            return convertToMetadataBrief(candidat);
        }
        return null;
    }

    @POST
    @Path("/delete")
    public Response delete(final List<MetadataBrief> metadataList,@Context HttpServletRequest req) throws ConfigurationException {
        //@TODO GEOC-113 implements delete method with permission of user
        //the user can select multiple records to delete,
        // but some of records can have a restricted permission for this user.
        //So we need to send an error message to prevent this case.
        //It would be great if we can returns the ids of metadata which cannot be deleted
        for (MetadataBrief brief : metadataList) {
            metadataBusiness.deleteMetadata(brief.getId());
        }
        return Response.ok("records deleted with success!").build();
    }

    /**
     * Proceed to export metadata, creates all necessary files in tmp directory and returns file name
     * and directory name for next callback.
     * If there is one metadata into given list then it will creates xml file, otherwise
     * a zip file will be created.
     *
     * @param metadataList given metadata list
     * @return Response that contains the directory and file names.
     */
    @POST
    @Path("/exportMetadata")
    public Response exportMetadata(final List<MetadataBrief> metadataList) {
        final File directory = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        directory.deleteOnExit();
        directory.mkdir();
        final List<File> files = new ArrayList<>();
        for (final MetadataBrief brief : metadataList) {
            final Metadata metadata = metadataBusiness.getMetadataById(brief.getId());
            if (metadata != null) {
                try {
                    final File file = new File(directory, cleanFileName(metadata.getMetadataId()) + ".xml");
                    file.deleteOnExit();

                    FileUtilities.stringToFile(file, metadata.getMetadataIso());
                    files.add(file);
                } catch (IOException ex) {
                    return Response.status(500).entity(ex.getMessage()).build();
                }
            }
        }

        final File file;
        if (files.size() == 1) {
            file = files.get(0);
        } else {
            file = new File(directory, UUID.randomUUID().toString() + ".zip");
            file.deleteOnExit();
            try {
                FileUtilities.zip(file, ZipOutputStream.DEFLATED, Deflater.BEST_COMPRESSION, null, files.toArray());
            } catch (IOException ex) {
                return Response.status(500).entity(ex.getMessage()).build();
            }
        }

        Map<String,String> map = new HashMap<>();
        map.put("directory", directory.getName());
        map.put("file", file.getName());
        return Response.ok(map).build();
    }

    private static String cleanFileName(String s) {
        s = s.replace(":", "_");
        s = s.replace("/", "_");
        return s;
    }

    /**
     * Download exported metadata for given file name and directory anme located in tmp folder.
     * this is the callback of exportMetadata method.
     * @param directory given directory name.
     * @param file given file name to download
     * @return Response as attachment file or error with status 500
     */
    @GET
    @Path("/download/{directory}/{file}")
    public Response download(@PathParam("directory") final String directory,@PathParam("file") final String file) {
        try{
            final File dir = new File(System.getProperty("java.io.tmpdir"), directory);
            final File f = new File(dir,file);
            return Response.ok(f).header("content-disposition", "attachment; filename="+ f.getName()).build();
        }catch(Exception ex) {
            LOGGER.log(Level.WARNING,ex.getLocalizedMessage(),ex);
            return Response.status(500).entity(ex.getMessage()).build();
        }
    }

    /**
     * Change the owner id for given metadata list.
     * @param ownerId given user id
     * @param metadataList the metadata list to apply changes
     * @return Response
     */
    @POST
    @Path("/changeOwner/{ownerId}")
    public Response changeOwner(@PathParam("ownerId") final int ownerId,final List<MetadataBrief> metadataList) {
        for (MetadataBrief brief : metadataList) {
            metadataBusiness.updateOwner(brief.getId(), ownerId);
        }
        return Response.ok("owner applied with success!").build();
    }

    /**
     * Change the validation state for given metadata list.
     * @param isvalid given state of validation
     * @param metadataList the metadata list to apply changes
     * @return Response
     */
    @POST
    @Path("/changeValidation/{isvalid}")
    public Response changeValidation(@PathParam("isvalid") final boolean isvalid,final List<MetadataBrief> metadataList) {
        for (MetadataBrief brief : metadataList) {
            metadataBusiness.updateValidation(brief.getId(), isvalid);
        }
        return Response.ok("validation applied with success!").build();
    }

    /**
     * Change the published state for given metadata list.
     * @param ispublished given state of published
     * @param metadataList the metadata list to apply changes
     * @return Response
     */
    @POST
    @Path("/changePublication/{ispublished}")
    public Response changePublication(@PathParam("ispublished") final boolean ispublished,final List<MetadataBrief> metadataList) throws ConfigurationException {
        for (MetadataBrief brief : metadataList) {
            metadataBusiness.updatePublication(brief.getId(), ispublished);
        }
        return Response.ok("Published state applied with success!").build();
    }

    @GET
    @Path("/metadataJson/iso/{metadataId}/{prune}")
    public Response getIsoMetadataJson(final @PathParam("metadataId") int metadataId,
                                       final @PathParam("prune") boolean prune) {
        final StringWriter buffer = new StringWriter();
        try{
            DefaultMetadata metadata = metadataBusiness.getMetadata(metadataId);
            if (metadata != null) {
                metadata.prune();
                //get template name
                final String templateName = metadataBusiness.getMetadataById(metadataId).getProfile();
                final Template template = Template.getInstance(templateName);
                template.write(metadata,buffer,prune);
            }
        } catch(Exception ex) {
            LOGGER.log(Level.WARNING, "error while writing metadata json.", ex);
        }
        return Response.ok(buffer.toString()).build();
    }


}
