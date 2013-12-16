package org.aksw.agdistis.experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.aksw.agdistis.algorithm.DisambiguationAlgorithm;
import org.aksw.agdistis.algorithm.NEDAlgo_selectedBasedCandidates_HITS;
import org.aksw.agdistis.algorithm.lda.NEDAlgo_LDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import datatypeshelper.io.xml.CorpusXmlReader;
import datatypeshelper.utils.corpus.Corpus;

public class TextDisambiguation {
    private static Logger log = LoggerFactory.getLogger(TextDisambiguation.class);

    public static void main(String[] args) throws IOException, URISyntaxException {
        if (args.length < 3) {
            log.error("Wrong number of arguments! Usage:\n TextDisambiguation index-directory language-tag dataset-file...");
            return;
        }

        String languageTag = args[1];// "en"; // de
        // File dataDirectory = new File("/data/r.usbeck/index_dbpedia_39_en"); // "/Users/ricardousbeck";
        // File indexDirectory = new File("/data/m.roeder/daten/dbpedia/3.9/AGDISTIS_Index"); // "/Users/ricardousbeck";
        File indexDirectory = new File(args[0]); // "/Users/ricardousbeck";
        URL inferencerFile = NEDAlgo_LDA.class.getClassLoader().getResource("wiki_" + languageTag + ".inferencer");
        if (inferencerFile == null) {
            log.error("Couldn't get \"wiki_" + languageTag + ".inferencer\" from resources. Aborting.");
        }
        URL pipeFile = NEDAlgo_LDA.class.getClassLoader().getResource("wiki_" + languageTag + ".pipe");
        if (inferencerFile == null) {
            log.error("Couldn't get \"wiki_" + languageTag + ".pipe\" from resources. Aborting.");
        }
        String nodeType = "http://dbpedia.org/resource/";// "http://yago-knowledge.org/resource/"
        String edgeType = "http://dbpedia.org/ontology/";// "http://yago-knowledge.org/resource/"

        String testFiles[] = new String[args.length - 2];
        System.arraycopy(args, 2, testFiles, 0, testFiles.length);
        File testFile;
        // for (String TestFile : new String[] { "datasets/reuters.xml", "datasets/500newsgoldstandard.xml" }) {
        for (String testInput : testFiles) {
            // "german_corpus_new.xml"
            // "datasets/test.xml", "datasets/AIDACorpus.xml"
            testFile = new File(testInput);
            if (testFile.exists()) {
                log.info("Starting test with file \"" + testFile.getAbsolutePath() + "\"...");
                CorpusXmlReader reader = new CorpusXmlReader(testFile);
                Corpus corpus = reader.getCorpus();
                log.info("Corpus size: " + corpus.getNumberOfDocuments());

                DisambiguationAlgorithm algo = NEDAlgo_selectedBasedCandidates_HITS.createAlgorithm(indexDirectory,
                        new File(inferencerFile.toURI()), new File(pipeFile.toURI()), nodeType, edgeType);
                // DisambiguationAlgorithm algo = NEDAlgo_LDA.createAlgorithm(indexDirectory,
                // new File(inferencerFile.toURI()), new File(pipeFile.toURI()));
                // DisambiguationAlgorithm algo = new NEDAlgo_HITS(dataDirectory, nodeType, edgeType);
                // DisambiguationAlgorithm algo = new NEDAIDADisambiguator();
                // DisambiguationAlgorithm algo = new NEDSpotlightPoster();

                // for (int maxDepth = 1; maxDepth <= 3; ++maxDepth) {
                BufferedWriter bw = new BufferedWriter(new FileWriter("Test_" + testFile.getName()
                        + ".txt", true));
                bw.write("input: " + testFile.getAbsolutePath() + "\n");

                // algo.setMaxDepth(maxDepth);
                // for (double threshholdTrigram = 1; threshholdTrigram > 0.0; threshholdTrigram -= 0.01) {
                // algo.setThreshholdTrigram(threshholdTrigram);

                Evaluator ev = new Evaluator(languageTag, corpus, algo);
                ev.fmeasure();
                ev.writeFmeasureToFile(bw);

                System.gc();
                // }
                bw.close();
                // }
                algo.close();
            } else {
                log.error("Couldn't find file \"" + testFile.getAbsolutePath() + "\". Ignoring this file.");
            }
        }
    }
}