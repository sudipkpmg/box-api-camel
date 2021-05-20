package gov.tn.dhs.ecm.service;

import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import gov.tn.dhs.ecm.util.ConnectionHelper;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class DownloadFileService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(DownloadFileService.class);

    public DownloadFileService(ConnectionHelper connectionHelper) {
        super(connectionHelper);
    }

    public void process(Exchange exchange) {
        String appUserId = exchange.getIn().getHeader("appUserId", String.class);
        String fileId = exchange.getIn().getHeader("fileId", String.class);

        logger.info("appUserId = {}", appUserId);
        logger.info("fileId = {}", fileId);

        BoxDeveloperEditionAPIConnection api = getBoxApiConnection();
        api.asUser(appUserId);

        BoxFile file = null;
        BoxFile.Info info = null;
        try {
            file = new BoxFile(api, fileId);
            info = file.getInfo();
        } catch (BoxAPIException e) {
            setupError("400", "File not found");
        }
        try {
            String fileName = info.getName();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            file.download(outputStream);
            final byte[] bytes = outputStream.toByteArray();
            String fileNameSuggestion = String.format("attachment; filename=\"%s\"", fileName);
            logger.info("retrieved file {}", fileNameSuggestion);
            exchange.getIn().setHeader("Content-Disposition", fileNameSuggestion);
            setupOctetStreamResponse(exchange, "200", bytes);
        } catch (Exception ex) {
            setupError("500", "Download error");
        }

    }

}
