package fi.csc.emrex.smp;

import fi.csc.emrex.common.elmo.ElmoParser;
import fi.csc.emrex.common.model.Person;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by jpentika on 11/11/15.
 */
@Setter
@Slf4j
public class QuestionnaireLinkBuilder {
    private HttpServletRequest context;

    public String buildLink() {
        String sessionId = context.getSession().getId();
        Person shibPerson = (Person) context.getSession().getAttribute("shibPerson");
        String decodedXml = (String) context.getSession().getAttribute("elmoxmlstring");
        LocalDateTime startTime = (LocalDateTime) context.getSession().getAttribute("sessionStartTime");

        String hostInstitution = "X";
        String hostCountry = "X";
        String ectsImported = "X";

        if (decodedXml != null) {
            try {
                ElmoParser parser = ElmoParser.elmoParser(decodedXml);
                hostInstitution = parser.getHostInstitution();
                hostCountry =parser.getHostCountry();
                ectsImported = Integer.toString(parser.getETCSCount());
            } catch (Exception ex) {
                log.error("Creation of questionnaire url failed when decoding Elmo.", ex);
            }
        }

        String homeOrganization = "X";
        if (shibPerson != null)
            homeOrganization = shibPerson.getHomeOrganization();


        String duration = "X";
        if (startTime != null)
            duration = Double.toString(Duration.between(startTime, LocalDateTime.now()).getSeconds());


        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String link = "https://ankieter.mimuw.edu.pl/en/surveys/79/?session_id=" + sessionId;
        link += "&home_institution=" + homeOrganization;
        link += "&home_country=" + "fi";
        link += "&host_institution=" + hostInstitution;
        link += "&host_country=" + hostCountry; 
        link += "&date_of_import=" + LocalDateTime.now().format(dateFormatter);
        link += "&time_spent=" + duration;
        link += "&grades_imported=" + "X";
        link += "&ects_imported=" + ectsImported;
        link += "&grades_imported_percent=" + "X";
        link += "&ects_imported_percent=" + "X";
        return link;
    }
}
