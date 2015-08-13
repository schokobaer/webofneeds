package won.cryptography.service;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyPairGeneratorSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.cryptography.exception.KeyNotSupportedException;
import won.cryptography.key.KeyInformationExtractor;
import won.cryptography.model.WONCryptographyModel;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;

/**
 * All kind of stuff with cryptographic keys.
 *
 * @author Fabian Salcher
 * @version 2014-07
 */
public class KeyPairService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private KeyPairGenerator keyPairGenerator = new KeyPairGeneratorSpi.ECDSA();

    public KeyPairService(KeyInformationExtractor keyInformationExtractor) {
        this.keyInformationExtractor = keyInformationExtractor;
    }

    private KeyInformationExtractor keyInformationExtractor;

    public KeyPairService() {}

    public KeyPair generateNewKeyPair() {
        KeyPair pair = null;

        try {

            // use the predefined curves
            ECGenParameterSpec ecGenSpec = new ECGenParameterSpec("brainpoolp384r1");
            keyPairGenerator.initialize(ecGenSpec, new SecureRandom());
            pair = keyPairGenerator.generateKeyPair();

        } catch (Exception e) {
            logger.warn("An error occurred!", e);
        }

        return pair;
    }

    /**
     * produces RDF out of the public key of the key pair and adds it to the
     * model of the subject
     *
     * @param subject Apache Jena Resource where the RDF will be added
     * @param keyPair KeyPair which includes the public key
     * @throws won.cryptography.exception.KeyNotSupportedException
     */
    public void appendPublicKeyRDF(Resource subject, KeyPair keyPair)
            throws KeyNotSupportedException {
        appendPublicKeyRDF(subject, keyPair.getPublic());
    }

    /**
     * produces RDF out of the public key and adds it to the model of the subject
     *
     * @param subject   Apache Jena Resource where the RDF will be added
     * @param publicKey public key which will be added
     * @throws won.cryptography.exception.KeyNotSupportedException
     */
    public void appendPublicKeyRDF(Resource subject, PublicKey publicKey)
            throws KeyNotSupportedException {

        Model model = subject.getModel();

        Resource blankNode = model.createResource();
        subject.addProperty(WONCryptographyModel.CERT_key_Property, blankNode);


        String curveID = keyInformationExtractor.getCurveID(publicKey);
        if (curveID != null) {
            blankNode.addProperty(WONCryptographyModel.CRYPT_ecc_curveID_Property,
                    curveID);
        }

        blankNode.addProperty(WONCryptographyModel.CRYPT_ecc_algorithm_Property,
                keyInformationExtractor.getAlgorithm(publicKey));

        blankNode.addProperty(WONCryptographyModel.CRYPT_ecc_qx_Property,
                keyInformationExtractor.getQX(publicKey));

        blankNode.addProperty(WONCryptographyModel.CRYPT_ecc_qy_Property,
                keyInformationExtractor.getQY(publicKey));


    }

}