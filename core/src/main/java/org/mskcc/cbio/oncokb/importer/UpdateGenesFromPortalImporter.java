
package org.mskcc.cbio.oncokb.importer;

import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.PortalGeneService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author jiaojiao Sep/8/2017 Import alteration data from portal database
 */
public class UpdateGenesFromPortalImporter {

    public static void main(String[] args) throws Exception {
        List<Gene> genes = ApplicationContextSingleton.getGeneBo().findAll().stream().filter(gene -> gene.getEntrezGeneId().intValue() > 0).collect(Collectors.toList());
        if (genes.size() == 0) {
            return;
        }
        List<Gene> portalGenes = PortalGeneService.findGenes(genes.stream().map(gene -> gene.getEntrezGeneId().toString()).collect(Collectors.toList()));

        for (Gene gene : genes) {
            Optional<Gene> matchPortalGene = portalGenes.stream().filter(portalGene -> portalGene.getEntrezGeneId().intValue() == gene.getEntrezGeneId().intValue()).findFirst();
            if (!matchPortalGene.isPresent()) {
                System.out.println("ERROR: the gene is not available in portal. " + gene.getEntrezGeneId() + "/" + gene.getHugoSymbol());
            } else {
                Gene matchedGene = matchPortalGene.get();
                if (!matchedGene.getHugoSymbol().equals(gene.getHugoSymbol())) {
                    System.out.println("WARNING: the gene hugo symbol is updated, updating from " + gene.getHugoSymbol() + " to " + matchedGene.getHugoSymbol());
                    gene.setHugoSymbol(matchedGene.getHugoSymbol());
                    ApplicationContextSingleton.getGeneBo().update(gene);
                }
            }
        }
    }
}
