package fi.csc.emrex.ncp;

/**
 * Created by jpentika on 19/08/15.
 */
public class CustomRequest {
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    private String sessionId;

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    private String returnUrl;
}
