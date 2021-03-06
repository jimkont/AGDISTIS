import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.aksw.agdistis.util.Triple;
import org.aksw.agdistis.util.TripleIndex;
import org.apache.lucene.search.spell.NGramDistance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TripleIndexTest {
	Logger log = LoggerFactory.getLogger(TripleIndexTest.class);
	private TripleIndex index;

	@Before
	public void init() {
		index = new TripleIndex(new File("E:\\project\\gsoc2014\\dbpedia3.9\\en_index"));
	}

	@After
	public void close() {
		index.close();
	}

	@Test
	public void testRedirects() {
		String candidateURL = "http://dbpedia.org/resource/JS_Bach";
		List<Triple> redirect = index.search(candidateURL, "http://dbpedia.org/ontology/wikiPageRedirects", null);
		for (Triple t : redirect) {
			log.debug(t.toString());
		}
		assertTrue(redirect.size() > 0);
		candidateURL = "http://dbpedia.org/resource/Johann_Sebastian_Bach";
		redirect = index.search(candidateURL, "http://dbpedia.org/ontology/wikiPageRedirects", null);
		assertTrue(redirect.size() == 0);
	}

	@Test
	public void testDisambiguation() {
		String candidateURL = "http://dbpedia.org/resource/JSB";
		List<Triple> dis = index.search(candidateURL, "http://dbpedia.org/ontology/wikiPageDisambiguates", null);
		assertTrue(dis.size() > 0);
		for (Triple t : dis) {
			log.debug(t.toString());
		}
	}

	@Test
	public void testType() {
		String candidateURL = "http://dbpedia.org/resource/Tim_Burton";
		List<Triple> type = index.search(candidateURL, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", null);
		assertTrue(type.size() > 0);
		for (Triple t : type) {
			log.debug(t.toString());
		}
	}

	@Test
	public void testRdfsLabel() {
		String candidateURL = "http://dbpedia.org/resource/AccessibleComputing";
		List<Triple> type = index.search(candidateURL, "http://www.w3.org/2000/01/rdf-schema#label", null);
		assertTrue(type.size() > 0);
		for (Triple t : type) {
			log.debug(t.toString());
		}
	}

	@Test
	public void testSurfaceForm() {
		String candidateURL = "http://dbpedia.org/resource/Tim_Burton";
		List<Triple> type = index.search(candidateURL, "http://www.w3.org/2004/02/skos/core#altLabel", null);
		assertTrue(type.size() > 0);
		for (Triple t : type) {
			log.debug(t.toString());
		}
	}
	@Test
	public void testMultipleTermsPerField() {
		String candidate = "New York";//"New York";
		List<Triple> type = index.search(null, "http://www.w3.org/2000/01/rdf-schema#label", candidate);
		assertTrue(type.size() > 1);
		for (Triple t : type) {
			log.debug(t.toString());
		}
	}

	@Test
	public void testSurfaceFormsDistance() {
		String candidateURL = "http://dbpedia.org/resource/Tim_Burton";
		List<Triple> label = index.search(candidateURL, "http://www.w3.org/2000/01/rdf-schema#label", null);
		List<Triple> surfaceForms = index.search(candidateURL, "http://www.w3.org/2004/02/skos/core#altLabel", null);
		log.debug(" * "+surfaceForms.size());
		NGramDistance n = new NGramDistance(3);
		for (Triple t : surfaceForms) {
			log.debug(label.get(0).getObject() + " " + t.getObject() + " : " + n.getDistance(label.get(0).getObject(), t.getObject()));
			assertTrue(n.getDistance(label.get(0).getObject(), t.getObject()) > 0);

		}
	}
}
