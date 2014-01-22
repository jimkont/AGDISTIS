package org.aksw.agdistis.datatypes.filtering;

public interface NamedEntityTypeFilter {
    public boolean fitsIntoDomain(String candidateURL);
}
