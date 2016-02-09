package fi.csc.emrex.smp;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {
    org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration.class
})
    public class FiSmpApplication {
    final static Logger log = LoggerFactory.getLogger(FiSmpApplication.class);

    public static final String SHIB_SHIB_IDENTITY_PROVIDER = "shib-Shib-Identity-Provider";

    public static void verifySessionId(String providedSessionId, String expectedSessionId) {

        log.info("Expected Session Id: {}", expectedSessionId);

        if (!providedSessionId.equals(expectedSessionId)) {
            throw new RuntimeException("providedSessionId does not match");
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(FiSmpApplication.class, args);
    }

    public static List<NCPResult> getNCPs(String url) throws ParseException, URISyntaxException {
        RestTemplate template = new RestTemplate();
        String result = template.getForObject(new URI(url), String.class);

        final JSONObject json = (JSONObject) new JSONParser().parse(result);
        Object NCPS = json.get("ncps");
        List<Map> ncp_list = (List<Map>) NCPS;
        List<NCPResult> results = ncp_list.stream().map(ncp -> new NCPResult(
                (String) ncp.get("countryCode"),
                (String) ncp.get("acronym"),
                (String) ncp.get("url"),
                (String) ncp.get("pubKey")
        )).collect(Collectors.toList());
        return results;
    }

    public static String getPubKeyByReturnUrl(String returnUrl, String emregUrl) throws Exception {
        String pubKey = null;
        log.info("Pubkey by url: {}", returnUrl);
        List<NCPResult> ncps = FiSmpApplication.getNCPs(emregUrl);
        for (NCPResult ncp : ncps) {
            if (ncp.getUrl().equals(returnUrl)) {
                log.info("Url matches: {}", returnUrl);
                return ncp.getCertificate();
            }
        }
        return pubKey;
    }

    public static String getUrl(NCPChoice choice, HttpServletRequest request) {
        final String idp = request.getHeader(SHIB_SHIB_IDENTITY_PROVIDER);
        return idp != null ? choice.getUrl() + "Shibboleth.sso/Login?entityID=" + request.getHeader(SHIB_SHIB_IDENTITY_PROVIDER) : choice.getUrl();
    }
}
