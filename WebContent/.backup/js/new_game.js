/**
 #1 - it will be better to have all user inputs in the modal done entirely in JS. Don't clutter
 lobbies.html more than it needs to be
 #2 - This is a work in progress. Most of the logic for the "New Game" dialog should go in here as well.
 #3 - Happy hacking!!
 **/

function populateDropdown(dropdown, dgo) {
    for (var i = dgo.min; i <= dgo.max; i++) {
        var option = document.createElement("option");
        option.setAttribute("value", i);
        if (i === dgo.default) option.setAttribute("selected", '');
        option.innerHTML = i;
        dropdown.appendChild(option);
    }
}

