package won.protocol.model;

import won.protocol.message.MessageEvent;
import won.protocol.message.WonMessageType;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.net.URI;

@Entity
@Table(name = "message_event")
public class MessageEventPlaceholder
{

  public MessageEventPlaceholder(URI parentURI, MessageEvent messageEvent){
    this.parentURI = parentURI;
    this.messageURI = messageEvent.getMessageURI();
    this.messageType = messageEvent.getMessageType();
    this.senderURI = messageEvent.getSenderURI();
    this.senderNeedURI = messageEvent.getSenderNeedURI();
    this.senderNodeURI = messageEvent.getSenderNodeURI();
    this.receiverURI = messageEvent.getReceiverURI();
    this.receiverNeedURI = messageEvent.getReceiverNeedURI();
    this.receiverNodeURI = messageEvent.getReceiverNodeURI();
  }

  @Id
  @GeneratedValue
  @Column( name = "id" )
  private Long id;

  @Column(name = "messageURI", unique = true)
  private URI messageURI;

  // this URI refers to the need (in case of create, de-/activate) or connection (in case of hint, open,
  // close, etc.)
  @Column(name = "parentURI")
  private URI parentURI;

  @Column(name = "messageType")
  private WonMessageType messageType; // ConnectMessage, CreateMessage, NeedStateMessage
  @Column(name = "senderURI")
  private URI senderURI;
  @Column(name = "senderNeedURI")
  private URI senderNeedURI;
  @Column(name = "senderNodeURI")
  private URI senderNodeURI;
  @Column(name = "receiverURI")
  private URI receiverURI;
  @Column(name = "receiverNeedURI")
  private URI receiverNeedURI;
  @Column(name = "receiverNodeURI")
  private URI receiverNodeURI;


  @XmlTransient
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public URI getMessageURI() {
    return messageURI;
  }

  public void setMessageURI(final URI messageURI) {
    this.messageURI = messageURI;
  }

  public URI getParentURI() {
    return parentURI;
  }

  public void setParentURI(final URI parentURI) {
    this.parentURI = parentURI;
  }

  public WonMessageType getMessageType() {
    return messageType;
  }

  public void setMessageType(final WonMessageType messageType) {
    this.messageType = messageType;
  }

  public URI getSenderURI() {
    return senderURI;
  }

  public void setSenderURI(final URI senderURI) {
    this.senderURI = senderURI;
  }

  public URI getSenderNeedURI() {
    return senderNeedURI;
  }

  public void setSenderNeedURI(final URI senderNeedURI) {
    this.senderNeedURI = senderNeedURI;
  }

  public URI getSenderNodeURI() {
    return senderNodeURI;
  }

  public void setSenderNodeURI(final URI senderNodeURI) {
    this.senderNodeURI = senderNodeURI;
  }

  public URI getReceiverURI() {
    return receiverURI;
  }

  public void setReceiverURI(final URI receiverURI) {
    this.receiverURI = receiverURI;
  }

  public URI getReceiverNeedURI() {
    return receiverNeedURI;
  }

  public void setReceiverNeedURI(final URI receiverNeedURI) {
    this.receiverNeedURI = receiverNeedURI;
  }

  public URI getReceiverNodeURI() {
    return receiverNodeURI;
  }

  public void setReceiverNodeURI(final URI receiverNodeURI) {
    this.receiverNodeURI = receiverNodeURI;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof MessageEventPlaceholder)) return false;

    final MessageEventPlaceholder that = (MessageEventPlaceholder) o;

    if (messageType != null ? !messageType.equals(that.messageType) : that.messageType != null) return false;
    if (messageURI != null ? !messageURI.equals(that.messageURI) : that.messageURI != null) return false;
    if (receiverURI != null ? !receiverURI.equals(that.receiverURI) : that.receiverURI != null) return false;
    if (senderURI != null ? !senderURI.equals(that.senderURI) : that.senderURI != null) return false;
//    if (signatures != null ? !signatures.equals(that.signatures) : that.signatures != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = messageURI != null ? messageURI.hashCode() : 0;
    result = 31 * result + (messageType != null ? messageType.hashCode() : 0);
    result = 31 * result + (senderURI != null ? senderURI.hashCode() : 0);
    result = 31 * result + (receiverURI != null ? receiverURI.hashCode() : 0);
    return result;
  }
}
