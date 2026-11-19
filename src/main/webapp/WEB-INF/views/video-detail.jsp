<%@ page pageEncoding="UTF-8" %><%@ include file="common/header.jspf" %>
<a class="back" href="${pageContext.request.contextPath}/home">← Trang chủ</a><div class="pagehead"><div><h1><c:out value="${video.title}"/></h1><p class="muted"><c:out value="${video.owner.fullName}"/> · ${video.views} lượt xem</p></div><c:if test="${owner}"><a class="secondary" href="${pageContext.request.contextPath}/video/edit?id=${video.videoId}">Sửa video</a></c:if></div>
<c:choose><c:when test="${fn:startsWith(video.source,'http')}"><c:set var="playUrl" value="${video.source}"/></c:when><c:otherwise><c:url var="playUrl" value="/media/video"><c:param name="id" value="${video.videoId}"/></c:url></c:otherwise></c:choose>
<video class="player" controls preload="metadata" playsinline><source src="${fn:escapeXml(playUrl)}">Trình duyệt không hỗ trợ phát video. Hãy dùng Chrome hoặc Edge.</video>
<div class="panel description"><h2>Mô tả</h2><p><c:out value="${empty video.description ? 'Chưa có mô tả.' : video.description}"/></p><small>Nếu video không phát, hãy dùng MP4 mã hóa H.264/AAC hoặc thử file mẫu trong thư mục samples.</small></div>
<%@ include file="common/footer.jspf" %>
