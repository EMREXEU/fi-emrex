package fi.csc.emrex.smp;

import fi.csc.emrex.smp.openpgp.PGPEncryptor;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Session;

import com.sun.mail.smtp.SMTPTransport;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import lombok.Getter;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
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
    //@Value("${smp.email.host}")
    //private String emailHost = "mailtrap.io";
    @Value("${smp.email.sender}")
    private String emailSender = "no-reply@emrex01.csc.fi";

    private String path;

    private List<String> files;
    private PGPEncryptor pgp;

    /**
     * public InstitutionDataWriter(Person user) { this.user = user; this.email
     * = null; this.key = null; this.path = this.generatePath(); }
     */
    InstitutionDataWriter(Person user, String dirMap, String pdfBaseDir) {
        this.user = user;
        this.email = null;
        this.key = null;
        this.filename = null;
        this.dirMap = dirMap;
        this.pdfBaseDir = pdfBaseDir;
        this.generatePath();
        this.files = new ArrayList<>();
        this.pgp = new PGPEncryptor();
    }

    public void writeDataToInstitutionFolder(byte[] bytePDF, String fileType) {
        createPath();
        writeToFile(bytePDF, fileType);

    }

    void writeData(byte[] bytePDF, byte[] elmoXml) {
        this.writeDataToInstitutionFolder(bytePDF, ".pdf");
        this.writeDataToInstitutionFolder(elmoXml, ".xml");
        log.debug("Created files");
        if (this.email != null && this.key != null) {
            this.createMail();
        }
    }

    private void writeToFile(byte[] bytePDF, String fileType) {
        this.filename = generateFileName();
        String tempfilename = this.path + "/" + this.filename + fileType;
        try (FileOutputStream fos = new FileOutputStream(tempfilename)) {
            fos.write(bytePDF);
            this.files.add(tempfilename);
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

    private void createMail() {
        log.debug("Sending Mail");
        // Get system properties
        Properties properties = System.getProperties();

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);

        try {

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(this.emailSender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(this.email));
            message.setSubject(this.emailTopic);
            this.emailBody = FileUtils.readFileToString(new File(this.emailBodyFile), "UTF-8");
            BodyPart messageBodyPart = new MimeBodyPart();
            Multipart multipart = new MimeMultipart();
            messageBodyPart.setText(this.emailBody);
            multipart.addBodyPart(messageBodyPart);
            for (String tempFileName : this.files) {

                File inFile = new File(tempFileName);

                messageBodyPart = new MimeBodyPart();

                DataSource source = new FileDataSource(inFile);

                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(tempFileName);
                multipart.addBodyPart(messageBodyPart);
                log.debug("icnluded" + tempFileName);
            }
            String mailFileName = this.path + "/" + this.filename + ".eml";
            File mailFile = new File(mailFileName);
            FileOutputStream mfOutStream = new FileOutputStream(mailFile);
            multipart.writeTo(mfOutStream);
            mfOutStream.flush();
            mfOutStream.close();
            ByteArrayOutputStream mailContentStream = new ByteArrayOutputStream();
            this.pgp.encryptFileToStream(mailFile, new File(this.key), mailContentStream, true);

            // Send the complete message parts
            message.setContent(mailContentStream.toString(), "application/pgp-encrypted");

            // Send message
            SMTPTransport t = (SMTPTransport) session.getTransport("smtp");

            t.setStartTLS(true);

            if ("true".equals(properties.getProperty("mail.smtp.auth"))) {
                System.out.println(properties.getProperty("mail.smtp.host") + ", "
                        + properties.getProperty("mail.smtp.port") + ", "
                        + properties.getProperty("mail.smtp.user") + ", "
                        + properties.getProperty("mail.smtp.pass"));
                t.connect(
                        properties.getProperty("mail.smtp.host"),
                        Integer.parseInt(properties.getProperty("mail.smtp.port")),
                        properties.getProperty("mail.smtp.user"),
                        properties.getProperty("mail.smtp.pass")
                );
            } else {
                t.connect();
            }
            if (t.isConnected()) {
                log.debug("connected");
            }
            t.sendMessage(message, message.getAllRecipients());
            t.close();

        } catch (MessagingException mex) {
            mex.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(InstitutionDataWriter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(InstitutionDataWriter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PGPException ex) {
            Logger.getLogger(InstitutionDataWriter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(InstitutionDataWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
