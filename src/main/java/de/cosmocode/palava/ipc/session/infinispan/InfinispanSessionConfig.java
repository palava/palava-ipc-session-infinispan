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

import de.cosmocode.palava.ipc.IpcSessionConfig;

/**
 * Static constant holder class for infinispan session config key names.
 *
 * @since 1.4
 * @author Willi Schoenborn
 */
final class InfinispanSessionConfig {

    public static final String PREFIX = IpcSessionConfig.PREFIX;
    
    public static final String INITIAL_CHECK_DELAY = PREFIX + "initialCheckDelay";
    
    public static final String CHECK_PERIOD = PREFIX + "checkPeriod";
    
    public static final String CHECK_PERIOD_UNIT = PREFIX + "checkPeriodUnit";
    
    private InfinispanSessionConfig() {
        
    }

}
