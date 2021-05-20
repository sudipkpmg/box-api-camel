package gov.tn.dhs.ecm.service;

import com.box.sdk.*;
import com.eclipsesource.json.JsonObject;
import gov.tn.dhs.ecm.config.AppProperties;
import gov.tn.dhs.ecm.model.FolderCreationRequest;
import gov.tn.dhs.ecm.model.FolderCreationSuccessResponse;
import gov.tn.dhs.ecm.util.ConnectionHelper;
import gov.tn.dhs.ecm.util.JsonUtil;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Iterator;

@Service
public class CreateFolderService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(CreateFolderService.class);

    private static final String CITIZEN_METADATA_TEMPLATE = "CitizenFolderMetadataTemplate";
    private static final String CITIZEN_METADATA_SCOPE = "enterprise";

    private final int SUBFOLDER_LOOKBACK = 10;

    // Changing this to 3 or 10, or something, would make testing easy.
    private final int MAX_FOLDER_SIZE = 20000;

    private final AppProperties appProperties;

    public CreateFolderService(ConnectionHelper connectionHelper, AppProperties appProperties) {
        super(connectionHelper);
        this.appProperties = appProperties;
    }

    public void process(Exchange exchange) {
        FolderCreationRequest folderCreationRequest = exchange.getIn().getBody(FolderCreationRequest.class);
        logger.info("folderCreationRequest = {}", JsonUtil.toJson(folderCreationRequest));
        try {
            BoxDeveloperEditionAPIConnection api = getBoxApiConnection();
            api.asUser(appProperties.getAppUserId());

            String firstName = folderCreationRequest.getFirstName();
            String lastName = folderCreationRequest.getLastName();
            String mpiId = folderCreationRequest.getMpiId();
            String logonUser = folderCreationRequest.getLogonUserId();

            String folderName = null;
            if (!isNullOrEmpty(mpiId)) {
                folderName = "mpi-id-".concat(mpiId);
            }
            if (folderName == null) {
                setupError("400", "Some of the parameters are missing or not valid");
            }

            boolean citizensFolderByIdExists = this.checkIfCitizensFolderExists(api, appProperties.getRootCitizensFolderId(), mpiId);
            if (citizensFolderByIdExists) {
                setupError("409", "Folder already exists!");
            }

            String appUserName = lastName.concat("-" + folderName);

            if (isNullOrEmpty(folderName) || isNullOrEmpty(lastName) || isNullOrEmpty(firstName) || isNullOrEmpty(logonUser)) {
                setupError("400", "Some of the parameters are missing or not valid");
            }

            // Create child folder
            BoxFolder.Info childFolderInfo = this.createCitizensFolder(api, folderName);
            String folderID = childFolderInfo.getID();
            logger.info("Created Folder ID: " + folderID);

            // Create an App User
            BoxUser.Info createdAppUserInfo = BoxUser.createAppUser(api, appUserName);

            // BoxCollaborator
            BoxCollaborator appUser = new BoxUser(api, createdAppUserInfo.getID());
            logger.info("App User Created ID - " + createdAppUserInfo.getID());

            BoxFolder boxFolder = new BoxFolder(api, folderID);
            boxFolder.collaborate(appUser, BoxCollaboration.Role.EDITOR);

            // Apply metadata
            final JsonObject jsonObject = new JsonObject();

            jsonObject.add(appProperties.getCitizenMetadataTemplateFirstName(), firstName);
            jsonObject.add(appProperties.getCitizenMetadataTemplateLastName(), lastName);
            jsonObject.add(appProperties.getCitizenMetadataTemplateMpiId(), mpiId);
            jsonObject.add(appProperties.getCitizenMetadataTemplateLogonUserId(), logonUser);

            String scope = appProperties.getCitizenFolderMetadataTemplateScope();
            Metadata metadata = new Metadata(jsonObject);
            boxFolder.createMetadata(appProperties.getCitizenMetadataTemplate(), scope, metadata);
            // System.out.println("Metadata applied to the folder");

            // Create Metadata Cascade Policy on folder
            boxFolder.addMetadataCascadePolicy(scope, appProperties.getCitizenMetadataTemplate());
            // Note: Why not just use metadata cascade policy for the above metadata creation?
            // Because the Box Metadata Cascade Policy is quite unreliable. In manual testing as
            // of September 2020, it doesn't even reliably apply the metadata on the five children
            // folders here. We still enable it though, in the case a Case Worker or other DHS employee
            // is using Box directly -- files and folders created this way would (hopefully) get the
            // Citizens Metadata applied to them by the cascade policy.

            FolderCreationSuccessResponse folderCreationSuccessResponse = new FolderCreationSuccessResponse();
            folderCreationSuccessResponse.setAppUserId(createdAppUserInfo.getID());
            folderCreationSuccessResponse.setFolderId(folderID);
            logger.info(JsonUtil.toJson(folderCreationSuccessResponse));
            setupResponse(exchange, "200", folderCreationSuccessResponse);
        } catch (BoxAPIException e) {
            int code = e.getResponseCode();
            String message = "Internal server error";
            switch (e.getResponseCode()) {
                case 400:
                    message = "Some of the parameters are missing or not valid";
                    break;
                case 403:
                    message = "User does not have the required access to perform the action";
                    break;
                case 404:
                    message = "The parent folder could not be found, or the authenticated user does not have access to the parent folder";
                    break;
                case 409:
                    message = "Folder already exists";
                    break;
            }
            setupError(Integer.toString(code), message);
        }
    }

    public static boolean isNullOrEmpty(String str) {
        if (str != null && !str.isEmpty() && str.length() != 0)
            return false;
        return true;
    }

    private BoxFolder.Info getSubfolderForCreation(BoxAPIConnection api, BoxFolder folder) {
        // Make a Box API call to Folder items and specify sort field and order.
        // This will get all subfolders from the most-recent first, and then we pick the
        // smallest current subfolder of the first N. This is done as an estimation
        // for the absoulte correct way of iterating through all subfolders (possibly many
        // subfolders) to find the subfolder with the fewest items in it (potentially very
        // slow).

        // This should be class constants or application-level configurations, probably.
        Iterator<BoxItem.Info> itemIterator = folder.getChildren("date", BoxFolder.SortDirection.DESC, "item_collection").iterator();
        BoxFolder.Info subFolder = null;
        int numFolders = SUBFOLDER_LOOKBACK;
        long currentMinValue = Long.MAX_VALUE;
        while (itemIterator.hasNext()) {
            BoxItem.Info itemInfo = itemIterator.next();
            if (itemInfo instanceof BoxFolder.Info) {
                BoxFolder.Info folderInfo = (BoxFolder.Info) itemInfo;
                // check and compare folderInfo item_collection.total_count with that of subFolders
                // and pick the folder with the smaller
                BoxFolder boxFolder = new BoxFolder(api, folderInfo.getID());
                long childItemCount = boxFolder.getChildrenRange(0, 1).fullSize();
                if (childItemCount < currentMinValue) {
                    subFolder = folderInfo;
                }
                numFolders--;
            }
            if (numFolders < 1) {
                break;
            }
        }
        return subFolder; // could still be null
    }

    /**
     * Gets the size of the folder (not recursive, only returns the number of items in the first
     * level of the folder).
     */
    private long getFolderSize(BoxDeveloperEditionAPIConnection api, String folderId) {
        BoxFolder folder = new BoxFolder(api, folderId);
        PartialCollection<BoxItem.Info> items = folder.getChildrenRange(0, 1);
        return items.fullSize();
    }

    private BoxFolder.Info createCitizensFolder(BoxDeveloperEditionAPIConnection api, String name) {
        // The root of all Citizens Folders
        BoxFolder rootNode = new BoxFolder(api, appProperties.getRootCitizensFolderId());

        BoxFolder.Info superNode = this.getSuperNode(api, rootNode);

        final int MAX_FOLDER_SIZE = appProperties.getMaxCitizensFoldersPerSubfolder();


        if (superNode == null || this.getFolderSize(api, superNode.getID()) >= MAX_FOLDER_SIZE) {
            String randomUUID = java.util.UUID.randomUUID().toString();
            String superNodeName = "supernode-".concat(randomUUID);
            superNode = rootNode.createFolder(superNodeName);
            BoxFolder superNodeFolder = new BoxFolder(api, superNode.getID());
            return superNodeFolder.createFolder(name);
        } else {
            BoxFolder superNodeFolder = new BoxFolder(api, superNode.getID());
            return superNodeFolder.createFolder(name);
        }
    }

    private BoxFolder.Info getSuperNode(BoxDeveloperEditionAPIConnection api, BoxFolder rootNode) {
        // Make a Box API call to Folder items and specify sort field and order.
        // This will get all subfolders from the most-recent first, and then we pick the
        // smallest current subfolder of the first N. This is done as an estimation
        // for the absoultely correct way of iterating through all subfolders (possibly many
        // subfolders) to find the subfolder with the fewest items in it (potentially very
        // slow if there are many subfolders).
        final int SUBFOLDER_LOOKBACK = appProperties.getCitizensFolderIterationLookback();

        String sortField = "date";
        BoxFolder.SortDirection sortDirection = BoxFolder.SortDirection.DESC;
        Iterator<BoxItem.Info> itemIterator = rootNode.getChildren(sortField, sortDirection).iterator();

        BoxFolder.Info superNode = null;
        long superNodeSize = 0;
        int numFolders = SUBFOLDER_LOOKBACK;
        while (itemIterator.hasNext()) {
            BoxItem.Info itemInfo = itemIterator.next();
            if (itemInfo instanceof BoxFolder.Info) {
                BoxFolder.Info folderInfo = (BoxFolder.Info) itemInfo;
                // Check and compare each `folderInfo` size to select the folder with the minimum size
                if (superNode == null) {
                    superNode = folderInfo;
                    superNodeSize = this.getFolderSize(api, folderInfo.getID());
                } else {
                    long folderSize = this.getFolderSize(api, folderInfo.getID());
                    if (folderSize < superNodeSize) {
                        superNode = folderInfo;
                        superNodeSize = folderSize;
                    }
                }

                numFolders--;
            } else {
                // Order of items returned are folders -> files -> weblinks.
                // Meaning, we should never get here, because the Citizens Folder root
                // should only contain folders directly under it.
                // Even if we did, we intentionally don't want to decrement numFolders.
            }
            if (numFolders < 1) {
                break;
            }
        }
        // could still be null -- caller should handle
        return superNode;
    }

    /**
     * Use Box's Metadata query ability to check if the Citizen's folder for the given mpi id already exists.
     * Returns true if any files/folders in the root citizens folder exist with the given mpi id, and false otherwise.
     *
     * The Box API will return accurate results as soon as metadata has been added, removed, updated or deleted for a file or folder
     *     https://developer.box.com/guides/metadata/queries/comparison/
     *
     * Note: This ability relies on a Metadata Query Index to be created in Box:
     *       https://developer.box.com/guides/metadata/queries/indexes/
     *
     * Todo: Call in to Box Support to request the query index needed.
     */
    private boolean checkIfCitizensFolderExists(BoxDeveloperEditionAPIConnection api, String rootCitizensFolderId, String mpiId) {
        String citizenMetadataTemplate = appProperties.getCitizenMetadataTemplate();
        MetadataTemplate metadataTemplate = null;
        try {
            metadataTemplate = MetadataTemplate.getMetadataTemplate(api, citizenMetadataTemplate);
        } catch (BoxAPIException e) {
            e.printStackTrace();
        }
        String metadataScope = metadataTemplate.getScope();
        String from = String.format("%s.%s", metadataScope, appProperties.getCitizenMetadataTemplate());

        String query = "";
        JsonObject queryParameters = new JsonObject();
        if ( (mpiId != null) && (mpiId.length() > 0) ) {
            query = appProperties.getCitizenMetadataTemplateMpiId() + " = :mpiidArg";
            queryParameters.add("mpiidArg", mpiId);
        } else {
            return false;
        }

        BoxResourceIterable<BoxMetadataQueryItem> results = null;
        try {
            results = MetadataTemplate.executeMetadataQuery(api, from, query, queryParameters, rootCitizensFolderId);
        } catch (Exception e) {
            return false;
        }
        return results.iterator().hasNext();
    }

}
