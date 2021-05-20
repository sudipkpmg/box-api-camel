package gov.tn.dhs.ecm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResult {

    @JsonProperty("complete")
    private String complete;

    @JsonProperty("fileData")
    private List<FileInfo> fileData;

    public String getComplete() {
        return complete;
    }

    public void setComplete(String complete) {
        this.complete = complete;
    }

    public List<FileInfo> getFileData() {
        return fileData;
    }

    public void setFileData(List<FileInfo> fileData) {
        this.fileData = fileData;
    }
}
