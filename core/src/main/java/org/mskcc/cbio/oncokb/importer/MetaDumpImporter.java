/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.importer;

import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.PortalAlteration;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.FileUtils;
import org.mskcc.cbio.oncokb.util.PropertiesUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

/**
 * @author jiaojiao Sep/8/2017 Import alteration data from portal database
 */
public class MetaDumpImporter {
    static final String META_DUMP_PATH = "importer.meta_dump_path";
    static final String JDBC_URL = "jdbc.url";

    public static void main(String[] args) throws Exception {

        URI jdbcUrl = parseJdbcUrl();
        if (jdbcUrl != null) {
            String host = jdbcUrl.getHost();
            String[] pathFragments = jdbcUrl.getPath().trim().substring(1).split("/\\?/");
            if (pathFragments.length == 0) {
                throw new Exception("You need to specify a database name");
            }
            String dbName = pathFragments[0];

            String metaDumpPath = PropertiesUtils.getProperties(META_DUMP_PATH);
            if (metaDumpPath != null) {
                Runtime rt = Runtime.getRuntime();
                Process pr = rt.exec("mysql -p -h " + host + " " + dbName + " < " + metaDumpPath);
            }
        }
    }

    private static URI parseJdbcUrl() {
        Properties dbProperties = new Properties();
        InputStream inputStream = GeneralImporter.class.getClassLoader().getResourceAsStream("properties/database.properties");
        try {
            if (inputStream == null) {
                throw new Exception("No input stream can be found.");
            }
            dbProperties.load(inputStream);
        } catch (Exception e) {
            System.out.println("database.properties cannot be found in the classpath");
        }
        if (dbProperties.get(JDBC_URL) != null) {
            String url = dbProperties.getProperty(JDBC_URL);
            String cleanURI = url.substring(5);
            return URI.create(cleanURI);
        } else {
            return null;
        }
    }
}
