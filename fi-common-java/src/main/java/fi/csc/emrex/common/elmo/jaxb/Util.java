/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.common.elmo.jaxb;

import https.github_com.emrex_eu.elmo_schemas.tree.v1.Elmo;
import https.github_com.emrex_eu.elmo_schemas.tree.v1.LearningOpportunitySpecification;
import fi.csc.emrex.common.elmo.ElmoParser;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.slf4j.LoggerFactory;

/**
 *
 * @author salum
 */
public class Util {

    final static org.slf4j.Logger log = LoggerFactory.getLogger(ElmoParser.class);

    public static List<LearningOpportunitySpecification> getAllLearningOpportunities(LearningOpportunitySpecification los, List<LearningOpportunitySpecification> losList) {
        if (los != null) {
            losList.add(los);
            List<LearningOpportunitySpecification.HasPart> hasParts = los.getHasPart();
            for (LearningOpportunitySpecification.HasPart hasPart : hasParts) {
                getAllLearningOpportunities(hasPart.getLearningOpportunitySpecification(), losList);
            }
            if (hasParts != null) {
                hasParts.clear();
            }
        }
        return losList;
    }

    private static Elmo getElmo(String elmoString) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance("https.github_com.emrex_eu.elmo_schemas.tree.v1");
        Unmarshaller u = jc.createUnmarshaller();
        Elmo elmo = (Elmo) u.unmarshal(new StringReader(elmoString));
        return elmo;
    }

    private static String marshalElmo(Elmo elmo) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance("https.github_com.emrex_eu.elmo_schemas.tree.v1");
        StringWriter out = new StringWriter();
        Marshaller m = jc.createMarshaller();
        m.marshal(elmo, out);
        return out.toString();
    }

    public static String virtaJAXBParser(String elmoString) {
        int elmoIndex = 0;
        try {
            Elmo elmo = getElmo(elmoString);
            List<Elmo.Report> reports = elmo.getReport();
            ArrayList<LearningOpportunitySpecification> elmoLoSList = new ArrayList<LearningOpportunitySpecification>();
            for (Elmo.Report report : reports) {
                ArrayList<LearningOpportunitySpecification> losList = new ArrayList<LearningOpportunitySpecification>();
                List<LearningOpportunitySpecification> tempList = report.getLearningOpportunitySpecification();
                for (LearningOpportunitySpecification los : tempList) {
                    getAllLearningOpportunities(los, losList);
                }
                tempList.clear();
                tempList.addAll(losList);
                elmoLoSList.addAll(losList);
            }
            for (int i = 0; i < elmoLoSList.size(); i++) {
                LearningOpportunitySpecification los = elmoLoSList.get(i);
                List<LearningOpportunitySpecification.Identifier> identifierList = los.getIdentifier();
                LearningOpportunitySpecification.Identifier elmoID = new LearningOpportunitySpecification.Identifier();
                elmoID.setType("elmo");
                elmoID.setValue(String.valueOf(elmoIndex++));
                identifierList.add(elmoID);
            }
            return marshalElmo(elmo);
        } catch (JAXBException ex) {
            Logger.getLogger(ElmoParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static String getCourses(String elmoString, List<String> courses) {
        try {
            Elmo elmo = getElmo(elmoString);
            List<Elmo.Report> reports = elmo.getReport();
            log.debug("reports: " + reports.size());
            for (Elmo.Report report : reports) {
                ArrayList<LearningOpportunitySpecification> losList = new ArrayList<LearningOpportunitySpecification>();
                List<LearningOpportunitySpecification> tempList = report.getLearningOpportunitySpecification();
                for (LearningOpportunitySpecification los : tempList) {
                    getAllLearningOpportunities(los, losList);
                }

                tempList.clear();
                for (LearningOpportunitySpecification spec : losList) {
                    List<LearningOpportunitySpecification.Identifier> identifiers = spec.getIdentifier();
                    for (LearningOpportunitySpecification.Identifier id : identifiers) {
                        if ("elmo".equals(id.getType()) && courses.contains(id.getValue())) {
                            tempList.add(spec);
                        }
                    }
                }

            }
            return marshalElmo(elmo);
        } catch (JAXBException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }
}
