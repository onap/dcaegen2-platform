// ============LICENSE_START=======================================================
// Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
// ================================================================================
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============LICENSE_END=========================================================

package com.att.vcc.configmanager.watcher;

import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;

import com.att.vcc.logger.*;

/**
 * Watch a directory for changes to files.
 */

public class DirWatcher {

	private static String className = "DirWatcher";
	
	private static VccLogger logger = new VccLogger("DirWatcher");
	
    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        logger.info(className, "register", "register dir: "+dir);
        keys.put(key, dir);
    }

    DirWatcher(Path dir) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();

        register(dir);
    }

    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents() {
    	String methodName = "processEvents";
    	
    	IFileChangeProcessor eventProcessor = new DtiEventChangeProcessor();
    	
        for (;;) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            try {
	            Path dir = keys.get(key);
	            if (dir == null) {
	                logger.warn(className, methodName, "WatchKey not recognized!!");
	                continue;
	            }
	
	            for (WatchEvent<?> event: key.pollEvents()) {
	                WatchEvent.Kind kind = event.kind();
	                		
	                if (kind == OVERFLOW) {
	                    continue;
	                }
	
	                // Context for directory entry event is the file name of entry
	                WatchEvent<Path> ev = cast(event);
	                Path name = ev.context();
	                Path child = dir.resolve(name);
	
	                // print out event
	                logger.debug(className, methodName, "Event="+event.kind().name()+", Path="+child);
	                if (kind == ENTRY_CREATE)
	                	eventProcessor.processEvent(child.toFile()); 
	            }
	            
	            // reset key and remove from set if directory no longer accessible             
	            boolean valid = key.reset();             
	            if (!valid) {                 
	            	keys.remove(key); 
	            }
	            
                // all directories are inaccessible                 
	            if (keys.isEmpty()) {                     
	            	break; 
	            }
	            
            } catch (Exception ex) {
            	logger.error(className, methodName, "Catch Exception::", "ERROR001", ex);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1)
        	System.exit(-1);

        // register directory and process its events
        Path dir = Paths.get(args[0]);
        new DirWatcher(dir).processEvents();
    }
}