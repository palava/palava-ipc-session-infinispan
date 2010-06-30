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

import com.google.common.collect.Maps;
import de.cosmocode.palava.ipc.AbstractIpcSession;
import de.cosmocode.palava.ipc.IpcSession;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 
 * 
 * @author Tobias Sarnowski
 */
final class Session extends AbstractIpcSession implements IpcSession, Serializable {

    private static final long serialVersionUID = 5123722821784161701L;

    private SessionKey key;

    private Map<Object, Object> ctx;

    public Session() {
        
    }

    Session(String sessionId, String identifier, long time, TimeUnit timeUnit) {
        ctx = Maps.newHashMap();
        key = new SessionKey(sessionId, identifier);
        setTimeout(time, timeUnit);
    }

    public SessionKey getKey() {
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
    public static final class SessionKey implements Serializable {

        private static final long serialVersionUID = 7054937369389336961L;
        
        private String sessionId;
        private String identifier;

        public SessionKey(String sessionId, String identifier) {
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
            return "SessionKey{sessionId='" + sessionId + '\'' + ", identifier='" + identifier + '\'' + '}';
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
            result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof SessionKey)) {
                return false;
            }
            SessionKey other = (SessionKey) obj;
            if (identifier == null) {
                if (other.identifier != null) {
                    return false;
                }
            } else if (!identifier.equals(other.identifier)) {
                return false;
            }
            if (sessionId == null) {
                if (other.sessionId != null) {
                    return false;
                }
            } else if (!sessionId.equals(other.sessionId)) {
                return false;
            }
            return true;
        }
        
    }
    
}
