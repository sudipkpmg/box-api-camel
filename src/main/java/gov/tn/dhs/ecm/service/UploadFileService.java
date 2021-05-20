package gov.tn.dhs.ecm.service;

import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import gov.tn.dhs.ecm.model.UploadFileResponse;
import gov.tn.dhs.ecm.util.ConnectionHelper;
import gov.tn.dhs.ecm.util.JsonUtil;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import java.io.IOException;
import java.io.InputStream;

@Service
public class UploadFileService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(UploadFileService.class);

    public UploadFileService(ConnectionHelper connectionHelper) {
        super(connectionHelper);
    }

    public void process(Exchange exchange) {
        String appUserId = exchange.getIn().getHeader("appUserId", String.class);
        String boxFolderId = exchange.getIn().getHeader("boxFolderId", String.class);

        logger.info("appUserId = {}", appUserId);
        logger.info("boxFolderId = {}", boxFolderId);

        InputStream is = exchange.getIn().getBody(InputStream.class);
        try {
            MimeBodyPart mimeMessage = new MimeBodyPart(is);
            DataHandler dh = mimeMessage.getDataHandler();
            try (InputStream fileStream = dh.getInputStream()) {
                String fileName = dh.getName();
                UploadFileResponse uploadFileResponse = uploadToBox(fileStream, fileName, boxFolderId, appUserId);
                logger.info("uploadFileResponse = {}", JsonUtil.toJson(uploadFileResponse));
                setupResponse(exchange, "200", JsonUtil.toJson(uploadFileResponse), String.class);
            } catch (IOException e) {
                setupError("500", "File stream for uploaded file could not be read");
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private UploadFileResponse uploadToBox(InputStream inputStream, String fileName, String boxFolderId, String appUserId) {
        BoxDeveloperEditionAPIConnection api = getBoxApiConnection();
        api.asUser(appUserId);
        BoxFolder parentFolder = null;
        try {
            parentFolder = new BoxFolder(api, boxFolderId);
            BoxFolder.Info info = parentFolder.getInfo();
            logger.info("Parent Folder with ID {} and name {} found", boxFolderId, info.getName());
        } catch (BoxAPIException e) {
            setupError("400", "Folder not found");
        }
        BoxFile.Info newFileInfo = null;
        try {
            newFileInfo = parentFolder.uploadFile(inputStream, fileName);
        } catch (BoxAPIException e) {
            setupError("409", "File with the same name already exists");
        } catch (Exception e) {
            setupError("500", "Service error");
        }
        String fileId = newFileInfo.getID();
        UploadFileResponse uploadFileResponse = new UploadFileResponse();
        uploadFileResponse.setStatus("File upload completed");
        uploadFileResponse.setFileId(fileId);
        return uploadFileResponse;
    }

}
