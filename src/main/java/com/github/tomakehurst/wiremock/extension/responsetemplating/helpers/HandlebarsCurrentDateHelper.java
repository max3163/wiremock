package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.mitchellbosecke.pebble.extension.Function;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

public class HandlebarsCurrentDateHelper implements Function {
    
    protected List<String> argumentNames;
    public final static String NAME = "now";
    
    public HandlebarsCurrentDateHelper() {
        this.argumentNames = new ArrayList<>();
        this.argumentNames.add("format");
        this.argumentNames.add("offset");
        this.argumentNames.add("timezone");
    }
    
    @Override
    public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
        String format = (String) args.get("format");
        String offset = (String) args.get("offset");
        String timezone = (String) args.get("timezone");

        Date date = new Date();
        if (offset != null) {
            date = new DateOffset(offset).shift(date);
        }

        return new RenderableDate(date, format, timezone);
    }

    @Override
    public List<String> getArgumentNames() {
        return argumentNames;
    }
    
}
