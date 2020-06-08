package org.mskcc.cbio.oncokb.util;

import org.apache.commons.collections.CollectionUtils;
import org.mskcc.cbio.oncokb.model.*;

import java.util.*;

/**
 * Created by hongxinzhang on 4/5/16.
 */
public class NumberUtils {
    public static Set<GeneNumber> getGeneNumberList(Set<Gene> genes) {
        Map<Gene, Set<Evidence>> evidences = EvidenceUtils.getEvidenceByGenes(genes);
        Set<GeneNumber> geneNumbers = new HashSet<>();

        Iterator it = evidences.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Gene, Set<Evidence>> pair = (Map.Entry) it.next();
            GeneNumber geneNumber = new GeneNumber();

            geneNumber.setGene(pair.getKey());

            LevelOfEvidence highestSensitiveLevel = LevelUtils.getHighestLevelFromEvidenceByLevels(pair.getValue(), LevelUtils.getSensitiveLevels());
            LevelOfEvidence highestResistanceLevel = LevelUtils.getHighestLevelFromEvidenceByLevels(pair.getValue(), LevelUtils.getResistanceLevels());
            geneNumber.setHighestSensitiveLevel(highestSensitiveLevel != null ? highestSensitiveLevel.name() : null);
            geneNumber.setHighestResistanceLevel(highestResistanceLevel != null ? highestResistanceLevel.name() : null);

            Set<Alteration> alterations = AlterationUtils.getAllAlterations(pair.getKey());
            List<Alteration> excludeVUS = AlterationUtils.excludeVUS(pair.getKey(), new ArrayList<>(alterations));
            geneNumber.setAlteration(excludeVUS.size());
            geneNumbers.add(geneNumber);
        }
        return geneNumbers;
    }

    public static Set<GeneNumber> getGeneNumberListWithLevels(Set<Gene> genes, Set<LevelOfEvidence> levels) {
        if (levels == null) {
            return getGeneNumberList(genes);
        }
        Map<Gene, Set<Evidence>> evidences = EvidenceUtils.getEvidenceByGenes(genes);
        Set<GeneNumber> geneNumbers = new HashSet<>();

        Iterator it = evidences.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Gene, Set<Evidence>> pair = (Map.Entry) it.next();
            GeneNumber geneNumber = new GeneNumber();

            geneNumber.setGene(pair.getKey());

            LevelOfEvidence highestSensitiveLevel = LevelUtils.getHighestLevelFromEvidenceByLevels(pair.getValue(), new HashSet<LevelOfEvidence>(CollectionUtils.intersection(levels, LevelUtils.getSensitiveLevels())));
            LevelOfEvidence highestResistanceLevel = LevelUtils.getHighestLevelFromEvidenceByLevels(pair.getValue(), new HashSet<LevelOfEvidence>(CollectionUtils.intersection(levels, LevelUtils.getResistanceLevels())));
            geneNumber.setHighestSensitiveLevel(highestSensitiveLevel != null ? highestSensitiveLevel.name() : null);
            geneNumber.setHighestResistanceLevel(highestResistanceLevel != null ? highestResistanceLevel.name() : null);

            Set<Alteration> alterations = AlterationUtils.getAllAlterations(pair.getKey());
            List<Alteration> excludeVUS = AlterationUtils.excludeVUS(pair.getKey(), new ArrayList<>(alterations));
            geneNumber.setAlteration(excludeVUS.size());
            geneNumbers.add(geneNumber);
        }
        return geneNumbers;
    }

    public static Set<LevelNumber> getLevelNumberList() {
        Set<LevelNumber> levelList = new HashSet<>();
        Map<LevelOfEvidence, Set<Gene>> genes = getLevelBasedGenesList();
        return getLevelNumbers(genes);
    }

    public static Set<LevelNumber> getLevelNumberListByLevels(Set<LevelOfEvidence> levels) {
        if (levels == null) {
            return getLevelNumberList();
        }
        Map<LevelOfEvidence, Set<Gene>> genes = getLevelBasedGenesListByLevels(levels);
        return getLevelNumbers(genes);
    }

    public static Set<LevelNumber> getLevelNumbers(Map<LevelOfEvidence, Set<Gene>> genes) {
        Set<LevelNumber> levelList = new HashSet<>();

        for (Map.Entry<LevelOfEvidence, Set<Gene>> entry : genes.entrySet()) {
            LevelNumber levelNumber = new LevelNumber();
            levelNumber.setLevel(entry.getKey());
            levelNumber.setGenes(entry.getValue());
            if (LevelUtils.getTherapeuticLevels().contains(entry.getKey())) {
                levelNumber.setType(LevelNumberType.TX);
            } else if (LevelUtils.getDiagnosticLevels().contains(entry.getKey())) {
                levelNumber.setType(LevelNumberType.DX);
            } else if (LevelUtils.getPrognosticLevels().contains(entry.getKey())) {
                levelNumber.setType(LevelNumberType.PX);
            }
            levelList.add(levelNumber);
        }

        return levelList;
    }

    public static Map<LevelOfEvidence, Set<Gene>> getLevelBasedGenesList() {
        Map<Gene, Set<Evidence>> evidences = EvidenceUtils.getAllGeneBasedEvidences();
        Map<LevelOfEvidence, Set<Gene>> genes = new HashMap<>();
        for (Map.Entry<Gene, Set<Evidence>> entry : evidences.entrySet()) {
            Set<LevelOfEvidence> levelsOfEvidence = LevelUtils.getLevelsFromEvidence(entry.getValue());

            for(LevelOfEvidence levelOfEvidence : levelsOfEvidence) {
                if (!genes.containsKey(levelOfEvidence)) {
                    genes.put(levelOfEvidence, new HashSet<Gene>());
                }
                genes.get(levelOfEvidence).add(entry.getKey());
            }
        }
        return genes;
    }

    public static Map<LevelOfEvidence, Set<Gene>> getLevelBasedGenesListByLevels(Set<LevelOfEvidence> levels) {
        if (levels == null) {
            return getLevelBasedGenesList();
        }
        Map<Gene, Set<Evidence>> evidences = EvidenceUtils.getAllGeneBasedEvidences();
        Map<LevelOfEvidence, Set<Gene>> genes = new HashMap<>();
        for (Map.Entry<Gene, Set<Evidence>> entry : evidences.entrySet()) {
            Set<LevelOfEvidence> levelsOfEvidence = LevelUtils.getLevelsFromEvidenceByLevels(entry.getValue(), levels);

            for(LevelOfEvidence levelOfEvidence : levelsOfEvidence) {
                if (!genes.containsKey(levelOfEvidence)) {
                    genes.put(levelOfEvidence, new HashSet<Gene>());
                }
                genes.get(levelOfEvidence).add(entry.getKey());
            }
        }
        return genes;
    }

    public static Set<GeneNumber> getAllGeneNumberList() {
        return getGeneNumberList(GeneUtils.getAllGenes());
    }

    public static Set<GeneNumber> getAllGeneNumberListByLevels(Set<LevelOfEvidence> levels) {
        if (levels == null) {
            return getAllGeneNumberList();
        }
        return getGeneNumberListWithLevels(GeneUtils.getAllGenes(), levels);
    }

    public static Integer getDrugsCount() {
        Map<Gene, Set<Evidence>> evidences = EvidenceUtils.getAllGeneBasedEvidences();

        Map<Drug, Boolean> drugs = new HashMap<>();
        Set<LevelNumber> levelList = new HashSet<>();

        for (Map.Entry<Gene, Set<Evidence>> entry : evidences.entrySet()) {
            for (Evidence evidence : entry.getValue()) {
                for (Treatment treatment : evidence.getTreatments()) {
                    for (Drug drug : treatment.getDrugs()) {
                        if (!drugs.containsKey(drug)) {
                            drugs.put(drug, true);
                        }
                    }
                }
            }
        }

        return drugs.size();
    }

    public static Integer getDrugsCountByLevels(Set<LevelOfEvidence> levels) {
        if (levels == null) {
            return getDrugsCount();
        }
        Map<Gene, Set<Evidence>> evidences = EvidenceUtils.getAllGeneBasedEvidences();
        Map<Drug, Boolean> drugs = new HashMap<>();
        Set<LevelNumber> levelList = new HashSet<>();

        for (Map.Entry<Gene, Set<Evidence>> entry : evidences.entrySet()) {
            for (Evidence evidence : entry.getValue()) {
                if (evidence.getLevelOfEvidence() != null && levels.contains(evidence.getLevelOfEvidence())) {
                    for (Treatment treatment : evidence.getTreatments()) {
                        for (Drug drug : treatment.getDrugs()) {
                            if (!drugs.containsKey(drug)) {
                                drugs.put(drug, true);
                            }
                        }
                    }
                }
            }
        }
        return drugs.size();
    }
}
