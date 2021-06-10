package gov.tn.dhs.ecm.util;

import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import gov.tn.dhs.ecm.config.BoxProperties;
import org.springframework.stereotype.Component;

@Component
public class ConnectionHelper {

    private final BoxProperties boxProperties;

    public ConnectionHelper(BoxProperties boxProperties) {
        this.boxProperties = boxProperties;
    }

    public BoxDeveloperEditionAPIConnection getBoxDeveloperEditionAPIConnection() {
        String clientId = boxProperties.getClientID();
        String clientSecret = boxProperties.getClientSecret();
        String enterpriseID = boxProperties.getEnterpriseID();
        String publicKeyID = boxProperties.getPublicKeyID();
        String privateKey = boxProperties.getPrivateKey();
        String passphrase = boxProperties.getPassphrase();
        BoxConfig boxConfig = new BoxConfig(
                clientId,
                clientSecret,
                enterpriseID,
                publicKeyID,
                privateKey,
                passphrase
        );
        BoxDeveloperEditionAPIConnection api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig);
        return api;
    }

}
