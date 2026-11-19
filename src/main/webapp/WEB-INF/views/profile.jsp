<%@ page pageEncoding="UTF-8" %><%@ include file="common/header.jspf" %>
<div class="pagehead"><div><p class="eyebrow">TÀI KHOẢN CỦA BẠN</p><h1>Hồ sơ cá nhân</h1><p class="muted">Cập nhật thông tin và xác nhận email mới bằng OTP.</p></div></div>
<c:if test="${not empty flash}"><p class="alert alert-success" role="status"><c:out value="${flash}"/></p></c:if>
<c:if test="${not empty error}"><p class="alert alert-danger" role="alert"><c:out value="${error}"/></p></c:if>
<div class="row g-4"><section class="col-lg-7"><form class="panel editor" method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/profile/update">
<h2>Thông tin cá nhân</h2><input type="hidden" name="csrf" value="${sessionScope.csrf}">
<c:if test="${not empty profile.images}"><img class="rounded-circle mb-3" width="100" height="100" style="object-fit:cover" src="${fn:escapeXml(profile.images)}" alt="Ảnh đại diện"></c:if>
<label for="fullName">Họ tên *</label><input id="fullName" name="fullName" required maxlength="100" value="${fn:escapeXml(profile.fullName)}" autocomplete="name">
<label for="phone">Số điện thoại *</label><input id="phone" type="tel" name="phone" required pattern="[+]?[0-9]{9,15}" maxlength="16" value="${fn:escapeXml(profile.phone)}" autocomplete="tel"><small>9–15 chữ số; có thể bắt đầu bằng dấu +.</small>
<label for="images">Ảnh đại diện</label><input id="images" type="file" name="images" accept="image/png,image/jpeg"><small>PNG/JPEG, tối đa 2 MB. Ảnh được upload lên Cloudinary. Để trống để giữ ảnh hiện tại.</small><button class="btn btn-primary mt-3" type="submit">Lưu hồ sơ</button></form></section>
<section class="col-lg-5"><div class="panel"><h2>Đổi email</h2><p>Email hiện tại: <strong><c:out value="${profile.email}"/></strong></p>
<form method="post" action="${pageContext.request.contextPath}/profile/email/request"><input type="hidden" name="csrf" value="${sessionScope.csrf}"><label for="newEmail">Email mới *</label><input id="newEmail" name="email" type="email" required maxlength="254" autocomplete="email"><button class="btn btn-primary mt-3" type="submit">Gửi OTP đến email mới</button><p class="muted">Mỗi lần gửi cách nhau 60 giây. OTP hết hạn sau 5 phút.</p></form>
<c:if test="${not empty profile.pendingEmail}"><hr><c:if test="${demoProfileMail}"><p><a href="${pageContext.request.contextPath}/demo/mail">Xem OTP trong hộp thư demo</a></p></c:if><p>Đang chờ xác nhận: <strong><c:out value="${profile.pendingEmail}"/></strong></p>
<form method="post" action="${pageContext.request.contextPath}/profile/email/confirm"><input type="hidden" name="csrf" value="${sessionScope.csrf}"><label for="otp">OTP gồm 6 số</label><input id="otp" name="otp" required pattern="[0-9]{6}" maxlength="6" inputmode="numeric" autocomplete="one-time-code"><button class="btn btn-success mt-3" type="submit">Xác nhận đổi email</button></form>
<form class="mt-2" method="post" action="${pageContext.request.contextPath}/profile/email/cancel"><input type="hidden" name="csrf" value="${sessionScope.csrf}"><button class="btn btn-outline-secondary" type="submit">Hủy đổi email</button></form></c:if>
</div></section></div><%@ include file="common/footer.jspf" %>
