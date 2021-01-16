package org.mskcc.cbio.oncokb.importer;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class KinaseAnalysis {
    private KinaseAnalysis() {
        throw new AssertionError();
    }


    public static void main(String[] args) throws IOException {
        Set<AnalysisResultRow> resultRows = new LinkedHashSet<>();
        CacheUtils.getAllGenes().stream().filter(gene -> {
            Set<Evidence> summaryEvidences = EvidenceUtils.getEvidenceByGeneAndEvidenceTypes(gene, Collections.singleton(EvidenceType.GENE_SUMMARY));
            // evidences should only have one item, but just in case
            if (!summaryEvidences.isEmpty()) {
                String summary = summaryEvidences.iterator().next().getDescription();
                return summary.toLowerCase().contains("kinase");
            } else {
                return false;
            }
        }).forEach(gene -> {
            AlterationUtils.getAllAlterations(ReferenceGenome.GRCh37, gene).stream().filter(alteration -> !AlterationUtils.isInferredAlterations(alteration.getAlteration()) && !AlterationUtils.isPositionedAlteration(alteration)).forEach(alteration -> {
                Query query = new Query();
                query.setHugoSymbol(alteration.getGene().getHugoSymbol());
                query.setAlteration(alteration.getAlteration());
                IndicatorQueryResp resp = IndicatorUtils.processQuery(query, null, false, null);
                Oncogenicity oncogenicity = Oncogenicity.getByEffect(resp.getOncogenic());
                if (oncogenicity != null && AlterationUtils.hasOncogenic(Collections.singleton(oncogenicity))) {
                    List<String> content = new ArrayList<>();
                    content.add(gene.getHugoSymbol());
                    content.add(alteration.getAlteration());
                    content.add(resp.getOncogenic());
                    if (resp.getTreatments().isEmpty()) {
                        AnalysisResultRow row = new AnalysisResultRow();
                        row.setHugoSymbol(gene.getHugoSymbol());
                        row.setAlteration(alteration.getAlteration());
                        row.setAlterationName((StringUtils.isEmpty(alteration.getName()) || alteration.getName().equals(alteration.getAlteration())) ? "" : alteration.getName());
                        row.setOncogenicity(resp.getOncogenic());

                        resultRows.add(row);
                    } else {
                        resp.getTreatments().forEach(indicatorQueryTreatment -> {
                            AnalysisResultRow row = new AnalysisResultRow();
                            row.setHugoSymbol(gene.getHugoSymbol());
                            row.setAlteration(alteration.getAlteration());
                            row.setAlterationName((StringUtils.isEmpty(alteration.getName()) || alteration.getName().equals(alteration.getAlteration())) ? "" : alteration.getName());
                            row.setOncogenicity(resp.getOncogenic());
                            row.setLevel(indicatorQueryTreatment.getLevel().getLevel());
                            row.setTreatment(indicatorQueryTreatment.getDrugs().stream().map(drug -> drug.getDrugName()).collect(Collectors.joining("+")));

                            org.mskcc.cbio.oncokb.apiModels.TumorType respCancerType = indicatorQueryTreatment.getLevelAssociatedCancerType();
                            TumorType cancerType = new TumorType();
                            cancerType.setSubtype(respCancerType.getName());
                            cancerType.setMainType(respCancerType.getMainType().getName());
                            row.setLevelAssociatedCancerType(TumorTypeUtils.getTumorTypeName(cancerType));
                            resultRows.add(row);
                        });
                    }
                }
            });
        });

        List<String> headers = new ArrayList<>();
        headers.add("Gene");
        headers.add("Alteration");
        headers.add("Alternative name of alteration");
        headers.add("Oncogenicity");
        headers.add("Level");
        headers.add("Treatment");
        headers.add("Level Associated Cancer Type");
        System.out.println(headers.stream().collect(Collectors.joining("\t")));

        for (AnalysisResultRow row : resultRows) {
            System.out.println(row.toString());
        }
    }
}


class AnalysisResultRow {
    String hugoSymbol = "";
    String alteration = "";
    String alterationName = "";
    String oncogenicity = "";
    String level = "";
    String treatment = "";
    String levelAssociatedCancerType = "";

    public String getHugoSymbol() {
        return hugoSymbol;
    }

    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    public String getAlteration() {
        return alteration;
    }

    public void setAlteration(String alteration) {
        this.alteration = alteration;
    }

    public String getAlterationName() {
        return alterationName;
    }

    public void setAlterationName(String alterationName) {
        this.alterationName = alterationName;
    }

    public String getOncogenicity() {
        return oncogenicity;
    }

    public void setOncogenicity(String oncogenicity) {
        this.oncogenicity = oncogenicity;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getLevelAssociatedCancerType() {
        return levelAssociatedCancerType;
    }

    public void setLevelAssociatedCancerType(String levelAssociatedCancerType) {
        this.levelAssociatedCancerType = levelAssociatedCancerType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnalysisResultRow)) return false;
        AnalysisResultRow that = (AnalysisResultRow) o;
        return Objects.equals(getHugoSymbol(), that.getHugoSymbol()) &&
            Objects.equals(getAlteration(), that.getAlteration()) &&
            Objects.equals(getAlterationName(), that.getAlterationName()) &&
            Objects.equals(getOncogenicity(), that.getOncogenicity()) &&
            Objects.equals(getLevel(), that.getLevel()) &&
            Objects.equals(getTreatment(), that.getTreatment()) &&
            Objects.equals(getLevelAssociatedCancerType(), that.getLevelAssociatedCancerType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHugoSymbol(), getAlteration(), getAlterationName(), getOncogenicity(), getLevel(), getTreatment(), getLevelAssociatedCancerType());
    }

    @Override
    public String toString() {
        List<String> content = new ArrayList<>();
        content.add(hugoSymbol);
        content.add(alteration);
        content.add(alterationName);
        content.add(oncogenicity);
        content.add(level);
        content.add(treatment);
        content.add(levelAssociatedCancerType);
        return content.stream().collect(Collectors.joining("\t"));
    }
}
