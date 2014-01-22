package org.aksw.agdistis.experiment.filtering;

import datatypeshelper.preprocessing.docsupplier.DocumentSupplier;
import datatypeshelper.preprocessing.docsupplier.decorator.AbstractDocumentSupplierDecorator;
import datatypeshelper.utils.doc.Document;
import datatypeshelper.utils.doc.ner.NamedEntitiesInText;

public class NamedEntityCountingDocumentSupplierDecorator extends AbstractDocumentSupplierDecorator {

    public NamedEntityCountingDocumentSupplierDecorator(DocumentSupplier documentSource) {
        super(documentSource);
    }

    private int namedEntityCount = 0;

    @Override
    protected Document prepareDocument(Document document) {
        NamedEntitiesInText nes = document.getProperty(NamedEntitiesInText.class);
        if (nes != null) {
            namedEntityCount += nes.getNamedEntities().size();
        }
        return document;
    }

    public int getNamedEntityCount() {
        return namedEntityCount;
    }
}
