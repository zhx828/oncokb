package org.mskcc.cbio.oncokb.apiModels;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Hongxin on 10/28/16.
 */
public class AnnotatedVariant {
    String grch37Isoform;
    String grch37RefSeq;
    String grch38Isoform;
    String grch38RefSeq;
    Integer entrezGeneId;
    String gene;
    String referenceGenome;
    String variant;
    String proteinChange;
    String oncogenicity;
    String mutationEffect;
    String mutationEffectPmids;
    String mutationEffectAbstracts;
    String mutationEffectDescription;

    public String getGrch37Isoform() {
        return grch37Isoform;
    }

    public void setGrch37Isoform(String grch37Isoform) {
        this.grch37Isoform = grch37Isoform;
    }

    public String getGrch37RefSeq() {
        return grch37RefSeq;
    }

    public void setGrch37RefSeq(String grch37RefSeq) {
        this.grch37RefSeq = grch37RefSeq;
    }

    public String getGrch38Isoform() {
        return grch38Isoform;
    }

    public void setGrch38Isoform(String grch38Isoform) {
        this.grch38Isoform = grch38Isoform;
    }

    public String getGrch38RefSeq() {
        return grch38RefSeq;
    }

    public void setGrch38RefSeq(String grch38RefSeq) {
        this.grch38RefSeq = grch38RefSeq;
    }

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public String getReferenceGenome() {
        return referenceGenome;
    }

    public void setReferenceGenome(String referenceGenome) {
        this.referenceGenome = referenceGenome;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public String getProteinChange() {
        return proteinChange;
    }

    public void setProteinChange(String proteinChange) {
        this.proteinChange = proteinChange;
    }

    public String getOncogenicity() {
        return oncogenicity;
    }

    public void setOncogenicity(String oncogenicity) {
        this.oncogenicity = oncogenicity;
    }

    public String getMutationEffect() {
        return mutationEffect;
    }

    public void setMutationEffect(String mutationEffect) {
        this.mutationEffect = mutationEffect;
    }

    public String getMutationEffectPmids() {
        return mutationEffectPmids;
    }

    public void setMutationEffectPmids(String mutationEffectPmids) {
        this.mutationEffectPmids = mutationEffectPmids;
    }

    public String getMutationEffectAbstracts() {
        return mutationEffectAbstracts;
    }

    public void setMutationEffectAbstracts(String mutationEffectAbstracts) {
        this.mutationEffectAbstracts = mutationEffectAbstracts;
    }

    public String getMutationEffectDescription() {
        return mutationEffectDescription;
    }

    public void setMutationEffectDescription(String mutationEffectDescription) {
        this.mutationEffectDescription = mutationEffectDescription;
    }

    public AnnotatedVariant(String grch37Isoform, String grch37RefSeq, String grch38Isoform, String grch38RefSeq, Integer entrezGeneId, String gene, String referenceGenome, String variant, String proteinChange, String oncogenicity, String mutationEffect, String mutationEffectPmids, String mutationEffectAbstracts, String mutationEffectDescription) {
        this.grch37Isoform = grch37Isoform;
        this.grch37RefSeq = grch37RefSeq;
        this.grch38Isoform = grch38Isoform;
        this.grch38RefSeq = grch38RefSeq;
        this.entrezGeneId = entrezGeneId;
        this.gene = gene;
        this.referenceGenome = referenceGenome;
        this.variant = variant;
        this.proteinChange = proteinChange;
        this.oncogenicity = oncogenicity;
        this.mutationEffect = mutationEffect;
        this.mutationEffectPmids = mutationEffectPmids;
        this.mutationEffectAbstracts = mutationEffectAbstracts;
        this.mutationEffectDescription = mutationEffectDescription;
    }
}
