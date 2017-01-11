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
import fi.csc.emrex.common.util.Security;
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
import org.springframework.beans.factory.annotation.Value;

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

    @Value("${ncp.environment}")
    private String env;

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

        ElmoParser finalParser = ElmoParser.elmoParser(xmlString);

        String statisticalLogLine = generateStatisticalLogLine(parser, finalParser, "FI");
        StatisticalLogger.log(statisticalLogLine);

        xmlString = dataSign.sign(xmlString.trim(), StandardCharsets.UTF_8);
        if (courses != null) {
            model.addAttribute("returnCode", context.getSession().getAttribute("returnCode"));
            model.addAttribute("elmo", xmlString);
        } else {
            model.addAttribute("returnCode", "NCP_NO_RESULTS");
            model.addAttribute("elmo", null);
        }
        model.addAttribute("buttonText", "Confirm selection");

        model.addAttribute("buttonClass", "pure-button custom-go-button custom-inline");

        return "review";
    }

    private String getElmoXml(@RequestParam(value = "courses", required = false) String[] courses, ElmoParser parser) throws ParserConfigurationException {
        String xmlString;
        if (courses != null) {
            List<String> courseList = Arrays.asList(courses);
            xmlString = parser.getCourseData(courseList);
        } else {
            xmlString = parser.getCourseData(null);
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
    public String ncp1(@ModelAttribute CustomRequest customRequest, HttpServletRequest request, Model model) {
        return this.greeting(customRequest, request, model);
    }

    @RequestMapping(value = "/ncp/", method = RequestMethod.POST)
    public String greeting(@ModelAttribute CustomRequest customRequest, HttpServletRequest request, Model model) {
        log.info("/ncp/");
        if (customRequest != null) {
            try {
                if (customRequest.getSessionId() != null) {
                    if (customRequest.getSessionId().equals(Security.stripXSS(customRequest.getSessionId()))) {
                        context.getSession().setAttribute("sessionId", customRequest.getSessionId());
                    } else {
                        throw new Exception("Suspected XSS-injection");
                    }
                }
                if (customRequest.getReturnUrl() != null) {
                    String returnUrl = customRequest.getReturnUrl();
                    log.info("unprocessed returnURL: " + returnUrl);
                    String temp = Security.stripXSS(returnUrl);
                    log.info("processed returnURL: " + temp);
                    if (!returnUrl.equals(temp)) {
                        throw new Exception("Suspected XSS-injection");
                    }
                    if (!returnUrl.startsWith("https")) {
                        log.debug("env: "+env);
                        if (!"dev".equals(env)) {
                            throw new Exception("Only HTTPS allowed");
                        }
                    }
                    context.getSession().setAttribute("returnUrl", returnUrl);

                }
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
                return "error";
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
                        if ("dev".equals(env)) {
                            elmoXML = virtaClient.fetchStudies("17488477125", personalId);
                        } else {
                            elmoXML = "";
                        }
                    } else {
                        elmoXML = virtaClient.fetchStudies(OID, personalId);
                    }
                    log.debug(elmoXML);
                    ElmoParser parser = null;
                    if (elmoXML == null) {
                        log.debug("elmoXML null");
                        context.getSession().setAttribute("returnCode", "NCP_NO_RESULTS");

                    } else {
                        context.getSession().setAttribute("returnCode", "NCP_OK");
                        parser = ElmoParser.elmoParserFromVirta(elmoXML);
                        context.getSession().setAttribute("elmo", parser);

                    }
                    String personalLogLine = generatePersonalLogLine(customRequest, headerHandler, parser);
                    PersonalLogger.log(personalLogLine);

                    String statisticalLogLine = generateStatisticalLogLine(parser, null, "FI");
                    StatisticalLogger.log(statisticalLogLine);
                }
                return "norex";

            } catch (Exception e) {
                log.error("Elmo was null and fetching elmo failed somehow.", e);
                model.addAttribute("error", "Fetching study data failed");
                return "error";
            }

        }
        return "norex";
    }

    private String generatePersonalLogLine(@ModelAttribute CustomRequest customRequest, ShibbolethHeaderHandler headerHandler, ElmoParser parser) {
        String personalLogLine = "NCP\t" + customRequest.getSessionId();
        personalLogLine += "\t" + customRequest.getReturnUrl();
        personalLogLine += "\t" + headerHandler.getFirstName() + " " + headerHandler.getLastName();
        if (parser == null) {
            personalLogLine += "\t" + "not-available";
        } else {
            personalLogLine += "\t" + parser.getHostInstitution();
        }
        return personalLogLine;
    }

    private String generateStatisticalLogLine(ElmoParser initialParser, ElmoParser finalParser, String source) throws Exception {
        String statisticalLogLine = finalParser != null ? "FINISH" : "START";
        statisticalLogLine += "\t" + context.getSession().getAttribute("sessionId");
        statisticalLogLine += "\t" + context.getSession().getAttribute("returnUrl");
        statisticalLogLine += "\t" + source;
        if (initialParser != null) {
            statisticalLogLine += "\t" + initialParser.getHostInstitution();
            statisticalLogLine += "\t" + initialParser.getCourseCount();
            statisticalLogLine += "\t" + initialParser.getETCSCount();
            statisticalLogLine += "\t" + (finalParser != null ? finalParser.getCourseCount() : -1);
            statisticalLogLine += "\t" + (finalParser != null ? finalParser.getETCSCount() : -1);
        } else {
            statisticalLogLine += "\tnone\t0\t0\t0\t0"; // zero courses, zero ects, from finland
        }
        return statisticalLogLine;
    }

}
