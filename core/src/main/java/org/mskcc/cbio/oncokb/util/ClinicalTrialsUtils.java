package org.mskcc.cbio.oncokb.util;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.json.simple.parser.ParseException;
import org.mskcc.cbio.oncokb.clinicaltrials.Status;
import org.mskcc.cbio.oncokb.model.SpecialTumorType;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.Arms;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.ClinicalTrial;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.ClinicalTrialMap;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.Coordinates;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.GenericMapClass;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.Site;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.TumorMap;
import org.springframework.util.ResourceUtils;

/**
 * Created by Yifu Yao on 3/9/2021
 */
public class ClinicalTrialsUtils {

    private static ClinicalTrialsUtils instance = null;

    public ClinicalTrialsUtils() {}

    public static ClinicalTrialsUtils getInstance() {
        if (instance == null) {
            instance = new ClinicalTrialsUtils();
        }
        return instance;
    }

    private static final String LOCAL_DIR = "/data/clinicalTrials/";
    private static final String MAPPING_RESULT_FILE = "result.json.gz";
    private static final String SITES_FILE = "sites.json.gz";
    private static final String TRIALS_FILE = "trials.json.gz";
    private static final String CLASSPATH = "classpath:";

    public boolean isFilesConfigured() {
        return (
            S3Utils.getInstance().isPropertiesConfigured() ||
            isLocalFilesExisted()
        );
    }

    public boolean isLocalFilesExisted() {
        return (
            ResourceUtils.isUrl(CLASSPATH + LOCAL_DIR + MAPPING_RESULT_FILE) &&
            ResourceUtils.isUrl(CLASSPATH + LOCAL_DIR + SITES_FILE) &&
            ResourceUtils.isUrl(CLASSPATH + LOCAL_DIR + TRIALS_FILE)
        );
    }

    public Map<String, TumorMap> loadMappingResult() throws Exception {
        Map<String, TumorMap> result = new HashMap<>();
        result =
            new GenericMapClass<TumorMap>(
                new TypeToken<Map<String, TumorMap>>() {}
            )
            .getMap(MAPPING_RESULT_FILE);
        return result;
    }

    public Map<String, Site> loadSitesMap() throws Exception {
        Map<String, Site> sites = new HashMap<>();
        sites =
            new GenericMapClass<Site>(new TypeToken<Map<String, Site>>() {})
            .getMap(SITES_FILE);
        return sites;
    }

    public Map<String, ClinicalTrialMap> loadTrialsMap() throws Exception {
        Map<String, ClinicalTrialMap> trials = new HashMap<>();
        trials =
            new GenericMapClass<ClinicalTrialMap>(
                new TypeToken<Map<String, ClinicalTrialMap>>() {}
            )
            .getMap(TRIALS_FILE);
        return trials
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().getPreviousTrialStatus() == null)
            .collect(
                Collectors.toMap(
                    entry -> entry.getKey(),
                    entry -> entry.getValue()
                )
            );
    }

    public Map<String, TumorMap> getAllMappingResult() {
        return CacheUtils.getAllClinicalTrialsMappingResult();
    }

    public Map<String, Site> getAllSites() {
        return CacheUtils.getAllClinicalTrialsSites();
    }

    public Map<String, ClinicalTrialMap> getAllTrials() {
        return CacheUtils.getAllClinicalTrials();
    }

    public Site getSite(String siteKey) {
        Map<String, Site> sites = getAllSites();
        Site site = new Site();
        if (sites.containsKey(siteKey)) {
            site = sites.get(siteKey);
        }
        return site;
    }

    public Set<ClinicalTrialMap> filterTrialsByTreatment(
        Set<ClinicalTrialMap> trials,
        String treatment
    ) {
        if (treatment != null && !treatment.isEmpty()) {
            Set<ClinicalTrialMap> res = new HashSet<>();
            Set<String> drugs = Arrays
                .stream(treatment.split("\\+"))
                .map(item -> item.trim().toLowerCase())
                .collect(Collectors.toSet());
            res = filterTrialsByDrugNameOrCode(trials, drugs);
            return res;
        }
        return trials;
    }

    public Set<ClinicalTrialMap> filterTrialsByDrugNameOrCode(
        Set<ClinicalTrialMap> trials,
        Set<String> drugs
    ) {
        if (!drugs.isEmpty()) {
            Set<ClinicalTrialMap> res = new HashSet<>();
            drugs =
                drugs
                    .stream()
                    .map(drug -> drug.toLowerCase())
                    .collect(Collectors.toSet());
            for (ClinicalTrialMap trial : trials) {
                for (Arms arm : trial.getArms()) {
                    if (
                        Stream
                            .concat(
                                arm
                                    .getDrugs()
                                    .stream()
                                    .map(
                                        drug -> drug.getNcitCode().toLowerCase()
                                    ),
                                arm
                                    .getDrugs()
                                    .stream()
                                    .map(
                                        drug -> drug.getDrugName().toLowerCase()
                                    )
                            )
                            .collect(Collectors.toSet())
                            .containsAll(drugs)
                    ) {
                        res.add(trial);
                        break;
                    }
                }
            }
            return res;
        }
        return trials;
    }

    public Set<ClinicalTrialMap> filterTrialsBySpecialCancerType(
        SpecialTumorType specialTumorType
    ) {
        Set<ClinicalTrialMap> trials = new HashSet<>();
        if (specialTumorType == null) return trials;

        TumorType matchedSpecialTumorType = TumorTypeUtils.getBySpecialTumor(
            specialTumorType
        );
        if (matchedSpecialTumorType == null) return trials;

        switch (specialTumorType) {
            case ALL_TUMORS:
                return getAllTrials()
                    .values()
                    .stream()
                    .collect(Collectors.toSet());
            case ALL_SOLID_TUMORS:
            case ALL_LIQUID_TUMORS:
                return TumorTypeUtils
                    .getAllTumorTypes()
                    .stream()
                    .filter(
                        tumorType ->
                            tumorType.getTumorForm() != null &&
                            tumorType
                                .getTumorForm()
                                .equals(matchedSpecialTumorType.getTumorForm())
                    )
                    .map(
                        tumorType ->
                            filterTrialsByCancerType(
                                StringUtils.isNotEmpty(tumorType.getSubtype())
                                    ? tumorType.getSubtype()
                                    : tumorType.getMainType()
                            )
                    )
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            default:
                return trials;
        }
    }

    public Set<ClinicalTrialMap> filterTrialsByCancerType(String cancerType) {
        Map<String, TumorMap> tumors = getAllMappingResult();
        Set<ClinicalTrialMap> trials = new HashSet<>();

        Set<String> tumorCodesByMainType = new HashSet<>();
        List<TumorType> allOncoTreeSubtypes = TumorTypeUtils.getAllSubtypes();
        for (TumorType oncoTreeType : allOncoTreeSubtypes) {
            if (
                (
                    oncoTreeType.getMainType() != null &&
                    cancerType.equalsIgnoreCase(oncoTreeType.getMainType())
                ) ||
                (
                    oncoTreeType.getCode() != null &&
                    cancerType.equalsIgnoreCase(oncoTreeType.getCode())
                )
            ) {
                tumorCodesByMainType.add(oncoTreeType.getCode());
            }
        }
        if (tumorCodesByMainType.size() > 0) {
            for (String code : tumorCodesByMainType) {
                if (tumors.containsKey(code)) {
                    trials.addAll(
                        getTrialsByIDList(tumors.get(code).getTrials())
                    );
                }
            }
        } else {
            TumorType matchedSubtype = TumorTypeUtils.getBySubtype(cancerType);
            if (matchedSubtype != null) {
                String codeByName = matchedSubtype.getCode();
                if (tumors.containsKey(codeByName)) {
                    trials.addAll(
                        getTrialsByIDList(tumors.get(codeByName).getTrials())
                    );
                }
            } else { // cancertype is oncotree code
                if (tumors.containsKey(cancerType)) {
                    trials.addAll(
                        getTrialsByIDList(tumors.get(cancerType).getTrials())
                    );
                }
            }
        }

        // Include the trials which have been marked to apply all tumors/all solid tumors/all liquid tumors
        TumorType matchedTumorType = TumorTypeUtils.getByName(cancerType);
        if (matchedTumorType != null) {
            if (TumorTypeUtils.isSolidTumor(matchedTumorType) && tumors.containsKey(SpecialTumorType.ALL_SOLID_TUMORS.getTumorType())) {
                trials.addAll(
                    getTrialsByIDList(tumors.get(SpecialTumorType.ALL_SOLID_TUMORS.getTumorType()).getTrials())
                );
            } else if (TumorTypeUtils.isLiquidTumor(matchedTumorType) && tumors.containsKey(SpecialTumorType.ALL_LIQUID_TUMORS.getTumorType())) {
                trials.addAll(
                    getTrialsByIDList(tumors.get(SpecialTumorType.ALL_LIQUID_TUMORS.getTumorType()).getTrials())
                );
            }
        }

        if (tumors.containsKey(SpecialTumorType.ALL_LIQUID_TUMORS.getTumorType())) {
            trials.addAll(
                getTrialsByIDList(tumors.get(SpecialTumorType.ALL_TUMORS.getTumorType()).getTrials())
            );
        }

        return trials;
    }

    public Set<ClinicalTrialMap> getTrialsByIDList(List<String> ids) {
        return ids
            .stream()
            .map(id -> getAllTrials().get(id))
            .collect(Collectors.toSet());
    }

    public Set<ClinicalTrial> replaceKeysWithSites(
        Set<ClinicalTrialMap> input
    ) {
        Set<ClinicalTrial> res = new HashSet<>();
        for (ClinicalTrialMap trial : input) {
            ClinicalTrial add = new ClinicalTrial();
            add.setArms(trial.getArms());
            add.setBriefTitle(trial.getBriefTitle());
            add.setCollaborators(trial.getCollaborators());
            add.setCurrentTrialStatus(trial.getCurrentTrialStatus());
            add.setCurrentTrialStatusDate(trial.getCurrentTrialStatusDate());
            add.setEligibility(trial.getEligibility());
            add.setNctId(trial.getNctId());
            add.setPhase(trial.getPhase());
            add.setPreviousTrialStatus(trial.getPreviousTrialStatus());
            add.setPreviousTrialStatusDate(trial.getPreviousTrialStatusDate());
            add.setPrincipalInvestigator(trial.getPrincipalInvestigator());
            List<Site> sites = new ArrayList<>();
            trial
                .getSites()
                .stream()
                .forEach(key -> sites.add(getAllSites().get(key)));
            add.setSites(sites);
            res.add(add);
        }
        return res;
    }

    public Set<ClinicalTrialMap> filterTrialsByLocation(
        Set<ClinicalTrialMap> trials,
        String location,
        Double distance
    ) {
        if (location != null) {
            if (distance == null) distance = 100.0;
            Set<ClinicalTrialMap> res = new HashSet<>();
            Coordinates ori = OpenStreetMapUtils
                .getInstance()
                .getCoordinates(location);
            for (ClinicalTrialMap trial : trials) {
                for (String siteKey : trial.getSites()) {
                    Site site = getSite(siteKey);
                    Coordinates des = site.getOrg().getCoordinates();
                    if (des == null) {
                        continue;
                    }
                    if (
                        OpenStreetMapUtils
                            .getInstance()
                            .calculateDistance(ori, des) <=
                        distance
                    ) {
                        res.add(trial);
                        break;
                    }
                }
            }
            return res;
        }
        return trials;
    }

    public List<ClinicalTrial> filterClinicalTrialsByLocation(
        List<ClinicalTrial> trials,
        String location,
        Double distance
    ) {
        if (location != null) {
            if (distance == null) distance = 100.0;
            List<ClinicalTrial> res = new ArrayList<>();
            Coordinates ori = OpenStreetMapUtils
                .getInstance()
                .getCoordinates(location);
            for (ClinicalTrial trial : trials) {
                for (Site site : trial.getSites()) {
                    Coordinates des = site.getOrg().getCoordinates();
                    if (des == null) {
                        continue;
                    }
                    if (
                        OpenStreetMapUtils
                            .getInstance()
                            .calculateDistance(ori, des) <=
                        distance
                    ) {
                        res.add(trial);
                        break;
                    }
                }
            }
            return res;
        }
        return trials;
    }

    public Set<ClinicalTrialMap> filterTrialsByTreatmentAndLocation(
        Set<ClinicalTrialMap> trials,
        String treatment,
        String location,
        Double distance
    ) {
        trials = filterTrialsByTreatment(trials, treatment);
        trials = filterTrialsByLocation(trials, location, distance);
        return trials;
    }

    public Set<ClinicalTrialMap> filterTrialsByTreatmentForIndicatorQueryTreatment(
        String cancerType,
        Set<String> drugs
    ) {
        Set<ClinicalTrialMap> trials = filterTrialsByCancerType(cancerType);
        Set<ClinicalTrialMap> res = filterTrialsByDrugNameOrCode(
            trials,
            drugs
        );
        return res.stream().filter(map -> acceptableStatus(map.getCurrentTrialStatus())).collect(Collectors.toSet());
    }

    private boolean acceptableStatus(String statusTerm) {
        Set<Status> acceptableStatuses = new HashSet<>();
        acceptableStatuses.add(Status.ACTIVE);
        acceptableStatuses.add(Status.TEM_CLOSED_ACCURAL);
        acceptableStatuses.add(Status.ENROLLING_BY_INVITATION);
        acceptableStatuses.add(Status.NA);
        acceptableStatuses.add(Status.ENROLLING_BY_INVITATION);
        acceptableStatuses.add(Status.TEM_CLOSED_ACCURAL_INTERVENTION);
        acceptableStatuses.add(Status.IN_REVIEW);
        acceptableStatuses.add(Status.APPROVED);

        Status status = Status.getByTerm(statusTerm);
        return status != null && acceptableStatuses.contains(status);
    }
}
