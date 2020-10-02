package org.onap.blueprintgenerator.models.blueprint;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.TreeMap;
import org.junit.Test;

public class ResourceConfigTest {

    @Test
    public void createResourceConfig() {
        TreeMap<String, LinkedHashMap<String, Object>> result = new ResourceConfig()
            .createResourceConfig(new TreeMap<>(), "demo");

        // TODO: move string to file
        String expectedResult = "{demo_cpu_limit={type=string, default=250m}, demo_cpu_request={type=string, default=250m}, demo_memory_limit={type=string, default=128Mi}, demo_memory_request={type=string, default=128Mi}}";
        assertEquals(expectedResult, result.toString());
    }
}
