/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.smp.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author salum
 */
public class Person {

    @Value("${smp.verification.threshold}")
    private double threshold ;

    private String firstName;
    private String lastName;
    /**
     * Format: 0 Not known 1 Male 2 Female 9 Not specified
     */
    private int gender;
    private LocalDate birthDate;
    private DateTimeFormatter dateFormatter;
    private String homeOrganization;

    public Person() {

    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
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

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setGender(String gender) {
        Integer temp = Integer.getInteger(gender);
        if (temp == null) {
            this.gender = 9;
        } else {
            this.gender = temp;
        }
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

    public VerificationReply verifiy(Person otherPerson) {
        //TODO implement verification algorithm;
        VerificationReply r = new VerificationReply();
        r.setFullNameFromHomeInstitute(this.getFullName());
        r.setFullNameInElmo(otherPerson.getFullName());
        boolean bdMatch = false;
        boolean nameMatch = false;
        int match = 0;
        LocalDate vbd = this.birthDate;
        LocalDate ebd = otherPerson.getBirthDate();

        if (ebd == null || vbd == null) {
            r.addMessage("Birth date not set for " + (ebd == null ? "elmo" : "local") + " person.");
        } else if (!ebd.equals(vbd)) {
            r.addMessage("Birth date does not match.");

        } else {
            bdMatch = true;
        }
        double score = 0;
        score += levenshteinDistance(this.getLastName(), otherPerson.getLastName());
        score += levenshteinDistance(this.getFirstName(), otherPerson.getFirstName());
        double ratio = StringUtils.isNotBlank(this.getFullName()) ? score / this.getFullName().length() : 0.0;
        r.addMessage("Error ratio " + ratio + " based on Levenshtein check on name.");
        if (ratio > this.threshold) {
            r.addMessage("Ratio over threshold "+threshold);
        } else {
            nameMatch = true;
        }

        r.setVerified(bdMatch && nameMatch);

        return r;
    }

    private static int levenshteinDistance(String s, String t) {
        if (s == null && t == null) {
            return 0;
        }
        if (s == null || s.length() == 0) {
            return t.length();
        }
        if (t == null || t.length() == 0) {
            return s.length();
        }

        int[] v0 = new int[t.length() + 1];
        int[] v1 = new int[t.length() + 1];

        for (int i = 0; i < v0.length; i++) {
            v0[i] = i;
        }

        for (int i = 0; i < s.length(); i++) {
            // calculate v1 (current row distances) from the previous row v0

            // first element of v1 is A[i+1][0]
            // edit distance is delete (i+1) chars from s to match empty t
            v1[0] = i + 1;

            // use formula to fill in the rest of the row
            for (int j = 0; j < t.length(); j++) {
                int cost = (s.charAt(i) == t.charAt(j)) ? 0 : 1;
                v1[j + 1] = Math.min(Math.min(v1[j] + 1, v0[j + 1] + 1), v0[j] + cost);
            }

            // copy v1 (current row) to v0 (previous row) for next iteration
            for (int j = 0; j < v0.length; j++) {
                v0[j] = v1[j];
            }
        }

        return v1[t.length()];
    }

}
