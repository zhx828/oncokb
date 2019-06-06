package org.mskcc.cbio.oncokb.importer;

/**
 * Created by Hongxin on 5/26/16.
 */
public final class GeneralImporter {

    public static void main(String[] args) throws Exception {
//        MetaDumpImporter.main(args);
//        VariantConsequenceImporter.main(args);
//        PortalAlterationImporter.main(args);

        // At the moment, we should have all the genes in the database.
        // Next step will be validating the genes and populate the gene alias list
        UpdateGenesFromPortalImporter.main(args);
        UpdateGeneAliasImporter.main(args);

    }
}
