package won.protocol.util;

import com.google.common.collect.HashBiMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import won.protocol.exception.DataIntegrityException;
import won.protocol.exception.IncorrectPropertyCountException;
import won.protocol.message.WonMessageBuilder;
import won.protocol.model.MatchingBehaviorType;
import won.protocol.model.NeedContentPropertyType;
import won.protocol.model.NeedGraphType;
import won.protocol.model.NeedState;
import won.protocol.vocabulary.WON;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * This class wraps the need models (need and sysinfo graphs in a need dataset).
 * It provides abstraction for the need structure of is/seeks content nodes that are part of the need model.
 * It can be used to load and query an existing need dataset (or models).
 * Furthermore it can be used to create a need model by adding triples.
 * <p>
 * Created by hfriedrich on 16.03.2017.
 */
public class NeedModelWrapper {
    protected Model needModel;
    protected Model sysInfoModel;

    private final HashBiMap<MatchingBehaviorType, Resource> matchingBehaviorMap = initMap();

    /**
     * Create a new need model (incluing sysinfo)
     *
     * @param needUri need uri to create the need models for
     */
    public NeedModelWrapper(String needUri) {

        needModel = ModelFactory.createDefaultModel();
        DefaultPrefixUtils.setDefaultPrefixes(needModel);
        needModel.createResource(needUri, WON.NEED);
        sysInfoModel = ModelFactory.createDefaultModel();
        DefaultPrefixUtils.setDefaultPrefixes(sysInfoModel);
        sysInfoModel.createResource(needUri, WON.NEED);
    }

    /**
     * Load a need dataset and extract the need and sysinfo models from it
     *
     * @param ds need dataset to load
     */
    public NeedModelWrapper(Dataset ds) {

        Iterator<String> iter = ds.listNames();
        while (iter.hasNext()) {
            String m = iter.next();
            if (m.endsWith("#need") || m.contains(WonMessageBuilder.CONTENT_URI_SUFFIX)) {
                needModel = ds.getNamedModel(m);
                needModel.setNsPrefixes(ds.getDefaultModel().getNsPrefixMap());
            } else if (m.endsWith("#sysinfo")) {
                sysInfoModel = ds.getNamedModel(m);
                sysInfoModel.setNsPrefixes(ds.getDefaultModel().getNsPrefixMap());
            }
        }

        if ((sysInfoModel == null) && (needModel != null)) {
            this.sysInfoModel = ModelFactory.createDefaultModel();
            DefaultPrefixUtils.setDefaultPrefixes(this.sysInfoModel);
            this.sysInfoModel.createResource(getNeedUri(), WON.NEED);
        }

        if ((needModel == null) && (sysInfoModel != null)) {
            this.needModel = ModelFactory.createDefaultModel();
            DefaultPrefixUtils.setDefaultPrefixes(this.needModel);
            this.needModel.createResource(getNeedNode(NeedGraphType.SYSINFO).getURI(), WON.NEED);
        }

        checkModels();
    }

    /**
     * Load the need and sysinfo models, if one of these models is null then initialize the other one as default model
     *
     * @param needModel
     * @param sysInfoModel
     */
    public NeedModelWrapper(Model needModel, Model sysInfoModel) {

        this.needModel = needModel;
        this.sysInfoModel = sysInfoModel;

        if ((sysInfoModel == null) && (needModel != null)) {
            this.sysInfoModel = ModelFactory.createDefaultModel();
            DefaultPrefixUtils.setDefaultPrefixes(this.sysInfoModel);
            this.sysInfoModel.createResource(getNeedUri(), WON.NEED);
        }

        if ((needModel == null) && (sysInfoModel != null)) {
            this.needModel = ModelFactory.createDefaultModel();
            DefaultPrefixUtils.setDefaultPrefixes(this.needModel);
            this.needModel.createResource(getNeedNode(NeedGraphType.SYSINFO).getURI(), WON.NEED);
        }

        checkModels();
    }

    private HashBiMap initMap() {

        HashBiMap<MatchingBehaviorType, Resource> matchingBehaviorMap = HashBiMap.create();
        matchingBehaviorMap.put(MatchingBehaviorType.MUTUAL, WON.MATCHING_BEHAVIOR_MUTUAL);
        matchingBehaviorMap.put(MatchingBehaviorType.DO_NOT_MATCH, WON.MATCHING_BEHAVIOR_DO_NOT_MATCH);
        matchingBehaviorMap.put(MatchingBehaviorType.LAZY, WON.MATCHING_BEHAVIOR_LAZY);
        matchingBehaviorMap.put(MatchingBehaviorType.STEALTHY, WON.MATCHING_BEHAVIOR_STEALTHY);
        return matchingBehaviorMap;
    }

    private void checkModels() {
        try {
            getNeedNode(NeedGraphType.NEED);
            getNeedNode(NeedGraphType.SYSINFO);
        } catch (NullPointerException e1) {
            throw new DataIntegrityException("at least one graph of need or sysinfo must exist in dataset", e1);
        } catch (IncorrectPropertyCountException e2) {
            throw new DataIntegrityException("need and sysinfo models must be a won:Need");
        }
    }

    /**
     * get the need or sysinfo model
     *
     * @param graph type specifies the need or sysinfo model to return
     * @return need or sysinfo model
     */
    public Model getNeedModel(NeedGraphType graph) {

        if (graph.equals(NeedGraphType.NEED)) {
            return needModel;
        } else {
            return sysInfoModel;
        }
    }

    /**
     * get the node of the need of either the need model or the sysinfo model
     *
     * @param graph type specifies the need or sysinfo need node to return
     * @return need or sysinfo need node
     */
    public Resource getNeedNode(NeedGraphType graph) {

        if (graph.equals(NeedGraphType.NEED)) {
            return RdfUtils.findOneSubjectResource(needModel, RDF.type, WON.NEED);
        } else {
            return RdfUtils.findOneSubjectResource(sysInfoModel, RDF.type, WON.NEED);
        }
    }

    public String getNeedUri() {
        return getNeedNode(NeedGraphType.NEED).getURI();
    }

    public void setMatchingBehavior(MatchingBehaviorType matchingBehavior) {

        Resource matchingResource = matchingBehaviorMap.get(matchingBehavior);
        Resource need = getNeedNode(NeedGraphType.NEED);
        need.removeAll(WON.HAS_MATCHING_BEHAVIOR);
        need.addProperty(WON.HAS_MATCHING_BEHAVIOR, matchingResource);
    }

    public MatchingBehaviorType getMatchingBehavior() {

        RDFNode matchingBehavior = RdfUtils.findOnePropertyFromResource(
                needModel, getNeedNode(NeedGraphType.NEED), WON.HAS_MATCHING_BEHAVIOR);

        // default matching behavior is MUTUAL
        if (matchingBehavior == null) {
            return MatchingBehaviorType.MUTUAL;
        }

        return matchingBehaviorMap.inverse().get(matchingBehavior.asResource());
    }

    public void addFlag(Resource flag) {
        getNeedNode(NeedGraphType.NEED).addProperty(WON.HAS_FLAG, flag);
    }

    public boolean hasFlag(Resource flag) {
        return getNeedNode(NeedGraphType.NEED).hasProperty(WON.HAS_FLAG, flag);
    }

    public void addFacetUri(String facetUri) {

        Resource facet = needModel.createResource(facetUri);
        getNeedNode(NeedGraphType.NEED).addProperty(WON.HAS_FACET, facet);
    }

    public Collection<String> getFacetUris() {

        Collection<String> facetUris = new LinkedList<>();
        NodeIterator iter = needModel.listObjectsOfProperty(getNeedNode(NeedGraphType.NEED), WON.HAS_FACET);
        while (iter.hasNext()) {
            facetUris.add(iter.next().asResource().getURI());
        }
        return facetUris;
    }

    public void setNeedState(NeedState state) {

        Resource stateRes = NeedState.ACTIVE.equals(state) ? WON.NEED_STATE_ACTIVE : WON.NEED_STATE_INACTIVE;
        Resource need = getNeedNode(NeedGraphType.SYSINFO);
        need.removeAll(WON.IS_IN_STATE);
        need.addProperty(WON.IS_IN_STATE, stateRes);
    }

    public NeedState getNeedState() {

        RDFNode state = RdfUtils.findOnePropertyFromResource(sysInfoModel, getNeedNode(NeedGraphType.SYSINFO), WON.IS_IN_STATE);
        if (state.equals(WON.NEED_STATE_ACTIVE)) {
            return NeedState.ACTIVE;
        } else {
            return NeedState.INACTIVE;
        }
    }

    public ZonedDateTime getCreationDate() {

        String dateString = RdfUtils.findOnePropertyFromResource(
                sysInfoModel, getNeedNode(NeedGraphType.SYSINFO), DCTerms.created).asLiteral().getString();
        return ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
    }

    public void setConnectionContainerUri(String containerUri) {
        Resource container = sysInfoModel.createResource(containerUri);
        Resource need = getNeedNode(NeedGraphType.SYSINFO);
        need.removeAll(WON.HAS_CONNECTIONS);
        need.addProperty(WON.HAS_CONNECTIONS, container);
    }

    public String getConnectionContainerUri() {
        return RdfUtils.findOnePropertyFromResource(
                sysInfoModel, getNeedNode(NeedGraphType.SYSINFO), WON.HAS_CONNECTIONS).asResource().getURI();
    }

    public void setWonNodeUri(String nodeUri) {

        Resource node = sysInfoModel.createResource(nodeUri);
        Resource need = getNeedNode(NeedGraphType.SYSINFO);
        need.removeAll(WON.HAS_WON_NODE);
        need.addProperty(WON.HAS_WON_NODE, node);
    }

    public String getWonNodeUri() {
        return RdfUtils.findOnePropertyFromResource(
                sysInfoModel, getNeedNode(NeedGraphType.SYSINFO), WON.HAS_WON_NODE).asResource().getURI();
    }

    /**
     * create a content node below the need node of the need model.
     *
     * @param type specifies which property (e.g. IS, SEEKS, ...) is used to connect the need node with the content node
     * @param uri  uri of the content node, if null then create blank node
     * @return content node created
     */
    public Resource createContentNode(NeedContentPropertyType type, String uri) {

        if (NeedContentPropertyType.ALL.equals(type)) {
            throw new IllegalArgumentException("NeedContentPropertyType.ALL not defined for this method");
        }

        Resource contentNode = (uri != null) ? needModel.createResource(uri) : needModel.createResource();
        addContentPropertyToNeedNode(type, contentNode);
        return contentNode;
    }

    private void addContentPropertyToNeedNode(NeedContentPropertyType type, RDFNode contentNode) {

        Resource needNode = getNeedNode(NeedGraphType.NEED);
        if (NeedContentPropertyType.IS.equals(type)) {
            needNode.addProperty(WON.IS, contentNode);
        } else if (NeedContentPropertyType.SEEKS.equals(type)) {
            needNode.addProperty(WON.SEEKS, contentNode);
        } else if (NeedContentPropertyType.SEEKS_SEEKS.equals(type)) {
            Resource intermediate = needModel.createResource();
            needNode.addProperty(WON.SEEKS, intermediate);
            intermediate.addProperty(WON.SEEKS, contentNode);
        } else if (NeedContentPropertyType.IS_AND_SEEKS.equals(type)) {
            needNode.addProperty(WON.IS, contentNode);
            needNode.addProperty(WON.SEEKS, contentNode);
        }
    }

    /**
     * get all content nodes of a specified type
     *
     * @param type specifies which content nodes to return (IS, SEEKS, ALL, ...)
     * @return content nodes
     */
    public Collection<Resource> getContentNodes(NeedContentPropertyType type) {

        Collection<Resource> contentNodes = new LinkedList<>();
        String queryClause = null;
        String isClause = "{ ?needNode a won:Need. ?needNode won:is ?contentNode. }";
        String isAndSeeksClause = "{ ?needNode a won:Need. ?needNode won:is ?contentNode. ?needNode won:seeks ?contentNode. }";
        String seeksClause = "{ ?needNode a won:Need. ?needNode won:seeks ?contentNode. FILTER NOT EXISTS { ?needNode won:seeks/won:seeks ?contentNode. } }";
        String seeksSeeksClause = "{ ?needNode a won:Need. ?needNode won:seeks/won:seeks ?contentNode. }";

        switch (type) {
            case IS:
                queryClause = isClause;
                break;
            case SEEKS:
                queryClause = seeksClause;
                break;
            case IS_AND_SEEKS:
                queryClause = isAndSeeksClause;
                break;
            case SEEKS_SEEKS:
                queryClause = seeksSeeksClause;
                break;
            case ALL:
                queryClause = isClause + "UNION \n" + seeksClause + "UNION \n" + seeksSeeksClause;
        }

        String queryString = "prefix won: <http://purl.org/webofneeds/model#> \n" +
                "SELECT DISTINCT ?contentNode WHERE { \n" + queryClause + "\n }";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, needModel);
        ResultSet rs = qexec.execSelect();

        while (rs.hasNext()) {
            QuerySolution qs = rs.next();
            if (qs.contains("contentNode")) {
                contentNodes.add(qs.get("contentNode").asResource());
            }
        }

        return contentNodes;
    }

    public void setContentPropertyStringValue(NeedContentPropertyType type, Property p, String value) {

        Collection<Resource> nodes = getContentNodes(type);
        for (Resource node : nodes) {
            node.removeAll(p);
            node.addLiteral(p, value);
        }
    }

    public void addContentPropertyStringValue(NeedContentPropertyType type, Property p, String value) {

        Collection<Resource> nodes = getContentNodes(type);
        for (Resource node : nodes) {
            node.addLiteral(p, value);
        }
    }

    public String getContentPropertyStringValue(NeedContentPropertyType type, Property p) {
        return getContentPropertyObject(type, p).asLiteral().getString();
    }

    public String getContentPropertyStringValue(NeedContentPropertyType type, String propertyPath) {
        Node node = getContentPropertyObject(type, propertyPath);
        return node != null ? node.getLiteralLexicalForm() : null;
    }

    public Collection<String> getContentPropertyStringValues(NeedContentPropertyType type, Property p) {

        Collection<String> values = new LinkedList<>();
        Collection<Resource> nodes = getContentNodes(type);
        for (Resource node : nodes) {
            NodeIterator nodeIterator = needModel.listObjectsOfProperty(node, p);
            while (nodeIterator.hasNext()) {
                values.add(nodeIterator.next().asLiteral().getString());
            }
        }
        return values;
    }

    private RDFNode getContentPropertyObject(NeedContentPropertyType type, Property p) {

        Collection<Resource> nodes = getContentNodes(type);
        RDFNode object = null;
        for (Resource node : nodes) {
            NodeIterator nodeIterator = needModel.listObjectsOfProperty(node, p);
            if (nodeIterator.hasNext()) {
                if (object != null) {
                    throw new IncorrectPropertyCountException("expected exactly one occurrence of property " + p.getURI(), 1, 2);
                }
                object = nodeIterator.next();
            }
        }

        if (object == null) {
            throw new IncorrectPropertyCountException("expected exactly one occurrence of property " + p.getURI(), 1, 0);
        }

        return object;
    }

    private Node getContentPropertyObject(NeedContentPropertyType type, String propertyPath) {

        Path path = PathParser.parse(propertyPath, DefaultPrefixUtils.getDefaultPrefixes());
        Collection<Resource> nodes = getContentNodes(type);

        if (nodes.size() != 1) {
            throw new IncorrectPropertyCountException("expected exactly one occurrence of object for property path " +
                    propertyPath, 1, nodes.size());
        }

        Node node = nodes.iterator().next().asNode();
        return RdfUtils.getNodeForPropertyPath(needModel, node, path);
    }

    private boolean isSplittableNode(RDFNode node) {
        return node.isResource() &&
                (node.isAnon() ||
                        (   node.asResource().getURI().startsWith(getNeedUri()) &&
                                (! node.asResource().getURI().equals(getNeedUri()))
                        ));
    }

    /**
     * Returns the RDFNodes reachable from the specified startNode in breadth-first order.
     * @param startNode
     * @param model
     * @return
     */
    private List<RDFNode> breadthFirstOrder(RDFNode startNode, Model model){
        return breadthFirstOrder(startNode, model, Collections.emptyList());
    }

    /**
     * Returns the RDFNodes reachable from the specified startNode in breadth-first order.
     * @param startNode
     * @param model
     * @param blacklist
     * @return
     */
    private List<RDFNode> breadthFirstOrder(RDFNode startNode, Model model, List<RDFNode> blacklist){
        if (!startNode.isResource()) return Collections.emptyList();
        if (blacklist.contains(startNode)) return Collections.emptyList();
        Resource startResource = startNode.asResource();
        List<RDFNode> toVisit =  model.listObjectsOfProperty(startResource, null).toList();
        List<RDFNode> result = new ArrayList<>();
        result.add(0, startNode); //put start node first -> breadth first
        List<RDFNode> newBlacklist = new ArrayList<>(blacklist.size()+1);
        newBlacklist.add(startNode); //don't revisit
        newBlacklist.addAll(blacklist);
        for(RDFNode newStartNode: toVisit) {
            //recurse
            result.addAll(breadthFirstOrder(newStartNode, model, Collections.unmodifiableList(newBlacklist)));
        }
        return result;
    }

    /**
     * Returns a copy of the model in which no node reachable from the need node has multiple incoming edges
     * (unless the graph contains a circle, see below). This is achieved by making copies of all nodes that have multiple
     * incoming edges, such that each copy and the original get on of the incoming edges. The outgoing
     * edges of the original are replicated in the copies.
     *
     * Nodes are visited in breadth first order. Each node is only be split once.
     * Nodes that were newly introduced by this algorithm are never split.
     *
     * In that special case that the graph contains a circle, the resulting graph still contains a circle, and
     * possibly one or more nodes with more than one incoming edge.
     *
     * @return
     */
    public Model normalizeNeedModel() {
        Model copy = RdfUtils.cloneModel(needModel);
        Set<RDFNode> blacklist = new HashSet<>();
        RDFNode needNode = copy.getResource(getNeedUri().toString());
        List<RDFNode> nodesInBreadthFirstOrderFromNeed = breadthFirstOrder(needNode, copy);
        List<RDFNode> nodesToVisit = new ArrayList<>();
        nodesToVisit.addAll(nodesInBreadthFirstOrderFromNeed);
        //now add all the other nodes that are objects
        List<RDFNode> otherNodes = needModel.listObjects().toList();
        otherNodes.removeAll(nodesInBreadthFirstOrderFromNeed);
        nodesToVisit.addAll(otherNodes);
        for(RDFNode toSplitIfNecessary: nodesToVisit) {
            List<RDFNode> processedAndCreatedNodes = findAndSplitOneNode(copy,toSplitIfNecessary, blacklist);
            blacklist.addAll(processedAndCreatedNodes);
        }
        return copy;
    }

    /**
     * Finds a node to split and applies the binary split until it is
     * unsplit (i.e. each copy and the remaining original has exactly one incoming edge.
     *
     * The split node and all new copies are returned and are not to be processed
     * any further during the splitting algorithm.
     *
     * @return true if a node was split, false if there were none
     */
    private List<RDFNode> findAndSplitOneNode(Model model, RDFNode node, Set<RDFNode> blacklist) {

        if (!blacklist.contains(node) && isSplittableNode(node)) {
            StmtIterator stmtIt = model.listStatements(null, null, node);
            List<Resource> subjectsOfInEdges = new ArrayList<>();
            while (stmtIt.hasNext()) {
                Statement stmt = stmtIt.nextStatement();
                subjectsOfInEdges.add(stmt.getSubject());
            }
            if (subjectsOfInEdges.size() > 1) {
                //more than 1 in-edges. split node
                //remember the new nodes and the current node and pass them back as result
                //so they can be added to the blacklist
                List<RDFNode> newlyProcessed = new ArrayList<>();
                for (Resource subjectToSplitOff : subjectsOfInEdges) {
                    RDFNode newNode = binarySplitNode(node.asResource(), subjectToSplitOff, model);
                    newlyProcessed.add(newNode);
                }
                newlyProcessed.add(node);
                return newlyProcessed;
            }
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    /**
     * Splits one copy off the specified node, connects it with the specified subject, copies all outgoing edges
     * and returns the newly generated copy.
     * @param node
     * @param subjectToSplitOff
     * @param model
     * @return
     */
    private RDFNode binarySplitNode(Resource node, Resource subjectToSplitOff, Model model) {
        //make copy
        Resource copy = copyNode(node, model);
        //re-point one edge from subjectToSplitOff to copy (doesn't matter if it has multiple edges to it or not)
        StmtIterator it = model.listStatements(subjectToSplitOff, null, node);
        Statement statement = it.nextStatement();
        it.close();
        Statement copiedStatement = new StatementImpl(statement.getSubject(), statement.getPredicate(), copy);
        model.add(copiedStatement);
        model.remove(statement);
        List<Statement> statementList = new ArrayList<>();
        //copy outgoing edges from node to copy
        it = model.listStatements(node, null, (RDFNode)null);
        while(it.hasNext()) {
            statement = it.nextStatement();
            copiedStatement = new StatementImpl(copy, statement.getPredicate(), statement.getObject());
            statementList.add(copiedStatement);
        }
        model.add(statementList);
        return copy;
    }

    private Resource copyNode(Resource node, Model model) {
        if (node.isAnon()) return model.createResource();
        int i = 0;
        String uri = node.getURI() + RandomStringUtils.randomAlphanumeric(4);
        String newUri = uri+"_"+ i;
        while (model.containsResource(new ResourceImpl(newUri))){
            i++;
            newUri = uri+"_"+i;
        }
        return model.getResource(newUri);
    }
}
