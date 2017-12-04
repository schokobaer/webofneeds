package won.utils.shacl;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.junit.Assert;
import org.junit.Test;
import org.topbraid.shacl.util.ModelPrinter;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;
import won.utils.TestTemplate;
import won.utils.goals.GraphBlending;

import java.io.IOException;
import java.io.InputStream;

public class ShaclTest extends TestTemplate {

    @Test
    public void validateP1DataWithP1Shape() {

        Resource report = ValidationUtil.validateModel(p1DataModel, p1ShapeModel, false);
        System.out.println(ModelPrinter.get().print(report.getModel()));

        ShaclReportWrapper reportWrapper = new ShaclReportWrapper(report);
        Assert.assertFalse(reportWrapper.isConform());
        Assert.assertEquals(3, reportWrapper.getValidationResults().size());

        for (ValidationResultWrapper result : reportWrapper.getValidationResults()) {
            Assert.assertEquals(SH.Violation, result.getResultSeverity());
            Assert.assertEquals("ride1", result.getFocusNode().getLocalName());
        }
    }

    @Test
    public void validateP2DataWithP2Shape() {

        Resource report = ValidationUtil.validateModel(p2DataModel, p2ShapeModel, false);
        System.out.println(ModelPrinter.get().print(report.getModel()));

        ShaclReportWrapper reportWrapper = new ShaclReportWrapper(report);
        Assert.assertFalse(reportWrapper.isConform());
        Assert.assertEquals(2, reportWrapper.getValidationResults().size());

        for (ValidationResultWrapper result : reportWrapper.getValidationResults()) {
            Assert.assertEquals(SH.Violation, result.getResultSeverity());
            Assert.assertEquals("myRide", result.getFocusNode().getLocalName());
            Assert.assertEquals("Less than 1 values", result.getResultMessage());
            Assert.assertEquals(SH.MinCountConstraintComponent, result.getSourceConstraintComponent());
        }
    }

    @Test
    public void validateBlendedDataWithShapes() {

        Model blended = GraphBlending.blendSimple(p1DataModel, p2DataModel, "http://example.org/blended#");

        blended.write(System.out, "TRIG");

        Resource report = ValidationUtil.validateModel(blended, p1ShapeModel, false);
        ShaclReportWrapper reportWrapper = new ShaclReportWrapper(report);

        System.out.println(ModelPrinter.get().print(report.getModel()));

        Assert.assertTrue(reportWrapper.isConform());

        report = ValidationUtil.validateModel(blended, p2ShapeModel, false);
        reportWrapper = new ShaclReportWrapper(report);
        Assert.assertTrue(reportWrapper.isConform());
    }

    private Dataset loadDataset(String path) throws IOException {

        InputStream is = null;
        Dataset dataset = null;
        try {
            is = getClass().getResourceAsStream(path);
            dataset = DatasetFactory.create();
            RDFDataMgr.read(dataset, is, RDFFormat.TRIG.getLang());
        } finally {
            if (is != null) {
                is.close();
            }
        }

        return dataset;
    }
}