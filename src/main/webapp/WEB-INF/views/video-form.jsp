<%@ page pageEncoding="UTF-8" %><%@ include file="common/header.jspf" %>
<a class="back" href="${pageContext.request.contextPath}/video">← Video của tôi</a><div class="pagehead"><div><h1>${edit ? 'Sửa video' : 'Thêm video'}</h1><p class="muted">Chọn video MP4, WebM hoặc OGG và ảnh đại diện.</p></div></div>
<c:if test="${not empty error}"><p class="notice error" role="alert"><c:out value="${error}"/></p></c:if>
<c:if test="${uploadDemo}"><p class="notice">Form minh họa Servlet Multipart theo PDF và Form3: gửi bằng multipart/form-data; server lấy Part và lưu file bằng Part.write.</p></c:if>
<form class="panel editor" method="post" action="${pageContext.request.contextPath}${uploadDemo ? '/multiPartServlet' : edit ? '/video/update' : '/video/insert'}" enctype="multipart/form-data"><input type="hidden" name="csrf" value="${sessionScope.csrf}"><c:if test="${edit}"><input type="hidden" name="id" value="${video.videoId}"></c:if>
<label for="title">Tiêu đề *</label><input id="title" name="title" required maxlength="200" value="${fn:escapeXml(video.title)}">
<label for="description">Mô tả</label><textarea id="description" name="description" maxlength="500" rows="4"><c:out value="${video.description}"/></textarea>
<label for="categoryid">Danh mục</label><select id="categoryid" name="categoryid"><option value="">Không chọn</option><c:forEach items="${categories}" var="cat"><option value="${cat.categoryid}" ${video.category.categoryid == cat.categoryid ? 'selected' : ''}><c:out value="${cat.categoryname}"/></option></c:forEach></select>
<label for="videoFile">Upload video</label><input id="videoFile" type="file" name="${uploadDemo ? 'multiPartServlet' : 'videoFile'}" accept="video/mp4,video/webm,video/ogg"><small>Tối đa 100 MB. File tải lên được ưu tiên hơn link. ${edit ? 'Để trống nếu giữ video cũ.' : ''}</small>
<label for="source">Hoặc link video trực tiếp</label><input type="url" id="source" name="source" maxlength="1000" value="${fn:startsWith(video.source,'http') ? fn:escapeXml(video.source) : ''}" placeholder="https://example.com/video.mp4"><small>Nhập link MP4/WebM/OGG, không phải link trang YouTube.</small>
<label for="posterFile">Upload ảnh bìa</label><input id="posterFile" type="file" name="posterFile" accept="image/png,image/jpeg,image/gif,image/webp"><small>Ảnh tối đa 2 MB.</small>
<label for="poster">Hoặc link ảnh bìa</label><input id="poster" type="url" name="poster" maxlength="500" value="${fn:startsWith(video.poster,'http') ? fn:escapeXml(video.poster) : ''}">
<c:if test="${not empty video.poster}"><label><input type="checkbox" name="removePoster" value="1"> Xóa ảnh bìa hiện tại</label></c:if>
<fieldset><legend>Trạng thái</legend><label class="radio"><input type="radio" name="active" value="1" ${video.active == 1 ? 'checked' : ''} required> Công khai</label><label class="radio"><input type="radio" name="active" value="0" ${video.active == 0 ? 'checked' : ''}> Ẩn</label></fieldset>
<div class="formactions"><button type="submit">${edit ? 'Lưu thay đổi' : 'Đăng video'}</button><a class="secondary" href="${pageContext.request.contextPath}/video">Hủy</a></div></form>
<%@ include file="common/footer.jspf" %>
