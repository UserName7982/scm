console.log("admin login");

document.querySelector("#Image").addEventListener("change", function(event) {
    var file = event.target.files[0];
    var reader = new FileReader();
    reader.onload = function() {
        document.querySelector("#Upload_image").setAttribute("src", reader.result);
    };
    reader.readAsDataURL(file);
});
