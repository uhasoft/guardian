/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.uhasoft.guardian.transport.heartbeat.client;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.uhasoft.guardian.config.SentinelConfig;
import com.uhasoft.guardian.util.StringUtil;

/**
 * Simple HTTP request representation.
 *
 * @author leyou
 */
public class SimpleHttpRequest {

    private InetSocketAddress socketAddress;
    private String requestPath = "";
    private int soTimeout = 3000;
    private Map<String, String> params;
    private Charset charset = Charset.forName(SentinelConfig.charset());

    public SimpleHttpRequest(InetSocketAddress socketAddress, String requestPath) {
        this.socketAddress = socketAddress;
        this.requestPath = requestPath;
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    public SimpleHttpRequest setSocketAddress(InetSocketAddress socketAddress) {
        this.socketAddress = socketAddress;
        return this;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public SimpleHttpRequest setRequestPath(String requestPath) {
        this.requestPath = requestPath;
        return this;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public SimpleHttpRequest setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
        return this;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public SimpleHttpRequest setParams(Map<String, String> params) {
        this.params = params;
        return this;
    }

    public Charset getCharset() {
        return charset;
    }

    public SimpleHttpRequest setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public SimpleHttpRequest addParam(String key, String value) {
        if (StringUtil.isBlank(key)) {
            throw new IllegalArgumentException("Parameter key cannot be empty");
        }
        if (params == null) {
            params = new HashMap<String, String>();
        }
        params.put(key, value);
        return this;
    }
}
