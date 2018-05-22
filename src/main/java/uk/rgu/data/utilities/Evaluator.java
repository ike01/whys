package uk.rgu.data.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import uk.rgu.data.model.AlignedConcept;
import uk.rgu.data.model.Concept;
import uk.rgu.data.ontologyprocessor.OntologyModel;
import uk.rgu.data.ontologyprocessor.RDFManager;
import uk.rgu.data.ontologyprocessor.word2vec.VectorOps;

/**
 *
 * @author 1113938
 */
public class Evaluator {

  private static final RDFManager rdfStore = new RDFManager();
//  static String vectorModelPath = "/program-data/DGXWord2Vec/data/model/wikipedia_plain_model300_min10_iter5_custom_token.txt";

  /**
   * Retrieves reference alignments of specified relation type from an alignment
   * scheme.
   *
   * @param alignmentScheme
   * @param relationTypes
   * @return
   */
  public static List<AlignedConcept> getGroundTruth(String alignmentScheme, List<String> relationTypes) {
    List<AlignedConcept> alignments = new ArrayList();
    for (String relationType : relationTypes) {
      alignments.addAll(rdfStore.getAlignments(alignmentScheme, relationType));
    }
    // Get alignments
    return alignments;
  }

  /**
   * Retrieves reference alignments of specified relation type whose corresponding
   * concept terms have vectors.
   *
   * @param alignmentScheme
   * @param relationTypes
   * @param vectorOps
   * @return
   */
  public static List<AlignedConcept> getGroundTruthWithVectors(String alignmentScheme, List<String> relationTypes, VectorOps vectorOps) {
    List<AlignedConcept> refAlignmentsWithVectors = new ArrayList();
    // Get alignments
    List<AlignedConcept> refAlignments = getGroundTruth(alignmentScheme, relationTypes);
    // Select alignments with concept term vectors
    for (AlignedConcept ac : refAlignments) {
      Concept c1 = rdfStore.getConceptUsingFullConceptId(OntologyModel.getGraph(alignmentScheme.split("_")[0]), ac.concept_1);
      Concept c2 = rdfStore.getConceptUsingFullConceptId(OntologyModel.getGraph(alignmentScheme.split("_")[1]), ac.concept_2);

      if (vectorOps.hasVector(c1.getAllLabels()) && vectorOps.hasVector(c2.getAllLabels())) { // True if at least one concept terms has vectors
        refAlignmentsWithVectors.add(ac);
      }
    }

    return refAlignmentsWithVectors;
  }


  /**
   * Calculates precision: proportion of recommended alignments that are correct.
   *
   * @param groundTruth
   * @param recommendedAlignments
   * @return
   */
  public static double getPrecision(List<AlignedConcept> groundTruth, List<AlignedConcept> recommendedAlignments) {
    // Test that there is ground truth
    if (groundTruth.isEmpty()) {
      System.out.println("NO GROUNDTRUTH FOR EVALUATION!");
      return 0.0;
    }

    int correctCount = 0;
    // count recommended alignments that are contained in the ground truth
    for (AlignedConcept align : groundTruth) {
      if (recommendedAlignments.contains(align)) {
        correctCount++;
      }
    }

    return (double) correctCount / recommendedAlignments.size();
  }

  /**
   * Calculates recall: proportion of alignments that were discovered.
   *
   * @param groundTruth
   * @param recommendedAlignments
   * @return
   */
  public static double getRecall(List<AlignedConcept> groundTruth, List<AlignedConcept> recommendedAlignments) {
    // Test that there is ground truth
    if (groundTruth.isEmpty()) {
      System.out.println("NO GROUNDTRUTH FOR EVALUATION!");
      return 0.0;
    }

    int correctCount = 0;
    // count recommended alignments that are contained in the ground truth
    for (AlignedConcept align : groundTruth) {
      if (recommendedAlignments.contains(align)) {
        correctCount++;
      }
    }

    return (double) correctCount / groundTruth.size();
  }

  public static int getCorrect(List<AlignedConcept> groundTruth, List<AlignedConcept> recommendedAlignments) {
    int count = 0;
    // count recommended alignments that are contained in the ground truth
    for (AlignedConcept align : recommendedAlignments) {
      if (AlignedConcept.containsTheAlignment(groundTruth, align)) {
        count++;
      }
    }

    return count;
  }

  /**
   * Precision, recall and f1-measure of recommended alignments.
   *
   * @param alignmentScheme
   * @param relationTypes
   * @param recommendedAlignments
   * @return
   */
  public static String evaluate(String alignmentScheme, List<String> relationTypes, List<AlignedConcept> recommendedAlignments) {
    // Get ground truth
    List<AlignedConcept> groundTruth = getGroundTruth(alignmentScheme, relationTypes);
    // Test that there is ground truth
    if (groundTruth.isEmpty()) {
      System.out.println("NO GROUNDTRUTH FOR EVALUATION!");
      return null;
    }
    // count correct alignments in recommended list
    int correctCount = 0;
    for (AlignedConcept align : groundTruth) {
      if (recommendedAlignments.contains(align)) {
        correctCount++;
      }
    }

    double precision = (double) correctCount / recommendedAlignments.size();
    double recall = (double) correctCount / groundTruth.size();
    double f1 = 2 * precision * recall / (precision + recall);

    return "p=" + precision + ",r=" + recall + ",f1=" + f1;
  }

  /**
   * Precision, recall and f1-measure of recommended alignments.
   *
   * @param alignmentScheme
   * @param relationTypes
   * @param recommendedAlignments
   * @return
   */
  public static String evaluate(String alignmentScheme, List<String> relationTypes, Set<AlignedConcept> recommendedAlignments) {
    // Get ground truth
    List<AlignedConcept> groundTruth = getGroundTruth(alignmentScheme, relationTypes);
    // Test that there is ground truth
    if (groundTruth.isEmpty()) {
      System.out.println("NO GROUNDTRUTH FOR EVALUATION!");
      return null;
    }
    // count correct alignments in recommended list
    int correctCount = 0;
    for (AlignedConcept align : groundTruth) {
      if (recommendedAlignments.contains(align)) {
        correctCount++;
      }
    }

    double precision = (double) correctCount / recommendedAlignments.size();
    double recall = (double) correctCount / groundTruth.size();
    double f1 = 2 * precision * recall / (precision + recall);

    return "p=" + precision + ",r=" + recall + ",f1=" + f1;
  }

  /**
   * Precision, recall and f1-measure of recommended alignments.
   *
   * @param groundTruth
   * @param recommendedAlignments
   * @return
   */
  public static String evaluate(List<AlignedConcept> groundTruth, List<AlignedConcept> recommendedAlignments) {
    // Test that there is ground truth
    if (groundTruth.isEmpty()) {
      System.out.println("NO GROUNDTRUTH FOR EVALUATION!");
      return null;
    }
    // count correct alignments in recommended list
    int correctCount = 0;
    for (AlignedConcept align : groundTruth) {
      if (recommendedAlignments.contains(align)) {
        correctCount++;
      }
    }

    double precision = (double) correctCount / recommendedAlignments.size();
    double recall = (double) correctCount / groundTruth.size();
    double f1 = 2 * precision * recall / (precision + recall);

    return "p=" + precision + ",r=" + recall + ",f1=" + f1;
  }

  /**
   * Precision, recall and f1-measure of recommended alignments.
   *
   * @param groundTruth
   * @param recommendedAlignments
   * @return
   */
  public static String evaluate(List<AlignedConcept> groundTruth, Set<AlignedConcept> recommendedAlignments) {
    // Test that there is ground truth
    if (groundTruth.isEmpty()) {
      System.out.println("NO GROUNDTRUTH FOR EVALUATION!");
      return null;
    }
    // count correct alignments in recommended list
    int correctCount = 0;
    for (AlignedConcept align : groundTruth) {
      if (recommendedAlignments.contains(align)) {
        correctCount++;
      }
    }

    double precision = (double) correctCount / recommendedAlignments.size();
    double recall = (double) correctCount / groundTruth.size();
    double f1 = 2 * precision * recall / (precision + recall);

    return "p=" + precision + ",r=" + recall + ",f1=" + f1;
  }

  /**
   * Precision, recall and f1-measure of recommended alignments.
   *
   * @param groundTruth
   * @param selectedGroundTruth
   * @param recommendedAlignments
   * @return
   */
  public static String evaluate(List<AlignedConcept> groundTruth, List<AlignedConcept> selectedGroundTruth, List<AlignedConcept> recommendedAlignments) {
    // Test that there is ground truth
    if (groundTruth.isEmpty()) {
      System.out.println("NO GROUNDTRUTH FOR EVALUATION!");
      return null;
    }
    // remove correct alignments that are not part of selected ground truth
    List<AlignedConcept> found = new ArrayList();
    for (AlignedConcept ac : recommendedAlignments) {
      if (!(groundTruth.contains(ac) && !selectedGroundTruth.contains(ac))) {
        found.add(ac);
      }
    }

    // count correct alignments in recommended list
    int correctCount = 0;
    for (AlignedConcept align : selectedGroundTruth) {
      if (found.contains(align)) {
        correctCount++;
      }
    }

    double precision = (double) correctCount / found.size();
    double recall = (double) correctCount / selectedGroundTruth.size();
    double f1 = 2 * precision * recall / (precision + recall);

    return "p=" + precision + ",r=" + recall + ",f1=" + f1;
  }

  /**
   * Check if an alignment exists in the ground truth.
   *
   * @param groundTruth
   * @param recommendedAlignment
   * @return
   */
  public static boolean trueAlignment(List<AlignedConcept> groundTruth, AlignedConcept recommendedAlignment) {
    return groundTruth.contains(recommendedAlignment);
  }

  public static List<Concept> conceptsWithVectors(List<Concept> conceptList, VectorOps vectorOps) {
    List<Concept> concepts = new ArrayList();
    for (Concept c : conceptList) {
      if (vectorOps.hasVector(c.getAllLabels())) {
        concepts.add(c);
      }
    }

    return concepts;
  }

}
