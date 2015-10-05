/*
* The contents of this file are subject to the terms of the Common Development and
* Distribution License (the License). You may not use this file except in compliance with the
* License.
*
* You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
* specific language governing permission and limitations under the License.
*
* When distributing Covered Software, include this CDDL Header Notice in each file and include
* the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
* Header, with the fields enclosed by brackets [] replaced by your own identifying
* information: "Portions copyright [year] [name of copyright owner]".
*
* Copyright 2014 ForgeRock AS.
*/
package org.forgerock.openam.rest.authz;

import com.iplanet.dpro.session.service.SessionService;
import com.iplanet.sso.SSOToken;
import com.sun.identity.shared.Constants;
import com.sun.identity.shared.debug.Debug;
import org.forgerock.authz.filter.api.AuthorizationResult;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.openam.rest.resource.SSOTokenContext;
import org.forgerock.openam.utils.Config;
import org.forgerock.util.promise.Promise;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CoreTokenResourceAuthzModuleTest {

    Config<SessionService> mockConfig = mock(Config.class);
    SessionService mockService = mock(SessionService.class);
    Debug mockDebug = mock(Debug.class);

    @BeforeTest
    public void beforeTest() {
        given(mockConfig.get()).willReturn(mockService);
    }

    @Test
    public void shouldBlockAllAccessIfResourceDisabled() throws Exception {

        //given
        CoreTokenResourceAuthzModule testModule = new CoreTokenResourceAuthzModule(mockConfig, mockDebug, false);
        SSOTokenContext mockSSOTokenContext = mock(SSOTokenContext.class);

        //when
        Promise<AuthorizationResult, ResourceException> result = testModule.authorize(mockSSOTokenContext);

        //then
        assertFalse(result.get().isAuthorized());

    }

    @Test
    public void shouldAuthorizeAccessToSuperUserIfResourceEnabled() throws Exception {

        //given
        CoreTokenResourceAuthzModule testModule = new CoreTokenResourceAuthzModule(mockConfig, mockDebug, true);
        SSOTokenContext mockSSOTokenContext = mock(SSOTokenContext.class);
        SSOToken mockSSOToken = mock(SSOToken.class);

        given(mockSSOTokenContext.getCallerSSOToken()).willReturn(mockSSOToken);
        given(mockSSOToken.getProperty(Constants.UNIVERSAL_IDENTIFIER)).willReturn("test");
        given(mockService.isSuperUser("test")).willReturn(true);

        //when
        Promise<AuthorizationResult, ResourceException> result = testModule.authorize(mockSSOTokenContext);

        //then
        assertTrue(result.get().isAuthorized());

    }

    @Test
    public void shouldBlockAllAccessIfResourceEnabledButNonSuperUser() throws Exception {

        //given
        CoreTokenResourceAuthzModule testModule = new CoreTokenResourceAuthzModule(mockConfig, mockDebug, true);
        SSOTokenContext mockSSOTokenContext = mock(SSOTokenContext.class);
        SSOToken mockSSOToken = mock(SSOToken.class);

        given(mockSSOTokenContext.getCallerSSOToken()).willReturn(mockSSOToken);
        given(mockSSOToken.getProperty(Constants.UNIVERSAL_IDENTIFIER)).willReturn("test");
        given(mockService.isSuperUser("test")).willReturn(false);

        //when
        Promise<AuthorizationResult, ResourceException> result = testModule.authorize(mockSSOTokenContext);

        //then
        assertFalse(result.get().isAuthorized());

    }

}
