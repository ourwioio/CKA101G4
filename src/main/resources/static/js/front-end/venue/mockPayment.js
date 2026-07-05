function updateTimer() {
    if (secondsLeft <= 0) {
        document.getElementById('countdown').textContent = '00:00';
        document.getElementById('expireForm').submit();
        return;
    }
    var m = Math.floor(secondsLeft / 60);
    var s = secondsLeft % 60;
    document.getElementById('countdown').textContent =
        String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0');
    secondsLeft--;
}

updateTimer();
setInterval(updateTimer, 1000);
