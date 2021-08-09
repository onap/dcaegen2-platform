/*
 * # ============LICENSE_START=======================================================
 * # Copyright (c) 2021 AT&T Intellectual Property. All rights reserved.
 * # ================================================================================
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 * # ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.platform.helmchartgenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * An utility class for various file related tasks.
 * @author Dhrumin Desai
 */
@Slf4j
public class Utils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Utils() {}

    public static <T> T deserializeJsonFileToModel(String filePath, Class<T> modelClass) {
        return deserializeJsonFileToModel(new File(filePath), modelClass);
    }

    /**
     * maps json file to a model class
     * @param file Json file which holds the data
     * @param modelClass target model class for mapping
     * @return mapped model instance
     */
    public static <T> T deserializeJsonFileToModel(File file, Class<T> modelClass) {
        try {
            return MAPPER.readValue(file, modelClass);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    /**
     * copies dir/file to a temp location on OS
     * @param srcLocation
     * @return
     */
    public static File cloneFileToTempLocation(String srcLocation) {
        File cloneLocation = null;
        try {
            Path tempRootDir = Files.createTempDirectory("chart");
            cloneLocation = new File(tempRootDir.toString());
            log.info("cloning dir/file at : " + tempRootDir);
            FileUtils.copyDirectory(new File(srcLocation), cloneLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert cloneLocation != null;
        return cloneLocation;
    }

    /**
     * deletes dir / file from temp location of OS
     * @param dir  dir to be deleted
     */
    public static void deleteTempFileLocation(File dir) {
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            log.warn("Could not delete dir/file: " + dir.getAbsolutePath());
        }
    }

    /**
     * puts value into a map if only exists
     * @param map a map
     * @param key a key
     * @param value a value
     */
    public static void putIfNotNull(Map<String, Object> map, String key, Object value){
        if(value != null){
            map.put(key, value);
        }
    }

    /**
     * maps a yaml file to Map
     * @param fileLocation location of the yaml file
     * @return populated map
     */
    public static Map<String, Object> convertYamlFileToMap(String fileLocation) {
        Yaml yaml = new Yaml();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(fileLocation);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Map<String, Object> yamlKv = yaml.load(inputStream);
        return yamlKv;
    }
}

