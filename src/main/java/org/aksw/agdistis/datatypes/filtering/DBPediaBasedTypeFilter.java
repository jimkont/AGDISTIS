package org.aksw.agdistis.datatypes.filtering;

import java.util.Arrays;
import java.util.HashSet;

import org.aksw.agdistis.util.TripleIndex;

public class DBPediaBasedTypeFilter extends AbstractNamedEntityTypeFilter {

    private static final String ALLOWED_TYPES[] = new String[] { "http://dbpedia.org/ontology/Place",
            "http://dbpedia.org/ontology/Person", "http://dbpedia.org/ontology/Organisation",
            "http://dbpedia.org/class/yago/YagoGeoEntity", "http://xmlns.com/foaf/0.1/Person",
            "http://dbpedia.org/ontology/WrittenWork" };

    public DBPediaBasedTypeFilter(TripleIndex index) {
        super(index, new HashSet<String>(Arrays.asList(ALLOWED_TYPES)));
    }

}
