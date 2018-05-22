//package uk.rgu.data.oaei;
//
//import com.hp.hpl.jena.ontology.OntClass;
//import fr.inrialpes.exmo.align.impl.ObjectAlignment;
//import fr.inrialpes.exmo.align.parser.AlignmentParser;
//import fr.inrialpes.exmo.ontowrap.OntowrapException;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Properties;
//import org.apache.commons.lang3.StringUtils;
//import org.semanticweb.owl.align.Alignment;
//import org.semanticweb.owl.align.AlignmentException;
//import org.semanticweb.owl.align.AlignmentProcess;
//import org.semanticweb.owl.align.Cell;
//import uk.rgu.data.model.AlignedConcept;
//import uk.rgu.data.model.PreAlignedConcept;
//import uk.rgu.data.ontologyprocessor.Relation;
//import uk.rgu.data.ontologyprocessor.word2vec.VectorOps;
//import uk.rgu.data.ontologyprocessor.word2vec.align.Evaluator;
//
///**
// *
// * @author 1113938
// */
//public class WordEmbContext extends ObjectAlignment implements AlignmentProcess {
//
//  static String vectorModelPath = "/program-data/DGXWord2Vec/data/model/wikipedia_plain_model300_min10_iter5_custom_token.txt";
////  static String vectorModelPath = "data/geo_hascontext1_model.txt";
//  // Get vectors
//  static VectorOps vectorOps = new VectorOps(vectorModelPath);
////  List<PreAlignedConcept> preAlignedConcepts = new ArrayList<PreAlignedConcept>(); // list to contain minimum threshold and above (to avoid multiple processing)
//
//  public static void main2(String[] args) {
//
//    String s1 = "co-author";
//    s1 = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(s1), " ");
//    System.out.println(s1);
//    s1 = s1.replaceAll("\\s-\\s", "-");
//    System.out.println(s1);
//    System.out.println(vectorOps.sentenceSimilarity(s1, "author"));
//  }
//
//  @Override
//  public void align(Alignment a, Properties prprts) throws AlignmentException {
//    try {
//      // Match classes
//      for (Object cl2 : ontology2().getClasses()) {
//        for (Object cl1 : ontology1().getClasses()) {
//          // add mapping into alignment object
//          addAlignCell(cl1, cl2, "=", match(cl1, cl2));
//        }
//      }
//      // Match dataProperties
////      for (Object p2 : ontology2().getDataProperties()) {
////        for (Object p1 : ontology1().getDataProperties()) {
////          // add mapping into alignment object
////          addAlignCell(p1, p2, "=", match(p1, p2));
////        }
////      }
//      // Match objectProperties
////      for (Object p2 : ontology2().getObjectProperties()) {
////        for (Object p1 : ontology1().getObjectProperties()) {
////          // add mapping into alignment object
////          addAlignCell(p1, p2, "=", match(p1, p2));
////        }
////      }
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }
//
//  public double match(Object o1, Object o2) throws AlignmentException {
////    double t = .69; // vector similarity threshold for alignment
//    try {
//      String s1 = ontology1().getEntityName(o1);
//      String s2 = ontology2().getEntityName(o2);
////      System.out.println(s1 + " vs " + s2);
//
//      if (s1 == null || s2 == null) {
//        System.out.println("a string is null");
//        return 0.;
//      } else {
//        // convert from camelCase to human-readable
//        s1 = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(s1), ' ');
//        s2 = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(s2), ' ');
//
//        s1 = s1.replaceAll("\\s-\\s", "-"); // e.g. reverts "co - author" to "co-author"
//        s2 = s2.replaceAll("\\s-\\s", "-");
//
//        // prepare for lookup in word embedding vocabulary
//        s1 = VectorOps.prepareStringSpaces(s1);
//        s2 = VectorOps.prepareStringSpaces(s2);
//        double sim = vectorOps.sentenceSimilarity(s1, s2);
////        if (sim > 0.8) {
////          System.out.println(s1 + " VS " + s2 + " = " + sim);
////        }
//        return sim;
//      }
//    } catch (OntowrapException owex) {
//      throw new AlignmentException("Error getting entity name", owex);
//    }
//  }
//
//  public static void main(String[] args) {
//    Collection evaluationResults = new ArrayList<String>();
//
//    // Get vectors
//    VectorOps vectorOps = new VectorOps();
//
//    String alignmentScheme = "EUROVOC_GEMET"; // also shows alignment order for concepts
//    String scheme1 = "EUROVOC";
//    String scheme2 = "GEMET";
//    List<String> relationTypes = new ArrayList();
//    relationTypes.add(Relation.Predicate.EXACT_MATCH.value);
//    relationTypes.add(Relation.Predicate.CLOSE_MATCH.value); // non-transitively equivalent concepts
//
//    List<AlignedConcept> groundTruth = Evaluator.getGroundTruth(alignmentScheme, relationTypes);
//    List<AlignedConcept> selectedGroundTruth = Evaluator.getGroundTruthWithVectors(alignmentScheme, relationTypes, vectorOps);
//
//    // Retrieve concepts from both ontologies
//    LinkedConceptService linkedConceptService = new LinkedConceptService(new RDFManager());
//    List<ConceptContext> conceptContexts1 = linkedConceptService.getConceptsAndContext(scheme1, 1); // get scheme 2: gemet
//    List<ConceptContext> conceptContexts2 = linkedConceptService.getConceptsAndContext(scheme2, 1); // get scheme 2: gemet
//    List<Concept> concepts1 = linkedConceptService.getAllConcept(scheme1); // get scheme 1: eurovoc
//    List<Concept> concepts2 = linkedConceptService.getAllConcept(scheme2); // get scheme 2: gemet
//    System.out.println("Size of " + scheme1 + " => " + concepts1.size());
//    System.out.println("Size of " + scheme2 + " => " + concepts2.size());
//    concepts1 = Evaluator.conceptsWithVectors(concepts1, vectorOps);
//    concepts2 = Evaluator.conceptsWithVectors(concepts2, vectorOps);
//
//    System.out.println("Concepts without vectors removed. " + scheme1 + " => " + concepts1.size());
//    System.out.println("Concepts without vectors removed. " + scheme2 + " => " + concepts2.size());
//
//    vectorOps = new VectorOps(vectorModelPath); // change word embedding model
//
//    List<PreAlignedConcept2> preAlignedConcepts = new ArrayList<PreAlignedConcept2>(); // list to contain minimum threshold and above (to avoid multiple processing)
//
//    for (Concept c1 : concepts1) {
//      for (Concept c2 : concepts2) {
//        if (c1.getConceptId().split("-").length > 1 && c2.getConceptId().split("-").length > 1) { // test for valid concepts (should have a concept id)
//          double vecSim = vectorOps.maxSimilarity(c1.getAllLabels(), c2.getAllLabels()); // maximum cosine similarity
//          // check if similarity is up to the threshold by string similarity or vector similarity
//          if (vecSim >= minSimilarity) { // check alignment
//            double maxContextSemantic = vectorOps.maxSimilarity(getContextTerms(conceptContexts1, c1), getContextTerms(conceptContexts2, c2));
//            AlignedConcept ac = new AlignedConcept(Concept.getFullConceptUri(c1.getConceptId()), Concept.getFullConceptUri(c2.getConceptId()), Relation.Predicate.CLOSE_MATCH.value);
//            preAlignedConcepts.add(new PreAlignedConcept2(ac, vecSim, maxContextSemantic)); // keep similarity and maximum context similarity
//          }
//        } // end if (test of valid concepts)
//
//      } // concept2 loop ends
//    } // concept1 loop ends
//
//    for (double vt = minSimilarity; vt <= maxSimilarity; vt += 0.1) { // try different thresholds
//      for (double ct = 0.08; ct <= 0.12; ct += 0.02) {
//        System.out.println("PROCESSING: concept similarity threshold=" + vt + ",context similarity threshold=" + ct);
//        List<AlignedConcept> recommendedAlignments;
//        List<PreAlignedConcept> alignmentsInThreshold = new ArrayList<PreAlignedConcept>();
//        for (PreAlignedConcept2 pac : preAlignedConcepts) {
//          if (pac.similarity_1 >= vt && pac.similarity_2 >= ct) {
//            PreAlignedConcept p = new PreAlignedConcept(pac.alignedConcept, pac.similarity_1);
//            alignmentsInThreshold = PreAlignedConcept.updateAlignments(alignmentsInThreshold, p);
//          }
//        }
//
//        recommendedAlignments = PreAlignedConcept.getAlignments(alignmentsInThreshold); // get alingments (removes confidence/similarity values)
//        // write out results of using these thresholds
//        System.out.println("vector similarity threshold=" + vt + ",context similarity threshold=" + ct + ",Alignments found=" + recommendedAlignments.size());
//        String result = Evaluator.evaluate(groundTruth, selectedGroundTruth, recommendedAlignments);
//        System.out.println(result);
//        evaluationResults.add("vector similarity threshold=" + vt + ",context similarity threshold=" + ct + ",Alignments found=" + recommendedAlignments.size());
//        evaluationResults.add(result);
//        System.out.println();
//      }
//    }
//
//    // print results
//    evaluationResults.forEach(System.out::println);
//  }
//
//  public static void dummyMatch() throws AlignmentException {
//    double minSimilarity = 0.7;
//    double maxSimilarity = 0.9;
//    Collection evaluationResults = new ArrayList<String>();
//
//    List<AlignTrainTest> allTestcaseData = Alignment_oaei.generateAlignTrainTest();
//
//    // test alignment performance at different thresholds
//    for (double ht = minSimilarity; ht <= maxSimilarity; ht += 0.01) { // try different thresholds
//      List<Integer> found = new ArrayList();
//      List<Integer> correct = new ArrayList();
//      List<Integer> expected = new ArrayList();
//
//      for (AlignTrainTest alignTrainTest : allTestcaseData) {
//        List<OntClass> concepts1 = OntoOps.getOntoClasses(alignTrainTest.sourceOnto.getAbsolutePath());
//        List<OntClass> concepts2 = OntoOps.getOntoClasses(alignTrainTest.targetOnto.getAbsolutePath());
//        // ground truth
//        AlignmentParser aparser = new AlignmentParser(0);
//        Alignment reference = aparser.parse(alignTrainTest.referenceAlignment.toPath().toUri());
//        List<AlignedConcept> groundTruth = new ArrayList();
//        for (Iterator<Cell> iterator = reference.iterator(); iterator.hasNext();) {
//          Cell cell = iterator.next();
//          groundTruth.add(new AlignedConcept(cell.getObject1AsURI().toString(), cell.getObject2AsURI().toString(), Relation.Predicate.EXACT_MATCH.value));
//        }
//
//        List<PreAlignedConcept> preAlignedConcepts = new ArrayList<PreAlignedConcept>(); // list to contain minimum threshold and above (to avoid multiple processing)
//
//        for (OntClass c1 : concepts1) {
//          for (OntClass c2 : concepts2) {
////          double stringSim = StringOps.maxSimilarityByNormalizedLevenshtein(c1.getAllLabels(), c2.getAllLabels()); // maximum string similarity
//            double hybridSim = vectorOps.maxHybridSimilarity(OntoOps.getLabels(c1), OntoOps.getLabels(c2)); // maximum cosine similarity
//            // check if similarity is up to the threshold by string similarity or vector similarity
////          if (stringSim >= minSimilarity || vecSim >= minSimilarity) { // check alignment
//            if (hybridSim >= minSimilarity) { // check alignment
//              AlignedConcept ac = new AlignedConcept(c1.getURI(), c2.getURI(), Relation.Predicate.CLOSE_MATCH.value);
//              preAlignedConcepts.add(new PreAlignedConcept(ac, hybridSim));
////              preAlignedConcepts = PreAlignedConcept.updateAlignments(preAlignedConcepts, new PreAlignedConcept(ac, hybridSim));
//            }
//
//          } // concept2 loop ends
//        } // concept1 loop ends
//
//        System.out.println("PROCESSING: concept similarity threshold=" + ht);
//        List<AlignedConcept> recommendedAlignments;
//        List<PreAlignedConcept> alignmentsInThreshold = new ArrayList<PreAlignedConcept>();
//        for (PreAlignedConcept pac : preAlignedConcepts) {
//          if (pac.confidence >= ht) {
//            PreAlignedConcept p = new PreAlignedConcept(pac.alignedConcept, pac.confidence);
////          alignmentsInThreshold.add(p);
//            alignmentsInThreshold = PreAlignedConcept.updateAlignments(alignmentsInThreshold, p);
//          }
//        }
//
//        recommendedAlignments = PreAlignedConcept.getAlignments(alignmentsInThreshold); // get alingments (removes confidence/similarity values)
//
//        found.add(recommendedAlignments.size());
//        correct.add(Evaluator.getCorrect(groundTruth, recommendedAlignments));
//        expected.add(alignTrainTest.expectedClassCount);
//
////System.out.println("Ground truth size => " + groundTruth.size());
////System.out.println("Expected size => " + alignTrainTest.expectedClassCount);
////if (groundTruth.size() != alignTrainTest.expectedClassCount) System.exit(1);
//        // write out results of using these thresholds
////        System.out.println("hybrid similarity threshold=" + ht + ",Alignments found=" + recommendedAlignments.size());
////        String result = Evaluator.evaluate(groundTruth, recommendedAlignments);
////        System.out.println(result);
////        evaluationResults.add("hybrid similarity threshold=" + ht + ",Alignments found=" + recommendedAlignments.size());
////        evaluationResults.add(result);
////        System.out.println();
//      }
//
//      double precision = HarmonicPR.hPrecision(correct, found);
//      double recall = HarmonicPR.hRecall(correct, expected);
//      double f1 = 2 * precision * recall / (precision + recall);
//
//      evaluationResults.add("Threshold = " + ht + " | H(p) = " + precision + ", H(r) = " + recall + ", H(fm) = " + f1 + " | Alignments expected " + HarmonicPR.sum(expected) + ", found " + HarmonicPR.sum(found));
//
//    }
//
//    // print results
//    evaluationResults.forEach(System.out::println);
//  }
//
//}
