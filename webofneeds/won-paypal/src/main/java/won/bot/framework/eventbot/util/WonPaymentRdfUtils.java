package won.bot.framework.eventbot.util;

import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import won.protocol.vocabulary.WON;
import won.protocol.vocabulary.WONPAY;

public class WonPaymentRdfUtils {

	public static final String PAY_AMOUNT = "pay_amount";
	public static final String PAY_TYPE = "pay_type";
	public static final String PAY_RECEIVER = "pay_receiver";
	public static final String PAY_TAX = "pay_tax";
	public static final String PAY_INVOICE_NUMBER = "pay_invoicenumber";
	public static final String PAY_INVOICE_DETAILS = "pay_invoicedetails";
	public static final String PAY_FEE_PAYER = "pay_feepayer";

	public static Model createModelWithBaseResource() {
		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("", "no:uri");
		model.createResource(model.getNsPrefixURI(""));
		return model;
	}

	public static Resource createResource(Model model) {
		Resource baseRes = model.createResource(model.getNsPrefixURI(""));
		return baseRes;
	}
		
	/**
	 * Generates a Payment Model given by the Details.
	 * @param paymentDetails Details of Payment.
	 * @return A Model with the parsed Details.
	 */
	public static Model generatePaymentModel(Map<String, String> paymentDetails) {
		Model model = createModelWithBaseResource();
		Resource msgResource = createResource(model);

		String message = "Type: ";

		// Payment Type
		if (paymentDetails.containsKey(PAY_TYPE)) {
			String type = paymentDetails.get(PAY_TYPE).toLowerCase();
			message += paymentDetails.get(PAY_TYPE) + "\n";
			if ("paypalpayment".equals(type)) {
				msgResource.addProperty(RDF.type, WONPAY.PAYPAL_PAYMENT);
			} else if ("cash".equals(type)) {
				msgResource.addProperty(RDF.type, WONPAY.CASH_PAYMENT);
			}
		}

		// Amount
		if (paymentDetails.containsKey(PAY_AMOUNT)) {
			Double amount = Double.parseDouble(paymentDetails.get(PAY_AMOUNT).replace("€", ""));
			msgResource.addLiteral(WONPAY.HAS_AMOUNT, amount);
			msgResource.addProperty(WONPAY.HAS_CURRENCY, "EUR");
			message += "Amount: " + amount.toString() + "\n";
			message += "Currency: EUR\n";
		}

		// Receiver
		if (paymentDetails.containsKey(PAY_RECEIVER)) {
			msgResource.addProperty(WONPAY.HAS_RECEIVER, paymentDetails.get(PAY_RECEIVER));
			message += "Receiver: " + paymentDetails.get(PAY_RECEIVER) + "\n";
		}

		// Tax
		if (paymentDetails.containsKey(PAY_TAX)) {
			Double tax = Double.parseDouble(paymentDetails.get(PAY_TAX).replace("€", ""));
			msgResource.addLiteral(WONPAY.HAS_TAX, tax);
			message += "Tax: € " + tax + "\n";
		}

		// Invoice Number
		if (paymentDetails.containsKey(PAY_INVOICE_NUMBER)) {
			msgResource.addLiteral(WONPAY.HAS_INVOICE_NUMBER, paymentDetails.get(PAY_INVOICE_NUMBER));
			message += "Invoice Number: " + paymentDetails.get(PAY_INVOICE_NUMBER) + "\n";
		}

		// Invoice Details
		if (paymentDetails.containsKey(PAY_INVOICE_DETAILS)) {
			msgResource.addLiteral(WONPAY.HAS_INVOICE_DETAILS, paymentDetails.get(PAY_INVOICE_DETAILS));
			message += "Invoice Details: " + paymentDetails.get(PAY_INVOICE_DETAILS) + "\n";
		}

		// Payment Type
		if (paymentDetails.containsKey(PAY_FEE_PAYER)) {
			String feePayer = paymentDetails.get(PAY_FEE_PAYER).toLowerCase();
			message += paymentDetails.get(PAY_TYPE) + "\n";
			if ("sender".equals(feePayer)) {
				msgResource.addProperty(WONPAY.HAS_FEE_PAYER, WONPAY.FEE_PAYER_SENDER);
			} else if ("receiver".equals(feePayer)) {
				msgResource.addProperty(WONPAY.HAS_FEE_PAYER, WONPAY.FEE_PAYER_RECEIVER);
			}
		}

		// Message
		msgResource.addProperty(WON.HAS_TEXT_MESSAGE, message, "en");

		return model;
	}

}
