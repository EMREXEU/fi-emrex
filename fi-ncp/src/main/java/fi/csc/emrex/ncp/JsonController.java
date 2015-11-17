/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.ncp;

import fi.csc.emrex.common.elmo.ElmoParser;
import fi.csc.emrex.common.util.ShibbolethHeaderHandler;
import fi.csc.emrex.ncp.virta.VirtaClient;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author salum
 */
@RestController
public class JsonController {

    final static Logger log = LoggerFactory.getLogger(JsonController.class);

    @Autowired
    private HttpServletRequest context;

    @Autowired
    private VirtaClient virtaClient;


    @RequestMapping(value = "/elmo", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> fetchElmoXml(HttpServletRequest request) throws Exception {


        Map<String, Object> model = new HashMap<>();
        model.put("returnUrl", context.getSession().getAttribute("returnUrl"));
        model.put("sessionId", context.getSession().getAttribute("sessionId"));

        ShibbolethHeaderHandler header = new ShibbolethHeaderHandler(request);
        header.stringifyHeader();
        String OID = header.getHeiOid();
        String PersonalID = header.getPersonalID();
        log.info("Fetching data from Virta client OID: {} PersonalID {}", OID, PersonalID);
        model.put("elmoXml", virtaClient.fetchStudies(OID, PersonalID));

        return model;
    }

    @RequestMapping(value = "/ncp/api/elmo", method = RequestMethod.GET)
    @ResponseBody
    public String npcGetElmoJSON(@RequestParam(value = "courses", required = false) String[] courses) throws Exception {
        if (courses != null) {
            log.debug("Courses.length= {}", courses.length);
            for (int i = 0; i < courses.length; i++) {
                log.trace("Course {} ", courses[i]);

            }
        }
        return this.getElmoJSON(courses);
    }

    @RequestMapping(value = "/api/elmo", method = RequestMethod.GET)
    @ResponseBody
    public String getElmoJSON(
            @RequestParam(value = "courses", required = false) String[] courses) throws Exception {
        if (courses != null) {
            for (int i = 0; i < courses.length; i++) {
                log.trace("Course: {}", courses[i]);
            }
        }
        try {

            ElmoParser parser = (ElmoParser) context.getSession().getAttribute("elmo");
            String xmlString;
            if (courses != null && courses.length > 0) {
                log.debug("Courses count: {}", courses.length);
                List<String> courseList = Arrays.asList(courses);
                xmlString = parser.getCourseData(courseList);
            } else {
                log.error("No selected courses");
                xmlString = parser.getCourseData();
            }

            JSONObject json = XML.toJSONObject(xmlString);
            return json.toString();
        } catch (Exception e) {

            StackTraceElement elements[] = e.getStackTrace();
            Map<String, Object> error = new HashMap<String, Object>();
            Map<String, Object> log = new HashMap<String, Object>();
            error.put("message", e.getMessage());
            for (int i = 0, n = elements.length; i < n; i++) {
                log.put(elements[i].getFileName() + " " + elements[i].getLineNumber(),
                        elements[i].getMethodName());
            }
            error.put("stack", log);
            return new JSONObject(error).toString();
        }
    }

}
