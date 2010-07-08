/**
 * Copyright 2010 CosmoCode GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.cosmocode.palava.ipc.session.infinispan;

/**
 * MBean interface for {@link SessionProvider}.
 *
 * @since 1.3
 * @author Willi Schoenborn
 */
public interface SessionProviderMBean {

    /**
     * Time since start.
     * 
     * @return Number of seconds since cache started.
     */
    long getTimeSinceStart();

    /**
     * Current number of entries.
     * 
     * @return Number of entries currently in the cache.
     */
    int getCurrentNumberOfEntries();

    /**
     * Total number of entries.
     * 
     * @return Number of entries stored in cache .
     */
    long getTotalNumberOfEntries();

    /**
     * Stores.
     * 
     * @return Number of put operations on the cache.
     */
    long getStores();

    /**
     * Retrievals.
     * 
     * @return Number of get operations.
     */
    long getRetrievals();

    /**
     * Hits.
     * 
     * @return Number of cache get hits.
     */
    long getHits();

    /**
     * Misses.
     * 
     * @return Number of cache get misses.
     */
    long getMisses();

    /**
     * Remove hits.
     * 
     * @return Number of cache removal hits.
     */
    long getRemoveHits();

    /**
     * Remove misses.
     * 
     * @return Number of cache removal misses.
     */
    long getRemoveMisses();

    /**
     * Evictions.
     * 
     * @return Number of cache eviction.
     */   
    long getEvictions();
    
}
