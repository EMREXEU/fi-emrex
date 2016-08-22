package fi.csc.emrex.smp.openpgp;

/**
 * PGP Encryptor for encryption of files source
 * https://katariakapil.wordpress.com/2009/08/22/pgp-encryption-bouncycastle-openpgp/
 * 1) Download the dependent JARS
 * http://www.bouncycastle.org/download/bcprov-jdk14-122.jar
 * http://www.bouncycastle.org/download/bcpg-jdk14-122.jar 2) Should have a
 * key/pair
 */

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Iterator;

/**
 * Implementation of the Bouncy Castle (BC) PGP Encryption/Decryption algorithm.
 * Used to encryptFile files
 */
public class PGPEncryptor {

    private static final Logger log = LoggerFactory.getLogger(PGPEncryptor.class);
    private static final String PROVIDER = "BC";

    /**
     * Initialize the Bouncy Castle provider.
     */
    public PGPEncryptor() {
        if (Security.getProvider(PROVIDER) == null) {
            Security.addProvider(new BouncyCastleProvider());
            log.info("Initialized BouncyCastle security provider");
        }
        log.debug("Initialized PGPEncryptor");
    }

    public void encryptFile(File inFile, File keyFile, File outFile, boolean isArmoredOutput) throws IOException,
            NoSuchProviderException, NoSuchAlgorithmException, PGPException {

        OutputStream out = null;
        OutputStream cOut = null;
        PGPCompressedDataGenerator comData = null;

        try {
            // get public key
            PGPPublicKey encKey = readPublicKey(keyFile);
            System.out.println(
                    "Key Strength = " + encKey.getBitStrength()
            );
            System.out.println(
                    "Algorithm = " + encKey.getAlgorithm()
            );

            int count = 0;
            for (java.util.Iterator iterator = encKey.getUserIDs(); iterator.hasNext(); ) {
                count++;
                System.out.println((String) iterator.next());
            }
            System.out.println(
                    "Key Count = " + count
            );

            // init output stream
            out = new FileOutputStream(outFile);
            if (isArmoredOutput) {
                out = new ArmoredOutputStream(out);
            }

            // encryptFile and compress input file content
            PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(PGPEncryptedData.CAST5, new SecureRandom(), PROVIDER);
            cPk.addMethod(encKey);

            cOut = cPk.open(out, new byte[1 << 16]);
            comData = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);

            // write encrypted content to a file
            PGPUtil.writeFileToLiteralData(comData.open(cOut), PGPLiteralData.BINARY, inFile, new byte[1 << 16]);

        } finally {
            if (comData != null) {
                comData.close();
            }
            if (cOut != null) {
                cOut.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    public OutputStream encryptFileToStream(File inFile, File keyFile, OutputStream out, boolean isArmoredOutput) throws IOException,
            NoSuchProviderException, NoSuchAlgorithmException, PGPException {

        OutputStream cOut = null;
        PGPCompressedDataGenerator comData = null;

        try {
            // get public key
            PGPPublicKey encKey = readPublicKey(keyFile);
            System.out.println(
                    "Key Strength = " + encKey.getBitStrength()
            );
            System.out.println(
                    "Algorithm = " + encKey.getAlgorithm()
            );

            int count = 0;
            for (java.util.Iterator iterator = encKey.getUserIDs(); iterator.hasNext(); ) {
                count++;
                System.out.println((String) iterator.next());
            }
            System.out.println(
                    "Key Count = " + count
            );

            // init output stream
            if (isArmoredOutput) {
                out = new ArmoredOutputStream(out);
            }

            // encryptFile and compress input file content
            PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(PGPEncryptedData.CAST5, new SecureRandom(), PROVIDER);
            cPk.addMethod(encKey);

            cOut = cPk.open(out, new byte[1 << 16]);
            comData = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);

            // write encrypted content to a file
            PGPUtil.writeFileToLiteralData(comData.open(cOut), PGPLiteralData.BINARY, inFile, new byte[1 << 16]);

        } catch(Exception ex) {
            log.error("Failed to encrypt body part: " + inFile.getName(), ex);
        } finally {
            if (comData != null) {
                comData.close();
            }
            if (cOut != null) {
                cOut.close();
            }
            if (out != null) {
                out.flush();
                out.close();
            }
        }
        return out;
    }

    public boolean validateEncryptionKey(File keyFile) throws IOException, PGPException {

        PGPPublicKey encKey = readPublicKey(keyFile);

        return encKey.isEncryptionKey();
    }

    private static PGPPublicKey readPublicKey(File keyFile)
            throws IOException, PGPException {

        PGPPublicKey key = null;
        InputStream in = null;
        try {
            in = PGPUtil.getDecoderStream(new FileInputStream(keyFile));
            PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(in);

            // iterate through the key rings.
            Iterator rIt = pgpPub.getKeyRings();
            while (key == null && rIt.hasNext()) {
                PGPPublicKeyRing kRing = (PGPPublicKeyRing) rIt.next();
                Iterator kIt = kRing.getPublicKeys();
                while (key == null && kIt.hasNext()) {
                    PGPPublicKey k = (PGPPublicKey) kIt.next();
                    if (k.isEncryptionKey()) {
                        key = k;
                    }
                }
            }
            if (key == null) {
                throw new IllegalArgumentException(
                        "Can’t find encryption key in key ring: " + keyFile.getCanonicalPath()
                );
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return key;
    }

}
