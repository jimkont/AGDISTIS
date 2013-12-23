#!/bin/bash

java -Xmx4G -Dfile.encoding=UTF-8 -classpath .:AGDISTIS-0.0.1-SNAPSHOT-jar-with-dependencies.jar:lib/aida.jar:lib/topicmodeling.lang-0.0.2-SNAPSHOT-jar-with-dependencies.jar:lib/extended-mallet-0.0.1-SNAPSHOT-jar-with-dependencies.jar:lib/commons-4.1.1.jar org.aksw.agdistis.experiment.TextDisambiguation AGDISTIS_Index en lda-filter $1 $2 $3

java -Xmx4G -Dfile.encoding=UTF-8 -classpath .:AGDISTIS-0.0.1-SNAPSHOT-jar-with-dependencies.jar:lib/aida.jar:lib/topicmodeling.lang-0.0.2-SNAPSHOT-jar-with-dependencies.jar:lib/extended-mallet-0.0.1-SNAPSHOT-jar-with-dependencies.jar:lib/commons-4.1.1.jar org.aksw.agdistis.experiment.TextDisambiguation AGDISTIS_Index en lda-weight $1 $2 $3

java -Xmx4G -Dfile.encoding=UTF-8 -classpath .:AGDISTIS-0.0.1-SNAPSHOT-jar-with-dependencies.jar:lib/aida.jar:lib/topicmodeling.lang-0.0.2-SNAPSHOT-jar-with-dependencies.jar:lib/extended-mallet-0.0.1-SNAPSHOT-jar-with-dependencies.jar:lib/commons-4.1.1.jar org.aksw.agdistis.experiment.TextDisambiguation AGDISTIS_Index en hits $1 $2 $3

echo finished

