package org.aksw.agdistis.datatypes;

import datatypeshelper.utils.doc.ner.NamedEntityInText;

public interface DisambiguationResults {
    
    public String findResult(NamedEntityInText namedEntity);
}
