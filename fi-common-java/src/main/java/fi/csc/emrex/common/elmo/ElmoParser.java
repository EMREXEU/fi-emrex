/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.common.elmo;

import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class representing a single Elmo xml.
 *
 * @author salum
 */
public class ElmoParser {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(ElmoParser.class);

    private Document document;

    /**
     * Creates a dom model of elmo xml and adds elmo identifiers to courses
     *
     * @param elmo
     */
    public ElmoParser(String elmo) {
        //Get the DOM Builder Factory

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //Get the DOM Builder
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            StringReader sr = new StringReader(elmo);
            InputSource s = new InputSource(sr);

            //Load and Parse the XML document
            //document contains the complete XML as a Tree.
            document = builder.parse(s);

            NodeList learnings = document.getElementsByTagName("learningOpportunitySpecification");
            for (int i = 0; i < learnings.getLength(); i++) {
                Element identifier = document.createElement("identifier");
                identifier.setAttribute("type", "elmo");
                identifier.setTextContent(String.valueOf(i));
                Element e = (Element) learnings.item(i);
                e.appendChild(identifier);
            }
            document.normalizeDocument();

        } catch (Exception ex) {
            Logger.getLogger(ElmoParser.class.getName()).log(Level.SEVERE, null, ex);
            log.error("Parsing of elmo failed", ex);

        }

    }

    public byte[] getAttachedPDF() throws Exception {
        NodeList attachments = document.getElementsByTagName("attachment");
        if (attachments.getLength() == 1) {
            NodeList childs = attachments.item(0).getChildNodes();
            if (childs.getLength() == 1) {
                return DatatypeConverter.parseBase64Binary(childs.item(0).getTextContent());
            }
        }
        throw new Exception("PDF not attached to xml");
    }


    public void addPDFAttachment(byte[] pdf) {
        NodeList reports = document.getElementsByTagName("report");
        if (reports.getLength() > 0) {
            Element attachment = document.createElement("attachment");
            attachment.setAttribute("title", "Transcription of studies");
            attachment.setAttribute("contentType", "application/pdf");
            attachment.setAttribute("encoding", "base64");
            attachment.setTextContent(DatatypeConverter.printBase64Binary(pdf));
            reports.item(0).appendChild(attachment);
        }
    }

    /**
     * Complete XML of found Elmo
     *
     * @return String representation of Elmo-xml
     * @throws ParserConfigurationException
     */
    public String getCourseData() throws ParserConfigurationException {
        return getStringFromDoc(document);
    }

    /**
     * Elmo with a learning instance selection removes all learning
     * opportunities not selected even if a learning opprtunity has a child that
     * is among the selected courses.
     *
     * @param courses
     * @return String representation of Elmo-xml with selected courses
     * @throws ParserConfigurationException
     */
    public String getCourseData(List<String> courses) throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        String copyElmo = this.getStringFromDoc(document);
        try {

            StringReader sr = new StringReader(copyElmo);
            InputSource s = new InputSource(sr);
            Document doc = docBuilder.parse(s);

            NodeList learnings = doc.getElementsByTagName("learningOpportunitySpecification");
            List<Node> removeNodes = new ArrayList<>();
            for (int i = 0; i < learnings.getLength(); i++) {
                Element specification = (Element) learnings.item(i);
                NodeList identifiers = specification.getElementsByTagName("identifier");
                for (int j = 0; j < identifiers.getLength(); j++) {
                    Element id = (Element) identifiers.item(j);
                    if (id.getParentNode() == specification) {
                        if (id.hasAttribute("type") && id.getAttribute("type").equals("elmo")) {
                            String idContent = id.getTextContent();
                            boolean doesntContain = !courses.contains(idContent);

                            if (doesntContain) {
                                removeNodes.add(specification);
                            }

                        }
                    }
                }
            }
            for (Node remove : removeNodes) {
                Node parent = remove.getParentNode();
                if (parent != null) {
                    Node parentsParent = parent.getParentNode();
                    if (parentsParent != null)
                        parentsParent.removeChild(parent);
                }
            }

            NodeList reports = doc.getElementsByTagName("report");
            for (int i = 0; i < reports.getLength(); i++) {
                Element report = (Element) reports.item(i);
                log.debug("Report " + i);
                NodeList learnList = report.getElementsByTagName("learningOpportunitySpecification");
                if (learnList.getLength() < 1) {
                    log.error("Empty report");
                    report.getParentNode().removeChild(report);
                }
            }

            return getStringFromDoc(doc);

        } catch (SAXException | IOException ex) {
            Logger.getLogger(ElmoParser.class.getName()).log(Level.SEVERE, null, ex);
            return null;

        }
    }

    public int getETCSCount() throws Exception {
        HashMap<String, Integer> result = new HashMap();
        NodeList list = document.getElementsByTagName("report");
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression learningOpportunityExpression = xpath.compile("//learningOpportunitySpecification");
        NodeList learningOpportunities = (NodeList) learningOpportunityExpression.evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < learningOpportunities.getLength(); i++) {
            String type = "undefined";
            NodeList types = ((Element) learningOpportunities.item(i)).getElementsByTagName("type");
            for (int j = 0; j < types.getLength(); j++) {
                if (types.item(j).getParentNode() == learningOpportunities.item(i))
                    type = types.item(j).getTextContent();
            }

            Integer credits = 0;
                  XPathExpression valueExpression = xpath.compile("credit/value");
            String valueContent = ((Node)valueExpression.evaluate(learningOpportunities.item(i), XPathConstants.NODE)).getTextContent();
            credits = Integer.parseInt(valueContent);

            if (result.containsKey(type)) {
                credits += result.get(type);
                result.replace(type, credits);
            }
            else
                result.put(type, credits);
        }

        // lets take biggest number by type so same numbers are not counted several times
        int count = 0;
        for(Map.Entry<String, Integer> entry : result.entrySet())
        {
            if (entry.getValue() > count)
                count = entry.getValue().intValue();
        }
        return count;
    }

    public String getHostInstitution() {

        String hostInstitution = "unknown";
        NodeList reports = document.getElementsByTagName("report");
        if (reports.getLength() == 1) {
            NodeList issuers = ((Element) reports.item(0)).getElementsByTagName("issuer");
            if (issuers.getLength() == 1) {
                NodeList titles = ((Element) issuers.item(0)).getElementsByTagName("identifier");
                for (int i = 0; i < titles.getLength(); i++) {
                    Element title = (Element) titles.item(i);
                    String type = title.getAttribute("type").toLowerCase();
                    hostInstitution = titles.item(i).getTextContent();
                    if (type == "erasmus")
                        return hostInstitution;
                }
            }
        }
        return hostInstitution;
    }


    private String getStringFromDoc(org.w3c.dom.Document doc) {
        DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
        LSSerializer lsSerializer = domImplementation.createLSSerializer();

        LSOutput lsOutput = domImplementation.createLSOutput();
        lsOutput.setEncoding(StandardCharsets.UTF_8.name());
        Writer stringWriter = new StringWriter();
        lsOutput.setCharacterStream(stringWriter);
        lsSerializer.write(doc, lsOutput);
        return stringWriter.toString();
    }

}
