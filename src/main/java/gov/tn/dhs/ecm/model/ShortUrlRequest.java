package gov.tn.dhs.ecm.model;

public class ShortUrlRequest {

    private String destination;

    private ShortUrlRequestDomain domain;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public ShortUrlRequestDomain getDomain() {
        return domain;
    }

    public void setDomain(ShortUrlRequestDomain domain) {
        this.domain = domain;
    }
}
