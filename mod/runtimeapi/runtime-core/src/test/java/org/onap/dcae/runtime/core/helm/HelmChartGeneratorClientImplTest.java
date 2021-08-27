package org.onap.dcae.runtime.core.helm;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.dcaegen2.platform.helmchartgenerator.chartbuilder.ChartBuilder;
import org.onap.dcaegen2.platform.helmchartgenerator.distribution.ChartDistributor;

import java.io.File;
import java.nio.file.Files;

import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class HelmChartGeneratorClientImplTest {

    @Mock
    private ChartBuilder chartBuilder;

    @Mock
    private ChartDistributor distributor;

    private HelmChartGeneratorClientImpl client;

    private File mockChartFile;

    @Before
    public void setUp() throws Exception {
        client = new HelmChartGeneratorClientImpl(chartBuilder, distributor);
        mockChartFile = Files.createTempFile("chart", "tgz").toFile();
    }

    @Test
    public void testGenerateHelmChart() throws Exception{
        client.generateHelmChart("someSpec");
        Mockito.verify(chartBuilder, Mockito.times(1)).build(any(), any(), any(), any());
    }

    @Test
    public void testDistribute() throws Exception{
        client.distribute(mockChartFile);
        Mockito.verify(distributor, Mockito.times(1)).distribute(mockChartFile);
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.forceDelete(mockChartFile);
    }
}