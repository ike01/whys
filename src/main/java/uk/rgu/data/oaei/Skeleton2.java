package uk.rgu.data.oaei;

/*
 * $Id: Skeleton.java 1404 2010-03-31 08:53:09Z euzenat $
 *
 * Copyright (C) INRIA, 2006-2008, 2010
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */
// Alignment API classes
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;

// Alignment API implementation classes
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

// SAX standard classes
import org.xml.sax.SAXException;

// Java standard classes
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.File;
import java.net.URI;
import java.util.Properties;
import org.semanticweb.owl.align.Evaluator;

/**
 * The Skeleton of code for embeding the alignment API
 *
 * Takes two files as arguments and align them.
 */
public class Skeleton2 {

  public static void main(String[] args) {
    URI onto1 = null;
    URI onto2 = null;
    Properties params = new BasicParameters();

    try {
//	     Loading ontologies
//	    if (args.length >= 2) {
//		onto1 = new URI( args[0] );
//		onto2 = new URI( args[1] );
//	    } else {
//		System.err.println("Need two arguments to proceed");
//		return ;
//	    }

//      onto1 = new URI("file:///C:/dev/rgu/alignment_api/html/tutorial/myOnto.owl");
//      onto2 = new URI("file:///C:/dev/rgu/alignment_api/html/tutorial/edu.mit.visus.bibtex.owl");
      File f1 = new File("data/2016_conference/cmt.owl");
      File f2 = new File("data/2016_conference/Conference.owl");

      onto1 = f1.toPath().toUri();
      onto2 = f2.toPath().toUri();
      // Aligning
//      AlignmentProcess a1 = new StringDistAlignment();
      AlignmentProcess a0 = new StringEquiv();
      a0.init(onto1, onto2);
      a0.align((Alignment) null, params);
      a0.cut(.79);

      // Outputing
//      PrintWriter writer = new PrintWriter(
//              new BufferedWriter(
//                      new OutputStreamWriter(System.out, "UTF-8")), true);
//      AlignmentVisitor renderer = new RDFRendererVisitor(writer);
//      a0.render(renderer);
//      writer.flush();
//      writer.close();
      // Load the reference alignment
      AlignmentParser aparser = new AlignmentParser(0);
      Alignment reference = aparser.parse(new File("data/2016_conference/reference-alignment/cmt-Conference.rdf").toURI());

      // Evaluate alignment
      Evaluator evaluator = new PRecEvaluator(reference, a0);
      evaluator.eval(new Properties());
      System.err.println("a0: Precision: " + ((PRecEvaluator) evaluator).getPrecision() + " Recall: " + ((PRecEvaluator) evaluator).getRecall() + " F-measure: " + ((PRecEvaluator) evaluator).getFmeasure() + " over " + a0.nbCells() + " cells");
//      System.out.println("R = " + reference.nbCells());
      System.out.println("Correct = " + ((PRecEvaluator) evaluator).getCorrect());
      System.out.println("Found = " + ((PRecEvaluator) evaluator).getFound());
      System.out.println("Expected = " + ((PRecEvaluator) evaluator).getExpected());

      AlignmentProcess a1 = new StringDistAlignment();
      params.setProperty("stringFunction", "equalDistance");
      a1.init(onto1, onto2);
      a1.align((Alignment) null, params);
      a1.cut(.89);

      evaluator = new PRecEvaluator(reference, a1);
      evaluator.eval(new Properties());
      System.err.println("a1: Precision: " + ((PRecEvaluator) evaluator).getPrecision() + " Recall: " + ((PRecEvaluator) evaluator).getRecall() + " F-measure: " + ((PRecEvaluator) evaluator).getFmeasure() + " over " + a1.nbCells() + " cells");

      AlignmentProcess a2 = new StringDistAlignment();
      a2.init(onto1, onto2);
      params = new Properties();
      params.setProperty("stringFunction", "levenshteinDistance");
      a2.align((Alignment) null, params);
      a2.cut(.89);

      evaluator = new PRecEvaluator(reference, a2);
      evaluator.eval(new Properties());
      System.err.println("a2: Precision: " + ((PRecEvaluator) evaluator).getPrecision() + " Recall: " + ((PRecEvaluator) evaluator).getRecall() + " F-measure: " + ((PRecEvaluator) evaluator).getFmeasure() + " over " + a2.nbCells() + " cells");

    } catch (Exception e) {
      e.printStackTrace();
    };
  }

}
