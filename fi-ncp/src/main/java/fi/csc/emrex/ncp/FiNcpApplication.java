package fi.csc.emrex.ncp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class FiNcpApplication {

    private static final String ELMO_XML_FIN = "src/main/resources/Example-elmo-Finland.xml";
    private static final String ELMO_XML_NOR = "src/main/resources/Example-elmo-Norway.xml";
    private static final String ELMO_XML_FIN_URL = "https://raw.githubusercontent.com/EMREXEU/fi-ncp/master/src/main/resources/Example-elmo-Finland.xml";
    private static final String ELMO_XML_SWE = "src/main/resources/Example-elmo-Sweden-1.0.xml";
    private static final String ELMO_XML_NOR_10 = "src/main/resources/nor-emrex-1.0.xml";
    public static String getElmo() throws Exception {
        return new String(Files.readAllBytes(Paths.get(new File(ELMO_XML_NOR_10).getAbsolutePath())));
    }

    public static void main(String[] args) {
        SpringApplication.run(FiNcpApplication.class, args);
    }
}
