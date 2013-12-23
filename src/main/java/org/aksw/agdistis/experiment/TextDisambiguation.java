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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import datatypeshelper.io.xml.CorpusXmlReader;
import datatypeshelper.utils.corpus.Corpus;
import datatypeshelper.utils.doc.Document;
import datatypeshelper.utils.doc.ner.NamedEntitiesInText;
import datatypeshelper.utils.doc.ner.NamedEntityInText;

public class TextDisambiguation {
    private static Logger log = LoggerFactory.getLogger(TextDisambiguation.class);

    private static final String NODE_TYPE = "http://dbpedia.org/resource/";// "http://yago-knowledge.org/resource/"
    private static final String EDGE_TYPE = "http://dbpedia.org/ontology/";// "http://yago-knowledge.org/resource/"

    public static void main(String[] args) throws IOException, URISyntaxException {
        if (args.length < 5) {
            log.error("Wrong number of arguments! Usage:\n TextDisambiguation index-directory language-tag [hits|lda-filter|lda-weight] [orig|exp|both] dataset-file...");
            return;
        }

        String languageTag = args[1];// "en"; // de
        // File dataDirectory = new File("/data/r.usbeck/index_dbpedia_39_en"); // "/Users/ricardousbeck";
        // File indexDirectory = new File("/data/m.roeder/daten/dbpedia/3.9/AGDISTIS_Index"); // "/Users/ricardousbeck";

        if ((!args[2].equals("hits")) && (!args[2].equals("lda-filter")) && (!args[2].equals("lda-weight"))) {
            log.error("unknown algorithm \"" + args[2] + "\". Aborting.");
            return;
        }

        if ((!args[3].equals("orig")) && (!args[3].equals("exp")) && (!args[3].equals("both"))) {
            log.error("unknown label expension option \"" + args[3] + "\". Aborting.");
            return;
        }

        String testFiles[] = new String[args.length - 4];
        System.arraycopy(args, 4, testFiles, 0, testFiles.length);
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
                algo = createAlgorithm(args);
                if (algo == null) {
                    return;
                }

                for (int maxDepth = 2; maxDepth <= 3; ++maxDepth) {
                    BufferedWriter bw = new BufferedWriter(new FileWriter("Test_" + testFile.getName()
                            + "_" + algo.getClass().getSimpleName() + ".txt", true));
                    bw.write("input: " + testFile.getAbsolutePath() + "\n");

                    algo.setMaxDepth(maxDepth);
                    for (double threshholdTrigram = 1; threshholdTrigram > 0.5; threshholdTrigram -= 0.01) {

                        algo.setThreshholdTrigram(threshholdTrigram);
                        Evaluator ev = new Evaluator(languageTag, corpus, algo);
                        ev.fmeasure();
                        ev.writeFmeasureToFile(bw);
                        bw.write("\n");

                        System.gc();
                    }
                    bw.close();
                }
                algo.close();
            } else {
                log.error("Couldn't find file \"" + testFile.getAbsolutePath() + "\". Ignoring this file.");
            }
        }
    }

    private static DisambiguationAlgorithm createAlgorithm(String args[]) {
        String languageTag = args[1];// "en"; // de
        File indexDirectory = new File(args[0]); // "/Users/ricardousbeck";

        URL inferencerFile = NEDAlgo_LDA.class.getClassLoader().getResource("wiki_" + languageTag + ".inferencer");
        if (inferencerFile == null) {
            log.error("Couldn't get \"wiki_" + languageTag + ".inferencer\" from resources. Aborting.");
        }
        URL pipeFile = NEDAlgo_LDA.class.getClassLoader().getResource("wiki_" + languageTag + ".pipe");
        if (inferencerFile == null) {
            log.error("Couldn't get \"wiki_" + languageTag + ".pipe\" from resources. Aborting.");
        }
        DisambiguationAlgorithm algo = null;
        try {
            if (args[2].equals("lda-filter")) {
                algo = NEDAlgo_selectedBasedCandidates_HITS.createAlgorithm(indexDirectory,
                        new File(inferencerFile.toURI()), new File(pipeFile.toURI()), NODE_TYPE, EDGE_TYPE);
                LDABasedFilteringCandidateUtil cu = ((NEDAlgo_selectedBasedCandidates_HITS) algo)
                        .getCandidateUtils();
                if (args[3].equals("exp")) {
                    cu.setUseHeuristicExpansion(true);
                    cu.setUseHeuristicExpansionOnly(true);
                } else if (args[3].equals("both")) {
                    cu.setUseHeuristicExpansion(true);
                    cu.setUseHeuristicExpansionOnly(false);
                } else {
                    cu.setUseHeuristicExpansion(false);
                    cu.setUseHeuristicExpansionOnly(false);
                }
            } else {
                algo = new NEDAlgo_HITS(indexDirectory, NODE_TYPE, EDGE_TYPE);
                CandidateUtil cu = ((NEDAlgo_HITS) algo).getCandidateUtils();
                cu.setUseLabelReplacePossibilities(false);
                if (args[2].equals("lda-weight")) {
                    ((NEDAlgo_HITS) algo).setNodeConfiguratorFactory(LDABasedNodeConfiguratorFactory
                            .create(cu.getIndex(), new File(inferencerFile.toURI()), new File(pipeFile.toURI())));
                }
                if (args[3].equals("exp")) {
                    cu.setUseHeuristicExpansion(true);
                    cu.setUseHeuristicExpansionOnly(true);
                } else if (args[3].equals("both")) {
                    cu.setUseHeuristicExpansion(true);
                    cu.setUseHeuristicExpansionOnly(false);
                } else {
                    cu.setUseHeuristicExpansion(false);
                    cu.setUseHeuristicExpansionOnly(false);
                }
            }
        } catch (URISyntaxException e) {
            log.error("Couldn't parse URI to inferencer or pipe. Returning null.", e);
        }
        return algo;
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