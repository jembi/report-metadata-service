package org.jembi.ciol.report_metadata;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MetadataValidation {
    private static final Logger LOGGER = LogManager.getLogger(HttpServer.class);

    public static String readJsonFile(final String fileName) {
        String jsonData = "";

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonData += line + "\n";
            }
            bufferedReader.close();
        } catch (Exception e) {
            LOGGER.error("Couldn't read metadata configuration file");
            e.printStackTrace();
        }
        return jsonData;
    }


    public static List<String> getKeysInJson(String json) throws JsonParseException, IOException {

        List<String> keys = new ArrayList<>();
        JsonFactory factory = new JsonFactory();
        JsonParser jsonParser = factory.createParser(json);
        while (!jsonParser.isClosed()) {
            if (jsonParser.nextToken() == JsonToken.FIELD_NAME) {
                keys.add((jsonParser.getCurrentName()));
            }
        }
        return keys;
    }
}
