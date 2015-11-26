package fi.csc.emrex.smp;

import fi.csc.emrex.common.model.Person;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by jpentika on 02/11/15.
 */
@Getter
@Setter
@Slf4j
public class InstitutionDataWriter {

    private String dirMap;
    private String pdfBaseDir;
    private Person user;
    private String verificationScore;
    private boolean verified;
    private String email;
    private String key;

    private String filename;

    @Value("${smp.email.body.file}")
    private String emailBodyFile;
    private String emailBody;
    @Value("${smp.email.topic}")
    private String emailTopic;

    private String path;

    /**
     * public InstitutionDataWriter(Person user) { this.user = user; this.email
     * = null; this.key = null; this.path = this.generatePath(); }
     */
    InstitutionDataWriter(Person user, String dirMap, String pdfBaseDir) {
        this.user = user;
        this.email = null;
        this.key = null;

        this.dirMap = dirMap;
        this.pdfBaseDir = pdfBaseDir;
        this.generatePath();
    }

    public void writeDataToInstitutionFolder(byte[] bytePDF, String fileType) {
        createPath();
        writeToFile(bytePDF, fileType);

    }

    private void writeToFile(byte[] bytePDF, String fileType) {
        try (FileOutputStream fos = new FileOutputStream(this.path + "/" + generateFileName() + fileType)) {
            fos.write(bytePDF);
        } catch (IOException ioe) {
            Logger.getLogger(JsonController.class.getName()).log(Level.SEVERE, null, ioe);
            ioe.printStackTrace();
        }
    }

    private String generateFileName() {
        String filename = "emrex_";
        String name = user.getFirstName() + "_" + user.getLastName() + "_";
        filename += Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        filename += user.getHeiOid() + "_";
        filename += new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + "_";
        if (verified) {
            filename += "verified_";
        } else {
            filename += "unverified_";
        }
        log.debug("Generated filename: " + filename);
        return filename;
    }

    private void createPath() {
        //String path = generatePath();
        log.debug("Generated path:" + this.path);
        new File(path).mkdirs();
    }

    private void generatePath() {
        String dirname = this.pdfBaseDir;
        log.debug("map file: " + dirMap);
        try {

            File jsonfile = new File(dirMap);
            log.debug("JSON file location: " + jsonfile.getAbsolutePath());
            String json = FileUtils.readFileToString(jsonfile, "UTF-8");
            JSONObject root = (JSONObject) JSONValue.parse(json);

            JSONObject home = (JSONObject) root.get(user.getHomeOrganization());
            if (home != null) {
                System.out.println(home);
                this.email = (String) home.get("email");
                this.key = (String) home.get("key");
                String path = (String) home.get("path");
                if (path != null) {
                    dirname += path;
                } else {
                    dirname += "unknown_organization";
                }
            } else {
                dirname += "unknown_organization";
            }

        } catch (Exception ex) {
            Logger.getLogger(JsonController.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.path = dirname;
    }

    private void sendMail() {

    }
}
