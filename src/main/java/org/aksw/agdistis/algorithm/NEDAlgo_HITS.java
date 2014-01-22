package org.aksw.agdistis.algorithm;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.aksw.agdistis.datatypes.AgdistisResults;
import org.aksw.agdistis.datatypes.DisambiguationResults;
import org.aksw.agdistis.graph.BreadthFirstSearch;
import org.aksw.agdistis.graph.HITS;
import org.aksw.agdistis.graph.Node;
import org.aksw.agdistis.graph.NodeConfiguratorFactory;
import org.aksw.agdistis.util.TripleIndex;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import datatypeshelper.utils.doc.Document;
import datatypeshelper.utils.doc.ner.NamedEntitiesInText;
import datatypeshelper.utils.doc.ner.NamedEntityInText;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class NEDAlgo_HITS implements DisambiguationAlgorithm {

    private static final Logger log = LoggerFactory.getLogger(NEDAlgo_HITS.class);

    private static final int NUMBER_OF_HITS_ITERATIONS = 20;

    private String edgeType = null;
    private String nodeType = null;
    private CandidateUtil cu = null;
    private TripleIndex index = null;
    // needed for the experiment about which properties increase accuracy
    private HashSet<String> restrictedEdges = null;
    private double threshholdTrigram = 0.87;
    private int maxDepth = 2;
    private NodeConfiguratorFactory nodeConfiguratorFactory = null;

    public NEDAlgo_HITS(File indexDirectory, String nodeType, String edgeType) {
        this.nodeType = nodeType;
        this.edgeType = edgeType;
        this.cu = new CandidateUtil(indexDirectory, nodeType);
        this.index = cu.getIndex();
    }

    public NEDAlgo_HITS(TripleIndex index, String nodeType, String edgeType) {
        this.nodeType = nodeType;
        this.edgeType = edgeType;
        this.cu = new CandidateUtil(index, nodeType);
        this.index = index;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.aksw.agdistis.algorithm.DisambiguationAlgorithm#run(datatypeshelper
     * .utils.doc.Document, double, int)
     */
    @Override
    public DisambiguationResults run(Document document) {
        NamedEntitiesInText namedEntities = document.getProperty(NamedEntitiesInText.class);
        Map<Integer, String> results = new HashMap<Integer, String>();
        DirectedSparseGraph<Node, String> graph = new DirectedSparseGraph<Node, String>();

        try {
            // 0) insert candidates into Text
            log.debug("\tinsert candidates");
            if (nodeConfiguratorFactory != null) {
                cu.insertCandidatesIntoText(graph, document, threshholdTrigram,
                        nodeConfiguratorFactory.createConfigurator(document));
            } else {
                cu.insertCandidatesIntoText(graph, document, threshholdTrigram);
            }

            // 1) let spread activation/ breadth first searc run
            log.info("\tGraph size before BFS: " + graph.getVertexCount());
            BreadthFirstSearch bfs = new BreadthFirstSearch(index);
            bfs.run(maxDepth, graph, edgeType, nodeType);
            log.info("\tGraph size after BFS: " + graph.getVertexCount());
            // double lambda = 0.2;
            // double spreadActivationThreshold = 0.01;
            // SpreadActivation sa = new SpreadActivation();
            // sa.run(spreadActivationThreshold, maxDepth, lambda, graph);

            // 2) let HITS run
            log.debug("\trun HITS");
            HITS h = new HITS();
            h.runHits(graph, NUMBER_OF_HITS_ITERATIONS);

            // 3) store the candidate with the highest hub, highest authority
            // ratio
            log.debug("\torder results");
            ArrayList<Node> orderedList = new ArrayList<Node>();
            orderedList.addAll(graph.getVertices());
            Collections.sort(orderedList);
            for (NamedEntityInText entity : namedEntities) {
                for (int i = 0; i < orderedList.size(); i++) {
                    Node m = orderedList.get(i);
                    // there can be one node (candidate) for two labels
                    if (m.containsId(entity.getStartPos())) {
                        if (!results.containsKey(entity.getStartPos())) {
                            results.put(entity.getStartPos(), m.getCandidateURI());
                            break;
                        }
                    }

                }
            }

        } catch (RepositoryException e) {
            log.error(e.getLocalizedMessage());
        } catch (InterruptedException e) {
            log.error(e.getLocalizedMessage());
        } catch (UnsupportedEncodingException e) {
            log.error(e.getLocalizedMessage());
        }
        return new AgdistisResults(results);
    }

    @Override
    public void close() {
        cu.close();
    }

    public void restrictEdgesTo(HashSet<String> restrictedEdges) {
        this.restrictedEdges = restrictedEdges;
    }

    public void setThreshholdTrigram(double threshholdTrigram) {
        this.threshholdTrigram = threshholdTrigram;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public String getRedirect(String findResult) {
        return cu.redirect(findResult);
    }

    @Override
    public double getThreshholdTrigram() {
        return this.threshholdTrigram;
    }

    public CandidateUtil getCandidateUtils() {
        return cu;
    }

    public void setNodeConfiguratorFactory(NodeConfiguratorFactory nodeConfiguratorFactory) {
        this.nodeConfiguratorFactory = nodeConfiguratorFactory;
    }

    public NodeConfiguratorFactory getNodeConfiguratorFactory() {
        return nodeConfiguratorFactory;
    }

    @Override
    public int getMaxDepth() {
        return maxDepth;
    }
}