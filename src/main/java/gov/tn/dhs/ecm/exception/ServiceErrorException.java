package gov.tn.dhs.ecm.exception;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceErrorException extends RuntimeException  {

    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;

    public ServiceErrorException(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
