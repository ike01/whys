package uk.rgu.data.oaei;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import info.debatty.java.stringsimilarity.Cosine;
import info.debatty.java.stringsimilarity.Damerau;
import info.debatty.java.stringsimilarity.Jaccard;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.Levenshtein;
import info.debatty.java.stringsimilarity.LongestCommonSubsequence;
import info.debatty.java.stringsimilarity.MetricLCS;
import info.debatty.java.stringsimilarity.NGram;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import info.debatty.java.stringsimilarity.OptimalStringAlignment;
import info.debatty.java.stringsimilarity.QGram;
import info.debatty.java.stringsimilarity.SorensenDice;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.text.similarity.FuzzyScore;
import org.ivml.alimo.ISub;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.simmetrics.StringMetric;
import org.simmetrics.StringMetrics;
import org.simmetrics.metrics.MongeElkan;
import uk.rgu.data.model.AlignedConcept;
import uk.rgu.data.model.Concept;
import uk.rgu.data.model.ConceptContext;
import uk.rgu.data.model.RecommendedConcept;
import uk.rgu.data.utilities.Relation;
import uk.rgu.data.ontologyprocessor.word2vec.UseConceptVectors;
import uk.rgu.data.ontologyprocessor.word2vec.VectorOps;
import uk.rgu.data.utilities.Evaluator;
import uk.rgu.data.utilities.StringOps;

/**
 *
 * @author aikay
 */
public class Test {

  public static void main2(String[] args) {
//    System.out.println(prepareString("exampleTest"));

    NormalizedLevenshtein l = new NormalizedLevenshtein();
//    Jaccard l = new Jaccard();
    System.out.println("Edit distance: " + l.similarity("p reference", "preference"));
  }

  public static String prepareString(String str) {
//    str = StringOps.stripAllParentheses(str);
    str = str.replaceAll("\\b\\d+\\b|[\\.:,\"\'\\(\\)\\[\\]|/?!;]+", "");

    str = StringUtils.join(
            StringUtils.splitByCharacterTypeCamelCase(str),
            ' '
    );

    return str.replaceAll("\\s+", "_").toLowerCase().trim();
  }

  public static void stringSimilarity() {
    String first = "Dinner"; // "meeting";
    String second = "Banquet"; // "melting";
    int max = first.length() > second.length() ? first.length() : second.length();
    int min = first.length() < second.length() ? first.length() : second.length();
    String longer = first.length() > second.length() ? first : second;
    System.out.println("info.debatty".toUpperCase());
    Jaccard j = new Jaccard();
    System.out.println("Jaccard (similarity): " + j.similarity(first, second));
    SorensenDice sd = new SorensenDice();
    System.out.println("SorensenDice (similarity): " + sd.similarity(first, second));
    JaroWinkler jw = new JaroWinkler();
    System.out.println("JaroWinkler (similarity): " + jw.similarity(first, second));
    Levenshtein l = new Levenshtein();
    System.out.println("Levenshtein (distance): " + l.distance(first, second));
    NormalizedLevenshtein nl = new NormalizedLevenshtein();
    System.out.println("NormalizedLevenshtein (similarity): " + nl.similarity(first, second));
    Damerau d = new Damerau();
    System.out.println("Damerau (distance): " + d.distance(first, second));
    double dSim = 1 - d.distance(first, second) / max;
    System.out.println("Damerau (similarity): " + dSim);
    Cosine c = new Cosine();
    System.out.println("Cosine (similarity): " + c.similarity(first, second));
    QGram q = new QGram(2);
    System.out.println("QGram (distance): " + q.distance(first, second));
    double qSim = 1 - q.distance(first, second) / max;
    System.out.println("QGram (similarity): " + qSim);
    LongestCommonSubsequence lcs = new LongestCommonSubsequence();
    System.out.println("LongestCommonSubsequence (distance): " + lcs.distance(first, second));
    MetricLCS mlcs = new MetricLCS();
    System.out.println("MetricLCS (distance): " + mlcs.distance(first, second));
    System.out.println("MetricLCS (similarity1): " + (1 - mlcs.distance(first, second)));
    OptimalStringAlignment osa = new OptimalStringAlignment();
    System.out.println("OptimalStringAlignment (distance): " + osa.distance(first, second));
    double osaSim = 1 - osa.distance(first, second) / max;
    System.out.println("OptimalStringAlignment (similarity): " + osaSim);
    NGram n = new NGram(2);
    System.out.println("NGram (distance): " + n.distance(first, second));
    double nSim = 1 - n.distance(first, second);
    System.out.println("NGram (similarity): " + nSim);
    SorensenDice s = new SorensenDice();
    System.out.println("SorensenDice (similarity): " + s.similarity(first, second));
    System.out.println("apache.commons".toUpperCase());
    FuzzyScore fs = new FuzzyScore(Locale.ENGLISH);
    System.out.println("FuzzyScore: " + fs.fuzzyScore(first, second));
    System.out.println("FuzzyScore (similarity): " + (double) fs.fuzzyScore(first, second) / fs.fuzzyScore(longer, longer));
    System.out.println("OTHERS");
    ISub isub = new ISub();
    System.out.println("ISub (Stoilos (score)): " + isub.score(first, second));
    double sm = Math.max(0.0, (double) (min - l.distance(first, second)) / min);
    System.out.println("String Matching (similarity): " + sm);
    StringMetric metric = StringMetrics.mongeElkan();
    System.out.println("MongeElkan (similarity): " + metric.compare(first.toLowerCase(), second.toLowerCase()));
  }

  public static void selectCandidates(double t, int n) throws AlignmentException {
    String vectorModelPath = "C:/dev/rgu/word2vec/models/geo_hascontext1_model.txt";
//    String vectorModelPath = "C:/dev/rgu/word2vec/models/GoogleNews-vectors-negative300.bin.gz";
//    String vectorModelPath = "/program-data/DGXWord2Vec/data/model/wikipedia_plain_model300_min10_iter5_custom_token.txt";
    // Get vectors
    VectorOps vectorOps = new VectorOps(vectorModelPath);

    Collection out = new ArrayList<String>(); // for csv output
    List<AlignTrainTest> allTestcaseData = Alignment_oaei.generateConfAlignTrainTest();

    // Header
    String header = "sourceScheme,targetScheme,string_match,has_vector,similarity,offset_next_similar,edit_distance,offset_next_edit_distance,max_vec_sim,offset_next_max,avg_vec_sim,offset_next_avg,prefix_overlap,context_size,overlap_with_nearest(match),parents_overlap,children_overlap,sibling_overlap,avg_max_overlap,context_overlap,class";
    out.add(header);
    int total = 0;
    int correct = 0;
    int goldstandard = 0;
    List<AlignedConcept> seenAlignmentList = new ArrayList<>(); // overall list of alignments returned
    Collection monitoring = new ArrayList<>();

    for (AlignTrainTest alignTrainTest : allTestcaseData) {
      System.out.println("Fetching concepts ... ");
      String sourceScheme = FilenameUtils.removeExtension(alignTrainTest.referenceAlignment.getName()).split("-")[0];
      String targetScheme = FilenameUtils.removeExtension(alignTrainTest.referenceAlignment.getName()).split("-")[1];

      List<OntClass> concepts1 = OntoOps.getOntoClasses(alignTrainTest.sourceOnto.getAbsolutePath());
      List<OntClass> concepts2 = OntoOps.getOntoClasses(alignTrainTest.targetOnto.getAbsolutePath());

      OntModel sourceModel = OntoOps.getOntologyModel(alignTrainTest.sourceOnto.getAbsolutePath());
      OntModel targetModel = OntoOps.getOntologyModel(alignTrainTest.targetOnto.getAbsolutePath());

//      List<String> doc1 = OntoOps.getOntoTerms(sourceModel); // individual terms in ontology
//      List<String> doc2 = OntoOps.getOntoTerms(targetModel);
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
      goldstandard += groundTruth.size();
      // Retrieve semantic contexts
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

      for (int typeId = 1; typeId <= 3; typeId++) { // try different types of similarity (switch statement within)
        // select concepts wit similarity above chosen threshold as candidate alignment concepts
        Map<Concept, ArrayList<RecommendedConcept>> candidateAlignments = new HashMap<Concept, ArrayList<RecommendedConcept>>(); // keeps candidate alignments
        for (OntClass ontClass1 : concepts1) {
          ArrayList<RecommendedConcept> similarConcepts = new ArrayList<RecommendedConcept>(); // Similar concepts above a threshold
//      int selected = 0; // tracks number of concepts selected up to n
          for (OntClass ontClass2 : concepts2) {
            if (ontClass1 != null && ontClass2 != null) { // test for valid concepts (should have a concept id)
//            double vecSim = StringOps.maxSimilarityByJaccard(OntoOps.getLabels(ontClass1), OntoOps.getLabels(ontClass2)); // maximum cosine similarity
              double vecSim;
              switch (typeId) {
                case 1:
                  vecSim = vectorOps.maxHybridSimilarity(VectorOps.prepareStringSpaces(OntoOps.getLabels(ontClass1)), VectorOps.prepareStringSpaces(OntoOps.getLabels(ontClass2)));
                  t = 0.5;
                  break;
                case 2:
                  vecSim = StringOps.maxSimilarityByStoilos(VectorOps.prepareStringSpaces(OntoOps.getLabels(ontClass1)), VectorOps.prepareStringSpaces(OntoOps.getLabels(ontClass2)));
                  t = 0.85;
                  break;
                case 3:
                  Set<String> c1ParentContextTerms = getContextTerms(conceptParents1, ontClass1);
                  Set<String> c2ParentContextTerms = getContextTerms(conceptParents2, ontClass2);
                  Set<String> c1ChildrenContextTerms = getContextTerms(conceptChildren1, ontClass1);
                  Set<String> c2ChildrenContextTerms = getContextTerms(conceptChildren2, ontClass2);
                  double parentContextSemanticOverlap = vectorOps.maxHybridSimilarity(VectorOps.prepareStringSpaces(c1ParentContextTerms), VectorOps.prepareStringSpaces(c2ParentContextTerms)); // semantic overlap of parents
                  double childrenContextSemanticOverlap = vectorOps.maxHybridSimilarity(VectorOps.prepareStringSpaces(c1ChildrenContextTerms), VectorOps.prepareStringSpaces(c2ChildrenContextTerms)); // semantic overlap of children
//                  Set<String> c1SiblingContextTerms = getContextTerms(conceptSiblings1, ontClass1);
//                  Set<String> c2SiblingContextTerms = getContextTerms(conceptSiblings2, ontClass2);
//                  double siblingContextSemanticOverlap = vectorOps.maxHybridSimilarity(VectorOps.prepareStringSpaces(c1SiblingContextTerms), VectorOps.prepareStringSpaces(c2SiblingContextTerms)); // semantic overlap of siblings

                  // average of maximum parents, children and siblings overlap
                  vecSim = (parentContextSemanticOverlap + childrenContextSemanticOverlap) / 2;
                  t = 0.3;
                  break;
//                case 4:
//                  double parentContextOverlap = StringOps.maxSimilarityByStoilos(VectorOps.prepareStringSpaces(c1ParentContextTerms), VectorOps.prepareStringSpaces(c2ParentContextTerms)); // semantic overlap of parents
//                  double childrenContextOverlap = StringOps.maxSimilarityByStoilos(VectorOps.prepareStringSpaces(c1ChildrenContextTerms), VectorOps.prepareStringSpaces(c2ChildrenContextTerms)); // semantic overlap of children
//                  // average of maximum parents, children overlap
//                  vecSim = (parentContextOverlap + childrenContextOverlap) / 2;
//                  t = 0.5;
//                  break;
                default:
                  vecSim = 0.0;
                  break;
              }
              // check if similarity is up to the threshold by string similarity or vector similarity
              if (vecSim >= 0.1) { // check alignment (low score to preserve concepts below threshold for offsets computation)
                Concept c2 = new Concept(ontClass2.getURI(), OntoOps.getLabel(ontClass2), targetScheme);
//            selected++;
//            AlignedConcept ac = new AlignedConcept(Concept.getFullConceptUri(c1.getConceptId()), Concept.getFullConceptUri(c2.getConceptId()), Relation.Predicate.CLOSE_MATCH.value);
                similarConcepts.add(new RecommendedConcept(c2, vecSim, typeId)); // keep similarity
              }
            } // end if (test of valid concepts)

          } // concept2 loop ends
          if (!similarConcepts.isEmpty()) {
            Collections.sort(similarConcepts, new RecommendedConcept.RecommendedConceptComparator()); // sort in descending order of score
            int N = n < similarConcepts.size() ? n + 1 : similarConcepts.size(); // +1 to allow comptuing offsets to next most similar
            similarConcepts = new ArrayList<>(similarConcepts.subList(0, N));
            Concept c1 = new Concept(ontClass1.getURI(), OntoOps.getLabel(ontClass1), sourceScheme);
            candidateAlignments.put(c1, similarConcepts);
          }
        } // concept1 loop ends

        // Generate features for alignment concepts
        // FEATURES
        int conceptCount = 0;
        for (Map.Entry<Concept, ArrayList<RecommendedConcept>> entry : candidateAlignments.entrySet()) {
          Concept c1 = entry.getKey(); // source concept
//          System.out.println(++conceptCount + ". Generating features for " + c1);
          ArrayList<RecommendedConcept> selectedConcepts = entry.getValue();
//          System.out.println("Count of concepts above threshold " + selectedConcepts.size());
//        Set<String> similarConceptIds = new HashSet<String>();
//        for (int i = 0; i < selectedConcepts.size(); i++) {
//          similarConceptIds.add(selectedConcepts.get(i).getId());
//        }

          for (int i = 0; i < selectedConcepts.size() - 1; i++) {
            RecommendedConcept c2 = selectedConcepts.get(i); // target concept
            AlignedConcept alignedConcept = new AlignedConcept(c1.getId(), c2.getId(), Relation.Predicate.EXACT_MATCH.value);
            if (c2.getScore() >= t && !AlignedConcept.containsTheAlignment(seenAlignmentList, alignedConcept)) { // continue if similarity is up to threshold and alignment is not selected already
              seenAlignmentList.add(alignedConcept); // add new to list
              // GENERATE FEATURES BELOW

              // 19. Class (label) (aligned = "Y", not aligned = "N")
              String classLabel = "N";
              total++;
              // Load the reference alignment
              if (Evaluator.trueAlignment(groundTruth, alignedConcept)) {
                classLabel = "Y";
                correct++;
                if (typeId == 3)
                  monitoring.add("HERE: " + c1.getLabel() + " vs " + c2.getLabel() + " = " + c2.getScore());
              }
            }
          }
        }
        System.out.println("typeId => " + typeId);
        System.out.println("alignmentList => " + seenAlignmentList.size());
        System.out.println("correct => " + correct);
      } // end similarity type loop
    }
monitoring.forEach(System.out::println);
    System.out.println("Candidate alignments = " + total);
    System.out.println("Also candidate alignments = " + seenAlignmentList.size());
    System.out.println("Correct found = " + correct);
    System.out.println("Gold standard = " + goldstandard);
  }

  public static void main(String[] args) throws AlignmentException {
//    selectCandidates(0.5, 1);
    stringSimilarity();

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

  public static Set<String> getContextTerms(List<ConceptContext> conceptContexts, OntClass c) {
    Set<String> contextConceptTerms = new HashSet<String>();
    for (ConceptContext cc : conceptContexts) {
      if (cc.getId().equals(c.getURI())) {
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

    return contextConceptTerms;
  }

  public static String getContextTermsString(List<ConceptContext> conceptContexts, OntClass c) {
    String contextConceptTerms = "";
    for (ConceptContext cc : conceptContexts) {
      if (cc.getId().equals(c.getURI())) {
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
}
