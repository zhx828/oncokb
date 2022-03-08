package org.mskcc.cbio.oncokb.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.genome_nexus.client.Hotspot;
import org.genome_nexus.client.IntegerRange;
import org.genome_nexus.client.ProteinLocation;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.AlterationPositionBoundary;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mskcc.cbio.oncokb.Constants.MISSENSE_VARIANT;
import static org.mskcc.cbio.oncokb.util.HotspotUtils.extractProteinPos;
import static org.mskcc.cbio.oncokb.util.VariantConsequenceUtils.toGNMutationType;

/**
 * Created by Hongxin on 11/03/16.
 */

public class HotspotUtils {
    private static final String HOTSPOT_FILE_PATH = "/data/cancer-hotspots-gn.json";
    private static Map<Gene, List<EnrichedHotspot>> hotspotMutations = new HashMap<>();

    static {
        System.out.println("Cache all hotspots at " + MainUtils.getCurrentTime());
        getHotspotsFromDataFile();
    }

    private static void getHotspotsFromDataFile() {
        List<EnrichedHotspot> hotspots = new ArrayList<>();
        Gson gson = new GsonBuilder().create();
        Hotspot[] mutations = gson.fromJson(new BufferedReader(new InputStreamReader(HotspotUtils.class.getResourceAsStream(HOTSPOT_FILE_PATH))), Hotspot[].class);
        for (int i = 0; i < mutations.length; i++) {
            EnrichedHotspot enrichedHotspot = new EnrichedHotspot(mutations[i]);
            hotspots.add(enrichedHotspot);
        }
        parseData(hotspots);
    }

    private static void parseData(List<EnrichedHotspot> hotspots) {
        if (hotspots != null) {
            for (EnrichedHotspot hotspotMutation : hotspots) {
                Gene gene = GeneUtils.getGeneByHugoSymbol(hotspotMutation.getHugoSymbol());
                if (gene != null) {
                    if (!hotspotMutations.containsKey(gene)) {
                        hotspotMutations.put(gene, new ArrayList<EnrichedHotspot>());
                    }
                    hotspotMutations.get(gene).add(hotspotMutation);
                }
            }
        }
    }

    public static boolean isHotspot(Alteration alteration) {
        if (alteration == null || alteration.getGene() == null || alteration.getProteinStart().intValue() == AlterationPositionBoundary.START.getValue() || alteration.getProteinEnd().intValue() == AlterationPositionBoundary.END.getValue()) {
            return false;
        }

        // There are few genes we cannot map to GRCh38 yet
        Set<String> notMappedHugos = new HashSet<>();
        notMappedHugos.add("MYD88");
        notMappedHugos.add("TET3");
        notMappedHugos.add("RYBP");
        notMappedHugos.add("WT1");
        if (notMappedHugos.contains(alteration.getGene().getHugoSymbol()) && !alteration.getReferenceGenomes().contains(ReferenceGenome.GRCh37)) {
            return false;
        }

        AlterationUtils.annotateAlteration(alteration, alteration.getAlteration());

        ProteinLocation proteinLocation = new ProteinLocation();
        proteinLocation.setStart(alteration.getProteinStart());
        proteinLocation.setEnd(alteration.getProteinEnd());
        proteinLocation.setMutationType(toGNMutationType(alteration.getConsequence()));
        List<EnrichedHotspot> hotspots = new ArrayList<>();

        if (hotspotMutations.get(alteration.getGene()) == null) {
            return false;
        }

        // for alteration that is missense but ends as mis, it is a range mutation
        if(alteration.getConsequence() != null && alteration.getConsequence().equals(VariantConsequenceUtils.findVariantConsequenceByTerm(MISSENSE_VARIANT)) && alteration.getAlteration().endsWith("mis")) {
            return false;
        }

        for (EnrichedHotspot hotspot : hotspotMutations.get(alteration.getGene())) {
            if (!hotspot.getType().equals("3d")) {
                hotspots.add(hotspot);
            }
        }
        return proteinLocationHotspotsFilter(hotspots, proteinLocation, alteration.getRefResidues()).size() > 0;
    }

    // Logic from GN
    private static List<Hotspot> proteinLocationHotspotsFilter(List<EnrichedHotspot> hotspots, ProteinLocation proteinLocation, String referenceResidues) {
        int start = proteinLocation.getStart();
        int end = proteinLocation.getEnd();
        String type = proteinLocation.getMutationType();
        List<Hotspot> result = new ArrayList<>();

        for (EnrichedHotspot hotspot : hotspots) {
            boolean validPosition = true;

            // Protein location
            int hotspotStart = hotspot.getStart();
            int hotspotStop = hotspot.getEnd();
            validPosition &= (start <= hotspotStop && end >= hotspotStart);

            // Mutation type
            boolean validMissense = type.equals("Missense_Mutation") && (hotspot.getType().contains("3d") || hotspot.getType().contains("single residue"));
            boolean validInFrameInsertion = type.equals("In_Frame_Ins") && (hotspot.getType().contains("in-frame"));
            boolean validInFrameDeletion = type.equals("In_Frame_Del") && (hotspot.getType().contains("in-frame"));
            boolean validSplice = (type.equals("Splice_Site") || type.equals("Splice_Region")) && (hotspot.getType().contains("splice"));

            // Add hotspot
            if (validPosition && (validMissense || validInFrameInsertion || validInFrameDeletion || validSplice)) {
                if(validMissense) {
                    boolean validReferenceResidues = (referenceResidues + proteinLocation.getStart()).equals(hotspot.getResidue());
                    if (validReferenceResidues) {
                        result.add(hotspot);
                    }
                } else {
                    result.add(hotspot);
                }
            }
        }

        return result;
    }

    public static List<EnrichedHotspot> getAllHotspots() {
        List<EnrichedHotspot> allHotspots = new ArrayList<>();
        for (Map.Entry<Gene, List<EnrichedHotspot>> entry : hotspotMutations.entrySet()) {
            allHotspots.addAll(entry.getValue());
        }
        return allHotspots;
    }

    public static IntegerRange extractProteinPos(String proteinChange) {
        IntegerRange proteinPos = null;
        Integer start = -1;
        Integer end = -1;

        List<Integer> positions = extractPositiveIntegers(proteinChange);

        // ideally positions.size() should always be 2
        if (positions.size() >= 2) {
            start = positions.get(0);
            end = positions.get(positions.size() - 1);
        }
        // in case no end point, use start as end
        else if (positions.size() > 0) {
            start = end = positions.get(0);
        }

        if (!start.equals(-1)) {
            proteinPos = new IntegerRange();
            proteinPos.setStart(start);
            proteinPos.setEnd(end);
        }

        return proteinPos;
    }

    private static List<Integer> extractPositiveIntegers(String input) {
        if (input == null) {
            return Collections.emptyList();
        }

        List<Integer> list = new ArrayList<>();
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(input);

        while (m.find()) {
            list.add(Integer.parseInt(m.group()));
        }

        return list;
    }
}
