/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.apache.nifi.nar;

import org.apache.nifi.bundle.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;

/**
 * Uses the Java executor service scheduler to continuously load new DCAE jars
 */
public class DCAEAutoLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DCAEAutoLoader.class);

    private static final long POLL_INTERVAL_MS = 5000;

    /**
     * Runnable task that grabs list of remotely stored jars, identifies ones that haven't
     * been processed, builds Nifi bundles for those unprocessed ones and loads them into
     * the global extension manager.
     */
    private static class LoaderTask implements Runnable {

        private static final Logger LOGGER = LoggerFactory.getLogger(LoaderTask.class);

        private final URI indexJsonDcaeJars;
        private final ExtensionDiscoveringManager extensionManager;
        private final Set<URL> processed = new LinkedHashSet();

        private LoaderTask(URI indexJsonDcaeJars, ExtensionDiscoveringManager extensionManager) {
            this.indexJsonDcaeJars = indexJsonDcaeJars;
            this.extensionManager = extensionManager;
        }

        @Override
        public void run() {
            try {
                List<URL> toProcess = DCAEClassLoaders.getDCAEJarsURLs(this.indexJsonDcaeJars);
                toProcess.removeAll(processed);

                if (!toProcess.isEmpty()) {
                    Set<Bundle> bundles = DCAEClassLoaders.createDCAEBundles(toProcess);
                    this.extensionManager.discoverExtensions(bundles);
                    processed.addAll(toProcess);

                    LOGGER.info(String.format("#Added DCAE bundles: %d, #Total DCAE bundles: %d ",
                        bundles.size(), processed.size()));
                }
            } catch (final Exception e) {
                LOGGER.error("Error loading DCAE jars due to: " + e.getMessage(), e);
            }
        }
    }

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private ScheduledFuture taskFuture;

    public synchronized void start(URI indexJsonDcaeJars, final ExtensionDiscoveringManager extensionManager) {
        // Restricting to a single thread
        if (taskFuture != null && !taskFuture.isCancelled()) {
            return;
        }

        LOGGER.info("Starting DCAE Auto-Loader: {}", new Object[]{indexJsonDcaeJars});

        LoaderTask task = new LoaderTask(indexJsonDcaeJars, extensionManager);
        this.taskFuture = executor.scheduleAtFixedRate(task, 0, POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
        LOGGER.info("DCAE Auto-Loader started");
    }

    public synchronized void stop() {
        if (this.taskFuture != null) {
            this.taskFuture.cancel(true);
            LOGGER.info("DCAE Auto-Loader stopped");
        }
    }

}
