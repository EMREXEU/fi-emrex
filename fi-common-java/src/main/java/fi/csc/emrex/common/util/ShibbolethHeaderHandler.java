package fi.csc.emrex.common.util;

import fi.csc.emrex.common.model.Person;

import java.io.UnsupportedEncodingException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Created by jpentika on 28/10/15.
 */
@Slf4j
public class ShibbolethHeaderHandler {

    private HttpServletRequest request;

    public ShibbolethHeaderHandler(HttpServletRequest request) {
        this.request = request;
    }

    public String stringifyHeader() {
        final String requestURI = request.getRequestURI();
        String result = "Header attributes:";
        result += "\n requestURI: " + requestURI;

        final String requestURL = request.getRequestURL().toString();
        result += "\n requestURL: " + requestURL;

        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            result += "\n" + headerName + ": " + request.getHeader(headerName);
        }
        return result;
    }

    public String getFirstName() {
        return toUTF8(request.getHeader("shib-givenName"));
    }

    public String getLastName() {
        return toUTF8(request.getHeader("shib-sn"));
    }

    public String getBirthDate() {
        return toUTF8(request.getHeader("shib-SHIB_schacDateOfBirth"));
    }

    public String getHomeOrganization() {
        return toUTF8(request.getHeader("shib-SHIB_schacHomeOrganization"));
    }

    public String getHomeOrganizationName() {
        return toUTF8(request.getHeader("shib-organization_name"));
    }

    public String getOID() {
        return getLastPartOfHeader("shib-SHIB_funetEduPersonLearnerId", "[.]");
    }

    public Person generatePerson() {
        Person person = new Person();
        person.setFirstName(getFirstName());
        person.setLastName(getLastName());
        if (StringUtils.isNotEmpty(getBirthDate())) {
            if (getBirthDate().length() == 6) {
                person.setBirthDate(getBirthDate(), "ddMMyy");
                if (person.getBirthDate() != null && person.getBirthDate().isAfter(LocalDate.now().minusYears(16))) {
                    person.setBirthDate(person.getBirthDate().minusYears(100));
                }
            } else if (getBirthDate().length() == 8) {
                person.setBirthDate(getBirthDate(), null);
            } else {
                log.warn("Student " + getOID() + " date of birth was of invalid length. Value was \"" + getBirthDate() + "\"");
            }
        } else {
            log.warn("Student " + getOID() + " has no date of birth set");
        }
        person.setHomeOrganization(getHomeOrganization());
        person.setHomeOrganizationName(getHomeOrganizationName());
        person.setOID(getOID());
        person.setHeiOid(getHeiOid());
        return person;
    }

    public String getHeiOid() {
        return getLastPartOfHeader("shib-unique-code", "[:]");
    }

    public String getPersonalID() {
        return getLastPartOfHeader("shib-unique-id", "[:]");
    }

    private String getLastPartOfHeader(String shibHeader, String regexp) {
        String header = request.getHeader(shibHeader);
        if (header == null)
            return null;
        String[] splittedHeader = header.split(regexp);
        if (splittedHeader.length < 1)
            return null;
        else
            return splittedHeader[splittedHeader.length - 1];
    }

    private String toUTF8(String text) {
        try {
            byte[] latin1 = text.getBytes("ISO-8859-1");
            String unsafe = new String(latin1, "UTF-8");
            return Security.stripXSS(unsafe);
        } catch (UnsupportedEncodingException | NullPointerException ex) {
            //log.error(ex.getMessage());
            return null;
        }
    }
}
