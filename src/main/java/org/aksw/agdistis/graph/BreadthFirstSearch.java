package org.aksw.agdistis.graph;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.aksw.agdistis.util.Triple;
import org.aksw.agdistis.util.TripleIndex;
import org.openrdf.repository.RepositoryException;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class BreadthFirstSearch {
    private TripleIndex index;

    public BreadthFirstSearch(TripleIndex index) throws RepositoryException {
        this.index = index;
    }

    public void run(int maxDepth, DirectedSparseGraph<Node, String> graph, String edgeType, String nodeType)
            throws UnsupportedEncodingException {
        HashMap<String, Node> findNode = new HashMap<String, Node>();
        Queue<Node> q = new LinkedList<Node>();
        for (Node node : graph.getVertices()) {
            findNode.put(node.getCandidateURI(), node);
            q.add(node);
        }
        while (!q.isEmpty()) {
            Node currentNode = q.poll();
            int level = currentNode.getLevel();
            if (level < maxDepth) {
                List<Triple> outgoingNodes = index.search(currentNode.getCandidateURI(), null, null);
                for (Triple targetNode : outgoingNodes) {
                    if (targetNode.getPredicate().startsWith(edgeType) && targetNode.getObject().startsWith(nodeType)) {
                        int levelNow = level + 1;
                        Node Node = null;
                        if (findNode.containsKey(targetNode)) {
                            Node = findNode.get(targetNode);
                        } else {
                            Node = new Node(targetNode.getObject(), 0, levelNow);
                            findNode.put(targetNode.getObject(), Node);
                            q.add(Node);
                        }
                        graph.addEdge(graph.getEdgeCount() + ";" + targetNode.getPredicate(), currentNode, Node);
                    }
                }
            }
        }
    }
}
