package fi.csc.emrex.common;

import fi.csc.emrex.common.util.TestUtil;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * Created by marko.hollanti on 07/10/15.
 */
public class PDFGenTests extends TestCase {




    @Test
    public void testFinElmo() throws Exception {
        generatePdf("Example-elmo-complicated.xml", "/tmp/elmo-complicated.pdf");
        generatePdf("Example-elmo-Finland.xml", "/tmp/elmo-finland.pdf");
        generatePdf("Example-elmo-Norway.xml", "/tmp/elmo-norway.pdf");

        final String decodedXml = TestUtil.getFileContent("Example-elmo-Finland.xml");
    }

    private void generatePdf(String filename, String uri) throws Exception {
        final String decodedXml = TestUtil.getFileContent(filename);
        new PdfGen().generatePdf(decodedXml, uri);
    }
}


