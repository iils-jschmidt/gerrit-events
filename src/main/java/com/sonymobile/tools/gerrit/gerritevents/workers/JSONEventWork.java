/*
 *  The MIT License
 *
 *  Copyright 2013 rinrinne All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.sonymobile.tools.gerrit.gerritevents.workers;

import com.google.gson.JsonObject;

import com.sonymobile.tools.gerrit.gerritevents.GerritJsonEventFactory;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Provider;

/**
 * The class to hand JSON event object over to {@link AbstractJsonObjectWork}.
 *
 * @author rinrinne &lt;rinrin.ne@gmail.com&gt;
 */
public class JSONEventWork extends AbstractJsonObjectWork {

    /**
     * @return the json
     */
    public JsonObject getJson() {
        return json;
    }

    private JsonObject json;
    private Provider provider;

    /**
     * Default constructor.
     *
     * @param json the JSON object.
     */
    public JSONEventWork(JsonObject json) {
        this.json = json;
    }

    /**
     * Constructor with parameters.
     *
     * @param json the JSON object.
     * @param provider the provider.
     */
    public JSONEventWork(JsonObject json, Provider provider) {
        this.json = json;
        this.provider = provider;
    }

    @Override
    public void perform(Coordinator coordinator) {
        if (GerritJsonEventFactory.isInterestingAndUsable(getJson())) {
            perform(getJson(), coordinator, provider);
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("[");
        str.append(getClass().getSimpleName());
        str.append(": \"");
        str.append(getJson().toString());
        if (provider != null) {
            str.append(", " + provider.toString());
        }
        str.append("\"]");
        return str.toString();
    }
}
