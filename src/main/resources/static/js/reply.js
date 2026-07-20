/**
 * 問題回報後台管理 - 互動模組 (7欄位對齊版)
 */
window.addEventListener('DOMContentLoaded', () => {
    loadFeedback();
});

// 格式化日期函數
function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.getFullYear() + '-' + 
           String(date.getMonth() + 1).padStart(2, '0') + '-' + 
           String(date.getDate()).padStart(2, '0') + ' ' + 
           String(date.getHours()).padStart(2, '0') + ':' + 
           String(date.getMinutes()).padStart(2, '0');
}

// 載入所有回報資料
async function loadFeedback() {
    try {
        const response = await fetch('/feedback/all');
        if (!response.ok) throw new Error('無法獲取資料');
        const data = await response.json();

        const tbody = document.getElementById('feedbackTableBody');
        tbody.innerHTML = '';

        // 計算待處理數量
        const pendingItems = data.filter(item => item.ticketStatus === 0);
        document.getElementById('totalCount').innerText = pendingItems.length;

        data.forEach(item => {
            const isReplied = item.ticketStatus === 1;
            const nameDisplay = item.memberName || '訪客';
            const createdAt = formatDate(item.createdAt);
            const repliedAt = formatDate(item.repliedAt);

            // 安全編碼，避免引號導致 JS 錯誤
            const encodedSubject = encodeURIComponent(item.subject || '');
            const encodedContent = encodeURIComponent(item.content || '');

            // 總共 7 個 <td>，精確對齊上方修改後的 <thead>
            const row = `<tr>
                <td>${item.ticketId}</td>
                <td>${nameDisplay}</td>
                <td>${item.email}</td>
                <td>${item.subject}</td>
                <td style="font-size: 13px; color: #888;">${createdAt}</td>
                <td style="font-size: 13px; color: #888;">${repliedAt}</td>
                <td>
                    <div class="status-btn-container">
                        ${isReplied
                            ? '<button disabled class="status-badge replied">已回覆</button>'
                            : `<button class="btn-action" onclick="openModal(${item.ticketId}, '${item.email}', '${encodedSubject}', '${encodedContent}')">待回覆</button>`
                        }
                    </div>
                </td>
            </tr>`;
            tbody.insertAdjacentHTML('beforeend', row);
        });
    } catch (error) {
        console.error('載入資料失敗:', error);
    }
}

// 開啟彈窗：解碼並賦值
function openModal(id, email, encSubject, encContent) {
    const subject = decodeURIComponent(encSubject);
    const content = decodeURIComponent(encContent);

    document.getElementById('modalTicketId').value = id;
    document.getElementById('modalEmail').value = email;
    document.getElementById('modalContent').innerText = `主旨：${subject}\n內容：${content}`;
    
    const textArea = document.getElementById('replyContent');
    textArea.style.borderColor = '#e0dcd5';
    textArea.value = '';

    document.getElementById('replyModal').style.display = 'flex';
}

function closeModal() {
    document.getElementById('replyModal').style.display = 'none';
}

// 送出回覆
async function submitReply(event) {
    const btn = event.target;
    const textArea = document.getElementById('replyContent');
    const ticketId = document.getElementById('modalTicketId').value;
    const email = document.getElementById('modalEmail').value;
    const replyContent = textArea.value;

    if (!replyContent.trim()) {
        textArea.style.borderColor = '#a85b50';
        alert("回覆內容不可為空！");
        return;
    }

    btn.disabled = true;
    btn.innerText = "處理中...";

    try {
        const dbFormData = new URLSearchParams();
        dbFormData.append('ticketId', ticketId);
        dbFormData.append('replyContent', replyContent);
        dbFormData.append('employeeId', 1);

        const dbRes = await fetch('/feedback/reply', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: dbFormData
        });

        if (!dbRes.ok) throw new Error("資料庫更新失敗");

        const mailRes = await fetch('/feedback/send', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ 'ticketId': ticketId, 'email': email, 'message': replyContent })
        });

        if (mailRes.ok) {
            alert("回覆成功且郵件已發送！");
            closeModal();
            loadFeedback();
        } else {
            alert("資料庫更新成功，但郵件伺服器寄送失敗。");
        }
    } catch (error) {
        console.error(error);
        alert("操作失敗，請稍後再試。");
    } finally {
        btn.disabled = false;
        btn.innerText = "送出回覆";
    }
}