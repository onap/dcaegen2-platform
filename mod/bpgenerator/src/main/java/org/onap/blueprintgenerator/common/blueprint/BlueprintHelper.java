/*============LICENSE_START=======================================================
 org.onap.dcae
 ================================================================================
 Copyright (c) 2020 Nokia. All rights reserved.
 ================================================================================
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 ============LICENSE_END=========================================================
 */

package org.onap.blueprintgenerator.common.blueprint;


import lombok.experimental.UtilityClass;

import java.util.LinkedHashMap;

@UtilityClass
public class BlueprintHelper {

    public static final String INTEGER_TYPE = "integer";
    public static final String BOOLEAN_TYPE = "boolean";
    public static final String STRING_TYPE = "string";

    public static LinkedHashMap<String, Object> createInputValue(String type, String description, Object defaultValue) {
        LinkedHashMap<String, Object> inputMap = new LinkedHashMap<>();
        inputMap.put("type", type);
        inputMap.put("description", description);
        inputMap.put("default", defaultValue);
        return inputMap;
    }

    public static LinkedHashMap<String, Object> createInputValue(String type, Object defaultValue) {
        LinkedHashMap<String, Object> inputMap = new LinkedHashMap<>();
        inputMap.put("type", type);
        inputMap.put("default", defaultValue);
        return inputMap;
    }

    public static LinkedHashMap<String, Object> createIntegerInput(String description, Object defaultValue){
        return createInputValue(INTEGER_TYPE, description, defaultValue);
    }

    public static LinkedHashMap<String, Object> createBooleanInput(String description, Object defaultValue){
        return createInputValue(BOOLEAN_TYPE, description, defaultValue);
    }

    public static LinkedHashMap<String, Object> createStringInput(String description, Object defaultValue){
        return createInputValue(STRING_TYPE, description, defaultValue);
    }

    public static LinkedHashMap<String, Object> createStringInput(Object defaultValue){
        return createInputValue(STRING_TYPE, defaultValue);
    }

    public static String joinUnderscore(String firstValue, String secondValue){
        return firstValue + "_" + secondValue;
    }

    public static boolean isDataRouterType(String type) {
        return type.equals("data_router") || type.equals("data router");
    }

    public static boolean isMessageRouterType(String type) {
        return type.equals("message_router") || type.equals("message router");
    }
}