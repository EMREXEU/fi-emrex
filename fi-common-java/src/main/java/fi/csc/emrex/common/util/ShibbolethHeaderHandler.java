package fi.csc.emrex.common.util;

import fi.csc.emrex.common.model.Person;
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

    public String stringifyHeader() {
        final String requestURI = request.getRequestURI();
        String result = new String("Header attributes:");
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

    public String getFirstName(){
        return request.getHeader("shib-cn");
    }

    public String getLastName(){
        return request.getHeader("shib-sn");
    }

    public String getBirthDate(){
        return request.getHeader("shib-schacDateOfBirth");
    }

    public String getHomeOrganization(){
        return request.getHeader("shib-schacHomeOrganization");
    }

    public String getOID(){
        return request.getHeader("");
    }

    public Person generatePerson() {
        Person person = new Person();
        person.setFirstName(getFirstName());
        person.setLastName(getLastName());
        person.setBirthDate(getBirthDate(), "YYYYMMDD");
        person.setHomeOrganization(getHomeOrganization());
        person.setOID(getOID());
        person.setHeiOid(getHeiOid());
        return person;
    }

    public String getHeiOid() {
        return getLastPartOfHeader("shib-unique-code");
    }

    public String getPersonalID() {
        return getLastPartOfHeader("shib-unique-id");
    }

    private String getLastPartOfHeader(String shibHeader) {
        String header = request.getHeader(shibHeader);
        if (header == null)
            return null;
        String[] splittedHeader = header.split("[:]");
        if (splittedHeader.length < 1)
            return null;
        else
            return splittedHeader[splittedHeader.length - 1];
    }

}
