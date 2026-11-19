<%@ page pageEncoding="UTF-8" %><%@ include file="common/header.jspf" %>
<div class="pagehead"><div><p class="eyebrow">KHÁM PHÁ & CHIA SẺ</p><h1>Video mới nhất</h1><p class="muted">10 video công khai mới nhất từ cộng đồng.</p></div><a class="button" href="${pageContext.request.contextPath}/video/add">+ Đăng video</a></div>
<c:if test="${not empty flash}"><p class="notice"><c:out value="${flash}"/></p></c:if><%@ include file="video-cards.jspf" %>
<%@ include file="common/footer.jspf" %>
