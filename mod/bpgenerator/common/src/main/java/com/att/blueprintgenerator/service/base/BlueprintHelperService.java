/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  AT&T Intellectual Property. All rights reserved.
 *  *  ================================================================================
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *  *  ============LICENSE_END=========================================================
 *
 *
 */

package com.att.blueprintgenerator.service.base;


import com.att.blueprintgenerator.constants.Constants;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020
 * Application: DCAE/ONAP - Blueprint Generator
 * Common Module: Used by both ONAp and DCAE Blueprint Applications
 * Service: For Common Functions used across
 */

@Service
public class BlueprintHelperService {


    public static LinkedHashMap<String, Object> createInputValue(String type, String description, Object defaultValue) {
        LinkedHashMap<String, Object> inputMap = new LinkedHashMap<>();
        inputMap.put("type", type);
        inputMap.put("description", description);
        inputMap.put("default", defaultValue);
        return inputMap;
    }

    public static LinkedHashMap<String, Object> createInputValue(String type, String description) {
        LinkedHashMap<String, Object> inputMap = new LinkedHashMap<>();
        inputMap.put("type", type);
        inputMap.put("description", description);
        return inputMap;
    }

    public static LinkedHashMap<String, Object> createInputValue(String type, Object defaultValue) {
        LinkedHashMap<String, Object> inputMap = new LinkedHashMap<>();
        inputMap.put("type", type);
        inputMap.put("default", defaultValue);
        return inputMap;
    }

    public static LinkedHashMap<String, Object> createIntegerInput(String description, Object defaultValue){
        return createInputValue(Constants.INTEGER_TYPE, description, defaultValue);
    }

    public static LinkedHashMap<String, Object> createIntegerInput(String description){
        return createInputValue(Constants.INTEGER_TYPE, description);
    }

    public static LinkedHashMap<String, Object> createIntegerInput(Object defaultValue){
        return createInputValue(Constants.INTEGER_TYPE, defaultValue);
    }

    public static LinkedHashMap<String, Object> createBooleanInput(String description, Object defaultValue){
        return createInputValue(Constants.BOOLEAN_TYPE, description, defaultValue);
    }

    public static LinkedHashMap<String, Object> createBooleanInput(String description){
        return createInputValue(Constants.BOOLEAN_TYPE, description);
    }

    public static LinkedHashMap<String, Object> createBooleanInput(Object defaultValue){
        return createInputValue(Constants.BOOLEAN_TYPE, defaultValue);
    }

    public static LinkedHashMap<String, Object> createStringInput(String description, Object defaultValue){
        return createInputValue(Constants.STRING_TYPE, description, defaultValue);
    }

/*    public static LinkedHashMap<String, Object> createStringInput(String description){
        return createInputValue(Constants.STRING_TYPE, description);
    }*/

    public static LinkedHashMap<String, Object> createStringInput(Object defaultValue){
        return createInputValue(Constants.STRING_TYPE, defaultValue);
    }

    public static String joinUnderscore(String firstValue, String secondValue){
        return firstValue + "_" + secondValue;
    }

    public static boolean isDataRouterType(String type) {
        return type.equals(Constants.DATA_ROUTER) || type.equals(Constants.DATAROUTER_VALUE);
    }

    public static boolean isMessageRouterType(String type) {
        return type.equals(Constants.MESSAGE_ROUTER) || type.equals(Constants.MESSAGEROUTER_VALUE);
    }

    public String getNamePrefix(String name) {
        return name.isEmpty() ? "" : name + "_";
    }

}
