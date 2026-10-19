document.addEventListener('DOMContentLoaded', () => {
  const startButton = document.getElementById('startSimulationBtn');

  if (startButton) {
    startButton.addEventListener('click', () => {
      window.location.href = '../index.html';
    });
  }
});
