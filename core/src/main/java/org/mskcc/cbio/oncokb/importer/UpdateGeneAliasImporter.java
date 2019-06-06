package org.mskcc.cbio.oncokb.importer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.util.*;

import java.io.IOException;
import java.util.*;

/**
 * @author Hongxin Zhang
 */
public class UpdateGeneAliasImporter {
    private static final String REMOVE_GENE_ASSOCIATION = "/data/remove-alias-association.tsv";

    private UpdateGeneAliasImporter() {
        throw new AssertionError();
    }

    public static void main(String[] args) {
        List<Gene> genes = ApplicationContextSingleton.getGeneBo().findAll();

        for (Gene gene : genes) {
            gene.setGeneAliases(new HashSet<>(PortalGeneService.findGeneAliases(Integer.toString(gene.getEntrezGeneId()))));
            ApplicationContextSingleton.getGeneBo().saveOrUpdate(gene);
        }

        removeAliasAssociation(genes);
    }

    private static void removeAliasAssociation(List<Gene> allGenes) {
        try {
            List<String> lines = FileUtils.readTrimedLinesStream(
                UpdateGeneAliasImporter.class.getResourceAsStream(REMOVE_GENE_ASSOCIATION));
            for (String line : lines) {
                if (!line.startsWith("#") && line.trim().length() > 0) {
                    String parts[] = line.split("\t");
                    if (parts.length < 2) {
                        throw new IllegalArgumentException("The row should have two elements. Current case: " + line);
                    }
                    Integer entrezGeneId = Integer.parseInt(parts[0]);
                    String geneAlias = parts[1];
                    if (entrezGeneId != null) {
                        Optional<Gene> optionalGene = allGenes.stream().filter(g -> g.getEntrezGeneId().intValue() == entrezGeneId.intValue()).findFirst();
                        if (optionalGene.isPresent()) {
                            optionalGene.get().getGeneAliases().remove(geneAlias);
                            ApplicationContextSingleton.getGeneBo().update(optionalGene.get());
                            System.out.println("INFO: removed " + geneAlias + " from " + optionalGene.get().getHugoSymbol());
                        } else {
                            System.out.println("WARNING: entrezGeneId does not exist. " + line);
                        }
                    } else {
                        System.out.println("ERROR: entrezGeneId should be integer. " + line);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to read the file.");
        }
    }
}
