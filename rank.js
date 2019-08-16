

function assignRank() {
    var players = ["nahum", "blamethedogs", "Murfer", "henry", "ZOOZ"];
    var RP = [Math.floor(Math.random() * 300) + 1, Math.floor(Math.random() * 300) + 1,
            Math.floor(Math.random() * 300) + 1, Math.floor(Math.random() * 300) + 1,
            Math.floor(Math.random() * 300) + 1];

    var sortedRP = RP.sort(function(a, b){return b-a});
    var i;
    for (i = 0; i < players.length; i++) {

    document.getElementById("leaderboard").innerHTML += players[i] + " - "
     + sortedRP[i] + "<br>";
    }
}

function onPlayerKill() {
// Just for testing purposes, actual code will not have random numbers here obviously
  pRank = Math.floor(Math.random() * 20) + 1;
  eRank = Math.floor(Math.random() * 20) + 1;

  document.getElementById("peranks").innerHTML =
  "Your rank: <b>" + pRank + "</b><br>" + "Enemy rank: <b>" + eRank + "<b>";

  if (pRank >= eRank) {
    rpGain = Math.floor((eRank*5)-((pRank/10)*5)+10);
  } else if (pRank < eRank) {
    rpGain = Math.floor((eRank*10)+(pRank*(eRank-pRank)));
  }
  document.getElementById("rpGain").innerHTML = "+" + rpGain + "RP";
}

function onPlayerDeath() {
    dpRank = Math.floor(Math.random() * 20) + 1;

    //In the real code "d_prank" here will be replaced with pRank
    document.getElementById("d_prank").innerHTML = "Your rank: <b>" + dpRank;

    if (dpRank > 5) {
        rpLossDeath = Math.floor(dpRank*((Math.random() * 1.6) + 1.35));
        //rpLossDeath = Math.ceil((Math.pow(dpRank, 1.5))/(Math.random() * 6.25) + 6.0);
    } else if (dpRank <= 5) {
        rpLossDeath = 0;
    }
    document.getElementById("rpLoss").innerHTML = "-" + rpLossDeath + "RP";
}