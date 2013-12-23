package org.aksw.agdistis.util;

import org.junit.Assert;
import org.junit.Test;

public class URLHelperTest {

    @Test
    public void testPositiveExamples() {
        String examples[] = new String[] { "ftp://hans:geheim@ftp.example.org", "http://de.wikipedia.org",
                "http://de.wikipedia.org/wiki/Uniform_Resource_Locator", "mailto:hans@example.org",
                "news:alt.hypertext", "nntp:alt.hypertext", "telnet:example.org", "file:///C:/foo/bar.txt" };
        for (int i = 0; i < examples.length; i++) {
            Assert.assertTrue("It was expected that \"" + examples[i] + " is a URL.", URLHelper.isURL(examples[i]));
        }
    }

    @Test
    public void testNegativeExamples() {
        String examples[] = new String[] { "ftp.example.org", "dbpedia.org/resource/Http_cache", "Http://" };
        for (int i = 0; i < examples.length; i++) {
            Assert.assertFalse("It was expected that \"" + examples[i] + " is not a URL.", URLHelper.isURL(examples[i]));
        }
    }
}
