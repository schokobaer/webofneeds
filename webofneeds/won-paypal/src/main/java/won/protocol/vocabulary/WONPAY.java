package won.protocol.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class WONPAY {

	public static final String BASE_URI = "http://purl.org/webofneeds/payment#";
	public static final String MODEL_URI = "http://purl.org/webofneeds/model#";
	
	private static Model m = ModelFactory.createDefaultModel();
	
	// Propertys
	public static Property HAS_PAYMENT_TYPE = m.createProperty(BASE_URI + "hasPaymentType");
	public static Property HAS_AMOUNT = m.createProperty(BASE_URI + "hasAmount");
	public static Property HAS_CURRENCY = m.createProperty(BASE_URI + "hasCurrency");
	
	public static Property HAS_PAYPAL_TX_KEY = m.createProperty(BASE_URI + "hasPaypalTxKey");
	
	// Models
	public static final Resource PAYPAL_PAYMENT = m.createResource(MODEL_URI + "PaypalPayment");

}
