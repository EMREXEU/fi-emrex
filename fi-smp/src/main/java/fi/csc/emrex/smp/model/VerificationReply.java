/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.smp.model;

import java.util.ArrayList;
import java.util.List;

public class VerificationReply {

    private String sessionId;

    private int score;

    private boolean verified;

    private String fullNameInElmo;

    private String fullNameFromHomeInstitute;


    private final List<String> messages;


    public String getFullNameFromHomeInstitute() {
        return fullNameFromHomeInstitute;
    }

    public void setFullNameFromHomeInstitute(String fullNameFromHomeInstitute) {
        this.fullNameFromHomeInstitute = fullNameFromHomeInstitute;
    }

    public String getFullNameInElmo() {
        return fullNameInElmo;
    }

    public void setFullNameInElmo(String fullNameInElmo) {
        this.fullNameInElmo = fullNameInElmo;
    }

    public VerificationReply() {
        messages = new ArrayList<String>();
    }


    public String getSessionId() {
        return sessionId;
    }


    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }


    public int getScore() {
        return score;
    }


    public void setScore(int score) {
        this.score = score;
    }


    public boolean isVerified() {
        return verified;
    }


    public void setVerified(boolean verified) {
        this.verified = verified;
    }


    public List<String> getMessages() {
        return messages;
    }


    public void addMessage(String msg) {
        messages.add(msg);
    }

}
