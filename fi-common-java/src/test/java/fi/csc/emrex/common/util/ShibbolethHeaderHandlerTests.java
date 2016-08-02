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

    @Mock private HttpServletRequest mockHttpServletRequestStandard;
    @Mock private HttpServletRequest mockHttpServletRequestTwoDigit;

    public static String studentHEIOIDHeaderName = "shib-unique-code";
    public static String studentHEIOIDHeaderContent = ": urn:mace:terena.org:schac:personalUniqueCode:fi:hy.fi:x8734";

    public static String personalIdHeaderName = "shib-unique-id";
    public static String personalIdHeaderContent = "urn:mace:terena.org:schac:personalUniqueID:fi:FIC:020896-358x";

    public static String OIDheaderName = "shib-SHIB_funetEduPersonLearnerId";
    public static String OIDHeaderContent = "1.2.246.562.24.17488477125";

    public static String homeOrganisationHeaderName ="shib-SHIB_schacHomeOrganization";
    public static String homeOrganisationHeaderContent = "oamk.fi";

    public static String birthdayHeaderName = "shib-SHIB_schacDateOfBirth";

    public static DateTimeFormatter standardFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static DateTimeFormatter twoDigitFormatter = DateTimeFormatter.ofPattern("ddMMyy");

    public ShibbolethHeaderHandler standardHandler;
    public ShibbolethHeaderHandler twoDigitHandler;
    public LocalDate mockBirthday;
    public String testPersonStandardBirthday;
    public String testPersonTwoDigitBirthday;
    public Person testPersonStandard;
    public Person testPersonTwoDigit;

    @Before
    public void createTestAssets() {
        Mockito.when(mockHttpServletRequestStandard.getHeader(studentHEIOIDHeaderName)).thenReturn(studentHEIOIDHeaderContent);
        Mockito.when(mockHttpServletRequestTwoDigit.getHeader(studentHEIOIDHeaderName)).thenReturn(studentHEIOIDHeaderContent);
        Mockito.when(mockHttpServletRequestStandard.getHeader(personalIdHeaderName)).thenReturn(personalIdHeaderContent);
        Mockito.when(mockHttpServletRequestTwoDigit.getHeader(personalIdHeaderName)).thenReturn(personalIdHeaderContent);
        Mockito.when(mockHttpServletRequestStandard.getHeader(OIDheaderName)).thenReturn(OIDHeaderContent);
        Mockito.when(mockHttpServletRequestTwoDigit.getHeader(OIDheaderName)).thenReturn(OIDHeaderContent);
        Mockito.when(mockHttpServletRequestStandard.getHeader(homeOrganisationHeaderName)).thenReturn(homeOrganisationHeaderContent);
        Mockito.when(mockHttpServletRequestTwoDigit.getHeader(homeOrganisationHeaderName)).thenReturn(homeOrganisationHeaderContent);

        mockBirthday = LocalDate.now().minusYears(18);

        testPersonStandardBirthday = mockBirthday.format(standardFormatter);
        Mockito.when(mockHttpServletRequestStandard.getHeader(birthdayHeaderName)).thenReturn(testPersonStandardBirthday);
        testPersonTwoDigitBirthday = mockBirthday.format(twoDigitFormatter);
        Mockito.when(mockHttpServletRequestTwoDigit.getHeader(birthdayHeaderName)).thenReturn(testPersonTwoDigitBirthday);

        standardHandler = new ShibbolethHeaderHandler(mockHttpServletRequestStandard);
        testPersonStandard = standardHandler.generatePerson();

        twoDigitHandler = new ShibbolethHeaderHandler(mockHttpServletRequestTwoDigit);
        testPersonTwoDigit = twoDigitHandler.generatePerson();
    }

    @Test
    public void testHeaderParsing() throws Exception {
        assertEquals("x8734", standardHandler.getHeiOid());
        assertEquals("17488477125", standardHandler.getOID());
        assertEquals("020896-358x", standardHandler.getPersonalID());
        assertEquals(homeOrganisationHeaderContent, standardHandler.getHomeOrganization());
        assertEquals(testPersonStandardBirthday, testPersonStandard.getBirthDate().format(standardFormatter));
        assertEquals(testPersonTwoDigitBirthday, testPersonTwoDigit.getBirthDate().format(twoDigitFormatter));
    }

    @Test
    public void testTwoDigitYearCompensation() throws Exception {
        assertEquals(mockBirthday.getYear(), testPersonTwoDigit.getBirthDate().getYear());
    }

}


