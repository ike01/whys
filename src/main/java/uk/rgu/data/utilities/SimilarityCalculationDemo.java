package uk.rgu.data.utilities;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

public class SimilarityCalculationDemo {

  private static ILexicalDatabase db = new NictWordNet();

  /*
	//available options of metrics
	private static RelatednessCalculator[] rcs = { new HirstStOnge(db),
			new LeacockChodorow(db), new Lesk(db), new WuPalmer(db),
			new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db) };
   */
  private static double compute(String word1, String word2) {

    double s = new WuPalmer(db).calcRelatednessOfWords(word1, word2);
    return s;
  }

  public static void main(String[] args) {
    WS4JConfiguration ws4j = WS4JConfiguration.getInstance();
    ws4j.setMFS(true);
    WuPalmer wup = new WuPalmer(db);
//    System.setProperty("wordnet.database.dir", "C:/Program Files (x86)/WordNet/2.1/dict/");
    String[] words = {"add", "get", "filter", "remove", "check", "find", "collect", "create"};

    for (int i = 0; i < words.length - 1; i++) {
      for (int j = i + 1; j < words.length; j++) {
//        double distance = compute(words[i], words[j]);
        double s = wup.calcRelatednessOfWords(words[i], words[j]);
        System.out.println(words[i] + " -  " + words[j] + " = " + s);
      }
    }



//    JWS ws = new JWS("./lib", "3.0");
//  Resnik res = ws.getResnik();
//  TreeMap<String, Double> scores1 = res.res(word1, word2, partOfSpeech);
//  for(Entry<String, Double> e: scores1.entrySet())
//      System.out.println(e.getKey() + "\t" + e.getValue());
//  System.out.println("\nhighest score\t=\t" + res.max(word1, word2, partOfSpeech) + "\n\n\n");
  }
}
