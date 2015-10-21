/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.smp;

import fi.csc.emrex.smp.model.Person;
import fi.csc.emrex.smp.model.VerifiedReport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONTokener;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author salum
 */
@Controller
public class JsonController {

    @Value("${emreg.url}")
    private String emregUrl;

    @Value("${smp.return.url}")
    private String returnUrl;

    @Value("${smp.university.directory.map}")
    private String dirMap;
    @Value("${smp.university.base.directory}")
    private String pdfBaseDir;

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

    private void printAttributes(HttpServletRequest request) {
        if (request != null) {

            final Enumeration<String> attributeNames = request.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                final String name = attributeNames.nextElement();
                System.out.println(name + ": " + request.getAttribute(name).toString());
            }
        }
    }

    @RequestMapping("/smp/api/store")
    @ResponseBody
    public void smpstore() {
        store();
    }

    @RequestMapping("/api/store")
    @ResponseBody
    public void store() {
        FileReader fr = null;
        String dirname = this.pdfBaseDir;
        String filename = "emrex_";
        Person user = (Person) context.getSession().getAttribute("shibPerson");
        filename += user.getFirstName() + "_" + user.getLastName() + "_";
        filename += new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + "_";
        //TODO verification result
        byte[] bytePDF = (byte[]) context.getSession().getAttribute("pdf");

        try {
            fr = new FileReader(new File(dirMap));
            JSONTokener tokener = new JSONTokener(fr);
            JSONObject root = new JSONObject((Map) tokener);

            String home = (String) root.get(user.getHomeOrganization());
            if (home != null) {
                dirname += home + "/";
            } else {
                //TODO default here
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JsonController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                Logger.getLogger(JsonController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream(dirname + filename + ".pdf");
            fos.write(bytePDF);
        } catch (IOException ioe) {
            Logger.getLogger(JsonController.class.getName()).log(Level.SEVERE, null, ioe);
            ioe.printStackTrace();
        }
    }
}
