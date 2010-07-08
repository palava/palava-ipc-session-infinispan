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

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import de.cosmocode.palava.concurrent.BackgroundScheduler;
import de.cosmocode.palava.core.Registry;
import de.cosmocode.palava.core.lifecycle.Disposable;
import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;
import de.cosmocode.palava.ipc.IpcConnection;
import de.cosmocode.palava.ipc.IpcConnectionDestroyEvent;
import de.cosmocode.palava.ipc.IpcSession;
import de.cosmocode.palava.ipc.IpcSessionConfig;
import de.cosmocode.palava.ipc.IpcSessionNotAttachedException;
import de.cosmocode.palava.ipc.IpcSessionProvider;
import de.cosmocode.palava.jmx.MBeanService;

/**
 * Session provider baced by a {@link Cache}.
 *
 * @author Tobias Sarnowski
 */
@Singleton
final class SessionProvider implements IpcSessionProvider, Initializable, Runnable,
    IpcConnectionDestroyEvent, Disposable, SessionProviderMBean {

    private static final Logger LOG = LoggerFactory.getLogger(SessionProvider.class);

    private final Cache<Session.Key, IpcSession> cache;
    private final AdvancedCache<Session.Key, IpcSession> advancedCache;
    
    private final Registry registry;
    private final MBeanService mBeanService;
    
    private ScheduledExecutorService scheduler;
    private final long time;
    private final TimeUnit timeUnit;

    @Inject
    @SuppressWarnings("unchecked")
    public SessionProvider(
        Registry registry,
        MBeanService mBeanService,
        @SessionCache Cache cache,
        @BackgroundScheduler ScheduledExecutorService scheduler,
        @Named(IpcSessionConfig.EXPIRATION_TIME) long time,
        @Named(IpcSessionConfig.EXPIRATION_TIME_UNIT) TimeUnit timeUnit) {
        this.registry = Preconditions.checkNotNull(registry, "Registry");
        this.mBeanService = Preconditions.checkNotNull(mBeanService, "MBeanService");
        this.cache = (Cache<Session.Key, IpcSession>) Preconditions.checkNotNull(cache, "Cache");
        this.advancedCache = this.cache.getAdvancedCache();
        this.scheduler = Preconditions.checkNotNull(scheduler, "Scheduler");
        this.time = time;
        this.timeUnit = Preconditions.checkNotNull(timeUnit, "TimeUnit");
    }

    @Override
    public void initialize() throws LifecycleException {
        registry.register(IpcConnectionDestroyEvent.class, this);
        scheduler.scheduleAtFixedRate(this, 1, 15, TimeUnit.MINUTES);
        mBeanService.register(this);
    }

    @Override
    public IpcSession getSession(String sessionId, String identifier) {
        IpcSession session = cache.get(new Session.Key(sessionId, identifier));
        if (session != null && session.isExpired()) {
            expireSession(session);
            session = null;
        }
        if (session == null) {
            session = new Session(UUID.randomUUID().toString(), identifier, time, timeUnit);
            LOG.info("Created {}", session);
        }
        return session;
    }

    @Override
    public void run() {
        for (IpcSession session : cache.values()) {
            if (session.isExpired()) {
                expireSession(session);
            }
        }
    }

    private void expireSession(IpcSession session) {
        LOG.info("Expiring {}", session);
        try {
            cache.removeAsync(Session.Key.get(session));
        } finally {
            session.clear();
        }
    }

    @Override
    public void eventIpcConnectionDestroy(IpcConnection connection) {
        final IpcSession session;
        
        try {
            session = connection.getSession();
        } catch (IpcSessionNotAttachedException e) {
            return;
        }
        
        cache.put(Session.Key.get(session), session);
    }

    @Override
    public int getCurrentNumberOfEntries() {
        return advancedCache.getStats().getCurrentNumberOfEntries();
    }

    @Override
    public long getEvictions() {
        return advancedCache.getStats().getEvictions();
    }

    @Override
    public long getHits() {
        return advancedCache.getStats().getHits();
    }

    @Override
    public long getMisses() {
        return advancedCache.getStats().getMisses();
    }

    @Override
    public long getRemoveHits() {
        return advancedCache.getStats().getRemoveHits();
    }

    @Override
    public long getRemoveMisses() {
        return advancedCache.getStats().getRemoveMisses();
    }

    @Override
    public long getRetrievals() {
        return advancedCache.getStats().getRetrievals();
    }

    @Override
    public long getStores() {
        return advancedCache.getStats().getStores();
    }

    @Override
    public long getTimeSinceStart() {
        return advancedCache.getStats().getTimeSinceStart();
    }

    @Override
    public long getTotalNumberOfEntries() {
        return advancedCache.getStats().getTotalNumberOfEntries();
    }

    @Override
    public void dispose() throws LifecycleException {
        try {
            mBeanService.unregister(this);
        } finally {
            registry.remove(this);
        }
    }

    @Override
    public String toString() {
        return "SessionProvider {" + "cache=" + cache + '}';
    }

}
