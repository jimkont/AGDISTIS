package org.aksw.agdistis.datatypes.filtering;

import java.util.List;
import java.util.Set;

import org.aksw.agdistis.util.Triple;
import org.aksw.agdistis.util.TripleIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractNamedEntityTypeFilter implements NamedEntityTypeFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNamedEntityTypeFilter.class);

    private TripleIndex index;
    private Set<String> allowedTypes;

    public AbstractNamedEntityTypeFilter(TripleIndex index, Set<String> allowedTypes) {
        this.index = index;
        this.allowedTypes = allowedTypes;
    }

    public boolean fitsIntoDomain(String candidateURL) {
        List<Triple> tmp = index.search(candidateURL, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", null);
        // if (tmp.isEmpty())
        // return true;
        for (Triple triple : tmp) {
            if (!triple.getObject().contains("wordnet") && !triple.getObject().contains("wikicategory"))
                LOGGER.debug("\ttype: " + triple.getObject());
            if (allowedTypes.contains(triple.getObject())) {
                return true;
            }
        }
        return false;
    }
}
