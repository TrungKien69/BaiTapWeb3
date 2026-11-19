<%@ page pageEncoding="UTF-8" %>
<%@ include file="../common/header.jspf" %>
<div class="pagehead"><div><p class="eyebrow">QUẢN LÝ DỮ LIỆU</p><h1>Danh mục</h1><p class="muted">Thêm, cập nhật và sắp xếp danh mục của bạn.</p></div><a class="button" href="${pageContext.request.contextPath}/admin/category/add">+ Thêm danh mục</a></div>
<c:if test="${not empty flash}"><p class="notice" role="status"><c:out value="${flash}"/></p></c:if>
<section class="panel"><div class="toolbar"><form method="get" action="${pageContext.request.contextPath}/admin/categories" class="search"><input type="search" name="q" value="${fn:escapeXml(q)}" placeholder="Tìm tên danh mục…" aria-label="Tìm tên danh mục"><button type="submit" class="secondary">Tìm kiếm</button><c:if test="${not empty q}"><a href="${pageContext.request.contextPath}/admin/categories">Xóa lọc</a></c:if></form><span class="muted"><c:out value="${total}"/> danh mục</span></div>
<div class="tablewrap"><table><thead><tr><th>STT / Mã</th><th>Hình ảnh</th><th>Tên danh mục</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>
<c:forEach items="${listcate}" var="cate" varStatus="st">
<tr><td class="muted">${offset + st.index + 1} / #${cate.categoryid}</td><td>
<c:choose><c:when test="${empty cate.images}"><span class="noimage">Chưa có ảnh</span></c:when><c:otherwise>
<c:choose><c:when test="${fn:startsWith(cate.images, 'http://') or fn:startsWith(cate.images, 'https://')}"><c:set var="imgUrl" value="${cate.images}"/></c:when><c:otherwise><c:url var="imgUrl" value="/image"><c:param name="fname" value="${cate.images}"/></c:url></c:otherwise></c:choose>
<img class="thumb" src="${fn:escapeXml(imgUrl)}" alt="Ảnh danh mục" loading="lazy" referrerpolicy="no-referrer">
</c:otherwise></c:choose></td><td class="name"><c:out value="${cate.categoryname}"/></td><td><span class="badge ${cate.status == 1 ? 'on' : 'off'}">${cate.status == 1 ? 'Hoạt động' : 'Khóa'}</span></td>
<td><div class="actions"><a class="edit" href="${pageContext.request.contextPath}/admin/category/edit?id=${cate.categoryid}">Sửa</a>
<form action="${pageContext.request.contextPath}/admin/category/delete" method="post" onsubmit="return confirm('Bạn muốn xóa danh mục này?');"><input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="categoryid" value="${cate.categoryid}"><button class="danger" type="submit">Xóa</button></form></div></td></tr>
</c:forEach>
<c:if test="${empty listcate}"><tr><td colspan="5" class="empty">Chưa có danh mục phù hợp. Hãy thêm danh mục đầu tiên.</td></tr></c:if>
</tbody></table></div><nav class="pagination" aria-label="Phân trang"><span>Trang ${page} / ${totalPages}</span><div class="actions">
<c:if test="${page > 1}"><c:url var="prev" value="/admin/categories"><c:param name="q" value="${q}"/><c:param name="page" value="${page - 1}"/></c:url><a class="secondary" href="${fn:escapeXml(prev)}">← Trước</a></c:if>
<c:if test="${page < totalPages}"><c:url var="next" value="/admin/categories"><c:param name="q" value="${q}"/><c:param name="page" value="${page + 1}"/></c:url><a class="secondary" href="${fn:escapeXml(next)}">Sau →</a></c:if>
</div></nav></section>
<%@ include file="../common/footer.jspf" %>
