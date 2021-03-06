package org.mskcc.cbio.oncokb.importer;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.FileUtils;
import org.mskcc.cbio.oncokb.util.GeneUtils;

import java.io.IOException;
import java.util.List;

public class ChangeHugoSymbol {
    private ChangeHugoSymbol() {
        throw new AssertionError();
    }

    private static final String CHANGE_HUGO_FILE = "/data/change-hugo-symbol.txt";

    public static void main(String[] args) throws IOException {

        List<String> lines = FileUtils.readTrimedLinesStream(
            VariantConsequenceImporter.class.getResourceAsStream(CHANGE_HUGO_FILE));

        int nLines = lines.size();
        System.out.println("importing...");
        for (int i = 0; i < nLines; i++) {
            String line = lines.get(i);
            if (line.startsWith("#")) continue;

            String[] parts = line.split("\t");

            String oldHugo = parts[0];
            String newHugo = parts[1];

            System.out.println("Update " + oldHugo + " to " + newHugo);
            Gene gene = GeneUtils.getGeneByHugoSymbol(oldHugo);
            if (gene == null) {
                System.out.println("Gene is not available: " + oldHugo);
            } else {
                if (!gene.getGeneAliases().contains(oldHugo)) {
                    gene.getGeneAliases().add(oldHugo);
                }
                gene.setHugoSymbol(newHugo);
                ApplicationContextSingleton.getGeneBo().update(gene);
            }
        }
    }
}
