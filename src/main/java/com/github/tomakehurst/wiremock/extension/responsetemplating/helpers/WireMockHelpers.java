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
package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mitchellbosecke.pebble.attributes.AttributeResolver;
import com.mitchellbosecke.pebble.extension.Extension;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.extension.Function;
import com.mitchellbosecke.pebble.extension.NodeVisitorFactory;
import com.mitchellbosecke.pebble.extension.Test;
import com.mitchellbosecke.pebble.operator.BinaryOperator;
import com.mitchellbosecke.pebble.operator.UnaryOperator;
import com.mitchellbosecke.pebble.tokenParser.TokenParser;

/**
 * This enum is implemented similar to the StringHelpers of handlebars.
 * It is basically a library of all available wiremock helpers
 */
public class WireMockHelpers implements Extension{

    @Override
    public Map<String, Filter> getFilters() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Test> getTests() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Function> getFunctions() {
        Map<String, Function> functions = new HashMap<>();
        functions.put(HandlebarsCurrentDateHelper.NAME, new HandlebarsCurrentDateHelper());
        return functions;
    }

    @Override
    public List<TokenParser> getTokenParsers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<BinaryOperator> getBinaryOperators() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<UnaryOperator> getUnaryOperators() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> getGlobalVariables() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<NodeVisitorFactory> getNodeVisitors() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AttributeResolver> getAttributeResolver() {
        // TODO Auto-generated method stub
        return null;
    }

}
