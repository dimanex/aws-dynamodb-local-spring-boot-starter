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
package com.dimanex.spring.session.dynamodb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "dynamodb.local")
public class DynamoDBLocalStarterConfiguration {

    private String cors = "*";

    private String dbPath = "." + File.separator;

    private boolean delayTransientStatuses = false;

    private boolean inMemory = true;

    private boolean optimizeDbBeforeStartup = false;

    private int port = 8000;

    private boolean sharedDb = false;

}
