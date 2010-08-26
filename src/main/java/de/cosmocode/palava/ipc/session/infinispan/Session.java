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

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import de.cosmocode.palava.ipc.AbstractIpcSession;
import de.cosmocode.palava.ipc.IpcSession;

/**
 * Custon {@link IpcSession} implementation which holds a {@link Session.Key}.
 * 
 * @author Tobias Sarnowski
 */
final class Session extends AbstractIpcSession implements IpcSession, Serializable {

    private static final long serialVersionUID = 1399654783127769393L;

    private Key key;

    private Map<Object, Object> ctx;

    public Session() {
        
    }

    Session(String sessionId, String identifier, long time, TimeUnit timeUnit) {
        ctx = Maps.newHashMap();
        key = new Key(sessionId, identifier);
        setTimeout(time, timeUnit);
    }

    public Key getKey() {
        return key;
    }

    @Override
    protected Map<Object, Object> context() {
        return ctx;
    }

    @Override
    public String getSessionId() {
        return key.getSessionId();
    }

    @Override
    public String getIdentifier() {
        return key.getIdentifier();
    }

    @Override
    public String toString() {
        return "Session{key=" + key + ", entries=" + ctx.size() + '}';
    }

    /**
     * Serializable map key for {@link Session}s containing
     * session id and identifier.
     *
     * @author Tobias Sarnowski
     * @author Willi Schoenborn
     */
    public static final class Key implements Serializable {

        private static final long serialVersionUID = -7883453312926176278L;
        
        private String sessionId;
        private String identifier;

        public Key(String sessionId, String identifier) {
            this.sessionId = sessionId;
            this.identifier = identifier;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getIdentifier() {
            return identifier;
        }

        @Override
        public String toString() {
            return "Session.Key {sessionId='" + sessionId + '\'' + ", identifier='" + identifier + '\'' + '}';
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(sessionId, identifier);
        }

        @Override
        public boolean equals(Object that) {
            if (this == that) {
                return true;
            } else if (that instanceof Key) {
                final Key other = Key.class.cast(that);
                return Objects.equal(sessionId, other.sessionId) &&
                    Objects.equal(identifier, other.identifier);
            } else {
                return false;
            }
        }
        
        /**
         * Returns the {@link Key} for the given {@link IpcSession}.
         * 
         * @since 1.2
         * @param session the session
         * @return the key of the session
         */
        public static Key get(IpcSession session) {
            if (session instanceof Session) {
                return Session.class.cast(session).getKey();
            } else {
                return new Key(session.getSessionId(), session.getIdentifier());
            }
        }
        
    }
    
}
