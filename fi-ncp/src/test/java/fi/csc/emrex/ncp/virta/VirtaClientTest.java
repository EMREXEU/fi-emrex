package fi.csc.emrex.ncp.virta;

import fi.csc.tietovaranto.emrex.ELMOOpiskelijavaihto;
import fi.csc.tietovaranto.emrex.ELMOOpiskelijavaihtoRequest;
import fi.csc.tietovaranto.emrex.ELMOOpiskelijavaihtoResponse;
import fi.csc.tietovaranto.emrex.ELMOOpiskelijavaihtoService;
import junit.framework.TestCase;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.time.LocalDate;
import org.junit.Ignore;

/**
 * Created by marko.hollanti on 08/10/15.
 */
public class VirtaClientTest extends TestCase {

    private VirtaClient instance;
    private ELMOOpiskelijavaihtoService elmoOpiskelijavaihtoService;

    public void setUp() throws Exception {
        elmoOpiskelijavaihtoService = Mockito.mock(ELMOOpiskelijavaihtoService.class);
        instance = new VirtaClient();
        instance.setElmoOpiskelijavaihtoService(elmoOpiskelijavaihtoService);
    }
    @Ignore
    @Test
    public void testFetchStudies() throws Exception {

        final String expected = "<elmo xsi:nil=\"true\" xmlns=\"http://purl.org/net/elmo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>";

        ELMOOpiskelijavaihto elmoOpiskelijavaihto = Mockito.mock(ELMOOpiskelijavaihto.class);
        Mockito.when(elmoOpiskelijavaihtoService.getELMOOpiskelijavaihtoSoap11()).thenReturn(elmoOpiskelijavaihto);

        ELMOOpiskelijavaihtoResponse elmoOpiskelijavaihtoResponse = new ELMOOpiskelijavaihtoResponse();
        Mockito.when(elmoOpiskelijavaihto.elmoOpiskelijavaihto(Matchers.any(ELMOOpiskelijavaihtoRequest.class))).thenReturn(elmoOpiskelijavaihtoResponse);

        final String result = instance.fetchStudies(createVirtaUser());
        assertNotNull(result);
        //assertEquals(expected, result);
    }

    private VirtaUser createVirtaUser() {
        return new VirtaUser("17488477125", null);
    }

}