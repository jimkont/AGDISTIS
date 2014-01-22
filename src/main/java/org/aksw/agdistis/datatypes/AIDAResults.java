package org.aksw.agdistis.datatypes;

import mpi.aida.AidaManager;
import mpi.aida.data.DisambiguationResults;
import mpi.aida.data.ResultEntity;
import mpi.aida.data.ResultMention;
import datatypeshelper.utils.doc.ner.NamedEntityInText;

public class AIDAResults implements org.aksw.agdistis.datatypes.DisambiguationResults {
    private DisambiguationResults results;

    public AIDAResults(DisambiguationResults results) {
        this.results = results;
    }

    public String findResult(NamedEntityInText namedEntity) {
        if (results == null) {
            return null;
        }
        // Print the disambiguation results.
        int startPos = namedEntity.getStartPos();
        for (ResultMention rm : results.getResultMentions()) {
            ResultEntity re = results.getBestEntity(rm);
            if (rm.getCharacterOffset() == startPos) {
                String wikiURL = AidaManager.getWikipediaUrl(re);
                return wikiURL.replace("http://en.wikipedia.org/wiki", "http://dbpedia.org/resource");
            }
        }
        return null;
    }
}
