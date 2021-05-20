package gov.tn.dhs.ecm.service;

import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import gov.tn.dhs.ecm.model.DocumentViewResult;
import gov.tn.dhs.ecm.model.ShortUrlRequest;
import gov.tn.dhs.ecm.model.ShortUrlRequestDomain;
import gov.tn.dhs.ecm.util.ConnectionHelper;
import gov.tn.dhs.ecm.util.JsonUtil;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
public class ViewDocumentService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(ViewDocumentService.class);

    public ViewDocumentService(ConnectionHelper connectionHelper) {
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
            URL previewUrl = file.getPreviewLink();
            String previewUrlString = previewUrl.toString();
            String shortUrl = getShortPreviewUrl(previewUrlString);
            logger.info("previewUrl = {}", previewUrl);
            DocumentViewResult documentViewResult = new DocumentViewResult();
            documentViewResult.setPreviewUrl(previewUrlString);
            documentViewResult.setShortPreviewUrl(shortUrl);
            logger.info("documentViewResult = {}", JsonUtil.toJson(documentViewResult));
            setupResponse(exchange, "200", documentViewResult);
        } catch (BoxAPIException e) {
            logger.error(e.getMessage());
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

    private String getShortPreviewUrl(String url) {
        ShortUrlRequestDomain domain = new ShortUrlRequestDomain();
        domain.setFullname("rebrand.ly");
        ShortUrlRequest shortUrlRequest = new ShortUrlRequest();
        shortUrlRequest.setDestination(url);
        shortUrlRequest.setDomain(domain);
        HttpResponse<JsonNode> response = Unirest.post("https://api.rebrandly.com/v1/links")
                .header("Content-Type", "application/json")
                .header("apikey", "b6dcfd08101e4972965087d77d1b1f9f")
                .body(shortUrlRequest)
                .asJson();
        String shortUrl = "http://" + response.getBody().getObject().getString("shortUrl");
        logger.info("shortUrl = {}", shortUrl);
        return shortUrl;
    }


}



