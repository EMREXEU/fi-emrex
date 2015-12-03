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
    @Value("${smp.email.host}")
    private String emailHost = "mailtrap.io";
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
        String filename = this.path + "/" + generateFileName() + fileType;
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            fos.write(bytePDF);
            this.files.add(filename);
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
        Properties properties = new Properties();//System.getProperties();

        // Setup mail server
        properties.setProperty("mail.smtp.host", "mailtrap.io");
        properties.setProperty("mail.smtp.port", "2525");
        properties.setProperty("mail.smtp.user", "51654912ca3888833");
        properties.setProperty("mail.smtp.pass", "8b80f0550205cd");
        properties.setProperty("mail.smtp.auth", "true");
        //TODO set props
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
            for (String filename : this.files) {
                log.debug("endcrypting " + filename);
                File inFile = new File(filename);
                String cryptFile = filename + ".sec";
                File outFile = new File(cryptFile);
                messageBodyPart = new MimeBodyPart();
                this.pgp.encryptFile(inFile, new File(this.key), outFile, true);
                DataSource source = new FileDataSource(cryptFile);

                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(cryptFile);
                multipart.addBodyPart(messageBodyPart);
                log.debug("encrypted" + cryptFile);
            }

            // Send the complete message parts
            message.setContent(multipart);

            // Send message
            SMTPTransport t = (SMTPTransport) session.getTransport("smtp");

            t.setStartTLS(true);
            t.connect("mailtrap.io", 2525, "51654912ca3888833", "8b80f0550205cd");
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
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(InstitutionDataWriter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PGPException ex) {
            Logger.getLogger(InstitutionDataWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    public MimeMessage encrypt(Session session, MimeMessage mimeMessage, InternetAddress recipient) throws Exception {
      // get the PGP EncryptionUtilities
      EncryptionUtils pgpUtils = EncryptionManager.getEncryptionUtils(EncryptionManager.PGP);
      // load the PGP keystore from the given file.
      EncryptionKeyManager pgpKeyMgr = pgpUtils.createKeyManager();
      pgpKeyMgr.loadPublicKeystore(new FileInputStream(new File(SystemData.getWatchDogConfig() + "test.asc")), null);
      // get the PGP public key for encryption
      java.security.Key pgpKey = pgpKeyMgr.getPublicKey((String) pgpKeyMgr.publicKeyAliases().iterator().next());
      // encrypt the message
      return pgpUtils.encryptMessage(session, mimeMessage, pgpKey);
   }
}
