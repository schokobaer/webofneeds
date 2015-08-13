package common.event;


import com.hp.hpl.jena.query.Dataset;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import won.protocol.util.RdfUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * This event is used in the matching service to indicate that a new need has been found.
 * It includes the URIs of the need and the won node and optionally the serialized resource
 *
 * User: hfriedrich
 * Date: 04.06.2015
 */
public class NeedEvent
{
  private String uri;
  private String wonNodeUri;
  private String serializedNeedResource;
  private Lang serializationFormat;

  private TYPE eventType;

  public static enum TYPE
  {
    CREATED, ACTIVATED, DEACTIVATED
  }

  public NeedEvent(String uri, String wonNodeUri, TYPE eventType) {
    this.uri = uri;
    this.wonNodeUri = wonNodeUri;
    this.eventType = eventType;
    serializedNeedResource = null;
    serializationFormat = null;
  }

  public NeedEvent(String uri, String wonNodeUri, TYPE eventType, String resource, Lang format) {
    this.uri = uri;
    this.wonNodeUri = wonNodeUri;
    this.eventType = eventType;
    serializedNeedResource = resource;
    serializationFormat = format;
  }

  public NeedEvent(String uri, String wonNodeUri, TYPE eventType, Dataset ds) {
    this.uri = uri;
    this.wonNodeUri = wonNodeUri;
    this.eventType = eventType;
    StringWriter sw = new StringWriter();
    RDFDataMgr.write(sw, ds, RDFFormat.TRIG.getLang());
    serializedNeedResource = sw.toString();
    serializationFormat = RDFFormat.TRIG.getLang();
  }

  public String getUri() {
    return uri;
  }

  public String getWonNodeUri() {
    return wonNodeUri;
  }

  public TYPE getEventType() {
    return eventType;
  }

  public String getSerializedNeedResource() {
    return serializedNeedResource;
  }

  public Lang getSerializationFormat() {
    return serializationFormat;
  }

  public Dataset deserializeNeedDataset() {
    InputStream is = new ByteArrayInputStream(serializedNeedResource.getBytes(StandardCharsets.UTF_8));
    return RdfUtils.toDataset(is, new RDFFormat(serializationFormat));
  }

}