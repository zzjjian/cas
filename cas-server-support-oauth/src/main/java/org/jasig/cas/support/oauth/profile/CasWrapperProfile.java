/*
 *  Copyright 2012 The JA-SIG Collaborative
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jasig.cas.support.oauth.profile;

import java.util.Map;

import org.jasig.cas.support.oauth.provider.impl.CasWrapperProvider20;
import org.scribe.up.profile.UserProfile;

/**
 * This class is a specific profile after OAuth authentication in CAS server wrapping OAuth protocol.
 * 
 * @author Jerome Leleu
 * @since 3.5.1
 */
public class CasWrapperProfile extends UserProfile {
    
    private static final long serialVersionUID = 4123272060735126175L;
    
    public CasWrapperProfile() {
        super();
    }
    
    public CasWrapperProfile(Object id) {
        super(id);
    }
    
    public CasWrapperProfile(Object id, Map<String, Object> attributes) {
        super(id, attributes);
    }
    
    protected String getProviderType() {
        return CasWrapperProvider20.TYPE;
    }
}
