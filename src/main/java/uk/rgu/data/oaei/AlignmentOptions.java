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
public class AlignmentOptions {

  public static void main(String[] args) {
    URI onto1 = null;
    URI onto2 = null;
    Properties params = new Properties();

    try {
//	     Loading ontologies
//	    if (args.length >= 2) {
//		onto1 = new URI( args[0] );
//		onto2 = new URI( args[1] );
//	    } else {
//		System.err.println("Need two arguments to proceed");
//		return ;
//	    }

      onto1 = new URI("file:///C:/dev/rgu/alignment_api/html/tutorial/myOnto.owl");
      onto2 = new URI("file:///C:/dev/rgu/alignment_api/html/tutorial/edu.mit.visus.bibtex.owl");
      // Aligning
      AlignmentProcess a1 = new StringDistAlignment();
      params.setProperty("stringFunction", "smoaDistance");
      a1.init(onto1, onto2);
      a1.align((Alignment) null, params);

      AlignmentProcess a2 = new StringDistAlignment();
      a2.init(onto1, onto2);
      params = new Properties();
      params.setProperty("stringFunction", "ngramDistance");
      a2.align((Alignment) null, params);

      // Clone a1
      System.err.println( a1.nbCells() );
      BasicAlignment a3 = (BasicAlignment)(a1.clone());
      System.err.println( a3.nbCells() );

      System.err.println( a2.nbCells() );

      // Merge the two results.
      a3.ingest( a2 );
      System.err.println( a3.nbCells() );

      // Invert the alignement
      Alignment a4 = a3.inverse();
      System.err.println( a4.nbCells() );

      // Trim above .5
      a4.cut( .5 );
      System.err.println( a4.nbCells() );

//      a4.cut( .8 );
//      System.err.println( a4.nbCells() );

      // Load the reference alignment
      AlignmentParser aparser = new AlignmentParser(0);
      Alignment reference = aparser.parse( new File( "C:/dev/rgu/alignment_api/html/tutorial/refalign.rdf" ).toURI() );

      Evaluator evaluator = new PRecEvaluator( reference, a3 );
      evaluator.eval(new Properties());
      System.err.println("Precision: " + ((PRecEvaluator)evaluator).getPrecision() + " Recall: " + ((PRecEvaluator)evaluator).getRecall() + " F-measure: " + ((PRecEvaluator)evaluator).getFmeasure()+" over "+a1.nbCells()+" cells");

//      // Outputing
//      PrintWriter writer = new PrintWriter(
//              new BufferedWriter(
//                      new OutputStreamWriter(System.out, "UTF-8")), true);
//      AlignmentVisitor renderer = new RDFRendererVisitor(writer);
//      a1.render(renderer);
//      System.out.println("=====================================");
//      a2.render(renderer);
//      writer.flush();
//      writer.close();

    } catch (Exception e) {
      e.printStackTrace();
    };
  }
}
