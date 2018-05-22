package uk.rgu.data.ontologyprocessor.word2vec;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Set;
import uk.rgu.data.ontologyprocessor.RDFManager;
import uk.rgu.data.utilities.StringOps;

/**
 *
 * @author aikay
 */
public class UseConceptVectors {

  private static final RDFManager rdfStore = new RDFManager();
  static DecimalFormat df = new DecimalFormat("#.0000");
  static String vectorModelPath = "/program-data/DGXWord2Vec/data/model/wikipedia_annotated_model300_min10_iter5_custom_token.txt";

  public static void main(String[] args) {
//    String scheme = "GEMET";
//    VectorOps vectorOps = new VectorOps("model/geo_hascontext2_model.txt");
//    // Concept access service
//    LinkedConceptService linkedConceptService = new LinkedConceptService(new RDFManager());
//    List<Concept> concepts = linkedConceptService.getAllConcept(scheme);
//    System.out.println("Concept size => " + concepts.size());
//    for (Concept concept : concepts) {
//
//    }

//    VectorOps vectorOps = new VectorOps("model/geo_wiki_model.txt");
//    vectorOps.nearestWords("hes", 10);
//    vectorOps.nearestWords("im", 10);
//    vectorOps.nearestWords("theyll", 10);
//    alignments();
//    System.out.println(VectorOps.prepareString("child's_rights (gemet)"));
//    alignments("EUROVOC_GEMET", "EUROVOC", "GEMET");
//Set<String> cnpt2Lbl = rdfStore.getConceptUsingFullConceptId(Ontology.getGraph("GEMET"), "http://www.eionet.europa.eu/gemet/concept/4030").getAllLabels();
//cnpt2Lbl.forEach(System.out::println);
  }

  /**
   * Compares strings for exact match (case insensitive). Additional
   * pre-processing is done to string when vectorLabel is set to true.
   *
   * @param list1
   * @param list2
   * @param vectorLabel
   * @return
   */
  public static boolean checkStringMatch(Set<String> list1, Set<String> list2, boolean vectorLabel) {
    boolean match = false;
    for (String s1 : list1) {
      for (String s2 : list2) {
        if (vectorLabel) {
          s1 = VectorOps.prepareStringUnderscores(s1);
          s2 = VectorOps.prepareStringUnderscores(s2);
        }
        if (s1.equalsIgnoreCase(s2)) {
          return true;
        }
      }
    }

    return match;
  }

  /**
   * Compares strings for exact match (case insensitive) after parentheses,
   * dashes and underscores have been removed. Further pre-processing is done to
   * strings if vectorLabel is set to true.
   *
   * @param list1
   * @param list2
   * @param vectorLabel
   * @return
   */
  public static boolean checkStringMatch2(Set<String> list1, Set<String> list2, boolean vectorLabel) {
    boolean match = false;
    for (String s1 : list1) {
      s1 = s1.replaceAll("-", " "); // remove dashes
      s1 = s1.replaceAll("_", " "); // remove underscores
      s1 = StringOps.stripAllParentheses(s1); // remove parentheses
      for (String s2 : list2) {
        s2 = s2.replaceAll("-", " "); // remove dashes
        s2 = s2.replaceAll("_", " "); // remove underscores
        s2 = StringOps.stripAllParentheses(s2); // remove parentheses
        if (vectorLabel) {
          s1 = VectorOps.prepareStringUnderscores(s1);
          s2 = VectorOps.prepareStringUnderscores(s2);
        }
        if (s1.equalsIgnoreCase(s2)) {
          return true;
        }
      }
    }

    return match;
  }

  public static boolean checkMatchHaveVectors(VectorOps vectorOps, Set<String> list1, Set<String> list2) {
    boolean hasVec = false;
    for (String s1 : list1) {
      for (String s2 : list2) {
        if (s1.equalsIgnoreCase(s2)) {
          return vectorOps.hasVector(VectorOps.prepareStringUnderscores(s1));
        }
      }
    }

    return hasVec;
  }

  private static int matchRankPosition(Map<String, Double> sortedMap, Set<String> alignedConceptLabels) {
    int count = 0;
    int rank = 0;
    double lastSim = 10.0; // possible max (can accommodate up to ~10-15 alternative labels for a concept)
    for (Map.Entry<String, Double> entry : sortedMap.entrySet()) {
      count++;
      String ecl = entry.getKey().trim();
      double sim = entry.getValue();
      if (sim < lastSim) { // allows for ties by having same rank when sim does not change
        rank = count;
        lastSim = sim;
      }

      for (String acl : alignedConceptLabels) {
        System.out.println("acl : " + acl);
        System.out.println("entry : " + ecl);
        acl = VectorOps.prepareStringUnderscores(acl);
        ecl = VectorOps.prepareStringUnderscores(ecl);
        if (acl.equalsIgnoreCase(ecl)) {
          return rank;
        }
      }
    }

    return -1; // concept label is not in map
  }

}
