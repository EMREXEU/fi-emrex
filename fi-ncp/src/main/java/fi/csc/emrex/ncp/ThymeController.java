/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.ncp;

import fi.csc.emrex.common.PdfGen;
import fi.csc.emrex.common.PersonalLogger;
import fi.csc.emrex.common.StatisticalLogger;
import fi.csc.emrex.common.elmo.ElmoParser;
import fi.csc.emrex.common.util.ShibbolethHeaderHandler;
import fi.csc.emrex.ncp.virta.VirtaClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * @author salum
 */
@EnableAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration.class
})
@Controller
@Slf4j
public class ThymeController {

    @Autowired
    private HttpServletRequest context;

    @Autowired
    private VirtaClient virtaClient;

    @Autowired
    private DataSign dataSign;

    // function for local testing
    @RequestMapping(value = "/ncp/review", method = RequestMethod.GET)
    public String ncpReview(@RequestParam(value = "courses", required = false) String[] courses,
                            Model model) throws Exception {
        return this.review(courses, model);
    }

    @RequestMapping(value = "/review", method = RequestMethod.GET)
    public String review(@RequestParam(value = "courses", required = false) String[] courses,
                         Model model) throws Exception {

        model.addAttribute("sessionId", context.getSession().getAttribute("sessionId"));
        model.addAttribute("returnUrl", context.getSession().getAttribute("returnUrl"));
        ElmoParser parser = (ElmoParser) context.getSession().getAttribute("elmo");

        String xmlString;

        // Generate pdf with existing courses and add pdf to xml
        xmlString = getElmoXml(courses, parser);

        PdfGen pdfGenerator = new PdfGen();

        byte[] pdf = pdfGenerator.generatePdf(xmlString);

        parser.addPDFAttachment(pdf);

        xmlString = getElmoXml(courses, parser);

        xmlString = dataSign.sign(xmlString.trim(), StandardCharsets.UTF_8);

        model.addAttribute("elmo", xmlString);
        model.addAttribute("buttonText", "Confirm selection");
        model.addAttribute("returnCode", context.getSession().getAttribute("returnCode"));
        model.addAttribute("buttonClass", "pure-button custom-go-button custom-inline");

        ElmoParser finalParser = new ElmoParser(xmlString);

        String statisticalLogLine = (String)context.getSession().getAttribute("sessionId");
        statisticalLogLine += "\t" + context.getSession().getAttribute("sessionId");
        statisticalLogLine += "\t" + finalParser.getCoursesCount();
        statisticalLogLine += "\t" + finalParser.getETCSCount();
        StatisticalLogger.log(statisticalLogLine);

        return "review";
    }

    private String getElmoXml(@RequestParam(value = "courses", required = false) String[] courses, ElmoParser parser) throws ParserConfigurationException {
        String xmlString;
        if (courses != null && courses.length > 0) {
            List<String> courseList = Arrays.asList(courses);
            xmlString = parser.getCourseData(courseList);
        } else {
            xmlString = parser.getCourseData();
        }
        return xmlString;
    }

    @RequestMapping(value = "/ncp/abort", method = RequestMethod.GET)
    public String smpabort(Model model) {
        return abort(model);
    }

    @RequestMapping(value = "/abort", method = RequestMethod.GET)
    public String abort(Model model) {
        // same submit button with ame url and color is used, but without Elmo
        model.addAttribute("sessionId", context.getSession().getAttribute("sessionId"));
        model.addAttribute("returnUrl", context.getSession().getAttribute("returnUrl"));
        model.addAttribute("buttonText", "Cancel");
        model.addAttribute("returnCode", "NCP_CANCEL");
        model.addAttribute("buttonClass", "pure-button custom-panic-button custom-inline");
        return "review";
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String ncp1(@ModelAttribute CustomRequest customRequest, HttpServletRequest request) {
        return this.greeting(customRequest, request);
    }

    @RequestMapping(value = "/ncp/", method = RequestMethod.POST)
    public String greeting(@ModelAttribute CustomRequest customRequest, HttpServletRequest request) {
        log.info("/ncp/");
        if (context.getSession().getAttribute("sessionId") == null) {
            context.getSession().setAttribute("sessionId", customRequest.getSessionId());
        }
        if (context.getSession().getAttribute("returnUrl") == null) {
            context.getSession().setAttribute("returnUrl", customRequest.getReturnUrl());
        }
        log.info("Return URL: {}", context.getSession().getAttribute("returnUrl"));
        log.info("Session ID: {}", context.getSession().getAttribute("sessionId"));

        try {
            if (context.getSession().getAttribute("elmo") == null) {
                String elmoXML;
                ShibbolethHeaderHandler headerHandler = new ShibbolethHeaderHandler(request);
                log.debug(headerHandler.stringifyHeader());
                String OID = headerHandler.getOID();
                String personalId = headerHandler.getPersonalID();

                if (OID == null && personalId == null) {
                    //TODO delete 
                    elmoXML = FiNcpApplication.getElmo();
                } else {
                    elmoXML = virtaClient.fetchStudies(OID, personalId);
                }

                String personalLogLine = customRequest.getSessionId();
                personalLogLine += "\t" + customRequest.getReturnUrl();
                personalLogLine += "\t" + headerHandler.getFirstName() + " " + headerHandler.getLastName();

                String statisticalLogLine = customRequest.getSessionId();
                statisticalLogLine += "\t" + customRequest.getReturnUrl();

                if (elmoXML == null) {
                    context.getSession().setAttribute("returnCode", "NCP_NO_RESULTS");
                    personalLogLine += "\t" + "not-available";
                    statisticalLogLine += "\t0\t0\tfi"; // zero courses, zero ects, from finland
                } else {
                    context.getSession().setAttribute("returnCode", "NCP_OK");
                    ElmoParser parser = new ElmoParser(elmoXML);
                    context.getSession().setAttribute("elmo", parser);

                    personalLogLine += "\t" + parser.getHostInstitution();
                    statisticalLogLine += "\t" + parser.getCoursesCount();
                    statisticalLogLine += "\t" + parser.getETCSCount();
                }

                StatisticalLogger.log(statisticalLogLine);
                PersonalLogger.log(personalLogLine);

            }
            return "norex";

        } catch (Exception e) {
            log.error("Elmo was null and fetching elmo failed somehow.", e);
        }
        return "norex";
    }

}
