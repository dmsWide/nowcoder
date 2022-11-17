$(function () {
    $("#uploadForm").submit(upload);
    /*$("#forgetForm").submit(forget);*/
});

function upload() {
    $.ajax({
        url: "http://upload.qiniup.com",
        method: "post",
        processData: false,/*不要将表单的内容转换为字符串*/
        contentType: false,/*不让jquery设置上传的类型 浏览器会自己设置*/
        data: new FormData($("#uploadForm")[0]),
        success: function (data) {
            /*七牛云服务器返回json对象*/
            if (data && data.code == 0) {
                // 更新头像访问路径
                $.post(
                    CONTEXT_PATH + "/user/header/url",
                    {"fileName": $("input[name='key']").val()},
                    function (data) {
                        /*普通字符串解析为json对象*/
                        data = $.parseJSON(data);
                        if (data.code == 0) {
                            /*成功的话 就刷新当前页面*/
                            window.location.reload();
                        } else {
                            alert(data.msg);
                        }
                    }
                );
            } else {
                alert("上传失败！");
            }
        }

    });
    //return false 表示事件到此为止 不需要再提交表单 提交表单的操作之前已经做过了
    return false;
}

function forget() {
    $.post(
        CONTEXT_PATH + "/user/forgetPassword",
        {"oldPassword": $("input[name='oldPassword']").val(), "newPassword": $("input[name='newPassword']").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                // window.location.href = "/logout";
            } else {
                alert(data.msg);
            }
        }
    );
}
