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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 *
 * @author salum
 */
@RestController
public class JsonController {

    @Autowired
    private HttpServletRequest context;

    @Autowired
    private VirtaClient virtaClient;

    @RequestMapping(value = "/login", method = RequestMethod.GET, produces = "application/json;charset=UTF-8", headers = "Accept=*")
    public @ResponseBody
    Map<String, Object> test() {

        System.out.println("Login");
        Map<String, Object> model = new HashMap<>();
        model.put("id", "zzz");
        model.put("content", "Oh well");
        return model;
    }

    @RequestMapping(value = "/elmo", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> fetchElmoXml(HttpServletRequest request) throws Exception {

        System.out.println("elmo");
        Map<String, Object> model = new HashMap<>();
        model.put("returnUrl", context.getSession().getAttribute("returnUrl"));
        model.put("sessionId", context.getSession().getAttribute("sessionId"));

        ShibbolethHeaderHandler header = new ShibbolethHeaderHandler(request);
        header.printAttributes();
        String OID = header.getOID();
        String PersonalID = header.getPersonalID();

        model.put("elmoXml", virtaClient.fetchStudies(OID, PersonalID));

        return model;
    }

    @RequestMapping(value = "/ncp/api/elmo", method = RequestMethod.GET)
    @ResponseBody
    public String npcGetElmoJSON(@RequestParam(value = "courses", required = false) String[] courses) throws Exception {
        System.out.println("/ncp/api/elmo");
                if (courses != null) {
                    System.out.println("courses.length="+courses.length);
            for (int i = 0; i < courses.length; i++) {
                System.out.print(courses[i] + ", ");

            }System.out.println("");
        }
        return this.getElmoJSON(courses);
    }

    @RequestMapping(value = "/api/elmo", method = RequestMethod.GET)
    @ResponseBody
    public String getElmoJSON(
            @RequestParam(value = "courses", required = false) String[] courses) throws Exception {
        System.out.println("/api/elmo");
        if (courses != null) {
            for (int i = 0; i < courses.length; i++) {
                System.out.print(courses[i] + ", ");

            }System.out.println("");
        }
        try {

            ElmoParser parser = (ElmoParser) context.getSession().getAttribute("elmo");
            String xmlString;
            if (courses != null && courses.length > 0) {
                System.out.println("courses count: " + courses.length);
                List<String> courseList = Arrays.asList(courses);
                xmlString = parser.getCourseData(courseList);
            } else {
                System.out.println("null courses");
                xmlString = parser.getCourseData();
            }


            JSONObject json = XML.toJSONObject(xmlString);
            //System.out.println(json.toString());
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

    @RequestMapping("/resource")
    public Map<String, Object> home() {

        System.out.println("Here we go again");
        Map<String, Object> model = new HashMap<>();
        model.put("id", UUID.randomUUID().toString());
        model.put("content", "Hello World");
        return model;
    }



}
