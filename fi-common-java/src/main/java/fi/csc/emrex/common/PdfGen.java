package fi.csc.emrex.common;

import com.itextpdf.text.*;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by marko.hollanti on 20/08/15.
 */
public class PdfGen {

    private static final long serialVersionUID = 1L;

    private static final float MARGIN_LEFT = 28;
    private static final float MARGIN_RIGHT = 28;
    private static final float MARGIN_TOP = 100;
    private static final float MARGIN_BOTTOM = 28;

    private static final Font FONT_NORMAL = new Font(FontFamily.HELVETICA, 8);
    private static final Font FONT_BOLD = new Font(FontFamily.HELVETICA, 8, Font.BOLD);
    private static final Font FONT_HEADING = new Font(FontFamily.HELVETICA, 18, Font.BOLD);

    public void generatePdf(String xml, String uri) throws Exception {
        ByteArrayOutputStream bos = generatePDFAsByteArray(xml);
        writePdf(bos, new URI(uri));
    }

    public byte[] generatePdf(String xml) throws Exception {
        return generatePDFAsByteArray(xml).toByteArray();
    }

    private ByteArrayOutputStream generatePDFAsByteArray(String xml) throws Exception {
        List<ElmoDocument> edList = getElmoDocuments(xml);
        return createPdf(edList);
    }

    private List<ElmoDocument> getElmoDocuments(String elmoXml) throws Exception {
        List<ElmoDocument> docs = new ArrayList<ElmoDocument>();
        Document doc = createDocument(elmoXml);
        NodeList list = doc.getElementsByTagName("report");
        for (int i = 0; i < list.getLength(); i++) {
            Node report = list.item(i);
            ElmoDocument ed = new ElmoXmlImportHelper().getDocument(report);
            docs.add(ed);
        }
        return docs;
    }


    private Document createDocument(String xml) {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(false);
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (Exception e) {
            throw new IllegalArgumentException("Klarte ikke å lage xml dokument", e);
        }

        Document doc = null;

        try {
            doc = docBuilder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
        } catch (Exception e) {
            throw new IllegalArgumentException("Klarte ikke parse XML-dokument.", e);
        }
        return doc;
    }


    private ByteArrayOutputStream createPdf(List<ElmoDocument> documents) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        com.itextpdf.text.Document document =
                new com.itextpdf.text.Document(PageSize.A4,
                        MARGIN_LEFT, MARGIN_RIGHT, MARGIN_TOP,
                        MARGIN_BOTTOM);
        PdfWriter.getInstance(document, bos);
        document.open();
        for (ElmoDocument doc : documents) {
            createPage(document, doc);
            document.newPage();
        }

        document.close();
        return bos;
    }


    private void createPage(com.itextpdf.text.Document document, ElmoDocument doc) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Phrase("Transcript for " + doc.getPersonName() + " (" + doc.getBirthday() + ")", FONT_HEADING));
        document.add(p);

        p = new Paragraph();
        p.add(new Phrase("Institution: " + doc.getInstitutionName(), FONT_BOLD));
        document.add(p);
        p = new Paragraph();
        p.add(new Phrase(" "));
        document.add(p);
        PdfPTable table = new PdfPTable(new float[]{15, 30, 15, 10, 10, 8, 8});
        table.setWidthPercentage(100);

        table.addCell(createHeaderCell("Code"));
        table.addCell(createHeaderCell("Title"));
        table.addCell(createHeaderCell("Level"));
        table.addCell(createHeaderCell("Type"));
        table.addCell(createHeaderCell("Credits"));
        table.addCell(createHeaderCell("Result"));
        table.addCell(createHeaderCell("Date"));

        for (ElmoResult res : doc.getResults()) {
            table.addCell(createCell(res.getCode()));
            table.addCell(createCell(res.getName()));
            table.addCell(createCell(res.getLevel()));
            table.addCell(createCell(res.getType()));
            table.addCell(createCell(res.getCredits()));
            table.addCell(createCell(res.getResult()));
            table.addCell(createCell(res.getDate()));

        }

        document.add(table);
    }


    private PdfPCell createHeaderCell(String text) {
        PdfPCell cell = null;
        cell = new PdfPCell(new Phrase(text, FONT_BOLD));
        cell.setVerticalAlignment(Rectangle.ALIGN_BOTTOM);
        cell.setBorder(Rectangle.BOTTOM);
        return cell;
    }


    private PdfPCell createCell(String text) {
        PdfPCell cell = null;
        cell = new PdfPCell(new Phrase(text, FONT_NORMAL));
        cell.setVerticalAlignment(Rectangle.ALIGN_BOTTOM);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }


    private void writePdf(ByteArrayOutputStream str, URI uri) throws Exception {
        FileUtils.writeByteArrayToFile(new File(uri.getPath()), str.toByteArray());
    }


}
