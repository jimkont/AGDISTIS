package org.aksw.agdistis.algorithm;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.aksw.agdistis.datatypes.AgdistisResults;
import org.aksw.agdistis.datatypes.DisambiguationResults;
import org.aksw.agdistis.util.JsonEntity;
import org.aksw.agdistis.util.JsonText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import datatypeshelper.utils.doc.Document;
import datatypeshelper.utils.doc.DocumentText;
import datatypeshelper.utils.doc.ner.NamedEntitiesInText;
import datatypeshelper.utils.doc.ner.NamedEntityInText;

public class NEDSpotlightPoster implements DisambiguationAlgorithm {
    Logger log = LoggerFactory.getLogger(NEDSpotlightPoster.class);

    public NEDSpotlightPoster() {
    }

    public static void main(String args[]) throws IOException {

        NamedEntitiesInText nes = new NamedEntitiesInText(new NamedEntityInText(38, 8, "Lovelace"),
                new NamedEntityInText(62, 11, "Rob Epstein"), new NamedEntityInText(78, 16, "Jeffery Friedman"),
                new NamedEntityInText(101, 9, "Admission"),
                new NamedEntityInText(126, 10, "Paul Weitz"));

        String sentence = "Recent work includes the 2013 films ``Lovelace,'' directed by Rob Epstein and Jeffery Friedman and ``Admission,'' directed by Paul Weitz.";

        Document document = new Document();
        DocumentText text = new DocumentText(sentence);

        document.addProperty(text);
        document.addProperty(nes);

        NEDSpotlightPoster spot = new NEDSpotlightPoster();
        Map<Integer, String> positionToURL = new HashMap<Integer, String>();
        spot.doTASK(document, positionToURL);

    }

    public void doTASK(Document document, Map<Integer, String> positionToURL) throws IOException {
        log.info("" + document.getProperty(DocumentText.class));
        String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        text += "<annotation ";
        String textValue = document.getProperty(DocumentText.class).getStringValue().replace("&", "")
                .replace("\"", "'");
        text += "text=\"" + textValue + "\">\n";
        for (NamedEntityInText ne : document.getProperty(NamedEntitiesInText.class)) {
            String namedEntity = textValue.substring(ne.getStartPos(), ne.getEndPos());
            text += "\t<surfaceForm name=\"" + namedEntity + "\" offset=\"" + (ne.getStartPos()) + "\" />\n";
        }
        text += "</annotation>";
        log.info(text);
        text = URLEncoder.encode(text, "UTF-8").replace("+", "%20");
        String urlParameters = "text=" + text + "";
        // System.out.println(urlParameters);
        // String request = "http://spotlight.dbpedia.org/rest/disambiguate";
        // String request = "http://localhost:2222/rest/disambiguate";
        String request = "http://200.131.219.34:8080/dbpedia-spotlight-de/rest/disambiguate";
        // String request = "http://de.dbpedia.org/spotlight/rest/disambiguate";
        // String request = "http://ec2-54-214-114-131.us-west-2.compute.amazonaws.com:8080/rest/disambiguate";

        URL url = new URL(request);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        connection.disconnect();
        StringBuilder sb = new StringBuilder();
        // BufferedReader reader = new BufferedReader(new
        // InputStreamReader(connection.getInputStream()));
        InputStreamReader reader = new InputStreamReader(connection.getInputStream());
        char buffer[] = new char[1024];
        int length = reader.read(buffer);
        while (length > 0) {
            while (length > 0) {
                sb.append(buffer, 0, length);
                length = reader.read(buffer);
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            length = reader.read(buffer);
        }
        wr.close();
        reader.close();
        // System.out.println(URLDecoder.decode(sb.toString(), "UTF-8"));
        parseJSON(sb.toString().replace("@URI", "URI").replace("@offset", "offset"), positionToURL);

    }

    private void parseJSON(String string, Map<Integer, String> positionToURL) throws IOException {
        Reader reader = new StringReader(string);

        Gson gson = new GsonBuilder().create();
        JsonText p = gson.fromJson(reader, JsonText.class);
        for (JsonEntity ent : p.Resources) {
            log.info(ent.offset + " " + URLDecoder.decode(ent.URI, "UTF-8"));
            positionToURL.put(ent.offset, URLDecoder.decode(ent.URI, "UTF-8"));
        }

        reader.close();

    }

    @Override
    public DisambiguationResults run(Document document) {
        Map<Integer, String> positionToURL = new HashMap<Integer, String>();
        try {
            doTASK(document, positionToURL);
        } catch (IOException e) {
            log.error("Coudln't disambiguate entities in document.", e);
        }
        return new AgdistisResults(positionToURL);
    }

    @Override
    public void close() {
    }

    @Override
    public void setThreshholdTrigram(double threshholdTrigram) {
    }

    @Override
    public void setMaxDepth(int maxDepth) {
    }

    @Override
    public String getRedirect(String findResult) {
        return findResult;
    }

    @Override
    public double getThreshholdTrigram() {
        return 0;
    }

    @Override
    public int getMaxDepth() {
        return 0;
    }
}
