package org.mskcc.cbio.oncokb.util;

import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.apiModels.Citations;
import org.mskcc.cbio.oncokb.apiModels.MutationEffectResp;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.model.oncotree.TumorType;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by hongxinzhang on 4/5/16.
 */
public class IndicatorUtils {
    public static IndicatorQueryResp processQuery(Query query, String geneStatus,
                                                  Set<LevelOfEvidence> levels, String source, Boolean highestLevelOnly,
                                                  Set<EvidenceType> evidenceTypes) {
        geneStatus = geneStatus != null ? geneStatus : "complete";
        highestLevelOnly = highestLevelOnly == null ? false : highestLevelOnly;

        levels = levels == null ? LevelUtils.getPublicAndOtherIndicationLevels() :
            new HashSet<>(CollectionUtils.intersection(levels, LevelUtils.getPublicAndOtherIndicationLevels()));

        Set<EvidenceType> selectedTreatmentEvidence = new HashSet<>();
        if (evidenceTypes == null || evidenceTypes.isEmpty()) {
            evidenceTypes = new HashSet<>(EvidenceTypeUtils.getAllEvidenceTypes());
            selectedTreatmentEvidence = EvidenceTypeUtils.getTreatmentEvidenceTypes();
        } else {
            selectedTreatmentEvidence = Sets.intersection(evidenceTypes, EvidenceTypeUtils.getTreatmentEvidenceTypes());
        }

        boolean hasTreatmentEvidence = !selectedTreatmentEvidence.isEmpty();
        boolean hasOncogenicEvidence = evidenceTypes.contains(EvidenceType.ONCOGENIC);
        boolean hasMutationEffectEvidence = evidenceTypes.contains(EvidenceType.MUTATION_EFFECT);

        IndicatorQueryResp indicatorQuery = new IndicatorQueryResp();
        indicatorQuery.setQuery(query);

        Gene gene = null;
        List<Alteration> relevantAlterations = new ArrayList<>();

        Set<Evidence> allQueryRelatedEvidences = new HashSet<>();

        // Queried alteration
        Alteration alteration;

        if (query == null) {
            return indicatorQuery;
        }

        query.enrich();

        source = source == null ? "oncokb" : source;

        // Temporary forward previous production annotation
        if (!com.mysql.jdbc.StringUtils.isNullOrEmpty(query.getAlteration()) && query.getAlteration().equals("EGFRvIII")) {
            query.setAlteration("vIII");
        }

        // Deal with fusion without primary gene, and this is only for legacy fusion event
        // The latest fusion event has been integrated with alteration type. Please see next if-else condition
        // for more info.
        // TODO: support entrezGeneId fusion
        AlterationType alterationType = AlterationType.getByName(query.getAlterationType());
        Map<String, Object> fusionGeneAltsMap = new HashMap<>();
        if (query.getHugoSymbol() != null
            && alterationType != null &&
            alterationType.equals(AlterationType.FUSION)) {
            fusionGeneAltsMap = findFusionGeneAndRelevantAlts(query);

            // Dup: For single gene deletion event. We should map to Deletion instead of Truncating Mutation when Deletion has been curated
            if (query.getSvType() != null && query.getSvType().equals(StructuralVariantType.DELETION)) {
                Set<String> queryFusionGenes = (Set<String>) fusionGeneAltsMap.get("queryFusionGenes");
                if (queryFusionGenes.size() == 1) {
                    Gene queryFusionGene = GeneUtils.getGeneByHugoSymbol(queryFusionGenes.iterator().next());
                    if (queryFusionGene != null) {
                        Alteration deletion = AlterationUtils.findAlteration(queryFusionGene, "Deletion");
                        if (deletion != null) {
                            query.setAlteration("deletion");
                            query.setConsequence("feature_truncation");
                            fusionGeneAltsMap = findFusionGeneAndRelevantAlts(query);
                        }
                    }
                }
            }

            gene = (Gene) fusionGeneAltsMap.get("pickedGene");
            relevantAlterations = (List<Alteration>) fusionGeneAltsMap.get("relevantAlts");
            Set<Gene> allGenes = (LinkedHashSet<Gene>) fusionGeneAltsMap.get("allGenes");
        } else if (alterationType != null && alterationType.equals(AlterationType.STRUCTURAL_VARIANT)) {
            VariantConsequence variantConsequence = VariantConsequenceUtils.findVariantConsequenceByTerm(query.getConsequence());
            Boolean isFunctionalFusion = variantConsequence != null && variantConsequence.getTerm().equals("fusion");

            if (isFunctionalFusion || !com.mysql.jdbc.StringUtils.isNullOrEmpty(query.getAlteration())) {
//                if(query.getAlteration() != null && query.getAlteration().matches("fusion")) {
//                    query.
//                }
                fusionGeneAltsMap = findFusionGeneAndRelevantAlts(query);
                gene = (Gene) fusionGeneAltsMap.get("pickedGene");
                relevantAlterations = (List<Alteration>) fusionGeneAltsMap.get("relevantAlts");
            } else {
                query.setAlteration("truncating mutation");
                query.setConsequence("feature_truncation");

                fusionGeneAltsMap = findFusionGeneAndRelevantAlts(query);

                // For single gene deletion event. We should map to Deletion instead of Truncating Mutation when Deletion has been curated
                if (query.getSvType() != null && query.getSvType().equals(StructuralVariantType.DELETION)) {
                    Set<String> queryFusionGenes = (Set<String>) fusionGeneAltsMap.get("queryFusionGenes");
                    if (queryFusionGenes.size() == 1) {
                        Gene queryFusionGene = GeneUtils.getGeneByHugoSymbol(queryFusionGenes.iterator().next());
                        if (queryFusionGene != null) {
                            Alteration deletion = AlterationUtils.findAlteration(queryFusionGene, "Deletion");
                            if (deletion != null) {
                                query.setAlteration("deletion");
                                fusionGeneAltsMap = findFusionGeneAndRelevantAlts(query);
                            }
                        }
                    }
                }

                gene = (Gene) fusionGeneAltsMap.get("pickedGene");
                fusionGeneAltsMap = new HashMap<>();
                // As long as this is a structural variant event, we need to attach the Truncating Mutation
                Alteration truncatingMutations = AlterationUtils.getTruncatingMutations(gene);
                if (truncatingMutations != null && !relevantAlterations.contains(truncatingMutations)) {
                    relevantAlterations.add(truncatingMutations);
                    List<Alteration> truncMutRelevants = AlterationUtils.getRelevantAlterations(truncatingMutations);
                    for (Alteration alt : truncMutRelevants) {
                        if (!relevantAlterations.contains(alt)) {
                            relevantAlterations.add(alt);
                        }
                    }
                }
            }
        } else {
            gene = GeneUtils.getGene(query.getEntrezGeneId(), query.getHugoSymbol());
            if (gene != null) {
                Alteration alt = AlterationUtils.getAlteration(gene.getHugoSymbol(), query.getAlteration(),
                    null, query.getConsequence(), query.getProteinStart(), query.getProteinEnd());

                AlterationUtils.annotateAlteration(alt, alt.getAlteration());

                relevantAlterations = AlterationUtils.getRelevantAlterations(alt);
            }
        }

        // For fusions
        if (fusionGeneAltsMap.containsKey("hasRelevantAltsGenes")) {
            // If there are more than two genes have matches we need to compare the highest level, then oncogenicity
            TreeSet<IndicatorQueryResp> result = new TreeSet<>(new IndicatorQueryRespComp());
            for (Gene tmpGene : (List<Gene>) fusionGeneAltsMap.get("hasRelevantAltsGenes")) {
                Query tmpQuery = new Query(query.getId(), query.getType(), tmpGene.getEntrezGeneId(),
                    tmpGene.getHugoSymbol(), query.getAlteration(), null, query.getSvType(),
                    query.getTumorType(), query.getConsequence(), query.getProteinStart(),
                    query.getProteinEnd(), query.getHgvs());
                result.add(IndicatorUtils.processQuery(tmpQuery, geneStatus, levels, source, highestLevelOnly, evidenceTypes));
            }
            return result.iterator().next();
        }

        if (gene != null) {
            query.setHugoSymbol(gene.getHugoSymbol());
            query.setEntrezGeneId(gene.getEntrezGeneId());

            // Gene exist should only be set to true if entrezGeneId is bigger than 0
            indicatorQuery.setGeneExist(gene.getEntrezGeneId() > 0);

            // Gene summary

            if (evidenceTypes.contains(EvidenceType.GENE_SUMMARY)) {
                indicatorQuery.setGeneSummary(SummaryUtils.geneSummary(gene));
                allQueryRelatedEvidences.addAll(EvidenceUtils.getEvidenceByGeneAndEvidenceTypes(gene, Collections.singleton(EvidenceType.GENE_SUMMARY)));
            }

            alteration = AlterationUtils.getAlteration(gene.getHugoSymbol(), query.getAlteration(),
                null, query.getConsequence(), query.getProteinStart(), query.getProteinEnd());
            AlterationUtils.annotateAlteration(alteration, alteration.getAlteration());

            List<Alteration> nonVUSRelevantAlts = AlterationUtils.excludeVUS(relevantAlterations);
            Map<String, LevelOfEvidence> highestLevels = new HashMap<>();
            List<Alteration> alleles = AlterationUtils.getAlleleAlterations(alteration);
            List<TumorType> oncoTreeTypes = new ArrayList<>();

            Alteration matchedAlt = AlterationUtils.findAlteration(alteration.getGene(), alteration.getAlteration());
            indicatorQuery.setVariantExist(matchedAlt != null);

            // Whether alteration is hotpot from Matt's list
            if (query.getProteinEnd() == null || query.getProteinStart() == null) {
                indicatorQuery.setHotspot(HotspotUtils.isHotspot(alteration));
            } else {
                indicatorQuery.setHotspot(HotspotUtils.isHotspot(alteration));
            }

            if (query.getTumorType() != null) {
                oncoTreeTypes = TumorTypeUtils.getMappedOncoTreeTypesBySource(query.getTumorType(), source);
            }

            indicatorQuery.setVUS(isVUS(matchedAlt == null ? alteration : matchedAlt));

            if (indicatorQuery.getVUS()) {
                List<Evidence> vusEvidences = EvidenceUtils.getEvidence(Collections.singletonList(matchedAlt), Collections.singleton(EvidenceType.VUS), null);
                if (vusEvidences != null) {
                    allQueryRelatedEvidences.addAll(vusEvidences);
                }
            }

            if (alleles == null || alleles.size() == 0) {
                indicatorQuery.setAlleleExist(false);
            } else {
                indicatorQuery.setAlleleExist(true);
            }

            Set<Evidence> treatmentEvidences = new HashSet<>();

            if (nonVUSRelevantAlts.size() > 0) {
                if (hasOncogenicEvidence) {
                    IndicatorQueryOncogenicity indicatorQueryOncogenicity = getOncogenicity(matchedAlt == null ? alteration : matchedAlt, alleles, query, source, geneStatus);

                    if (indicatorQueryOncogenicity.getOncogenicityEvidence() != null) {
                        allQueryRelatedEvidences.add(indicatorQueryOncogenicity.getOncogenicityEvidence());
                    }

                    // Only set oncogenicity if no previous data assigned.
                    if (indicatorQuery.getOncogenic() == null && indicatorQueryOncogenicity.getOncogenicity() != null) {
                        indicatorQuery.setOncogenic(indicatorQueryOncogenicity.getOncogenicity().getOncogenic());
                    }
                }

                if (hasMutationEffectEvidence) {
                    IndicatorQueryMutationEffect indicatorQueryMutationEffect = getMutationEffect(matchedAlt == null ? alteration : matchedAlt, alleles, query, source, geneStatus);

                    if (indicatorQueryMutationEffect.getMutationEffectEvidence() != null) {
                        allQueryRelatedEvidences.add(indicatorQueryMutationEffect.getMutationEffectEvidence());
                    }

                    // Only set oncogenicity if no previous data assigned.
                    if (indicatorQuery.getMutationEffect() == null && indicatorQueryMutationEffect.getMutationEffect() != null) {
                        MutationEffectResp mutationEffectResp = new MutationEffectResp();
                        mutationEffectResp.setKnownEffect(indicatorQueryMutationEffect.getMutationEffect().getMutationEffect());
                        mutationEffectResp.setDescription(indicatorQueryMutationEffect.getMutationEffectEvidence().getDescription());
                        mutationEffectResp.setCitations(MainUtils.getCitationsByEvidence(indicatorQueryMutationEffect.getMutationEffectEvidence()));
                        indicatorQuery.setMutationEffect(mutationEffectResp);
                    }
                }

                if (hasTreatmentEvidence) {
                    treatmentEvidences = EvidenceUtils.keepHighestLevelForSameTreatments(
                        EvidenceUtils.getRelevantEvidences(query, source, geneStatus, matchedAlt,
                            selectedTreatmentEvidence, levels));
                }
            }

            // Set hotspot oncogenicity to Predicted Oncogenic
            if (indicatorQuery.getHotspot() && !MainUtils.isValidHotspotOncogenicity(Oncogenicity.getByEffect(indicatorQuery.getOncogenic()))) {
                indicatorQuery.setOncogenic(Oncogenicity.PREDICTED.getOncogenic());

                // Check whether the gene has Oncogenic Mutations annotated
                Alteration oncogenicMutation = AlterationUtils.findAlteration(gene, "Oncogenic Mutations");
                if (oncogenicMutation != null) {
                    relevantAlterations.add(oncogenicMutation);
                    if (hasTreatmentEvidence) {
                        treatmentEvidences.addAll(EvidenceUtils.keepHighestLevelForSameTreatments(
                            EvidenceUtils.convertEvidenceLevel(
                                EvidenceUtils.getEvidence(Collections.singletonList(oncogenicMutation),
                                    selectedTreatmentEvidence, levels), new HashSet<>(oncoTreeTypes))));
                    }
                }
            }

            if (hasTreatmentEvidence && treatmentEvidences != null && !treatmentEvidences.isEmpty()) {
                if (highestLevelOnly) {
                    Set<Evidence> filteredEvis = new HashSet<>();
                    // Get highest sensitive evidences
                    Set<Evidence> sensitiveEvidences = EvidenceUtils.getSensitiveEvidences(treatmentEvidences);
                    filteredEvis.addAll(EvidenceUtils.getOnlySignificantLevelsEvidences(sensitiveEvidences));

                    // Get highest resistance evidences
                    Set<Evidence> resistanceEvidences = EvidenceUtils.getResistanceEvidences(treatmentEvidences);
                    filteredEvis.addAll(EvidenceUtils.getOnlyHighestLevelEvidences(resistanceEvidences));

                    treatmentEvidences = filteredEvis;
                }
                if (!treatmentEvidences.isEmpty()) {
                    List<IndicatorQueryTreatment> treatments = getIndicatorQueryTreatments(treatmentEvidences);

                    // Make sure the treatment in KIT is always sorted.
                    if (gene.getHugoSymbol().equals("KIT")) {
                        CustomizeComparator.sortKitTreatment(treatments);
                    }
                    indicatorQuery.setTreatments(treatments);
                    highestLevels = findHighestLevel(new HashSet<>(treatments));
                    indicatorQuery.setHighestSensitiveLevel(highestLevels.get("sensitive"));
                    indicatorQuery.setHighestResistanceLevel(highestLevels.get("resistant"));
                    indicatorQuery.setOtherSignificantSensitiveLevels(getOtherSignificantLevels(indicatorQuery.getHighestSensitiveLevel(), "sensitive", treatmentEvidences));
                    indicatorQuery.setOtherSignificantResistanceLevels(getOtherSignificantLevels(indicatorQuery.getHighestResistanceLevel(), "resistance", treatmentEvidences));

                    allQueryRelatedEvidences.addAll(treatmentEvidences);
                }
            }

            // Tumor type summary
            if (evidenceTypes.contains(EvidenceType.TUMOR_TYPE_SUMMARY) && query.getTumorType() != null) {
                Map<String, Object> tumorTypeSummary = SummaryUtils.tumorTypeSummary(gene, query, matchedAlt,
                    new ArrayList<>(relevantAlterations),
                    oncoTreeTypes);
                if (tumorTypeSummary != null) {
                    indicatorQuery.setTumorTypeSummary((String) tumorTypeSummary.get("summary"));
                    Date lateEdit = tumorTypeSummary.get("lastEdit") == null ? null : (Date) tumorTypeSummary.get("lastEdit");
                    if (lateEdit != null) {
                        Evidence lastEditTTSummary = new Evidence();
                        lastEditTTSummary.setLastEdit(lateEdit);
                        allQueryRelatedEvidences.add(lastEditTTSummary);
                    }
                }
            }

            // Mutation summary
            if (evidenceTypes.contains(EvidenceType.MUTATION_SUMMARY)) {
                indicatorQuery.setVariantSummary(SummaryUtils.oncogenicSummary(gene, matchedAlt,
                    new ArrayList<>(relevantAlterations), query));
            }

            // This is special case for KRAS wildtype. May need to come up with a better plan for this.
            if (gene != null && (gene.getHugoSymbol().equals("KRAS") || gene.getHugoSymbol().equals("NRAS"))
                && query.getAlteration() != null
                && StringUtils.containsIgnoreCase(query.getAlteration(), "wildtype")) {
                if (oncoTreeTypes.contains(TumorTypeUtils.getOncoTreeCancerType("Colorectal Cancer"))) {
                    indicatorQuery.setGeneSummary("RAS (KRAS/NRAS) which is wildtype (not mutated) in this sample, encodes an upstream activator of the pro-oncogenic MAP- and PI3-kinase pathways and is mutated in approximately 40% of late stage colorectal cancers.");
                    indicatorQuery.setVariantSummary("The absence of a mutation in the RAS genes is clinically important because it expands approved treatments available to treat this tumor. RAS status in stage IV colorectal cancer influences patient responses to the anti-EGFR antibody therapies cetuximab and panitumumab.");
                    indicatorQuery.setTumorTypeSummary("These drugs are FDA-approved for the treatment of KRAS wildtype colorectal tumors together with chemotherapy or alone following progression through standard chemotherapy.");
                } else {
                    indicatorQuery.setVariantSummary("");
                    indicatorQuery.setTumorTypeSummary("");
                    indicatorQuery.setTreatments(new ArrayList<IndicatorQueryTreatment>());
                    indicatorQuery.setHighestResistanceLevel(null);
                    indicatorQuery.setHighestSensitiveLevel(null);
                }
            }
        } else {
            indicatorQuery.setGeneExist(false);
        }
        indicatorQuery.setDataVersion(MainUtils.getDataVersion());

        Date lastUpdate = getLatestDateFromEvidences(allQueryRelatedEvidences);
        indicatorQuery.setLastUpdate(lastUpdate == null ? MainUtils.getDataVersionDate() :
            new SimpleDateFormat("MM/dd/yyy").format(lastUpdate));

        // Give default oncogenicity if no data has been assigned.
        if (indicatorQuery.getOncogenic() == null) {
            indicatorQuery.setOncogenic("");
        }
        return indicatorQuery;
    }

    private static IndicatorQueryOncogenicity getOncogenicity(Alteration alteration, List<Alteration> alternativeAllele, Query query, String source, String geneStatus) {
        Oncogenicity oncogenicity = null;
        Evidence oncogenicityEvidence = null;


        // Find alteration specific oncogenicity
        List<Evidence> selfAltOncogenicEvis = EvidenceUtils.getEvidence(Collections.singletonList(alteration),
            Collections.singleton(EvidenceType.ONCOGENIC), null);
        if (selfAltOncogenicEvis != null) {
            oncogenicityEvidence = MainUtils.findHighestOncogenicEvidenceByEvidences(new HashSet<>(selfAltOncogenicEvis));
            if (oncogenicityEvidence != null) {
                oncogenicity = Oncogenicity.getByEffect(oncogenicityEvidence.getKnownEffect());
            }
        }

        // Find Oncogenicity from alternative alleles
        if ((oncogenicity == null || oncogenicity.equals(Oncogenicity.INCONCLUSIVE))
            && alternativeAllele.size() > 0) {
            oncogenicityEvidence = MainUtils.findHighestOncogenicEvidenceByEvidences(new HashSet<>(EvidenceUtils.getEvidence(new ArrayList<>(alternativeAllele), Collections.singleton(EvidenceType.ONCOGENIC), null)));
            if (oncogenicityEvidence != null) {
                Oncogenicity tmpOncogenicity = MainUtils.setToAlleleOncogenicity(Oncogenicity.getByEffect(oncogenicityEvidence.getKnownEffect()));
                if (tmpOncogenicity != null) {
                    oncogenicity = tmpOncogenicity;
                }
            }
        }

        // If there is no oncogenic info available for this variant, find oncogenicity from relevant variants
        if (oncogenicity == null || oncogenicity.equals(Oncogenicity.INCONCLUSIVE)) {
            oncogenicityEvidence = MainUtils.findHighestOncogenicEvidenceByEvidences(
                EvidenceUtils.getRelevantEvidences(query, source, geneStatus, alteration,
                    Collections.singleton(EvidenceType.ONCOGENIC), null));
            if (oncogenicityEvidence != null) {
                Oncogenicity tmpOncogenicity = Oncogenicity.getByEffect(oncogenicityEvidence.getKnownEffect());
                if (tmpOncogenicity != null) {
                    oncogenicity = tmpOncogenicity;
                }
            }
        }
        return new IndicatorQueryOncogenicity(oncogenicity, oncogenicityEvidence);
    }

    private static IndicatorQueryMutationEffect getMutationEffect(Alteration alteration, List<Alteration> alternativeAllele, Query query, String source, String geneStatus) {
        IndicatorQueryMutationEffect indicatorQueryMutationEffect = new IndicatorQueryMutationEffect();
        // Find alteration specific mutation effect
        List<Evidence> selfAltMEEvis = EvidenceUtils.getEvidence(Collections.singletonList(alteration),
            Collections.singleton(EvidenceType.MUTATION_EFFECT), null);
        if (selfAltMEEvis != null) {
            indicatorQueryMutationEffect = MainUtils.findHighestMutationEffectByEvidence(new HashSet<>(selfAltMEEvis));
        }

        // Find mutation effect from alternative alleles
        if ((indicatorQueryMutationEffect.getMutationEffect() == null || indicatorQueryMutationEffect.getMutationEffect().equals(MutationEffect.INCONCLUSIVE))
            && alternativeAllele.size() > 0) {
            indicatorQueryMutationEffect =
                MainUtils.setToAlternativeAlleleMutationEffect(
                    MainUtils.findHighestMutationEffectByEvidence(
                        new HashSet<>(
                            EvidenceUtils.getEvidence(
                                new ArrayList<>(alternativeAllele)
                                , Collections.singleton(EvidenceType.MUTATION_EFFECT)
                                , null
                            )
                        )
                    )
                );
        }

        // If there is no mutation effect info available for this variant, find mutation effect from relevant variants
        if (indicatorQueryMutationEffect.getMutationEffect() == null || indicatorQueryMutationEffect.getMutationEffect().equals(MutationEffect.INCONCLUSIVE)) {
            indicatorQueryMutationEffect = MainUtils.findHighestMutationEffectByEvidence(
                EvidenceUtils.getRelevantEvidences(query, source, geneStatus, alteration,
                    Collections.singleton(EvidenceType.MUTATION_EFFECT), null));
        }
        return indicatorQueryMutationEffect;
    }

    private static Date getLatestDateFromEvidences(Set<Evidence> evidences) {
        Date date = null;
        if (evidences != null) {
            for (Evidence evidence : evidences) {
                if (evidence.getLastEdit() != null) {
                    if (date == null) {
                        date = evidence.getLastEdit();
                    } else if (date.before(evidence.getLastEdit())) {
                        date = evidence.getLastEdit();
                    }
                }
            }
        }
        return date;
    }

    private static List<LevelOfEvidence> getOtherSignificantLevels(LevelOfEvidence highestLevel, String type, Set<Evidence> evidences) {
        List<LevelOfEvidence> otherSignificantLevels = new ArrayList<>();
        if (type != null && highestLevel != null && evidences != null) {
            if (type.equals("sensitive")) {
                if (highestLevel.equals(LevelOfEvidence.LEVEL_2B)) {
                    Map<LevelOfEvidence, Set<Evidence>> levels = EvidenceUtils.separateEvidencesByLevel(evidences);
                    if (levels.containsKey(LevelOfEvidence.LEVEL_3A)) {
                        otherSignificantLevels.add(LevelOfEvidence.LEVEL_3A);
                    }
                }
            } else if (type.equals("resistance")) {

            }
        }
        return otherSignificantLevels;
    }

    private static List<IndicatorQueryTreatment> getIndicatorQueryTreatments(Set<Evidence> evidences) {
        List<IndicatorQueryTreatment> treatments = new ArrayList<>();
        if (evidences != null) {
            List<Evidence> sortedEvidence = new ArrayList<>(evidences);

            CustomizeComparator.sortEvidenceBasedOnPriority(sortedEvidence);

            for (Evidence evidence : sortedEvidence) {
                Citations citations = MainUtils.getCitationsByEvidence(evidence);
                for (Treatment treatment : evidence.getSortedTreatment()) {
                    IndicatorQueryTreatment indicatorQueryTreatment = new IndicatorQueryTreatment();
                    indicatorQueryTreatment.setDrugs(treatment.getDrugs());
                    indicatorQueryTreatment.setApprovedIndications(treatment.getApprovedIndications());
                    indicatorQueryTreatment.setLevel(evidence.getLevelOfEvidence());
                    indicatorQueryTreatment.setPmids(citations.getPmids());
                    indicatorQueryTreatment.setAbstracts(citations.getAbstracts());
                    treatments.add(indicatorQueryTreatment);
                }
            }
        }
        return treatments;
    }

    private static Boolean isVUS(Alteration alteration) {
        if (alteration == null) {
            return false;
        }
        List<Alteration> alterations = AlterationUtils.excludeVUS(Collections.singletonList(alteration));
        return alterations.size() == 0;
    }

    private static Map<String, LevelOfEvidence> findHighestLevel(Set<IndicatorQueryTreatment> treatments) {
        int levelSIndex = -1;
        int levelRIndex = -1;

        Map<String, LevelOfEvidence> levels = new HashMap<>();

        if (treatments != null) {
            for (IndicatorQueryTreatment treatment : treatments) {
                LevelOfEvidence levelOfEvidence = treatment.getLevel();
                if (levelOfEvidence != null) {
                    int _index = -1;
                    if (LevelUtils.isSensitiveLevel(levelOfEvidence)) {
                        _index = LevelUtils.SENSITIVE_LEVELS.indexOf(levelOfEvidence);
                        if (_index > levelSIndex) {
                            levelSIndex = _index;
                        }
                    } else if (LevelUtils.isResistanceLevel(levelOfEvidence)) {
                        _index = LevelUtils.RESISTANCE_LEVELS.indexOf(levelOfEvidence);
                        if (_index > levelRIndex) {
                            levelRIndex = _index;
                        }
                    }
                }
            }
        }
        levels.put("sensitive", levelSIndex > -1 ? LevelUtils.SENSITIVE_LEVELS.get(levelSIndex) : null);
        levels.put("resistant", levelRIndex > -1 ? LevelUtils.RESISTANCE_LEVELS.get(levelRIndex) : null);
        return levels;
    }

    public static Map<String, LevelOfEvidence> findHighestLevelByEvidences(Set<Evidence> treatmentEvidences) {
        List<IndicatorQueryTreatment> treatments = getIndicatorQueryTreatments(treatmentEvidences);
        return findHighestLevel(new HashSet<>(treatments));
    }

    private static List<Alteration> findRelevantAlts(Gene gene, String alteration) {
        Set<Alteration> relevantAlts = new LinkedHashSet<>();
        Alteration alt = AlterationUtils.getAlteration(gene.getHugoSymbol(), alteration,
            null, null, null, null);
        AlterationUtils.annotateAlteration(alt, alt.getAlteration());

        relevantAlts.addAll(AlterationUtils.getRelevantAlterations(alt));

        Alteration revertAlt = AlterationUtils.getRevertFusions(alt);
        if (revertAlt != null) {
            relevantAlts.addAll(AlterationUtils.getRelevantAlterations(revertAlt));
        }
        return new ArrayList<>(relevantAlts);
    }

    public static Map<String, Object> findFusionGeneAndRelevantAlts(Query query) {
        List<String> geneStrsList = Arrays.asList(query.getHugoSymbol().split("-"));
        Set<String> geneStrsSet = new HashSet<>();
        Gene gene = null;
        List<Alteration> fusionPair = new ArrayList<>();
        List<Alteration> relevantAlterations = new ArrayList<>();
        Map<String, Object> map = new HashedMap();

        if (geneStrsList != null) {
            geneStrsSet = new HashSet<>(geneStrsList);
            map.put("queryFusionGenes", geneStrsSet);
        }

        // Deal with two different genes fusion event.
        if (geneStrsSet.size() >= 2) {
            Set<Gene> tmpGenes = new LinkedHashSet<>();
            for (String geneStr : geneStrsSet) {
                Gene tmpGene = GeneUtils.getGeneByHugoSymbol(geneStr);
                if (tmpGene != null) {
                    tmpGenes.add(tmpGene);
                }
            }
            if (tmpGenes.size() > 0) {

                List<Gene> hasRelevantAltsGenes = new ArrayList<>();
                for (Gene tmpGene : tmpGenes) {
                    List<Alteration> tmpRelevantAlts = findRelevantAlts(tmpGene, query.getHugoSymbol() + " Fusion");
                    if (tmpRelevantAlts != null && tmpRelevantAlts.size() > 0) {
                        hasRelevantAltsGenes.add(tmpGene);
                    }
                }

                if (hasRelevantAltsGenes.size() > 1) {
                    map.put("hasRelevantAltsGenes", hasRelevantAltsGenes);
                } else if (hasRelevantAltsGenes.size() == 1) {
                    gene = hasRelevantAltsGenes.iterator().next();
                    if (!com.mysql.jdbc.StringUtils.isNullOrEmpty(query.getAlteration())) {
                        relevantAlterations = findRelevantAlts(gene, query.getAlteration());
                    } else {
                        relevantAlterations = findRelevantAlts(gene, query.getHugoSymbol() + " Fusion");
                    }
                }

                // None of relevant alterations found in both genes.
                if (gene == null) {
                    gene = tmpGenes.iterator().next();
                }
                map.put("allGenes", tmpGenes);
            }
        } else if (geneStrsSet.size() == 1) {
            String geneStr = geneStrsSet.iterator().next();
            if (geneStr != null) {
                Gene tmpGene = GeneUtils.getGeneByHugoSymbol(geneStr);
                if (tmpGene != null) {
                    gene = tmpGene;
                    Alteration alt = AlterationUtils.getAlteration(gene.getHugoSymbol(), query.getAlteration(),
                        AlterationType.getByName(query.getAlterationType()), query.getConsequence(), null, null);
                    AlterationUtils.annotateAlteration(alt, alt.getAlteration());
                    if (!com.mysql.jdbc.StringUtils.isNullOrEmpty(query.getAlteration())) {
                        relevantAlterations = findRelevantAlts(gene, query.getAlteration());
                    } else {
                        relevantAlterations = AlterationUtils.getRelevantAlterations(alt);

                        // Map Truncating Mutations to single gene fusion event
                        Alteration truncatingMutations = AlterationUtils.getTruncatingMutations(gene);
                        if (truncatingMutations != null && !relevantAlterations.contains(truncatingMutations)) {
                            relevantAlterations.add(truncatingMutations);
                        }
                    }
                }
            }
            LinkedHashSet<Gene> allGenes = new LinkedHashSet<>();
            for (String subGeneStr : geneStrsSet) {
                Gene tmpGene = GeneUtils.getGeneByHugoSymbol(subGeneStr);
                if (tmpGene != null) {
                    allGenes.add(tmpGene);
                }
            }
            map.put("allGenes", allGenes);
        }

        map.put("pickedGene", gene);
        map.put("relevantAlts", relevantAlterations);
        return map;
    }
}

class IndicatorQueryRespComp implements Comparator<IndicatorQueryResp> {

    public IndicatorQueryRespComp() {
    }

    @Override
    public int compare(IndicatorQueryResp e1, IndicatorQueryResp e2) {
        Integer result = LevelUtils.compareLevel(e1.getHighestSensitiveLevel(), e2.getHighestSensitiveLevel());
        if (result != 0) {
            return result;
        }

        result = LevelUtils.compareLevel(e1.getHighestResistanceLevel(), e2.getHighestResistanceLevel());
        if (result != 0) {
            return result;
        }

        result = MainUtils.compareOncogenicity(Oncogenicity.getByEffect(e1.getOncogenic()), Oncogenicity.getByEffect(e2.getOncogenic()), true);

        if (result != 0) {
            return result;
        }

        if (e1.getGeneExist() == null || !e1.getGeneExist()) {
            return 1;
        }

        if (e2.getGeneExist() == null || !e2.getGeneExist()) {
            return -1;
        }
        return -1;
    }
}

class IndicatorQueryOncogenicity {
    Oncogenicity oncogenicity;
    Evidence oncogenicityEvidence;

    public IndicatorQueryOncogenicity(Oncogenicity oncogenicity, Evidence oncogenicityEvidence) {
        this.oncogenicity = oncogenicity;
        this.oncogenicityEvidence = oncogenicityEvidence;
    }

    public Oncogenicity getOncogenicity() {
        return oncogenicity;
    }

    public Evidence getOncogenicityEvidence() {
        return oncogenicityEvidence;
    }
}


class IndicatorQueryMutationEffect {
    MutationEffect mutationEffect;
    Evidence mutationEffectEvidence;

    public IndicatorQueryMutationEffect() {
    }

    public MutationEffect getMutationEffect() {
        return mutationEffect;
    }

    public Evidence getMutationEffectEvidence() {
        return mutationEffectEvidence;
    }

    public void setMutationEffect(MutationEffect mutationEffect) {
        this.mutationEffect = mutationEffect;
    }

    public void setMutationEffectEvidence(Evidence mutationEffectEvidence) {
        this.mutationEffectEvidence = mutationEffectEvidence;
    }
}
