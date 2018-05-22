package uk.rgu.data.oaei;

import uk.rgu.data.utilities.HarmonicPR;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import uk.rgu.data.model.PreAlignedConcept;
import uk.rgu.data.model.RecommendedConcept;
import uk.rgu.data.model.Related;
import uk.rgu.data.utilities.Relation;
import uk.rgu.data.utilities.TFIDF;
import uk.rgu.data.ontologyprocessor.word2vec.VectorOps;
import uk.rgu.data.utilities.Evaluator;
import uk.rgu.data.utilities.FileOps;
import uk.rgu.data.utilities.StringOps;

/**
 *
 * @author 1113938
 */
public class Hybrid extends ObjectAlignment implements AlignmentProcess {

  static String vectorModelPath = "C:/dev/rgu/word2vec/models/GoogleNews-vectors-negative300.bin.gz";
//  static String vectorModelPath = "/program-data/DGXWord2Vec/data/model/wikipedia_plain_model300_min10_iter5_custom_token.txt";
//  static String vectorModelPath = "/program-data/DGXWord2Vec/data/model/wikipedia_plain_model300_min5_iter20_custom_token.txt";
//  static String vectorModelPath = "C:/dev/rgu/word2vec/models/geo_hascontext1_model.txt";
  // Get vectors
  VectorOps vectorOps;// = new VectorOps(vectorModelPath);
  static DecimalFormat df = new DecimalFormat("#.####");

  public Hybrid(VectorOps vectorOps) {
    this.vectorOps = vectorOps;
  }

  @Override
  public void align(Alignment a, Properties prprts) throws AlignmentException {
    try {
      // Match classes
      for (Object cl2 : ontology2().getClasses()) {
        for (Object cl1 : ontology1().getClasses()) {
          // add mapping into alignment object
          addAlignCell(cl1, cl2, "=", match(cl1, cl2));
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

  public double match(Object o1, Object o2) throws AlignmentException {
//    double t = .69; // vector similarity threshold for alignment
    try {
      String s1 = ontology1().getEntityName(o1);
      String s2 = ontology2().getEntityName(o2);

      if (s1 == null || s2 == null) {
        System.out.println("a string is null");
        return 0.;
      } else {
        // Edit distance
        Set<String> set1 = new HashSet<String>();
        set1.add(s1);
        Set<String> set2 = new HashSet<String>();
        set2.add(s2);
        double levSim = StringOps.maxSimilarityByNormalizedLevenshtein(set1, set2);

        // Vector similarity
        // convert from camelCase to human-readable
        s1 = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(s1), ' ');
        s2 = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(s2), ' ');

        s1 = s1.replaceAll("\\s-\\s", "-"); // e.g. reverts "co - author" to "co-author"
        s2 = s2.replaceAll("\\s-\\s", "-");

        // prepare for lookup in word embedding vocabulary
        s1 = VectorOps.prepareStringSpaces(s1);
        s2 = VectorOps.prepareStringSpaces(s2);

//        double vecSim = vectorOps.hybridSimilarity(s1, s2);
        double vecSim = vectorOps.sentenceSimilarity(s1, s2);

//        return vecSim;
        return levSim > vecSim ? levSim : vecSim; // return greater of the 2 similarities
      }
    } catch (OntowrapException owex) {
      throw new AlignmentException("Error getting entity name", owex);
    }
  }
}
