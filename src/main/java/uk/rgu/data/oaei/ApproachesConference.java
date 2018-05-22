package uk.rgu.data.oaei;

import uk.rgu.data.utilities.HarmonicPR;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Evaluator;
import uk.rgu.data.model.AlignedConcept;
import uk.rgu.data.ontologyprocessor.word2vec.VectorOps;
import uk.rgu.data.utilities.Relation;

/**
 *
 * @author 1113938
 */
public class ApproachesConference {
//  static String vectorModelPath = "C:/dev/rgu/word2vec/models/GoogleNews-vectors-negative300.bin.gz";
  static String vectorModelPath = "/program-data/DGXWord2Vec/data/model/wikipedia_plain_model300_min10_iter5_custom_token.txt";

  public static void main(String[] arg) {
    VectorOps vectorOps = new VectorOps(vectorModelPath);
    try {
//      AlignedConcept.overlap(isub(), hybrid(vectorOps));
      wordEmb(vectorOps);
//      isub();
//      stringEquiv();
    } catch (AlignmentException ex) {
      Logger.getLogger(ApproachesConference.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public static List<AlignedConcept> stringEquiv() throws AlignmentException { // string equality applied on local names of entities which were lowercased
    List<AlignedConcept> alignments = new ArrayList<>();
    System.out.println("\n===StringEquiv===");
    List<AlignTrainTest> allTestcaseData = Alignment_oaei.generateConfAlignTrainTest();
    List<Integer> found = new ArrayList();
    List<Integer> correct = new ArrayList();
    List<Integer> expected = new ArrayList();

    Properties params = new BasicParameters();

    for (AlignTrainTest alignTrainTest : allTestcaseData) {
      URI onto1 = alignTrainTest.sourceOnto.toPath().toUri();
      URI onto2 = alignTrainTest.targetOnto.toPath().toUri();

      AlignmentProcess a1 = new StringEquiv();
      a1.init(onto1, onto2);
      a1.align((Alignment) null, params);
      a1.cut(1.0);
a1.forEach(a -> {
  alignments.add(new AlignedConcept(a.getObject1().toString().replaceAll("<", "").replaceAll(">", ""), a.getObject2().toString().replaceAll("<", "").replaceAll(">", ""), Relation.Predicate.EXACT_MATCH.value));
});
      // Load the reference alignment
      AlignmentParser aparser = new AlignmentParser(0);
      Alignment reference = aparser.parse(alignTrainTest.referenceAlignment.toPath().toUri());

      // Evaluate alignment
      Evaluator evaluator = new PRecEvaluator(reference, a1);
      evaluator.eval(new Properties());

      // Print results
      System.out.println(alignTrainTest);
      double p = ((PRecEvaluator) evaluator).getPrecision();
      double r = ((PRecEvaluator) evaluator).getRecall();

      found.add(a1.nbCells());
      correct.add(((PRecEvaluator) evaluator).getCorrect());
//      expected.add(((PRecEvaluator) evaluator).getExpected());
      expected.add(alignTrainTest.expectedClassCount);

      System.err.println("Precision: " + p + " Recall: " + r + " F-measure: " + ((PRecEvaluator) evaluator).getFmeasure() + " over " + a1.nbCells() + " cells");
    }

    System.out.println(found);
    System.out.println(correct);
    System.out.println(expected);

    double precision = HarmonicPR.hPrecision(correct, found);
    double recall = HarmonicPR.hRecall(correct, expected);
    double f1 = 2 * precision * recall / (precision + recall);

    System.out.println();
    System.out.println("H(p) = " + precision + " H(r) = " + recall + " H(fm) = " + f1);
    return alignments;
  }

  public static List<AlignedConcept> edna() throws AlignmentException { // string editing distance matcher
    List<AlignedConcept> alignments = new ArrayList<>();
    System.out.println("\n===edna===");
    List<AlignTrainTest> allTestcaseData = Alignment_oaei.generateConfAlignTrainTest();

    double t = 0.88;

//    for (double t = start; t < .95; t += 0.01) {
    List<Integer> found = new ArrayList();
    List<Integer> correct = new ArrayList();
    List<Integer> expected = new ArrayList();

    Properties params = new BasicParameters();

    for (AlignTrainTest alignTrainTest : allTestcaseData) {
      URI onto1 = alignTrainTest.sourceOnto.toPath().toUri();
      URI onto2 = alignTrainTest.targetOnto.toPath().toUri();

      AlignmentProcess a1 = new EditDist();
//        AlignmentProcess a1 = new StringDistAlignment();
      a1.init(onto1, onto2);
//      params = new Properties();
//        params.setProperty("stringFunction", "levenshteinDistance");
      a1.align((Alignment) null, params);
      a1.cut(t);

      // Load the reference alignment
      AlignmentParser aparser = new AlignmentParser(0);
      Alignment reference = aparser.parse(alignTrainTest.referenceAlignment.toPath().toUri());

      // Evaluate alignment
      Evaluator evaluator = new PRecEvaluator(reference, a1);
      evaluator.eval(new Properties());
a1.forEach(a -> {
  alignments.add(new AlignedConcept(a.getObject1().toString().replaceAll("<", "").replaceAll(">", ""), a.getObject2().toString().replaceAll("<", "").replaceAll(">", ""), Relation.Predicate.EXACT_MATCH.value));
});
      // Print results
//        System.out.println(alignTrainTest);
      double p = ((PRecEvaluator) evaluator).getPrecision();
      double r = ((PRecEvaluator) evaluator).getRecall();

      found.add(a1.nbCells());
      correct.add(((PRecEvaluator) evaluator).getCorrect());
//      expected.add(((PRecEvaluator) evaluator).getExpected());
      expected.add(alignTrainTest.expectedClassCount);

//      System.err.println("Precision: " + p + " Recall: " + r + " F-measure: " + ((PRecEvaluator) evaluator).getFmeasure() + " over " + a1.nbCells() + " cells");
    }

//    System.out.println(found);
//    System.out.println(correct);
//    System.out.println(expected);
    double precision = HarmonicPR.hPrecision(correct, found);
    double recall = HarmonicPR.hRecall(correct, expected);
    double f1 = 2 * precision * recall / (precision + recall);

//      System.out.println();
    System.out.println("Threshold = " + t + " H(p) = " + precision + " H(r) = " + recall + " H(fm) = " + f1);
//    }
    return alignments;
  }

  public static List<AlignedConcept> isub() throws AlignmentException { // string editing distance matcher
    List<AlignedConcept> alignments = new ArrayList<>();
    System.out.println("\n===stoilos===");
    List<AlignTrainTest> allTestcaseData = Alignment_oaei.generateConfAlignTrainTest();

    double t = 0.96;

//    for (double t = start; t <= 1.; t += 0.01) {
    List<Integer> found = new ArrayList();
    List<Integer> correct = new ArrayList();
    List<Integer> expected = new ArrayList();

    Properties params = new BasicParameters();

    for (AlignTrainTest alignTrainTest : allTestcaseData) {
      URI onto1 = alignTrainTest.sourceOnto.toPath().toUri();
      URI onto2 = alignTrainTest.targetOnto.toPath().toUri();

      AlignmentProcess a1 = new Stoilos();
//        AlignmentProcess a1 = new StringDistAlignment();
      a1.init(onto1, onto2);
      params = new Properties();
//        params.setProperty("stringFunction", "levenshteinDistance");
      a1.align((Alignment) null, params);
      a1.cut(t);
a1.forEach(a -> {
  alignments.add(new AlignedConcept(a.getObject1().toString().replaceAll("<", "").replaceAll(">", ""), a.getObject2().toString().replaceAll("<", "").replaceAll(">", ""), Relation.Predicate.EXACT_MATCH.value));
});
      // Load the reference alignment
      AlignmentParser aparser = new AlignmentParser(0);
      Alignment reference = aparser.parse(alignTrainTest.referenceAlignment.toPath().toUri());

      // Evaluate alignment
      Evaluator evaluator = new PRecEvaluator(reference, a1);
      evaluator.eval(new Properties());

      // Print results
//        System.out.println(alignTrainTest);
//      double p = ((PRecEvaluator) evaluator).getPrecision();
//      double r = ((PRecEvaluator) evaluator).getRecall();

      found.add(a1.nbCells());
      correct.add(((PRecEvaluator) evaluator).getCorrect());
//      expected.add(((PRecEvaluator) evaluator).getExpected());
      expected.add(alignTrainTest.expectedClassCount);

//      System.err.println("Precision: " + p + " Recall: " + r + " F-measure: " + ((PRecEvaluator) evaluator).getFmeasure() + " over " + a1.nbCells() + " cells");
    }

//    System.out.println(found);
//    System.out.println(correct);
//    System.out.println(expected);
    double precision = HarmonicPR.hPrecision(correct, found);
    double recall = HarmonicPR.hRecall(correct, expected);
    double f1 = 2 * precision * recall / (precision + recall);

//      System.out.println();
    System.out.println("Threshold = " + t + " H(p) = " + precision + " H(r) = " + recall + " H(fm) = " + f1);
//    }
    return alignments;
  }

  public static List<AlignedConcept> wordNet() throws AlignmentException {
    List<AlignedConcept> alignments = new ArrayList<>();
    System.out.println("\n===WordNet===");
    List<AlignTrainTest> allTestcaseData = Alignment_oaei.generateConfAlignTrainTest();

    double start = .8;

    for (double t = start; t <= 1.0; t += 0.01) {
      Properties params = new BasicParameters();
      List<Integer> found = new ArrayList();
      List<Integer> correct = new ArrayList();
      List<Integer> expected = new ArrayList();

      for (AlignTrainTest alignTrainTest : allTestcaseData) {
        URI onto1 = alignTrainTest.sourceOnto.toPath().toUri();
        URI onto2 = alignTrainTest.targetOnto.toPath().toUri();

        AlignmentProcess a1 = new WordNetWP();
        a1.init(onto1, onto2);
//        params = new Properties();
//        params.setProperty("threshold", Double.toString(t));
        a1.align((Alignment) null, params);
        a1.cut(t);
//a1.forEach(a -> {
//  alignments.add(new AlignedConcept(a.getObject1().toString().replaceAll("<", "").replaceAll(">", ""), a.getObject2().toString().replaceAll("<", "").replaceAll(">", ""), Relation.Predicate.EXACT_MATCH.value));
//});
        // Load the reference alignment
        AlignmentParser aparser = new AlignmentParser(0);
        Alignment reference = aparser.parse(alignTrainTest.referenceAlignment.toPath().toUri());

        // Evaluate alignment
        Evaluator evaluator = new PRecEvaluator(reference, a1);
        evaluator.eval(new Properties());

        // Print results
//        System.out.println(alignTrainTest);
        double p = ((PRecEvaluator) evaluator).getPrecision();
        double r = ((PRecEvaluator) evaluator).getRecall();

        found.add(a1.nbCells());
        correct.add(((PRecEvaluator) evaluator).getCorrect());
//        expected.add(((PRecEvaluator) evaluator).getExpected());
        expected.add(alignTrainTest.expectedClassCount);

//      System.err.println("Precision: " + p + " Recall: " + r + " F-measure: " + ((PRecEvaluator) evaluator).getFmeasure() + " over " + a1.nbCells() + " cells");
      }

//    System.out.println(found);
//    System.out.println(correct);
//    System.out.println(expected);
      double precision = HarmonicPR.hPrecision(correct, found);
      double recall = HarmonicPR.hRecall(correct, expected);
      double f1 = 2 * precision * recall / (precision + recall);

      System.out.println("Threshold = " + t + " H(p) = " + precision + " H(r) = " + recall + " H(fm) = " + f1);
    }

    return alignments;
  }

  public static List<AlignedConcept> wordEmb(VectorOps vectorOps) throws AlignmentException {
    List<AlignedConcept> alignments = new ArrayList<>();
    System.out.println("\n===WordEmb===");
    List<AlignTrainTest> allTestcaseData = Alignment_oaei.generateConfAlignTrainTest();

    double t = .85;

//    for (double t = start; t < 1.0; t += 0.01) {
      Properties params = new BasicParameters();
      List<Integer> found = new ArrayList();
      List<Integer> correct = new ArrayList();
      List<Integer> expected = new ArrayList();

      for (AlignTrainTest alignTrainTest : allTestcaseData) {
        URI onto1 = alignTrainTest.sourceOnto.toPath().toUri();
        URI onto2 = alignTrainTest.targetOnto.toPath().toUri();

        AlignmentProcess a1 = new WordEmb(vectorOps);
        a1.init(onto1, onto2);
//        params = new Properties();
//        params.setProperty("threshold", Double.toString(t));
        a1.align((Alignment) null, params);
        a1.cut(t);
a1.forEach(a -> {
  alignments.add(new AlignedConcept(a.getObject1().toString().replaceAll("<", "").replaceAll(">", ""), a.getObject2().toString().replaceAll("<", "").replaceAll(">", ""), Relation.Predicate.EXACT_MATCH.value));
});
        // Load the reference alignment
        AlignmentParser aparser = new AlignmentParser(0);
        Alignment reference = aparser.parse(alignTrainTest.referenceAlignment.toPath().toUri());

        // Evaluate alignment
        Evaluator evaluator = new PRecEvaluator(reference, a1);
        evaluator.eval(new Properties());

        // Print results
//        System.out.println(alignTrainTest);
        double p = ((PRecEvaluator) evaluator).getPrecision();
        double r = ((PRecEvaluator) evaluator).getRecall();

        found.add(a1.nbCells());
        correct.add(((PRecEvaluator) evaluator).getCorrect());
//        expected.add(((PRecEvaluator) evaluator).getExpected());
        expected.add(alignTrainTest.expectedClassCount);

//      System.err.println("Precision: " + p + " Recall: " + r + " F-measure: " + ((PRecEvaluator) evaluator).getFmeasure() + " over " + a1.nbCells() + " cells");
      }

//    System.out.println(found);
//    System.out.println(correct);
//    System.out.println(expected);
      double precision = HarmonicPR.hPrecision(correct, found);
      double recall = HarmonicPR.hRecall(correct, expected);
      double f1 = 2 * precision * recall / (precision + recall);

      System.out.println("Threshold = " + t + " H(p) = " + precision + " H(r) = " + recall + " H(fm) = " + f1);
//    }

    return alignments;
  }

  public static void domainWordEmb() throws AlignmentException {
    System.out.println("\n===DomainWordEmb===");
    List<AlignTrainTest> allTestcaseData = Alignment_oaei.generateConfAlignTrainTest();

    double start = .85;

    for (double t = start; t < 1.0; t += 0.01) {
      Properties params = new BasicParameters();
      List<Integer> found = new ArrayList();
      List<Integer> correct = new ArrayList();
      List<Integer> expected = new ArrayList();

      for (AlignTrainTest alignTrainTest : allTestcaseData) {
        String modelPath = "data/model/" + FilenameUtils.removeExtension(alignTrainTest.sourceOnto.getName()) + "-" + FilenameUtils.removeExtension(alignTrainTest.targetOnto.getName()) + "_model.txt";
        URI onto1 = alignTrainTest.sourceOnto.toPath().toUri();
        URI onto2 = alignTrainTest.targetOnto.toPath().toUri();

        AlignmentProcess a1 = new DomainWordEmb();
        a1.init(onto1, onto2);
        params = new Properties();
        params.setProperty("word2vec", modelPath); // path to word2vec model
        a1.align((Alignment) null, params);
        a1.cut(t);

        // Load the reference alignment
        AlignmentParser aparser = new AlignmentParser(0);
        Alignment reference = aparser.parse(alignTrainTest.referenceAlignment.toPath().toUri());

        // Evaluate alignment
        Evaluator evaluator = new PRecEvaluator(reference, a1);
        evaluator.eval(new Properties());

        // Print results
//        System.out.println(alignTrainTest);
        double p = ((PRecEvaluator) evaluator).getPrecision();
        double r = ((PRecEvaluator) evaluator).getRecall();

        found.add(a1.nbCells());
        correct.add(((PRecEvaluator) evaluator).getCorrect());
//        expected.add(((PRecEvaluator) evaluator).getExpected());
        expected.add(alignTrainTest.expectedClassCount);

//      System.err.println("Precision: " + p + " Recall: " + r + " F-measure: " + ((PRecEvaluator) evaluator).getFmeasure() + " over " + a1.nbCells() + " cells");
      }

//    System.out.println(found);
//    System.out.println(correct);
//    System.out.println(expected);
      double precision = HarmonicPR.hPrecision(correct, found);
      double recall = HarmonicPR.hRecall(correct, expected);
      double f1 = 2 * precision * recall / (precision + recall);

      System.out.println("Threshold = " + t + " H(p) = " + precision + " H(r) = " + recall + " H(fm) = " + f1);
    }
  }

  public static List<AlignedConcept> hybrid(VectorOps vectorOps) throws AlignmentException {
    List<AlignedConcept> alignments = new ArrayList<>();
    System.out.println("\n===Hybrid===");
    List<AlignTrainTest> allTestcaseData = Alignment_oaei.generateConfAlignTrainTest();

    double t = .85;

//    for (double t = start; t < 1.0; t += 0.01) {
      List<Integer> found = new ArrayList();
      List<Integer> correct = new ArrayList();
      List<Integer> expected = new ArrayList();

      Properties params = new BasicParameters();

      for (AlignTrainTest alignTrainTest : allTestcaseData) {
        URI onto1 = alignTrainTest.sourceOnto.toPath().toUri();
        URI onto2 = alignTrainTest.targetOnto.toPath().toUri();

        AlignmentProcess a1 = new Hybrid(vectorOps);
        a1.init(onto1, onto2);
//        params = new Properties();
        a1.align((Alignment) null, params);
        a1.cut(t);
a1.forEach(a -> {
  alignments.add(new AlignedConcept(a.getObject1().toString().replaceAll("<", "").replaceAll(">", ""), a.getObject2().toString().replaceAll("<", "").replaceAll(">", ""), Relation.Predicate.EXACT_MATCH.value));
});
        // Load the reference alignment
        AlignmentParser aparser = new AlignmentParser(0);
        Alignment reference = aparser.parse(alignTrainTest.referenceAlignment.toPath().toUri());

        // Evaluate alignment
        Evaluator evaluator = new PRecEvaluator(reference, a1);
        evaluator.eval(new Properties());

        // Print results
//        System.out.println(alignTrainTest);
        double p = ((PRecEvaluator) evaluator).getPrecision();
        double r = ((PRecEvaluator) evaluator).getRecall();

        found.add(a1.nbCells());
        correct.add(((PRecEvaluator) evaluator).getCorrect());
//        expected.add(((PRecEvaluator) evaluator).getExpected());
        expected.add(alignTrainTest.expectedClassCount);

//        System.err.println("Precision: " + p + " Recall: " + r + " F-measure: " + ((PRecEvaluator) evaluator).getFmeasure() + " over " + a1.nbCells() + " cells");
      }

//    System.out.println(found);
//    System.out.println(correct);
//    System.out.println(expected);
      double precision = HarmonicPR.hPrecision(correct, found);
      double recall = HarmonicPR.hRecall(correct, expected);
      double f1 = 2 * precision * recall / (precision + recall);

      System.out.println("Threshold = " + t + " | H(p) = " + precision + ", H(r) = " + recall + ", H(fm) = " + f1 + " | Alignments expected " + HarmonicPR.sum(expected) + ", found " + HarmonicPR.sum(found));
//    }
    return alignments;
  }

}
