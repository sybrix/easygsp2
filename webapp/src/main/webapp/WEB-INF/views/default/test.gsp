<%@ extends 'parent.jsp' %>

<%@ block main %>

hello
${hello}



<button id="btn">CLick ajax Me</button>

<form action="http://localhost:8080/" enctype="multipart/form-data" method="post">
    <input type="file" name="file"/>
    <input type="submit" name="n" value="x"/>
</form>

<%@ include 'includeme.gsp' %>

<%@ script %>
<script>
    var markers = [
        {"position": "128.3657142857143", "markerPosition": "7"},
        {"position": "235.1944023323615", "markerPosition": "19"},
        {"position": "42.5978231292517", "markerPosition": "-3"}
    ];


    $("#btn").click(function () {
        $.ajax({
            type: "POST",
            url: "/x",
            data: JSON.stringify(markers),
            contentType: "application/json",
            dataType: "json",
            processData: false,
            success: function (data) {
                alert(data);
            },
            failure: function (errMsg) {
                alert(errMsg);
            }
        });

    })
</script>
<%@ endScript %>

<%@ endblock %>
