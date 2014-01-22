package org.aksw.agdistis.algorithm;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.aksw.agdistis.datatypes.AgdistisResults;
import org.aksw.agdistis.datatypes.DisambiguationResults;
import org.aksw.agdistis.graph.Node;
import org.aksw.agdistis.util.TripleIndex;

import datatypeshelper.utils.doc.Document;
import datatypeshelper.utils.doc.ner.NamedEntitiesInText;
import datatypeshelper.utils.doc.ner.NamedEntityInText;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class NEDAlgo_wihtoutGraphTechniques implements DisambiguationAlgorithm {
    private CandidateUtil cu;
    private double threshholdTrigram;

    public NEDAlgo_wihtoutGraphTechniques(File indexDirectory, String nodeType) {
        cu = new CandidateUtil(indexDirectory, nodeType);
    }

    public NEDAlgo_wihtoutGraphTechniques(TripleIndex index, String nodeType) {
        cu = new CandidateUtil(index, nodeType);
    }

    public DisambiguationResults run(Document document) {
        NamedEntitiesInText namedEntities = document.getProperty(NamedEntitiesInText.class);
        Map<Integer, String> results = new HashMap<Integer, String>();
        DirectedSparseGraph<Node, String> graph = new DirectedSparseGraph<Node, String>();
        // 0) insert candidates into Text
        cu.insertCandidatesIntoText(graph, document, threshholdTrigram);

        // 3) store the candidate with the highest hub, highest authority ratio
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
        return new AgdistisResults(results);
    }

    public void close() {
        cu.close();
    }

    public DirectedSparseGraph<Node, String>[] getAllGraphs() {
        return null;
    }

    public CandidateUtil getCu() {
        return cu;
    }

    @Override
    public void setThreshholdTrigram(double threshholdTrigram) {
        this.threshholdTrigram = threshholdTrigram;
    }

    @Override
    public double getThreshholdTrigram() {
        return this.threshholdTrigram;
    }

    @Override
    public void setMaxDepth(int maxDepth) {
    }

    @Override
    public int getMaxDepth() {
        return 0;
    }

    @Override
    public String getRedirect(String findResult) {
        return cu.redirect(findResult);
    }
}
