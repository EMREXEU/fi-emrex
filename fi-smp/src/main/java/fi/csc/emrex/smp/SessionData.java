package fi.csc.emrex.smp;

/**
 * Created by jpentika on 08/10/15.
 */
public class SessionData {

    private String elmoSessionId;
    private String sessionId;
    private String returnUrl;
    private String url;
    private String ncpPublicKey;

    public SessionData(){};

    public String getNcpPublicKey() {
        return ncpPublicKey;
    }

    public void setNcpPublicKey(String ncpPublicKey) {
        this.ncpPublicKey = ncpPublicKey;
    }

    public String getElmoSessionId() {
        return elmoSessionId;
    }

    public void setElmoSessionId(String elmoSessionId) {
        this.elmoSessionId = elmoSessionId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }


    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

}
