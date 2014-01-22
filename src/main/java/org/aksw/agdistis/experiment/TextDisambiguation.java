package org.aksw.agdistis.experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;

import org.aksw.agdistis.algorithm.CandidateUtil;
import org.aksw.agdistis.algorithm.DisambiguationAlgorithm;
import org.aksw.agdistis.algorithm.NEDAlgo_HITS;
import org.aksw.agdistis.algorithm.lda.LDABasedFilteringCandidateUtil;
import org.aksw.agdistis.algorithm.lda.LDABasedNodeConfiguratorFactory;
import org.aksw.agdistis.algorithm.lda.NEDAlgo_LDA;
import org.aksw.agdistis.algorithm.lda.NEDAlgo_selectedBasedCandidates_HITS;
import org.aksw.agdistis.util.TripleIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import datatypeshelper.io.xml.CorpusXmlReader;
import datatypeshelper.utils.corpus.Corpus;
import datatypeshelper.utils.doc.Document;
import datatypeshelper.utils.doc.ner.NamedEntitiesInText;
import datatypeshelper.utils.doc.ner.NamedEntityInText;

public class TextDisambiguation {
    private static Logger log = LoggerFactory.getLogger(TextDisambiguation.class);

    private static final String DBPEDIA_NODE_TYPE = "http://dbpedia.org/resource/";// "http://yago-knowledge.org/resource/"
    private static final String DBPEDIA_DE_NODE_TYPE = "http://de.dbpedia.org/resource/";// "http://yago-knowledge.org/resource/"
    private static final String DBPEDIA_EDGE_TYPE = "http://dbpedia.org/ontology/";// "http://yago-knowledge.org/resource/"

    private static final String YAGO_NODE_TYPE = "http://yago-knowledge.org/resource/";
    private static final String YAGO_EDGE_TYPE = "http://yago-knowledge.org/resource/";

    public static void main(String[] args) throws IOException, URISyntaxException {
        if (args.length < 8) {
            log.error("Wrong number of arguments! Usage:\n TextDisambiguation index-directory [dbpedia|yago] language-tag [hits|lda-filter|lda-weight-X] [orig|exp|both|all] startMaxDepth endMaxDepth dataset-file...");
            return;
        }

        String languageTag = args[2];// "en"; // de
        // File dataDirectory = new File("/data/r.usbeck/index_dbpedia_39_en"); // "/Users/ricardousbeck";
        // File indexDirectory = new File("/data/m.roeder/daten/dbpedia/3.9/AGDISTIS_Index"); // "/Users/ricardousbeck";

        if ((!args[4].equals("orig")) && (!args[4].equals("exp"))
                && (!args[4].equals("both") && (!args[4].equals("all")))) {
            log.error("unknown label expension option \"" + args[4] + "\". Aborting.");
            return;
        }

        int startMaxDepth, endMaxDepth;
        try {
            startMaxDepth = Integer.parseInt(args[5]);
            endMaxDepth = Integer.parseInt(args[6]);
        } catch (NumberFormatException e) {
            log.error("Couldn't parse the max depth configuration. Aborting.", e);
            return;
        }

        String nodeType;
        String edgeType;
        if (args[1].equals("dbpedia")) {
            nodeType = languageTag.equals("en") ? DBPEDIA_NODE_TYPE : DBPEDIA_DE_NODE_TYPE;
            edgeType = DBPEDIA_EDGE_TYPE;
        } else if (args[1].equals("yago")) {
            nodeType = YAGO_NODE_TYPE;
            edgeType = YAGO_EDGE_TYPE;
        } else {
            log.error("unknown knowledge base type \"" + args[1] + "\". Aborting.");
            return;
        }

        String algorithmType = args[3];
        TripleIndex index = null;
        if ((algorithmType.equals("hits")) || (algorithmType.equals("lda-filter"))
                || (algorithmType.startsWith("lda-weight-")) || (algorithmType.startsWith("trigram"))) {
            index = new TripleIndex(new File(args[0]));
        } else if ((!algorithmType.equals("spotlight")) && (!algorithmType.startsWith("aida"))) {
            log.error("unknown algorithm \"" + algorithmType + "\". Aborting.");
            return;
        }

        String testFiles[] = new String[args.length - 7];
        System.arraycopy(args, 7, testFiles, 0, testFiles.length);
        File testFile;
        DisambiguationAlgorithm algo;
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
                preprocessURLs(corpus);

                // DisambiguationAlgorithm algo = NEDAlgo_selectedBasedCandidates_HITS.createAlgorithm(indexDirectory,
                // new File(inferencerFile.toURI()), new File(pipeFile.toURI()), nodeType, edgeType);
                // DisambiguationAlgorithm algo = NEDAlgo_LDA.createAlgorithm(indexDirectory,
                // new File(inferencerFile.toURI()), new File(pipeFile.toURI()));
                // DisambiguationAlgorithm algo = new NEDAIDADisambiguator();
                // DisambiguationAlgorithm algo = new NEDSpotlightPoster();
                // algo = createAlgorithm(index, languageTag, args[3], args[4], nodeType, edgeType);
                AlgorithmCreator creator = new AlgorithmCreator(index, languageTag, algorithmType, args[4], nodeType,
                        edgeType);
                // if (algo == null) {
                // return;
                // }

                for (int maxDepth = startMaxDepth; maxDepth <= endMaxDepth; ++maxDepth) {
                    BufferedWriter bw = new BufferedWriter(new FileWriter("Test_" + testFile.getName()
                            + "_" + algorithmType + ".txt", true));
                    bw.write("input: " + testFile.getAbsolutePath() + "\n");

                    // algo.setMaxDepth(maxDepth);
                    creator.setMaxDepth(maxDepth);
                    for (double threshholdTrigram = 0.7; threshholdTrigram > 0.499; threshholdTrigram -= 0.01) {

                        // algo.setThreshholdTrigram(threshholdTrigram);
                        creator.setThreshholdTrigram(threshholdTrigram);
                        // Evaluator ev = new Evaluator(languageTag, corpus, algo);
                        WorkerBasedEvaluator ev = new WorkerBasedEvaluator(languageTag, nodeType, corpus, creator);
                        ev.fmeasure(4);
                        ev.writeFmeasureToFile(bw);
                        bw.write("\n");

                        System.gc();
                    }
                    bw.close();
                }
                // algo.close();
            } else {
                log.error("Couldn't find file \"" + testFile.getAbsolutePath() + "\". Ignoring this file.");
            }
        }

        try {
            index.close();
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public static void preprocessURLs(Corpus corpus) {
        NamedEntitiesInText nes;
        for (Document document : corpus) {
            nes = document.getProperty(NamedEntitiesInText.class);
            if (nes != null) {
                for (NamedEntityInText ne : nes.getNamedEntities()) {
                    try {
                        ne.setNamedEntity(URLDecoder.decode(ne.getNamedEntityUri(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        log.warn("Couldn't decode URL.", e);
                    }
                }
            }
        }
    }
}