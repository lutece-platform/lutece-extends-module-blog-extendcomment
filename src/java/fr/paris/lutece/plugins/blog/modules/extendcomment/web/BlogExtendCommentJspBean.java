/*
 * Copyright (c) 2002-2020, City of Paris
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import fr.paris.lutece.plugins.blog.business.Blog;
import fr.paris.lutece.plugins.blog.business.BlogSearchFilter;
import fr.paris.lutece.plugins.blog.modules.extendcomment.utils.BlogCommentConstants;
import fr.paris.lutece.plugins.blog.service.docsearch.BlogSearchService;
import fr.paris.lutece.plugins.extend.business.extender.ResourceExtenderDTO;
import fr.paris.lutece.plugins.extend.business.extender.ResourceExtenderDTOFilter;
import fr.paris.lutece.plugins.extend.modules.comment.business.Comment;
import fr.paris.lutece.plugins.extend.modules.comment.business.CommentFilter;
import fr.paris.lutece.plugins.extend.modules.comment.business.config.CommentExtenderConfig;
import fr.paris.lutece.plugins.extend.modules.comment.service.CommentService;
import fr.paris.lutece.plugins.extend.modules.comment.service.ICommentService;
import fr.paris.lutece.plugins.extend.modules.comment.util.constants.CommentConstants;
import fr.paris.lutece.plugins.extend.service.extender.IResourceExtender;
import fr.paris.lutece.plugins.extend.service.extender.IResourceExtenderService;
import fr.paris.lutece.plugins.extend.service.extender.ResourceExtenderService;
import fr.paris.lutece.plugins.extend.service.extender.config.IResourceExtenderConfigService;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.resource.IExtendableResource;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.service.workflow.WorkflowService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.web.constants.Parameters;
import fr.paris.lutece.portal.web.util.LocalizedDelegatePaginator;
import fr.paris.lutece.util.date.DateUtil;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.html.IPaginator;
import fr.paris.lutece.util.html.Paginator;

/**
 * 
 * CommentResourceExtenderComponent
 * 
 */
@Controller( controllerJsp = "BlogExtendCommentJspBean.jsp", controllerPath = "jsp/admin/plugins/blog/modules/extendcomment", right = "BLOG_EXTENDCOMMENT_MANAGEMENT" )
public class BlogExtendCommentJspBean extends AbstractManageBlogExtendcommentJspBean
{
    // TEMPLATES
    private static final String TEMPLATE_MANAGE_COMMENTS = "admin/plugins/blog/modules/extendcomment/manage_blog_extendcomment.html";
    protected static final String VIEW_MANAGE_COMMENT = "manageComment";
    // Properties
    private static final String PROPERTY_DEFAULT_LIST_ITEM_PER_PAGE = "blog.extendcomment.listItems.itemsPerPage";

    protected String _strCurrentPageIndex = "1";
    protected int _nItemsPerPage;
    protected int _nDefaultItemsPerPage;

    private IResourceExtenderService _extenderService = SpringContextService.getBean( ResourceExtenderService.BEAN_SERVICE );

    private ICommentService _commentService = SpringContextService.getBean( CommentService.BEAN_SERVICE );
    private IResourceExtenderConfigService _configService = SpringContextService.getBean( CommentConstants.BEAN_CONFIG_SERVICE );
    private IResourceExtenderService _resourceExtenderService = SpringContextService.getBean( ResourceExtenderService.BEAN_SERVICE );;

    /**
     * Build the Manage View
     *
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_MANAGE_COMMENT, defaultView = true )
    public String getCommentBlog( HttpServletRequest request )
    {

        _strCurrentPageIndex = Paginator.getPageIndex( request, Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );
        _nDefaultItemsPerPage = AppPropertiesService.getPropertyInt( PROPERTY_DEFAULT_LIST_ITEM_PER_PAGE, 50 );
        _nItemsPerPage = Paginator.getItemsPerPage( request, Paginator.PARAMETER_ITEMS_PER_PAGE, _nItemsPerPage, _nDefaultItemsPerPage );
        List<Integer> listBlogsId = new ArrayList<>( );
        AdminUser adminUser = AdminUserService.getAdminUser( request );

        int nIdExtender = 0;
        ResourceExtenderDTO resourceExtender = null;

        String strIdExtender = request.getParameter( BlogCommentConstants.PARAMETER_ID_EXTENDER );
        if ( strIdExtender == null || strIdExtender.isEmpty( ) )
        {

            ResourceExtenderDTOFilter filter = new ResourceExtenderDTOFilter( );
            filter.setFilterExtendableResourceType( Blog.PROPERTY_RESOURCE_TYPE );
            filter.setFilterExtenderType( "comment" );
            filter.setIncludeWildcardResource( true );
            List<ResourceExtenderDTO> listExtender = _extenderService.findByFilter( filter );
            if ( listExtender != null && !listExtender.isEmpty( ) )
                resourceExtender = listExtender.get( 0 );

        }
        else
        {
            nIdExtender = Integer.parseInt( strIdExtender );
            resourceExtender = _extenderService.findByPrimaryKey( nIdExtender );
        }

        if ( resourceExtender == null )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, BlogCommentConstants.MESSAGE_UNVAILABLE_EXTENDER, AdminMessage.TYPE_STOP ) );
        }

        IResourceExtender extender = _extenderService.getResourceExtender( resourceExtender.getExtenderType( ) );
        CommentExtenderConfig config = _configService.find( extender.getKey( ), resourceExtender.getIdExtendableResource( ), Blog.PROPERTY_RESOURCE_TYPE );

        // We get the pagination info from the session
        Boolean bIsAscSort = false;

        String strIsAscSort = request.getParameter( BlogCommentConstants.COMMENT_ADMIN_IS_ASC_SORT );
        if ( strIsAscSort != null )
        {
            bIsAscSort = Boolean.parseBoolean( strIsAscSort );
        }

        String strSortedAttributeName = request.getParameter( BlogCommentConstants.COMMENT_ADMIN_SORTED_ATTRIBUTE_NAME );
        String strFilterState = request.getParameter( BlogCommentConstants.COMMENT_ADMIN_FILTER_STATE );
        String strFilterPinned = request.getParameter( BlogCommentConstants.COMMENT_ADMIN_FILTER_PINNED );
        String strFilterMarkAsImportant = request.getParameter( BlogCommentConstants.COMMENT_ADMIN_FILTER_MARK_AS_IMPORTANT );

        String strNewSortedAttributeName = request.getParameter( Parameters.SORTED_ATTRIBUTE_NAME );
        if ( strNewSortedAttributeName != null )
        {
            // We update sort properties
            strSortedAttributeName = strNewSortedAttributeName;
            bIsAscSort = Boolean.parseBoolean( request.getParameter( Parameters.SORTED_ASC ) );
        }

        if ( request.getParameter( CommentConstants.PARAMETER_FILTER_STATE ) != null )
        {
            strFilterState = request.getParameter( CommentConstants.PARAMETER_FILTER_STATE );
        }

        if ( request.getParameter( CommentConstants.PARAMETER_FILTER_PINNED ) != null )
        {
            strFilterPinned = request.getParameter( CommentConstants.PARAMETER_FILTER_PINNED );
        }

        if ( request.getParameter( CommentConstants.PARAMETER_FILTER_MARK_AS_IMPORTANT ) != null )
        {
            strFilterMarkAsImportant = request.getParameter( CommentConstants.PARAMETER_FILTER_MARK_AS_IMPORTANT );
        }

        int nItemsOffset = _nItemsPerPage * ( Integer.parseInt( _strCurrentPageIndex ) - 1 );

        CommentFilter commentFilter = new CommentFilter( );
        if ( StringUtils.isNotBlank( strFilterState ) && StringUtils.isNumeric( strFilterState ) )
        {
            commentFilter.setCommentState( Integer.parseInt( strFilterState ) );
        }
        if ( StringUtils.isNotBlank( strFilterPinned ) )
        {
            commentFilter.setPinned( new Boolean( strFilterPinned ) );
        }
        if ( StringUtils.isNotBlank( strFilterMarkAsImportant ) )
        {
            commentFilter.setImportant( new Boolean( strFilterMarkAsImportant ) );
        }
        commentFilter.setSortedAttributeName( strSortedAttributeName );
        commentFilter.setAscSort( bIsAscSort );

        BlogSearchFilter filter = new BlogSearchFilter( );
        filter.setUser( adminUser.getFirstName( ) );

        BlogSearchService.getInstance( ).getSearchResults( filter, listBlogsId );

        List<Comment> listComments = _commentService.findByResource( resourceExtender.getIdExtendableResource( ), Blog.PROPERTY_RESOURCE_TYPE, commentFilter,
                nItemsOffset, _nItemsPerPage, config.getAuthorizeSubComments( ) );

        listComments.stream( ).filter( comment -> listBlogsId.stream( ).anyMatch( p -> String.valueOf( p ).equals( comment.getIdExtendableResource( ) ) ) )
                .collect( Collectors.toList( ) );
        int nItemsCount = listComments.size( );

        // We get the paginator
        IPaginator<Comment> paginator = new LocalizedDelegatePaginator<Comment>( listComments, _nItemsPerPage, getHomeUrl( request ),
                Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex, nItemsCount, AdminUserService.getLocale( request ) );

        Map<String, Object> model = new HashMap<String, Object>( );
        List<Comment> listCommentDisplay = paginator.getPageItems( );
        if ( adminUser != null && WorkflowService.getInstance( ).isAvailable( ) )
        {
            for ( Comment comment : listCommentDisplay )
            {

                comment.setListWorkflowActions( WorkflowService.getInstance( ).getActions( comment.getIdComment( ),
                        _commentService.getResourceType( comment.getExtendableResourceType( ) ), config.getIdWorkflow( ), adminUser ) );

            }
        }

        HashMap<String, IExtendableResource> mapResourceExtender = new HashMap<>( );
        for ( Comment comment : listCommentDisplay )
        {
            if ( !mapResourceExtender.containsKey( comment.getIdExtendableResource( ) ) )
            {

                mapResourceExtender.put( comment.getIdExtendableResource( ),
                        _resourceExtenderService.getExtendableResource( comment.getIdExtendableResource( ), resourceExtender.getExtendableResourceType( ) ) );
            }

        }
        model.put( CommentConstants.MARK_ALL_RESOURCES, true );
        model.put( CommentConstants.MARK_RESOURCE_EXTENDER_MAP, mapResourceExtender );

        model.put( CommentConstants.MARK_ID_EXTENDABLE_RESOURCE, resourceExtender.getIdExtendableResource( ) );
        model.put( CommentConstants.MARK_EXTENDABLE_RESOURCE_TYPE, resourceExtender.getExtendableResourceType( ) );

        model.put( CommentConstants.MARK_LIST_COMMENTS, listCommentDisplay );
        model.put( CommentConstants.MARK_PAGINATOR, paginator );
        model.put( CommentConstants.MARK_NB_ITEMS_PER_PAGE, Integer.toString( paginator.getItemsPerPage( ) ) );
        model.put( CommentConstants.MARK_ASC_SORT, bIsAscSort );
        model.put( Parameters.SORTED_ATTRIBUTE_NAME, strSortedAttributeName );
        model.put( CommentConstants.PARAMETER_ID_COMMENT, request.getParameter( CommentConstants.PARAMETER_ID_COMMENT ) );
        model.put( CommentConstants.MARK_USE_BBCODE, config.getUseBBCodeEditor( ) );
        model.put( CommentConstants.MARK_ADMIN_BADGE, config.getAdminBadge( ) );
        model.put( CommentConstants.MARK_ALLOW_SUB_COMMENTS, config.getAuthorizeSubComments( ) );
        model.put( CommentConstants.MARK_FILTER_STATE, strFilterState );
        model.put( CommentConstants.MARK_FILTER_MARK_AS_IMPORTANT, strFilterMarkAsImportant );
        model.put( CommentConstants.MARK_FILTER_PINNED, strFilterPinned );
        model.put( CommentConstants.MARK_LIST_COMMENT_STATES, _commentService.getRefListCommentStates( request.getLocale( ) ) );
        model.put( CommentConstants.MARK_LIST_MARK_AS_IMPORTANT_FILTER, _commentService.getRefListFilterAsImportant( request.getLocale( ) ) );
        model.put( CommentConstants.MARK_LIST_PINNED_FILTER, _commentService.getRefListFilterAsPinned( request.getLocale( ) ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MANAGE_COMMENTS, request.getLocale( ), model );

        String strContent = template.getHtml( );

        return strContent;

    }

}
