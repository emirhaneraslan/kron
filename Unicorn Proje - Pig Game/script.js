const newGameButton = document.querySelector(".btn--new");
const rollDiceButton = document.querySelector(".btn--roll");
const holdButton = document.querySelector(".btn--hold");
const playerOneCurrentScore = document.querySelector("#current--0");
const playerTwoCurrentScore = document.querySelector("#current--1");
const playerOneTotalScore = document.querySelector("#score--0"); 
const playerTwoTotalScore = document.querySelector("#score--1");
const diceImg = document.querySelector(".dice");
const sections = document.querySelectorAll("section");


document.addEventListener("DOMContentLoaded", () => {
    playerOneTotalScore.textContent = 0; 
    playerTwoTotalScore.textContent = 0;
  });
  
  window.addEventListener("beforeunload", () => {
    playerOneTotalScore.textContent = 0;
    playerTwoTotalScore.textContent = 0;
  });
  
  
  let currentScore = 0; 
  let activePlayer = 0; 
  
  function switchPlayer() {
    activePlayer = activePlayer === 0 ? 1 : 0;
    sections[activePlayer].classList.add("player--active");
    sections[1 - activePlayer].classList.remove("player--active");
  }
  
  function rollDice() {
    const dice = Math.floor(Math.random() * 6) + 1;
    diceImg.setAttribute("src", `dice-${dice}.png`);
  
    if (dice !== 1) {
      currentScore += dice; 
      document.querySelector(`#current--${activePlayer}`).textContent =
        currentScore;
    } else {
      currentScore = 0;
      document.querySelector(`#current--${activePlayer}`).textContent =
        currentScore;
      switchPlayer();
    }
  }
  
  function hold() {
    document.querySelector(`#score--${activePlayer}`).textContent = 
      parseInt(document.querySelector(`#score--${activePlayer}`).textContent) + 
      currentScore;
    
    document.querySelector(`#current--${activePlayer}`).textContent = 0;
    currentScore = 0;
    
    if (parseInt(document.querySelector(`#score--${activePlayer}`).textContent) >= 100) {
      activePlayer === 0 ? alert("Player 1 won") : alert("Player 2 won")
      newGame(); 
    } else {
      switchPlayer();
    }
  }
  
  function newGame() {
    playerOneCurrentScore.textContent = 0;
    playerTwoCurrentScore.textContent = 0;
    playerOneTotalScore.textContent = 0;
    playerTwoTotalScore.textContent = 0;
    currentScore = 0;
    activePlayer = 0;
    sections[0].classList.add("player--active");
    sections[1].classList.remove("player--active");
  }
  
  newGameButton.addEventListener("click", newGame);
  rollDiceButton.addEventListener("click", rollDice);
  holdButton.addEventListener("click", hold);
  

