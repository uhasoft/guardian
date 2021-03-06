/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.uhasoft.guardian.cluster.server.envoy.rls;

/**
 * @author Eric Zhao
 */
public final class SentinelEnvoyRlsConstants {

    public static final int DEFAULT_GRPC_PORT = 10245;
    public static final String SERVER_APP_NAME = "sentinel-rls-token-server";

    public static final String GRPC_PORT_ENV_KEY = "SENTINEL_RLS_GRPC_PORT";
    public static final String GRPC_PORT_PROPERTY_KEY = "csp.sentinel.grpc.server.port";
    public static final String RULE_FILE_PATH_ENV_KEY = "SENTINEL_RLS_RULE_FILE_PATH";
    public static final String RULE_FILE_PATH_PROPERTY_KEY = "csp.sentinel.rls.rule.file";

    public static final String ENABLE_ACCESS_LOG_ENV_KEY = "SENTINEL_RLS_ACCESS_LOG";

    private SentinelEnvoyRlsConstants() {}
}
