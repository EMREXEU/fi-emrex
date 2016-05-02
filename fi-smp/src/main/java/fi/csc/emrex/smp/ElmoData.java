package fi.csc.emrex.smp;

/**
 * Created by marko.hollanti on 20/08/15.
 */
public class ElmoData {

    private String sessionId;
    private String elmo;
    private String returnCode;
    private String returnMessage;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getElmo() {
        return elmo;
    }

    public void setElmo(String elmo) {
        this.elmo = elmo;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getReturnMessage() {
        return returnMessage;
    }

    public void setReturnMessage(String returnMessage) {
        this.returnMessage = returnMessage;
    }


}
