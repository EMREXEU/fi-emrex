package fi.csc.emrex.ncp;

import junit.framework.TestCase;

/**
 * Created by marko.hollanti on 04/09/15.
 */
public class FiNcpApplicationTest extends TestCase {

    public void testGetElmo() throws Exception {

        final String elmoXml = FiNcpApplication.getElmo();
        assertNotNull(elmoXml);
        assertTrue(elmoXml.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
    }
}