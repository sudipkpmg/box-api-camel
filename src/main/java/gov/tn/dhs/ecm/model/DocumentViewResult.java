package gov.tn.dhs.ecm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentViewResult {

    @JsonProperty("previewUrl")
    private String previewUrl;

    @JsonProperty("shortPreviewUrl")
    private String shortPreviewUrl;

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getShortPreviewUrl() {
        return shortPreviewUrl;
    }

    public void setShortPreviewUrl(String shortPreviewUrl) {
        this.shortPreviewUrl = shortPreviewUrl;
    }

}


