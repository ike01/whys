package uk.rgu.data.oaei;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import uk.rgu.data.ontologyprocessor.word2vec.VectorGenerator;
import uk.rgu.data.utilities.FileOps;
import uk.rgu.data.utilities.NLPOps;
import uk.rgu.data.utilities.StringOps;

/**
 * Generates word embedding for a pair of ontologies being aligned. Each
 * ontology has its corpus.
 *
 * @author aikay
 */
public class GenerateAlignmentModels {

  private static final Logger LOG = Logger.getLogger(GenerateAlignmentModels.class.getName());
  static NLPOps nlp = new NLPOps(); // NLP operations
  static int sentencesRetained = 0;
  static int sentencesRemoved = 0;

  public static void main(String[] args) throws IOException, Exception {
    String baseDir = "data/";
    List<AlignTrainTest> allTestcaseData = Alignment_oaei.generateConfAlignTrainTest();
    for (AlignTrainTest alignTrainTest : allTestcaseData) {
      LOG.log(Level.INFO, "PROCESSING : {0}", alignTrainTest.toString());
      File source = alignTrainTest.sourceOnto;
      File target = alignTrainTest.targetOnto;

      File sourceText = new File(baseDir + "ontotext/" + FilenameUtils.removeExtension(source.getName()) + ".txt");
      File targetText = new File(baseDir + "ontotext/" + FilenameUtils.removeExtension(target.getName()) + ".txt");

      String textPath = baseDir + "model/" + FilenameUtils.removeExtension(source.getName()) + "-" + FilenameUtils.removeExtension(target.getName()) + ".txt";
      sentencesRetained = 0;
      sentencesRemoved = 0;

      LOG.log(Level.INFO, "Processing => {0} ...", sourceText.getName());
      String fileContent = FileOps.getAllText(sourceText);
      writeSentencesToFile(textPath, fileContent, 5, 100); // minimum 5 length and maximum 100 length sentences

      LOG.log(Level.INFO, "Processing => {0} ...", targetText.getName());
      fileContent = FileOps.getAllText(targetText);
      writeSentencesToFile(textPath, fileContent, 5, 100); // minimum 5 length and maximum 100 length sentences

      System.out.println("All files have been written out!");
      System.out.println("Sentences retained: " + sentencesRetained);
      System.out.println("Sentences discarded: " + sentencesRemoved);

      LOG.info("Generating word2vec model");
      String modelPath = baseDir + "model/" + FilenameUtils.removeExtension(source.getName()) + "-" + FilenameUtils.removeExtension(target.getName()) + "_model.txt";
      VectorGenerator.createVectors(textPath, modelPath);
      LOG.log(Level.INFO, "COMPLETE : {0}", alignTrainTest.toString());
    }
  }

  public static void writeSentencesToFile(String outPath, String textString, int minSentenceLength, int maxSentenceLength) throws IOException {
    // 2. Split into sentences and write to file
    // Get all sentences
    List<String> sentences = nlp.getAllSentences(textString);

    PrintWriter pw = new PrintWriter(new FileWriter(outPath, true)); // output writer

    for (String sentence : sentences) {
      int len = StringOps.getWordLength(sentence);
      if (len >= minSentenceLength && len <= maxSentenceLength) {
        sentencesRetained++;
        pw.println(sentence);
      } else {
        sentencesRemoved++;
        LOG.info("Very long/short sentence skipped!");
        System.out.println(sentence);
      }
    }

    pw.close();
  }
}
