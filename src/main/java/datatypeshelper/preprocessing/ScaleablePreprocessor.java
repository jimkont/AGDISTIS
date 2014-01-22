package datatypeshelper.preprocessing;

import datatypeshelper.utils.corpus.Corpus;

public interface ScaleablePreprocessor extends Preprocessor {

    /**
     * @return com.unister.semweb.topic_modeling.utils.Corpus
     */
    public Corpus getCorpus(int numberOfDocuments);

}
