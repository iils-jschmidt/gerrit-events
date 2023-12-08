/*
 *  The MIT License
 *
 *  Copyright 2013 Jyrki Puttonen. All rights reserved.
 *  Copyright 2013 Sony Mobile Communications AB. All rights reserved.
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
package com.sonymobile.tools.gerrit.gerritevents.workers.rest;

import com.google.gson.Gson;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.ChangeBasedEvent;
import com.sonymobile.tools.gerrit.gerritevents.dto.rest.ChangeId;
import com.sonymobile.tools.gerrit.gerritevents.dto.rest.ReviewInput;
import com.sonymobile.tools.gerrit.gerritevents.rest.RestConnectionConfig;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * An abstract Job implementation
 * to be scheduled on {@link com.sonymobile.tools.gerrit.gerritevents.GerritSendCommandQueue}.
 *
 */
public abstract class AbstractRestCommandJob implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRestCommandJob.class);

    /**
     * The GSON API.
     */
    private static final Gson GSON = new Gson();
    /**
     * The config.
     */
    private final RestConnectionConfig config;
    /**
     * The listener.
     */
    protected final PrintStream altLogger;

    /**
     * The Event.
     */
    protected final ChangeBasedEvent event;

    /**
     * Constructor.
     *
     * @param config    config
     * @param altLogger alternative stream to also write log output to (ex: a build log)
     * @param event     event
     */
    protected AbstractRestCommandJob(RestConnectionConfig config, PrintStream altLogger, ChangeBasedEvent event) {
        this.config = config;
        this.altLogger = altLogger;
        this.event = event;
    }

    @Override
    public void run() {
        ReviewInput reviewInput = createReview();

        String reviewEndpoint = resolveEndpointURL();

        HttpPost httpPost = createHttpPostEntity(reviewInput, reviewEndpoint);

        if (httpPost == null) {
            return;
        }

        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(null, -1),
            config.getHttpCredentials());
        HttpClientBuilder builder =
            HttpClients.custom().setDefaultCredentialsProvider(credsProvider);
        if (config.getGerritProxy() != null && !config.getGerritProxy().isEmpty()) {
            try {
                URL url = new URL(config.getGerritProxy());
                HttpHost proxy = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
                builder.setProxy(proxy);
            } catch (MalformedURLException e) {
                logger.error("Could not parse proxy URL, attempting without proxy.", e);
                if (altLogger != null) {
                    altLogger.print("ERROR Could not parse proxy URL, attempting without proxy. "
                            + e.getMessage());
                }
            }
        }
        CloseableHttpClient httpClient = builder.build();
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            try {
                String response = IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
                if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    logger.error("Gerrit response: {}", httpResponse.getStatusLine().getReasonPhrase());
                    if (altLogger != null) {
                        altLogger.print("ERROR Gerrit response: " + httpResponse.getStatusLine().getReasonPhrase());
                    }
                }
            } finally {
                httpResponse.close();
            }
        } catch (Exception e) {
            logger.error("Failed to submit result to Gerrit", e);
            if (altLogger != null) {
                altLogger.print("ERROR Failed to submit result to Gerrit" + e.toString());
            }
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                logger.error("Failed to close connection to Gerrit", e);
                if (altLogger != null) {
                    altLogger.print("ERROR Failed to close connection to Gerrit" + e.toString());
                }
            }
        }
    }

    /**
     * Create the input for the command.
     *
     * @return the input
     */
    protected abstract ReviewInput createReview();

    /**
     * Construct the post.
     *
     * @param reviewInput    input
     * @param reviewEndpoint end point
     * @return the entity
     */
    private HttpPost createHttpPostEntity(ReviewInput reviewInput, String reviewEndpoint) {
        HttpPost httpPost = new HttpPost(reviewEndpoint);

        String asJson = GSON.toJson(reviewInput);

        StringEntity entity = null;
        entity = new StringEntity(asJson, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        return httpPost;
    }

    /**
     * What it says resolve Endpoint URL.
     *
     * @return the url.
     */
    private String resolveEndpointURL() {
        String gerritFrontEndUrl = config.getGerritFrontEndUrl();
        if (!gerritFrontEndUrl.endsWith("/")) {
            gerritFrontEndUrl = gerritFrontEndUrl + "/";
        }

        ChangeId changeId = new ChangeId(event.getChange().getProject(), event.getChange().getBranch(),
                event.getChange().getId());

        return gerritFrontEndUrl + "a/changes/" + changeId.asUrlPart()
                + "/revisions/" + event.getPatchSet().getRevision() + "/review";
    }

    /**
     * REST related configuration.
     * @return the config
     */
    public RestConnectionConfig getConfig() {
        return config;
    }
}
