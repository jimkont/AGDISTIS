package org.aksw.agdistis.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.turtle.TurtleWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedirectLabelMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedirectLabelMapper.class);

    public static void main(String[] args) {
        if (args.length != 2) {
            LOGGER.error("wrong number of arguments. Usage: RedirectLabelMapper <index-directory> <new-label-file>");
            return;
        }
        TripleIndex index = new TripleIndex(new File(args[0]));
        DirectoryReader reader = index.getIreader();
        FileOutputStream fout = null;
        TurtleWriter writer = null;
        URI predicate = new URIImpl("http://www.w3.org/2000/01/rdf-schema#label");
        try {
            fout = new FileOutputStream(args[1]);
            writer = new TurtleWriter(fout);
            writer.startRDF();
            int maxDocCount = reader.maxDoc();
            Document document;
            String subjectString, predicateString;
            LiteralImpl object;
            List<Triple> redirect;
            for (int i = 0; i < maxDocCount; ++i) {
                document = reader.document(i);
                predicateString = document.get(TripleIndex.FIELD_NAME_PREDICATE);
                if (predicateString.equals("http://www.w3.org/2000/01/rdf-schema#label")) {
                    subjectString = document.get(TripleIndex.FIELD_NAME_SUBJECT);
                    object = new LiteralImpl(document.get(TripleIndex.FIELD_NAME_OBJECT_LITERAL));
                    redirect = index.search(subjectString, "http://dbpedia.org/ontology/wikiPageRedirects", null);
                    if (redirect.size() > 0) {
                        if (redirect.size() > 1) {
                            LOGGER.error("The entity "
                                    + subjectString
                                    + " has "
                                    + redirect.size()
                                    + " redirects to other entities. I will use only the first one and ignore all other.");
                        }
                        writer.handleStatement(new StatementImpl(new URIImpl(redirect.get(0).getObject()), predicate,
                                object));
                    } else {
                        writer.handleStatement(new StatementImpl(new URIImpl(subjectString), predicate, object));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't create labels file. Aborting.", e);
        } finally {
            if (index != null) {
                index.close();
            }
            if (writer != null) {
                try {
                    writer.endRDF();
                } catch (Exception e) {
                }
            }
            if (fout != null) {
                try {
                    fout.close();
                } catch (Exception e) {
                }
            }
        }
    }
}
