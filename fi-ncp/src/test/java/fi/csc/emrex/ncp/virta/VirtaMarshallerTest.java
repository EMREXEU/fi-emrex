package fi.csc.emrex.ncp.virta;

import fi.csc.tietovaranto.emrex.ELMOOpiskelijavaihtoResponse;
import junit.framework.TestCase;
import org.junit.Before;
import org.purl.net.elmo.ElmoBase;

/**
 * Created by marko.hollanti on 13/10/15.
 */
public class VirtaMarshallerTest extends TestCase {

    public void testMarshal() throws Exception {

        final String result = VirtaMarshaller.marshal(createResponse());
        assertEquals("<elmo xmlns=\"http://purl.org/net/elmo\"/>", result);
    }

    private ELMOOpiskelijavaihtoResponse createResponse() {
        final ELMOOpiskelijavaihtoResponse response = new ELMOOpiskelijavaihtoResponse();
        response.setElmo(new ElmoBase());
        return response;
    }
}