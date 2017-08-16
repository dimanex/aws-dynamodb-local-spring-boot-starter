/*
 * Copyright 2017 DiManEx B.V. . All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.dimanex;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.dimanex.spring.session.dynamodb.config.DynamoDBLocalStarterConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class DynamoDBLocalStarterApplication implements ApplicationRunner {

    // Informational parameters
    private static final String HELP_OPTION = "help";

    // Value parameters
    private static final String CORS_OPTION = "cors";
    private static final String DB_PATH_OPTION = "dbPath";
    private static final String PORT_OPTION = "port";

    // Flag parameters
    private static final String DTS_OPTION = "delayTransientStatuses";
    private static final String IN_MEMORY_OPTION = "inMemory";
    private static final String ODBBS_OPTION = "optimizeDbBeforeStartup";
    private static final String SHARED_DB_OPTION = "sharedDb";

    @Autowired
    private DynamoDBLocalStarterConfiguration config;

    public static void main(String... args) throws Exception {
        SpringApplication.run(DynamoDBLocalStarterApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (args.containsOption(HELP_OPTION)) {
            showHelp();
            return;
        }

        DynamoDBProxyServer server = null;
        try {
            String[] dDbLocalArgs = merge(args, config).toArray(new String[]{});
            log.info("DynamoDB Local args: {}", String.join(" ", dDbLocalArgs));
            server = ServerRunner.createServerFromCommandLineArgs(dDbLocalArgs);
            server.start();
            server.join();
        } finally {
            if (server != null) {
                server.stop();
            }
        }
    }

    private void showHelp() {
        final String lineSep = System.getProperty("line.separator");
        final String helpMessage = String.join(lineSep, "",
                "usage: ./mvnw spring-boot:run -Dsqlite4java.library.path=./native-deps -Drun" +
                        ".arguments=\"<OPTIONS_GO_HERE__READ_BELOW>\"",
                "",
                "            [--port=<port-no.>] [--inMemory] [--delayTransientStatuses]",
                "            [--dbPath=<path>][--sharedDb] [--cors=<allow-list>]",
                "",
                "--cors=<allow-list>          Enable CORS support for javascript against a",
                "                             specific allow-list list the domains separated",
                "                             by , use '*' for public access (default is",
                "                             '*')",
                "",
                "--dbPath=<path>              Specify the location of your database file.",
                "                             Default is the current directory.",
                "",
                "--delayTransientStatuses     When specified, DynamoDB Local will introduce",
                "                             delays to hold various transient table and",
                "                             index statuses so that it simulates actual",
                "                             service more closely. Currently works only for",
                "                             CREATING and DELETING online index statuses.",
                "",
                "--help                       Display DynamoDB Local usage and options.",
                "",
                "--inMemory                   When specified, DynamoDB Local will run in",
                "                             memory.",
                "",
                "--optimizeDbBeforeStartup    Optimize the underlying backing store database",
                "                             tables before starting up the server",
                "",
                "--port=<port-no.>            Specify a port number. Default is 8000",
                "",
                "--sharedDb                   When specified, DynamoDB Local will use a",
                "                             single database instead of separate databases",
                "                             for each credential and region. As a result,",
                "                             all clients will interact with the same set of",
                "                             tables, regardless of their region and",
                "                             credential configuration. (Useful for",
                "                             interacting with Local through the JS Shell in",
                "                             addition to other SDKs)",
                ""
        );

        log.info(helpMessage);
    }

    private List<String> merge(ApplicationArguments args, DynamoDBLocalStarterConfiguration config) {
        List<String> mergedArgs = new ArrayList<>();

        // Handle 'delayTransientStatuses' parameter
        if (args.containsOption(DTS_OPTION) || config.isDelayTransientStatuses()) {
            mergedArgs.add(optionify(DTS_OPTION));
        }

        // Handle 'inMemory' parameter
        if (!args.containsOption(DB_PATH_OPTION) && (args.containsOption(IN_MEMORY_OPTION) || config.isInMemory())) {
            mergedArgs.add(optionify(IN_MEMORY_OPTION));
        }

        // Handle 'optimizeDbBeforeStartup' parameter
        if (args.containsOption(ODBBS_OPTION) || config.isOptimizeDbBeforeStartup()) {
            mergedArgs.add(optionify(ODBBS_OPTION));
        }

        // Handle 'sharedDb'
        if (args.containsOption(SHARED_DB_OPTION) || config.isSharedDb()) {
            mergedArgs.add(optionify(SHARED_DB_OPTION));
        }

        // Handle 'cors' parameter
        if (args.containsOption(CORS_OPTION) && isValidParamValue(args.getOptionValues(CORS_OPTION))) {
            mergedArgs.add(optionify(CORS_OPTION));
            mergedArgs.add(args.getOptionValues(CORS_OPTION).get(0));
        } else {
            mergedArgs.add(optionify(CORS_OPTION));
            mergedArgs.add(config.getCors());
        }

        // Handle 'dbPath' parameter
        if (!mergedArgs.contains(optionify(IN_MEMORY_OPTION))) {
            if (args.containsOption(DB_PATH_OPTION) && isValidParamValue(args
                    .getOptionValues(DB_PATH_OPTION))) {

                mergedArgs.add(optionify(DB_PATH_OPTION));
                mergedArgs.add(args.getOptionValues(DB_PATH_OPTION).get(0));
            } else {
                mergedArgs.add(optionify(DB_PATH_OPTION));
                mergedArgs.add(config.getDbPath());
            }
        }

        // Handle 'port'
        if (args.containsOption(PORT_OPTION) && isValidParamValue(args.getOptionValues(PORT_OPTION))) {
            mergedArgs.add(optionify(PORT_OPTION));
            mergedArgs.add(args.getOptionValues(PORT_OPTION).get(0));
        } else {
            mergedArgs.add(optionify(PORT_OPTION));
            mergedArgs.add(String.valueOf(config.getPort()));
        }

        return mergedArgs;
    }

    private boolean isValidParamValue(List<String> paramValue) {
        return paramValue != null && paramValue.size() > 0;
    }

    private String optionify(String paramName) {
        return "-" + paramName;
    }

}
