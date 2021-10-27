package org.onap.dcaegen2.platform.helmchartgenerator;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.platform.helmchartgenerator.chartbuilder.AddOnsManager;
import org.onap.dcaegen2.platform.helmchartgenerator.models.componentspec.base.Auxilary;
import org.onap.dcaegen2.platform.helmchartgenerator.models.componentspec.base.ComponentSpec;
import org.onap.dcaegen2.platform.helmchartgenerator.models.componentspec.common.TlsInfo;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddOnsManagerTest {

    private AddOnsManager manager;

    @Mock
    private Utils utils;

    @BeforeEach
    void setUp() {
        manager = new AddOnsManager(utils);
    }

    @Test
    void testIncludeCertificationYamlAddOn() throws Exception {
        final String specFileLocation = "src/test/input/specs/ves.json";
        when(utils.deserializeJsonFileToModel(specFileLocation, ComponentSpec.class)).thenReturn(getMockCs());
        manager.includeAddons(specFileLocation,
                new File("src/test/dcae-ves-collector"),
                "src/test/input/blueprint");
        Assertions.assertTrue(Files.exists(Path.of("src/test/dcae-ves-collector/templates/certificates.yaml")));
    }

    private ComponentSpec getMockCs() {
        ComponentSpec cs = new ComponentSpec();
        Auxilary auxilary = new Auxilary();
        TlsInfo tlsInfo = new TlsInfo();
        tlsInfo.setUseExternalTls(true);
        tlsInfo.setUseTls(true);
        auxilary.setTlsInfo(tlsInfo);
        cs.setAuxilary(auxilary);
        return cs;
    }

    @AfterEach
    void tearDown() throws Exception{
        FileUtils.deleteDirectory(new File("src/test/dcae-ves-collector"));
    }
}
