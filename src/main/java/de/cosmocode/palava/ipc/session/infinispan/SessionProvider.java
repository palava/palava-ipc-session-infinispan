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

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.cosmocode.palava.core.Registry;
import de.cosmocode.palava.core.lifecycle.Disposable;
import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;
import de.cosmocode.palava.ipc.IpcConnection;
import de.cosmocode.palava.ipc.IpcConnectionDestroyEvent;
import de.cosmocode.palava.ipc.IpcSession;
import de.cosmocode.palava.ipc.IpcSessionNotAttachedException;
import de.cosmocode.palava.ipc.IpcSessionProvider;
import de.cosmocode.palava.ipc.session.infinispan.Session.Key;
import de.cosmocode.palava.jmx.MBeanService;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.util.UUID;

/**
 * Session provider baced by a {@link Cache}.
 *
 * @author Tobias Sarnowski
 */
@Singleton
final class SessionProvider implements IpcSessionProvider, Initializable,
    IpcConnectionDestroyEvent, Disposable, SessionProviderMBean {

    private static final Logger LOG = LoggerFactory.getLogger(SessionProvider.class);

    private final Cache<Key, IpcSession> cache;
    
    private final Registry registry;
    private final MBeanService mBeanService;

    private final TransactionManager transactionManager;

    @Inject
    @SuppressWarnings("unchecked")
    public SessionProvider(
        Registry registry,
        MBeanService mBeanService,
        // don't use generics here, will break injection
        @SessionCache Cache cache) {
        this.registry = Preconditions.checkNotNull(registry, "Registry");
        this.mBeanService = Preconditions.checkNotNull(mBeanService, "MBeanService");
        this.cache = Preconditions.checkNotNull(cache, "Cache");
        this.transactionManager = cache.getAdvancedCache().getTransactionManager();
    }
    
    @Override
    public void initialize() throws LifecycleException {
        registry.register(IpcConnectionDestroyEvent.class, this);
        mBeanService.register(this);
    }

    @Override
    public IpcSession getSession(String sessionId, String identifier) {
        if (transactionManager != null) {
            try {
                transactionManager.begin();
            } catch (NotSupportedException e) {
                LOG.warn("Transactions not supported, although a transaction manager was configured for infinispan");
            } catch (SystemException e) {
                throw new IllegalStateException(e);
            }
        }

        IpcSession session = cache.get(new Key(sessionId, identifier));
        if (session == null) {
            session = new Session(UUID.randomUUID().toString(), identifier);
            LOG.info("Created {}", session);
        }
        return session;
    }

    @Override
    public void eventIpcConnectionDestroy(IpcConnection connection) {
        final IpcSession session;
        
        try {
            session = connection.getSession();
        } catch (IpcSessionNotAttachedException e) {
            return;
        }
        
        cache.put(Key.get(session), session);

        if (transactionManager != null) {
            try {
                transactionManager.commit();
            } catch (RollbackException e) {
                LOG.error("Cache put was rolled back", e);
            } catch (HeuristicMixedException e) {
                throw new IllegalStateException(e);
            } catch (HeuristicRollbackException e) {
                LOG.error("Cache put was rolled back", e);
            } catch (SystemException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public int getCurrentNumberOfEntries() {
        return cache.getAdvancedCache().getStats().getCurrentNumberOfEntries();
    }

    @Override
    public long getEvictions() {
        return cache.getAdvancedCache().getStats().getEvictions();
    }

    @Override
    public long getHits() {
        return cache.getAdvancedCache().getStats().getHits();
    }

    @Override
    public long getMisses() {
        return cache.getAdvancedCache().getStats().getMisses();
    }

    @Override
    public long getRemoveHits() {
        return cache.getAdvancedCache().getStats().getRemoveHits();
    }

    @Override
    public long getRemoveMisses() {
        return cache.getAdvancedCache().getStats().getRemoveMisses();
    }

    @Override
    public long getRetrievals() {
        return cache.getAdvancedCache().getStats().getRetrievals();
    }

    @Override
    public long getStores() {
        return cache.getAdvancedCache().getStats().getStores();
    }

    @Override
    public long getTimeSinceStart() {
        return cache.getAdvancedCache().getStats().getTimeSinceStart();
    }

    @Override
    public long getTotalNumberOfEntries() {
        return cache.getAdvancedCache().getStats().getTotalNumberOfEntries();
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
