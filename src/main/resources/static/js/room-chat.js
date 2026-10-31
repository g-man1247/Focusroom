/**
 * Focusroom V2 - Real-Time Room Chat & Presence via WebSocket (SockJS + STOMP)
 */
let stompClient = null;
let currentRoomId = null;
let currentUsername = null;

function initRoomChat(roomId, username) {
    currentRoomId = roomId;
    currentUsername = username;

    const chatMessages = document.getElementById('chatMessages');
    if (chatMessages) {
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // Quiet in production console

    stompClient.connect({}, function (frame) {
        // Subscribe to public room topic
        stompClient.subscribe('/topic/room/' + currentRoomId, function (payload) {
            const message = JSON.parse(payload.body);
            appendChatMessage(message);
        });

        // Announce user joined
        stompClient.send('/app/chat.addUser/' + currentRoomId, {}, JSON.stringify({
            roomId: currentRoomId,
            senderUsername: currentUsername,
            messageType: 'JOIN'
        }));
    }, function (error) {
        console.warn('STOMP connection error:', error);
    });

    const chatForm = document.getElementById('chatForm');
    if (chatForm) {
        chatForm.addEventListener('submit', function (e) {
            e.preventDefault();
            const input = document.getElementById('chatInput');
            const text = input ? input.value.trim() : '';
            if (text && stompClient && stompClient.connected) {
                stompClient.send('/app/chat.sendMessage/' + currentRoomId, {}, JSON.stringify({
                    roomId: currentRoomId,
                    senderUsername: currentUsername,
                    message: text,
                    messageType: 'CHAT'
                }));
                input.value = '';
            }
        });
    }
}

function appendChatMessage(message) {
    const chatMessages = document.getElementById('chatMessages');
    if (!chatMessages) return;

    const msgEl = document.createElement('div');

    if (message.messageType === 'JOIN' || message.messageType === 'LEAVE' || message.messageType === 'SYSTEM') {
        msgEl.className = 'chat-msg system';
        msgEl.innerText = `${message.senderFullName || message.senderUsername} ${message.message}`;
    } else {
        const isMine = message.senderUsername === currentUsername;
        msgEl.className = 'chat-msg ' + (isMine ? 'mine' : 'other');

        if (!isMine) {
            const senderName = document.createElement('span');
            senderName.className = 'chat-sender-name';
            senderName.innerText = message.senderFullName || message.senderUsername;
            msgEl.appendChild(senderName);
        }

        const bubble = document.createElement('div');
        bubble.className = 'chat-bubble';
        bubble.innerText = message.message;
        msgEl.appendChild(bubble);
    }

    chatMessages.appendChild(msgEl);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}
