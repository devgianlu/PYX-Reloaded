/**
 #1 - it will be better to have all user inputs in the modal done entirely in JS. Don't clutter
 lobbies.html more than it needs to be
 #2 - This is a work in progress. Most of the logic for the "New Game" dialog should go in here as well.
 #3 - Happy hacking!!
 **/

function populateDropdown(dropdown, min, max) {
    for (var i = min; i <= max; i++) {
        var option = document.createElement("option");
        option.setAttribute("value", i);
        option.innerHTML = i;
        dropdown.appendChild(option);
    }
}

window.onload = function () {
    populateDropdown(document.getElementById("goal"), 4, 69);
    populateDropdown(document.getElementById("playersLimit"), 3, 20);
    populateDropdown(document.getElementById("spectatorsLimit"), 3, 20);
};

