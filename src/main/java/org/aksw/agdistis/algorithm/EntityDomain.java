package org.aksw.agdistis.algorithm;

public enum EntityDomain {

    PERSON,
    ORGANIZATION,
    PLACE,
    UNKNOWN;

    private static final String PERSON_TYPES[] = { "http://dbpedia.org/ontology/Person",
            "http://yago-knowledge.org/resource/yagoLegalActor", "http://xmlns.com/foaf/0.1/Person" };
    private static final String ORGANIZATION_TYPES[] = { "http://dbpedia.org/ontology/Organisation",
            "http://dbpedia.org/ontology/WrittenWork", "http://yago-knowledge.org/resource/wordnet_exchange_111409538" };
    private static final String PLACE_TYPES[] = { "http://dbpedia.org/ontology/Place",
            "http://dbpedia.org/class/yago/YagoGeoEntity" };

    public static EntityDomain getDomainForType(String typeUri) {
        for (int i = 0; i < PERSON_TYPES.length; i++) {
            if (PERSON_TYPES[i].equals(typeUri)) {
                return PERSON;
            }
        }
        for (int i = 0; i < ORGANIZATION_TYPES.length; i++) {
            if (ORGANIZATION_TYPES[i].equals(typeUri)) {
                return ORGANIZATION;
            }
        }
        for (int i = 0; i < PLACE_TYPES.length; i++) {
            if (PLACE_TYPES[i].equals(typeUri)) {
                return PLACE;
            }
        }
        return UNKNOWN;
    }

}
