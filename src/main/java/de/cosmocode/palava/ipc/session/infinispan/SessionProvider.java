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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.cosmocode.palava.concurrent.BackgroundScheduler;
import de.cosmocode.palava.core.Registry;
import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;
import de.cosmocode.palava.ipc.IpcConnection;
import de.cosmocode.palava.ipc.IpcConnectionDestroyEvent;
import de.cosmocode.palava.ipc.IpcSession;
import de.cosmocode.palava.ipc.IpcSessionProvider;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Tobias Sarnowski
 */
@Singleton
final class SessionProvider implements IpcSessionProvider, Initializable, Runnable, IpcConnectionDestroyEvent {
    private static final Logger LOG = LoggerFactory.getLogger(SessionProvider.class);

    private Cache<Session.SessionKey, Session> cache;
    private ScheduledExecutorService scheduledExecutorService;

    @Inject
    public SessionProvider(@SessionCache Cache cache,
                           @BackgroundScheduler ScheduledExecutorService scheduledExecutorService,
                           Registry registry) {
        this.cache = cache;
        this.scheduledExecutorService = scheduledExecutorService;
        registry.register(IpcConnectionDestroyEvent.class, this);
    }


    @Override
    public void initialize() throws LifecycleException {
        scheduledExecutorService.scheduleAtFixedRate(this, 1, 15, TimeUnit.MINUTES);
    }

    @Override
    public IpcSession getSession(String sessionId, String identifier) {
        Session session = cache.get(new Session.SessionKey(sessionId, identifier));
        if (session == null) {
            sessionId = UUID.randomUUID().toString();
            session = new Session(sessionId, identifier);
            LOG.debug("Created new session {}", session);
        }
        return session;
    }


    @Override
    public void run() {
        for (Map.Entry<Session.SessionKey,Session> entry: cache.entrySet()) {
            Session session = entry.getValue();

            if (session.isExpired()) {
                LOG.debug("Expiring {}...", session);
                cache.removeAsync(entry.getKey());
            }
        }
    }

    @Override
    public String toString() {
        return "SessionProvider{" +
                "cache=" + cache +
                '}';
    }

    @Override
    public void eventIpcConnectionDestroy(IpcConnection connection) {
        Session session = (Session)connection.getSession();
        cache.put(session.getKey(), session);
    }
}