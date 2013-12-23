package datatypeshelper.utils.doc;

import java.util.Vector;

import com.unister.semweb.ml.text.features.Term;

public class TermTokenizedText extends AbstractArrayContainingDocumentProperty implements DocumentProperty {

    private static final long serialVersionUID = -4516711449280786319L;

    protected Vector<Term> termTokenizedText;

    public TermTokenizedText() {
        termTokenizedText = new Vector<Term>();
    }

    @Override
    public Object getValue() {
        return termTokenizedText;
    }

    public Vector<Term> getTermTokenizedText() {
        return termTokenizedText;
    }

    public void setTermTokenizedText(Vector<Term> termTokenizedText) {
        this.termTokenizedText = termTokenizedText;
    }

    public void addTerm(Term term)
    {
        termTokenizedText.add(term);
    }

    @Override
    public Object[] getValueAsArray() {
        return termTokenizedText.toArray();
    }
}
