<%@ page pageEncoding="UTF-8" %><%@ include file="common/header.jspf" %>
<div class="pagehead"><div><p class="eyebrow">CHỈ DÀNH CHO CHẠY THỬ CỤC BỘ</p><h1>Hộp thư demo</h1><p class="muted">Thư bên dưới không được gửi ra email thật. Chỉ hiển thị email đang xác nhận trong phiên hiện tại.</p></div></div>
<p><a class="secondary" href="${pageContext.request.contextPath}/${sessionScope.pendingPurpose == 'RESET' ? 'reset-password' : 'activate'}">Quay lại nhập OTP</a></p>
<c:forEach items="${messages}" var="message"><pre class="panel mail"><c:out value="${message}"/></pre></c:forEach><c:if test="${empty messages}"><p class="notice">Chưa có thư cho yêu cầu hiện tại. Hãy đăng ký hoặc gửi mã quên mật khẩu trước.</p></c:if>
<%@ include file="common/footer.jspf" %>
