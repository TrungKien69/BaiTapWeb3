document.addEventListener('DOMContentLoaded',()=>{
 document.querySelectorAll('form').forEach(form=>{
  form.querySelectorAll('input:not([type=hidden]):not([type=radio]):not([type=checkbox]),textarea,select').forEach(el=>el.classList.add(el.tagName==='SELECT'?'form-select':'form-control'));
  const check=()=>{form.querySelectorAll('input,textarea').forEach(el=>{el.setCustomValidity('');if(el.required&&el.type!=='file'&&el.type!=='password'&&!el.value.trim())el.setCustomValidity('Vui lòng nhập thông tin.');if(el.type==='file'&&el.files.length){let max=(el.name==='videoFile'||el.name==='multiPartServlet')?100:2;if(el.files[0].size>max*1024*1024)el.setCustomValidity('File tối đa '+max+' MB.');}});
   const pw=form.querySelector('[name=password]'),confirm=form.querySelector('[name=confirm]');if(pw&&confirm&&pw.value!==confirm.value)confirm.setCustomValidity('Mật khẩu xác nhận không khớp.');};
  form.addEventListener('input',check);form.addEventListener('change',check);
  form.addEventListener('submit',e=>{check();form.classList.add('was-validated');if(!form.checkValidity()){e.preventDefault();form.reportValidity();}});
 });
});
