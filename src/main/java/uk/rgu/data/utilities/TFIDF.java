package uk.rgu.data.utilities;

import com.hp.hpl.jena.ontology.OntClass;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import uk.rgu.data.oaei.OntoOps;

/**
 *
 * @author 1113938
 */
public class TFIDF {

  /**
   * Computes term frequency (case insensitive)
   * @param doc list of strings
   * @param term String represents a term
   * @return term frequency of term in document
   */
  public static double tfRaw(List<String> doc, String term) {
    double result = 0;
    for (String word : doc) {
      if (term.equalsIgnoreCase(word)) {
        result++;
      }
    }
    return (double) result; // mostly 0 or 1
  }

  /**
   * Computes term frequency (case insensitive)
   * @param doc list of strings
   * @param term String represents a term
   * @return term frequency of term in document
   */
  public static double tfLenAdjusted(List<String> doc, String term) {
    double result = 0;
    for (String word : doc) {
      if (term.equalsIgnoreCase(word)) {
        result++;
      }
    }
    return (double) result / (doc.size());
  }

  /**
   * Inverse document frequency.
   * @param docs list of list of strings represents the dataset
   * @param term String represents a term
   * @return the inverse term frequency of term in documents
   */
  public static double idf(List<List<String>> docs, String term) {
    double n = 0;
    for (List<String> doc : docs) {
      for (String word : doc) {
        if (term.equalsIgnoreCase(word)) {
          n++;
          break;
        }
      }
    }
    return Math.log((double) docs.size() / n);
  }

  /**
   * Inverse document frequency.
   * @param docs list of list of strings represents the dataset
   * @param term String represents a term
   * @return the inverse term frequency of term in documents
   */
  public static double idfSmooth(List<List<String>> docs, String term) {
    double n = 0;
    for (List<String> doc : docs) {
      for (String word : doc) {
        if (term.equalsIgnoreCase(word)) {
          n++;
          break;
        }
      }
    }
    return Math.log(1 + (double) docs.size() / n);
  }

  /**
   * @param doc a text document
   * @param docs all documents
   * @param term term
   * @return the TF-IDF of term
   */
  public static double tfIdf(List<String> doc, List<List<String>> docs, String term) {
    double tf = tfRaw(doc, term);
    double idf = idf(docs, term);
    return tf * idf;
  }

  /**
   * @param doc a text document
   * @param docs all documents
   * @param term term
   * @return the TF-IDF of term
   */
  public static double tfIdf2(List<String> doc, List<List<String>> docs, String term) {
    double tf = tfLenAdjusted(doc, term);
    double idf = idfSmooth(docs, term);
    return tf * idf;
  }

  public static double cosine_similarity(Map<String, Double> v1, Map<String, Double> v2) {
    Set<String> both = new HashSet(v1.keySet());
    both.retainAll(v2.keySet());
    double sclar = 0, norm1 = 0, norm2 = 0;
    for (String k : both) {
      sclar += v1.get(k) * v2.get(k);
    }
    for (String k : v1.keySet()) {
      norm1 += v1.get(k) * v1.get(k);
    }
    for (String k : v2.keySet()) {
      norm2 += v2.get(k) * v2.get(k);
    }
    return sclar / Math.sqrt(norm1 * norm2);
  }

//  public static double weightTerm(List<String> doc, String term) {
//    double result = 0;
//    for (String word : doc) {
//      if (term.equalsIgnoreCase(word)) {
//        result++;
//      }
//    }
//
//    return (double) 2 / (result + 1);
//  }

  public static Map<String, Double> weighStringTerms(String s, List<List<String>> coll) {
    Map<String, Double> v = new HashMap<>();
    List<String> doc = new ArrayList();
    for (String l : Arrays.asList(s.split("\\s+"))) {
      if (!Pattern.matches("\\p{Punct}", l.trim())) { // pattern matches !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
        doc.add(l.trim());
      }
    }
    for (String t : doc) {
      double w = tfIdf(doc, coll, t);
//      System.out.println("Weight of " + t + " => " + w);
      v.put(t, w);
    }

    return v;
  }

  public static Map<String, Double> weighStringTerms(Set<String> strSet, List<List<String>> coll) {
    Map<String, Double> v = new HashMap<>();
    List<String> doc = new ArrayList();
    for (String s : strSet) {
      for (String l : Arrays.asList(s.split("\\s+"))) {
        if (!Pattern.matches("\\p{Punct}", l.trim())) { // pattern matches !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
          doc.add(l.trim());
        }
      }
    }
    for (String t : doc) {
      double w = tfIdf(doc, coll, t);
//      System.out.println("Weight of " + t + " => " + w);
      v.put(t, w);
    }

    return v;
  }

  public static Map<String, Double> weighStringTerms(List<String> doc, List<List<String>> coll) {
    Map<String, Double> v = new HashMap<>();
    for (String t : doc) {
      double w = tfIdf2(doc, coll, t);
      v.put(t, w);
    }

    return v;
  }

  public static Map<String, Double> normalise(Map<String, Double> map) {
    double lenVec = Math.sqrt(map.values().stream().mapToDouble(t -> t * t).sum());
    for (Map.Entry<String, Double> entry : map.entrySet()) {
      entry.setValue(entry.getValue() / lenVec);
    }

    return map;
  }

  /**
   * Generates collection. The words in labels of a concept forms a document.
   * Collection is the list of all documents.
   *
   * @param concepts
   * @param removeStopwords
   * @return
   */
  public static List<List<String>> getCollection(List<OntClass> concepts, boolean removeStopwords) {
    List<List<String>> coll = new ArrayList<>();
    for (OntClass c : concepts) {
      coll.add(getDocument(c, removeStopwords));
    }

    return coll;
  }

  /**
   * Generates a concept document. The words in labels of a concept forms its
   * document (case insensitive).
   *
   * @param concept
   * @param removeStopwords
   * @return
   */
  public static List<String> getDocument(OntClass concept, boolean removeStopwords) {
    List<String> doc = new ArrayList<>();
    for (String s : OntoOps.getLabels(concept)) {
      for(String w : s.toLowerCase().split("\\s+")) { // add all words in labels of a concept to document
        if (removeStopwords) { // if removing stopwords
          if (!StopWords.MYSQL_STOP_WORDS.contains(w)) { // add to list if not a stopword
            doc.add(w);
          }
        } else{
          doc.add(w);
        }

      }
    }

    return doc;
  }

  /**
   * Generates collection of concept properties. The words in properties of a concept forms a document.
   * Collection is the list of all documents.
   *
   * @param concepts
   * @param removeStopwords
   * @return
   */
  public static List<List<String>> getPropertiesCollection(List<OntClass> concepts, boolean removeStopwords) {
    List<List<String>> coll = new ArrayList<>();
    for (OntClass c : concepts) {
      coll.add(getPropertiesDocument(c, removeStopwords));
    }

    return coll;
  }

  /**
   * Generates a concept's properties document. The words in properties of a concept forms its
   * document (case insensitive).
   *
   * @param concept
   * @param removeStopwords
   * @return
   */
  public static List<String> getPropertiesDocument(OntClass concept, boolean removeStopwords) {
    List<String> doc = new ArrayList<>();
    for (String s : OntoOps.getProperties(concept)) {
      for(String w : s.toLowerCase().split("\\s+")) { // add all words in labels of a concept to document
        if (removeStopwords) { // if removing stopwords
          if (!StopWords.MYSQL_STOP_WORDS.contains(w)) { // add to list if not a stopword
            doc.add(w);
          }
        } else{
          doc.add(w);
        }

      }
    }

    return doc;
  }

//  public static double weightTerm2(List<String> doc, String term) {
//    double result = 0;
//    for (String word : doc) {
//      if (term.equalsIgnoreCase(word)) {
//        result++;
//      }
//    }
//
//    return (1 - Math.log10(result));
//  }

  public static void main(String[] args) {

//    List<String> doc1 = Arrays.asList("rock", "ipsum", "dolor", "igneous", "rock", "sedimentary", "rock", "rock", "ipsum");
//    List<String> doc2 = Arrays.asList("rock", "incorrupte", "at", "igneous", "pro", "quo");
    List<List<String>> coll = new ArrayList();
//    coll.add(doc1);
//    coll.add(doc2);
//    List<String> doc3 = Arrays.asList("Has", "persius", "disputationi", "id", "simul");
//    List<List<String>> documents = Arrays.asList(doc1, doc2, doc3);
//
//    TFIDFCalculator calculator = new TFIDFCalculator();
//    double tfidf = calculator.tfIdf(doc1, documents, "igneous");
//    double tf = calculator.tf(doc1, "igneous");
//    System.out.println("TF (igneous) = " + tf);
//    System.out.println("Inverse TF (igneous) = " + (1 - tf));
//    System.out.println("TF-IDF (igneous) = " + tfidf);
//    System.out.println("Inverse TF-IDF (igneous) = " + (1 - tfidf));
//
//    tfidf = calculator.tfIdf(doc1, documents, "rock");
//    tf = calculator.tf(doc1, "rock");
//    System.out.println("TF (rock) = " + tf);
//    System.out.println("Inverse TF (rock) = " + (1 - tf));
//    System.out.println("TF-IDF (rock) = " + tfidf);
//    System.out.println("Inverse TF-IDF (rock) = " + (1 - tfidf));
//
//    tfidf = calculator.tfIdf(doc2, documents, "at");
//    tf = calculator.tf(doc2, "at");
//    System.out.println("TF (at) = " + tf);
//    System.out.println("Inverse TF (at) = " + (1 - tf));
//    System.out.println("TF-IDF (at) = " + tfidf);
//    System.out.println("Inverse TF-IDF (at) = " + (1 - tfidf));
    TFIDF calc = new TFIDF();

    List<String> doc1 = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "b");
    coll.add(doc1);
    List<String> doc2 = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "r", "s", "a", "b");
    coll.add(doc2);

    Map<String, Double> v1 = TFIDF.normalise(TFIDF.weighStringTerms(doc1, coll));
    Map<String, Double> v2 = TFIDF.normalise(TFIDF.weighStringTerms(doc2, coll));

    System.out.println("Cosine similarity => " + cosine_similarity(v1, v2));
  }
}
