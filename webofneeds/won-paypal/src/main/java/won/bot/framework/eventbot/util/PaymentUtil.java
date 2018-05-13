package won.bot.framework.eventbot.util;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import won.bot.framework.eventbot.EventListenerContext;
import won.protocol.agreement.AgreementProtocolState;
import won.protocol.model.Connection;
import won.protocol.util.WonConversationUtils;
import won.protocol.vocabulary.WON;
import won.protocol.vocabulary.WONPAY;

public class PaymentUtil {

	private static Model createModelWithBaseResource() {
		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("", "no:uri");
		model.createResource(model.getNsPrefixURI(""));
		return model;
	}
	
	private static Resource createResource(Model model) {
		Resource baseRes = model.createResource(model.getNsPrefixURI(""));
		return baseRes;
	}

	public static Model generateModelByAgreements(Connection con, EventListenerContext ctx) throws Exception {
		Model model = createModelWithBaseResource();
		Resource msgResource = createResource(model);

		AgreementProtocolState state = WonConversationUtils.getAgreementProtocolState(con.getConnectionURI(),
				ctx.getLinkedDataSource());
		Dataset dataset = state.getAgreements();
		Model agreements = dataset.getUnionModel();

		Map<String, String> payDetails = new LinkedHashMap<>();
		StmtIterator iterator = agreements.listStatements();
		while (iterator.hasNext()) {
			Statement stmt = iterator.next();
			Model stmtModel = stmt.getModel();
			Property prop = stmt.getPredicate();
			if (prop.equals(WON.HAS_TEXT_MESSAGE)) {
				RDFNode obj = stmt.getObject();
				String text = obj.asLiteral().getString();
				if(text.startsWith("pay_")) {
					int posEnd = text.indexOf(":", 4);
					if (posEnd > 0) {
						String key = text.substring(4, posEnd).toLowerCase();
						String val = text.substring(posEnd+1).trim();
						payDetails.put(key, val);
					}
				}
			}
		}

		/*
		 * // Second Way: texts.clear(); String queryString = "PREFIX won: <" +
		 * WON.HAS_TEXT_MESSAGE.getNameSpace() + ">\n\n" + "SELECT ?agr\n" + "WHERE {\n"
		 * + "?s won:" + WON.HAS_TEXT_MESSAGE.getLocalName() + " ?agr\n" + "}"; Query
		 * query = QueryFactory.create(queryString); QueryExecution qExec =
		 * QueryExecutionFactory.create(query, dataset); ResultSet qResults =
		 * qExec.execSelect(); while (qResults.hasNext()) { QuerySolution qSolution =
		 * qResults.next(); texts.add(qSolution.getLiteral("agr").getString()); }
		 */

		msgResource.addProperty(RDF.type, WONPAY.PAYMENT);
		
		String message = "Type: ";
		
		// Payment Type
		if (!payDetails.containsKey("type")) {
			throw new Exception("No Payment Type set");
		}
		message += payDetails.get("type") + "\n";
		if (payDetails.get("type").equals("PaypalPayment")) {
			msgResource.addProperty(WONPAY.HAS_PAYMENT_TYPE, WONPAY.PAYPAL_PAYMENT);
		}
		else if (payDetails.get("type").equals("Cash")) {
			msgResource.addProperty(WONPAY.HAS_PAYMENT_TYPE, WONPAY.CASH_PAYMENT);
		}
		
		// Amount
		if (!payDetails.containsKey("amount")) {
			throw new Exception("No Payment Amount set");
		}
		Double amount = Double.parseDouble(payDetails.get("amount").replace("€", ""));
		msgResource.addLiteral(WONPAY.HAS_AMOUNT, amount);
		msgResource.addProperty(WONPAY.HAS_CURRENCY, "EUR");
		message += "Amount: " + amount.toString() + "\n";
		message += "Currency: EUR\n";
		
		// Receiver
		if (payDetails.containsKey("receiver")) {
			msgResource.addProperty(WONPAY.HAS_RECEIVER, payDetails.get("receiver"));
			message += "Receiver: " + payDetails.containsKey("receiver") + "\n"; 
		}
		
		msgResource.addProperty(WON.HAS_TEXT_MESSAGE, message, "en");
		

		return model;
	}
	
	

}
