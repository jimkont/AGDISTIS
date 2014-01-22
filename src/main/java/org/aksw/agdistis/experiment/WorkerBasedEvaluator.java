package org.aksw.agdistis.experiment;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.aksw.agdistis.algorithm.DisambiguationAlgorithm;
import org.aksw.agdistis.datatypes.DisambiguationResults;
import org.aksw.agdistis.util.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import datatypeshelper.preprocessing.docsupplier.CorpusWrappingDocumentSupplier;
import datatypeshelper.preprocessing.docsupplier.DocumentSupplier;
import datatypeshelper.utils.corpus.Corpus;
import datatypeshelper.utils.doc.Document;
import datatypeshelper.utils.doc.DocumentText;
import datatypeshelper.utils.doc.ner.NamedEntitiesInText;
import datatypeshelper.utils.doc.ner.NamedEntityInText;

public class WorkerBasedEvaluator {
    private Logger log = LoggerFactory.getLogger(WorkerBasedEvaluator.class);
    private Corpus corpus;
    private DocumentSupplier supplier;
    private String languageTag;
    private AlgorithmCreator algoCreator;
    private String nodeType;

    private double truePositives;
    private double trueNegatives;
    private double falsePositives;
    private double falseNegatives;
    private double precision;
    private double recall;
    private double fmeasure;

    private Semaphore finishedWorkers = new Semaphore(0);

    public WorkerBasedEvaluator(String languageTag, String nodeType, Corpus corpus, AlgorithmCreator algoCreator) {
        this.corpus = corpus;
        this.languageTag = languageTag;
        this.algoCreator = algoCreator;
        this.nodeType = nodeType;
    }

    public void fmeasure() {
        fmeasure(1);
    }

    public void fmeasure(int workerCount) {
        supplier = new CorpusWrappingDocumentSupplier(corpus);

        for (int i = 0; i < workerCount; i++) {
            (new Thread(new EvaluationWorker(this, algoCreator.createAlgorithm(), languageTag, i))).start();
        }

        try {
            finishedWorkers.acquire(workerCount);
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for workers to finish.", e);
        }

        precision = truePositives / (truePositives + falsePositives);
        recall = truePositives / (truePositives + falseNegatives);
        fmeasure = 2 * ((precision * recall) / (precision + recall));
        log.error("NED preci.: " + precision + " recall: " + recall + " f1: " + fmeasure + " \t"
                + algoCreator.getThreshholdTrigram());
    }

    public void writeFmeasureToFile(BufferedWriter bw) {
        try {
            bw.write("NED maxdepth:\t" + algoCreator.getMaxDepth() + "\tTri.-Th.:\t"
                    + algoCreator.getThreshholdTrigram() + "\tpreci.:\t" + precision + "\trecall:\t" + recall
                    + "\tf1:\t" + fmeasure);
            bw.flush();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    private synchronized Document getNextDocument() {
        // return supplier.getNextDocument();
        Document document = supplier.getNextDocument();
        if ((document != null) && ((document.getDocumentId() % 10) == 0)) {
            log.debug("Document " + document.getDocumentId() + " gos to a worker.");
        }
        return document;
    }

    private void workerFinished(int id) {
        finishedWorkers.release();
        log.debug("Worker " + id + " finished.");
    }

    private String mapToDbpedia(String yagoUri) {
        if (yagoUri == null) {
            return null;
        }
        List<Triple> mapping = algoCreator.getIndex().search(yagoUri, "http://www.w3.org/2002/07/owl#sameAs",
                null);
        if (mapping.size() == 1) {
            return mapping.get(0).getObject();
        } else if (mapping.size() > 1) {
            log.error("More than one mapping \"" + yagoUri + "\"");
            return yagoUri;
        } else {
            return yagoUri;
        }
    }

    private static class EvaluationWorker implements Runnable {

        private static final Logger LOGGER = LoggerFactory.getLogger(WorkerBasedEvaluator.EvaluationWorker.class);

        private WorkerBasedEvaluator evaluator;
        private DisambiguationAlgorithm algorithm;
        private String languageTag;
        private int id;

        public EvaluationWorker(WorkerBasedEvaluator evaluator, DisambiguationAlgorithm algorithm, String languageTag,
                int id) {
            this.evaluator = evaluator;
            this.algorithm = algorithm;
            this.id = id;
            this.languageTag = languageTag;
        }

        @Override
        public void run() {
            String dbpUriWithWrongLang, dbpUriWithCorrectLang;
            if (languageTag.equals("en")) {
                dbpUriWithWrongLang = "http://de.dbpedia.org/resource/";
                dbpUriWithCorrectLang = "http://dbpedia.org/resource/";
            } else {
                dbpUriWithWrongLang = "http://dbpedia.org/resource/";
                dbpUriWithCorrectLang = "http://de.dbpedia.org/resource/";
            }
            Document document = evaluator.getNextDocument();
            DisambiguationResults results;
            while (document != null) {
                try {
                    DocumentText docText = document.getProperty(DocumentText.class);
                    if ((docText != null) && (0 < docText.getText().length())) {
                        results = algorithm.run(document);
                        NamedEntitiesInText namedEntities = document.getProperty(NamedEntitiesInText.class);
                        for (NamedEntityInText namedEntity : namedEntities) {
                            if (namedEntity.getLength() > 2) {
                                String correctVotingURL = namedEntity.getNamedEntityUri();
                                // TODO fix that if NIF is used
                                if (correctVotingURL.startsWith("rln:"))
                                    correctVotingURL = correctVotingURL.replace("rln:",
                                            "http://rdflivenews.aksw.org/resource/");
                                if (correctVotingURL.startsWith("dbpr:"))
                                    correctVotingURL = correctVotingURL
                                            .replace("dbpr:", dbpUriWithCorrectLang);
                                if (correctVotingURL.startsWith(dbpUriWithWrongLang)) {
                                    correctVotingURL = "http://aksw.org/notInWiki";
                                }
                                correctVotingURL = correctVotingURL.replace("%26", "&");
                                correctVotingURL = algorithm.getRedirect(correctVotingURL);

                                String disambiguatedURL = algorithm.getRedirect(results.findResult(namedEntity));
                                if (evaluator.nodeType.equals("http://yago-knowledge.org/resource/")) {
                                    disambiguatedURL = evaluator.mapToDbpedia(disambiguatedURL);
                                }

                                if (correctVotingURL.equals(disambiguatedURL)) {
                                    ++evaluator.truePositives;
                                    LOGGER.debug("\t tp: " + correctVotingURL + " -> " + disambiguatedURL);
                                } else if (correctVotingURL.startsWith(dbpUriWithCorrectLang)
                                        && disambiguatedURL == null) {
                                    ++evaluator.falseNegatives;
                                    LOGGER.debug("\t fn: " + correctVotingURL + " -> " + disambiguatedURL);
                                } else if ((correctVotingURL.startsWith("http://aksw.org/notInWiki") || correctVotingURL
                                        .startsWith("http://rdflivenews.aksw.org/resource/"))
                                        && disambiguatedURL == null) {
                                    ++evaluator.trueNegatives;
                                    LOGGER.debug("\t tn: " + correctVotingURL + " -> " + disambiguatedURL);
                                } else if (correctVotingURL.startsWith(dbpUriWithCorrectLang)
                                        && disambiguatedURL.startsWith(dbpUriWithCorrectLang)
                                        && !(correctVotingURL.equals(disambiguatedURL))) {
                                    ++evaluator.falsePositives;
                                    LOGGER.debug("\t fp: " + correctVotingURL + " -> " + disambiguatedURL);
                                } else if ((correctVotingURL.startsWith("http://aksw.org/notInWiki") || correctVotingURL
                                        .startsWith("http://rdflivenews.aksw.org/resource/"))
                                        && disambiguatedURL.startsWith(dbpUriWithCorrectLang)) {
                                    ++evaluator.falsePositives;
                                    LOGGER.debug("\t fp: " + correctVotingURL + " -> " + disambiguatedURL);
                                } else {
                                    LOGGER.error("STRANGE: " + correctVotingURL + " -> " + disambiguatedURL);
                                }

                            }
                        }
                    } else {
                        LOGGER.error("Text is empty!");
                    }
                } catch (Exception e) {
                    LOGGER.error("Could not process doc: " + document.getDocumentId(), e);
                }
                document = evaluator.getNextDocument();
            }
            evaluator.workerFinished(id);
        }
    }

}
