console.log("script loaded");

let currentTheme = getTheme();

changeTheme();

function changeTheme(){
    changepagetheme(currentTheme, currentTheme);
    document.querySelector("html").classList.add(currentTheme);
    const changeThemeButton = document.querySelector("#theme_change_button");
    changeThemeButton.addEventListener("click", (event) => {
        console.log("button clicked");
        let oldTheme = currentTheme;
        if(currentTheme === "dark"){
            currentTheme = "light";
        }else{
            currentTheme = "dark";
        }
        changepagetheme(currentTheme, oldTheme);
    });
}

function changepagetheme(newTheme, oldTheme){
    setTheme(newTheme);
    document.querySelector("html").classList.remove(oldTheme);
    document.querySelector("html").classList.add(newTheme);
    document.querySelector("#theme_change_button").querySelector("span").textContent = newTheme === "light" ? "Dark" : "Light";
}

function setTheme(theme){
    localStorage.setItem("theme", theme);
}

function getTheme(){
    let theme = localStorage.getItem("theme");
    return theme ? theme : "light";
}
