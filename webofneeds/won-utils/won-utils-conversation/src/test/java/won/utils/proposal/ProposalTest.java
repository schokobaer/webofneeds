package won.utils.proposal;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import won.protocol.agreement.HighlevelFunctionFactory;
import won.protocol.util.RdfUtils;

import java.io.IOException;
import java.io.InputStream;

public class ProposalTest {

    private static final String inputFolder = "/won/utils/proposal/input/";
    private static final String expectedOutputFolder = "/won/utils/proposal/expected/";
    
    @BeforeClass
    public static void setLogLevel() {
    	Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    	root.setLevel(Level.INFO);	
    }
 
    // This is the case where there is no open proposal...(each exist in their own envelope, both are accepted in an agreement)
    @Test @Ignore
    public void noOpenProposal () throws IOException {
        Dataset input = loadDataset( inputFolder + "2proposal-bothaccepted.trig");
        Dataset expectedOutput = loadDataset( expectedOutputFolder + "2proposal-bothaccepted.trig");
        test(input,expectedOutput);
    }
    

    // This is the case where there is one open proposal...(each exist in their own envelope, only one is accepted in an agreement)
    @Test @Ignore
    public void oneOpenProposal () throws IOException {
        Dataset input = loadDataset( inputFolder + "2proposal-one-accepted.trig");
        Dataset expectedOutput = loadDataset( expectedOutputFolder + "2proposal-one-accepted.trig");
        test(input,expectedOutput);
    }
    
    // This is the case where there are two open proposals ...(each exist in their own envelope)
    @Test @Ignore
    public void twoOpenProposals () throws IOException {
        Dataset input = loadDataset( inputFolder + "2proposal-noaccepted.trig");
        Dataset expectedOutput = loadDataset( expectedOutputFolder + "2proposal-noaccepted.trig");
        test(input,expectedOutput);
    }
    
       
/*    private static boolean passesTest(Dataset input, Dataset expectedOutput) {
    	ProposalFunction proposalFunction = new ProposalFunction();
        Dataset actual = proposalFunction.applyProposalFunction(input);
        return RdfUtils.isIsomorphicWith(expectedOutput, actual);
    }
    
  */
    
    public void test(Dataset input, Dataset expectedOutput) {

        // check that the computed dataset is the expected one
        Dataset actual =  HighlevelFunctionFactory.getProposalFunction().apply(input);
        //TODO: remove before checking in
        RdfUtils.Pair<Dataset> diff = RdfUtils.diff(expectedOutput, actual);
        if (!(diff.getFirst().isEmpty() && diff.getSecond().isEmpty())) {
            System.out.println("diff - only in expected:");
            RDFDataMgr.write(System.out, diff.getFirst(), Lang.TRIG);
            System.out.println("diff - only in actual:");
            RDFDataMgr.write(System.out, diff.getSecond(), Lang.TRIG);
        }
        Assert.assertTrue(RdfUtils.isIsomorphicWith(expectedOutput, actual));
    }

    /*
    private static RdfUtils.Pair<Dataset> loadDatasetPair(String filename) throws IOException {
        Dataset input = loadDataset( inputFolder + filename);
        Dataset expectedOutput = loadDataset( expectedOutputFolder + filename);
        return new RdfUtils.Pair<Dataset>(input, expectedOutput);
    }
  */  
    
    private static Dataset loadDataset(String path) throws IOException {

        InputStream is = null;
        Dataset dataset = null;
        try {
            is = ProposalTest.class.getResourceAsStream(path);
            dataset = DatasetFactory.create();
        	RDFDataMgr.read(dataset, is, RDFFormat.TRIG.getLang());
        } finally {
            if (is != null) {
                is.close();
            }
        }

        return dataset;
    }
  
    /*
    private static Dataset loadDatasetFromFileSystem(String path) throws IOException {

        InputStream is = null;
        Dataset dataset = null;
        try {
            is = new FileInputStream(new File(path));
            dataset = DatasetFactory.create();
            RDFDataMgr.read(dataset, is, RDFFormat.TRIG.getLang());
        } finally {
            if (is != null) {
                is.close();
            }
        }

        return dataset;
    }
    
    */
    
 /*   public static void main(String... args) throws Exception {
    	Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    	root.setLevel(Level.INFO);
    	//condense test cases
    	//read datasets (input and expected output)
    	//modify input, removing graphs and triples until the test breaks
    	String outputPath = "src/test/resources/won/utils/proposal/condensed/";
    	String inputPath = "src/test/resources/won/utils/proposal/input/";
    	Stream<Path> resources = Files.list(Paths.get(inputPath));
    	resources.forEach(resource ->  {
			try {
				System.out.println("trying to condense: " + resource.toAbsolutePath().toString());
				condenseTestCaseByQuery(resource, outputPath);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
    	
    } */
    
 /*   private static List<String> getClasspathEntriesByPath(String path) throws IOException {
        InputStream is = ProposalTest.class.getClassLoader().getResourceAsStream(path);

        StringBuilder sb = new StringBuilder();
        while (is.available()>0) {
            byte[] buffer = new byte[1024];
            sb.append(new String(buffer, Charset.defaultCharset()));
        }

        return Arrays
                .asList(sb.toString().split("\n"))          // Convert StringBuilder to individual lines
                .stream()                                   // Stream the list
                .filter(line -> line.trim().length()>0)     // Filter out empty lines
                .collect(Collectors.toList());              // Collect remaining lines into a List again
    } */
    
/*    private static String readCondensationQuery() {
	    InputStream is  = ProposalFunction.class.getResourceAsStream("/won/utils/proposal/query.sq");
	    StringWriter writer = new StringWriter();
	    try {
	        IOUtils.copy(is, writer, Charsets.UTF_8);
	    } catch (IOException e) {
	        throw new IllegalStateException("Could not read queryString file", e);
	    }
    	return writer.toString();
    } */
    
/*    private static void condenseTestCaseByQuery(Path resource, String outputPath) throws Exception {
    	String condensationQuery = readCondensationQuery();
    	UpdateRequest update = UpdateFactory.create(condensationQuery);
    	Dataset condensedDataset = loadDatasetFromFileSystem(resource.toFile().getAbsolutePath());
        UpdateProcessor updateProcessor = UpdateExecutionFactory.create(update,condensedDataset);
        updateProcessor.execute();
        Iterator<String> graphNames = condensedDataset.listNames();
        while(graphNames.hasNext()) {
        	Model graph = condensedDataset.getNamedModel(graphNames.next());
        	if(graph.isEmpty()) {
        		graphNames.remove();
        	}
        }
        RDFDataMgr.write(new FileOutputStream(outputPath + resource.getFileName()), condensedDataset, Lang.TRIG);
    	System.out.println("wrote condensed input file to: " + outputPath + resource.getFileName());
    } */
    
/*    private static void condenseTestCaseIteratively(String filename, String outputPath) throws Exception {
    	RdfUtils.Pair<Dataset> inputAndExpectedOutput = loadDatasetPair(filename);
    	try {
	    	if (!passesTest(inputAndExpectedOutput.getFirst(), inputAndExpectedOutput.getSecond())) {
	    		System.out.println("test does not pass, cannot condense: " + filename);
	    		return;
	    	}
    	} catch (Exception e) {
    		System.out.println("test throws an Exception, cannot condense: " + filename);
    		return;
    	}
    	Dataset condensedDataset = inputAndExpectedOutput.getFirst();
    	Dataset expectedOutput = inputAndExpectedOutput.getSecond();
    	Iterator<String> graphNamesIt = condensedDataset.listNames();
    	int deletedStatements = 0;
    	while(graphNamesIt.hasNext()) {
    		String graphName = graphNamesIt.next();
    		System.out.println("trying to remove graph: " + graphName);
        	Dataset backupDataset = RdfUtils.cloneDataset(condensedDataset);
    		condensedDataset.removeNamedModel(graphName);
    		if (!passesTest(condensedDataset, expectedOutput)) {
    			System.out.println("cannot remove graph: " + graphName + ", trying individual triples");
    			condensedDataset = backupDataset;
    			//now try to remove triples
    			Model condensedModel = condensedDataset.getNamedModel(graphName);
    			Model attepmtedStatements = ModelFactory.createDefaultModel();
    			boolean done = false;
    			while (!done) {
    				Model backupModel = RdfUtils.cloneModel(condensedModel);
    				StmtIterator it = condensedModel.listStatements();
    				done = true;
    				while(it.hasNext()) {
    					Statement stmt = it.next();
    					if (attepmtedStatements.contains(stmt)) {
    						System.out.println("attempted this before");
    						continue;
    					}
    					System.out.println("trying statement: " + stmt);
    					attepmtedStatements.add(stmt);
    					it.remove();
    					deletedStatements++;
    					done = false;
    					break;
    				}
    				
    				condensedDataset.removeNamedModel(graphName);
    				if (!condensedModel.isEmpty()) {
    					condensedDataset.addNamedModel(graphName, condensedModel);
    				}
    				if (!passesTest(condensedDataset, expectedOutput)) {
    					System.out.println("could not delete statement");
    					condensedModel = backupModel;
    					condensedDataset.replaceNamedModel(graphName, condensedModel);
    					deletedStatements--;
    				} else {
    					System.out.println("deleted a statement");
    				}
    			}
    			if (!passesTest(condensedDataset, expectedOutput)) {
					System.out.println("test does not pass after removing statements!");
					condensedDataset = backupDataset;
				} else {
					System.out.println("removed " + deletedStatements + " statements");
				}
    		} else {
    			System.out.println("removed graph: " + graphName);
    		}
    		
    		System.out.println("dataset has ");
    	}
    	RDFDataMgr.write(new FileOutputStream(Paths.get(outputPath + filename).toFile()), condensedDataset, Lang.TRIG);
    	System.out.println("wrote condensed input file to: " + outputPath + filename);
    }
    */
}
