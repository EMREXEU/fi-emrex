package fi.csc.emrex.smp;


import fi.csc.emrex.common.util.TestUtil;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;

/**
 * Created by marko.hollanti on 07/10/15.
 */
@SpringApplicationConfiguration
public class SignatureVerifierTest extends TestCase {

    private SignatureVerifier instance;

    @Before
    public void setUp() throws Exception {
        instance = new SignatureVerifier();
    }

    @Test
    public void testVerifySignature() throws Exception {

        final String cert = TestUtil.getFileContent("csc-cert.crt");
        final String dataOk = TestUtil.getFileContent("elmo_vastaus_base64_gzipped.txt");

        assertTrue(instance.verifySignature(cert, dataOk));

    }
}
