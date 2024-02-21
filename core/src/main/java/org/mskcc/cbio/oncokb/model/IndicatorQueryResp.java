package org.mskcc.cbio.oncokb.model;


import org.mskcc.cbio.oncokb.apiModels.Implication;
import org.mskcc.cbio.oncokb.apiModels.MutationEffectResp;

import java.util.ArrayList;
import java.util.List;


/**
 * TumorType generated by hbm2java
 */

public class IndicatorQueryResp implements java.io.Serializable {
    private Query query;
    private Boolean geneExist;
    private Boolean variantExist;
    private Boolean alleleExist;
    private String oncogenic;
    private MutationEffectResp mutationEffect;
    private GermlineVariant germline = new GermlineVariant();
    private LevelOfEvidence highestSensitiveLevel;
    private LevelOfEvidence highestResistanceLevel;
    private LevelOfEvidence highestDiagnosticImplicationLevel;
    private LevelOfEvidence highestPrognosticImplicationLevel;
    private LevelOfEvidence highestFdaLevel;
    private List<LevelOfEvidence> otherSignificantSensitiveLevels = new ArrayList<>();
    private List<LevelOfEvidence> otherSignificantResistanceLevels = new ArrayList<>();
    private Boolean VUS;
    private Boolean hotspot;
    private String geneSummary = "";
    private String variantSummary = "";
    private String tumorTypeSummary = "";
    private String prognosticSummary = "";
    private String diagnosticSummary = "";
    private List<Implication> diagnosticImplications = new ArrayList<>();
    private List<Implication> prognosticImplications = new ArrayList<>();
    private List<IndicatorQueryTreatment> treatments = new ArrayList<>();
    private String dataVersion;
    private String lastUpdate;

    public IndicatorQueryResp() {
    }

    public IndicatorQueryResp copy() {
        IndicatorQueryResp newResp = new IndicatorQueryResp();
        newResp.setQuery(this.query.copy());
        newResp.setGeneExist(this.geneExist);
        newResp.setVariantExist(this.variantExist);
        newResp.setAlleleExist(this.alleleExist);
        newResp.setOncogenic(this.oncogenic);
        newResp.setMutationEffect(this.mutationEffect);
        newResp.setHighestSensitiveLevel(this.highestSensitiveLevel);
        newResp.setHighestResistanceLevel(this.highestResistanceLevel);
        newResp.setHighestDiagnosticImplicationLevel(this.highestDiagnosticImplicationLevel);
        newResp.setHighestPrognosticImplicationLevel(this.highestPrognosticImplicationLevel);
        newResp.setOtherSignificantSensitiveLevels(new ArrayList<>(this.otherSignificantSensitiveLevels));
        newResp.setOtherSignificantResistanceLevels(new ArrayList<>(this.otherSignificantResistanceLevels));
        newResp.setVUS(this.VUS);
        newResp.setHotspot(this.hotspot);
        newResp.setGeneSummary(this.geneSummary);
        newResp.setVariantSummary(this.variantSummary);
        newResp.setTumorTypeSummary(this.tumorTypeSummary);
        newResp.setPrognosticSummary(this.prognosticSummary);
        newResp.setDiagnosticSummary(this.diagnosticSummary);
        newResp.setDiagnosticImplications(new ArrayList<>(diagnosticImplications));
        newResp.setPrognosticImplications(new ArrayList<>(prognosticImplications));
        newResp.setTreatments(new ArrayList<>(treatments));
        newResp.setDataVersion(dataVersion);
        newResp.setLastUpdate(lastUpdate);
        return newResp;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Boolean getGeneExist() {
        return geneExist;
    }

    public void setGeneExist(Boolean geneExist) {
        this.geneExist = geneExist;
    }

    public Boolean getVariantExist() {
        return variantExist;
    }

    public void setVariantExist(Boolean variantExist) {
        this.variantExist = variantExist;
    }

    public String getOncogenic() {
        return oncogenic;
    }

    public void setOncogenic(String oncogenic) {
        this.oncogenic = oncogenic;
    }

    public GermlineVariant getGermline() {
        return germline;
    }

    public void setGermline(GermlineVariant germline) {
        this.germline = germline;
    }

    public MutationEffectResp getMutationEffect() {
        return mutationEffect;
    }

    public void setMutationEffect(MutationEffectResp mutationEffect) {
        this.mutationEffect = mutationEffect;
    }

    public LevelOfEvidence getHighestSensitiveLevel() {
        return highestSensitiveLevel;
    }

    public void setHighestSensitiveLevel(LevelOfEvidence highestSensitiveLevel) {
        this.highestSensitiveLevel = highestSensitiveLevel;
    }

    public LevelOfEvidence getHighestResistanceLevel() {
        return highestResistanceLevel;
    }

    public void setHighestResistanceLevel(LevelOfEvidence highestResistanceLevel) {
        this.highestResistanceLevel = highestResistanceLevel;
    }

    public LevelOfEvidence getHighestDiagnosticImplicationLevel() {
        return highestDiagnosticImplicationLevel;
    }

    public void setHighestDiagnosticImplicationLevel(LevelOfEvidence highestDiagnosticImplicationLevel) {
        this.highestDiagnosticImplicationLevel = highestDiagnosticImplicationLevel;
    }

    public LevelOfEvidence getHighestPrognosticImplicationLevel() {
        return highestPrognosticImplicationLevel;
    }

    public void setHighestPrognosticImplicationLevel(LevelOfEvidence highestPrognosticImplicationLevel) {
        this.highestPrognosticImplicationLevel = highestPrognosticImplicationLevel;
    }

    public LevelOfEvidence getHighestFdaLevel() {
        return highestFdaLevel;
    }

    public void setHighestFdaLevel(LevelOfEvidence highestFdaLevel) {
        this.highestFdaLevel = highestFdaLevel;
    }

    public List<LevelOfEvidence> getOtherSignificantSensitiveLevels() {
        return otherSignificantSensitiveLevels;
    }

    public void setOtherSignificantSensitiveLevels(List<LevelOfEvidence> otherSignificantSensitiveLevels) {
        this.otherSignificantSensitiveLevels = otherSignificantSensitiveLevels;
    }

    public List<LevelOfEvidence> getOtherSignificantResistanceLevels() {
        return otherSignificantResistanceLevels;
    }

    public void setOtherSignificantResistanceLevels(List<LevelOfEvidence> otherSignificantResistanceLevels) {
        this.otherSignificantResistanceLevels = otherSignificantResistanceLevels;
    }

    public Boolean getVUS() {
        return VUS;
    }

    public void setVUS(Boolean VUS) {
        this.VUS = VUS;
    }

    public Boolean getHotspot() {
        return hotspot;
    }

    public void setHotspot(Boolean hotspot) {
        this.hotspot = hotspot;
    }

    public Boolean getAlleleExist() {
        return alleleExist;
    }

    public void setAlleleExist(Boolean alleleExist) {
        this.alleleExist = alleleExist;
    }

    public String getGeneSummary() {
        return geneSummary;
    }

    public void setGeneSummary(String geneSummary) {
        this.geneSummary = geneSummary;
    }

    public String getVariantSummary() {
        return variantSummary;
    }

    public void setVariantSummary(String variantSummary) {
        this.variantSummary = variantSummary;
    }

    public String getTumorTypeSummary() {
        return tumorTypeSummary;
    }

    public void setTumorTypeSummary(String tumorTypeSummary) {
        this.tumorTypeSummary = tumorTypeSummary;
    }

    public String getPrognosticSummary() {
        return prognosticSummary;
    }

    public void setPrognosticSummary(String prognosticSummary) {
        this.prognosticSummary = prognosticSummary;
    }

    public String getDiagnosticSummary() {
        return diagnosticSummary;
    }

    public void setDiagnosticSummary(String diagnosticSummary) {
        this.diagnosticSummary = diagnosticSummary;
    }

    public List<Implication> getDiagnosticImplications() {
        return diagnosticImplications;
    }

    public void setDiagnosticImplications(List<Implication> diagnosticImplications) {
        this.diagnosticImplications = diagnosticImplications;
    }

    public List<Implication> getPrognosticImplications() {
        return prognosticImplications;
    }

    public void setPrognosticImplications(List<Implication> prognosticImplications) {
        this.prognosticImplications = prognosticImplications;
    }

    public List<IndicatorQueryTreatment> getTreatments() {
        return treatments;
    }

    public void setTreatments(List<IndicatorQueryTreatment> treatments) {
        this.treatments = treatments;
    }

    public String getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(String dataVersion) {
        this.dataVersion = dataVersion;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}


