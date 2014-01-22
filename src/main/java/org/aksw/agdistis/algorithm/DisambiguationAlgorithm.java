package org.aksw.agdistis.algorithm;

import org.aksw.agdistis.datatypes.DisambiguationResults;

import datatypeshelper.utils.doc.Document;

public interface DisambiguationAlgorithm {

    public abstract DisambiguationResults run(Document document);

    public abstract void close();

    public abstract void setThreshholdTrigram(double threshholdTrigram);

    public abstract double getThreshholdTrigram();

    public abstract void setMaxDepth(int maxDepth);

    public abstract int getMaxDepth();

    public abstract String getRedirect(String findResult);

}