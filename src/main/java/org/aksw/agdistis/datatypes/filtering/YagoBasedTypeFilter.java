package org.aksw.agdistis.datatypes.filtering;

import java.util.Arrays;
import java.util.HashSet;

import org.aksw.agdistis.util.TripleIndex;

public class YagoBasedTypeFilter extends AbstractNamedEntityTypeFilter {

    private static final String ALLOWED_TYPES[] = new String[] { "http://yago-knowledge.org/resource/yagoGeoEntity",
            "http://yago-knowledge.org/resource/yagoLegalActor",
            "http://yago-knowledge.org/resource/wordnet_exchange_111409538" };

    public YagoBasedTypeFilter(TripleIndex index) {
        super(index, new HashSet<String>(Arrays.asList(ALLOWED_TYPES)));
    }

}
