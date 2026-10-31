/**
 * Focusroom V2 - Synchronized Pomodoro Focus Timer
 * Ensures timer state is authoritatively tracked and calculated by the Java backend
 */
let timerState = {
    sessionId: null,
    status: 'COMPLETED',
    sessionType: 'FOCUS',
    plannedMinutes: 25,
    activeSeconds: 0,
    remainingSeconds: 25 * 60,
    running: false
};

let localInterval = null;
const circumference = 2 * Math.PI * 120; // Radius = 120

function initTimer() {
    const circle = document.getElementById('timerProgressCircle');
    if (circle) {
        circle.style.strokeDasharray = `${circumference} ${circumference}`;
        circle.style.strokeDashoffset = '0';
    }

    // Fetch initial authoritative state from backend
    fetchTimerState();

    // Attach button listeners
    const btnStart = document.getElementById('btnTimerStart');
    const btnPause = document.getElementById('btnTimerPause');
    const btnResume = document.getElementById('btnTimerResume');
    const btnComplete = document.getElementById('btnTimerComplete');
    const btnCancel = document.getElementById('btnTimerCancel');

    if (btnStart) btnStart.addEventListener('click', () => sendTimerAction('START'));
    if (btnPause) btnPause.addEventListener('click', () => sendTimerAction('PAUSE'));
    if (btnResume) btnResume.addEventListener('click', () => sendTimerAction('RESUME'));
    if (btnComplete) btnComplete.addEventListener('click', () => sendTimerAction('COMPLETE'));
    if (btnCancel) btnCancel.addEventListener('click', () => sendTimerAction('CANCEL'));

    // Mode buttons (25m Focus, 5m Short Break, 15m Long Break)
    document.querySelectorAll('.timer-mode-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const minutes = parseInt(e.currentTarget.getAttribute('data-minutes'));
            const type = e.currentTarget.getAttribute('data-type');
            selectMode(minutes, type);
        });
    });
}

function selectMode(minutes, type) {
    if (timerState.running) {
        if (!confirm('Changing modes will reset your ongoing timer. Proceed?')) return;
    }
    const plannedInput = document.getElementById('timerPlannedMinutes');
    const typeInput = document.getElementById('timerSessionType');
    if (plannedInput) plannedInput.value = minutes;
    if (typeInput) typeInput.value = type;

    document.querySelectorAll('.timer-mode-btn').forEach(b => b.classList.remove('active', 'btn-primary'));
    const activeBtn = document.querySelector(`.timer-mode-btn[data-type="${type}"]`);
    if (activeBtn) activeBtn.classList.add('active', 'btn-primary');

    sendTimerAction('CANCEL', { plannedMinutes: minutes, sessionType: type });
}

function sendTimerAction(action, overrides = {}) {
    const plannedMinutes = overrides.plannedMinutes || parseInt(document.getElementById('timerPlannedMinutes')?.value || '25');
    const sessionType = overrides.sessionType || (document.getElementById('timerSessionType')?.value || 'FOCUS');
    const subject = document.getElementById('timerSubject')?.value || 'General Study';
    const roomId = document.getElementById('timerRoomId')?.value || null;

    const payload = {
        action: action,
        plannedMinutes: plannedMinutes,
        sessionType: sessionType,
        subject: subject,
        roomId: roomId ? parseInt(roomId) : null
    };

    const csrfToken = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    const headers = { 'Content-Type': 'application/json' };
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }

    fetch('/api/timer/action', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(data => {
        updateTimerUI(data);
    })
    .catch(err => console.error('Timer action failed:', err));
}

function fetchTimerState() {
    fetch('/api/timer/state')
        .then(res => res.json())
        .then(data => updateTimerUI(data))
        .catch(err => console.error('Timer fetch failed:', err));
}

function updateTimerUI(state) {
    timerState = state;

    if (localInterval) {
        clearInterval(localInterval);
        localInterval = null;
    }

    renderDisplay();

    // If running, tick every second locally
    if (timerState.running) {
        localInterval = setInterval(() => {
            if (timerState.remainingSeconds > 0) {
                timerState.remainingSeconds--;
                timerState.activeSeconds++;
                renderDisplay();
            } else {
                clearInterval(localInterval);
                playChime();
                sendTimerAction('COMPLETE');
            }
        }, 1000);
    }

    // Toggle button visibility
    const btnStart = document.getElementById('btnTimerStart');
    const btnPause = document.getElementById('btnTimerPause');
    const btnResume = document.getElementById('btnTimerResume');
    const btnComplete = document.getElementById('btnTimerComplete');
    const btnCancel = document.getElementById('btnTimerCancel');
    const statusText = document.getElementById('timerStatusText');

    if (btnStart) btnStart.style.display = (!timerState.running && timerState.status !== 'PAUSED') ? 'inline-flex' : 'none';
    if (btnPause) btnPause.style.display = timerState.running ? 'inline-flex' : 'none';
    if (btnResume) btnResume.style.display = (!timerState.running && timerState.status === 'PAUSED') ? 'inline-flex' : 'none';
    if (btnComplete) btnComplete.style.display = (timerState.running || timerState.status === 'PAUSED') ? 'inline-flex' : 'none';
    if (btnCancel) btnCancel.style.display = (timerState.running || timerState.status === 'PAUSED') ? 'inline-flex' : 'none';

    if (statusText) {
        if (timerState.status === 'PAUSED') {
            statusText.innerText = 'Paused';
            statusText.style.color = 'var(--warning)';
        } else if (timerState.running) {
            statusText.innerText = timerState.sessionType === 'FOCUS' ? 'Focusing...' : 'On Break';
            statusText.style.color = 'var(--primary)';
        } else {
            statusText.innerText = 'Ready';
            statusText.style.color = 'var(--text-muted)';
        }
    }
}

function renderDisplay() {
    const minutes = Math.floor(timerState.remainingSeconds / 60);
    const seconds = timerState.remainingSeconds % 60;
    const formatted = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;

    const display = document.getElementById('timerTimeDisplay');
    if (display) {
        display.innerText = formatted;
    }

    // Update document title for background tracking
    document.title = (timerState.running ? `(${formatted}) ` : '') + 'Focusroom';

    // Update SVG circle stroke offset
    const circle = document.getElementById('timerProgressCircle');
    if (circle) {
        const totalSec = timerState.plannedMinutes * 60;
        const progress = Math.min(1, Math.max(0, timerState.remainingSeconds / totalSec));
        const offset = circumference - (progress * circumference);
        circle.style.strokeDashoffset = offset;
    }
}

function playChime() {
    try {
        const ctx = new (window.AudioContext || window.webkitAudioContext)();
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.type = 'sine';
        osc.frequency.setValueAtTime(587.33, ctx.currentTime); // D5
        osc.frequency.setValueAtTime(880, ctx.currentTime + 0.15); // A5
        gain.gain.setValueAtTime(0.3, ctx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.8);
        osc.start();
        osc.stop(ctx.currentTime + 0.8);
    } catch (e) {
        // AudioContext not allowed without gesture
    }
}

document.addEventListener('DOMContentLoaded', initTimer);
