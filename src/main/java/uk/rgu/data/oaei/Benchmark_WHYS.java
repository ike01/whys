package uk.rgu.data.oaei;

import com.hp.hpl.jena.ontology.OntClass;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import uk.rgu.data.model.AlignedConcept;
import uk.rgu.data.model.Concept;
import uk.rgu.data.model.RecommendedConcept;
import uk.rgu.data.utilities.Relation;
import uk.rgu.data.ontologyprocessor.word2vec.VectorOps;
import uk.rgu.data.utilities.Evaluator;
import uk.rgu.data.utilities.HarmonicPR;
import uk.rgu.data.utilities.TFIDF;

/**
 *
 * @author aikay
 */
public class Benchmark_WHYS {

//  static String vectorModelPath = "C:/dev/rgu/word2vec/models/GoogleNews-vectors-negative300.bin.gz";
  static String vectorModelPath = "/program-data/DGXWord2Vec/data/model/wikipedia_plain_model300_min10_iter5_custom_token.txt";
//  static String vectorModelPath = "/program-data/DGXWord2Vec/data/model/wikipedia_plain_model300_min5_iter20_custom_token.txt";
//  static String vectorModelPath = "C:/dev/rgu/word2vec/models/geo_hascontext1_model.txt";
  // Get vectors
  VectorOps vectorOps;// = new VectorOps(vectorModelPath);
  static DecimalFormat df = new DecimalFormat("#.####");

  public Benchmark_WHYS(VectorOps vectorOps) {
    this.vectorOps = vectorOps;
  }

  /**
   * Weighted hybrid similarity
   *
   * @param threshold
   * @param maxN
   * @param editDistCut
   * @return
   * @throws AlignmentException
   */
  public String weightedHybridSimilarity(double threshold, int maxN, double editDistCut) throws AlignmentException { // AlignedConcept alignedConcept,
    List<AlignedConcept> alignments = new ArrayList<>();
    Collection out = new ArrayList<String>(); // for csv output
    List<AlignTrainTest> allTestcaseData = Alignment_oaei.generateBencAlignTrainTest();
    List<AlignedConcept> seenAlignmentList = new ArrayList<>(); // overall list of alignments returned
//    String ontoBase = "data/2016_benchmark/";
//    File sourceOnto = new File(ontoBase + "101_onto.rdf");

//    Collection results = new ArrayList<>();
    List<Integer> found = new ArrayList();
    List<Integer> correct = new ArrayList();
    List<Integer> expected = new ArrayList();

//    for (double cut = 7.0; cut <= 9.0; cut += 0.1) {
//      t = cut;
    System.out.println("current : " + threshold);

//      for (int counter = 1; counter <= 4; counter++) {
    for (AlignTrainTest alignTrainTest : allTestcaseData) {
      int total = 0;
      int correctFound = 0;
//        File targetOnto = new File(ontoBase + "30" + counter + "_onto.rdf");
//        File referenceAlignment = new File(ontoBase + "reference-alignment/30" + counter + "_refalign.rdf");

      String sourceScheme = FilenameUtils.removeExtension(alignTrainTest.sourceOnto.getName()).split("_")[0];
      String targetScheme = FilenameUtils.removeExtension(alignTrainTest.targetOnto.getName()).split("_")[0];
      List<OntClass> concepts1 = OntoOps.getOntoClasses(alignTrainTest.sourceOnto.getAbsolutePath());
      List<OntClass> concepts2 = OntoOps.getOntoClasses(alignTrainTest.targetOnto.getAbsolutePath());
//        concepts1.forEach(c -> System.out.println(OntoOps.getLabels(c)));
//        if (t < 7.1) {
//          concepts2.forEach(c -> System.out.println(OntoOps.getLabels(c)));
//        }
      List<List<String>> collection1 = TFIDF.getCollection(concepts1);
      List<List<String>> collection2 = TFIDF.getCollection(concepts2);

      // collection similarity
//      double collectionSim = compareCollection(collection1, collection2);
//        System.out.println("Concept size for " + sourceScheme + " => " + concepts1.size());
//        System.out.println("Concept size for " + targetScheme + " => " + concepts2.size());
//      System.out.println("Similarity: " + collectionSim);
//      int minVocab = concepts1.size() < concepts2.size() ? concepts1.size() : concepts2.size();
//      t = findThreshold(collectionSim, similarityValues, minVocab);
//      System.out.println("Threshold: " + t);
      // ground truth
      AlignmentParser aparser = new AlignmentParser(0);
      Alignment reference = aparser.parse(alignTrainTest.referenceAlignment.toPath().toUri());
      List<AlignedConcept> groundTruth = new ArrayList();
      for (Iterator<Cell> iterator = reference.iterator(); iterator.hasNext();) {
        Cell cell = iterator.next();
        groundTruth.add(new AlignedConcept(cell.getObject1AsURI().toString(), cell.getObject2AsURI().toString(), Relation.Predicate.EXACT_MATCH.value));
      }

      Map<Concept, ArrayList<RecommendedConcept>> candidateAlignments = new HashMap<Concept, ArrayList<RecommendedConcept>>(); // keeps candidate alignments
      // select concepts wit similarity above chosen threshold as candidate alignment concepts
      for (OntClass ontClass1 : concepts1) {
        ArrayList<RecommendedConcept> similarConcepts = new ArrayList<RecommendedConcept>(); // Similar concepts above a threshold
        for (OntClass ontClass2 : concepts2) {
          if (ontClass1 != null && ontClass2 != null) { // test for valid concepts (should have a concept id)
            double sim = vectorOps.maxWeightedHybridSimilarity(OntoOps.getLabels(ontClass1), collection1, OntoOps.getLabels(ontClass2), collection2, editDistCut);
//            sim = Math.max(sim, contextSemantic);
            // check if similarity is up to the threshold by string similarity or vector similarity
            if (sim >= threshold) { // check alignment (low score to preserve concepts below threshold for offsets computation)
              Concept c2 = new Concept(ontClass2.getURI(), OntoOps.getLabel(ontClass2), targetScheme);
              similarConcepts.add(new RecommendedConcept(c2, sim, 1)); // keep similarity
            }
          } // end if (test of valid concepts)
        } // concept2 loop ends

        if (!similarConcepts.isEmpty()) { // sort and select top n +1 (see within)
          Collections.sort(similarConcepts, new RecommendedConcept.RecommendedConceptComparator()); // sort in descending order of score
          int N = maxN < similarConcepts.size() ? maxN : similarConcepts.size(); // top 1
          similarConcepts = new ArrayList<>(similarConcepts.subList(0, N));
          Concept c1 = new Concept(ontClass1.getURI(), OntoOps.getLabel(ontClass1), sourceScheme);
          candidateAlignments.put(c1, similarConcepts);
        }
      } // concept1 loop ends

      // Evaluate
      for (Map.Entry<Concept, ArrayList<RecommendedConcept>> entry : candidateAlignments.entrySet()) {
        Concept c1 = entry.getKey();
        ArrayList<RecommendedConcept> selectedConcepts = entry.getValue();
        for (int i = 0; i < selectedConcepts.size(); i++) {
          RecommendedConcept c2 = selectedConcepts.get(i);
          AlignedConcept alignedConcept = new AlignedConcept(c1.getId(), c2.getId(), Relation.Predicate.EXACT_MATCH.value);
          if (c2.getScore() >= threshold && !AlignedConcept.containsTheAlignment(seenAlignmentList, alignedConcept)) { // continue if similarity is up to threshold and alignment is not selected already
            seenAlignmentList.add(alignedConcept); // add new to list
alignments.add(new AlignedConcept(c1.getId(), c2.getId(), Relation.Predicate.EXACT_MATCH.value));
            String classLabel = "N";
            total++;
            if (Evaluator.trueAlignment(groundTruth, alignedConcept)) { // check if present in the reference alignment
              correctFound++;
              classLabel = "Y";
            }
            out.add(c1.getLabel() + " vs " + c2.getLabel() + " = " + c2.getScore() + " => " + classLabel);
          } // end if unseen alignment and above similarity threshold
        }
      }
      found.add(total);
      correct.add(correctFound);
      expected.add(alignTrainTest.expectedClassCount);
    }
    // summary
    double precision = HarmonicPR.hPrecision(correct, found);
    double recall = HarmonicPR.hRecall(correct, expected);
    double f1 = 2 * precision * recall / (precision + recall);

    System.out.println();
    System.out.println("H(p) = " + precision + " H(r) = " + recall + " H(fm) = " + f1 + " , threshold = " + threshold + " , edit_cut = " + editDistCut);

    return precision + "," + recall + "," + f1 + "," + threshold + "," + editDistCut;
//    return alignments;
  }

  public static void main(String[] args) {
    VectorOps vectorOps = new VectorOps(vectorModelPath);
    Benchmark_WHYS whys = new Benchmark_WHYS(vectorOps);
    try {
//      generateFeatures(0.5, 1);

      Collection results = new ArrayList<>();
      results.add("precision,recall,f-measure,threshold,edit_dist_cut");
      for (double threshold = 0.7; threshold <= 1.; threshold+=0.01) {
        for (double editDistCut = 0.7; editDistCut <= 1.0; editDistCut+=0.01) {
          results.add(whys.weightedHybridSimilarity(threshold, 1, editDistCut));
        }
      }
      System.out.println("");
      results.forEach(System.out::println);
//      weightedHybridSimilarity((t + 0.05), 1, 0.8);
    } catch (AlignmentException ex) {
      Logger.getLogger(Benchmark_WHYS.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
