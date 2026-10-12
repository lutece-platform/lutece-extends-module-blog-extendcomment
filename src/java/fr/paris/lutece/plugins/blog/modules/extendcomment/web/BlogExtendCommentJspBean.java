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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.blog.business.Blog;
import fr.paris.lutece.plugins.blog.business.BlogSearchFilter;
import fr.paris.lutece.plugins.blog.modules.extendcomment.utils.BlogCommentConstants;
import fr.paris.lutece.plugins.blog.service.docsearch.BlogSearchService;
import fr.paris.lutece.plugins.extend.business.extender.ResourceExtenderDTO;
import fr.paris.lutece.plugins.extend.business.extender.ResourceExtenderDTOFilter;
import fr.paris.lutece.plugins.extend.modules.comment.business.Comment;
import fr.paris.lutece.plugins.extend.modules.comment.business.CommentFilter;
import fr.paris.lutece.plugins.extend.modules.comment.business.config.CommentExtenderConfig;
import fr.paris.lutece.plugins.extend.modules.comment.service.CommentPlugin;
import fr.paris.lutece.plugins.extend.modules.comment.service.ICommentService;
import fr.paris.lutece.plugins.extend.modules.comment.service.extender.CommentResourceExtender;
import fr.paris.lutece.plugins.extend.modules.comment.util.constants.CommentConstants;
import fr.paris.lutece.plugins.extend.service.extender.IResourceExtenderService;
import fr.paris.lutece.plugins.extend.service.extender.config.IResourceExtenderConfigService;
import fr.paris.lutece.api.user.User;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.resource.IExtendableResource;
import fr.paris.lutece.portal.service.workflow.WorkflowService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.web.cdi.mvc.Models;
import fr.paris.lutece.portal.web.constants.Parameters;
import fr.paris.lutece.portal.web.util.IPager;
import fr.paris.lutece.portal.web.util.Pager;
import fr.paris.lutece.util.url.UrlItem;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Lists the comments of the blogs edited by the current administrator.
 */
@RequestScoped
@Named
@Controller( controllerJsp = BlogExtendCommentJspBean.CONTROLLER_JSP, controllerPath = BlogExtendCommentJspBean.CONTROLLER_PATH, right = BlogExtendCommentJspBean.RIGHT_MANAGE_BLOG_EXTENDCOMMENT, securityTokenEnabled = true )
public class BlogExtendCommentJspBean extends MVCAdminJspBean
{
    public static final String RIGHT_MANAGE_BLOG_EXTENDCOMMENT = "BLOG_EXTENDCOMMENT_MANAGEMENT";

    static final String CONTROLLER_JSP = "ManageBlogExtendcomment.jsp";
    static final String CONTROLLER_PATH = "jsp/admin/plugins/blog/modules/extendcomment/";

    private static final long serialVersionUID = 4021186405786498326L;

    private static final String JSP_VIEW_EXTENDER_BLOG = "../../../blog/modules/extendcomment/" + CONTROLLER_JSP;
    private static final String TEMPLATE_MANAGE_COMMENTS = "admin/plugins/blog/modules/extendcomment/manage_blog_extendcomment.html";
    private static final String VIEW_MANAGE_COMMENT = "manageComment";
    private static final String MESSAGE_PAGE_TITLE = "module.blog.extendcomment.manage_blog_extendcomment.pageTitle";
    private static final String PROPERTY_ITEMS_PER_PAGE = "blog-extendcomment.listItems.itemsPerPage";

    @Inject
    private IResourceExtenderService _resourceExtenderService;

    @Inject
    private ICommentService _commentService;

    @Inject
    @Named( CommentConstants.BEAN_CONFIG_SERVICE )
    private IResourceExtenderConfigService _configService;

    @Inject
    private WorkflowService _workflowService;

    @Inject
    @Pager( name = "blog-extendcomment.comments", listBookmark = CommentConstants.MARK_LIST_COMMENTS, defaultItemsPerPage = PROPERTY_ITEMS_PER_PAGE, baseUrl = CONTROLLER_PATH
            + CONTROLLER_JSP )
    private IPager<Comment, Comment> _pager;

    /**
     * Builds the list of the comments of the blogs edited by the current administrator.
     *
     * @param request
     *            The HTTP request
     * @param model
     *            The model
     * @return The page
     */
    @View( value = VIEW_MANAGE_COMMENT, defaultView = true )
    public String getManageComment( HttpServletRequest request, Models model )
    {
        ResourceExtenderDTO resourceExtender = findResourceExtender( request.getParameter( BlogCommentConstants.PARAMETER_ID_EXTENDER ) );
        CommentExtenderConfig config = resourceExtender == null ? null : _configService.find( resourceExtender.getIdExtender( ) );

        if ( config == null )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, BlogCommentConstants.MESSAGE_UNVAILABLE_EXTENDER, AdminMessage.TYPE_STOP ) );
        }

        AdminUser adminUser = getUser( );

        boolean bIsAscSort = Boolean.parseBoolean( request.getParameter( BlogCommentConstants.COMMENT_ADMIN_IS_ASC_SORT ) );
        String strSortedAttributeName = request.getParameter( BlogCommentConstants.COMMENT_ADMIN_SORTED_ATTRIBUTE_NAME );
        String strNewSortedAttributeName = request.getParameter( Parameters.SORTED_ATTRIBUTE_NAME );
        if ( strNewSortedAttributeName != null )
        {
            strSortedAttributeName = strNewSortedAttributeName;
            bIsAscSort = Boolean.parseBoolean( request.getParameter( Parameters.SORTED_ASC ) );
        }

        String strFilterState = StringUtils.defaultIfEmpty( request.getParameter( CommentConstants.PARAMETER_FILTER_STATE ),
                request.getParameter( BlogCommentConstants.COMMENT_ADMIN_FILTER_STATE ) );
        String strFilterPinned = StringUtils.defaultIfEmpty( request.getParameter( CommentConstants.PARAMETER_FILTER_PINNED ),
                request.getParameter( BlogCommentConstants.COMMENT_ADMIN_FILTER_PINNED ) );
        String strFilterMarkAsImportant = StringUtils.defaultIfEmpty( request.getParameter( CommentConstants.PARAMETER_FILTER_MARK_AS_IMPORTANT ),
                request.getParameter( BlogCommentConstants.COMMENT_ADMIN_FILTER_MARK_AS_IMPORTANT ) );

        CommentFilter commentFilter = new CommentFilter( );
        if ( StringUtils.isNumeric( strFilterState ) )
        {
            commentFilter.setCommentState( Integer.parseInt( strFilterState ) );
        }
        if ( StringUtils.isNotBlank( strFilterPinned ) )
        {
            commentFilter.setPinned( Boolean.valueOf( strFilterPinned ) );
        }
        if ( StringUtils.isNotBlank( strFilterMarkAsImportant ) )
        {
            commentFilter.setImportant( Boolean.valueOf( strFilterMarkAsImportant ) );
        }
        commentFilter.setSortedAttributeName( strSortedAttributeName );
        commentFilter.setAscSort( bIsAscSort );

        Set<String> setEditedBlogs = getEditedBlogs( adminUser );
        List<Comment> listComments = _commentService
                .findByResource( resourceExtender.getIdExtendableResource( ), Blog.PROPERTY_RESOURCE_TYPE, commentFilter, 0, 0, false ).stream( )
                .filter( comment -> setEditedBlogs.contains( comment.getIdExtendableResource( ) ) ).collect( Collectors.toList( ) );

        UrlItem urlPager = new UrlItem( CONTROLLER_PATH + CONTROLLER_JSP );
        urlPager.addParameter( BlogCommentConstants.PARAMETER_ID_EXTENDER, resourceExtender.getIdExtender( ) );
        addParameterIfPresent( urlPager, CommentConstants.PARAMETER_FILTER_STATE, strFilterState );
        addParameterIfPresent( urlPager, CommentConstants.PARAMETER_FILTER_PINNED, strFilterPinned );
        addParameterIfPresent( urlPager, CommentConstants.PARAMETER_FILTER_MARK_AS_IMPORTANT, strFilterMarkAsImportant );
        addParameterIfPresent( urlPager, Parameters.SORTED_ATTRIBUTE_NAME, strSortedAttributeName );
        urlPager.addParameter( Parameters.SORTED_ASC, Boolean.toString( bIsAscSort ) );

        List<Comment> listPageComments = _pager.withBaseUrl( urlPager.getUrl( ) ).withListItem( listComments ).populateModels( request, model, getLocale( ) )
                .getPageItems( );

        Map<String, IExtendableResource> mapResources = new HashMap<>( );
        for ( Comment comment : listPageComments )
        {
            completeComment( comment, commentFilter, config, adminUser );
            mapResources.computeIfAbsent( comment.getIdExtendableResource( ),
                    strIdResource -> _resourceExtenderService.getExtendableResource( strIdResource, Blog.PROPERTY_RESOURCE_TYPE ) );
        }

        request.getSession( ).setAttribute( CommentPlugin.PLUGIN_NAME + CommentConstants.SESSION_COMMENT_ADMIN_POST_BACK_URL, JSP_VIEW_EXTENDER_BLOG );

        model.put( CommentConstants.MARK_ALL_RESOURCES, true );
        model.put( CommentConstants.MARK_RESOURCE_EXTENDER_MAP, mapResources );
        model.put( CommentConstants.MARK_ID_EXTENDABLE_RESOURCE, resourceExtender.getIdExtendableResource( ) );
        model.put( CommentConstants.MARK_EXTENDABLE_RESOURCE_TYPE, Blog.PROPERTY_RESOURCE_TYPE );
        model.put( BlogCommentConstants.MARK_ID_EXTENDER, resourceExtender.getIdExtender( ) );
        model.put( CommentConstants.MARK_ASC_SORT, bIsAscSort );
        model.put( Parameters.SORTED_ATTRIBUTE_NAME, strSortedAttributeName );
        model.put( CommentConstants.MARK_USE_BBCODE, config.getUseBBCodeEditor( ) );
        model.put( CommentConstants.MARK_ADMIN_BADGE, config.getAdminBadge( ) );
        model.put( CommentConstants.MARK_ALLOW_SUB_COMMENTS, config.getAuthorizeSubComments( ) );
        model.put( CommentConstants.MARK_FILTER_STATE, strFilterState );
        model.put( CommentConstants.MARK_FILTER_MARK_AS_IMPORTANT, strFilterMarkAsImportant );
        model.put( CommentConstants.MARK_FILTER_PINNED, strFilterPinned );
        model.put( CommentConstants.MARK_LIST_COMMENT_STATES, _commentService.getRefListCommentStates( getLocale( ) ) );
        model.put( CommentConstants.MARK_LIST_MARK_AS_IMPORTANT_FILTER, _commentService.getRefListFilterAsImportant( getLocale( ) ) );
        model.put( CommentConstants.MARK_LIST_PINNED_FILTER, _commentService.getRefListFilterAsPinned( getLocale( ) ) );

        return getPage( MESSAGE_PAGE_TITLE, TEMPLATE_MANAGE_COMMENTS, model );
    }

    /**
     * Finds the comment extender of the blogs: the one given by its id, else the one declared for every blog, else the
     * first one declared for a blog.
     *
     * @param strIdExtender
     *            The id of the extender, may be empty
     * @return The extender, or null when none matches
     */
    private ResourceExtenderDTO findResourceExtender( String strIdExtender )
    {
        if ( StringUtils.isNumeric( strIdExtender ) )
        {
            ResourceExtenderDTO resourceExtender = _resourceExtenderService.findByPrimaryKey( Integer.parseInt( strIdExtender ) );

            return resourceExtender != null && Blog.PROPERTY_RESOURCE_TYPE.equals( resourceExtender.getExtendableResourceType( ) )
                    && CommentResourceExtender.EXTENDER_TYPE_COMMENT.equals( resourceExtender.getExtenderType( ) ) ? resourceExtender : null;
        }

        ResourceExtenderDTOFilter filter = new ResourceExtenderDTOFilter( );
        filter.setFilterExtendableResourceType( Blog.PROPERTY_RESOURCE_TYPE );
        filter.setFilterExtenderType( CommentResourceExtender.EXTENDER_TYPE_COMMENT );
        filter.setIncludeWildcardResource( true );
        List<ResourceExtenderDTO> listExtenders = _resourceExtenderService.findByFilter( filter );

        if ( listExtenders == null )
        {
            return null;
        }

        return listExtenders.stream( ).filter( extender -> CommentConstants.CONSTANT_ALL_RESSOURCE_ID.equals( extender.getIdExtendableResource( ) ) )
                .findFirst( ).orElse( listExtenders.isEmpty( ) ? null : listExtenders.get( 0 ) );
    }

    /**
     * Gives the ids of the blogs whose versions the administrator edited.
     *
     * @param adminUser
     *            The administrator
     * @return The ids of the blogs
     */
    private Set<String> getEditedBlogs( AdminUser adminUser )
    {
        List<Integer> listIdBlogs = new ArrayList<>( );
        BlogSearchFilter filter = new BlogSearchFilter( );
        filter.setUserEditedBlogVersion( adminUser.getAccessCode( ) );
        BlogSearchService.getInstance( ).getSearchResults( filter, listIdBlogs );

        return listIdBlogs.stream( ).map( String::valueOf ).collect( Collectors.toSet( ) );
    }

    /**
     * Loads the answers and the workflow actions of a displayed comment.
     *
     * @param comment
     *            The comment
     * @param commentFilter
     *            The filter of the list
     * @param config
     *            The configuration of the extender
     * @param adminUser
     *            The administrator
     */
    private void completeComment( Comment comment, CommentFilter commentFilter, CommentExtenderConfig config, AdminUser adminUser )
    {
        if ( config.getAuthorizeSubComments( ) )
        {
            comment.setListSubComments( _commentService.findByIdParent( comment.getIdComment( ),
                    Integer.valueOf( Comment.COMMENT_STATE_PUBLISHED ).equals( commentFilter.getCommentState( ) ), commentFilter.getSortedAttributeName( ),
                    Boolean.TRUE.equals( commentFilter.getAscSort( ) ) ) );
        }

        if ( _workflowService.isAvailable( ) )
        {
            comment.setListWorkflowActions( _workflowService.getActions( comment.getIdComment( ),
                    _commentService.getResourceType( comment.getExtendableResourceType( ) ), config.getIdWorkflow( ), (User) adminUser ) );
        }
    }

    /**
     * Adds a parameter to an url when it has a value.
     *
     * @param url
     *            The url
     * @param strName
     *            The name of the parameter
     * @param strValue
     *            The value of the parameter
     */
    private static void addParameterIfPresent( UrlItem url, String strName, String strValue )
    {
        if ( StringUtils.isNotEmpty( strValue ) )
        {
            url.addParameter( strName, strValue );
        }
    }
}
