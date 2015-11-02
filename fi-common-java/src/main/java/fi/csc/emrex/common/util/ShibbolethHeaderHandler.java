package fi.csc.emrex.common.util;

import lombok.extern.slf4j.Slf4j;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * Created by jpentika on 28/10/15.
 */
@Slf4j
public class ShibbolethHeaderHandler {

    private HttpServletRequest request;

    public ShibbolethHeaderHandler(HttpServletRequest request) {
        this.request = request;
    }

    public void printAttributes() {
        final String requestURI = request.getRequestURI();
        log.debug("Header attributes:");
        log.debug("requestURI: " + requestURI);

        final String requestURL = request.getRequestURL().toString();
        log.debug("requestURL: " + requestURL);

        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            log.debug(headerName + ": " + request.getHeader(headerName));
        }
    }

    public String getOID() {
        return getLastPartOfHeader("shib-unique-code");
    }

    public String getPersonalID() {
        return getLastPartOfHeader("shib-unique-id");
    }

    private String getLastPartOfHeader(String shibHeader) {
        String header = request.getHeader(shibHeader);
        if (header == null )
            return null;
        String[] splittedHeader = header.split("[:]");
        if (splittedHeader.length < 1)
            return null;
        else
            return splittedHeader[splittedHeader.length - 1];
    }

}
