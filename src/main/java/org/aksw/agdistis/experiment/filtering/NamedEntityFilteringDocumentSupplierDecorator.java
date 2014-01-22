package org.aksw.agdistis.experiment.filtering;

import java.util.List;

import org.aksw.agdistis.datatypes.filtering.NamedEntityTypeFilter;

import datatypeshelper.preprocessing.docsupplier.DocumentSupplier;
import datatypeshelper.preprocessing.docsupplier.decorator.AbstractDocumentSupplierDecorator;
import datatypeshelper.utils.doc.Document;
import datatypeshelper.utils.doc.ner.NamedEntitiesInText;
import datatypeshelper.utils.doc.ner.NamedEntityInText;

public class NamedEntityFilteringDocumentSupplierDecorator extends AbstractDocumentSupplierDecorator {

    private NamedEntityTypeFilter filter;

    public NamedEntityFilteringDocumentSupplierDecorator(DocumentSupplier documentSource, NamedEntityTypeFilter filter) {
        super(documentSource);
        this.filter = filter;
    }

    @Override
    protected Document prepareDocument(Document document) {
        NamedEntitiesInText nes = document.getProperty(NamedEntitiesInText.class);
        if (nes != null) {
            filter(nes);
        }
        return document;
    }

    private void filter(NamedEntitiesInText nes) {
        List<NamedEntityInText> entities = nes.getNamedEntities();
        for (int i = entities.size() - 1; i >= 0; --i) {
            if (!filter.fitsIntoDomain(entities.get(i).getNamedEntityUri())) {
                entities.remove(i);
            }
        }
    }
}
