package org.aksw.agdistis.datatypes;

import java.util.Map;

import datatypeshelper.utils.doc.ner.NamedEntityInText;

public class AgdistisResults implements DisambiguationResults {

    private Map<Integer, String> results;

    public AgdistisResults(Map<Integer, String> results) {
        this.results = results;
    }

    public String findResult(NamedEntityInText namedEntity) {
        if (results.containsKey(namedEntity.getStartPos())) {
            return results.get(namedEntity.getStartPos());
        } else {
            return null;
        }
    }
}
