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

/**
 * Created by jpentika on 02/11/15.
 */
@Setter
@Slf4j
public class InstitutionDataWriter {


    private String dirMap;
    private String pdfBaseDir;
    private Person user;
    private String verificationScore;

    public InstitutionDataWriter(Person user) {
        this.user = user;
    }

    public void writeDataToInstitutionFolder(byte[] bytePDF, String fileType) {
        createPath();
        writeToFile(bytePDF, fileType);
    }

    private void writeToFile(byte[] bytePDF, String fileType) {
        try (FileOutputStream fos = new FileOutputStream(generatePath() + "/" + generateFileName() + fileType)) {
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
        filename += verificationScore + "_";
        log.debug("Generated filename: " + filename);
        return filename;
    }

    private void createPath()    {
        String path = generatePath();
        log.debug("Generated path:" + path);
        new File(path).mkdirs();
    }

    private String generatePath() {
        String dirname = this.pdfBaseDir;
        log.debug("map file: " + dirMap);
        try {

            File jsonfile = new File(dirMap);
            log.debug("JSON file location: " + jsonfile.getAbsolutePath());
            String json = FileUtils.readFileToString(jsonfile, "UTF-8");
            JSONObject root = (JSONObject)JSONValue.parse(json);

            String home = (String) root.get(user.getHomeOrganization());
            if (home != null)
                dirname += home;
            else
                dirname += "unknown_organization";

        } catch (Exception ex) {
            Logger.getLogger(JsonController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dirname;
    }
}
