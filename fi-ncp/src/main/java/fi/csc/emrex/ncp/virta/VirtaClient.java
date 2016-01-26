package fi.csc.emrex.ncp.virta;

import fi.csc.emrex.ncp.DateConverter;
import fi.csc.tietovaranto.emrex.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;

/**
 * Created by marko.hollanti on 28/09/15.
 */
@Slf4j
@Setter
@Component
public class VirtaClient {

    /**
     *
     */
    @Value("${ncp.virta.secret}")
    private  String AVAIN;
    @Value("${ncp.virta.system}")
    private  String JARJESTELMA  ;
    @Value("${ncp.virta.identifier}")
    private String TUNNUS;
    @Value("${ncp.virta.url}")
    private String virtaUrl;
    
    private ELMOOpiskelijavaihtoService elmoOpiskelijavaihtoService;

    
    public String fetchStudies(String oid, String ssn) {
        return fetchStudies(new VirtaUser(oid, ssn));
    }

    public String fetchStudies(VirtaUser virtaUser) {
        try {
            String marshal = VirtaMarshaller.marshal(sendRequest(virtaUser));
            log.error("fetch Studies marshalled");
            log.error(marshal);
            return marshal;
        } catch (Exception e) {
            log.error("FetchStudies failed. StudentID: {} PersonalID: {}", virtaUser.getOid(), virtaUser.getSsn(), e);
            return null;
        }
    }

    private ELMOOpiskelijavaihtoResponse sendRequest(VirtaUser virtaUser) throws MalformedURLException {
        ELMOOpiskelijavaihtoRequest request = createRequest(virtaUser);
        ELMOOpiskelijavaihtoResponse temp = getService().getELMOOpiskelijavaihtoSoap11().elmoOpiskelijavaihto(request);
        System.out.println(temp.toString());
        System.out.println(temp.getElmo());
        return temp;
    }

    private ELMOOpiskelijavaihtoService getService() throws MalformedURLException {
        if (elmoOpiskelijavaihtoService == null) {
            elmoOpiskelijavaihtoService = new ELMOOpiskelijavaihtoService(new URL(virtaUrl));
        }
        return elmoOpiskelijavaihtoService;
    }

    private ELMOOpiskelijavaihtoRequest createRequest(VirtaUser virtaUser) {
        ELMOOpiskelijavaihtoRequest request = new ELMOOpiskelijavaihtoRequest();
        request.setKutsuja(getKutsuja());
        request.setHakuehdot(getHakuehdot(virtaUser));
        return request;
    }

    private Hakuehdot getHakuehdot(VirtaUser virtaUser) {
        Hakuehdot hakuehdot = new Hakuehdot();

        if (virtaUser.isOidSet()) {
            hakuehdot.getContent().add(0, new ObjectFactory().createOID(virtaUser.getOid()));
        } else {
            hakuehdot.getContent().add(0, new ObjectFactory().createHeTu(virtaUser.getSsn()));
        }

        return hakuehdot;
    }

    private XMLGregorianCalendar convert(LocalDate date) {
        try {
            return DateConverter.convertLocalDateToXmlGregorianCalendar(date);
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private Kutsuja getKutsuja() {
        Kutsuja kutsuja = new Kutsuja();
        kutsuja.setAvain(AVAIN);
        kutsuja.setJarjestelma(JARJESTELMA);
        kutsuja.setTunnus(TUNNUS);
        return kutsuja;
    }

}
