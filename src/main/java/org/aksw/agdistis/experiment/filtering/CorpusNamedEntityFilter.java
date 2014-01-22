package org.aksw.agdistis.experiment.filtering;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.aksw.agdistis.datatypes.filtering.DBPediaBasedTypeFilter;
import org.aksw.agdistis.datatypes.filtering.NamedEntityTypeFilter;
import org.aksw.agdistis.datatypes.filtering.YagoBasedTypeFilter;
import org.aksw.agdistis.util.TripleIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import datatypeshelper.io.xml.CorpusXmlWriter;
import datatypeshelper.io.xml.stream.StreamBasedXmlDocumentSupplier;
import datatypeshelper.preprocessing.ListCorpusCreator;
import datatypeshelper.preprocessing.Preprocessor;
import datatypeshelper.preprocessing.docsupplier.DocumentSupplier;
import datatypeshelper.utils.corpus.Corpus;
import datatypeshelper.utils.corpus.DocumentListCorpus;
import datatypeshelper.utils.doc.Document;

public class CorpusNamedEntityFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CorpusNamedEntityFilter.class);

    public static void main(String[] args) {
        if ((args.length < 3) || ((!args[1].equals("dbpedia")) && (!args[1].equals("yago")))) {
            System.err
                    .println("wrong number of parameters!\nUsage: CorpusNamedEntityFilter <index-dir> <dbpedia|yago> <corpus-name> [filtered-corpus-name]");
            return;
        }

        String outputFile = args.length > 3 ? args[3] : args[2] + "_filtered.xml";

        try {
            TripleIndex index = new TripleIndex(new File(args[0]));

            NamedEntityTypeFilter filter;
            if (args[1].equals("dbpedia")) {
                filter = new DBPediaBasedTypeFilter(index);
            } else {
                filter = new YagoBasedTypeFilter(index);
            }

            NamedEntityCountingDocumentSupplierDecorator counter1, counter2;
            DocumentSupplier supplier = StreamBasedXmlDocumentSupplier.createReader(args[2]);
            supplier = counter1 = new NamedEntityCountingDocumentSupplierDecorator(supplier);
            supplier = new NamedEntityFilteringDocumentSupplierDecorator(supplier, filter);
            supplier = counter2 = new NamedEntityCountingDocumentSupplierDecorator(supplier);
            Preprocessor preprocessor = new ListCorpusCreator<List<Document>>(supplier,
                    new DocumentListCorpus<List<Document>>(new ArrayList<Document>()));
            Corpus corpus = preprocessor.getCorpus();

            System.out.println("Named entity before and after filtering: " + counter1.getNamedEntityCount() + " "
                    + counter2.getNamedEntityCount());

            CorpusXmlWriter writer = new CorpusXmlWriter(new File(outputFile));
            writer.writeCorpus(corpus);
        } catch (Exception e) {
            LOGGER.error("Error while filtering corpus. Aborting.", e);
        }
    }
}
