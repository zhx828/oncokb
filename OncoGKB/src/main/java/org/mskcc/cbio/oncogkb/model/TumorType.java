package org.mskcc.cbio.oncogkb.model;
// Generated Dec 12, 2013 4:07:37 PM by Hibernate Tools 3.2.1.GA


import java.util.HashSet;
import java.util.Set;

/**
 * TumorType generated by hbm2java
 */
public class TumorType  implements java.io.Serializable {


     private String tumorTypeId;
     private String name;
     private String shortName;
     private String color;
     private Set<DrugSensitivityEvidence> drugSensitivityEvidences = new HashSet<DrugSensitivityEvidence>(0);
     private Set<AlterationActivityEvidence> alterationActivityEvidences = new HashSet<AlterationActivityEvidence>(0);
     private Set<GeneEvidence> geneEvidences = new HashSet<GeneEvidence>(0);

    public TumorType() {
    }

	
    public TumorType(String tumorTypeId, String name, String shortName) {
        this.tumorTypeId = tumorTypeId;
        this.name = name;
        this.shortName = shortName;
    }
    public TumorType(String tumorTypeId, String name, String shortName, String color, Set<DrugSensitivityEvidence> drugSensitivityEvidences, Set<AlterationActivityEvidence> alterationActivityEvidences, Set<GeneEvidence> geneEvidences) {
       this.tumorTypeId = tumorTypeId;
       this.name = name;
       this.shortName = shortName;
       this.color = color;
       this.drugSensitivityEvidences = drugSensitivityEvidences;
       this.alterationActivityEvidences = alterationActivityEvidences;
       this.geneEvidences = geneEvidences;
    }
   
    public String getTumorTypeId() {
        return this.tumorTypeId;
    }
    
    public void setTumorTypeId(String tumorTypeId) {
        this.tumorTypeId = tumorTypeId;
    }
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    public String getShortName() {
        return this.shortName;
    }
    
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
    public String getColor() {
        return this.color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    public Set<DrugSensitivityEvidence> getDrugSensitivityEvidences() {
        return this.drugSensitivityEvidences;
    }
    
    public void setDrugSensitivityEvidences(Set<DrugSensitivityEvidence> drugSensitivityEvidences) {
        this.drugSensitivityEvidences = drugSensitivityEvidences;
    }
    public Set<AlterationActivityEvidence> getAlterationActivityEvidences() {
        return this.alterationActivityEvidences;
    }
    
    public void setAlterationActivityEvidences(Set<AlterationActivityEvidence> alterationActivityEvidences) {
        this.alterationActivityEvidences = alterationActivityEvidences;
    }
    public Set<GeneEvidence> getGeneEvidences() {
        return this.geneEvidences;
    }
    
    public void setGeneEvidences(Set<GeneEvidence> geneEvidences) {
        this.geneEvidences = geneEvidences;
    }




}

