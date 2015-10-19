/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.smp;

import fi.csc.emrex.common.PdfGen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 *
 * @author salum
 */
@Controller
public class PDFController {

    @Autowired
    private HttpServletRequest context;

    @RequestMapping(value="/smp/elmo", method= RequestMethod.GET)
    @ResponseBody
    public FileSystemResource smpelmo( HttpServletResponse response, Model model, @CookieValue(value = "elmoSessionId") String sessionIdCookie) throws Exception{
        return  this.elmo(response, model, sessionIdCookie);
    }
    @RequestMapping(value="/elmo", method= RequestMethod.GET)
    @ResponseBody
    public FileSystemResource elmo( HttpServletResponse response, Model model, @CookieValue(value = "elmoSessionId") String sessionIdCookie) throws Exception{


        final String decodedXml = (String) context.getSession().getAttribute("elmoxmlstring");

        new PdfGen().generatePdf(decodedXml, "/tmp/elmo.pdf");

        response.setHeader("Content-disposition", "attachment;filename=elmo.pdf");
        response.setContentType("application/pdf");

        return new FileSystemResource("/tmp/elmo.pdf");
    }

}
