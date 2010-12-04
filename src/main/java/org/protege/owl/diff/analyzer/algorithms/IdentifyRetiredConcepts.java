package org.protege.owl.diff.analyzer.algorithms;

import java.util.Properties;

import org.protege.owl.diff.analyzer.AnalyzerAlgorithm;
import org.protege.owl.diff.analyzer.EntityBasedDiff;
import org.protege.owl.diff.analyzer.MatchDescription;
import org.protege.owl.diff.analyzer.MatchedAxiom;
import org.protege.owl.diff.analyzer.util.AnalyzerAlgorithmComparator;
import org.protege.owl.diff.raw.OwlDiffMap;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class IdentifyRetiredConcepts implements AnalyzerAlgorithm {
    public static final MatchDescription RETIRED = new MatchDescription("Retired", MatchDescription.MIN_SEQUENCE);

    public static final String RETIREMENT_CLASS_PROPERTY = "retirement.class";
    
    private String retirementString;
    
    public void initialise(OwlDiffMap diffMap, Properties parameters) {
    	retirementString = (String) parameters.get(RETIREMENT_CLASS_PROPERTY);
    }

    public void apply(EntityBasedDiff diff) {
        if (retirementString == null) {
            return;
        }
        MatchedAxiom retiring = null;
        for (MatchedAxiom match : diff.getAxiomMatches()) {
            if (match.isFinal()) {
                continue;
            }
            if (match.getDescription() == MatchedAxiom.AXIOM_ADDED &&
                    match.getTargetAxiom() instanceof OWLSubClassOfAxiom) {
                OWLSubClassOfAxiom subClass = (OWLSubClassOfAxiom) match.getTargetAxiom();
                if (!subClass.getSubClass().isAnonymous() && 
                        !subClass.getSuperClass().isAnonymous() &&
                        subClass.getSuperClass().asOWLClass().getIRI().toString().startsWith(retirementString)) {
                    retiring = match;
                    break;
                }
            }
        }
        if (retiring != null) {
            MatchedAxiom newRetired = new MatchedAxiom(null, retiring.getTargetAxiom(), RETIRED);
            newRetired.setFinal(true);
            diff.removeMatch(retiring);
            diff.addMatch(newRetired);
        }
    }

    public int getPriority() {
        return AnalyzerAlgorithmComparator.DEFAULT_PRIORITY + 2;
    }

}
