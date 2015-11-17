package fi.csc.emrex.ncp;

import fi.csc.emrex.ncp.util.TestUtil;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;

/**
 * Created by marko.hollanti on 06/10/15.
 */
@SpringApplicationConfiguration
public class DataSignTest extends TestCase {

    private DataSign instance;

    @Before
    public void setUp() throws Exception {
        instance = new DataSign();
        instance.setCertificatePath("csc-cert.crt");
        instance.setEncryptionKeyPath("csc-privkey-pkcs8.key");
        instance.setEnvironment("dev");
    }

    @Test
    public void testSign() throws Exception {

        final String data = TestUtil.getFileContent("Example-elmo-Finland.xml");

        final String result = instance.sign(data, StandardCharsets.UTF_8);

        final byte[] decoded = DatatypeConverter.parseBase64Binary(result);
        assertNotNull(decoded);

        final byte[] decompressed = GzipUtil.gzipDecompressBytes(decoded);
        assertNotNull(decompressed);

        final String s = new String(decompressed);

        assertTrue(s.contains("<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/>"));
        assertTrue(s.endsWith("</X509Certificate></X509Data></KeyInfo></Signature></elmo>"));
    }
}