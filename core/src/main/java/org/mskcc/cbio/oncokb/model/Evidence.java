package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.mskcc.cbio.oncokb.model.tumor_type.TumorType;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.mskcc.cbio.oncokb.util.TumorTypeUtils;

import javax.persistence.*;
import java.util.*;


/**
 * Evidence generated by hbm2java
 *
 * @author jgao, Hongxin Zhang
 */
@NamedQueries({
    @NamedQuery(
        name = "findEvidencesByAlteration",
        query = "select e from Evidence e join e.alterations a where a.id=?"
    ),
    @NamedQuery(
        name = "findEvidencesByAlterationAndTumorType",
        query = "select e from Evidence e join e.alterations a where a.id=:alt and (e.cancerType=:tt or e.subtype=:tt)"
    ),
    @NamedQuery(
        name = "findEvidencesByAlterationAndCancerType",
        query = "select e from Evidence e join e.alterations a where a.id=? and e.cancerType=?"
    ),
    @NamedQuery(
        name = "findEvidencesByAlterationAndCancerTypeNoSubtype",
        query = "select e from Evidence e join e.alterations a where a.id=? and e.cancerType=? and e.subtype is null"
    ),
    @NamedQuery(
        name = "findEvidencesByAlterationAndSubtype",
        query = "select e from Evidence e join e.alterations a where a.id=? and e.subtype=?"
    ),
    @NamedQuery(
        name = "findEvidencesByAlterationsAndTumorTypesAndEvidenceTypes",
        query = "select e from Evidence e join e.alterations a where a.id in (:alts) and (e.cancerType in (:tts) or e.subtype in (:tt)) and  e.evidenceType in (:ets)"
    ),
    @NamedQuery(
        name = "findEvidencesByAlterationsAndCancerTypesAndEvidenceTypes",
        query = "select e from Evidence e join e.alterations a where a.id in (:alts) and e.cancerType in (:tts) and  e.evidenceType in (:ets)"
    ),
    @NamedQuery(
        name = "findEvidencesByAlterationsAndCancerTypesAndEvidenceTypesNoSubtype",
        query = "select e from Evidence e join e.alterations a where a.id in (:alts) and e.cancerType in (:tts) and  e.evidenceType in (:ets) and e.subtype is null"
    ),
    @NamedQuery(
        name = "findEvidencesByAlterationsAndSubtypesAndEvidenceTypes",
        query = "select e from Evidence e join e.alterations a where a.id in (:alts) and e.subtype in (:tts) and  e.evidenceType in (:ets)"
    ),
    @NamedQuery(
        name = "findEvidencesByAlterationsAndTumorTypesAndEvidenceTypesAndLevelOfEvidence",
        query = "select e from Evidence e join e.alterations a where a.id in (:alts) and (e.cancerType in (:tts) or e.subtype in (:tts)) and  e.evidenceType in (:ets) and e.levelOfEvidence in (:les)"
    ),
    @NamedQuery(
        name = "findEvidencesByAlterationsAndCancerTypesAndEvidenceTypesAndLevelOfEvidence",
        query = "select e from Evidence e join e.alterations a where a.id in (:alts) and e.cancerType in (:tts) and  e.evidenceType in (:ets) and e.levelOfEvidence in (:les)"
    ),
    @NamedQuery(
        name = "findEvidencesByAlterationsAndCancerTypesAndEvidenceTypesAndLevelOfEvidenceNoSubtype",
        query = "select e from Evidence e join e.alterations a where a.id in (:alts) and e.cancerType in (:tts) and  e.evidenceType in (:ets) and e.levelOfEvidence in (:les) and e.subtype is null"
    ),
    @NamedQuery(
        name = "findEvidencesByAlterationsAndSubtypesAndEvidenceTypesAndLevelOfEvidence",
        query = "select e from Evidence e join e.alterations a where a.id in (:alts) and e.subtype in (:tts) and  e.evidenceType in (:ets) and e.levelOfEvidence in (:les)"
    ),
    @NamedQuery(
        name = "findEvidencesByAlterationAndEvidenceType",
        query = "select e from Evidence e join e.alterations a where a.id=? and e.evidenceType=?"
    ),
    @NamedQuery(
        name = "findEvidencesByAlterationAndEvidenceTypeAndLevels",
        query = "select e from Evidence e join e.alterations a where a.id=? and e.evidenceType=? and e.levelOfEvidence=?"
    ),
    @NamedQuery(
        name = "findEvidencesByAlterationAndEvidenceTypeAndTumorType",
        query = "select e from Evidence e join e.alterations a where a.id=:alt and e.evidenceType=:et and (e.cancerType=:tt or e.subtype=:tt)"
    ),
    @NamedQuery(
        name = "findEvidencesByAlterationAndEvidenceTypeAndCancerType",
        query = "select e from Evidence e join e.alterations a where a.id=? and e.evidenceType=? and e.cancerType=?"
    ),
    @NamedQuery(
        name = "findEvidencesByAlterationAndEvidenceTypeAndCancerTypeNoSubtype",
        query = "select e from Evidence e join e.alterations a where a.id=? and e.evidenceType=? and e.cancerType=? and e.subtype is null"
    ),
    @NamedQuery(
        name = "findEvidencesByAlterationAndEvidenceTypeAndSubtype",
        query = "select e from Evidence e join e.alterations a where a.id=? and e.evidenceType=? and e.subtype=?"
    ),
    @NamedQuery(
        name = "findEvidencesByGene",
        query = "select e from Evidence e where e.gene=?"
    ),
    @NamedQuery(
        name = "findEvidencesByGeneAndEvidenceType",
        query = "select e from Evidence e where e.gene=? and e.evidenceType=?"
    ),
    @NamedQuery(
        name = "findEvidencesByGeneAndEvidenceTypeAndTumorType",
        query = "select e from Evidence e where e.gene=:g and e.evidenceType=:et and (e.cancerType=:tt or e.subtype=:tt)"
    ),
    @NamedQuery(
        name = "findEvidencesByGeneAndEvidenceTypeAndCancerType",
        query = "select e from Evidence e where e.gene=? and e.evidenceType=? and e.cancerType=?"
    ),
    @NamedQuery(
        name = "findEvidencesByGeneAndEvidenceTypeAndCancerTypeNoSubtype",
        query = "select e from Evidence e where e.gene=? and e.evidenceType=? and e.cancerType=? and e.subtype is null"
    ),
    @NamedQuery(
        name = "findEvidencesByGeneAndEvidenceTypeAndSubtype",
        query = "select e from Evidence e where e.gene=? and e.evidenceType=? and e.subtype=?"
    ),
    @NamedQuery(
        name = "findEvidencesByTumorType",
        query = "select e from Evidence e where (e.cancerType=:tt or e.subtype=:tt)"
    ),
    @NamedQuery(
        name = "findEvidencesByCancerType",
        query = "select e from Evidence e where e.cancerType=?"
    ),
    @NamedQuery(
        name = "findEvidencesByCancerTypeNoSubtype",
        query = "select e from Evidence e where e.cancerType=? and e.subtype is null"
    ),
    @NamedQuery(
        name = "findEvidencesBySubtype",
        query = "select e from Evidence e where e.subtype=?"
    ),
    @NamedQuery(
        name = "findEvidencesByGeneAndTumorType",
        query = "select e from Evidence e join e.alterations a where e.gene=:g and (e.cancerType=:tt or e.subtype=:tt)"
    ),
    @NamedQuery(
        name = "findEvidencesByGeneAndCancerType",
        query = "select e from Evidence e join e.alterations a where e.gene=? and e.cancerType=?"
    ),
    @NamedQuery(
        name = "findEvidencesByGeneAndSubtype",
        query = "select e from Evidence e join e.alterations a where e.gene=? and e.subtype=?"
    ),
    @NamedQuery(
        name = "findEvidencesByIds",
        query = "select e from Evidence e where e.id in (:ids)"
    ),
    @NamedQuery(
        name = "findEvidencesByUUIDs",
        query = "select e from Evidence e where e.uuid in (:uuids)"
    ),
    @NamedQuery(
        name = "findTumorTypesWithEvidencesForAlterations",
        query = "select distinct e.cancerType, e.subtype from Evidence e join e.alterations a where a.id in (:alts)"
    ),
    @NamedQuery(
        name = "findCancerTypesWithEvidencesForAlterations",
        query = "select distinct e.cancerType from Evidence e join e.alterations a where a.id in (:alts)"
    ),
    @NamedQuery(
        name = "findSubtypesWithEvidencesForAlterations",
        query = "select distinct e.subtype from Evidence e join e.alterations a where a.id in (:alts)"
    ),
    @NamedQuery(
        name = "findAllCancerTypes",
        query = "select distinct e.cancerType from Evidence e where e.cancerType is not null"
    ),
    @NamedQuery(
        name = "findAllSubtypes",
        query = "select distinct e.subtype from Evidence e where e.subtype is not null"
    ),
    @NamedQuery(
        name = "findEvidenceByUUIDs",
        query = "select e from Evidence e where e.uuid in (:uuids)"
    ),
})

@Entity
@Table(name = "evidence")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Evidence implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 40)
    private String uuid;

    @Column(name = "evidence_type")
    @Enumerated(EnumType.STRING)
    private EvidenceType evidenceType;

    @Column(name = "cancer_type", length = 100)
    private String cancerType;

    @Column(length = 50)
    private String subtype;

    @JsonIgnore
    @Column(name = "for_germline")
    private Boolean forGermline = false;

    @Transient
    private TumorType oncoTreeType;

    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "entrez_gene_id")
    private Gene gene;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "evidence_alteration", joinColumns = {
        @JoinColumn(name = "evidence_id", updatable = false, nullable = false)
    }, inverseJoinColumns = {
        @JoinColumn(name = "alteration_id", updatable = false, nullable = false)
    })
    private Set<Alteration> alterations;

    @Column(length = 65535)
    private String description;

    @Column(name = "additional_info", length = 65535)
    private String additionalInfo;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "evidence", cascade = CascadeType.ALL)
    private Set<Treatment> treatments = new HashSet<>(0);

    @Column(name = "known_effect")
    private String knownEffect;

    @Column(name = "last_edit")
    private Date lastEdit;

    @Column(name = "last_review")
    private Date lastReview;

    @Column(name = "level_of_evidence")
    @Enumerated(EnumType.STRING)
    private LevelOfEvidence levelOfEvidence;

    @Column(name = "solid_propagation_level")
    @Enumerated(EnumType.STRING)
    private LevelOfEvidence solidPropagationLevel;

    @Column(name = "liquid_propagation_level")
    @Enumerated(EnumType.STRING)
    private LevelOfEvidence liquidPropagationLevel;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "evidence_article", joinColumns = {
        @JoinColumn(name = "evidence_id", nullable = false, updatable = false)
    }, inverseJoinColumns = {
        @JoinColumn(name = "article_id", nullable = false, updatable = false)
    })
    private Set<Article> articles;

    public Evidence() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public EvidenceType getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(EvidenceType evidenceType) {
        this.evidenceType = evidenceType;
    }

    public String getCancerType() {
        return cancerType;
    }

    public void setCancerType(String cancerType) {
        this.cancerType = cancerType;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public Boolean getForGermline() {
        return forGermline;
    }

    public void setForGermline(Boolean forGermline) {
        this.forGermline = forGermline;
    }

    public void setOncoTreeType(TumorType oncoTreeType) {
        this.oncoTreeType = oncoTreeType;
    }

    public TumorType getOncoTreeType() {
        if (this.oncoTreeType != null)
            return this.oncoTreeType;

        TumorType oncoTreeType = null;

        if (this.subtype != null) {
            oncoTreeType = TumorTypeUtils.getOncoTreeSubtypeByCode(this.subtype);
        } else if (this.cancerType != null) {
            oncoTreeType = TumorTypeUtils.getOncoTreeCancerType(this.cancerType);
        }

        return oncoTreeType;
    }

    public Gene getGene() {
        return gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
    }

    public Set<Alteration> getAlterations() {
        return alterations;
    }

    public void setAlterations(Set<Alteration> alterations) {
        this.alterations = alterations;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public Set<Treatment> getTreatments() {
        return treatments;
    }

    @JsonIgnore
    public List<Treatment> getSortedTreatment() {
        List<Treatment> treatments = new ArrayList<>(this.getTreatments());
        Collections.sort(treatments, new Comparator<Treatment>() {
            public int compare(Treatment t1, Treatment t2) {
                return t1.getPriority() - t2.getPriority();
            }
        });
        return treatments;
    }

    @JsonIgnore
    public Integer getHighestTreatmentPriority() {
        Integer highestPriority = 1000;

        for (Treatment treatment : this.getTreatments()) {
            if (treatment.getPriority() < highestPriority) {
                highestPriority = treatment.getPriority();
            }
        }
        return highestPriority;
    }

    public void setTreatments(List<Treatment> treatments) {
        if (treatments != null) {
            for (int i = 0; i < treatments.size(); i++) {
                treatments.get(i).setDrugs(treatments.get(i).getDrugs());
                if(treatments.get(i).getPriority() == null) {
                    treatments.get(i).setPriority(i+1);
                }
            }
            this.treatments = new HashSet<>(treatments);
        }
    }

    public void setPriority(Integer priority) {
        for (Treatment treatment : this.getTreatments()) {
            treatment.setPriority(priority);
        }
    }

    public String getKnownEffect() {
        return knownEffect;
    }

    public void setKnownEffect(String knownEffect) {
        this.knownEffect = knownEffect;
    }

    public Date getLastEdit() {
        return lastEdit;
    }

    public void setLastEdit(Date lastEdit) {
        this.lastEdit = lastEdit;
    }

    public Date getLastReview() {
        return lastReview;
    }

    public void setLastReview(Date lastReview) {
        this.lastReview = lastReview;
    }

    public LevelOfEvidence getLevelOfEvidence() {
        return levelOfEvidence;
    }

    public void setLevelOfEvidence(LevelOfEvidence levelOfEvidence) {
        this.levelOfEvidence = levelOfEvidence;
    }

    public LevelOfEvidence getSolidPropagationLevel() {
        return solidPropagationLevel;
    }

    public void setSolidPropagationLevel(LevelOfEvidence solidPropagationLevel) {
        this.solidPropagationLevel = solidPropagationLevel;
    }

    public LevelOfEvidence getLiquidPropagationLevel() {
        return liquidPropagationLevel;
    }

    public void setLiquidPropagationLevel(LevelOfEvidence liquidPropagationLevel) {
        this.liquidPropagationLevel = liquidPropagationLevel;
    }

    public Set<Article> getArticles() {
        return articles;
    }

    public void setArticles(Set<Article> articles) {
        this.articles = articles;
    }

    public void addArticles(Set<Article> articles) {
        if (this.articles == null) {
            this.articles = articles;
        } else {
            for (Article article : articles) {
                if (!this.articles.contains(article)) {
                    this.articles.add(article);
                }
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Evidence other = (Evidence) obj;
        if (this.id == null || other.id == null ||
            !Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    public Evidence(Evidence e, Integer id) {
        if (id != null) {
            this.id = id;
        }
        this.uuid = e.uuid;
        this.evidenceType = e.evidenceType;
        this.cancerType = e.cancerType;
        this.subtype = e.subtype;
        this.oncoTreeType = e.oncoTreeType;
        this.gene = e.gene;
        this.description = e.description;
        this.additionalInfo = e.additionalInfo;
        this.knownEffect = e.knownEffect;
        this.lastEdit = e.lastEdit;
        this.lastReview = e.lastReview;
        this.levelOfEvidence = e.levelOfEvidence;
        this.solidPropagationLevel = e.solidPropagationLevel;
        this.liquidPropagationLevel = e.liquidPropagationLevel;
        // make deep copy of sets
        this.alterations = new HashSet<>(e.alterations);
        this.setTreatments(new ArrayList<>(e.treatments));
        this.articles = new HashSet<>(e.articles);
    }

    public Evidence(String uuid, EvidenceType evidenceType, String cancerType, String subtype, TumorType oncoTreeType, Gene gene, Set<Alteration> alterations, String description, String additionalInfo, List<Treatment> treatments,
                    String knownEffect, Date lastEdit, Date lastReview,
                    LevelOfEvidence levelOfEvidence,
                    LevelOfEvidence solidPropagationLevel, LevelOfEvidence liquidPropagationLevel,
                    Set<Article> articles) {
        this.uuid = uuid;
        this.evidenceType = evidenceType;
        this.cancerType = cancerType;
        this.subtype = subtype;
        this.gene = gene;
        this.alterations = alterations;
        this.description = description;
        this.additionalInfo = additionalInfo;
        this.knownEffect = knownEffect;
        this.lastEdit = lastEdit;
        this.lastReview = lastReview;
        this.levelOfEvidence = levelOfEvidence;
        this.solidPropagationLevel = solidPropagationLevel;
        this.liquidPropagationLevel = liquidPropagationLevel;
        this.articles = articles;
        if (treatments != null) {
            this.setTreatments(treatments);
        }
    }
}


