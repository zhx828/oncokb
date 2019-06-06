package org.mskcc.cbio.oncokb.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.model.Gene;

/**
 * @author jgao
 */
public final class PortalGeneService {
    private PortalGeneService() {
        throw new AssertionError();
    }

    private static final String CBIOPORTAL_GENES_ENDPOINT = "https://www.cbioportal.org/api/genes";

    public static Gene findGene(String geneId) {
        try {
            String response = HttpUtils.getRequest(CBIOPORTAL_GENES_ENDPOINT + "/" + geneId);
            return getGene(new JSONObject(response));
        } catch (IOException e) {
            System.out.println("Something goes wrong while fetching cBioPortal service");
            System.out.println(e);
            e.printStackTrace();
            return null;
        }
    }

    public static List<Gene> findGenes(List<String> geneIds) {
        List<Gene> genes = new ArrayList<>();
        try {
            String response = HttpUtils.postRequest(CBIOPORTAL_GENES_ENDPOINT + "/fetch", new Gson().toJson(geneIds));
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                Gene gene = getGene(jsonArray.getJSONObject(i));
                if (gene != null) {
                    genes.add(gene);
                }
            }
        } catch (IOException e) {
            System.out.println("Something goes wrong while fetching cBioPortal service");
            System.out.println(e);
            e.printStackTrace();
        }
        return genes;
    }

    public static List<String> findGeneAliases(String geneId) {
        try {
            List<String> aliases = new ArrayList<>();
            String response = HttpUtils.getRequest(CBIOPORTAL_GENES_ENDPOINT + "/" + geneId + "/aliases");
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                aliases.add(jsonArray.getString(i));
            }
            return aliases;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static Gene getGene(JSONObject jsonObj) {
        if (!jsonObj.has("hugoGeneSymbol") || !jsonObj.has("entrezGeneId")) {
            System.out.println("The gene model is not appropriate from cBioPortal" + jsonObj.toString());
            return null;
        } else {
            Gene gene = new Gene();
            gene.setHugoSymbol(jsonObj.getString("hugoGeneSymbol"));
            gene.setEntrezGeneId(jsonObj.getInt("entrezGeneId"));
            return gene;
        }
    }

}
