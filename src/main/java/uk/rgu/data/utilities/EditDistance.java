package uk.rgu.data.utilities;

import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;

/**
 *
 * @author 1113938
 */
public class EditDistance {

  /**
   * Computes the Levenshtein (edit) distance between two strings.
   * Source: http://rosettacode.org/wiki/Levenshtein_distance#Java
   *
   * @param a First string.
   * @param b Second string.
   * @return Levenshtein distance.
   */
  public static int levenshtein(String a, String b) {
    a = a.toLowerCase();
    b = b.toLowerCase();
    // i == 0
    int[] costs = new int[b.length() + 1];
    for (int j = 0; j < costs.length; j++) {
      costs[j] = j;
    }
    for (int i = 1; i <= a.length(); i++) {
      // j == 0; nw = lev(i - 1, j)
      costs[0] = i;
      int nw = i - 1;
      for (int j = 1; j <= b.length(); j++) {
        int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
        nw = costs[j];
        costs[j] = cj;
      }
    }
    return costs[b.length()];
  }

  public static void main(String[] args) {
    String[] data = {"ubiquitinating", "ubiquitin", "abcdef", "ghi", "proceedings", "in proceedings"};
    System.out.println("levenshtein");
    for (int i = 0; i < data.length; i += 2) {
      System.out.println("distance(" + data[i] + ", " + data[i + 1] + ") = " + levenshtein(data[i], data[i + 1]));
    }
    System.out.println("normalized levenshtein");
    NormalizedLevenshtein l = new NormalizedLevenshtein();
    for (int i = 0; i < data.length; i += 2) {
      System.out.println("distance(" + data[i] + ", " + data[i + 1] + ") = " + l.distance(data[i], data[i + 1]) + " | similarity(" + data[i] + ", " + data[i + 1] + ") = " + l.similarity(data[i], data[i + 1]));
    }
    System.out.println("Jaro-Winkler");
    JaroWinkler jw = new JaroWinkler();
    for (int i = 0; i < data.length; i += 2) {
      System.out.println("distance(" + data[i] + ", " + data[i + 1] + ") = " + jw.distance(data[i], data[i + 1]) + " | similarity(" + data[i] + ", " + data[i + 1] + ") = " + jw.similarity(data[i], data[i + 1]));
    }
  }

}
