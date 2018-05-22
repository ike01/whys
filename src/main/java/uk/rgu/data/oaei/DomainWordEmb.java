package uk.rgu.data.oaei;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Cell;
import uk.rgu.data.model.AlignedConcept;
import uk.rgu.data.model.Concept;
import uk.rgu.data.model.ConceptContext;
import uk.rgu.data.model.LinkedConcept;
import uk.rgu.data.model.RecommendedConcept;
import uk.rgu.data.model.Related;
import uk.rgu.data.utilities.Relation;
import uk.rgu.data.ontologyprocessor.word2vec.UseConceptVectors;
import uk.rgu.data.ontologyprocessor.word2vec.VectorOps;
import uk.rgu.data.utilities.Evaluator;
import uk.rgu.data.utilities.FileOps;
import uk.rgu.data.utilities.StringOps;

/**
 *
 * @author 1113938
 */
public class DomainWordEmb extends ObjectAlignment implements AlignmentProcess {

  static VectorOps vectorOps = null;
  static String modelPath = null;

  public static void setModel() {
    System.out.println("Setting word2vec model to: " + modelPath);
    vectorOps = new VectorOps(modelPath);
  }

  @Override
  public void align(Alignment a, Properties prprts) throws AlignmentException {
    try {
      // Match classes
      for (Object cl2 : ontology2().getClasses()) {
        for (Object cl1 : ontology1().getClasses()) {
          // add mapping into alignment object
          addAlignCell(cl1, cl2, "=", match(cl1, cl2, prprts.getProperty("word2vec")));
        }
      }
      // Match dataProperties
//      for (Object p2 : ontology2().getDataProperties()) {
//        for (Object p1 : ontology1().getDataProperties()) {
//          // add mapping into alignment object
//          addAlignCell(p1, p2, "=", match(p1, p2));
//        }
//      }
      // Match objectProperties
//      for (Object p2 : ontology2().getObjectProperties()) {
//        for (Object p1 : ontology1().getObjectProperties()) {
//          // add mapping into alignment object
//          addAlignCell(p1, p2, "=", match(p1, p2));
//        }
//      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public double match(Object o1, Object o2, String w2vModel) throws AlignmentException {
//    double t = .69; // vector similarity threshold for alignment
    try {
      String s1 = ontology1().getEntityName(o1);
      String s2 = ontology2().getEntityName(o2);
//      System.out.println(s1 + " vs " + s2);

      if (s1 == null || s2 == null) {
        System.out.println("a string is null");
        return 0.;
      } else {
        // convert from camelCase to human-readable
        s1 = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(s1), ' ');
        s2 = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(s2), ' ');

        s1 = s1.replaceAll("\\s-\\s", "-"); // e.g. reverts "co - author" to "co-author"
        s2 = s2.replaceAll("\\s-\\s", "-");

        // prepare for lookup in word embedding vocabulary
        s1 = VectorOps.prepareStringSpaces(s1);
        s2 = VectorOps.prepareStringSpaces(s2);
        if (modelPath != w2vModel) {
          modelPath = w2vModel;
          setModel();
        }
        double sim = vectorOps.sentenceSimilarity(s1, s2);
//        if (sim > 0.8) {
//          System.out.println(s1 + " VS " + s2 + " = " + sim);
//        }
        return sim;
      }
    } catch (OntowrapException owex) {
      throw new AlignmentException("Error getting entity name", owex);
    }
  }


  //#####################################################################

  /**
   * Retrieves the most similar concepts that are above a threshold.
   *
   * @param t
   * @param n
   */
  public static void generateFeatures(double t, int n) throws AlignmentException { // AlignedConcept alignedConcept,
    Collection out = new ArrayList<String>(); // for csv output
    List<AlignTrainTest> allTestcaseData = Alignment_oaei.generateConfAlignTrainTest();

    // Header
    String header = "sourceScheme,targetScheme,string_match,has_vector,similarity,offset_next_similar,rank,edit_distance,offset_next_edit_distance,max_vec_sim,offset_next_max,avg_vec_sim,offset_next_avg,context_size,overlap_with_nearest(match),parents_overlap,children_overlap,sibling_overlap,avg_max_overlap,context_overlap,class";
    out.add(header);

    for (AlignTrainTest alignTrainTest : allTestcaseData) {
      System.out.println("Fetching concepts ... ");
      String sourceScheme = FilenameUtils.removeExtension(alignTrainTest.referenceAlignment.getName()).split("-")[0];
      String targetScheme = FilenameUtils.removeExtension(alignTrainTest.referenceAlignment.getName()).split("-")[1];

      List<OntClass> concepts1 = OntoOps.getOntoClasses(alignTrainTest.sourceOnto.getAbsolutePath());
      List<OntClass> concepts2 = OntoOps.getOntoClasses(alignTrainTest.targetOnto.getAbsolutePath());

      OntModel sourceModel = OntoOps.getOntologyModel(alignTrainTest.sourceOnto.getAbsolutePath());
      OntModel targetModel = OntoOps.getOntologyModel(alignTrainTest.targetOnto.getAbsolutePath());

      List<String> doc1 = OntoOps.getOntoTerms(sourceModel); // individual terms in ontology
      List<String> doc2 = OntoOps.getOntoTerms(targetModel);

      System.out.println("Concept size for " + sourceScheme + " => " + concepts1.size());
      System.out.println("Concept size for " + targetScheme + " => " + concepts2.size());

      // ground truth
      AlignmentParser aparser = new AlignmentParser(0);
      Alignment reference = aparser.parse(alignTrainTest.referenceAlignment.toPath().toUri());
      List<AlignedConcept> groundTruth = new ArrayList();
      for (Iterator<Cell> iterator = reference.iterator(); iterator.hasNext();) {
        Cell cell = iterator.next();
        groundTruth.add(new AlignedConcept(cell.getObject1AsURI().toString(), cell.getObject2AsURI().toString(), Relation.Predicate.EXACT_MATCH.value));
      }

      List<LinkedConcept> linkedConcepts2 = getAllLinkedConcept(targetModel, concepts2, targetScheme);

      List<ConceptContext> conceptContexts1 = getConceptsAndContext(sourceModel, concepts1, sourceScheme); // source
      List<ConceptContext> conceptContexts2 = getConceptsAndContext(targetModel, concepts2, targetScheme); // target

      // Parent context : labels of parents of all concepts in scheme
      List<ConceptContext> conceptParents1 = getConceptsAndContextParents(sourceModel, concepts1, sourceScheme); // source
      List<ConceptContext> conceptParents2 = getConceptsAndContextParents(targetModel, concepts2, targetScheme); // target

      // Children context : labels of children of all concepts in scheme
      List<ConceptContext> conceptChildren1 = getConceptsAndContextChildren(sourceModel, concepts1, sourceScheme); // source
      List<ConceptContext> conceptChildren2 = getConceptsAndContextChildren(targetModel, concepts2, targetScheme); // target

      // Sibling context : labels of siblings (other children of parents) of all concepts in scheme
      List<ConceptContext> conceptSiblings1 = getConceptsAndContextSibling(sourceModel, concepts1, sourceScheme); // source
      List<ConceptContext> conceptSiblings2 = getConceptsAndContextSibling(targetModel, concepts2, targetScheme); // target

      Map<Concept, ArrayList<RecommendedConcept>> candidateAlignments = new HashMap<Concept, ArrayList<RecommendedConcept>>(); // keeps candidate alignments
      modelPath = "data/model/" + FilenameUtils.removeExtension(alignTrainTest.sourceOnto.getName()) + "-" + FilenameUtils.removeExtension(alignTrainTest.targetOnto.getName()) + "_model.txt";
      setModel(); // set word2vec model

      // select concepts wit similarity above chosen threshold as candidate alignment concepts
      for (OntClass ontClass1 : concepts1) {
        ArrayList<RecommendedConcept> similarConcepts = new ArrayList<RecommendedConcept>(); // Similar concepts above a threshold
//      int selected = 0; // tracks number of concepts selected up to n
        for (OntClass ontClass2 : concepts2) {
          if (ontClass1 != null && ontClass2 != null) { // test for valid concepts (should have a concept id)
            double vecSim = vectorOps.maxHybridSimilarity(OntoOps.getLabels(ontClass1), OntoOps.getLabels(ontClass2)); // maximum cosine similarity
            // check if similarity is up to the threshold by string similarity or vector similarity
            if (vecSim >= t) { // check alignment
              Concept c2 = new Concept(ontClass2.getURI(), OntoOps.getLabel(ontClass2), targetScheme);
//            selected++;
//            AlignedConcept ac = new AlignedConcept(Concept.getFullConceptUri(c1.getConceptId()), Concept.getFullConceptUri(c2.getConceptId()), Relation.Predicate.CLOSE_MATCH.value);
              similarConcepts.add(new RecommendedConcept(c2, vecSim)); // keep similarity
            }
          } // end if (test of valid concepts)

        } // concept2 loop ends
        if (!similarConcepts.isEmpty()) {
          Collections.sort(similarConcepts, new RecommendedConcept.RecommendedConceptComparator()); // sort in descending order of score
          Concept c1 = new Concept(ontClass1.getURI(), OntoOps.getLabel(ontClass1), sourceScheme);
          candidateAlignments.put(c1, similarConcepts);
        }
      } // concept1 loop ends

      // Generate features for alignment concepts
      // FEATURES
      int conceptCount = 0;
      for (Map.Entry<Concept, ArrayList<RecommendedConcept>> entry : candidateAlignments.entrySet()) {
        Concept c1 = entry.getKey();
        System.out.println(++conceptCount + ". Generating features for " + c1);
        ArrayList<RecommendedConcept> selectedConcepts = entry.getValue();
        System.out.println("Count of concepts above threshold " + selectedConcepts.size());
        Set<String> similarConceptIds = new HashSet<String>();
        for (int i = 0; i < selectedConcepts.size(); i++) {
          similarConceptIds.add(selectedConcepts.get(i).getId());
        }

        for (int i = 0; i < selectedConcepts.size(); i++) {
          if (i == n) {
            break; // top n max
          }
          RecommendedConcept c2 = selectedConcepts.get(i);
          RecommendedConcept next_c2 = null;
          int j = i + 1; // index of next concept
          if (j < selectedConcepts.size()) { // not exceeding index limit
            next_c2 = selectedConcepts.get(j);
          }

          // 0. Concept Ids
          String line = c1.getConceptIdFromFullUri() + "," + c2.getConceptIdFromFullUri() + ","; // an entry in output

          // 1. Exact string match
          String exactStringMatch = "false";
          if (UseConceptVectors.checkStringMatch(c1.getAllLabels(), c2.getAllLabels(), false)) {
            exactStringMatch = "true";
          }
          line += exactStringMatch + ",";

          // 2. Exact match have vectors
          String exactMatchHaveVectors = "false";
          if (UseConceptVectors.checkMatchHaveVectors(vectorOps, VectorOps.prepareStringSpaces(c1.getAllLabels()), VectorOps.prepareStringSpaces(c2.getAllLabels()))) {
            exactMatchHaveVectors = "true";
          }
          line += exactMatchHaveVectors + ",";

          // 3. Similarity measure (hybrid)
          line += c2.getScore() + ",";

          // 4. Offset to next most similar
          if (null != next_c2) {
            line += (c2.getScore() - next_c2.getScore()) + ",";
          } else {
            line += -1.0 + ",";
          }

          // 5. Rank in recommendation list
          line += (i + 1) + ",";

          // 6. NormalizedLevenshtein
          double normalizedLevenshtein = StringOps.maxSimilarityByNormalizedLevenshtein(c1.getAllLabels(), c2.getAllLabels());
          line += normalizedLevenshtein + ",";

          // 7. Offset to next maximum NormalizedLevenshtein
          if (null != next_c2) {
            double nextMaximumNormalizedLevenshtein = StringOps.maxSimilarityByNormalizedLevenshtein(c1.getAllLabels(), next_c2.getAllLabels());
            line += (normalizedLevenshtein - nextMaximumNormalizedLevenshtein) + ",";
          } else {
            line += -1.0 + ",";
          }

          // 8. Vector similarity
          double maximumVectorSimilarity = vectorOps.maxSimilarity(VectorOps.prepareStringSpaces(c1.getAllLabels()), VectorOps.prepareStringSpaces(c2.getAllLabels()));
          line += maximumVectorSimilarity + ",";

          // 9. Offset to next maximum vector similarity
          if (null != next_c2) {
            double nextMaximumVectorSimilarity = vectorOps.maxSimilarity(VectorOps.prepareStringSpaces(c1.getAllLabels()), VectorOps.prepareStringSpaces(next_c2.getAllLabels()));
            line += (maximumVectorSimilarity - nextMaximumVectorSimilarity) + ",";
          } else {
            line += -1.0 + ",";
          }

          // 10. Average vector similarity
          double averageVectorSimilarity = vectorOps.averageSimilarity(VectorOps.prepareStringSpaces(c1.getAllLabels()), VectorOps.prepareStringSpaces(c2.getAllLabels()));
          line += averageVectorSimilarity + ","; // last entry

          // 11. Offset to next average vector similarity
          if (null != next_c2) {
            double nextAverageVectorSimilarity = vectorOps.averageSimilarity(VectorOps.prepareStringSpaces(c1.getAllLabels()), VectorOps.prepareStringSpaces(next_c2.getAllLabels()));
            line += (averageVectorSimilarity - nextAverageVectorSimilarity) + ",";
          } else {
            line += -1.0 + ","; // last entry
          }

          // 12. Context size
          Set<String> contextConceptsIds = getContextIds(linkedConcepts2, c2);
          line += contextConceptsIds.size() + ",";

          // 13. Context overlap (numeric proportion)
          int denom = selectedConcepts.size() > contextConceptsIds.size() ? contextConceptsIds.size() : selectedConcepts.size() - 1; // -1 after c2 is removed
          int overlap = StringOps.countWordOverlap(contextConceptsIds, similarConceptIds);
          double contextOverlap = (double) overlap / denom;
          if (Double.isNaN(contextOverlap)) {
            contextOverlap = -1.0;
          }
          line += contextOverlap + ",";

          // 14. Semantic overlap of parents
          Set<String> c1ParentContextTerms = getContextTerms(conceptParents1, c1);
          Set<String> c2ParentContextTerms = getContextTerms(conceptParents2, c2);
          double parentContextSemanticOverlap = vectorOps.maxSimilarity(VectorOps.prepareStringSpaces(c1ParentContextTerms), VectorOps.prepareStringSpaces(c2ParentContextTerms));
          if (Double.isNaN(parentContextSemanticOverlap)) {
            parentContextSemanticOverlap = -1.0;
          }
          line += parentContextSemanticOverlap + ",";

          // 15. Semantic overlap of children
          Set<String> c1ChildrenContextTerms = getContextTerms(conceptChildren1, c1);
          Set<String> c2ChildrenContextTerms = getContextTerms(conceptChildren2, c2);
          double childrenContextSemanticOverlap = vectorOps.maxSimilarity(VectorOps.prepareStringSpaces(c1ChildrenContextTerms), VectorOps.prepareStringSpaces(c2ChildrenContextTerms));
          if (Double.isNaN(childrenContextSemanticOverlap)) {
            childrenContextSemanticOverlap = -1.0;
          }
          line += childrenContextSemanticOverlap + ",";

          // 16. Semantic overlap of siblings
          Set<String> c1SiblingContextTerms = getContextTerms(conceptSiblings1, c1);
          Set<String> c2SiblingContextTerms = getContextTerms(conceptSiblings2, c2);
          double siblingContextSemanticOverlap = vectorOps.maxSimilarity(VectorOps.prepareStringSpaces(c1SiblingContextTerms), VectorOps.prepareStringSpaces(c2SiblingContextTerms));
          if (Double.isNaN(siblingContextSemanticOverlap)) {
            siblingContextSemanticOverlap = -1.0;
          }
          line += siblingContextSemanticOverlap + ",";

          // 17. Average of maximum parents, children and siblings overlap
          double averageMaxContextOverlap = (parentContextSemanticOverlap + childrenContextSemanticOverlap + siblingContextSemanticOverlap) / 3;
          if (Double.isNaN(averageMaxContextOverlap)) {
            averageMaxContextOverlap = -1.0;
          }
          line += averageMaxContextOverlap + ",";

          // 18. Context overlap (semantic)
          String c1ContextTerms = getContextTermsString(conceptContexts1, c1);
          String c2ContextTerms = getContextTermsString(conceptContexts2, c2);
          double contextSemanticOverlap = vectorOps.hybridSimilarity(c1ContextTerms, c2ContextTerms);
          if (Double.isNaN(contextSemanticOverlap)) {
            contextSemanticOverlap = -1.0;
          }
          line += contextSemanticOverlap + ",";

          // 19. Class (label) (aligned = "Y", not aligned = "N")
          String classLabel = "N";

          // Load the reference alignment
          AlignedConcept alignedConcept = new AlignedConcept(c1.getId(), c2.getId(), Relation.Predicate.EXACT_MATCH.value);
          if (Evaluator.trueAlignment(groundTruth, alignedConcept)) {
            classLabel = "Y";
          }
          line += classLabel;
          System.out.println(line);
          out.add(line); // add to output
        }
      }
    }

    // Write output to file
    String fileName = "alignment_features_oaei" + n + "_(exact_match).csv";
    FileOps.printResults(out, fileName);
  }

  private static List<LinkedConcept> getAllLinkedConcept(OntModel ontModel, List<OntClass> concepts, String scheme) {
    List<LinkedConcept> linkedConcepts = new ArrayList();

    for (OntClass ontClass : concepts) {
      LinkedConcept l = new LinkedConcept(ontClass.getURI(), OntoOps.getLabel(ontClass), scheme);
      // sup
      List<String> sup = new ArrayList();
      for (OntClass contextClass : OntoOps.getSuperClasses(ontModel, ontClass.getURI())) {
        sup.add(contextClass.getURI());
      }
      l.setSuperClasslike(new Related(Relation.Predicate.BROADER.value, sup));
      // sub
      List<String> sub = new ArrayList();
      for (OntClass contextClass : OntoOps.getSubClasses(ontModel, ontClass.getURI())) {
        sub.add(contextClass.getURI());
      }
      l.setSubClasslike(new Related(Relation.Predicate.BROADER.value, sub));

      // add to result list
      linkedConcepts.add(l);
    }

    return linkedConcepts;
  }

  private static List<ConceptContext> getConceptsAndContext(OntModel ontModel, List<OntClass> concepts, String scheme) {
    List<ConceptContext> conceptContexts = new ArrayList();
    for (OntClass ontClass : concepts) {
      ConceptContext cc = new ConceptContext(ontClass.getURI(), OntoOps.getLabel(ontClass), scheme);
      Set<String> contextLabels = new HashSet();
      for (OntClass contextClass : OntoOps.getSemanticContext(ontModel, ontClass.getURI())) {
        contextLabels.addAll(OntoOps.getLabels(contextClass));
      }
      cc.context = contextLabels; // set context
      conceptContexts.add(cc); // add to list
    }

    return conceptContexts;
  }

  private static List<ConceptContext> getConceptsAndContextParents(OntModel ontModel, List<OntClass> concepts, String scheme) {
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

  private static List<ConceptContext> getConceptsAndContextChildren(OntModel ontModel, List<OntClass> concepts, String scheme) {
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

  private static List<ConceptContext> getConceptsAndContextSibling(OntModel ontModel, List<OntClass> concepts, String scheme) {
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
   * Retrieves unique list of conceptIds that are directly linked to a concept.
   *
   * @param linkedConcepts
   * @param c
   * @return
   */
  public static Set<String> getContextIds(List<LinkedConcept> linkedConcepts, Concept c) {
    Set<String> contextConceptIds = new HashSet<String>();
//    System.out.println("concept : " + c);
//    System.out.println("concept id : " + c.getId());
    String id = c.getId();
//    System.out.println("full uri : " + id);
    for (LinkedConcept cc : linkedConcepts) {
      if (cc.getId().equals(id)) {
//        System.out.println("Found concept context Id: " + cc);
        for (String sub : cc.getSubClasslike().getValues()) { // narrower context
          contextConceptIds.add(sub);
        }
        for (String sup : cc.getSuperClasslike().getValues()) { // broader context
          contextConceptIds.add(sup);
        }
        break; // early exit: occurs once!
      }
    }

    return contextConceptIds;
  }

  /**
   * Retrieves unique list of concept terms that are directly linked to a
   * concept.
   *
   * @param conceptContexts
   * @param c
   * @return
   */
  public static Set<String> getContextTerms(List<ConceptContext> conceptContexts, Concept c) {
    Set<String> contextConceptTerms = new HashSet<String>();
//    System.out.println("concept : " + c);
//    System.out.println("concept id : " + c.getConceptId());
//    String id = Concept.getFullConceptUri(c.getConceptId());
//    System.out.println("full uri : " + id);
    for (ConceptContext cc : conceptContexts) {
      if (cc.getId().equals(c.getId())) {
//        System.out.println("Found concept context term: " + cc);
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

//    String out = "";
//    for (String str : contextConceptTerms) {
//      out = out + str + " ";
//    }
    return contextConceptTerms;
  }

  /**
   * Retrieves concatenated string of concept terms that are directly linked to a
   * concept.
   *
   * @param conceptContexts
   * @param c
   * @return
   */
  public static String getContextTermsString(List<ConceptContext> conceptContexts, Concept c) {
    String contextConceptTerms = "";
//    System.out.println("concept : " + c);
//    System.out.println("concept id : " + c.getConceptId());
//    String id = Concept.getFullConceptUri(c.getConceptId());
//    System.out.println("full uri : " + id);
    for (ConceptContext cc : conceptContexts) {
      if (cc.getId().equals(c.getId())) {
//        System.out.println("Found concept context term: " + cc);
        for (String sub : cc.context) { // narrower context
          String str = sub.replaceAll("_", " ");
          str = str.replaceAll("\\s+", " "); // normalise spaces

          // prepare for lookup in word embedding vocabulary
          str = VectorOps.prepareStringSpaces(str);
          contextConceptTerms = contextConceptTerms + " " + str;
        }
        break; // early exit: occurs once!
      }
    }

    return contextConceptTerms.trim();
  }

  public static void main(String[] args) {
    try {
//      dummyMatch();
      generateFeatures(0.5, 1);
//      generateFeatures(0.5, 2);
    } catch (AlignmentException ex) {
      Logger.getLogger(Hybrid.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}
