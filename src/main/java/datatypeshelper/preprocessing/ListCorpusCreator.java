package datatypeshelper.preprocessing;

import java.util.List;

import datatypeshelper.preprocessing.docsupplier.DocumentSupplier;
import datatypeshelper.utils.corpus.Corpus;
import datatypeshelper.utils.corpus.DocumentListCorpus;
import datatypeshelper.utils.doc.Document;

public class ListCorpusCreator<T extends List<Document>> extends AbstractScaleablePreprocessor {

    public ListCorpusCreator(DocumentSupplier supplier, DocumentListCorpus<T> corpus) {
        super(supplier, corpus);
    }

    public void processDocument(Document document, Corpus corpus) {
        // if (!(DecoratedCorpusHelper.isUndecoratedCorpusInstanceOf(corpus, DocumentListCorpus.class))) {
        // throw new IllegalArgumentException("Got "
        // + corpus.getClass().getCanonicalName()
        // + " instead of the expected "
        // + DocumentListCorpus.class.getCanonicalName() + "!");
        // }
    }

    @Override
    protected Corpus getNewCorpus() {
        return super.corpus;
    }

    @Override
    protected void addDocumentToCorpus(Corpus corpus, Document document) {
        // try {
        // if (!REMOVE_DOCUMENTS_WITHOUT_TEXT || (document.getProperty(DocumentText.class) != null)) {
        // ((DocumentListCorpus<T>) DecoratedCorpusHelper.getUndecoratedCorpus(corpus)).addDocument(document);
        corpus.addDocument(document);
        // }
        // } catch (ClassCastException e) {
        // throw new IllegalArgumentException("Corpus should be of the type DocumentListCorpus.", e);
        // }
    }

}
