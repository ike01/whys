package uk.rgu.data.utilities;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

/**
 *
 * @author aikay
 */
public class WordNetOps {

  private static ILexicalDatabase db = new NictWordNet();
  WuPalmer wup;

  public WordNetOps() {
    WS4JConfiguration ws4j = WS4JConfiguration.getInstance();
    ws4j.setMFS(true);
    wup = new WuPalmer(db);
  }

  /**
   * Returns the similarity between two sentences/phrases using their word
   * vectors (0 if no pair of words in sentences have vectors).
   *
   * @param s1
   * @param s2
   * @return
   */
  public double sentenceSimilarity(String s1, String s2) {
//    System.out.println("s1 : " + s1);
//    System.out.println("s2 : " + s2);
    double sum = 0.0;
    int N = s1.split("\\s+").length;
    int M = s2.split("\\s+").length;
    if (N > M) {
      for (String w1 : s1.split("\\s+")) {
        double maxSim = 0.0;
        for (String w2 : s2.split("\\s+")) {
          double sim = wup.calcRelatednessOfWords(w1, w2);
          sim = sim > 1.? 1.0 : sim; // comparing same words result in max int
          if (sim > maxSim) {
            maxSim = sim;
          } // end if
        } // end inner for
        sum = sum + maxSim;
      } // end outer for
//      if (sum > -1.0) {
//        sum = sum + 1.0; // at least one one has vectors (assuming it has to be greater than 0.0)
//      }
//      System.out.println("N = " + N);
      sum = sum / N;
    } else {
      for (String w1 : s2.split("\\s+")) {
        double maxSim = 0.0;
        for (String w2 : s1.split("\\s+")) {
          double sim = wup.calcRelatednessOfWords(w1, w2);
          sim = sim > 1.? 1.0 : sim;
          if (sim > maxSim) {
            maxSim = sim;
          } // end if
        } // end inner for
        sum = sum + maxSim;
      } // end outer for
//      if (sum > -1.0) {
//        sum = sum + 1.0; // at least one one has vectors (assuming it has to be greater than 0.0)
//      }
//      System.out.println("M = " + M);
      sum = sum / M;
    }

    return sum;
  }

  public static void main(String[] args) {
    WordNetOps wordNetOps = new WordNetOps();
    System.out.println(wordNetOps.sentenceSimilarity("today", "today"));
  }
}
