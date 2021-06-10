package gov.tn.dhs.ecm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("box")
public class BoxProperties {

    private String clientID;
    private String clientSecret;
    private String publicKeyID;
    private String privateKey;
    private String passphrase;
    private String enterpriseID;
    private String rootCitizensFolderId;
    private String appUserId;

    private int maxCitizensFoldersPerSubfolder;
    private int citizensFolderIterationLookback;

    private int documentLinkDuration;

    private String citizenMetadataTemplate;
    private String citizenFolderMetadataTemplateScope;

    private String citizenMetadataTemplateFirstName;
    private String citizenMetadataTemplateLastName;
    private String citizenMetadataTemplateMpiId;
    private String citizenMetadataTemplateLogonUserId;

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getPublicKeyID() {
        return publicKeyID;
    }

    public void setPublicKeyID(String publicKeyID) {
        this.publicKeyID = publicKeyID;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public String getAppUserId() {
        return appUserId;
    }

    public void setAppUserId(String appUserId) {
        this.appUserId = appUserId;
    }

    public String getEnterpriseID() {
        return enterpriseID;
    }

    public void setEnterpriseID(String enterpriseID) {
        this.enterpriseID = enterpriseID;
    }

    public String getRootCitizensFolderId() {
        return rootCitizensFolderId;
    }

    public void setRootCitizensFolderId(String rootCitizensFolderId) {
        this.rootCitizensFolderId = rootCitizensFolderId;
    }

    public int getMaxCitizensFoldersPerSubfolder() {
        return this.maxCitizensFoldersPerSubfolder;
    }

    public void setMaxCitizensFoldersPerSubfolder(int maxCitizensFoldersPerSubfolder) {
        this.maxCitizensFoldersPerSubfolder = maxCitizensFoldersPerSubfolder;
    }

    public int getCitizensFolderIterationLookback() {
        return this.citizensFolderIterationLookback;
    }

    public void setCitizensFolderIterationLookback(int citizensFolderIterationLookback) {
        this.citizensFolderIterationLookback = citizensFolderIterationLookback;
    }

    public String getCitizenMetadataTemplate() {
        return citizenMetadataTemplate;
    }

    public void setCitizenMetadataTemplate(String citizenMetadataTemplate) {
        this.citizenMetadataTemplate = citizenMetadataTemplate;
    }

    public String getCitizenFolderMetadataTemplateScope() {
        return citizenFolderMetadataTemplateScope;
    }

    public void setCitizenFolderMetadataTemplateScope(String citizenFolderMetadataTemplateScope) {
        this.citizenFolderMetadataTemplateScope = citizenFolderMetadataTemplateScope;
    }

    public String getCitizenMetadataTemplateFirstName() {
        return citizenMetadataTemplateFirstName;
    }

    public void setCitizenMetadataTemplateFirstName(String citizenMetadataTemplateFirstName) {
        this.citizenMetadataTemplateFirstName = citizenMetadataTemplateFirstName;
    }

    public String getCitizenMetadataTemplateLastName() {
        return citizenMetadataTemplateLastName;
    }

    public void setCitizenMetadataTemplateLastName(String citizenMetadataTemplateLastName) {
        this.citizenMetadataTemplateLastName = citizenMetadataTemplateLastName;
    }

    public String getCitizenMetadataTemplateMpiId() {
        return citizenMetadataTemplateMpiId;
    }

    public void setCitizenMetadataTemplateMpiId(String citizenMetadataTemplateMpiId) {
        this.citizenMetadataTemplateMpiId = citizenMetadataTemplateMpiId;
    }

    public String getCitizenMetadataTemplateLogonUserId() {
        return citizenMetadataTemplateLogonUserId;
    }

    public void setCitizenMetadataTemplateLogonUserId(String citizenMetadataTemplateLogonUserId) {
        this.citizenMetadataTemplateLogonUserId = citizenMetadataTemplateLogonUserId;
    }

    public int getDocumentLinkDuration() {
        return documentLinkDuration;
    }

    public void setDocumentLinkDuration(int documentLinkDuration) {
        this.documentLinkDuration = documentLinkDuration;
    }
}
