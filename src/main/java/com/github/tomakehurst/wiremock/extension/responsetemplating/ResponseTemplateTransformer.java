/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.extension.responsetemplating;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.helpers.HandlebarsCurrentDateHelper;
import com.github.tomakehurst.wiremock.extension.responsetemplating.helpers.WireMockHelpers;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.PebbleEngine.Builder;
import com.mitchellbosecke.pebble.extension.Extension;
import com.mitchellbosecke.pebble.loader.StringLoader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

public class ResponseTemplateTransformer extends ResponseDefinitionTransformer {

    public static final String NAME = "response-template";
    private final boolean global;

    private final PebbleEngine pebbleEngine;

    public ResponseTemplateTransformer(boolean global) {
        this(global, Collections.<Extension>emptyList());
    }

    public ResponseTemplateTransformer(boolean global, String helperName, Extension helper) {
        this(global, Collections.<Extension>emptyList());
    }

    public ResponseTemplateTransformer(boolean global, List<Extension> extensions) {
        this.global = global;
        Builder builder = new PebbleEngine.Builder();
        for (Extension ext : extensions) {
            builder.extension(ext);
        }
        
       //Add all available wiremock helpers
       builder.extension(new WireMockHelpers());
        
        builder.loader( new StringLoader());
        pebbleEngine = builder.build();
    }


    @Override
    public boolean applyGlobally() {
        return global;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource files, Parameters parameters) {
        ResponseDefinitionBuilder newResponseDefBuilder = ResponseDefinitionBuilder.like(responseDefinition);
        final ImmutableMap<String, Object> model = ImmutableMap.<String, Object>builder().put("parameters", firstNonNull(parameters, Collections.<String, Object>emptyMap()))
                .put("request", RequestTemplateModel.from(request)).build();

        if (responseDefinition.specifiesTextBodyContent()) {
            PebbleTemplate bodyTemplate = uncheckedCompileTemplate(responseDefinition.getBody());
            applyTemplatedResponseBody(newResponseDefBuilder, model, bodyTemplate);
        } else if (responseDefinition.specifiesBodyFile()) {
            PebbleTemplate filePathTemplate = uncheckedCompileTemplate(responseDefinition.getBodyFileName());
            String compiledFilePath = uncheckedApplyTemplate(filePathTemplate, model);
            TextFile file = files.getTextFileNamed(compiledFilePath);
            PebbleTemplate bodyTemplate = uncheckedCompileTemplate(file.readContentsAsString());
            applyTemplatedResponseBody(newResponseDefBuilder, model, bodyTemplate);
        }

        if (responseDefinition.getHeaders() != null) {
            Iterable<HttpHeader> newResponseHeaders = Iterables.transform(responseDefinition.getHeaders().all(), new Function<HttpHeader, HttpHeader>() {
                @Override
                public HttpHeader apply(HttpHeader input) {
                    List<String> newValues = Lists.transform(input.values(), new Function<String, String>() {
                        @Override
                        public String apply(String input) {
                            PebbleTemplate template = uncheckedCompileTemplate(input);
                            return uncheckedApplyTemplate(template, model);
                        }
                    });

                    return new HttpHeader(input.key(), newValues);
                }
            });
            newResponseDefBuilder.withHeaders(new HttpHeaders(newResponseHeaders));
        }

        if (responseDefinition.getProxyBaseUrl() != null) {
            PebbleTemplate proxyBaseUrlTemplate = uncheckedCompileTemplate(responseDefinition.getProxyBaseUrl());
            String newProxyBaseUrl = uncheckedApplyTemplate(proxyBaseUrlTemplate, model);
            newResponseDefBuilder.proxiedFrom(newProxyBaseUrl);
        }

        return newResponseDefBuilder.build();
    }

    private void applyTemplatedResponseBody(ResponseDefinitionBuilder newResponseDefBuilder, ImmutableMap<String, Object> model, PebbleTemplate bodyTemplate) {
        String newBody = uncheckedApplyTemplate(bodyTemplate, model);
        newResponseDefBuilder.withBody(newBody);
    }

    private String uncheckedApplyTemplate(PebbleTemplate template, Map<String, Object> context) {
        try {
            Writer writer = new StringWriter();
            template.evaluate(writer, context);
            return writer.toString();
        } catch (IOException e) {
            return throwUnchecked(e, String.class);
        }
    }

    private PebbleTemplate uncheckedCompileTemplate(String content) {
        try {
            return pebbleEngine.getTemplate(content);
        } catch (Exception e) {
            return throwUnchecked(e, PebbleTemplate.class);
        }
    }

}
