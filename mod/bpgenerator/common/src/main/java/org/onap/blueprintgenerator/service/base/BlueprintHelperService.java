/*
 *
 *  * ============LICENSE_START=======================================================
 *  *  org.onap.dcae
 *  *  ================================================================================
 *  *  Copyright (c) 2020  AT&T Intellectual Property. All rights reserved.
 *  *  ================================================================================
 *  *  Modifications Copyright (c) 2021 Nokia
 *  *  ================================================================================
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
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

package org.onap.blueprintgenerator.service.base;

import org.onap.blueprintgenerator.constants.Constants;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * @author : Ravi Mantena
 * @date 10/16/2020 Application: DCAE/ONAP - Blueprint Generator Common Module: Used by both ONAp
 * and DCAE Blueprint Applications Service: An interface for Common Functions used across Blueprint
 */
@Service
public class BlueprintHelperService {

    /**
     * creates Input value by contatinating Type, Description and Default value
     *
     * @param type Input Type
     * @param description Description
     * @param defaultValue Default value of Type
     * @return
     */
    public LinkedHashMap<String, Object> createInputValue(
        String type, String description, Object defaultValue) {
        LinkedHashMap<String, Object> inputMap = new LinkedHashMap<>();
        inputMap.put("type", type);
        inputMap.put("description", description);
        inputMap.put("default", defaultValue);
        return inputMap;
    }

    /**
     * creates Input value by contatinating Type and Description
     *
     * @param type Input Type
     * @param description Description
     * @return
     */
    public LinkedHashMap<String, Object> createInputValue(String type, String description) {
        LinkedHashMap<String, Object> inputMap = new LinkedHashMap<>();
        inputMap.put("type", type);
        inputMap.put("description", description);
        return inputMap;
    }

    /**
     * creates Input value by contatinating Type and Default value
     *
     * @param type Input Type
     * @param defaultValue Default value of Type
     * @return
     */
    public LinkedHashMap<String, Object> createInputValue(String type, Object defaultValue) {
        LinkedHashMap<String, Object> inputMap = new LinkedHashMap<>();
        inputMap.put("type", type);
        inputMap.put("default", defaultValue);
        return inputMap;
    }

    /**
     * creates Input value by contatinating Description and Default value
     *
     * @param description Description
     * @param defaultValue Default value of Type
     * @return
     */
    public LinkedHashMap<String, Object> createIntegerInput(String description,
        Object defaultValue) {
        return createInputValue(Constants.INTEGER_TYPE, description, defaultValue);
    }

    /**
     * creates Integer Input value for given Description
     *
     * @param description Description
     * @return
     */
    public LinkedHashMap<String, Object> createIntegerInput(String description) {
        return createInputValue(Constants.INTEGER_TYPE, description);
    }

    /**
     * creates Integer Input value for given Default value
     *
     * @param defaultValue Default value of Type
     * @return
     */
    public LinkedHashMap<String, Object> createIntegerInput(Object defaultValue) {
        return createInputValue(Constants.INTEGER_TYPE, defaultValue);
    }

    /**
     * creates Integer Input value for given Description and Default value
     *
     * @param description Description
     * @param defaultValue Default value of Type
     * @return
     */
    public LinkedHashMap<String, Object> createBooleanInput(String description,
        Object defaultValue) {
        return createInputValue(Constants.BOOLEAN_TYPE, description, defaultValue);
    }

    /**
     * creates Boolean Input value for given Description
     *
     * @param description Description
     * @return
     */
    public LinkedHashMap<String, Object> createBooleanInput(String description) {
        return createInputValue(Constants.BOOLEAN_TYPE, description);
    }

    /**
     * creates Boolean Input value for given Default value
     *
     * @param defaultValue Default value of Type
     * @return
     */
    public LinkedHashMap<String, Object> createBooleanInput(Object defaultValue) {
        return createInputValue(Constants.BOOLEAN_TYPE, defaultValue);
    }

    /**
     * creates String Input value for given Default value
     * @param description Description
     * @param defaultValue Default value of Type
     * @return
     */
    public LinkedHashMap<String, Object> createStringInput(String description,
        Object defaultValue) {
        return createInputValue(Constants.STRING_TYPE, description, defaultValue);
    }

  /*  public LinkedHashMap<String, Object> createStringInput(String description){
    return createInputValue(Constants.STRING_TYPE, description);
  }*/

    /**
     * creates String Input value for given Default value
     *
     * @param defaultValue Default value of Type
     * @return
     */
    public LinkedHashMap<String, Object> createStringInput(Object defaultValue) {
        return createInputValue(Constants.STRING_TYPE, defaultValue);
    }

    /**
     * Concatenates String Input values with Underscore
     *
     * @param firstValue Value
     * @param secondValue Value
     * @return
     */
    public String joinUnderscore(String firstValue, String secondValue) {
        return firstValue + "_" + secondValue;
    }

    /**
     * Returns if the type is Data Router or not
     *
     * @param type Input Type
     * @return
     */
    public boolean isDataRouterType(String type) {
        return type.equals(Constants.DATA_ROUTER) || type.equals(Constants.DATAROUTER_VALUE);
    }

    /**
     * Returns if the type is Message Router or not
     *
     * @param type Input Type
     * @return
     */
    public boolean isMessageRouterType(String type) {
        return type.equals(Constants.MESSAGE_ROUTER) || type.equals(Constants.MESSAGEROUTER_VALUE);
    }

    /**
     * Returns if the type is Kafka or not
     *
     * @param type Input Type
     * @return
     */
    public boolean isKafkaStreamType(String type) {
        return type.equals(Constants.KAFKA_TYPE);
    }

    /**
     * Returns name with underscore for empty input
     *
     * @param name Name
     * @return
     */
    public String getNamePrefix(String name) {
        return name.isEmpty() ? "" : name + "_";
    }
}
