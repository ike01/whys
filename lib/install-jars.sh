#!/bin/bash
# Run script on first use to install non-maven project .jars (in /lib directory) to the local repository

echo "Installing local .jar files into the maven repository..."

mvn install:install-file -DgroupId=align.api -DartifactId=align -Dpackaging=jar -Dversion=4.8 -Dfile=lib/align.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=alignsvc -Dpackaging=jar -Dversion=4.8 -Dfile=lib/alignsvc.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=ontowrap -Dpackaging=jar -Dversion=4.8 -Dfile=lib/ontowrap.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=procalign -Dpackaging=jar -Dversion=4.8 -Dfile=lib/procalign.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=hermit -Dpackaging=jar -Dversion=4.8 -Dfile=lib/hermit/hermit.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=iddl -Dpackaging=jar -Dversion=4.8 -Dfile=lib/iddl/iddl.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=jade -Dpackaging=jar -Dversion=4.8 -Dfile=lib/jade/jade.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=jwnl -Dpackaging=jar -Dversion=4.8 -Dfile=lib/jwnl/jwnl.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=ontosim -Dpackaging=jar -Dversion=4.8 -Dfile=lib/ontosim/ontosim.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=osgi-core -Dpackaging=jar -Dversion=4.8 -Dfile=lib/osgi/osgi-core.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=owlapi10-api -Dpackaging=jar -Dversion=4.8 -Dfile=lib/owlapi10/api.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=owlapi10-impl -Dpackaging=jar -Dversion=4.8 -Dfile=lib/owlapi10/impl.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=owlapi10-io -Dpackaging=jar -Dversion=4.8 -Dfile=lib/owlapi10/io.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=owlapi10-rdfapi -Dpackaging=jar -Dversion=4.8 -Dfile=lib/owlapi10/rdfapi.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=owlapi10-rdfparser -Dpackaging=jar -Dversion=4.8 -Dfile=lib/owlapi10/rdfparser.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=owlapi30-owlapi-bin -Dpackaging=jar -Dversion=4.8 -Dfile=lib/owlapi30/owlapi-bin.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=oyster -Dpackaging=jar -Dversion=4.8 -Dfile=lib/oyster/oyster.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=skosapi -Dpackaging=jar -Dversion=4.8 -Dfile=lib/skosapi/skosapi.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=slf4j-api -Dpackaging=jar -Dversion=0.1.0 -Dfile=lib/slf4j/slf4j-api.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=log4j-over-slf4j -Dpackaging=jar -Dversion=0.1.0 -Dfile=lib/slf4j/log4j-over-slf4j.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=jcl-over-slf4j -Dpackaging=jar -Dversion=0.1.0 -Dfile=lib/slf4j/jcl-over-slf4j.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=commons-cli -Dpackaging=jar -Dversion=0.1.0 -Dfile=lib/cli/commons-cli.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=commons-fileupload -Dpackaging=jar -Dversion=0.1.0 -Dfile=lib/fileupload/commons-fileupload.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=resolver -Dpackaging=jar -Dversion=0.1.0 -Dfile=lib/xerces/resolver.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=xercesImpl -Dpackaging=jar -Dversion=0.1.0 -Dfile=lib/xerces/xercesImpl.jar -DgeneratePom=true

mvn install:install-file -DgroupId=align.api -DartifactId=xml-apis -Dpackaging=jar -Dversion=0.1.0 -Dfile=lib/xerces/xml-apis.jar -DgeneratePom=true

echo "Installation complete! Make sure there are no errors."
echo "This windows closes in 5 minutes!"

sleep 300