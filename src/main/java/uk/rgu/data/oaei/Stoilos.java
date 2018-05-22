package uk.rgu.data.oaei;

/*
 * $Id: NewMatcher.java 1399 2010-03-30 14:07:03Z euzenat $
 *
 * Copyright (C) 2006-2010, INRIA
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
import org.semanticweb.owl.align.AlignmentException;

// Alignment API implementation classes
import fr.inrialpes.exmo.align.impl.ObjectAlignment;

import fr.inrialpes.exmo.ontowrap.OntowrapException;

import java.net.URI;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.ivml.alimo.ISub;

/**
 * The Skeleton of code for extending the alignment API
 */
public class Stoilos extends ObjectAlignment implements AlignmentProcess {

  ISub isub = new ISub();

  public Stoilos() {
  }

  /**
   * The only method to implement is align. All the resources for reading the
   * ontologies and rendering the alignment are from ObjectAlignment and its
   * superclasses: - ontology1() and ontology2() returns objects LoadedOntology
   * - addAlignCell adds a new mapping in the alignment object
   */
  @Override
  public void align(Alignment alignment, Properties param) throws AlignmentException {
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

  /*
    * *Very* simple matcher, based on equality of names (in the example, only classes and properties)
   */
  public double match(Object o1, Object o2) throws AlignmentException {
    try {
      String s1 = ontology1().getEntityName(o1);
      String s2 = ontology2().getEntityName(o2);

//      System.out.println("o1 : " + s1 + " URI : " + ontology1().getEntityURI(o1));
//      System.out.println("o2 : " + s2 + " URI : " + ontology1().getEntityURI(o2));

      if (s1 == null || s2 == null) {
        System.out.println("a string is null");
        return 0.;
      } else {
//        Set<String> set1 = new HashSet<String>();
//        set1.add(s1);
//        Set<String> set2 = new HashSet<String>();
//        set2.add(s2);

        return isub.score(s1, s2);
      }
    } catch (OntowrapException owex) {
      throw new AlignmentException("Error getting entity name", owex);
    }
  }
}