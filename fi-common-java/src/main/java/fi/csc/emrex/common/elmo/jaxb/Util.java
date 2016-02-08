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
               log.debug("deleting parts: " + hasParts.size());
                hasParts.clear();
            }
        }
        return losList;
    }

    public static String virtaJAXBParser(String elmoString) {
        int elmoIndex = 0;
        try {
            JAXBContext jc = JAXBContext.newInstance("https.github_com.emrex_eu.elmo_schemas.tree.v1");
            Unmarshaller u = jc.createUnmarshaller();
            Elmo elmo = (Elmo) u.unmarshal(new StringReader(elmoString));
            List<Elmo.Report> reports = elmo.getReport();
            ArrayList<LearningOpportunitySpecification> elmoLoSList = new ArrayList<LearningOpportunitySpecification>();
            log.debug("reports: " + reports.size());
            for (Elmo.Report report : reports) {
                ArrayList<LearningOpportunitySpecification> losList = new ArrayList<LearningOpportunitySpecification>();
                List<LearningOpportunitySpecification> tempList = report.getLearningOpportunitySpecification();
                for (LearningOpportunitySpecification los : tempList) {
                    getAllLearningOpportunities(los, losList);
                }
               log.debug("templist size: " + tempList.size() + "; losList size: " + losList.size());
                tempList.clear();
                log.debug("templist cleared: " + tempList.size());
                tempList.addAll(losList);
                log.debug("templist fixed: " + tempList.size());
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
            log.debug("courses count: " + elmoLoSList.size());
            StringWriter out = new StringWriter();
            Marshaller m = jc.createMarshaller();
            //m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", new  ElmoNamespaceMapper());
            m.marshal(elmo, out);
            String toString = out.toString();
            log.debug(toString);
            return toString;
        } catch (JAXBException ex) {
            Logger.getLogger(ElmoParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
