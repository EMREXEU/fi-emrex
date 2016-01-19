package fi.csc.emrex.ncp.virta;

import fi.csc.tietovaranto.emrex.ELMOOpiskelijavaihtoResponse;
import junit.framework.TestCase;
import org.junit.Before;
//import org.purl.net.elmo.ElmoBase.ElmoBase;
//import https.github_com.emrex_eu.elmo_schemas.tree.v1.Elmo;
import org.junit.Ignore;
import org.purl.net.elmo.ElmoBase;

/**
 * Created by marko.hollanti on 13/10/15.
 */
public class VirtaMarshallerTest extends TestCase {

    
    public void testMarshal() throws Exception {

        final String result = VirtaMarshaller.marshal(createResponse());
        assertEquals("<elmo xmlns=\"https://github.com/emrex-eu/elmo-schemas/tree/v1\" xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\"/>", result);
    }

    private ELMOOpiskelijavaihtoResponse createResponse() {
        final ELMOOpiskelijavaihtoResponse response = new ELMOOpiskelijavaihtoResponse();
        response.setElmo(new ElmoBase());
        return response;
    }
}