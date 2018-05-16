package won.bot.framework.eventbot.util;

import java.util.regex.Pattern;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import won.protocol.vocabulary.WONPAY;

public class PaymentModelValidator {

	public static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
			Pattern.CASE_INSENSITIVE);
	public static final Pattern CURRENCY_PATTERN = Pattern.compile("^(EUR|USD|GBP|BTC)$", Pattern.CASE_INSENSITIVE);

	public void validate(Model model) throws Exception {
		validate(model.listSubjects().next());
	}
	
	public void validate(Resource baseRes) throws Exception {

		// get Resource
		if (baseRes == null) {
			throw new Exception("No Resource defined");
		}

		// Must haves: amount, currency, type

		// Amount
		if (!baseRes.hasProperty(WONPAY.HAS_AMOUNT)) {
			throw new Exception("No amount defined");
		}
		else if (baseRes.getProperty(WONPAY.HAS_AMOUNT).getLiteral().getDouble() <= 0) {
			throw new Exception("Negative amount defined");
		}

		// Currency
		if (!baseRes.hasProperty(WONPAY.HAS_CURRENCY)) {
			throw new Exception("No currency defined");
		}
		else if (!CURRENCY_PATTERN.matcher(
				baseRes.getProperty(WONPAY.HAS_CURRENCY).getLiteral().getString()).find()) {
			throw new Exception("No valid curreny defined: " +
					baseRes.getProperty(WONPAY.HAS_CURRENCY).getLiteral().getString());
		}

		// Type
		if (!baseRes.hasProperty(RDF.type)) {
			throw new Exception("No Payment type defined");
		} else if (baseRes.getProperty(RDF.type).getObject().asResource().equals(WONPAY.PAYPAL_PAYMENT)) {
			validatePaypal(baseRes);
		} else if (baseRes.getProperty(RDF.type).getObject().asResource().equals(WONPAY.CASH_PAYMENT)) {
			validateCash(baseRes);
		} else {
			throw new Exception("Unknown Payment type defined");
		}
	}

	private void validatePaypal(Resource baseRes) throws Exception {
		// Receiver; Must be email address
		if (!baseRes.hasProperty(WONPAY.HAS_RECEIVER)) {
			throw new Exception("No receiver defined");
		}
		else if (!EMAIL_ADDRESS_PATTERN.matcher(
				baseRes.getProperty(WONPAY.HAS_RECEIVER).getLiteral().getString()).find()) {
			throw new Exception("Unvalid receiver defined. Receiver must be an email address");
		}
	}
	
	private void validateCash(Resource baseRes) throws Exception {
		// Everything set ....
	}

}
