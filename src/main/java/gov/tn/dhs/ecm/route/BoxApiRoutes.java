package gov.tn.dhs.ecm.route;

import com.fasterxml.jackson.core.JsonParseException;
import gov.tn.dhs.ecm.exception.ServiceErrorException;
import gov.tn.dhs.ecm.model.*;
import gov.tn.dhs.ecm.service.*;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
class BoxApiRoutes extends RouteBuilder {

    public final CreateFolderService createFolderService;

    public final DownloadFileService downloadFileService;

    public final UploadFileService uploadFileService;

    public final SearchService searchService;

    public final DeleteDocumentService deleteDocumentService;

    public final ViewDocumentService viewDocumentService;

    public final LinkDocumentService linkDocumentService;

    @Value("${server.port}")
    private String serverPort;

    @Value("${runStatus}")
    private String runStatus;

    public BoxApiRoutes(
            CreateFolderService createFolderService,
            DownloadFileService downloadFileService,
            UploadFileService uploadFileService,
            SearchService searchService,
            DeleteDocumentService deleteDocumentService,
            ViewDocumentService viewDocumentService,
            LinkDocumentService linkDocumentService
    ) {
        this.createFolderService = createFolderService;
        this.downloadFileService = downloadFileService;
        this.uploadFileService = uploadFileService;
        this.searchService = searchService;
        this.deleteDocumentService = deleteDocumentService;
        this.viewDocumentService = viewDocumentService;
        this.linkDocumentService = linkDocumentService;
    }

    @Override
    public void configure() {

        onException(JsonParseException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(constant("{}"))
        ;

        onException(Exception.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(simple("${exception.message}"))
        ;

        onException(ServiceErrorException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, simple("${exception.code}"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(simple("${exception.message}"))
        ;

        restConfiguration()
                .enableCORS(true)
                .apiProperty("cors", "true") // cross-site
                .component("servlet")
                .port(serverPort)
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true");

        defineStatusPath();

        defineCreateFolderPath();

        defineDownloadFilePath();

        defineUploadFilePath();

        defineSearchPath();

        defineDeleteDocumentPath();

        defineViewDocumentPath();

        defineLinkDocumentPath();

    }

    private void defineStatusPath() {
        SimpleMessage simpleMessage = new SimpleMessage(runStatus);
        rest()
                .get("/")
                .to("direct:runningStatus")
        ;
        from("direct:runningStatus")
                .log("Status request sent")
                .log("runStatus property value is " + runStatus)
                .process(exchange -> exchange.getIn().setBody(simpleMessage, SimpleMessage.class))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setHeader("Content-Type", constant("application/json"))
                .endRest()
        ;
    }

    private void defineCreateFolderPath() {
        rest()
                .post("/folder")
                .type(FolderCreationRequest.class)
                .outType(FolderCreationSuccessResponse.class)
                .to("direct:createFolderService")
        ;
        from("direct:createFolderService")
                .routeId("route_post_folder")
                .bean(createFolderService)
                .endRest()
        ;
    }

    private void defineDownloadFilePath() {
        rest()
                .get("/file")
                .outType(byte[].class)
                .to("direct:downloadFileService")
        ;
        from("direct:downloadFileService")
                .routeId("route_get_file")
                .bean(downloadFileService)
                .endRest()
                ;
    }

    private void defineUploadFilePath() {
        rest()
                .bindingMode(RestBindingMode.off)
                .post("/file")
                .outType(String.class)
                .to("direct:uploadFileService")
        ;
        from("direct:uploadFileService")
                .routeId("route_post_file")
                .bean(uploadFileService)
                .endRest()
        ;
    }

    private void defineSearchPath() {
        rest()
                .get("/folder")
                .outType(SearchResult.class)
                .to("direct:searchService")
        ;
        from("direct:searchService")
                .routeId("route_get_folder")
                .bean(searchService)
                .endRest()
        ;
    }

    private void defineDeleteDocumentPath() {
        rest()
                .delete("/file")
                .outType(DocumentDeletionResult.class)
                .to("direct:deleteDocumentService")
        ;
        from("direct:deleteDocumentService")
                .routeId("route_delete_file")
                .bean(deleteDocumentService)
                .endRest()
        ;
    }

    private void defineViewDocumentPath() {
        rest()
                .get("/file-view")
                .outType(DocumentViewResult.class)
                .to("direct:viewDocumentService")
        ;
        from("direct:viewDocumentService")
                .routeId("route_get_file_view")
                .bean(viewDocumentService)
                .endRest()
        ;
    }

    private void defineLinkDocumentPath() {
        rest()
                .get("/file-link")
                .outType(DocumentLinkResult.class)
                .to("direct:linkDocumentService")
        ;
        from("direct:linkDocumentService")
                .routeId("route_get_file_link")
                .bean(linkDocumentService)
                .endRest()
        ;
    }

}
