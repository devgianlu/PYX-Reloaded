/**
#1 - this file is redundant as hell
#2 - it will be better to have all user inputs in the modal done entirely in JS. Don't clutter
     lobbies.html more than it needs to be
#3 - This is a work in progress. Most of the logic for the "New Game" dialog should go in here as well.
#4 - I already tried to get this into a parameterized function. I gave up, it's JS and fuck the nonexistent type system
#5 - Happy hacking!!
**/

function score_options() {
  var sel_min = 4;
  var sel_max = 69;
  var iter = sel_min;
  var select = document.createElement("select");
  select.setAttribute("id", "score_limit");
  window.onload = document.getElementById("rounds").appendChild(select);

  while (iter >= sel_min && iter <= sel_max) {
    var count = iter; 
    var value = "RoundOption" + count;
    var option = document.createElement("option");
    option.setAttribute("value", value);
    option.innerHTML = count;
    var final = option;
    window.onload = document.getElementById("score_limit").appendChild(final);
    iter++;
  }
}

function player_options() {
  var sel_min = 3;
  var sel_max = 20;
  var iter = sel_min;
  var select = document.createElement("select");
  select.setAttribute("id", "player_limit");
  document.getElementById("players").appendChild(select);

  while (iter >= sel_min && iter <= sel_max) {
    var count = iter; 
    var value = "PlayerOption" + count;
    var option = document.createElement("option");
    option.setAttribute("value", value);
    option.innerHTML = count;
    var final = option;
    window.onload = document.getElementById("player_limit").appendChild(final);
    iter++;
  }
}

function spectator_options() {
  var sel_min = 3;
  var sel_max = 20;
  var iter = sel_min;
  var select = document.createElement("select");
  select.setAttribute("id", "spectator_limit");
  document.getElementById("spectators").appendChild(select);

  while (iter >= sel_min && iter <= sel_max) {
    var count = iter; 
    var value = "SpectatorOption" + count;
    var option = document.createElement("option");
    option.setAttribute("value", value);
    option.innerHTML = count;
    var final = option;
    window.onload = document.getElementById("spectator_limit").appendChild(final);
    iter++;
  }
}

window.onload = function new_game_elements() {
  score_options();
  player_options();
  spectator_options();
}

