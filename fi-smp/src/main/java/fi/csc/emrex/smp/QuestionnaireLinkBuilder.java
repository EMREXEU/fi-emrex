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
        Person shibPerson = (Person)context.getSession().getAttribute("shibPerson");
        String decodedXml = (String) context.getSession().getAttribute("elmoxmlstring");
        LocalDateTime startTime = (LocalDateTime)context.getSession().getAttribute("sessionStartTime");

        String hostInstitution = "X";
        String ectsImported = "X";

        if (decodedXml != null) {
            try {
                ElmoParser parser = new ElmoParser(decodedXml);
                hostInstitution = parser.getHostInstitution();
                ectsImported = Integer.toString(parser.getETCSCount());
            } catch (Exception ex)
            {
                log.error("Creation of questionary url failed when decoding Elmo.", ex);
            }
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern ("yyyy-MM-dd");

        Duration duration = Duration.between(startTime, LocalDateTime.now());

        String link = "https://ankieter.mimuw.edu.pl/surveys/79/?session_id=" + sessionId;
        link += "&home_institution=" + shibPerson.getHomeOrganization();
        link += "&home_country=" + "fi";
        link += "&host_institution=" + hostInstitution;
        link += "&host_country=" + "X"; //not found in elmo
        link += "&date_of_import=" + LocalDateTime.now().format(dateFormatter);
        link += "&time_spent=" + Double.toString(duration.getSeconds());
        link += "&grades_imported=" + "X";
        link += "&ects_imported=" + ectsImported;
        link += "&grades_imported_percent=" + "X";
        link += "&ects_imported_percent=" + "X";
        return link;
    }


}
