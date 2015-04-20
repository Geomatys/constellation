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
import org.constellation.engine.security.WorkspaceService;
import org.constellation.json.metadata.binding.RootObj;
import org.constellation.model.metadata.Filter;
import org.constellation.model.metadata.GroupStatBrief;
import org.constellation.model.metadata.MetadataBrief;
import org.constellation.model.metadata.MetadataLightBrief;
import org.constellation.model.metadata.OwnerStatBrief;
import org.constellation.model.metadata.Page;
import org.constellation.model.metadata.PagedSearch;
import org.constellation.model.metadata.Profile;
import org.constellation.model.metadata.Search;
import org.constellation.model.metadata.Sort;
import org.constellation.model.metadata.User;
import org.geotoolkit.util.FileUtilities;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
 * Provides all necessary methods to serve rest api for metadata.
 * Used by the new metadata dashboard page.
 *
 * @author Mehdi Sidhoum (Geomatys).
 */
@Component
@Path("/1/metadata")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MetadataRest {

    /**
     * Used for debugging purposes.
     */
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
     * Inject user repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Inject workspaceService to serve upload directory.
     */
    @Inject
    private WorkspaceService workspaceService;

    public MetadataRest() {}

    /**
     * Returns the list of profiles used by metadata,
     * the list contains only profiles used not all.
     *
     * @return List of {@link Profile}
     */
    @GET
    @Path("/profiles")
    public List<Profile> getProfilesList() {
        final List<Profile> result = new ArrayList<>();
        final Map<String,Integer> map = metadataBusiness.getProfilesCount(new HashMap<String, Object>());
        if(map!=null){
            for(final Map.Entry<String,Integer> entry : map.entrySet()){
                result.add(new Profile(entry.getKey(),entry.getValue()));
            }
        }
        return result;
    }

    /**
     * Returns the list of users to serve metadata owner property.
     * since the UserRest api does not work yet,
     * we use this method to serve the list of users.
     *
     * @return List of {@link User}
     */
    @GET
    @Path("/usersList")
    public List<User> getUsersList() {
        final List<User> result = new ArrayList<>();
        //TODO use userBusiness because the implementation can differ since the user have groups in sub project.
        final List<CstlUser> users = userRepository.findAll();
        if(users != null) {
            for(final CstlUser u : users) {
                result.add(new User(u.getId(),u.getLogin(),u.getEmail(),u.getLastname(),u.getFirstname(),u.getActive()));
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

    /**
     * Proceed to get list of records {@link MetadataBrief} in Page object for dashboard.
     * the list can be filtered, sorted and use the pagination.
     *
     * @param pagedSearch given params of filters, sorting and pagination served by a pojo {link PagedSearch}
     * @param req the http request needed to get the current user.
     * @return {link Page} of {@link MetadataBrief}
     */
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

        //TODO use metadataBusiness instead of metadataRepository because the implementation can differ for example to treat the filter  for group
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

    /**
     * Proceed to fill a map of filters used to search records.
     * the filters are passed from a pojo {@link PagedSearch}
     *
     * @param pagedSearch {link PagedSearch} given filter params
     * @param req given http request object to extract the user
     * @return {@code Map} map of filters to send
     */
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
                    String value = f.getValue();
                    if("_all".equals(value)) {
                        continue; //no need to filter on owner field if we ask all users
                    }
                    try{
                        final int userId = Integer.valueOf(value);
                        filterMap.put("owner",userId);
                    }catch(Exception ex) {
                        //try as login
                        if("_me".equals(value)) {
                            //get user login
                            value = req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : null;
                        }
                        final Optional<CstlUser> optUser = userRepository.findOne(value);
                        if(optUser!=null && optUser.isPresent()){
                            final CstlUser user = optUser.get();
                            if(user != null){
                                filterMap.put(f.getField(),user.getId());
                            }
                        }
                    }
                } else if("group".equals(f.getField())) {
                    String value = f.getValue();
                    if("_all".equals(value)) {
                        continue; //no need to filter on group field if we ask all groups
                    }
                    try{
                        final int groupId = Integer.valueOf(value);
                        filterMap.put("group",groupId);
                    }catch(Exception ex) {
                        //do nothing
                    }
                } else if ("period".equals(f.getField())) {
                    final String value = f.getValue();
                    if("_all".equals(value)) {
                        continue; //no need to filter on period if we ask from the beginning.
                    }
                    long delta;
                    final long currentTs= System.currentTimeMillis();
                    final long dayTms = 1000*60*60*24L;
                    if("week".equalsIgnoreCase(value)) {
                        delta = currentTs - (dayTms*7);
                    }else if("month".equalsIgnoreCase(value)) {
                        delta = currentTs - (dayTms*30);
                    }else if("3months".equalsIgnoreCase(value)) {
                        delta = currentTs - (dayTms*90);
                    }else if("6months".equalsIgnoreCase(value)) {
                        delta = currentTs - (dayTms*180);
                    }else if("year".equalsIgnoreCase(value)) {
                        delta = currentTs - (dayTms*365);
                    }else {
                        continue;
                    }
                    filterMap.put("period",delta);
                } else if("profile".equals(f.getField())) {
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

    /**
     * Returns a singleton map that contains the total count of matched records for filtered list.
     * and all records in a lightweight list of pojo {@link MetadataLightBrief}.
     * it is usefull to proceed to select All metadata to proceed to do batch actions on them.
     *
     * @param pagedSearch given {@link PagedSearch} that does not contains the pagination.
     * @param req the http request to extract the user infos
     * @return singleton Map of couple total count, list of lightweight record.
     */
    @POST
    @Path("/searchIds")
    public Map searchIds(final PagedSearch pagedSearch,@Context HttpServletRequest req) {
        final List<MetadataLightBrief> list = new ArrayList<>();
        final Map<String,Object> filterMap = prepareFilters(pagedSearch, req);
        //TODO use metadataBusiness instead of metadataRepository because the implementation can differ for example to treat the filter  for group
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

    /**
     * Utility method to convert a metadata db model to ui model.
     *
     * @param md given metadata from data base model to convert.
     * @return {link MetadataBrief} converted pojo.
     */
    private MetadataBrief convertToMetadataBrief(final Metadata md) {
        final MetadataBrief mdb = new MetadataBrief();
        mdb.setId(md.getId());
        mdb.setFileIdentifier(md.getMetadataId());
        mdb.setTitle(md.getTitle());
        mdb.setType(md.getProfile());

        //TODO use userBusiness because the implementation can differ since the user have groups in sub project.
        final Optional<CstlUser> optUser = userRepository.findById(md.getOwner());
        User owner = null;
        if(optUser!=null && optUser.isPresent()){
            final CstlUser user = optUser.get();
            if(user != null){
                owner = new User(user.getId(),user.getLogin(),user.getEmail(),user.getLastname(),user.getFirstname(),user.getActive());
            }
        }
        mdb.setUser(owner);
        mdb.setUpdateDate(md.getDatestamp());
        mdb.setCreationDate(md.getDateCreation());
        mdb.setMdCompletion(md.getMdCompletion());
        mdb.setLevelCompletion(md.getLevel());
        mdb.setIsValidated(md.getIsValidated());
        mdb.setIsPublished(md.getIsPublished());
        mdb.setResume(md.getResume());
        return mdb;
    }

    /**
     * Return metadata brief object as json for given id.
     *
     * @param metadataId given metadata id
     * @return {@link MetadataBrief} object as json
     */
    @GET
    @Path("/{id}")
    public MetadataBrief get(@PathParam("id") final Integer metadataId) {
        final Metadata candidat = metadataBusiness.getMetadataById(metadataId);
        if(candidat != null) {
            return convertToMetadataBrief(candidat);
        }
        return null;
    }

    /**
     * Return stats counts of metadata as map object :
     *  total count of metadata
     *  total of metadata not validated
     *  total of metadata not published
     *  total of metadata published
     *
     * @return Response that contains the map.
     */
    @GET
    @Path("/getStats")
    public Response getStats() {
        final Map<String,Integer> map = new HashMap<>();

        final Map<String, Object> emptyFilter = new HashMap<>();
        final int total             = metadataBusiness.countTotal(emptyFilter);
        final int waitingToValidate = metadataBusiness.countValidated(false, emptyFilter);
        final int waitingToPublish  = metadataBusiness.countPublished(false, emptyFilter);
        final int published         = metadataBusiness.countPublished(true,  emptyFilter);

        map.put("total", total);
        map.put("waitingToValidate", waitingToValidate);
        map.put("waitingToPublish", waitingToPublish);
        map.put("published", published);
        return Response.ok(map).build();
    }

    /**
     * TODO get all needed stats for given filters
     *
     * @param search pojo that contains filters.
     * @return Response map with all metadata stats.
     */
    @POST
    @Path("/computeFullStats")
    public Response computeFullStats(final Search search) {
        final Map<String,Object> map = new HashMap<>();
        final List<Filter> filters = search.getFilters();
        final Map<String,Object> filterMap = new HashMap<>();
        if(filters != null) {
            for(final Filter f : filters) {
                if("owner".equals(f.getField())) {
                    String value = f.getValue();
                    if("_all".equals(value)) {
                        continue; //no need to filter on owner field if we ask all owners
                    }
                    try{
                        final int userId = Integer.valueOf(value);
                        filterMap.put("owner",userId);
                    }catch(Exception ex) {
                        //do nothing
                    }
                }else if("group".equals(f.getField())) {
                    String value = f.getValue();
                    if("_all".equals(value)) {
                        continue; //no need to filter on group field if we ask all groups
                    }
                    try{
                        final int groupId = Integer.valueOf(value);
                        filterMap.put("group",groupId);
                    }catch(Exception ex) {
                        //do nothing
                    }
                }else if ("period".equals(f.getField())) {
                    final String value = f.getValue();
                    if("_all".equals(value)) {
                        continue; //no need to filter on period if we ask from the beginning.
                    }
                    long delta;
                    final long currentTs= System.currentTimeMillis();
                    final long dayTms = 1000*60*60*24L;
                    if("week".equalsIgnoreCase(value)) {
                        delta = currentTs - (dayTms*7);
                    }else if("month".equalsIgnoreCase(value)) {
                        delta = currentTs - (dayTms*30);
                    }else if("3months".equalsIgnoreCase(value)) {
                        delta = currentTs - (dayTms*90);
                    }else if("6months".equalsIgnoreCase(value)) {
                        delta = currentTs - (dayTms*180);
                    }else if("year".equalsIgnoreCase(value)) {
                        delta = currentTs - (dayTms*365);
                    }else {
                        continue;
                    }
                    filterMap.put("period",delta);
                }
            }
        }

        Map<String,Integer> general = new HashMap<>();
        
        final int total             = metadataBusiness.countTotal(filterMap);
        final int waitingToValidate = metadataBusiness.countValidated(false, filterMap);
        final int waitingToPublish  = metadataBusiness.countPublished(false, filterMap);
        final int published         = metadataBusiness.countPublished(true,filterMap);

        general.put("total", total);
        general.put("waitingToValidate", waitingToValidate);
        general.put("waitingToPublish", waitingToPublish);
        general.put("published", published);

        //Get profiles distribution counts
        final List<Profile> profiles = new ArrayList<>();
        final Map<String,Integer> profilesMap = metadataBusiness.getProfilesCount(filterMap);
        if(profilesMap!=null){
            for(final Map.Entry<String,Integer> entry : profilesMap.entrySet()){
                profiles.add(new Profile(entry.getKey(),entry.getValue()));
            }
        }
        map.put("repartitionProfiles",profiles);

        //Get completion counts for metadata in 10 categories (10%, 20%, ... 100%)
        final int[] completionArray = metadataBusiness.countInCompletionRange(filterMap);
        map.put("completionPercents",completionArray);

        //TODO get the list of groups by passing filterMap, with stats toValidate,toPublish,published for each group
        final List<OwnerStatBrief> contributorsStatList = new ArrayList<>();
        map.put("contributorsStatList",contributorsStatList);

        //TODO the list in cstl is always empty since groups are not implemented yet, the business for subproject will return completed list.
        final List<GroupStatBrief> groupsStatList = new ArrayList<>();
        map.put("groupsStatList",groupsStatList);

        map.put("general",general);

        return Response.ok(map).build();
    }

    /**
     * Proceed to delete a list of metadata.
     * the http request provide the user that should be passed to check if the user can delete all record in the list.
     * the method must returns an appropriate response in case of user does not have the permission to delete all records
     * we need to return the id of metadata that cannot be deleted.
     *
     * @param metadataList given metadata list to delete.
     * @param req http request that contains the user.
     * @return Response that contains all neccessary info to inform the user about what records fails.
     * @throws ConfigurationException
     */
    @POST
    @Path("/delete")
    public Response delete(final List<MetadataBrief> metadataList,@Context HttpServletRequest req) throws ConfigurationException {
        //@TODO GEOC-113 implements delete method with permission of user
        //the user can select multiple records to delete,
        // but some of records can have a restricted permission for this user.
        //So we need to send an error message to prevent this case.
        //It would be great if we can returns the ids of metadata which cannot be deleted
        List<Integer> ids = new ArrayList<>();
        for (MetadataBrief brief : metadataList) {
            ids.add(brief.getId());
        }
        metadataBusiness.deleteMetadata(ids);
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

    /**
     * Utility function to clean the file name when exporting metadata to XML.
     * TODO use commons-utils instead.
     * @param s given string to clean
     * @return String value
     */
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
        List<Integer> ids = new ArrayList<>();
        for (MetadataBrief brief : metadataList) {
            ids.add(brief.getId());
        }
        metadataBusiness.updatePublication(ids, ispublished);
        return Response.ok("Published state applied with success!").build();
    }

    /**
     * Returns the json representation of metadata by using template for given metadata ID .
     * the metadata can be pruned in case of displaying purposes, or set prune to false for edition purposes.
     *
     * @param metadataId given metadata ID
     * @param prune flag that indicates if the metadata will be pruned or not to delete empty values.
     * @return Response that contains the metadata in json format.
     */
    @GET
    @Path("/metadataJson/iso/{metadataId}/{prune}")
    public Response getIsoMetadataJson(final @PathParam("metadataId") int metadataId,
                                       final @PathParam("prune") boolean prune) {
        final StringWriter buffer = new StringWriter();
        try{
            DefaultMetadata metadata = metadataBusiness.getMetadata(metadataId);
            if (metadata != null) {
                if(prune){
                    metadata.prune();
                }
                //get template name
                final String templateName = metadataBusiness.getMetadataById(metadataId).getProfile();
                final Template template = Template.getInstance(templateName);
                template.write(metadata,buffer,prune, false);
            }
        } catch(Exception ex) {
            LOGGER.log(Level.WARNING, "error while writing metadata json.", ex);
            return Response.status(500).entity(ex.getLocalizedMessage()).build();
        }
        return Response.ok(buffer.toString()).build();
    }

    /**
     * Returns the json representation of metadata by using template for new metadata with default values.
     *
     * @param profile the given profile name
     * @return Response that contains the metadata in json format.
     */
    @GET
    @Path("/metadataJson/new/{profile}")
    public Response getNewMetadataJson(final @PathParam("profile") String profile) {
        final StringWriter buffer = new StringWriter();
        try{
            //get template name
            final Template template = Template.getInstance(profile);
            template.write(new DefaultMetadata(), buffer, false, false);

        } catch(Exception ex) {
            LOGGER.log(Level.WARNING, "An error happen when building json representation of new metadata for profile "+profile, ex);
            return Response.status(500).entity(ex.getLocalizedMessage()).build();
        }
        return Response.ok(buffer.toString()).build();
    }

    /**
     * Convert metadata in given profile and return the json representation of the resulted metadata.
     *
     * @param metadataId given metadata id
     * @param prune optional flag that indicates if the prune will be applied
     * @param profile the target  profile name
     * @return {code Response}
     */
    @GET
    @Path("/convertMDJson/{metadataId}/{prune}/{profile}")
    public Response convertMetadataJson(final @PathParam("metadataId") int metadataId,
                                        final @PathParam("prune") boolean prune,
                                        final @PathParam("profile") String profile) {

        final StringWriter buffer = new StringWriter();
        try{
            DefaultMetadata metadata = metadataBusiness.getMetadata(metadataId);
            if (metadata != null) {
                if(prune){
                    metadata.prune();
                }
                Template newTemplate = Template.getInstance(profile);
                if (newTemplate != null) {
                    newTemplate.write(metadata, buffer, false, true);
                }
            }
        } catch(Exception ex) {
            LOGGER.log(Level.WARNING, "error while writing metadata json.", ex);
            return Response.status(500).entity(ex.getLocalizedMessage()).build();
        }
        return Response.ok(buffer.toString()).build();

    }

    /**
     * Proceed to save metadata values for given metadataId and values
     * using template defined by given profile.
     *
     * @param metadataId given metadata id
     * @param profile given profile, can be another profile of metadata's own
     * @param metadataValues {@code RootObj} metadata values to save
     * @return {code Response}
     */
    @POST
    @Path("/save/{metadataId}/{profile}")
    public Response saveMetadata(@PathParam("metadataId") final int metadataId,
                                 @PathParam("profile") final String profile,
                                 final RootObj metadataValues) {
        try {
            // Get previously saved metadata
            final Metadata pojo      = metadataBusiness.getMetadataById(metadataId);
            if (pojo != null) {
                
                // detect profile change
                final DefaultMetadata metadata;
                if (!pojo.getProfile().equals(profile)) {
                    metadata = new DefaultMetadata();
                    metadataBusiness.updateProfile(metadataId, profile);
                } else {
                    metadata = metadataBusiness.getMetadata(metadataId);
                }
                //get template
                final Template template = Template.getInstance(profile);
                template.read(metadataValues, metadata, false);

                //update dateStamp for metadata
                metadata.setDateStamp(new Date());

                //Save metadata
                metadataBusiness.updateMetadata(metadata.getFileIdentifier(), metadata);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "error while saving metadata.", ex);
            return Response.status(500).entity(ex.getLocalizedMessage()).build();
        }
        return Response.ok("Metadata saved successfully!").build();
    }

    /**
     * Proceed to create new metadata for given profile and values.
     *
     * @param profile given profile
     * @param metadataValues {@code RootObj} metadata values to save
     * @return {code Response}
     */
    @POST
    @Path("/createNew/{profile}")
    public Response createNewMetadata(@PathParam("profile") final String profile,final RootObj metadataValues) {
        try {
            //get template
            final Template template = Template.getInstance(profile);
            final DefaultMetadata metadata = new DefaultMetadata();
            template.read(metadataValues, metadata, true);
            String identifier = UUID.randomUUID().toString();
            metadata.setFileIdentifier(identifier);
            metadata.setDateStamp(new Date());
            metadataBusiness.updateMetadata(identifier, metadata);

        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "An error happen when creating new metadata.", ex);
            return Response.status(500).entity(ex.getLocalizedMessage()).build();
        }
        return Response.ok("Metadata saved successfully!").build();
    }

    /**
     * Proceed to duplicate metadata for given id.
     * an optional title is given to set the new title of cloned metadata.
     * if the given title is empty or null
     *
     * @param id given metadata id to duplicate
     * @param title optional title
     * @return Response
     */
    @POST
    @Path("/duplicate/{id}")
    public Response duplicateMetadata(@PathParam("id") final int id, final String title) {
        try {
            final String newTitle;
            if(!isBlank(title)){
                newTitle = title;
            }else {
                newTitle = null;
            }
            metadataBusiness.duplicateMetadata(id, newTitle);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING,ex.getLocalizedMessage(),ex);
            return Response.status(500).entity(ex.getMessage()).build();
        }
        return Response.ok("Metadata duplicated successfully!").build();
    }

    /**
     * Receive a {@code MultiPart} which contains xml file,
     * the metadata will be stored in server and returns the generated Id
     * and the nearest profile name that matches the metadata.
     *
     * @param mdFileIs {@code InputStream} the given xml stream
     * @param fileMetaDetail {@code FormDataContentDisposition} the file
     * @param request {@code HttpServletRequest} the hhtp request
     * @return {@code Response} with 200 code if upload work, 500 if not work.
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadMetadata(@FormDataParam("metadataFileInput") InputStream mdFileIs,
                                   @FormDataParam("metadataFileInput") FormDataContentDisposition fileMetaDetail,
                                   @Context HttpServletRequest request) {

        final File uploadDirectory = workspaceService.getUploadDirectory();
        final Map<String,Object> map = new HashMap<>();
        try {
            final File newFileMetaData = new File(uploadDirectory, fileMetaDetail.getFileName());
            if (mdFileIs != null) {
                if (!uploadDirectory.exists()) {
                    uploadDirectory.mkdir();
                }
                Files.copy(mdFileIs, newFileMetaData.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                final String xml = FileUtilities.getStringFromFile(newFileMetaData);
                final DefaultMetadata iso = (DefaultMetadata) metadataBusiness.unmarshallMetadata(xml);
                
                String identifier = iso.getFileIdentifier();
                if (metadataBusiness.existInternalMetadata(identifier, true, false)) {
                    identifier = UUID.randomUUID().toString();
                    iso.setFileIdentifier(identifier);
                    map.put("renewId", true);
                }else {
                    map.put("renewId", false);
                }
                map.put("usedDefaultProfile",metadataBusiness.getTemplateFromMetadata(iso) == null);

                metadataBusiness.updateMetadata(identifier, iso);

                final Metadata meta = metadataBusiness.searchFullMetadata(identifier, true, false);
                MetadataBrief brief = convertToMetadataBrief(meta);
                map.put("record",brief);
            }
        }catch(Exception ex) {
            LOGGER.log(Level.WARNING,ex.getLocalizedMessage(),ex);
            map.put("msg", ex.getLocalizedMessage());
            return Response.status(500).entity(map).build();
        }
        return Response.ok(map).build();
    }


}
