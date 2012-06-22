$(document).ready(function () {
    var applicationAdd = function(){
        var application = $("#application-name").val();
        jagg.post("/site/blocks/application/application-add/ajax/application-add.jag", {
            action:"addApplication",
            application:application
        }, function (result) {
            if (result.error == false) {
                $.cookie('highlight','true');
                window.location.reload();
            } else {
                jagg.message({content:result.error,type:"error"});
            }
        }, "json");
    };
    $("#application-add-button").click(applicationAdd);


    $('#application-name').keydown(function(event) {
         if (event.which == 13) {
               applicationAdd();
            }
        });
});