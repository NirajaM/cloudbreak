package com.sequenceiq.cloudbreak.api.model;

import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sequenceiq.cloudbreak.doc.ModelDescriptions.StackModelDescription;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class KerberosRequest implements JsonEntity {

    @ApiModelProperty(StackModelDescription.KERBEROS_MASTER_KEY)
    @Size(max = 50, min = 3, message = "The length of the Kerberos password has to be in range of 3 to 50")
    private String masterKey;

    @ApiModelProperty(StackModelDescription.KERBEROS_ADMIN)
    @Size(max = 15, min = 5, message = "The length of the Kerberos admin has to be in range of 5 to 15")
    private String admin;

    @ApiModelProperty(StackModelDescription.KERBEROS_PASSWORD)
    @Size(max = 50, min = 5, message = "The length of the Kerberos password has to be in range of 5 to 50")
    private String password;

    @ApiModelProperty(StackModelDescription.KERBEROS_KDC_URL)
    private String url;

    @ApiModelProperty(StackModelDescription.KERBEROS_ADMIN_URL)
    private String adminUrl;

    private String realm;

    @ApiModelProperty(StackModelDescription.KERBEROS_PRINCIPAL)
    private String principal;

    private String ldapUrl;

    private String containerDn;

    private Boolean tcpAllowed = false;

    private String descriptor;

    private String krb5Conf;

    public String getMasterKey() {
        return masterKey;
    }

    public void setMasterKey(String masterKey) {
        this.masterKey = masterKey;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAdminUrl() {
        return adminUrl;
    }

    public void setAdminUrl(String adminUrl) {
        this.adminUrl = adminUrl;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public Boolean getTcpAllowed() {
        return tcpAllowed;
    }

    public void setTcpAllowed(Boolean tcpAllowed) {
        this.tcpAllowed = tcpAllowed;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getLdapUrl() {
        return ldapUrl;
    }

    public void setLdapUrl(String ldapUrl) {
        this.ldapUrl = ldapUrl;
    }

    public String getContainerDn() {
        return containerDn;
    }

    public void setContainerDn(String containerDn) {
        this.containerDn = containerDn;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public String getKrb5Conf() {
        return krb5Conf;
    }

    public void setKrb5Conf(String krb5Conf) {
        this.krb5Conf = krb5Conf;
    }
}
