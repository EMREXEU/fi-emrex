/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.common.elmo;

import com.github.ooxi.jdatauri.DataUri;
import java.io.File;
import java.nio.charset.Charset;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

/**
 * A class representing a single Elmo xml.
 *
 * @author salum
 */
public class ElmoParser {

    final static org.slf4j.Logger log = LoggerFactory.getLogger(ElmoParser.class);

    static final String elmoSchema = "src/main/resources/schema-10.xsd";
    static final String euroPassSchema = "src/main/resources/EUROPASS_ISOCountries_V1.1.xsd";
    static final String xmldsigSchema = "src/main/resources/xmldsig-core-schema.xsd";
    static final String[] schemas = {euroPassSchema, xmldsigSchema, elmoSchema};
    static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    private Document document;
    private int gcc;

    protected ElmoParser(String elmo) throws SAXException, MalformedURLException, ParserConfigurationException, IOException {
        this.gcc = 0;
        log.debug("    protected ElmoParser(String elmo)");
        log.debug(elmo);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        /* TODO fix validation
         factory.setValidating(true);
         factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
         factory.setAttribute(JAXP_SCHEMA_SOURCE, schemas);
         */
        DocumentBuilder builder = factory.newDocumentBuilder();
        StringReader sr = new StringReader(elmo);
        InputSource s = new InputSource(sr);

        //Load and Parse the XML document
        //document contains the complete XML as a Tree.
        this.document = builder.parse(s);

    }

    /**
     * Creates a dom model of elmo xml
     *
     * @param elmo
     */
    public static ElmoParser elmoParser(String elmo) throws Exception {
        return new ElmoParser(elmo);
    }

    /**
     * Creates a dom model of elmo xml and adds elmo identifiers to courses and
     * flattens the learning opportunity specification hierarchy
     *
     * @param elmo
     */
    public static ElmoParser elmoParserFromVirta(String elmo) throws Exception {

        ElmoParser parser = new ElmoParser(elmo);
        parser.addElmoIdentifiers();
        parser.flattenLearningOpportunityHierarchy();
        parser.document.normalizeDocument();
        return parser;

    }

    public byte[] getAttachedPDF() throws Exception {
        NodeList elmos = document.getElementsByTagName("elmo");
        if (elmos.getLength() > 0) {

            Element elmo = (Element) elmos.item(0);
            NodeList attachments = elmo.getElementsByTagName("attachment");
            log.debug(attachments.getLength() + " attachments found");
            for (int i = 0; i < attachments.getLength(); i++) {
                //NodeList childs = attachments.item(0).getChildNodes();
                Element attachment = (Element) attachments.item(i);
                if (attachment.getParentNode().equals(elmo)) {
                    NodeList types = attachment.getElementsByTagName("type");
                    Element type = (Element) types.item(0);
                    if (type != null) {
                        if ("EMREX transcript".equals(type.getTextContent())) { // need to check for "Emrex trenscript"

                            NodeList content = attachment.getElementsByTagName("content");

                            for (int j = 0; j < content.getLength(); j++) {

                                //log.debug(content.item(j).getTextContent());
                                DataUri parse = DataUri.parse(content.item(j).getTextContent(), Charset.forName("UTF-8"));
                                if ("application/pdf".equals(parse.getMime())) {
                                    return parse.getData();
                                }
                                //return DatatypeConverter.parseBase64Binary(content.item(0).getTextContent());
                            }
                        }
                    }
                    throw new Exception("no content attachment in elmo in  xml");
                }

            }
            throw new Exception("no attachments in elmo in  xml");
        }
        throw new Exception("No elmo in xml");
    }

    public void addPDFAttachment(byte[] pdf) {
        NodeList elmos = document.getElementsByTagName("elmo");
        if (elmos.getLength() > 0) {

            //remove existing attachments to avoid duplicates
            NodeList removeNodes = document.getElementsByTagName("attachment");
            for (int i = 0; i < removeNodes.getLength(); i++) {
                Node parent = removeNodes.item(i).getParentNode();
                if (parent != null) {
                    parent.removeChild(removeNodes.item(i));
                }
            }

            // Add pdf attachment
            Element attachment = document.createElement("attachment");
            Element type = document.createElement("type");
            type.setTextContent("EMREX transcript");
            attachment.appendChild(type);
            //attachment.setAttribute("contentType", "application/pdf");
            //attachment.setAttribute("encoding", "base64");
            String data = "data:application/pdf;base64," + DatatypeConverter.printBase64Binary(pdf);
            Element content = document.createElement("content");
            content.setTextContent(data);
            attachment.appendChild(content);
            elmos.item(0).appendChild(attachment); // we assume that only one report exists
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
            //log.debug("getCourseDataCount "+ this.gcc++);
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
                            // log.debug("idContent "+idContent);
                            boolean doesntContain;
                            if (courses == null) {
                                doesntContain = true;
                            } else {
                                doesntContain = !courses.contains(idContent);
                            }
                            if (doesntContain) {
                                removeNodes.add(specification);
                                log.trace("removed courseid " + idContent);
                            } else {
                                log.trace("found courseid " + idContent);
                            }

                        }
                    }
                }
            }
            for (Node remove : removeNodes) {
                Node parent = remove.getParentNode();
                if (parent != null) {
                    if ("hasPart".equals(parent.getLocalName())) {
                        Node parentsParent = parent.getParentNode();
                        if (parentsParent != null) {
                            parentsParent.removeChild(parent);
                        }
                    } else {
                        parent.removeChild(remove);
                    }
                }
            }
            List<Node> removeEmptyReports = new ArrayList<>();
            NodeList reports = doc.getElementsByTagName("report");
            //log.debug("reports.getLength() " + reports.getLength());
            for (int i = 0; i < reports.getLength(); i++) {
                Element report = (Element) reports.item(i);
                //log.debug("Report " + i);
                NodeList learnList = report.getElementsByTagName("learningOpportunitySpecification");
                if (learnList.getLength() < 1) {
                    log.error("Empty report " + i);
                    removeEmptyReports.add(report);
                }
            }
            for (Node report : removeEmptyReports) {

                report.getParentNode().removeChild(report);
            }
            return getStringFromDoc(doc);

        } catch (SAXException | IOException ex) {
            Logger.getLogger(ElmoParser.class
                    .getName()).log(Level.SEVERE, null, ex);

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
            Element opportunitySpecification = (Element) learningOpportunities.item(i);
            NodeList types = opportunitySpecification.getElementsByTagName("type");
            for (int j = 0; j < types.getLength(); j++) {
                if (types.item(j).getParentNode() == opportunitySpecification) {
                    type = types.item(j).getTextContent();
                }
            }

            Integer credits = 0;
            //XPathExpression valueExpression = xpath.compile("//specifies/learningOpportunityInstance/credit");
            //Element credit = (Element) valueExpression.evaluate( XPathConstants.NODE);
            List<Element> specifications = this.toElementList(opportunitySpecification.getElementsByTagName("specifies"));
            for (Element spec : specifications) {
                if (opportunitySpecification.equals(spec.getParentNode())) {
                    List<Element> instances = this.toElementList(spec.getElementsByTagName("learningOpportunityInstance"));
                    for (Element instance : instances) {
                        if (spec.equals(instance.getParentNode())) {
                            List<Element> creditElemnets = this.toElementList(spec.getElementsByTagName("credit"));
                            for (Element credit : creditElemnets) {
                                NodeList schemes = credit.getElementsByTagName("scheme");
                                for (int j = 0; j < schemes.getLength(); j++) {
                                    Node scheme = schemes.item(j);
                                    if ("ects".equalsIgnoreCase(scheme.getTextContent())) {
                                        Node item = credit.getElementsByTagName("value").item(0);
                                        if (item != null) {
                                            String valueContent = item.getTextContent();
                                            double doubleValue = Double.parseDouble(valueContent);
                                            //log.debug(type + " double: " + doubleValue);
                                            credits = (int) doubleValue;
                                            //log.debug(type + " int: " + credits);
                                            if (result.containsKey(type)) {
                                                credits += result.get(type);
                                                result.replace(type, credits);
                                            } else {
                                                result.put(type, credits);
                                            }
                                        }

                                    }

                                }
                            }
                        }
                    }
                }

            }
        }
        // lets take biggest number by type so same numbers are not counted several times
        int count = 0;
        for (Map.Entry<String, Integer> entry : result.entrySet()) {
            // log.debug(entry.toString());
            if (entry.getValue() > count) {
                count = entry.getValue().intValue();
            }
        }
        return count;
    }

    public int getCoursesCount() throws Exception {
        int result = 0;
        NodeList list = document.getElementsByTagName("report");
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression learningOpportunityExpression = xpath.compile("//learningOpportunitySpecification");
        NodeList learningOpportunities = (NodeList) learningOpportunityExpression.evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < learningOpportunities.getLength(); i++) {
            String type = "undefined";
            NodeList types = ((Element) learningOpportunities.item(i)).getElementsByTagName("type");
            for (int j = 0; j < types.getLength(); j++) {
                if (types.item(j).getParentNode() == learningOpportunities.item(i)) {
                    type = types.item(j).getTextContent();
                }
            }

            if (type.toLowerCase().equals("module")) {
                result++;
            }
        }
        return result;
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
                    if (type.equals("schac")) {
                        log.info("instution identifier type schac");
                        return hostInstitution;
                    }
                }
            }
        }
        return hostInstitution;
    }

    public String getHostCountry() {

        String hostCountry = "unknown";
        NodeList reports = document.getElementsByTagName("report");
        if (reports.getLength() == 1) {
            NodeList issuers = ((Element) reports.item(0)).getElementsByTagName("issuer");
            if (issuers.getLength() == 1) {
                NodeList titles = ((Element) issuers.item(0)).getElementsByTagName("country");
                for (int i = 0; i < titles.getLength(); i++) {
                    Element title = (Element) titles.item(i);
                    hostCountry = title.getTextContent();
                }
            }
        }

        return hostCountry;
    }

    private void addElmoIdentifiers() {
        NodeList learnings = this.document.getElementsByTagName("learningOpportunitySpecification");
        for (int i = 0; i < learnings.getLength(); i++) {
            Element identifier = this.document.createElement("identifier");
            identifier.setAttribute("type", "elmo");
            identifier.setTextContent(String.valueOf(i));
            Element e = (Element) learnings.item(i);
            //Node parent = e.getParentNode();
            e.appendChild(identifier);
        }
    }

    private void flattenLearningOpportunityHierarchy() {
        //    System.out.println("doc hasPart count: " + this.document.getElementsByTagName("hasPart").getLength()
        //            + " lOS count: " + this.document.getElementsByTagName("learningOpportunitySpecification").getLength());
        NodeList reports = this.document.getElementsByTagName("report");
        for (int k = 0; k < reports.getLength(); k++) {
            Element report = (Element) reports.item(k);
            List<Element> learnings2 = this.toElementList(report.getElementsByTagName("learningOpportunitySpecification"));
            for (int j = 0; j < learnings2.size(); j++) {
                Node course = learnings2.get(j);
                Node parent = course.getParentNode();
                //     Node parentsParent = parent.getParentNode();
                //    parentsParent.removeChild(parent);
                report.appendChild(parent.removeChild(course));
                //  System.out.println(j);
            }
            //System.out.println("report learnings: " + learnings2.getLength());
            List<Element> hasParts = this.toElementList(report.getElementsByTagName("hasPart"));
            for (int m = 0; m < hasParts.size(); m++) {
                Node part = hasParts.get(m);
                Node parent = part.getParentNode();
                parent.removeChild(part).getTextContent();
            }
        }
        //    System.out.println("flattened hasPart count: " + this.document.getElementsByTagName("hasPart").getLength()
        //            + " lOS count: " + this.document.getElementsByTagName("learningOpportunitySpecification").getLength());
    }

    private List<Element> toElementList(NodeList nodeList) {
        List<Element> list = new ArrayList<Element>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            try {
                list.add((Element) nodeList.item(i));
            } catch (ClassCastException cce) {
                log.trace(cce.getMessage());
            }
        }
        return list;
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
