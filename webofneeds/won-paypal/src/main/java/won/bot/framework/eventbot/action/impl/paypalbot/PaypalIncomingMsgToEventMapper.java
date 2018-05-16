package won.bot.framework.eventbot.action.impl.paypalbot;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.BaseNeedAndConnectionSpecificEvent;
import won.bot.framework.eventbot.event.ConnectionSpecificEvent;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.MessageEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.paypalbot.PayPalEchoCommandEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.eventbot.util.EventCrawler;
import won.bot.framework.eventbot.util.PaymentModelValidator;
import won.bot.framework.eventbot.util.WonPaymentRdfUtils;
import won.protocol.agreement.AgreementProtocolState;
import won.protocol.message.WonMessage;
import won.protocol.model.Connection;
import won.protocol.util.WonConversationUtils;
import won.protocol.util.WonRdfUtils;
import won.protocol.vocabulary.WONAGR;
import won.protocol.vocabulary.WONPAY;
import won.service.payment.paypal.impl.PaypalPaymentService;
import won.service.payment.paypal.impl.PaypalPaymentStatus;

public class PaypalIncomingMsgToEventMapper extends BaseEventBotAction {

	private static final Long PROPOSAL_WAIT_TIME = 1000L;
	private static final String BOT_RECEIVER_ADDRESS = "test@won.org";

	private EventCrawler crawler = new EventCrawler(getEventListenerContext());
	private PaypalPaymentService paypalService = new PaypalPaymentService();

	public PaypalIncomingMsgToEventMapper(EventListenerContext eventListenerContext) {
		super(eventListenerContext);
	}

	@Override
	protected void doRun(Event event, EventListener executingListener) throws Exception {
		ConnectionSpecificEvent messageEvent = (ConnectionSpecificEvent) event;
		if (messageEvent instanceof MessageEvent) {
			EventListenerContext ctx = getEventListenerContext();
			EventBus bus = ctx.getEventBus();

			Connection con = ((BaseNeedAndConnectionSpecificEvent) messageEvent).getCon();
			WonMessage msg = ((MessageEvent) messageEvent).getWonMessage();

			if (isProposals(msg)) {
				handleProposal(con, bus);
			} else if (isAccepts(msg)) {
				// TODO: Find out how to handle this
				// handleAccepts(msg, con, bus);
			} else {
				String message = WonRdfUtils.MessageUtils.getTextMessage(msg).trim();
				handleMsg(message, con, bus);
			}
		}

	}

	private boolean isProposals(WonMessage msg) {

		Dataset dataset = msg.getCompleteDataset();
		Model model = dataset.getUnionModel();
		StmtIterator iterator = model.listStatements();
		while (iterator.hasNext()) {
			Statement stmt = iterator.next();
			Property prop = stmt.getPredicate();
			if (prop.equals(WONAGR.PROPOSES)) {
				return true;
			}
		}

		return false;
	}

	private boolean isAccepts(WonMessage msg) {

		Dataset dataset = msg.getCompleteDataset();
		Model model = dataset.getUnionModel();
		StmtIterator iterator = model.listStatements();
		while (iterator.hasNext()) {
			Statement stmt = iterator.next();
			Property prop = stmt.getPredicate();
			if (prop.equals(WONAGR.ACCEPTS)) {
				return true;
			}
		}

		return false;
	}

	private void handleMsg(String cmd, Connection con, EventBus bus) {
		EventListenerContext ctx = getEventListenerContext();

		if (cmd.startsWith("echo ")) {
			String echo = cmd.substring(5);
			bus.publish(new PayPalEchoCommandEvent(con, echo));
		} else if (cmd.equals("usage")) {
			bus.publish(new PayPalEchoCommandEvent(con, "echo: Repeats everything behind the echo"));
			bus.publish(new PayPalEchoCommandEvent(con, "accept: Accepts the last proposal"));
			bus.publish(new PayPalEchoCommandEvent(con,
					"payment receiver: Sends the receiver address of the bot and proposes it to you"));
			bus.publish(new PayPalEchoCommandEvent(con,
					"payment types: Sends a list of the supported payment types of the bot"));
			bus.publish(new PayPalEchoCommandEvent(con,
					"payment validate: Validates the agreed payment messages, summarizes them and proposes that"));
			bus.publish(new PayPalEchoCommandEvent(con,
					"payment generate: Takes the last accepted payment and generates it and returns the link for completing it"));
			bus.publish(new PayPalEchoCommandEvent(con, "payment check: Checks if the last payment is complete"));
			bus.publish(new PayPalEchoCommandEvent(con,
					"payment sample: Creates a sample payment message and proposes it to you"));
		} else if (cmd.equals("accept")) {
			accept(ctx, bus, con);
		} else if (cmd.equals("payment receiver")) {
			// TODO: Implement
			String msg = WonPaymentRdfUtils.PAY_RECEIVER + ": " + BOT_RECEIVER_ADDRESS;
			bus.publish(new PayPalEchoCommandEvent(con, msg));
			propose(ctx, bus, con, true, false, 1);
		} else if (cmd.equals("payment types")) {
			bus.publish(new PayPalEchoCommandEvent(con, WonPaymentRdfUtils.PAY_TYPE + ": PaypalPayment"));
			bus.publish(new PayPalEchoCommandEvent(con, WonPaymentRdfUtils.PAY_TYPE + ": Cash"));
		} else if (cmd.equals("payment validate")) {
			validate(ctx, bus, con);
		} else if (cmd.equals("payment generate")) {
			generate(ctx, bus, con);
		} else if (cmd.equals("payment check")) {
			check(ctx, bus, con);
		} else if (cmd.equals("payment sample")) {
			sample(ctx, bus, con);
		}
	}

	private void handleProposal(Connection con, EventBus bus) {
		accept(getEventListenerContext(), bus, con);
	}

	private void handleAccepts(WonMessage msg, Connection con, EventBus bus) {
		// TODO: Check if the accepted proposal source is a payment
		// Resource proposal = null;
		// Resource source = null;
		//
		// Dataset dataset = msg.getCompleteDataset();
		// Model model = dataset.getUnionModel();
		// StmtIterator iterator = model.listStatements();
		// while (iterator.hasNext()) {
		// Statement stmt = iterator.next();
		// Property prop = stmt.getPredicate();
		// if (prop.equals(WONAGR.ACCEPTS)) {
		// proposal = stmt.getObject().asResource();
		// }
		//// if (prop.equals(WONAGR.PROPOSES)) {
		//// source = stmt.getObject().asResource();
		//// }
		// }

		AgreementProtocolState agreementState = WonConversationUtils.getAgreementProtocolState(con.getConnectionURI(),
				getEventListenerContext().getLinkedDataSource());

		String msgUriString = msg.getMessageURI().toString();
		Model model = agreementState.getAgreements().getUnionModel();
		Resource payment = null;
		StmtIterator iterator = model.listStatements();
		while (iterator.hasNext()) {
			Statement stmt = iterator.next();
			Resource subj = stmt.getSubject();
			Property prop = stmt.getPredicate();
			RDFNode obj = stmt.getObject();
			// if (prop.equals(RDF.type)) {
			// if (obj.equals(WONPAY.PAYPAL_PAYMENT) ||
			// obj.equals(WONPAY.CASH_PAYMENT)) {
			// payment = obj.asResource();
			// }
			// }
			if (subj.getURI().equals(msgUriString)) {
				payment = subj;
			}
		}

		if (payment != null) {
			bus.publish(new PayPalEchoCommandEvent(con, "You accepted a Payment"));
		} else {
			bus.publish(new PayPalEchoCommandEvent(con, "You accepted something"));
		}
	}

	private void propose(EventListenerContext ctx, EventBus bus, Connection con, boolean allowOwnClauses,
			boolean allowCounterpartClauses, int count) {
		try {
			Thread.sleep(PROPOSAL_WAIT_TIME);
		} catch (InterruptedException e) {

		}
		crawler.referToEarlierMessages(ctx, bus, con, state -> {
			return state.getNLatestMessageUris(m -> {
				URI ownNeedUri = con.getNeedURI();
				URI remoteNeedUri = con.getRemoteNeedURI();
				return ownNeedUri != null && ownNeedUri.equals(m.getSenderNeedURI()) && allowOwnClauses
						|| remoteNeedUri != null && remoteNeedUri.equals(m.getSenderNeedURI())
								&& allowCounterpartClauses;

			}, count).subList(0, count);
		}, (messageModel, uris) -> WonRdfUtils.MessageUtils.addProposes(messageModel, uris),
				(Duration queryDuration, AgreementProtocolState state, URI... uris) -> {
					if (uris == null || uris.length == 0 || uris[0] == null) {
						return "Sorry, I cannot propose the messages - I did not find any.";
					}
					Optional<String> proposedString = state.getTextMessage(uris[0]);
					return proposedString.get();
				});
	}

	private void accept(EventListenerContext ctx, EventBus bus, Connection con) {
		crawler.referToEarlierMessages(ctx, bus, con, state -> {
			URI uri = state.getLatestPendingProposal(Optional.empty(), Optional.of(con.getRemoteNeedURI()));
			return uri == null ? Collections.EMPTY_LIST : Arrays.asList(uri);
		}, (messageModel, uris) -> WonRdfUtils.MessageUtils.addAccepts(messageModel, uris),
				(Duration queryDuration, AgreementProtocolState state, URI... uris) -> {
					if (uris == null || uris.length == 0 || uris[0] == null) {
						return "Sorry, I cannot accept any proposal - I did not find pending proposals";
					}
					return "Ok, I am hereby accepting your latest proposal (uri: " + uris[0] + ").";
				});
	}

	/**
	 * Creates a sample Payment Message and proposes it.
	 * 
	 * @param ctx
	 * @param bus
	 * @param con
	 */
	private void sample(EventListenerContext ctx, EventBus bus, Connection con) {
		Map<String, String> payDetails = new HashMap<>();
		payDetails.put(WonPaymentRdfUtils.PAY_TYPE, "paypalpayment");
		payDetails.put(WonPaymentRdfUtils.PAY_AMOUNT, "12 €");
		payDetails.put(WonPaymentRdfUtils.PAY_RECEIVER, BOT_RECEIVER_ADDRESS);

		Model model = WonPaymentRdfUtils.generatePaymentModel(payDetails);
		bus.publish(new ConnectionMessageCommandEvent(con, model));
		propose(ctx, bus, con, true, false, 1);
	}

	/**
	 * Validates the last agreements and creates a new Payment Message and proposes
	 * it.
	 * 
	 * @param ctx
	 * @param bus
	 * @param con
	 */
	private void validate(EventListenerContext ctx, EventBus bus, Connection con) {

		Model model = null;
		boolean valid = false;
		PaymentModelValidator validator = new PaymentModelValidator();

		try {
			Map<String, String> paymentDetails = EventCrawler.crawlPaymentDetails(con, ctx);
			model = WonPaymentRdfUtils.generatePaymentModel(paymentDetails);
			validator.validate(model);
			valid = true;
		} catch (Exception e) {
			model = WonRdfUtils.MessageUtils.textMessage(e.getMessage());
		}

		bus.publish(new ConnectionMessageCommandEvent(con, model));

		if (valid) {
			propose(ctx, bus, con, true, false, 1);
		}
	}

	/**
	 * Crawls for the last accepted payment and generates it. Afterwards the link
	 * for completing the payment will be published.
	 * 
	 * @param ctx
	 * @param bus
	 * @param con
	 */
	private void generate(EventListenerContext ctx, EventBus bus, Connection con) {
		Resource payment = EventCrawler.getLatestPaymentAgreement(con, ctx);
		PaymentModelValidator validator = new PaymentModelValidator();

		try {
			if (payment != null) {
				validator.validate(payment);
				bus.publish(new PayPalEchoCommandEvent(con, "Payment will be generated ..."));

				double amount = payment.getProperty(WONPAY.HAS_AMOUNT).getLiteral().getDouble();
				String currency = payment.getProperty(WONPAY.HAS_CURRENCY).getLiteral().getString();
				Resource type = payment.getProperty(RDF.type).getObject().asResource();

				if (WONPAY.PAYPAL_PAYMENT.equals(type)) {
					String receiver = payment.getProperty(WONPAY.HAS_RECEIVER).getLiteral().getString();
					String payKey = paypalService.create(receiver, amount, currency);
					String url = paypalService.getPaymentUrl(payKey);
					String msg = "Your PayKey: " + payKey + "   \nClick on link to pay: " + url;

					Model model = WonPaymentRdfUtils.generatePaypalKeyMessage(payment, payKey, msg);
					bus.publish(new ConnectionMessageCommandEvent(con, model));
				} else if (WONPAY.CASH_PAYMENT.equals(type)) {
					bus.publish(new PayPalEchoCommandEvent(con, "Pay in cash: " + amount + " " + currency));
				}

			} else {
				bus.publish(new PayPalEchoCommandEvent(con, "No accepted Payment yet"));
			}
		} catch (Exception e) {
			bus.publish(new PayPalEchoCommandEvent(con, e.getMessage()));
		}
	}

	private void check(EventListenerContext ctx, EventBus bus, Connection con) {
		Resource referer = EventCrawler.getLatestPaymentReference(con, ctx);

		if (referer != null) {
			if (referer.hasProperty(WONPAY.HAS_PAYPAL_TX_KEY)) {
				String payKey = referer.getProperty(WONPAY.HAS_PAYPAL_TX_KEY).getLiteral().getString();
				try {
					PaypalPaymentStatus status = paypalService.validate(payKey);
					bus.publish(new PayPalEchoCommandEvent(con, "The Payment has the following state: " + status.name()));
				} catch (Exception e) {
					bus.publish(new PayPalEchoCommandEvent(con, e.getMessage()));
				}
			} else {
				bus.publish(new PayPalEchoCommandEvent(con, "Nothing to check about this Payment"));
			}
		} else {
			bus.publish(new PayPalEchoCommandEvent(con, "No Payment generated yet"));
		}
	}

}
