package gov.tn.dhs.ecm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class CitizenMetadata   {

    @JsonProperty("logon_user_id")
    private String logonUserId;

    @JsonProperty("mpi_id")
    private String mpiId;

    @JsonProperty("sys_id")
    private String sysId;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("dob")
    private LocalDate dob;

    @JsonProperty("ssn4")
    private String ssn4;

    public String getLogonUserId() {
        return logonUserId;
    }

    public void setLogonUserId(String logonUserId) {
        this.logonUserId = logonUserId;
    }

    public String getMpiId() {
        return mpiId;
    }

    public void setMpiId(String mpiId) {
        this.mpiId = mpiId;
    }

    public String getSysId() {
        return sysId;
    }

    public void setSysId(String sysId) {
        this.sysId = sysId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getSsn4() {
        return ssn4;
    }

    public void setSsn4(String ssn4) {
        this.ssn4 = ssn4;
    }

}

