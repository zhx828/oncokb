package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.Tumor;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mskcc.cbio.oncokb.Constants.DEFAULT_REFERENCE_GENOME;

/**
 * Created by Hongxin on 12/5/16.
 */
public class SummaryUtilsTest {
    @Test
    public void testGetGeneMutationNameInVariantSummary() throws Exception {
        Gene gene = GeneUtils.getGene("ERBB2");
        assertEquals("ERBB2 amplification", SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Amplification"));
        assertEquals("ERBB2 amplification (gain)", SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "gain"));
        assertEquals(SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Amplification"), SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "amplification"));
        assertEquals(SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Amplification"), SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), " amplification"));

        assertEquals("ERBB2 deletion", SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Deletion"));
        assertEquals("ERBB2 deletion (loss)", SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "loss"));
        assertEquals(SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Deletion"), SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "deLetion"));
    }

    @Test
    public void testGetGeneMutationNameInTumorTypeSummary() throws Exception {
        Gene gene = GeneUtils.getGene("ERBB2");
        assertEquals("ERBB2-amplified", SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Amplification"));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Amplification"), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "amplification"));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Amplification"), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), " amplification"));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Amplification"), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "gain"));

        assertEquals("ERBB2 deletion", SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Deletion"));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Deletion"), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "loss"));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Deletion"), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), " loss"));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Deletion"), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "deLetion"));
    }


    @Test
    public void testRangeInframeMutationSummary() throws Exception {
        // has curated oncogenicity
        assertEquals("EGFR exon 20 insertions are likely oncogenic.", getVariantSummary("EGFR", "762_823ins"));

        // hotspot
        assertEquals("EGFR in-frame deletions occurring between amino acids 745 and 759 (745_759del) have been identified as statistically significant hotspots and are considered likely oncogenic.", getVariantSummary("EGFR", "745_759del"));

        // unknown
        assertEquals("The biologic significance of EGFR in-frame deletions occurring between amino acids 1 and 10 (1_10del) are unknown.", getVariantSummary("EGFR", "1_10del"));

        // unknown with curated in-frame indel
        assertEquals("Biological and oncogenic effects are curated for the following EGFR in-frame deletions occurring between amino acids 708 and 710 (708_710del): E709_T710delinsG, E709_T710delinsD.", getVariantSummary("EGFR", "708_710del"));

        // unknown with curated in-frame indel more than 5
        assertEquals("Biological and oncogenic effects are curated for 34 EGFR in-frame deletions occurring between amino acids 1 and 1000 (1_1000del).", getVariantSummary("EGFR", "1_1000del"));
    }

    private String getVariantSummary(String hugoSymbol, String proteinChange) {
        Gene gene = GeneUtils.getGene(hugoSymbol);

        Alteration alteration = new Alteration();
        alteration.setGene(gene);
        alteration.setAlteration(proteinChange);

        Query query = new Query();
        query.setHugoSymbol(hugoSymbol);
        query.setAlteration(proteinChange);

        // has curated oncogenicity
        Alteration matchedAlt = ApplicationContextSingleton.getAlterationBo().findExactlyMatchedAlteration(ReferenceGenome.GRCh37, alteration, AlterationUtils.getAllAlterations(ReferenceGenome.GRCh37, alteration.getGene()));
        if (matchedAlt == null) {
            AlterationUtils.annotateAlteration(alteration, proteinChange);
        } else {
            alteration = matchedAlt;
        }
        List<Alteration> relevantAlts = AlterationUtils.getRelevantAlterations(ReferenceGenome.GRCh37, alteration);
        return SummaryUtils.variantSummary(gene, alteration, relevantAlts, query);
    }
}
