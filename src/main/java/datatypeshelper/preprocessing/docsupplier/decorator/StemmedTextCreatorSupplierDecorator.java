package datatypeshelper.preprocessing.docsupplier.decorator;

import com.unister.semweb.ml.text.features.Term;

import datatypeshelper.preprocessing.docsupplier.DocumentSupplier;
import datatypeshelper.utils.doc.Document;
import datatypeshelper.utils.doc.DocumentText;
import datatypeshelper.utils.doc.TermTokenizedText;

public class StemmedTextCreatorSupplierDecorator extends AbstractDocumentSupplierDecorator {

    public StemmedTextCreatorSupplierDecorator(DocumentSupplier documentSource) {
        super(documentSource);
    }

    @Override
    protected Document prepareDocument(Document document) {
        TermTokenizedText tttext = document.getProperty(TermTokenizedText.class);
        if (tttext == null) {
            throw new IllegalArgumentException("Got a document without the needed TermTokenizedText property.");
        }
        document.addProperty(new DocumentText(createStemmedText(tttext)));
        return document;
    }

    private String createStemmedText(TermTokenizedText tttext) {
        StringBuilder result = new StringBuilder();
        for (Term term : tttext.getTermTokenizedText()) {
            result.append(term.getLemma());
            result.append(' ');
        }
        return result.toString();
    }

}
