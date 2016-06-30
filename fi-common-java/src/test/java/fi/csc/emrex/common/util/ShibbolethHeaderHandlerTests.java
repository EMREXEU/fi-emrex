package fi.csc.emrex.common.util;

import fi.csc.emrex.common.model.Person;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.cglib.core.Local;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;
/**
 * Created by jpentika on 28/10/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShibbolethHeaderHandlerTests {

    @Mock private HttpServletRequest mockHttpServletRequest;

    public static String studentHEIOIDHeaderName = "shib-unique-code";
    public static String studentHEIOIDHeaderContent = ": urn:mace:terena.org:schac:personalUniqueCode:fi:hy.fi:x8734";

    public static String personalIdHeaderName = "shib-unique-id";
    public static String personalIdHeaderContent = "urn:mace:terena.org:schac:personalUniqueID:fi:FIC:020896-358x";

    public static String OIDheaderName = "shib-SHIB_funetEduPersonLearnerId";
    public static String OIDHeaderContent = "1.2.246.562.24.17488477125";

    public static String homeOrganisationHeaderName ="shib-SHIB_schacHomeOrganization";
    public static String homeOrganisationHeaderContent = "oamk.fi";

    public static String birthdayHeaderName = "shib-SHIB_schacDateOfBirth";
    //public static String birthdayHeaderContent = "010280";

    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");

    public ShibbolethHeaderHandler handler;
    public LocalDate mockBirthday;
    public String testPersonBirthday;
    public Person testPerson;

    @Before
    public void createTestAssets() {
        Mockito.when(mockHttpServletRequest.getHeader(studentHEIOIDHeaderName)).thenReturn(studentHEIOIDHeaderContent);
        Mockito.when(mockHttpServletRequest.getHeader(personalIdHeaderName)).thenReturn(personalIdHeaderContent);
        Mockito.when(mockHttpServletRequest.getHeader(OIDheaderName)).thenReturn(OIDHeaderContent);
        mockBirthday = LocalDate.now().minusYears(18);
        testPersonBirthday = mockBirthday.format(formatter);
        Mockito.when(mockHttpServletRequest.getHeader(birthdayHeaderName)).thenReturn(testPersonBirthday);
        Mockito.when(mockHttpServletRequest.getHeader(homeOrganisationHeaderName)).thenReturn(homeOrganisationHeaderContent);

        handler = new ShibbolethHeaderHandler(mockHttpServletRequest);
        testPerson = handler.generatePerson();
    }

    @Test
    public void testHeaderParsing() throws Exception {
        assertEquals("x8734", handler.getHeiOid());
        assertEquals("17488477125", handler.getOID());
        assertEquals("020896-358x", handler.getPersonalID());
        assertEquals(homeOrganisationHeaderContent, handler.getHomeOrganization());
        assertEquals(testPersonBirthday, testPerson.getBirthDate().format(formatter));
    }

    @Test
    public void testTwoDigitYearCompensation() throws Exception {
        assertEquals(mockBirthday.getYear(), testPerson.getBirthDate().getYear());
    }

}


