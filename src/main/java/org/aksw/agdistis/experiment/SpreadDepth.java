package org.aksw.agdistis.experiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.aksw.agdistis.graph.Connectiveness;
import org.aksw.agdistis.graph.Node;
import org.aksw.agdistis.graph.SpreadActivation;
import org.openrdf.repository.RepositoryException;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * not used in final version of the paper
 * 
 * @author r.usbeck
 * 
 */
public class SpreadDepth {
    private static String fileToGraphs = "example.graph";
    private static boolean writeFile = false;
    private static BufferedWriter bw = null;

    public static void main(String[] args) throws UnsupportedEncodingException, IOException, RepositoryException,
            InterruptedException {
        ArrayList<String> graphNames = new ArrayList<String>();
        ArrayList<DirectedSparseGraph<Node, Integer>> graphs = new ArrayList<DirectedSparseGraph<Node, Integer>>();

        BufferedReader br = new BufferedReader(new FileReader(fileToGraphs));
        while (br.ready()) {
            graphNames.add(br.readLine());
            String edges = br.readLine();
            if (!edges.matches("")) {
                DirectedSparseGraph<Node, Integer> targetGraph = new DirectedSparseGraph<Node, Integer>();
                for (String node : edges.split("\t")) {
                    node = java.net.URLDecoder.decode(node, "UTF-8");
                    Node currentNode = new Node(node, 0, 0);
                    targetGraph.addVertex(currentNode);
                }
                graphs.add(targetGraph);
            }
        }
        br.close();

        if (writeFile) {
            bw = new BufferedWriter(new FileWriter("spreadingResults.txt"));
            bw.write("graph\tmaxDepth\tlambda(detunation)\tConnectivness");
            bw.newLine();
        }

        SpreadActivation sa = new SpreadActivation();
        double spreadActivationThreshold = 0.01;
        for (int maxDepth = 2; maxDepth <= 2; maxDepth++) {
            for (double lambda = 0.2; lambda <= 1; lambda += 0.2) {
                for (int i = 0; i < graphs.size(); i++) {
                    // let SpreadActivation run
                    sa.run(spreadActivationThreshold, maxDepth, lambda, graphs.get(i));
                    Connectiveness c = new Connectiveness();
                    double fractionOfConnectedNodes = c.meassureConnectiveness(graphs.get(i));
                    System.out.println(graphNames.get(i) + "\t" + maxDepth + "\t" + lambda + "\t"
                            + fractionOfConnectedNodes);

                    if (writeFile) {
                        bw.write(graphNames.get(i) + "\t" + maxDepth + "\t" + lambda + "\t" + fractionOfConnectedNodes);
                        bw.newLine();
                        bw.flush();
                    }

                }
            }
        }
        if (writeFile) {
            bw.close();
        }
    }

}
