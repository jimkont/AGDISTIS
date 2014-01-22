package org.aksw.agdistis.experiment;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.aksw.agdistis.algorithm.CandidateUtil;
import org.aksw.agdistis.algorithm.DisambiguationAlgorithm;
import org.aksw.agdistis.algorithm.NEDAIDADisambiguator;
import org.aksw.agdistis.algorithm.NEDAlgo_HITS;
import org.aksw.agdistis.algorithm.NEDAlgo_wihtoutGraphTechniques;
import org.aksw.agdistis.algorithm.NEDSpotlightPoster;
import org.aksw.agdistis.algorithm.lda.LDABasedFilteringCandidateUtil;
import org.aksw.agdistis.algorithm.lda.LDABasedNodeConfiguratorFactory;
import org.aksw.agdistis.algorithm.lda.NEDAlgo_LDA;
import org.aksw.agdistis.algorithm.lda.NEDAlgo_selectedBasedCandidates_HITS;
import org.aksw.agdistis.util.TripleIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlgorithmCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlgorithmCreator.class);

    private TripleIndex index;
    private String languageTag;
    private String algorithm;
    private String expansion;
    private String nodeType;
    private String edgeType;
    private double threshholdTrigram = -1;
    private int maxDepth = -1;

    public AlgorithmCreator(TripleIndex index, String languageTag, String algorithm,
            String expansion, String nodeType, String edgeType) {
        this.index = index;
        this.languageTag = languageTag;
        this.algorithm = algorithm;
        this.expansion = expansion;
        this.nodeType = nodeType;
        this.edgeType = edgeType;
    }

    public DisambiguationAlgorithm createAlgorithm() {
        URL inferencerFile = NEDAlgo_LDA.class.getClassLoader().getResource("wiki_" + languageTag + ".inferencer");
        if (inferencerFile == null) {
            LOGGER.error("Couldn't get \"wiki_" + languageTag + ".inferencer\" from resources. Aborting.");
        }
        URL pipeFile = NEDAlgo_LDA.class.getClassLoader().getResource("wiki_" + languageTag + ".pipe");
        if (inferencerFile == null) {
            LOGGER.error("Couldn't get \"wiki_" + languageTag + ".pipe\" from resources. Aborting.");
        }
        DisambiguationAlgorithm algo = null;
        try {
            if (algorithm.equals("lda-filter")) {
                algo = NEDAlgo_selectedBasedCandidates_HITS.createAlgorithm(index,
                        new File(inferencerFile.toURI()), new File(pipeFile.toURI()), nodeType, edgeType);
                LDABasedFilteringCandidateUtil cu = ((NEDAlgo_selectedBasedCandidates_HITS) algo)
                        .getCandidateUtils();
                if (expansion.equals("exp")) {
                    cu.setUseHeuristicExpansion(true);
                    cu.setUseHeuristicExpansionOnly(true);
                } else if (expansion.equals("both")) {
                    cu.setUseHeuristicExpansion(true);
                    cu.setUseHeuristicExpansionOnly(false);
                } else {
                    cu.setUseHeuristicExpansion(false);
                    cu.setUseHeuristicExpansionOnly(false);
                }
            } else if (algorithm.equals("spotlight")) {
                algo = new NEDSpotlightPoster();
            } else if (algorithm.equals("aida")) {
                algo = new NEDAIDADisambiguator();
            } else if (algorithm.equals("trigram")) {
                algo = new NEDAlgo_wihtoutGraphTechniques(index, nodeType);
            } else {
                algo = new NEDAlgo_HITS(index, nodeType, edgeType);
                CandidateUtil cu = ((NEDAlgo_HITS) algo).getCandidateUtils();
                if (algorithm.startsWith("lda-weight-")) {
                    LDABasedNodeConfiguratorFactory configurator = LDABasedNodeConfiguratorFactory
                            .create(cu.getIndex(), new File(inferencerFile.toURI()), new File(pipeFile.toURI()));
                    configurator.setLdaValueWeighting(Double.parseDouble(algorithm.substring(11)));
                    ((NEDAlgo_HITS) algo).setNodeConfiguratorFactory(configurator);
                }
                if (expansion.equals("exp")) {
                    cu.setUseHeuristicExpansion(true);
                    cu.setUseHeuristicExpansionOnly(true);
                    cu.setUseLabelReplacePossibilities(false);
                } else if (expansion.equals("both")) {
                    cu.setUseHeuristicExpansion(true);
                    cu.setUseHeuristicExpansionOnly(false);
                    cu.setUseLabelReplacePossibilities(false);
                } else if (expansion.equals("all")) {
                    cu.setUseHeuristicExpansion(true);
                    cu.setUseHeuristicExpansionOnly(false);
                    cu.setUseLabelReplacePossibilities(true);
                } else {
                    cu.setUseHeuristicExpansion(false);
                    cu.setUseHeuristicExpansionOnly(false);
                    cu.setUseLabelReplacePossibilities(false);
                }
            }

            if (threshholdTrigram > 0) {
                algo.setThreshholdTrigram(threshholdTrigram);
            }
            if (maxDepth > 0) {
                algo.setMaxDepth(maxDepth);
            }
        } catch (URISyntaxException e) {
            LOGGER.error("Couldn't parse URI to inferencer or pipe. Returning null.", e);
        }
        return algo;
    }

    public double getThreshholdTrigram() {
        return threshholdTrigram;
    }

    public void setThreshholdTrigram(double threshholdTrigram) {
        this.threshholdTrigram = threshholdTrigram;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public TripleIndex getIndex() {
        return index;
    }

}
