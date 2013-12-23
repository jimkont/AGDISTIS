package org.aksw.agdistis.experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.IntArrayList;
import com.unister.semweb.commons.sort.AssociativeSort;

public class LogAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogAnalyzer.class);

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: LogAnalyzer <log-file> <output-file>");
            return;
        }

        List<String> lines = null;
        try {
            lines = FileUtils.readLines(new File(args[0]));
        } catch (IOException e) {
            LOGGER.error("Couldn't read log file.");
            return;
        }

        String label = null, extendedLabel = null, uri = null, flags = null, numberOfCandidates = null;
        List<String> analyzedEntities = new ArrayList<String>();
        IntArrayList numberOfTrueFlagsForEntities = new IntArrayList();
        int start, end, count = 0, id = 0;
        for (String line : lines) {
            // extract the log message
            start = line.indexOf('<');
            end = line.lastIndexOf('>');
            if ((start >= 0) && (end >= 0)) {
                line = line.substring(start + 1, end);
            }

            if (line.startsWith("\tLabel: ")) {
                label = line.substring(8);
            } else if (line.startsWith("\t\tnumber of candidates:")) {
                start = line.indexOf(':', 20);
                numberOfCandidates = line.substring(start + 2);
            } else if (line.matches("\"[^\"]*\" --> \"[^\"]*\" = \\[[^\\]]*\\]")) {
                end = line.indexOf('"', 1);
                extendedLabel = line.substring(1, end);
                start = line.indexOf('"', end + 1);
                ++start;
                end = line.indexOf('"', start);
                uri = line.substring(start, end);
                start = line.indexOf('[', end);
                ++start;
                end = line.indexOf(']', start);
                flags = line.substring(start, end);
                // flags = flags.replace(" ", "");
                count = 0;
                start = flags.indexOf("true");
                while (start >= 0) {
                    ++count;
                    start = flags.indexOf("true", start + 4);
                }
            } else if (line.startsWith("\tGraph size:")) {
                if (uri.contains("//dbpedia.org")) {
                    analyzedEntities.add(id + ",\"" + uri + "\",\"" + label + "\",\"" + extendedLabel + "\","
                            + numberOfCandidates + ',' + count);
                    numberOfTrueFlagsForEntities.add(count);
                    ++id;
                    // output.println('"' + uri + "\",\"" + label + "\",\"" + extendedLabel + "\","
                    // + numberOfCandidates
                    // + ',' + flags);
                }
                uri = label = extendedLabel = numberOfCandidates = flags = null;
            }
        }

        String outputLines[] = analyzedEntities.toArray(new String[analyzedEntities.size()]);
        int counts[] = numberOfTrueFlagsForEntities.toArray();
        AssociativeSort.quickSort(counts, outputLines);

        int countsHistogram[] = new int[8];
        for (int i = 0; i < counts.length; i++) {
            ++countsHistogram[counts[i]];
        }

        PrintStream output = null;
        try {
            output = new PrintStream(new File(args[1]));
            output.println("0,1,2,3,4,5,6,7");
            for (int i = 0; i < countsHistogram.length; i++) {
                if (i > 0) {
                    output.print(',');
                }
                output.print(countsHistogram[i]);
            }
            output.println();
            output.println();
            // output.println("ID,URI,Label,ext. Label,#Candidates,URI found,No Disambig.,NodeType && No Number,N-Gram Sim Ok,fits into Domain,has abstract,after TM Filtering");
            output.println("ID,URI,Label,ext. Label,#Candidates,#true flags");
            for (int i = 0; i < outputLines.length; i++) {
                output.println(outputLines[i]);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Couldn't write output.", e);
            return;
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }
}
