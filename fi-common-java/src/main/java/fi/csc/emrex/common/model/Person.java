/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.common.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


/**
 *
 * @author salum
 */
public class Person {



    private String firstName;
    private String lastName;

    private LocalDate birthDate;
    private DateTimeFormatter dateFormatter;
    private String homeOrganization;
    private String OID;
    private String SSN; //HETU
    private String heiOid;

    public Person() {

    }

    public String getFullName() {
        if (firstName != null || lastName != null) {
            return firstName + " " + lastName;
        } else {
            return null;
        }
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

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getHomeOrganization() {
        return homeOrganization;
    }

    public void setHomeOrganization(String homeOrganization) {
        this.homeOrganization = homeOrganization;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setBirthDate(String birthDate, String dateFormat) {
        dateFormatter = DateTimeFormatter.ofPattern(dateFormat);
        if (birthDate == null || dateFormat == null) {
            this.birthDate = null;
        } else {
            this.birthDate = LocalDate.parse(birthDate, dateFormatter);
        }
    }

    public String getOID() {
        return OID;
    }

    public void setOID(String OID) {
        this.OID = OID;
    }

    public String getSSN() {
        return SSN;
    }

    public void setSSN(String SSN) {
        this.SSN = SSN;
    }

    public void setHeiOid(String heiOid) {
        this.heiOid = heiOid;
    }

    public String getHeiOid() {
        return heiOid;
    }
}
