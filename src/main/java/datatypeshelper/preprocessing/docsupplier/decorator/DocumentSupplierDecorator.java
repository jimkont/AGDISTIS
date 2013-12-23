package datatypeshelper.preprocessing.docsupplier.decorator;

import datatypeshelper.preprocessing.docsupplier.DocumentSupplier;

public interface DocumentSupplierDecorator extends DocumentSupplier {

    public DocumentSupplier getDecoratedDocumentSupplier();

    public void setDecoratedDocumentSupplier(DocumentSupplier supplier);
}
