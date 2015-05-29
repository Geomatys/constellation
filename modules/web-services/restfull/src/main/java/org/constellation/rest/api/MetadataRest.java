package org.constellation.rest.api;

import com.google.common.base.Optional;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.dto.metadata.ValidationList;
import org.constellation.business.IMetadataBusiness;
import org.constellation.engine.register.jooq.tables.pojos.CstlUser;
import org.constellation.engine.register.jooq.tables.pojos.Metadata;
import org.constellation.engine.register.repository.UserRepository;
import org.constellation.engine.security.WorkspaceService;
import org.constellation.json.metadata.binding.RootObj;
import org.constellation.admin.dto.metadata.Filter;
import org.constellation.admin.dto.metadata.GroupStatBrief;
import org.constellation.admin.dto.metadata.MetadataBrief;
import org.constellation.admin.dto.metadata.MetadataLightBrief;
import org.constellation.admin.dto.metadata.OwnerStatBrief;
import org.constellation.admin.dto.metadata.Page;
import org.constellation.admin.dto.metadata.PagedSearch;
import org.constellation.admin.dto.metadata.Profile;
import org.constellation.admin.dto.metadata.Search;
import org.constellation.admin.dto.metadata.Sort;
import org.constellation.admin.dto.metadata.User;
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
import org.constellation.business.IConfigurationBusiness;
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
     * Inject metadata business
     */
    @Inject
    private IMetadataBusiness metadataBusiness;
    
    /**
     * Inject configuration business
     */
    @Inject
    private IConfigurationBusiness configurationBusiness;

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
     * Returns the list of all profiles.
     * @return List of {@link Profile}
     */
    @GET
    @Path("/allProfiles")
    public List<Profile> getAllProfilesList() {
        final List<Profile> result = new ArrayList<>();
        final List<String> allProfiles = metadataBusiness.getAllProfiles();
        for(final String p : allProfiles){
            result.add(new Profile(p,0));
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
        return metadataBusiness.getUsers();
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

        final Map<Integer,List> result = metadataBusiness.filterAndGet(filterMap,sortEntry,pageNumber,rowsPerPage);
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
                        //try for case of current user's group
                        if("_mygroup".equals(value)) {
                            //try to find the user's group from login
                            final String login = req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : null;
                            final Optional<CstlUser> optUser = userRepository.findOne(login);
                            if(optUser!=null && optUser.isPresent()){
                                final CstlUser user = optUser.get();
                                if(user != null){
                                    final User pojoUser = metadataBusiness.getUser(user.getId());
                                    if(pojoUser != null && pojoUser.getGroup() != null) {
                                        filterMap.put("group",pojoUser.getGroup().getId());
                                    }
                                }
                            }
                        }
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
                } else if("validation_required".equals(f.getField())) {
                    final String value = f.getValue();
                    if(value != null) {
                        filterMap.put(f.getField(),value);
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
        
        final Map<Integer,String> map = metadataBusiness.filterAndGetWithoutPagination(filterMap);
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
        if(md.getOwner() != null){
            User owner = metadataBusiness.getUser(md.getOwner());
            mdb.setUser(owner);
        }
        mdb.setUpdateDate(md.getDatestamp());
        mdb.setCreationDate(md.getDateCreation());
        mdb.setMdCompletion(md.getMdCompletion());
        mdb.setLevelCompletion(md.getLevel());
        mdb.setIsValidated(md.getIsValidated());
        mdb.setIsPublished(md.getIsPublished());
        mdb.setResume(md.getResume());
        mdb.setValidationRequired(md.getValidationRequired());
        mdb.setComment(md.getComment());
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
     *  total of metadata validated
     *  total of metadata not validated
     *  total of metadata waiting to validate
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
        final int validated         = metadataBusiness.countValidated(true, emptyFilter);
        final int notValid          = metadataBusiness.countValidated(false, emptyFilter);
        final Map<String, Object> filter = new HashMap<>();
        filter.put("validation_required","REQUIRED");
        final int waitingToValidate = metadataBusiness.countValidated(false, filter);
        final int notPublish        = metadataBusiness.countPublished(false, emptyFilter);
        final int published         = metadataBusiness.countPublished(true,  emptyFilter);
        final Map<String, Object> filter2 = new HashMap<>();
        filter2.put("validated",Boolean.TRUE);
        final int waitingToPublish  = metadataBusiness.countPublished(false,  filter2);

        map.put("total", total);
        map.put("validated", validated);
        map.put("notValid", notValid);
        map.put("waitingToValidate", waitingToValidate);
        map.put("published", published);
        map.put("notPublish", notPublish);
        map.put("waitingToPublish", waitingToPublish);
        return Response.ok(map).build();
    }

    /**
     * Get all needed stats for given filters
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
                if("group".equals(f.getField())) {
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
        final int validated         = metadataBusiness.countValidated(true,filterMap);
        final int notValid          = metadataBusiness.countValidated(false, filterMap);
        final Map<String,Object> filter = new HashMap<>();
        filter.putAll(filterMap);
        filter.put("validation_required","REQUIRED");
        final int waitingToValidate = metadataBusiness.countValidated(false, filter);
        final int notPublish        = metadataBusiness.countPublished(false, filterMap);
        final int published         = metadataBusiness.countPublished(true,filterMap);
        final Map<String, Object> filter2 = new HashMap<>();
        filter2.putAll(filterMap);
        filter2.put("validated",Boolean.TRUE);
        final int waitingToPublish  = metadataBusiness.countPublished(false,  filter2);

        general.put("total", total);
        general.put("validated", validated);
        general.put("notValid", notValid);
        general.put("waitingToValidate", waitingToValidate);
        general.put("notPublish", notPublish);
        general.put("published", published);
        general.put("waitingToPublish", waitingToPublish);

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

        final List<OwnerStatBrief> contributorsStatList = metadataBusiness.getOwnerStatBriefs(new HashMap<>(filterMap));
        map.put("contributorsStatList",contributorsStatList);

        final List<GroupStatBrief> groupsStatList = metadataBusiness.getGroupStatBriefs(new HashMap<>(filterMap));
        map.put("groupsStatList",groupsStatList);

        map.put("general",general);

        return Response.ok(map).build();
    }

    /**
     * Proceed to delete a list of metadata.
     * the http request provide the user that should be passed to check if the user can delete all record in the list.
     * the method must returns an appropriate response in case of user does not have the permission to delete all records.
     *
     * @param metadataList given metadata list to delete.
     * @return Response that contains all necessary info to inform the user about what records fails.
     */
    @POST
    @Path("/delete")
    public Response delete(final List<MetadataBrief> metadataList) {
        //the user can select multiple records to delete,
        // but some of records can have a restricted permission for this user.
        //So we need to send an error message to prevent this case.
        final List<Integer> ids = new ArrayList<>();
        for (final MetadataBrief brief : metadataList) {
            ids.add(brief.getId());
        }
        try {
            metadataBusiness.deleteMetadata(ids);
            return Response.ok("records deleted with success!").build();
        }catch(Exception ex) {
            LOGGER.log(Level.WARNING,"Cannot delete metadata list due to exception error : "+ ex.getLocalizedMessage());
            final Map<String,String> map = new HashMap<>();
            map.put("msg",ex.getLocalizedMessage());
            return Response.status(403).entity(map).build();
        }
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
        final List<Integer> ids = new ArrayList<>();
        for (final MetadataBrief brief : metadataList) {
            ids.add(brief.getId());
        }
        try {
            metadataBusiness.updateOwner(ids, ownerId);
            return Response.ok("owner applied with success!!").build();
        }catch(Exception ex) {
            LOGGER.log(Level.WARNING,"Cannot change the owner for metadata list due to exception error : "+ ex.getLocalizedMessage());
            final Map<String,String> map = new HashMap<>();
            map.put("msg",ex.getLocalizedMessage());
            return Response.status(403).entity(map).build();
        }
    }

    /**
     * Change the validation state for given metadata list.
     * @param isvalid given state of validation
     * @param validationList the metadata list to apply changes
     *                       with optional comment in case of discarding validation.
     * @return Response
     */
    @POST
    @Path("/changeValidation/{isvalid}")
    public Response changeValidation(@PathParam("isvalid") final boolean isvalid,
                                     final ValidationList validationList) throws ConfigurationException{
        final List<MetadataBrief> metadataList = validationList.getMetadataList();
        final String comment = validationList.getComment();

        boolean canContinue = true;
        final List<Metadata> list = new ArrayList<>();
        for (final MetadataBrief brief : metadataList) {
            final int metadataId = brief.getId();
            final Metadata metadata = metadataBusiness.getMetadataById(metadataId);
            if(metadata == null) {
                //skip if null, never happen
                continue;
            }
            list.add(metadata);
            if(isvalid && "NONE".equalsIgnoreCase(metadata.getLevel()) && isLevelRequiredForValidation()) {
                canContinue = false;
                break; //no needs to continue in the loop because there are metadata with level=NONE.
            }
        }
        final Map<String,String> map = new HashMap<>();
        if(canContinue) {
            try {
                for (final Metadata md : list) {
                    if(isvalid) {
                        metadataBusiness.acceptValidation(md.getId());
                    } else if("REQUIRED".equalsIgnoreCase(md.getValidationRequired())){
                        metadataBusiness.denyValidation(md.getId(),comment);
                    } else {
                        if(md.getIsPublished()) {
                            metadataBusiness.updatePublication(md.getId(),false);
                        }
                        metadataBusiness.updateValidation(md.getId(),false);
                    }
                }
                map.put("status","ok");
                return Response.ok(map).build();
            }catch(Exception ex) {
                LOGGER.log(Level.WARNING,"Cannot change the validation for metadata list due to exception error : "+ ex.getLocalizedMessage());
                map.put("msg",ex.getLocalizedMessage());
                return Response.status(403).entity(map).build();
            }
        }
        map.put("notLevel","true");
        map.put("status","failed");
        return Response.status(403).entity(map).build();
    }

    /**
     * Change the published state for given metadata list.
     * @param ispublished given state of published
     * @param metadataList the metadata list to apply changes
     * @return Response
     */
    @POST
    @Path("/changePublication/{ispublished}")
    public Response changePublication(@PathParam("ispublished") final boolean ispublished,
                                      final List<MetadataBrief> metadataList) throws ConfigurationException {
        boolean canContinue = true;

        for (final MetadataBrief brief : metadataList) {
            final int metadataId = brief.getId();
            final Metadata metadata = metadataBusiness.getMetadataById(metadataId);
            if(metadata == null) {
                //skip if null, never happen
                continue;
            }
            if(ispublished && !metadata.getIsValidated()) {
                canContinue = false;
                break; //no needs to continue in the loop because there are not valid metadata.
            }
        }
        final Map<String,String> map = new HashMap<>();
        if(canContinue) {
            final List<Integer> ids = new ArrayList<>();
            for (final MetadataBrief brief : metadataList) {
                ids.add(brief.getId());
            }
            try {
                metadataBusiness.updatePublication(ids, ispublished);
                map.put("status","ok");
                return Response.ok(map).build();
            }catch(Exception ex) {
                LOGGER.log(Level.WARNING,"Cannot change the publication state for metadata list due to exception error : "+ ex.getLocalizedMessage());
                map.put("msg",ex.getLocalizedMessage());
                return Response.status(403).entity(map).build();
            }
        }

        map.put("notValidExists","true");
        map.put("status","failed");
        return Response.status(403).entity(map).build();
    }

    @POST
    @Path("/askForValidation/{userId}")
    public Response askForValidation(@PathParam("userId") final Integer userId,
                                     final List<MetadataBrief> metadataList,
                                     @Context HttpServletRequest req) {
        boolean canContinue = true;
        boolean validExists = false;
        boolean notOwner = false;
        for (final MetadataBrief brief : metadataList) {
            final int metadataId = brief.getId();

            //we need to get the ownerId of metadata, the given brief.getUser can be null
            // especially in case of when using selectAll to call this action by batch.
            //So we get the owner from the metadata pojo to prevent this case
            // and make sure we have the owner of metadata.

            final Metadata metadata = metadataBusiness.getMetadataById(metadataId);
            if(metadata == null) {
                //skip if null, never happen
                continue;
            }
            if(!metadata.getIsValidated()) {
                if(!userId.equals(metadata.getOwner())) {
                    notOwner = true;
                    canContinue = false;
                }
            }else {
                validExists = true;
                canContinue = false;
            }
        }
        final Map<String,String> map = new HashMap<>();
        if(canContinue) {
            try {
                for (final MetadataBrief brief : metadataList) {
                    metadataBusiness.askForValidation(brief.getId());
                }
                map.put("status","ok");
                return Response.ok(map).build();
            }catch(Exception ex) {
                LOGGER.log(Level.WARNING,"Cannot proceed to ask validation for metadata list due to exception error : "+ ex.getLocalizedMessage());
                map.put("msg",ex.getLocalizedMessage());
                return Response.status(403).entity(map).build();
            }
        }


        //TODO send email to moderator here?


        map.put("notOwner",""+notOwner);
        map.put("validExists",""+validExists);
        map.put("status","failed");
        return Response.status(403).entity(map).build();

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

    @GET
    @Path("/metadataJson/resolve/{fileIdentifier}")
    public Response resolveIsoMetadataJson(final @PathParam("fileIdentifier") String fileIdentifier) {
        final StringWriter buffer = new StringWriter();
        try{
            //Resolve metadata by fileIdentifier
            final Metadata md = metadataBusiness.searchFullMetadata(fileIdentifier, false, false);
            if(md != null) {
                final DefaultMetadata metadata = metadataBusiness.getMetadata(md.getId());
                if (metadata != null) {
                    metadata.prune();
                    //get template name
                    final String templateName = md.getProfile();
                    final Template template = Template.getInstance(templateName);
                    template.write(metadata,buffer,true, false);
                }
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

    /**
     * Allow to deactivate the requirement of level completion for metadata validation.
     * Used for developement purpose.
     * @return 
     */
    private boolean isLevelRequiredForValidation() {
        String value = configurationBusiness.getProperty("validation.require.level");
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return true;
    }

}
