package fi.csc.emrex.smp;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.bind.DatatypeConverter;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Iterator;

/**
 * Created by marko.hollanti on 07/10/15.
 */
@Slf4j
@Setter
@Component
public class SignatureVerifier {

    private String certificate;

    public boolean verifySignatureWithDecodedData(String certificate, String encodedData, Charset charset) throws Exception {

        // Instantiate the document to be signed.
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(IOUtils.toInputStream(encodedData, charset));

        return doVerifySignature(certificate, doc);
    }

    public boolean verifySignature(String certificate, String data) throws Exception {

        // Instantiate the document to be signed.
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        InputStream is = new ByteArrayInputStream(GzipUtil.gzipDecompressBytes(DatatypeConverter.parseBase64Binary(data))); // StandardCharsets.ISO_8859_1
        Document doc = dbf.newDocumentBuilder().parse(is);

        return doVerifySignature(certificate, doc);
    }

    private boolean doVerifySignature(String certificate, Document doc) throws Exception {

        // Create a DOM XMLSignatureFactory that will be used to generate the enveloped signature.
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

        // Find Signature element.
        NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) {
            throw new Exception("Cannot find Signature element");
        }

        X509Certificate cert = getCertificate(certificate);
        PublicKey pubKey = cert.getPublicKey();
        DOMValidateContext valContext = new DOMValidateContext(pubKey, nl.item(0));

        // Unmarshal the XMLSignature.
        XMLSignature signature = fac.unmarshalXMLSignature(valContext);

        // Validate the XMLSignature.
        boolean coreValidity = signature.validate(valContext);

        // Check core validation status.
        if (coreValidity == false) {
            log.error("Signature failed core validation");
            boolean sv = signature.getSignatureValue().validate(valContext);
            log.error("signature validation status: " + sv);
            if (sv == false) {
                // Check the validation status of each Reference.
                Iterator<?> i = signature.getSignedInfo().getReferences().iterator();
                for (int j = 0; i.hasNext(); j++) {
                    boolean refValid = ((Reference) i.next()).validate(valContext);
                    log.info("ref[" + j + "] validity status: " + refValid);
                }
            }
        }

        return coreValidity;
    }

    private static X509Certificate getCertificate(String certString) throws IOException, GeneralSecurityException {
        InputStream is = new ByteArrayInputStream(certString.getBytes());
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(is);
        is.close();
        return cert;
    }
}
