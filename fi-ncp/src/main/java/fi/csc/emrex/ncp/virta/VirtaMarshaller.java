package fi.csc.emrex.ncp.virta;

import fi.csc.tietovaranto.emrex.ELMOOpiskelijavaihtoResponse;
import org.purl.net.elmo.ElmoBase;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.StringWriter;

/**
 * Created by marko.hollanti on 13/10/15.
 */
public class VirtaMarshaller {

    public static final String NAMESPACE_URI = "http://purl.org/net/elmo";
    public static final String LOCAL_PART = "elmo";

    public static String marshal(ELMOOpiskelijavaihtoResponse response) throws JAXBException {
        final Marshaller m = JAXBContext.newInstance(ElmoBase.class).createMarshaller();
        m.setProperty(Marshaller.JAXB_FRAGMENT, true);
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        final StringWriter sw = new StringWriter();
        m.marshal(new JAXBElement<>(new QName(NAMESPACE_URI, LOCAL_PART), ElmoBase.class, response.getElmo()), sw);
        return sw.toString();
    }
}
