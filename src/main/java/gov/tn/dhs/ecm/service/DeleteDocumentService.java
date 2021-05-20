package gov.tn.dhs.ecm.service;

import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import gov.tn.dhs.ecm.model.DocumentDeletionResult;
import gov.tn.dhs.ecm.util.ConnectionHelper;
import gov.tn.dhs.ecm.util.JsonUtil;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DeleteDocumentService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(DeleteDocumentService.class);

    public DeleteDocumentService(ConnectionHelper connectionHelper) {
        super(connectionHelper);
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
            file.delete();
        } catch (BoxAPIException e) {
            switch (e.getResponseCode()) {
                case 404: {
                    setupError("404", "Document not found");
                }
                default: {
                    setupError("500", "Document deletion error");
                }
            }
        }

        DocumentDeletionResult documentDeletionResult = new DocumentDeletionResult();
        documentDeletionResult.setMessage("file successfully deleted");
        logger.info("documentDeletionResult = {}", JsonUtil.toJson(documentDeletionResult));
        setupResponse(exchange, "200", documentDeletionResult);
    }

}


