package uk.rgu.data.oaei;

import uk.rgu.data.utilities.HarmonicPR;
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
import uk.rgu.data.utilities.TFIDF;
import uk.rgu.data.ontologyprocessor.word2vec.VectorOps;
import uk.rgu.data.utilities.Evaluator;

/**
 *
 * @author 1113938
 */
public class Conference_WHYS {

  static String vectorModelPath = "C:/dev/rgu/word2vec/models/GoogleNews-vectors-negative300.bin.gz";
//  static String vectorModelPath = "/program-data/DGXWord2Vec/data/model/wikipedia_plain_model300_min10_iter5_custom_token.txt";
//  static String vectorModelPath = "/program-data/DGXWord2Vec/data/model/wikipedia_plain_model300_min5_iter20_custom_token.txt";
//  static String vectorModelPath = "C:/dev/rgu/word2vec/models/geo_hascontext1_model.txt";
  // Get vectors
  VectorOps vectorOps;// = new VectorOps(vectorModelPath);
  static DecimalFormat df = new DecimalFormat("#.####");

  public Conference_WHYS(VectorOps vectorOps) {
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
    List<AlignTrainTest> allTestcaseData = Alignment_oaei.generateConfAlignTrainTest();
    List<AlignedConcept> seenAlignmentList = new ArrayList<>(); // overall list of alignments returned

    List<Integer> found = new ArrayList();
    List<Integer> correct = new ArrayList();
    List<Integer> expected = new ArrayList();

    System.out.println("current : " + threshold);

    for (AlignTrainTest alignTrainTest : allTestcaseData) {
      int total = 0;
      int correctFound = 0;

      String sourceScheme = FilenameUtils.removeExtension(alignTrainTest.referenceAlignment.getName()).split("-")[0];
      String targetScheme = FilenameUtils.removeExtension(alignTrainTest.referenceAlignment.getName()).split("-")[1];

      List<OntClass> concepts1 = OntoOps.getOntoClasses(alignTrainTest.sourceOnto.getAbsolutePath());
      List<OntClass> concepts2 = OntoOps.getOntoClasses(alignTrainTest.targetOnto.getAbsolutePath());

      List<List<String>> collection1 = TFIDF.getCollection(concepts1);
      List<List<String>> collection2 = TFIDF.getCollection(concepts2);

      // collection similarity
//      double collectionSim = compareCollection(collection1, collection2);

//      System.out.println("Concept size for " + sourceScheme + " => " + concepts1.size());
//      System.out.println("Concept size for " + targetScheme + " => " + concepts2.size());

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
      int conceptCount = 0;
      for (Map.Entry<Concept, ArrayList<RecommendedConcept>> entry : candidateAlignments.entrySet()) {
        Concept c1 = entry.getKey();
        System.out.println(++conceptCount + ". Generating features for " + c1);
        ArrayList<RecommendedConcept> selectedConcepts = entry.getValue();
        System.out.println("Count of concepts above threshold " + selectedConcepts.size());

        for (int i = 0; i < selectedConcepts.size(); i++) {
          RecommendedConcept c2 = selectedConcepts.get(i);
          AlignedConcept alignedConcept = new AlignedConcept(c1.getId(), c2.getId(), Relation.Predicate.EXACT_MATCH.value);
          if (c2.getScore() >= threshold && !AlignedConcept.containsTheAlignment(seenAlignmentList, alignedConcept)) { // continue if similarity is up to threshold and alignment is not selected already
            seenAlignmentList.add(alignedConcept); // add new to list

//            Set<String> c1ParentContextTerms = getContextTerms(conceptParents1, c1.getId());
//            Set<String> c2ParentContextTerms = getContextTerms(conceptParents2, c2.getId());
//            double parentContextSemanticOverlap = StringOps.maxSimilarityByStoilos(VectorOps.prepareStringSpaces(c1ParentContextTerms), VectorOps.prepareStringSpaces(c2ParentContextTerms));
//            if (c1ParentContextTerms.isEmpty() || c2ParentContextTerms.isEmpty()) {
//              parentContextSemanticOverlap = -1;
//            }
//            Set<String> c1ChildrenContextTerms = getContextTerms(conceptChildren1, c1.getId());
//            Set<String> c2ChildrenContextTerms = getContextTerms(conceptChildren2, c2.getId());
//            double childrenContextSemanticOverlap = StringOps.maxSimilarityByStoilos(VectorOps.prepareStringSpaces(c1ChildrenContextTerms), VectorOps.prepareStringSpaces(c2ChildrenContextTerms));
//            if (c1ChildrenContextTerms.isEmpty() || c2ChildrenContextTerms.isEmpty()) {
//              childrenContextSemanticOverlap = -1;
//            }
//            double contextSemantic = (parentContextSemanticOverlap + childrenContextSemanticOverlap) / 2;

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
//    out.forEach(System.out::println);
//    System.out.println("Correct => " + correct);
//    System.out.println("Total => " + total);

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
    Conference_WHYS whys = new Conference_WHYS(vectorOps);
    try {
//      dummyMatch();
//      generateFeatures(0.5, 1);
//      weightedHybridSimilarity(0.75, 1, 0.8);
      System.out.println(whys.weightedHybridSimilarity(0.8, 1, 0.96));
//      AlignedConcept.overlap(ApproachesConference.hybrid(vectorOps), hybrid.weightedHybridSimilarity(0.76, 1, 0.89));
//      Collection results = new ArrayList<>();
//      results.add("precision,recall,f-measure,threshold,edit_dist_cut");
//      for (double threshold = 0.75; threshold <= 0.77; threshold+=0.01) {
//        for (double editDistCut = 0.95; editDistCut <= 0.96; editDistCut+=0.01) {
//          results.add(hybrid.weightedHybridSimilarity(threshold, 1, editDistCut));
//        }
//      }
//      System.out.println("");
//      results.forEach(System.out::println);
    } catch (AlignmentException ex) {
      Logger.getLogger(Conference_WHYS.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}
