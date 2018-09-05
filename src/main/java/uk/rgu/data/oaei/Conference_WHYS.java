package uk.rgu.data.oaei;

import uk.rgu.data.utilities.HarmonicPR;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import uk.rgu.data.model.AlignedConcept;
import uk.rgu.data.model.Concept;
import uk.rgu.data.model.ConceptContext;
import uk.rgu.data.model.PreAlignedConcept;
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

//  static String vectorModelPath = "C:/dev/rgu/word2vec/models/GoogleNews-vectors-negative300.bin.gz";
  static String vectorModelPath = "/program-data/DGXWord2Vec/data/model/wikipedia_plain_model300_min10_iter5_custom_token.txt";
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
    List<AlignmentProp> alignmentProp = new ArrayList();
    List<AlignmentPlus> alignmentsPlus = new ArrayList<>();
    int runCounter = 0;
    double alpha = 1.0;
    double beta = 0.5;
    double gamma = 0.3;
    double delta = 0.2;
    List<AlignedConcept> alignments = new ArrayList<>();
    Collection out = new ArrayList<String>(); // for csv output
    List<AlignTrainTest> allTestcaseData = Alignment_oaei.generateConfAlignTrainTest();
    List<AlignedConcept> seenAlignmentList = new ArrayList<>(); // overall list of alignments returned

    // counts for evaluation
    List<Integer> found = new ArrayList();
    List<Integer> correct = new ArrayList();
    List<Integer> expected = new ArrayList();
//    List<AlignedConcept> allGroundTruths = new ArrayList();

    System.out.println("current : " + threshold);

    for (AlignTrainTest alignTrainTest : allTestcaseData) {
      List<PreAlignedConcept> alignmentsInThreshold = new ArrayList<PreAlignedConcept>();
      runCounter++;
      int total = 0;
      int correctFound = 0;

      String sourceScheme = FilenameUtils.removeExtension(alignTrainTest.referenceAlignment.getName()).split("-")[0];
      String targetScheme = FilenameUtils.removeExtension(alignTrainTest.referenceAlignment.getName()).split("-")[1];

      List<OntClass> concepts1 = OntoOps.getOntoClasses(alignTrainTest.sourceOnto.getAbsolutePath());
      List<OntClass> concepts2 = OntoOps.getOntoClasses(alignTrainTest.targetOnto.getAbsolutePath());

      List<List<String>> collection1 = TFIDF.getCollection(concepts1, true); // source ontology collection
//      collection1.addAll(TFIDF.getCollection(concepts2, true));
      List<List<String>> collection2 = TFIDF.getCollection(concepts2, true); // target ontology collection
//      List<List<String>> collection2 = new ArrayList<>(collection1);

//      List<List<String>> propColl = TFIDF.getPropertiesCollection(concepts1, true); // source ontology concept properties
//      propColl.addAll(TFIDF.getPropertiesCollection(concepts2, true)); // add target ontology concept properties
//
//      OntModel sourceModel = OntoOps.getOntologyModel(alignTrainTest.sourceOnto.getAbsolutePath());
//      OntModel targetModel = OntoOps.getOntologyModel(alignTrainTest.targetOnto.getAbsolutePath());
//
//      // Parent context : labels of parents of all concepts in scheme
//      List<ConceptContext> conceptParents1 = getConceptsAndContextParents(sourceModel, concepts1, sourceScheme); // source
//      List<ConceptContext> conceptParents2 = getConceptsAndContextParents(targetModel, concepts2, targetScheme); // target
//
//      // Children context : labels of children of all concepts in scheme
//      List<ConceptContext> conceptChildren1 = getConceptsAndContextChildren(sourceModel, concepts1, sourceScheme); // source
//      List<ConceptContext> conceptChildren2 = getConceptsAndContextChildren(targetModel, concepts2, targetScheme); // target
//
//      // Sibling context : labels of siblings (other children of parents) of all concepts in scheme
//      List<ConceptContext> conceptSiblings1 = getConceptsAndContextSibling(sourceModel, concepts1, sourceScheme); // source
//      List<ConceptContext> conceptSiblings2 = getConceptsAndContextSibling(targetModel, concepts2, targetScheme); // target

//      alignmentProp.add(new AlignmentProp(sourceModel, targetModel, conceptParents1, conceptParents2, conceptChildren1, conceptChildren2, conceptSiblings1, conceptSiblings2));

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
        AlignedConcept a = new AlignedConcept(cell.getObject1AsURI().toString(), cell.getObject2AsURI().toString(), Relation.Predicate.EXACT_MATCH.value);
        groundTruth.add(a);
//        Concept c1 = new Concept(cell.getObject1AsURI().toString(), OntoOps.getLabel(sourceModel, cell.getObject1AsURI().toString()), sourceScheme);
//        Concept c2 = new Concept(cell.getObject2AsURI().toString(), OntoOps.getLabel(targetModel, cell.getObject2AsURI().toString()), targetScheme);
//        alignmentsPlus.add(new AlignmentPlus(a, runCounter, c1, c2));
      }
//      allGroundTruths.addAll(groundTruth);
      Map<Concept, ArrayList<RecommendedConcept>> candidateAlignments = new HashMap<Concept, ArrayList<RecommendedConcept>>(); // keeps candidate alignments
      // select concepts wit similarity above chosen threshold as candidate alignment concepts
      for (OntClass ontClass1 : concepts1) {
        ArrayList<RecommendedConcept> similarConcepts = new ArrayList<RecommendedConcept>(); // Similar concepts above a threshold
        for (OntClass ontClass2 : concepts2) {
          if (ontClass1 != null && ontClass2 != null) { // test for valid concepts (should have a concept id)
            Set<String> c1Terms = OntoOps.getLabels(ontClass1);
            Set<String> c2Terms = OntoOps.getLabels(ontClass2);
            double equiv_sim = vectorOps.maxWeightedHybridSimilarity(c1Terms, collection1, c2Terms, collection2, editDistCut);
            double sim = equiv_sim;

////            // parents
//            Set<String> c1ParentContextTerms = getContextTerms(conceptParents1, ontClass1.getURI());
//            Set<String> c2ParentContextTerms = getContextTerms(conceptParents2, ontClass2.getURI());
//            if (c1ParentContextTerms.isEmpty()) c1ParentContextTerms = c1Terms;
//            if (c2ParentContextTerms.isEmpty()) c2ParentContextTerms = c2Terms;
//            double parents_sim = vectorOps.maxWeightedHybridSimilarity(c1ParentContextTerms, collection1, c2ParentContextTerms, collection2, editDistCut);
////            double parents_sim = TFIDF.cosine_similarity(TFIDF.weighStringTerms(c1ParentContextTerms, collection1), TFIDF.weighStringTerms(c2ParentContextTerms, collection2));
//
//            // children
//            Set<String> c1ChildrenContextTerms = getContextTerms(conceptChildren1, ontClass1.getURI());
//            Set<String> c2ChildrenContextTerms = getContextTerms(conceptChildren2, ontClass2.getURI());
//            if (c1ChildrenContextTerms.isEmpty()) c1ChildrenContextTerms = c1Terms;
//            if (c2ChildrenContextTerms.isEmpty()) c2ChildrenContextTerms = c2Terms;
//            double children_sim = vectorOps.maxWeightedHybridSimilarity(c1ChildrenContextTerms, collection1, c2ChildrenContextTerms, collection2, editDistCut);
////            double children_sim = TFIDF.cosine_similarity(TFIDF.weighStringTerms(c1ChildrenContextTerms, collection1), TFIDF.weighStringTerms(c2ChildrenContextTerms, collection2));
//
//            // siblings
//            Set<String> c1SiblingsContextTerms = getContextTerms(conceptSiblings1, ontClass1.getURI());
//            Set<String> c2SiblingsContextTerms = getContextTerms(conceptSiblings2, ontClass2.getURI());
//            if (c1SiblingsContextTerms.isEmpty()) c1SiblingsContextTerms = c1Terms;
//            if (c2SiblingsContextTerms.isEmpty()) c2SiblingsContextTerms = c2Terms;
//            double siblings_sim = vectorOps.maxWeightedHybridSimilarity(c1SiblingsContextTerms, collection1, c2SiblingsContextTerms, collection2, editDistCut);
////            double siblings_sim = TFIDF.cosine_similarity(TFIDF.weighStringTerms(c1SiblingsContextTerms, collection1), TFIDF.weighStringTerms(c2SiblingsContextTerms, collection2));
//
//            // properties
//            double prop_sim = TFIDF.cosine_similarity(TFIDF.weighStringTerms(OntoOps.getProperties(ontClass1), propColl), TFIDF.weighStringTerms(OntoOps.getProperties(ontClass2), propColl));
////
//            double extra_evidence = Math.max(prop_sim, Math.max(siblings_sim, Math.max(parents_sim, children_sim)));

//            double sim = (alpha * equiv_sim) + (beta * parents_sim) + (gamma * children_sim) + (delta * siblings_sim);

//            double sim = Math.max(equiv_sim, ((parents_sim + children_sim + siblings_sim) / 3));
//            double sim = (parents_sim + children_sim + siblings_sim) / 3;
            // check if similarity is up to the threshold by string similarity or vector similarity
            if (sim >= threshold) { // check alignment (low score to preserve concepts below threshold for offsets computation)
              Concept c2 = new Concept(ontClass2.getURI(), OntoOps.getLabel(ontClass2), targetScheme);
              RecommendedConcept rc2 = new RecommendedConcept(c2, sim, 1);
//              rc2.contextSim = extra_evidence;
              similarConcepts.add(rc2); // keep similarity

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

      // Filter
      for (Map.Entry<Concept, ArrayList<RecommendedConcept>> entry : candidateAlignments.entrySet()) {
        Concept c1 = entry.getKey();
        ArrayList<RecommendedConcept> selectedConcepts = entry.getValue();

        for (int i = 0; i < selectedConcepts.size(); i++) {
          RecommendedConcept c2 = selectedConcepts.get(i);
          AlignedConcept alignedConcept = new AlignedConcept(c1.getId(), c2.getId(), Relation.Predicate.EXACT_MATCH.value);
//          PreAlignedConcept pac = new PreAlignedConcept(alignedConcept, c2.getScore());
//          if (c2.getScore() >= threshold && !AlignedConcept.containsTheAlignment(seenAlignmentList, alignedConcept)) { // continue if similarity is up to threshold and alignment is not selected already
//            if ((c2.getScore() >= 0.76) ||
//                    (c2.getScore() >= threshold && c2.contextSim >= 0.89)) { // continue if similarity is up to threshold and alignment is not selected already
//            seenAlignmentList.add(alignedConcept); // add new to list
            PreAlignedConcept pac = new PreAlignedConcept(alignedConcept, c2.getScore());
            alignmentsInThreshold = PreAlignedConcept.updateAlignments(alignmentsInThreshold, pac);
//            alignmentsInThreshold.add(pac);

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
//            }
        }
      }
// Evaluate
alignments = PreAlignedConcept.getAlignments(alignmentsInThreshold);
//  alignments.add(new AlignedConcept(c1.getId(), c2.getId(), Relation.Predicate.EXACT_MATCH.value));
          for (AlignedConcept alignedConcept : alignments) {
            String classLabel = "N";
            total++;
            if (Evaluator.trueAlignment(groundTruth, alignedConcept)) { // check if present in the reference alignment
              correctFound++;
              classLabel = "Y";
//              System.out.println("============================================");
//              System.out.println(c1.getId() + " vs " + c2.getId() + " \t " + c2.getScore());
//              System.out.println("c1:");
//              System.out.print("labels: ");
//              c1.getAllLabels().forEach(l -> { System.out.print(l + ", "); });
//              System.out.print("\nparents: ");
//              getContextTerms(conceptParents1, c1.getId()).forEach(p -> { System.out.print(p + ", "); });
//              System.out.print("\nchildren: ");
//              getContextTerms(conceptChildren1, c1.getId()).forEach(c -> { System.out.print(c + ", "); });
//              System.out.print("\nsiblings: ");
//              getContextTerms(conceptSiblings1, c1.getId()).forEach(s -> { System.out.print(s + ", "); });
//              System.out.print("\nproperties: ");
//              OntoOps.getProperties(sourceModel, c1.getId()).forEach(pty -> { System.out.print(pty + ", "); });
//
//              System.out.println("\n-------");
//              System.out.println("c2:");
//              System.out.print("labels: ");
//              c2.getAllLabels().forEach(l -> { System.out.print(l + " "); });
//              System.out.print("\nparents: ");
//              getContextTerms(conceptParents2, c2.getId()).forEach(p -> { System.out.print(p + ", "); });
//              System.out.print("\nchildren: ");
//              getContextTerms(conceptChildren2, c2.getId()).forEach(c -> { System.out.print(c + ", "); });
//              System.out.print("\nsiblings: ");
//              getContextTerms(conceptSiblings2, c2.getId()).forEach(s -> { System.out.print(s + ", "); });
//              System.out.print("\nproperties: ");
//              OntoOps.getProperties(targetModel, c2.getId()).forEach(pty -> { System.out.print(pty + ", "); });
//
//              System.out.println("");
            } else {
//              System.out.println("============================================");
//              System.out.println(c1.getId() + " vs " + c2.getId() + " \t " + c2.getScore());
//              System.out.println("c1:");
//              System.out.print("labels: ");
//              c1.getAllLabels().forEach(l -> { System.out.print(l + ", "); });
//              System.out.print("\nparents: ");
//              getContextTerms(conceptParents1, c1.getId()).forEach(p -> { System.out.print(p + ", "); });
//              System.out.print("\nchildren: ");
//              getContextTerms(conceptChildren1, c1.getId()).forEach(c -> { System.out.print(c + ", "); });
//              System.out.print("\nsiblings: ");
//              getContextTerms(conceptSiblings1, c1.getId()).forEach(s -> { System.out.print(s + ", "); });
//              System.out.print("\nproperties: ");
//              OntoOps.getProperties(sourceModel, c1.getId()).forEach(pty -> { System.out.print(pty + ", "); });
//
//              System.out.println("\n-------");
//              System.out.println("c2:");
//              System.out.print("labels: ");
//              c2.getAllLabels().forEach(l -> { System.out.print(l + " "); });
//              System.out.print("\nparents: ");
//              getContextTerms(conceptParents2, c2.getId()).forEach(p -> { System.out.print(p + ", "); });
//              System.out.print("\nchildren: ");
//              getContextTerms(conceptChildren2, c2.getId()).forEach(c -> { System.out.print(c + ", "); });
//              System.out.print("\nsiblings: ");
//              getContextTerms(conceptSiblings2, c2.getId()).forEach(s -> { System.out.print(s + ", "); });
//              System.out.print("\nproperties: ");
//              OntoOps.getProperties(targetModel, c2.getId()).forEach(pty -> { System.out.print(pty + ", "); });
//
//              System.out.println("");

            }
//            out.add(c1.getLabel() + " vs " + c2.getLabel() + " = " + c2.getScore() + " => " + classLabel);
          } // end if unseen alignment and above similarity threshold

        found.add(total);
        correct.add(correctFound);
        expected.add(alignTrainTest.expectedClassCount);
      }

    // summary
//    out.forEach(System.out::println);
//    System.out.println("Correct => " + correct);
//    System.out.println("Total => " + total);
/*
    for (int i = 0; i < alignmentsPlus.size(); i++) {
      if (alignmentsPlus.get(i).c1.getLabel() != null && !alignments.contains(alignmentsPlus.get(i).alignedConcept)) { // undiscovered alignments
        AlignmentPlus ap = alignmentsPlus.get(i);
        AlignmentProp aprop = alignmentProp.get(ap.id - 1);
        System.out.println("\n\n============================================");
        System.out.println(ap.c1.getId() + " vs " + ap.c2.getId());
        System.out.println("c1:");
        System.out.print("labels: ");
        ap.c1.getAllLabels().forEach(l -> { System.out.print(l + ", "); });
        System.out.print("\nparents: ");
        getContextTerms(aprop.conceptParents1, ap.c1.getId()).forEach(p -> { System.out.print(p + ", "); });
        System.out.print("\nchildren: ");
        getContextTerms(aprop.conceptChildren1, ap.c1.getId()).forEach(c -> { System.out.print(c + ", "); });
        System.out.print("\nsiblings: ");
        getContextTerms(aprop.conceptSiblings1, ap.c1.getId()).forEach(s -> { System.out.print(s + ", "); });
        System.out.print("\nproperties: ");
        OntoOps.getProperties(aprop.sourceModel, ap.c1.getId()).forEach(pty -> { System.out.print(pty + ", "); });

        System.out.println("\n-------");
        System.out.println("c2:");
        System.out.print("labels: ");
        ap.c2.getAllLabels().forEach(l -> { System.out.print(l + " "); });
        System.out.print("\nparents: ");
        getContextTerms(aprop.conceptParents2, ap.c2.getId()).forEach(p -> { System.out.print(p + ", "); });
        System.out.print("\nchildren: ");
        getContextTerms(aprop.conceptChildren2, ap.c2.getId()).forEach(c -> { System.out.print(c + ", "); });
        System.out.print("\nsiblings: ");
        getContextTerms(aprop.conceptSiblings2, ap.c2.getId()).forEach(s -> { System.out.print(s + ", "); });
        System.out.print("\nproperties: ");
        OntoOps.getProperties(aprop.targetModel, ap.c2.getId()).forEach(pty -> { System.out.print(pty + ", "); });
      }
    }
*/
    double precision = HarmonicPR.hPrecision(correct, found);
    double recall = HarmonicPR.hRecall(correct, expected);
    double f1 = 2 * precision * recall / (precision + recall);

    System.out.println();
    System.out.println("H(p) = " + precision + " H(r) = " + recall + " H(fm) = " + f1 + " , threshold = " + threshold + " , edit_cut = " + editDistCut);

    return precision + "," + recall + "," + f1 + "," + threshold + "," + editDistCut;
//    return alignments;
  }

  public String weightedVectorAddition(double threshold, int maxN, double editDistCut) throws AlignmentException { // AlignedConcept alignedConcept,
    double alpha = 1.0;
    double beta = 0.0;
//    double gamma = 0.2;
    List<AlignedConcept> alignments = new ArrayList<>();
    Collection out = new ArrayList(); // for csv output
    List<AlignTrainTest> allTestcaseData = Alignment_oaei.generateConfAlignTrainTest();
    List<AlignedConcept> seenAlignmentList = new ArrayList<>(); // overall list of alignments returned
    // counts for evaluation
    List<Integer> found = new ArrayList();
    List<Integer> correct = new ArrayList();
    List<Integer> expected = new ArrayList();
    List<AlignedConcept> allGroundTruths = new ArrayList();

    System.out.println("current : " + threshold);

    for (AlignTrainTest alignTrainTest : allTestcaseData) {
      List<PreAlignedConcept> alignmentsInThreshold = new ArrayList<PreAlignedConcept>();
      int total = 0;
      int correctFound = 0;

      String sourceScheme = FilenameUtils.removeExtension(alignTrainTest.referenceAlignment.getName()).split("-")[0];
      String targetScheme = FilenameUtils.removeExtension(alignTrainTest.referenceAlignment.getName()).split("-")[1];

      List<OntClass> concepts1 = OntoOps.getOntoClasses(alignTrainTest.sourceOnto.getAbsolutePath()); // source ontology concepts
      List<OntClass> concepts2 = OntoOps.getOntoClasses(alignTrainTest.targetOnto.getAbsolutePath()); // target ontology concepts

      List<List<String>> collection1 = TFIDF.getCollection(concepts1, true); // source ontology collection
//      collection1.addAll(TFIDF.getCollection(concepts2, true));
      List<List<String>> collection2 = TFIDF.getCollection(concepts2, true); // target ontology collection
//      List<List<String>> collection2 = new ArrayList<>(collection1);

      List<List<String>> propColl = TFIDF.getPropertiesCollection(concepts1, true); // source ontology concept properties
      propColl.addAll(TFIDF.getPropertiesCollection(concepts2, true)); // add target ontology concept properties

//      OntModel sourceModel = OntoOps.getOntologyModel(alignTrainTest.sourceOnto.getAbsolutePath());
//      OntModel targetModel = OntoOps.getOntologyModel(alignTrainTest.targetOnto.getAbsolutePath());
//
//      // Parent context : labels of parents of all concepts in scheme
//      List<ConceptContext> conceptParents1 = getConceptsAndContextParents(sourceModel, concepts1, sourceScheme); // source
//      List<ConceptContext> conceptParents2 = getConceptsAndContextParents(targetModel, concepts2, targetScheme); // target
//
//      // Children context : labels of children of all concepts in scheme
//      List<ConceptContext> conceptChildren1 = getConceptsAndContextChildren(sourceModel, concepts1, sourceScheme); // source
//      List<ConceptContext> conceptChildren2 = getConceptsAndContextChildren(targetModel, concepts2, targetScheme); // target

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
      allGroundTruths.addAll(groundTruth);

      Map<Concept, ArrayList<RecommendedConcept>> candidateAlignments = new HashMap<Concept, ArrayList<RecommendedConcept>>(); // keeps candidate alignments
      // select concepts wit similarity above chosen threshold as candidate alignment concepts
      for (OntClass ontClass1 : concepts1) {
        ArrayList<RecommendedConcept> similarConcepts = new ArrayList<RecommendedConcept>(); // Similar concepts above a threshold
        for (OntClass ontClass2 : concepts2) {
          if (ontClass1 != null && ontClass2 != null) { // test for valid concepts (should have a concept id)
            Set<String> c1Terms = OntoOps.getLabels(ontClass1);
            Set<String> c2Terms = OntoOps.getLabels(ontClass2);
//            double equiv_sim = Math.max(vectorOps.maxWeightedVectorSimilarity(c1Terms, collection1, c2Terms, collection2, editDistCut), vectorOps.maxWeightedHybridSimilarity(c1Terms, collection1, c2Terms, collection2, editDistCut));
            double equiv_sim = vectorOps.maxWeightedVectorSimilarity(c1Terms, collection1, c2Terms, collection2, editDistCut);
            double sim = equiv_sim;


//            Set<String> c1ParentContextTerms = getContextTerms(conceptParents1, ontClass1.getURI());
//            Set<String> c2ParentContextTerms = getContextTerms(conceptParents2, ontClass2.getURI());
//            if (c1ParentContextTerms.isEmpty()) c1ParentContextTerms = c1Terms;
//            if (c2ParentContextTerms.isEmpty()) c2ParentContextTerms = c2Terms;
//            double parents_sim = vectorOps.maxWeightedVectorSimilarity(c1ParentContextTerms, collection1, c2ParentContextTerms, collection2, editDistCut);
//
//            Set<String> c1ChildrenContextTerms = getContextTerms(conceptChildren1, ontClass1.getURI());
//            Set<String> c2ChildrenContextTerms = getContextTerms(conceptChildren2, ontClass2.getURI());
//            if (c1ChildrenContextTerms.isEmpty()) c1ChildrenContextTerms = c1Terms;
//            if (c2ChildrenContextTerms.isEmpty()) c2ChildrenContextTerms = c2Terms;
//            double children_sim = vectorOps.maxWeightedVectorSimilarity(c1ChildrenContextTerms, collection1, c2ChildrenContextTerms, collection2, editDistCut);

//            double context_sim = Math.max(parents_sim, children_sim);
//            double sim = (alpha * equiv_sim) + (beta * context_sim);
//            double sim = (alpha * equiv_sim) + (beta * parents_sim) + (gamma * children_sim);

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

      // Filter
      int conceptCount = 0;
      for (Map.Entry<Concept, ArrayList<RecommendedConcept>> entry : candidateAlignments.entrySet()) {
        Concept c1 = entry.getKey();
        System.out.println(++conceptCount + ". Generating features for " + c1);
        ArrayList<RecommendedConcept> selectedConcepts = entry.getValue();
        System.out.println("Count of concepts above threshold " + selectedConcepts.size());

        for (int i = 0; i < selectedConcepts.size(); i++) {
          RecommendedConcept c2 = selectedConcepts.get(i);
          AlignedConcept alignedConcept = new AlignedConcept(c1.getId(), c2.getId(), Relation.Predicate.EXACT_MATCH.value);
//          if (c2.getScore() >= threshold && !AlignedConcept.containsTheAlignment(seenAlignmentList, alignedConcept)) { // continue if similarity is up to threshold and alignment is not selected already
//            seenAlignmentList.add(alignedConcept); // add new to list
            PreAlignedConcept pac = new PreAlignedConcept(alignedConcept, c2.getScore());
            alignmentsInThreshold = PreAlignedConcept.updateAlignments(alignmentsInThreshold, pac);

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
        }
      }
// Evaluate
alignments = PreAlignedConcept.getAlignments(alignmentsInThreshold);
//  alignments.add(new AlignedConcept(c1.getId(), c2.getId(), Relation.Predicate.EXACT_MATCH.value));
          for (AlignedConcept alignedConcept : alignments) {
            String classLabel = "N";
            total++;
            if (Evaluator.trueAlignment(groundTruth, alignedConcept)) { // check if present in the reference alignment
              correctFound++;
              classLabel = "Y";
            }
//            out.add(c1.getLabel() + " vs " + c2.getLabel() + " = " + c2.getScore() + " => " + classLabel);
          } // end if unseen alignment and above similarity threshold

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

    System.out.println("===");

    System.out.println("H(p) = " + precision + " H(r) = " + recall + " H(fm) = " + f1 + " , threshold = " + threshold + " , edit_cut = " + editDistCut);

    return precision + "," + recall + "," + f1 + "," + threshold + "," + editDistCut;
//    return alignments;
  }

  public static List<ConceptContext> getConceptsAndContextParents(OntModel ontModel, List<OntClass> concepts, String scheme) {
    List<ConceptContext> conceptContexts = new ArrayList();
    for (OntClass ontClass : concepts) {
      ConceptContext cc = new ConceptContext(ontClass.getURI(), OntoOps.getLabel(ontClass), scheme);
      Set<String> contextLabels = new HashSet();
      for (OntClass contextClass : OntoOps.getSuperClasses(ontModel, ontClass.getURI())) {
        contextLabels.addAll(OntoOps.getLabels(contextClass));
      }
      cc.context = contextLabels; // set context
      conceptContexts.add(cc); // add to list
    }

    return conceptContexts;
  }

  public static List<ConceptContext> getConceptsAndContextChildren(OntModel ontModel, List<OntClass> concepts, String scheme) {
    List<ConceptContext> conceptContexts = new ArrayList();
    for (OntClass ontClass : concepts) {
      ConceptContext cc = new ConceptContext(ontClass.getURI(), OntoOps.getLabel(ontClass), scheme);
      Set<String> contextLabels = new HashSet();
      for (OntClass contextClass : OntoOps.getSubClasses(ontModel, ontClass.getURI())) {
        contextLabels.addAll(OntoOps.getLabels(contextClass));
      }
      cc.context = contextLabels; // set context
      conceptContexts.add(cc); // add to list
    }

    return conceptContexts;
  }

  public static List<ConceptContext> getConceptsAndContextSibling(OntModel ontModel, List<OntClass> concepts, String scheme) {
    List<ConceptContext> conceptContexts = new ArrayList();
    for (OntClass ontClass : concepts) {
      ConceptContext cc = new ConceptContext(ontClass.getURI(), OntoOps.getLabel(ontClass), scheme);
      Set<String> contextLabels = new HashSet();
      for (OntClass contextClass : OntoOps.getSiblingClasses(ontModel, ontClass.getURI())) {
        contextLabels.addAll(OntoOps.getLabels(contextClass));
      }
      cc.context = contextLabels; // set context
      conceptContexts.add(cc); // add to list
    }

    return conceptContexts;
  }

  /**
   * Retrieves unique list of concept terms that are directly linked to a
   * concept.
   *
   * @param conceptContexts
   * @param uri
   * @return
   */
  public static Set<String> getContextTerms(List<ConceptContext> conceptContexts, String uri) {
    Set<String> contextConceptTerms = new HashSet();
    for (ConceptContext cc : conceptContexts) {
      if (cc.getId().equals(uri)) {
        for (String sub : cc.context) { // narrower context
          String str = sub.replaceAll("_", " ");
          str = str.replaceAll("\\s+", " "); // normalise spaces
          // prepare for lookup in word embedding vocabulary
          str = VectorOps.prepareStringSpaces(str);
          contextConceptTerms.add(str);
        }
        break; // early exit: occurs once!
      }
    }

    return contextConceptTerms;
  }

  public static void main(String[] args) {
    VectorOps vectorOps = new VectorOps(vectorModelPath);
    Conference_WHYS whys = new Conference_WHYS(vectorOps);
    try {
//      dummyMatch();
//      generateFeatures(0.5, 1);
      whys.weightedHybridSimilarity(0.76, 2, 0.85);
//      System.out.println(whys.weightedHybridSimilarity(0.76, 1, 0.89));
//      whys.weightedVectorAddition(0.77, 2, 0.85);
//      AlignedConcept.overlap(ApproachesConference.wordNet(), whys.weightedHybridSimilarity(0.76, 1, 0.89));
/*
      Collection results = new ArrayList<>();
      results.add("precision,recall,f-measure,threshold,edit_dist_cut");
      for (double threshold = 0.7; threshold <= .9; threshold+=0.01) {
        for (double editDistCut = 0.85; editDistCut <= .9; editDistCut+=0.01) {
          String res = whys.weightedHybridSimilarity(threshold, 2, editDistCut);
//          String res = whys.weightedVectorAddition(threshold, 2, editDistCut);
          results.add(res);
          System.out.println(res);
        }
      }
      System.out.println("\n===COMPLETE===\n");
      results.forEach(System.out::println);
*/
    } catch (AlignmentException ex) {
      Logger.getLogger(Conference_WHYS.class.getName()).log(Level.SEVERE, null, ex);
    }
  }


  class AlignmentProp {
    OntModel sourceModel;
    OntModel targetModel;
    // Parents
    List<ConceptContext> conceptParents1; // source
    List<ConceptContext> conceptParents2; // target
    // Children context : labels of children of all concepts in scheme
    List<ConceptContext> conceptChildren1; // source
    List<ConceptContext> conceptChildren2; // target
    // Sibling context : labels of siblings (other children of parents) of all concepts in scheme
    List<ConceptContext> conceptSiblings1; // source
    List<ConceptContext> conceptSiblings2;

    public AlignmentProp(OntModel sourceModel, OntModel targetModel, List<ConceptContext> conceptParents1, List<ConceptContext> conceptParents2, List<ConceptContext> conceptChildren1, List<ConceptContext> conceptChildren2, List<ConceptContext> conceptSiblings1, List<ConceptContext> conceptSiblings2) {
      this.sourceModel = sourceModel;
      this.targetModel = targetModel;
      this.conceptParents1 = conceptParents1;
      this.conceptParents2 = conceptParents2;
      this.conceptChildren1 = conceptChildren1;
      this.conceptChildren2 = conceptChildren2;
      this.conceptSiblings1 = conceptSiblings1;
      this.conceptSiblings2 = conceptSiblings2;
    }

  }

  class AlignmentPlus {
    AlignedConcept alignedConcept;
    int id;
    Concept c1;
    Concept c2;

    public AlignmentPlus(AlignedConcept alignedConcept, int id, Concept c1, Concept c2) {
      this.alignedConcept = alignedConcept;
      this.id = id;
      this.c1 = c1;
      this.c2 = c2;
    }

  }
}
