/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.smp.model;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.json.XML;
/**
 *
 * @author salum
 */
public class VerifiedReport {

    private VerificationReply verification;
    private String report;
    private final ArrayList<String> messages;

    public VerifiedReport() {
        messages = new ArrayList<String>();
    }

    public VerificationReply getVerification() {
        return verification;
    }

    public void setVerification(VerificationReply verification) {
        this.verification = verification;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        //System.out.println(report);
        JSONObject json =  XML.toJSONObject(report);
        //System.out.println(json.toString());
        this.report = json.toString();
    }
    public void addMessage(String msg) {
        messages.add(msg);
    }
    public List<String> getMessages() {
        List<String> reply = new ArrayList<>();
        reply.addAll(this.messages);
        if (this.verification != null) {
            reply.addAll(this.verification.getMessages());
        }

        return reply;
    }
}
