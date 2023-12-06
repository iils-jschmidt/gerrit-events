package com.sonymobile.tools.gerrit.gerritevents.helpers;

import com.sonymobile.tools.gerrit.gerritevents.GerritQueryException;
import com.sonymobile.tools.gerrit.gerritevents.GerritQueryHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Class that holds static function that provides list of files for the change.
 */
public final class FileHelper {
    private static final Logger logger = LoggerFactory.getLogger(FileHelper.class);

    /**
     * Utility class should not have constructor.
     */
    private FileHelper() {
    }

    /**
     * Provides list of files related to the change.
     * @param gerritQueryHandler the query handler, responsible for the queries to gerrit.
     * @param changeId the Gerrit change id.
     * @return list of files from the change, null in case of errors
     */
    public static List<String> getFilesByChange(GerritQueryHandler gerritQueryHandler, String changeId) {
        try {
            List<JsonObject> jsonList = gerritQueryHandler.queryFiles("change:" + changeId);
            for (JsonObject json : jsonList) {
                if (json.has("type") && "stats".equalsIgnoreCase(json.get("type").getAsString())) {
                    continue;
                }
                if (json.has("currentPatchSet")) {
                    JsonObject currentPatchSet = json.getAsJsonObject("currentPatchSet");
                    if (currentPatchSet.has("files")) {
                        //TODO: was an opt method investigate what happen if files is not present
                        JsonArray changedFiles = currentPatchSet.get("files").getAsJsonArray();
                        int numberOfFiles = changedFiles.size();

                        if (numberOfFiles > 0) {
                            List<String> files = new ArrayList<>(numberOfFiles);
                            for (int i = 0; i < changedFiles.size(); i++) {
                                JsonObject file = changedFiles.get(i).getAsJsonObject();
                                files.add(file.get("file").getAsString());
                            }
                            return files;
                        }
                    }

                    break;
                }
            }
        } catch (IOException e) {
            logger.error("IOException occurred. ", e);
        } catch (GerritQueryException e) {
            logger.error("Bad query. ", e);
        }
        return Collections.emptyList();
    }
}
