package uk.rgu.data.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Custom string list comparator.
 *
 * @author aikay
 */
public class StringLengthListSort implements Comparator<String> {

  /**
   * Sort list of string in descending order of length (n-grams).
   *
   * @param s1
   * @param s2
   * @return
   */
  @Override
  public int compare(String s1, String s2) {
    return StringOps.getWordLength(s2) - StringOps.getWordLength(s1);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    List<String> list = new ArrayList<String>();
//    StringLengthListSort ss = new StringLengthListSort();
    list.add("technology");
    list.add("tropical savanna climate");
    list.add("dry season");
    list.add("rock structures");
    Collections.sort(list, new StringLengthListSort());
    for (String str : list)
      System.out.println(str);
  }

}
