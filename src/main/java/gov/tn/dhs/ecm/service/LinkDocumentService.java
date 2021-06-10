package gov.tn.dhs.ecm.service;

import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxSharedLink;
import gov.tn.dhs.ecm.config.BoxProperties;
import gov.tn.dhs.ecm.model.DocumentLinkResult;
import gov.tn.dhs.ecm.util.ConnectionHelper;
import gov.tn.dhs.ecm.util.JsonUtil;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class LinkDocumentService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(LinkDocumentService.class);

    private BoxProperties boxProperties;

    public LinkDocumentService(
            ConnectionHelper connectionHelper,
            BoxProperties boxProperties
    ) {
        super(connectionHelper);
        this.boxProperties = boxProperties;
    }

    public void process(Exchange exchange) {
        String appUserId = exchange.getIn().getHeader("appUserId", String.class);
        String fileId = exchange.getIn().getHeader("fileId", String.class);

        logger.info("appUserId = {}", appUserId);
        logger.info("fileId = {}", fileId);

        BoxDeveloperEditionAPIConnection api = getBoxApiConnection();
        api.asUser(appUserId);

        try {
            BoxFile file = new BoxFile(api, fileId);
            Date currentDate = new Date();
            long currentDateTimeval = currentDate.getTime();
            long linkDuration = boxProperties.getDocumentLinkDuration();
            long unshareDateTimeval = currentDateTimeval + linkDuration;
            Date unshareDate = new Date(unshareDateTimeval);
            BoxSharedLink.Permissions permissions = new BoxSharedLink.Permissions();
            permissions.setCanPreview(true);
            permissions.setCanDownload(true);
            BoxSharedLink sharedLink = file.createSharedLink(BoxSharedLink.Access.OPEN, unshareDate, permissions);
            String linkUrl = sharedLink.getURL();
            DocumentLinkResult documentLinkResult = new DocumentLinkResult();
            documentLinkResult.setLinkUrl(linkUrl);
            logger.info("documentLinkResult = {}", JsonUtil.toJson(documentLinkResult));
            setupResponse(exchange, "200", documentLinkResult);
        } catch (BoxAPIException e) {
            switch (e.getResponseCode()) {
                case 404: {
                    setupError("409", "Document not found");
                }
                default: {
                    setupError("500", "Document view error");
                }
            }
        }
    }

}



