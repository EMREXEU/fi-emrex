package fi.csc.emrex.ncp;

import fi.csc.emrex.ncp.virta.VirtaClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by marko.hollanti on 04/09/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonControllerTest {

    public static final String RETURN_URL = "returnUrl";
    public static final String SESSION_ID = "sessionId";
    public static final String ELMO_XML = "elmoXml";

    @Mock private HttpServletRequest mockHttpServletRequest;
    @Mock private VirtaClient mockVirtaClient;
    @InjectMocks private JsonController instance;

    @Test
    public void testFetchElmoXml() throws Exception {

        final HttpSession mockHttpSession = Mockito.mock(HttpSession.class);
        Mockito.when(mockHttpServletRequest.getSession()).thenReturn(mockHttpSession);

        Mockito.when(mockHttpServletRequest.getHeader("funetEduPersonLearnerId")).thenReturn("17488477125");
        Mockito.when(mockHttpServletRequest.getRequestURI()).thenReturn("RequestUri");
        Mockito.when(mockHttpServletRequest.getRequestURL()).thenReturn(new StringBuffer("RequestUrl"));
        Vector<String> headerNames = new Vector<>();
        headerNames.add("header");
        Mockito.when(mockHttpServletRequest.getHeaderNames()).thenReturn(headerNames.elements());

        final String returnUrl = RETURN_URL;
        Mockito.when(mockHttpSession.getAttribute(RETURN_URL)).thenReturn(returnUrl);

        final String sessionId = SESSION_ID;
        Mockito.when(mockHttpSession.getAttribute(SESSION_ID)).thenReturn(sessionId);

        Mockito.when(mockVirtaClient.fetchStudies(Matchers.anyString(), Matchers.anyString())).thenReturn(ELMO_XML);

        final Map<String, Object> result = instance.fetchElmoXml(mockHttpServletRequest);
        assertEquals(RETURN_URL, result.get(RETURN_URL));
        assertEquals(SESSION_ID, result.get(SESSION_ID));
        assertNotNull(result.get(ELMO_XML));

        Mockito.verify(mockHttpServletRequest, Mockito.times(2)).getSession();
        Mockito.verify(mockHttpSession).getAttribute(RETURN_URL);
        Mockito.verify(mockHttpSession).getAttribute(SESSION_ID);
        Mockito.verify(mockVirtaClient).fetchStudies(Matchers.anyString(), Matchers.anyString());

    }
}
