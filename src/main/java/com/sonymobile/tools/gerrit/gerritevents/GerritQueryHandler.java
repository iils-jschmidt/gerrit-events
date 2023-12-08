/*
 *  The MIT License
 *
 *  Copyright 2010 Sony Ericsson Mobile Communications. All rights reserved.
 *  Copyright 2014 Sony Mobile Communications AB. All rights reserved.
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
package com.sonymobile.tools.gerrit.gerritevents;

import com.sonymobile.tools.gerrit.gerritevents.ssh.Authentication;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnection;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnectionFactory;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class helps you call gerrit query to search for patch-sets.
 * @author Robert Sandell &lt;robert.sandell@sonyericsson.com&gt;
 */
public class GerritQueryHandler {

    /**
     * Logger instance.
     * Set protected to allow it  to be used in subclasses.
     */
    protected static final Logger logger = LoggerFactory.getLogger(GerritQueryHandler.class);
    /**
     * The base of the query ssh command to send to Gerrit.
     */
    public static final String QUERY_COMMAND = "gerrit query";
    private final String gerritHostName;
    private final int gerritSshPort;
    private final String gerritProxy;
    private final Authentication authentication;
    private final int connectionTimeout;


    /**
     * Creates a GerritQueryHandler with the specified values.
     * @param gerritHostName the hostName
     * @param gerritSshPort  the ssh port that the gerrit server listens to.
     * @param gerritProxy    the ssh Proxy url
     * @param authentication the authentication credentials.
     * @param connectionTimeout the connection timeout.
     */
    public GerritQueryHandler(String gerritHostName,
                              int gerritSshPort,
                              String gerritProxy,
                              Authentication authentication,
                              int connectionTimeout) {
        this.gerritHostName = gerritHostName;
        this.gerritSshPort = gerritSshPort;
        this.gerritProxy = gerritProxy;
        this.authentication = authentication;
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Creates a GerritQueryHandler with the specified values.
     * @param gerritHostName the hostName
     * @param gerritSshPort  the ssh port that the gerrit server listens to.
     * @param gerritProxy    the ssh Proxy url
     * @param authentication the authentication credentials.
     */
    public GerritQueryHandler(String gerritHostName,
                              int gerritSshPort,
                              String gerritProxy,
                              Authentication authentication) {
        this(gerritHostName, gerritSshPort, gerritProxy, authentication,
                                    GerritDefaultValues.DEFAULT_GERRIT_SSH_CONNECTION_TIMEOUT);
    }

    /**
     * Creates a GerritQueryHandler with the specified config.
     * @param config the config.
     */
    public GerritQueryHandler(GerritConnectionConfig config) {
        this(config.getGerritHostName(),
                config.getGerritSshPort(),
                GerritDefaultValues.DEFAULT_GERRIT_PROXY,
                config.getGerritAuthentication(),
                GerritDefaultValues.DEFAULT_GERRIT_SSH_CONNECTION_TIMEOUT);
    }

    /**
     * Creates a GerritQueryHandler with the specified config.
     *
     * @param config the config.
     */
    public GerritQueryHandler(GerritConnectionConfig2 config) {
        this(config.getGerritHostName(),
                config.getGerritSshPort(),
                config.getGerritProxy(),
                config.getGerritAuthentication(),
                GerritDefaultValues.DEFAULT_GERRIT_SSH_CONNECTION_TIMEOUT);
    }

    //CS IGNORE RedundantThrows FOR NEXT 18 LINES. REASON: Informative.
    //CS IGNORE JavadocMethod FOR NEXT 17 LINES. REASON: It is there.

    /**
     * Runs the query and returns the result as a list of Java JsonObjects.
     * It is the equivalent of calling queryJava(queryString, true, true, false, false).
     * @param queryString the query.
     * @return the query result as a List of JsonObjects.
     * @throws GerritQueryException if Gerrit reports an error with the query.
     * @throws SshException if there is an error in the SSH Connection.
     * @throws IOException for some other IO problem.
     */
    public List<JsonObject> queryJava(String queryString) throws SshException, IOException, GerritQueryException {
        return queryJava(queryString, true, true, false, false);
    }

    //CS IGNORE RedundantThrows FOR NEXT 22 LINES. REASON: Informative.
    //CS IGNORE JavadocMethod FOR NEXT 17 LINES. REASON: It is there.

    /**
     * Runs the query and returns the result as a list of Java JsonObjects.
     * @param queryString the query.
     * @param getPatchSets getPatchSets if all patch-sets of the projects found should be included in the result.
     *                      Meaning if --patch-sets should be appended to the command call.
     * @param getCurrentPatchSet if the current patch-set for the projects found should be included in the result.
     *                          Meaning if --current-patch-set should be appended to the command call.
     * @param getFiles if the files of the patch sets should be included in the result.
     *                          Meaning if --files should be appended to the command call.
     *
     * @return the query result as a List of JsonObjects.
     * @throws GerritQueryException if Gerrit reports an error with the query.
     * @throws SshException if there is an error in the SSH Connection.
     * @throws IOException for some other IO problem.
     */
    public List<JsonObject> queryJava(String queryString, boolean getPatchSets, boolean getCurrentPatchSet,
                                      boolean getFiles) throws SshException, IOException, GerritQueryException {
        return queryJava(queryString, getPatchSets, getCurrentPatchSet, getFiles, false);
    }

    //CS IGNORE RedundantThrows FOR NEXT 22 LINES. REASON: Informative.

    /**
     * Runs the query and returns the result as a list of Java JsonObjects.
     * @param queryString the query.
     * @param getPatchSets getPatchSets if all patch-sets of the projects found should be included in the result.
     *                      Meaning if --patch-sets should be appended to the command call.
     * @param getCurrentPatchSet if the current patch-set for the projects found should be included in the result.
     *                          Meaning if --current-patch-set should be appended to the command call.
     * @param getFiles if the files of the patch sets should be included in the result.
     *                          Meaning if --files should be appended to the command call.
     * @param getCommitMessage if full commit message should be included in the result.
     *                          Meaning if --commit-message should be appended to the command call.
     *
     * @return the query result as a List of JsonObjects.
     * @throws GerritQueryException if Gerrit reports an error with the query.
     * @throws SshException if there is an error in the SSH Connection.
     * @throws IOException for some other IO problem.
     */
    public List<JsonObject> queryJava(String queryString, boolean getPatchSets, boolean getCurrentPatchSet,
                                      boolean getFiles, boolean getCommitMessage) throws SshException,
                                      IOException, GerritQueryException {
        return queryJava(queryString, getPatchSets, getCurrentPatchSet, getFiles, getCommitMessage, false);
    }

    //CS IGNORE RedundantThrows FOR NEXT 24 LINES. REASON: Informative.

    /**
     * Runs the query and returns the result as a list of Java JsonObjects.
     * @param queryString the query.
     * @param getPatchSets getPatchSets if all patch-sets of the projects found should be included in the result.
     *                      Meaning if --patch-sets should be appended to the command call.
     * @param getCurrentPatchSet if the current patch-set for the projects found should be included in the result.
     *                          Meaning if --current-patch-set should be appended to the command call.
     * @param getFiles if the files of the patch sets should be included in the result.
     *                          Meaning if --files should be appended to the command call.
     * @param getCommitMessage if full commit message should be included in the result.
     *                          Meaning if --commit-message should be appended to the command call.
     * @param getComments if patchset comments should be included in the results.
     *                          Meaning if --comments should be appended to the command call.
     *
     * @return the query result as a List of JsonObjects.
     * @throws GerritQueryException if Gerrit reports an error with the query.
     * @throws SshException if there is an error in the SSH Connection.
     * @throws IOException for some other IO problem.
     */
    public List<JsonObject> queryJava(String queryString, boolean getPatchSets, boolean getCurrentPatchSet,
                                      boolean getFiles, boolean getCommitMessage, boolean getComments)
            throws SshException, IOException, GerritQueryException {

        final List<JsonObject> list = new LinkedList<JsonObject>();

        runQuery(queryString, getPatchSets, getCurrentPatchSet, getFiles, getCommitMessage, getComments,
                new LineVisitor() {
                    @Override
                    public void visit(String line) throws GerritQueryException {
                        JsonObject json = JsonParser.parseString(line.trim()).getAsJsonObject();
                        if (json.has("type") && "error".equalsIgnoreCase(json.get("type").getAsString())) {
                            throw new GerritQueryException(json.get("message").getAsString());
                        }
                        list.add(json);
                    }
                });
        return list;
    }


    //CS IGNORE RedundantThrows FOR NEXT 18 LINES. REASON: Informative.
    //CS IGNORE JavadocMethod FOR NEXT 17 LINES. REASON: It is there.

    /**
     * Runs the query and returns the result as a list of Java JsonObjects.
     * @param queryString the query.
     * @return the query result as a List of JsonObjects.
     * @throws GerritQueryException if Gerrit reports an error with the query.
     * @throws SshException if there is an error in the SSH Connection.
     * @throws IOException for some other IO problem.
     */
    public List<JsonObject> queryFiles(String queryString) throws
            SshException, IOException, GerritQueryException {
        return queryJava(queryString, false, true, true, false);
    }

    //CS IGNORE RedundantThrows FOR NEXT 18 LINES. REASON: Informative.
    //CS IGNORE JavadocMethod FOR NEXT 17 LINES. REASON: It is there.

    /**
     * Runs the query and returns the result as a list of Java JsonObjects.
     * @param queryString the query.
     * @return the query result as a List of JsonObjects.
     * @throws GerritQueryException if Gerrit reports an error with the query.
     * @throws SshException if there is an error in the SSH Connection.
     * @throws IOException for some other IO problem.
     */
    public List<JsonObject> queryCurrentPatchSets(String queryString) throws
            SshException, IOException, GerritQueryException {
        return queryJava(queryString, false, true, false, false);
    }


    //CS IGNORE RedundantThrows FOR NEXT 17 LINES. REASON: Informative.

    /**
     * Runs the query and returns the result as a list of JSON formatted strings.
     * This is the equivalent of calling queryJava(queryString, true, true, false, false).
     * @param queryString the query.
     * @return a List of JSON formatted strings.
     * @throws SshException if there is an error in the SSH Connection.
     * @throws IOException for some other IO problem.
     */
    public List<String> queryJson(String queryString) throws SshException, IOException {
        return queryJson(queryString, true, true, false, false);
    }

    //CS IGNORE RedundantThrows FOR NEXT 17 LINES. REASON: Informative.

    /**
     * Runs the query and returns the result as a list of JSON formatted strings.
     * @param queryString the query.
     * @param getPatchSets if all patch-sets of the projects found should be included in the result.
     *                      Meaning if --patch-sets should be appended to the command call.
     * @param getCurrentPatchSet if the current patch-set for the projects found should be included in the result.
     *                          Meaning if --current-patch-set should be appended to the command call.
     * @param getFiles if the files of the patch sets should be included in the result.
     *                          Meaning if --files should be appended to the command call.
     * @return a List of JSON formatted strings.
     * @throws SshException if there is an error in the SSH Connection.
     * @throws IOException for some other IO problem.
     */
    public List<String> queryJson(String queryString, boolean getPatchSets, boolean getCurrentPatchSet, boolean getFiles)
            throws SshException, IOException {
        return queryJson(queryString, getPatchSets, getCurrentPatchSet, getFiles, false);
    }

    //CS IGNORE RedundantThrows FOR NEXT 20 LINES. REASON: Informative.

    /**
     * Runs the query and returns the result as a list of JSON formatted strings.
     * @param queryString the query.
     * @param getPatchSets if all patch-sets of the projects found should be included in the result.
     *                      Meaning if --patch-sets should be appended to the command call.
     * @param getCurrentPatchSet if the current patch-set for the projects found should be included in the result.
     *                          Meaning if --current-patch-set should be appended to the command call.
     * @param getFiles if the files of the patch sets should be included in the result.
     *                          Meaning if --files should be appended to the command call.
     * @param getCommitMessage if full commit message should be included in the result.
     *                          Meaning if --commit-message should be appended to the command call.
     * @return a List of JSON formatted strings.
     * @throws SshException if there is an error in the SSH Connection.
     * @throws IOException for some other IO problem.
     */
    public List<String> queryJson(String queryString, boolean getPatchSets, boolean getCurrentPatchSet,
                                  boolean getFiles, boolean getCommitMessage)
            throws SshException, IOException {
        final List<String> list = new LinkedList<String>();
        try {
            runQuery(queryString, getPatchSets, getCurrentPatchSet, getFiles, getCommitMessage, false,
                    new LineVisitor() {
                        @Override
                        public void visit(String line) {
                            list.add(line.trim());
                        }
                    });
        } catch (GerritQueryException gqe) {
            logger.error("This should not have happened!", gqe);
        }
        return list;
    }

    //CS IGNORE RedundantThrows FOR NEXT 24 LINES. REASON: Informative.
    //CS IGNORE JavadocMethod FOR NEXT 20 LINES. REASON: It is there.

    /**
     * Runs the query on the Gerrit server and lets the provided visitor handle each line in the result.
     * @param queryString the query.
     * @param getPatchSets if all patch-sets of the projects found should be included in the result.
     *                      Meaning if --patch-sets should be appended to the command call.
     * @param getCurrentPatchSet if the current patch-set for the projects found should be included in the result.
     *                          Meaning if --current-patch-set should be appended to the command call.
     * @param getFiles if changed files list should be included in the result.
     *                          Meaning if --files should be appended to the command call.
     * @param getCommitMessage if full commit message should be included in the result.
     *                          Meaning if --commit-message should be appended to the command call.
     * @param getComments if patchset comments should be included in the results.
     *                          Meaning if --comments should be appended to the command call.
     * @param visitor the visitor to handle each line in the result.
     * @throws GerritQueryException if a visitor finds that Gerrit reported an error with the query.
     * @throws SshException if there is an error in the SSH Connection.
     * @throws IOException for some other IO problem.
     */
    private void runQuery(String queryString, boolean getPatchSets, boolean getCurrentPatchSet, boolean getFiles,
                          boolean getCommitMessage, boolean getComments, LineVisitor visitor)
            throws GerritQueryException, SshException, IOException {
        StringBuilder str = new StringBuilder(QUERY_COMMAND);
        str.append(" --format=JSON");
        if (getPatchSets) {
            str.append(" --patch-sets");
        }
        if (getCurrentPatchSet) {
            str.append(" --current-patch-set");
        }
        if (getFiles) {
            str.append(" --files");
        }
        if (getComments) {
            str.append(" --comments");
        }
        if (getCommitMessage) {
            str.append(" --commit-message");
        }
        str.append(" \"").append(queryString.replace("\"", "\\\"")).append("\"");

        SshConnection ssh = null;
        try {
            ssh = getConnection();
            BufferedReader reader = new BufferedReader(ssh.executeCommandReader(str.toString()));
            String incomingLine = null;
            while ((incomingLine = reader.readLine()) != null) {
                logger.trace("Incoming line: {}", incomingLine);
                visitor.visit(incomingLine);
            }
            logger.trace("Closing reader.");
            reader.close();
        } finally {
            cleanupConnection(ssh);
        }
    }

    /**
     * Creates a new SSH connection used to execute the queries.
     *
     * @return a fresh instance of {@link SshConnection}
     * @throws IOException for IO issues
     */
    protected SshConnection getConnection() throws IOException {
        return SshConnectionFactory.getConnection(gerritHostName, gerritSshPort, gerritProxy,
                authentication, connectionTimeout);
    }

    /**
     * Cleans up the SSH connection.
     *
     * @param ssh the SSH connection
     */
    protected void cleanupConnection(SshConnection ssh) {
        if (ssh != null) {
            ssh.disconnect();
        }
    }

    /**
     * Internal visitor for handling a line of text.
     * Used by {@link #runQuery(java.lang.String, boolean, boolean, boolean, boolean, boolean, LineVisitor)}.
     */
    interface LineVisitor {
        /**
         * Visits a line of query result.
         * @param line the line.
         * @throws GerritQueryException if you want to.
         */
        void visit(String line) throws GerritQueryException;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GerritQueryHandler)) {
            return false;
        }
        GerritQueryHandler gerritQueryHandler = (GerritQueryHandler)o;
        return Objects.equals(gerritHostName, gerritQueryHandler.gerritHostName)
                && gerritSshPort == gerritQueryHandler.gerritSshPort
                && Objects.equals(gerritProxy, gerritQueryHandler.gerritProxy)
                && Objects.equals(authentication, gerritQueryHandler.authentication)
                && connectionTimeout == gerritQueryHandler.connectionTimeout;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gerritHostName, gerritSshPort, gerritProxy, authentication, connectionTimeout);
    }

}
