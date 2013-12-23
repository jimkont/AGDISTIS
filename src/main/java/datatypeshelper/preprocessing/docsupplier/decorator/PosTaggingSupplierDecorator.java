package datatypeshelper.preprocessing.docsupplier.decorator;

import com.unister.semweb.topicmodeling.lang.postagging.PosTagger;
import com.unister.semweb.topicmodeling.lang.postagging.PosTaggingTermFilter;
import com.unister.semweb.topicmodeling.utils.doc.TermTokenizedText;

import datatypeshelper.preprocessing.docsupplier.DocumentSupplier;
import datatypeshelper.utils.doc.Document;
import datatypeshelper.utils.doc.DocumentText;

public class PosTaggingSupplierDecorator extends AbstractDocumentSupplierDecorator {

    private PosTagger postagger;

    public PosTaggingSupplierDecorator(DocumentSupplier documentSource, PosTagger postagger) {
        super(documentSource);
        this.postagger = postagger;
    }

    @Override
    protected Document prepareDocument(Document document) {
        DocumentText text = document.getProperty(DocumentText.class);
        if (text == null) {
            throw new IllegalArgumentException("Got a Document without a DocumentText property!");
        }
        TermTokenizedText ttText = postagger.tokenize(text.getText());
        if (ttText != null) {
            datatypeshelper.utils.doc.TermTokenizedText dataText = new datatypeshelper.utils.doc.TermTokenizedText();
            dataText.setTermTokenizedText(ttText.getTermTokenizedText());
            document.addProperty(dataText);
        }
        return document;
    }

    public void setPosTaggerFilter(PosTaggingTermFilter filter) {
        postagger.setFilter(filter);
    }

}
