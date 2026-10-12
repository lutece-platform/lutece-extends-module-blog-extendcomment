/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.blog.modules.extendcomment.web;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.test.AdminUserUtils;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.mocks.MockHttpServletRequest;
import fr.paris.lutece.test.mocks.MockHttpServletResponse;
import jakarta.inject.Inject;

/**
 * Tests of the blog comments back-office page.
 */
public class BlogExtendCommentJspBeanTest extends LuteceTestCase
{
    @Inject
    private BlogExtendCommentJspBean _instance;

    /**
     * An unknown extender id stops on a message instead of listing comments.
     *
     * @throws AccessDeniedException
     *             if the right is refused
     */
    @Test
    public void testManageCommentWithUnknownExtender( ) throws AccessDeniedException
    {
        MockHttpServletRequest request = new MockHttpServletRequest( );
        request.addParameter( "view", "manageComment" );
        request.addParameter( "idExtender", "987654" );
        AdminUserUtils.registerAdminUserWithRight( request, new AdminUser( ), BlogExtendCommentJspBean.RIGHT_MANAGE_BLOG_EXTENDCOMMENT );

        _instance.processController( request, new MockHttpServletResponse( ) );

        AdminMessage message = AdminMessageService.getMessage( request );
        assertNotNull( message, "An unknown extender must answer a message" );
        assertEquals( AdminMessage.TYPE_STOP, message.getType( ) );
    }

    /**
     * An administrator without the right of the feature is refused.
     */
    @Test
    public void testManageCommentWithoutRight( )
    {
        MockHttpServletRequest request = new MockHttpServletRequest( );
        request.addParameter( "view", "manageComment" );
        AdminUserUtils.registerAdminUserWithRight( request, new AdminUser( ), "OTHER_RIGHT" );

        assertThrows( AccessDeniedException.class, ( ) -> _instance.processController( request, new MockHttpServletResponse( ) ) );
    }
}
