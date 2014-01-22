package org.aksw.agdistis.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class LiteralAnalyzerTest {
    File indexDirectory = new File("/data/m.roeder/daten/dbpedia/3.9/AGDISTIS_Index"); // "/home/rusbeck/AGDISTIS/";

    String LABEL_URI_MAPPING[][] = { { "De la Pena", "http://dbpedia.org/resource/Iván_de_la_Peña" },
            { "Ronaldo", "http://dbpedia.org/resource/Ronaldo" },
            { "Barcelona", "http://dbpedia.org/resource/FC_Barcelona" },
            { "SUPERCUP", "http://dbpedia.org/resource/Supercopa_de_España" },
            { "BARCELONA", "http://dbpedia.org/resource/FC_Barcelona" },
            { "Atletico Madrid", "http://dbpedia.org/resource/Atlético_Madrid" },
            { "ATLETICO", "http://dbpedia.org/resource/Atlético_Madrid" },
            { "Barcelona", "http://dbpedia.org/resource/FC_Barcelona" },
            { "Esnaider", "http://dbpedia.org/resource/Juan_Esnáider" },
            { "BARCELONA", "http://dbpedia.org/resource/FC_Barcelona" },
            { "Pantic", "http://dbpedia.org/resource/Milinko_Pantić" },
            { "Atletico Madrid", "http://dbpedia.org/resource/Atlético_Madrid" },
            { "Giovanni", "http://dbpedia.org/resource/Giovanni_Silva_de_Oliveira" },
            { "Spanish Supercup", "http://dbpedia.org/resource/Supercopa_de_España" },
            { "Pizzi", "http://dbpedia.org/resource/Juan_Antonio_Pizzi" } };

    @Test
    public void testSearch() {
        TripleIndex index = new TripleIndex(indexDirectory);

        int maxNumberOfCandidates = 200;
        List<Triple> result = new ArrayList<Triple>();
        boolean correctUriFound;
        for (String labelUriPair[] : LABEL_URI_MAPPING) {
            result.clear();
            result.addAll(index
                    .search(null, "http://www.w3.org/2000/01/rdf-schema#label", labelUriPair[0], maxNumberOfCandidates));
            result.addAll(index.search(null, "http://www.w3.org/2004/02/skos/core#altLabel", labelUriPair[0],
                    maxNumberOfCandidates));
            result.addAll(index.search(null, "http://xmlns.com/foaf/0.1/name", labelUriPair[0], maxNumberOfCandidates));

            correctUriFound = false;
            for (Triple t : result) {
                if (labelUriPair[1].equals(t.getSubject())) {
                    correctUriFound = true;
                    break;
                }
            }
            Assert.assertTrue("Couldn't find \"" + labelUriPair[0] + "\" --> \"" + labelUriPair[1] + "\".",
                    correctUriFound);
        }
    }
}
