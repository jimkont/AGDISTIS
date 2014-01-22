package org.aksw.agdistis.algorithm.lda;

import java.io.File;
import java.util.List;

import org.aksw.agdistis.graph.Node;
import org.aksw.agdistis.graph.NodeConfigurator;
import org.aksw.agdistis.graph.NodeConfiguratorFactory;
import org.aksw.agdistis.util.Triple;
import org.aksw.agdistis.util.TripleIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.mallet.pipe.Pipe;
import cc.mallet.topics.LongBasedTopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.Instance;

import com.unister.semweb.topicmodeling.lang.Language;
import com.unister.semweb.topicmodeling.lang.postagging.PosTaggerFactory;
import com.unister.semweb.topicmodeling.lang.postagging.PosTaggingTermFilter;

import datatypeshelper.io.StorageHelper;
import datatypeshelper.preprocessing.SingleDocumentPreprocessor;
import datatypeshelper.preprocessing.docsupplier.DocumentSupplier;
import datatypeshelper.preprocessing.docsupplier.decorator.PosTaggingSupplierDecorator;
import datatypeshelper.preprocessing.docsupplier.decorator.StemmedTextCreatorSupplierDecorator;
import datatypeshelper.utils.doc.Document;
import datatypeshelper.utils.doc.DocumentText;
import datatypeshelper.utils.doc.ner.NamedEntitiesInText;

public class LDABasedNodeConfiguratorFactory implements NodeConfiguratorFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(LDABasedNodeConfiguratorFactory.class);

    private static final int INFERENCER_ITERATIONS = 500;
    private static final int INFERENCER_BURN_IN = 200;
    // thinning is the number of iterations until the next sample is used for the result
    private static final int INFERENCER_THINNING = 50;

    private static final String ABSTRACT_PREDICAT = "http://dbpedia.org/ontology/abstract";

    private double ldaValueWeighting = 10;

    private static Pipe pipe;

    public static LDABasedNodeConfiguratorFactory create(TripleIndex index, File inferencerFile, File pipeFile) {
        // LDA model --> inferencer
        LongBasedTopicInferencer inferencer;
        try {
            if (inferencerFile.getName().endsWith(".gz")) {
                inferencer = LongBasedTopicInferencer.readFromModelStateFile(inferencerFile);
            } else {
                inferencer = LongBasedTopicInferencer.read(inferencerFile);
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't load topic model inferencer. Returning null.", e);
            return null;
        }

        if (pipe == null) {
            readPipe(pipeFile);
            if (pipe == null) {
                LOGGER.error("Couldn't load the mallet preprocessing pipe from file. Returning null.");
                return null;
            }
        }
        final Alphabet alphabet = inferencer.getAlphabet();

        // create Preprocessing
        SingleDocumentPreprocessor preprocessor = new SingleDocumentPreprocessor();
        DocumentSupplier supplier = preprocessor;
        supplier = new PosTaggingSupplierDecorator(supplier, PosTaggerFactory.getPosTaggingStep(Language.ENG,
                new PosTaggingTermFilter() {
                    @Override
                    public boolean isTermGood(com.unister.semweb.ml.text.features.Term term) {
                        // return vocabulary.getId(term.getLemma()) != Vocabulary.WORD_NOT_FOUND;
                        return alphabet.contains(term.getLemma());
                    }
                }));
        supplier = new StemmedTextCreatorSupplierDecorator(supplier);
        preprocessor.setDocumentSupplier(supplier);

        return new LDABasedNodeConfiguratorFactory(index, preprocessor, inferencer);
    }

    private static synchronized void readPipe(File pipeFile) {
        pipe = StorageHelper.readFromFileSavely(pipeFile.getAbsolutePath());
    }

    protected static double getVectorLength(double[] vector) {
        double sum = 0;
        for (int i = 0; i < vector.length; i++) {
            sum += vector[i] * vector[i];
        }
        return Math.sqrt(sum);
    }

    protected static double getSimilarity(double[] topicVector, double topicVectorLength, double[] entityVector,
            double entityVectorLength) {
        double sum = 0;
        for (int i = 0; i < topicVector.length; i++) {
            sum += topicVector[i] * entityVector[i];
        }
        return sum / (topicVectorLength * entityVectorLength);
    }

    private TripleIndex index;
    private SingleDocumentPreprocessor preprocessor;
    private LongBasedTopicInferencer inferencer;

    public LDABasedNodeConfiguratorFactory(TripleIndex index,
            SingleDocumentPreprocessor preprocessor, LongBasedTopicInferencer inferencer) {
        this.index = index;
        this.preprocessor = preprocessor;
        this.inferencer = inferencer;
    }

    private Instance preprocess(String text) {
        Document tempDoc = new Document();
        tempDoc.addProperty(new DocumentText(text));
        preprocessor.getCorpus(tempDoc);

        // create an instance using the pipe of the training data
        text = tempDoc.getProperty(DocumentText.class).getText();
        return createInstance(text);
    }

    private static synchronized Instance createInstance(String text) {
        Instance instance = new Instance(text, "NED", "hash" + text.hashCode(), null);
        instance = pipe.instanceFrom(instance);
        return instance;
    }

    public NodeConfigurator createConfigurator(Document document) {
        return createLDABasedConfigurator(document);
    }

    public NodeConfigurator createLDABasedConfigurator(Document document) {
        NamedEntitiesInText namedEntities = document.getProperty(NamedEntitiesInText.class);
        if (namedEntities == null) {
            LOGGER.error("Got a document without the needed NamedEntitiesInText property. Returning null.");
            return null;
        }
        DocumentText docText = document.getProperty(DocumentText.class);
        if (docText == null) {
            LOGGER.error("Got a document without the needed DocumentText property. Returning null.");
            return null;
        }
        String text = docText.getText();

        Instance docAsInstance = preprocess(text);

        double topicVector[] = inferencer
                .getSampledDistribution(docAsInstance, INFERENCER_ITERATIONS, INFERENCER_THINNING, INFERENCER_BURN_IN);
        double topicVectorLength = getVectorLength(topicVector);

        return new LDABasedConfigurator(topicVector, topicVectorLength);
    }

    private class LDABasedConfigurator implements NodeConfigurator {

        private double docTopicVector[];
        private double docTopicVectorLength;

        public LDABasedConfigurator(double docTopicVector[], double docTopicVectorLength) {
            this.docTopicVector = docTopicVector;
            this.docTopicVectorLength = docTopicVectorLength;
        }

        @Override
        public void configureNode(Node currentNode) {
            String candidateURI = currentNode.getCandidateURI();
            double weight = 0;
            String entityText = getAbstract(candidateURI);
            if (entityText != null) {
                if (entityText.length() > 0) {
                    Instance entityAsInstance = preprocess(entityText);
                    double entityVector[] = inferencer
                            .getSampledDistribution(entityAsInstance, INFERENCER_ITERATIONS, INFERENCER_THINNING,
                                    INFERENCER_BURN_IN);
                    weight = getSimilarity(docTopicVector, docTopicVectorLength, entityVector,
                            getVectorLength(entityVector));
                } else {
                    LOGGER.warn("Got an empty abstract for \"" + candidateURI + "\".");
                }
            } else {
                LOGGER.warn("Couldn't get abstract for \"" + candidateURI + "\".");
            }
            currentNode.setAuthorityWeightForCalculation(1 + (ldaValueWeighting * weight));
        }

    }

    private String getAbstract(String uri) {
        List<Triple> triples;
        String entityAbstract = null;
        triples = index.search(uri, ABSTRACT_PREDICAT, null);
        // triples = abstractIndex.search(uri);
        if ((triples != null) && (triples.size() > 0)) {
            for (Triple t : triples) {
                if ("http://dbpedia.org/ontology/abstract".equals(t.getPredicate())) {
                    entityAbstract = t.getObject();
                }
            }
        }
        return entityAbstract;
    }

    public void setLdaValueWeighting(double ldaValueWeighting) {
        this.ldaValueWeighting = ldaValueWeighting;
    }
}
