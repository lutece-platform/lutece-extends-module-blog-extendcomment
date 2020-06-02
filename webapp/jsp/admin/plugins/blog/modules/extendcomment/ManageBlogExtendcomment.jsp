
<jsp:useBean id="blogExtendComment" scope="session" class="fr.paris.lutece.plugins.blog.modules.extendcomment.web.BlogExtendCommentJspBean" />
<% String strContent = blogExtendComment.processController( request , response ); %>

<%@ page errorPage="../../../../ErrorPage.jsp" %>
<jsp:include page="../../../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../../../AdminFooter.jsp" %>