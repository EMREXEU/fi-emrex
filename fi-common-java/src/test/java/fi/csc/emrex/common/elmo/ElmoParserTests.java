package fi.csc.emrex.common.elmo;

import fi.csc.emrex.common.util.TestUtil;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;


/**
 * Created by jpentika on 19/10/15.
 */
public class ElmoParserTests extends TestCase {

    @Test
    public void testRemoveCourses() throws Exception {
        String elmo = TestUtil.getFileContent("Example-elmo-Finland.xml");
        ElmoParser parser = new ElmoParser(elmo);
        List<String> courses = new ArrayList<String>();
        courses.add("0");
        courses.add("1");
        courses.add("2");
        String readyElmo = parser.getCourseData(courses);
        checkEmptyHasPartNodes(readyElmo);
    }

    @Test
    public void testGetHostInstitution() throws Exception {
        String elmo = TestUtil.getFileContent("Example-elmo-Finland.xml");
        ElmoParser parser = new ElmoParser(elmo);
        String host = parser.getHostInstitution();
        assertEquals("SF TAMPERE01", host);
    }

    @Test
    public void testCountECTS() throws Exception {
        runETCSTest("Example-elmo-Finland.xml", 72);
        runETCSTest("Example-elmo-Norway.xml", 512); // some crazy learner here
    }

    private void runETCSTest(String elmoName, int value) throws Exception {
        String elmo = TestUtil.getFileContent(elmoName);
        ElmoParser parser = new ElmoParser(elmo);
        int count = parser.getETCSCount();
        assertEquals(value, count);
    }

    @Test
    public void testAddAndReadPDF() throws Exception {
        String elmo = TestUtil.getFileContent("Example-elmo-Finland.xml");
        File pdfFile = TestUtil.getFile("elmo-finland.pdf");
        byte[] pdf = IOUtils.toByteArray(new FileInputStream(pdfFile));
        ElmoParser parser = new ElmoParser(elmo);
        parser.addPDFAttachment(pdf);
        byte[] readPdf = parser.getAttachedPDF();
        assertArrayEquals(pdf, readPdf);
    }

    private void checkEmptyHasPartNodes(String readyElmo) throws ParserConfigurationException, SAXException, IOException {
        Document document = getDocument(readyElmo);
        NodeList hasParts = document.getElementsByTagName("hasPart");
        for (int i = 0; i < hasParts.getLength(); i++) {
            Element hasPart = (Element) hasParts.item(i);
            String textContent = hasPart.getTextContent();
            String withoutWhiteSpaces = textContent.trim();
            assertEquals(false, withoutWhiteSpaces.isEmpty());
            assertEquals(true, hasParts.item(i).hasChildNodes());
        }
    }

    private Document getDocument(String xml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //Get the DOM Builder
        DocumentBuilder builder;

        builder = factory.newDocumentBuilder();
        StringReader sr = new StringReader(xml);
        InputSource s = new InputSource(sr);

        return builder.parse(s);
    }




}






