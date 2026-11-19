<%@ page pageEncoding="UTF-8" %><%@ include file="common/header.jspf" %>
<div class="pagehead"><div><p class="eyebrow">THƯ VIỆN CÁ NHÂN</p><h1>Video của tôi</h1><p class="muted">${total} video · 6 video mỗi trang.</p></div><a class="button" href="${pageContext.request.contextPath}/video/add">+ Thêm video</a></div>
<c:if test="${not empty flash}"><p class="notice"><c:out value="${flash}"/></p></c:if><c:set var="myVideos" value="${true}"/><%@ include file="video-cards.jspf" %>
<nav class="pagination"><span>Trang ${page} / ${pages}</span><div class="actions"><c:if test="${page > 1}"><a class="secondary" href="${pageContext.request.contextPath}/video?page=${page - 1}">← Trước</a></c:if><c:if test="${page < pages}"><a class="secondary" href="${pageContext.request.contextPath}/video?page=${page + 1}">Sau →</a></c:if></div></nav>
<%@ include file="common/footer.jspf" %>
