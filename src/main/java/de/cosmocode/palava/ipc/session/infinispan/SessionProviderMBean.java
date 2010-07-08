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
