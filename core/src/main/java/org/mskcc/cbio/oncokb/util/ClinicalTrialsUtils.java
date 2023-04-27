package org.mskcc.cbio.oncokb.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.mskcc.cbio.oncokb.model.SpecialTumorType;
import org.mskcc.cbio.oncokb.model.TumorForm;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMatching.*;

import static org.mskcc.cbio.oncokb.util.CacheUtils.getAllTrialsBySpecialTumorType;

public class ClinicalTrialsUtils {


    public static List<Trial> getTrials(String treatment, String cancerType)
        throws IOException, ParseException {

        Map<String, Trial> trialsMapping = CacheUtils.getTrialsMapping();
        Map<String, Tumor>  oncotreeMapping = CacheUtils.getOncoTreeMappingTrials();

        SpecialTumorType specialTumorType = SpecialTumorType.getByTumorType(cancerType);
        if(specialTumorType != null) {
            List<Trial> trials = getTrialsForSpecialCancerType(specialTumorType);
            if (treatment != null) {
                trials = getTrialByTreatment(trials,treatment);
            }
            return new ArrayList<Trial>(trials);
        } else {
            List<TumorType> tumorTypes = TumorTypeUtils.findRelevantTumorTypes(cancerType);
            List<Trial> trials = new ArrayList<>();
            Boolean cancerTypeInTumorTypes = false;
            for (TumorType tumorType: tumorTypes) {
                String mainType = tumorType.getMainType();
                trials.addAll(getTrialsByCancerType(oncotreeMapping, trialsMapping, mainType));
                if (mainType == cancerType) {
                    cancerTypeInTumorTypes = true;
                }
            }
            if (!cancerTypeInTumorTypes) {
                trials.addAll(getTrialsByCancerType(oncotreeMapping, trialsMapping, cancerType.toLowerCase()));
            }
            if (treatment != null) {
                trials = getTrialByTreatment(trials,treatment);
            }
            return new ArrayList<Trial>(trials);
        }
    }

    public Map<String, List<Trial>> getTrialsByCancerTypes(CancerTypesQuery body)
        throws UnsupportedEncodingException, IOException, ParseException {
        Map<String, List<Trial>> result = new HashMap<>();

        Map<String, Trial> trialsMapping = CacheUtils.getTrialsMapping();
        Map<String, Tumor> oncotreeMapping = CacheUtils.getOncoTreeMappingTrials();

            Set<String> cancerTypes = new HashSet<>(body.getCancerTypes());
            if (cancerTypes.contains(SpecialTumorType.ALL_TUMORS.getTumorType())) {
                List<Trial> trials = new ArrayList<>();
                Set<String> nctIDSet = new HashSet<>();
                for (Object item : oncotreeMapping.keySet()) {
                    String oncoTreeCode = (String) item;
                    Tumor tumor = getTumor(oncotreeMapping, trialsMapping, oncoTreeCode);

                    for (Trial curTrial : tumor.getTrials()) {
                        if (!nctIDSet.contains(curTrial.getNctId())) {
                            nctIDSet.add(curTrial.getNctId());
                            trials.add(curTrial);
                        }
                    }
                }
                result.put(SpecialTumorType.ALL_TUMORS.getTumorType(), trials);
                return result;
            }

            for (String cancerType : cancerTypes) {
                Set<String> nctIDSet = new HashSet<>();
                List<Trial> addTrials = new ArrayList<>();
                List<Trial> trials = new ArrayList<>();
                SpecialTumorType specialTumorType = ApplicationContextSingleton.getTumorTypeBo().getSpecialTumorTypeByName(cancerType);
                if (specialTumorType != null) {
                    trials = getTrialsForSpecialCancerType(specialTumorType);
                } else {
                    trials = getTrialsByCancerType(oncotreeMapping, trialsMapping, cancerType);
                }
                for (Trial trial : trials) {
                    if (!nctIDSet.contains(trial.getNctId())) {
                        nctIDSet.add(trial.getNctId());
                        addTrials.add(trial);
                    }
                }
                result.put(cancerType, addTrials);
        }
        return result;
    }

    public static Tumor getTumor(Map<String, Tumor> oncotreeMapping, Map<String, Trial> trialsMapping, String oncoTreeCode) {
        Tumor tumor = new Tumor();
        if (oncotreeMapping.containsKey(oncoTreeCode)) {
            tumor = oncotreeMapping.get(oncoTreeCode);

            Set<String> nctIDList = tumor.getTrials().stream().map(trial -> trial.getNctId()).collect(Collectors.toSet());

            List<Trial> trialsInfo = new ArrayList<>();
            for (String nctID : nctIDList) {
                if (trialsMapping.containsKey(nctID)) {
                    Trial trial = trialsMapping.get(nctID);
                    if (trial != null) {
                        trialsInfo.add(trial);
                    } else {
                        System.out.println("We do not have trial info for " + nctID);
                    }
                }
            }
            tumor.setTrials(trialsInfo);
        }

        return tumor;
    }

    public static List<Trial> getTrialByTreatment(List<Trial> trials, String treatment) {
        List<Trial> res = new ArrayList<>();
        Set<String> drugsNames = Arrays.stream(treatment.split(",|\\+")).map(item -> item.trim()).collect(Collectors.toSet());

        res = getTrialsByDrugName(trials, drugsNames);
        return res;
    }

    private static List<Trial> getTrialsByDrugName(List<Trial> trials, Set<String> drugsNames) {
        List<Trial> res = new ArrayList<>();
        for (Trial trial : trials) {
            List<Arm> arms = trial.getArms();
            if(arms != null && !arms.isEmpty()) {
                for (Arm arm : trial.getArms()) {
                    List<Drug> drugs = arm.getDrugs();
                    if (drugs != null && !drugs.isEmpty()) {
                        if (arm.getDrugs().stream().map(Drug::getDrugName).collect(Collectors.toSet()).containsAll(drugsNames)) {
                            res.add(trial);
                            break;
                        }
                    }
                }
            }
        }
        return res;
    }

    public static List<Trial> getTrialsForSpecialCancerType(SpecialTumorType specialTumorType) {
        List<Trial> trials = new ArrayList<>();
        if(specialTumorType == null) return trials;

        TumorType matchedSpecialTumorType = ApplicationContextSingleton.getTumorTypeBo().getBySpecialTumor(specialTumorType);
        if (matchedSpecialTumorType == null) return trials;

        return new ArrayList<>(getAllTrialsBySpecialTumorType(specialTumorType));
    }

    public static List<Trial> getTrialsByCancerType(Map<String, Tumor>  oncotreeMapping, Map<String, Trial>  trialsMapping, String cancerType) {
        List<Trial> trials = new ArrayList<>();

        Set<String> tumorCodesByMainType = new HashSet<>();
        List<TumorType> allOncoTreeSubtypes = ApplicationContextSingleton.getTumorTypeBo().getAllSubtypes();
        for (TumorType oncoTreeType : allOncoTreeSubtypes) {
            if (oncoTreeType.getMainType() != null && cancerType.equalsIgnoreCase(oncoTreeType.getMainType())) {
                tumorCodesByMainType.add(oncoTreeType.getCode());
                tumorCodesByMainType.add(oncoTreeType.getMainType());
            }
        }
        if (!tumorCodesByMainType.contains(cancerType.toLowerCase())) {
            tumorCodesByMainType.add(cancerType.toLowerCase());
        }
        if (tumorCodesByMainType.size() > 0) {
            for (String code : tumorCodesByMainType) {
                if (oncotreeMapping.containsKey(code))
                    trials.addAll(getTumor(oncotreeMapping, trialsMapping, code).getTrials());
            }
        } else {
            TumorType matchedSubtype = ApplicationContextSingleton.getTumorTypeBo().getBySubtype(cancerType);
            if (matchedSubtype != null) {
                String codeByName = matchedSubtype.getCode();
                if (oncotreeMapping.containsKey(codeByName))
                    trials.addAll(getTumor(oncotreeMapping, trialsMapping, codeByName).getTrials());
            }
        }

        return trials;
    }

    public static Set<Trial> getAllTrials(Map<String, Tumor>  oncotreeMapping, Map<String, Trial>  trialsMapping) {
        Set<Trial> trials = new HashSet<>();

        oncotreeMapping.keySet().forEach(code -> {
            trials.addAll(getTumor(oncotreeMapping, trialsMapping, (String) code).getTrials());
        });
        return trials;
    }
}
