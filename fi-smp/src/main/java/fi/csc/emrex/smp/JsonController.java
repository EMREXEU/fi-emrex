/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.smp;

import fi.csc.emrex.common.model.Person;
import fi.csc.emrex.smp.model.Link;
import fi.csc.emrex.smp.model.VerifiedReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author salum
 */
@Slf4j
@Controller
public class JsonController {

    @Value("${emreg.url}")
    private String emregUrl;

    @Value("${smp.return.url}")
    private String returnUrl;

    @Value("${smp.university.directory.map}")
    String dirMap;

    @Value("${smp.university.base.directory}")
    String pdfBaseDir;

    @Value("${smp.email.body.file}")
    private String emailBodyFile;

    @Autowired
    private HttpServletRequest context;

    @RequestMapping("/smp/api/smp")
    @ResponseBody
    public List<NCPResult> smpncps() throws Exception {
        return this.ncps();
    }

    @RequestMapping(value = "/smp/api/sessiondata", method = RequestMethod.POST)
    @ResponseBody
    public SessionData smpSessionData(@RequestBody NCPChoice choice, HttpServletRequest request) throws Exception {
        return this.sessionData(choice, request);
    }

    @RequestMapping(value = "/api/sessiondata", method = RequestMethod.POST)
    @ResponseBody
    public SessionData sessionData(@RequestBody NCPChoice choice, HttpServletRequest request) throws Exception {
        SessionData result = new SessionData();
        result.setElmoSessionId(context.getSession().getId());
        result.setNcpPublicKey(FiSmpApplication.getPubKeyByReturnUrl(choice.getUrl(), emregUrl));
        result.setUrl(FiSmpApplication.getUrl(choice, request));
        result.setSessionId(context.getSession().getId());
        result.setReturnUrl(returnUrl);
        return result;
    }

    @RequestMapping("/api/smp")
    @ResponseBody
    public List<NCPResult> ncps() throws Exception {
        List<NCPResult> results;
        results = (List<NCPResult>) context.getSession().getAttribute("ncps");
        if (results == null) {
            results = FiSmpApplication.getNCPs(emregUrl);
            context.getSession().setAttribute("ncps", results);
        }
        return results;
    }

    @RequestMapping("/smp/api/emreg")
    @ResponseBody
    public String smpemreg() throws URISyntaxException {
        return emreg();
    }

    @RequestMapping("/api/emreg")
    @ResponseBody
    public String emreg() throws URISyntaxException {
        String emreg = (String) context.getSession().getAttribute("emreg");
        if (emreg == null) {
            RestTemplate template = new RestTemplate();
            emreg = template.getForObject(new URI(emregUrl), String.class);
            context.getSession().setAttribute("emreg", emreg);
        }
        return emreg;
    }

    @RequestMapping("/smp/api/reports")
    @ResponseBody
    public List<VerifiedReport> smpreports() {
        return reports();
    }

    @RequestMapping("/api/reports")
    @ResponseBody
    public List<VerifiedReport> reports() {
        return (List<VerifiedReport>) this.context.getSession().getAttribute("reports");
    }

    @RequestMapping("/smp/api/questionnaire")
    @ResponseBody
    public Link smpQuestionnaireLink() {
        return questionnaireLink();
    }

    @RequestMapping("/api/questionnaire")
    @ResponseBody
    public Link questionnaireLink() {
        QuestionnaireLinkBuilder linkBuilder = new QuestionnaireLinkBuilder();
        linkBuilder.setContext(context);
        Link link = new Link();
        link.setLink(linkBuilder.buildLink());
        return link;
    }

    @RequestMapping("/smp/api/store")
    @ResponseBody
    public void smpstore() throws Exception {
        store();
    }

    @RequestMapping("/api/store")
    @ResponseBody
    public void store() throws Exception {
        Person user = (Person) context.getSession().getAttribute("shibPerson");
        boolean verified = false;
        try {
            List<VerifiedReport> results = (List<VerifiedReport>) context.getSession().getAttribute("reports");
            verified = results.get(0).getVerification().isVerified();
        } catch (ClassCastException | IndexOutOfBoundsException | NullPointerException e) {
            log.debug(e.getMessage());
        }
        byte[] bytePDF = (byte[]) context.getSession().getAttribute("pdf");
        byte[] elmoXml = ((String) context.getSession().getAttribute("elmoxmlstring")).getBytes("UTF-8");
        InstitutionDataWriter institutionDataWriter = new InstitutionDataWriter(user, dirMap, pdfBaseDir);
        institutionDataWriter.setVerified(true);
        institutionDataWriter.setEmailBodyFile(this.emailBodyFile);
        //institutionDataWriter.setDirMap(dirMap);
        //institutionDataWriter.setPdfBaseDir(pdfBaseDir);
        institutionDataWriter.writeData(bytePDF, elmoXml);

    }
}
