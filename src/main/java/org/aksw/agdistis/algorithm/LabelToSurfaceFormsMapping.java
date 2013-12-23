package org.aksw.agdistis.algorithm;

import java.util.Set;

@SuppressWarnings("rawtypes")
public class LabelToSurfaceFormsMapping {
    public String labels[];
    public Set surfaceForms[];

    public LabelToSurfaceFormsMapping() {
        labels = new String[0];
        surfaceForms = new Set[0];
    }

    public void add(String label, Set<String> surfaceForms) {
        String tempLabels[] = new String[labels.length + 1];
        System.arraycopy(labels, 0, tempLabels, 0, labels.length);
        tempLabels[labels.length] = label;
        labels = tempLabels;

        Set tempForms[] = new Set[this.surfaceForms.length + 1];
        System.arraycopy(this.surfaceForms, 0, tempForms, 0, this.surfaceForms.length);
        tempForms[this.surfaceForms.length] = surfaceForms;
        this.surfaceForms = tempForms;
    }
}