package fi.csc.emrex.common.util;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import javax.servlet.http.HttpServletRequest;
import static org.junit.Assert.assertEquals;
/**
 * Created by jpentika on 28/10/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShibbolethHeaderHandlerTests {

    @Mock private HttpServletRequest mockHttpServletRequest;

    public static String studentIdHeaderName = "shib-unique-code";
    public static String studentIdHeaderContent = ": urn:mace:terena.org:schac:personalUniqueCode:fi:hy.fi:x8734";
    public static String personalIdHeaderName = "shib-unique-id";
    public static String personalIdHeaderContent = "urn:mace:terena.org:schac:personalUniqueID:fi:FIC:020896-358x";


    @Test
    public void testHeaderParsing() throws Exception {
        Mockito.when(mockHttpServletRequest.getHeader(studentIdHeaderName)).thenReturn(studentIdHeaderContent);
        Mockito.when(mockHttpServletRequest.getHeader(personalIdHeaderName)).thenReturn(personalIdHeaderContent);
        ShibbolethHeaderHandler handler = new ShibbolethHeaderHandler(mockHttpServletRequest);
        assertEquals("x8734", handler.getHeiOid());
        assertEquals("020896-358x", handler.getPersonalID());

    }


}


