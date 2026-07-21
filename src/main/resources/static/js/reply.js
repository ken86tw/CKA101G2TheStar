/**
 * 問題回報後台管理 - 互動模組 (含檢查時顯示員工ID與員工姓名)
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
            const employeeId = item.employeeId || '無';
            const employeeName = item.employeeName || '無'; // 取得員工姓名

            // 安全編碼，避免引號或特殊字元導致 JS 錯誤
            const encodedSubject = encodeURIComponent(item.subject || '');
            const encodedContent = encodeURIComponent(item.content || '');
            const encodedReplyContent = encodeURIComponent(item.replyContent || '');

            // 總共 7 個 <td>，維持列表簡潔，不顯示員工ID與姓名
            const row = `<tr>
                <td>${item.ticketId}</td>
                <td>${nameDisplay}</td>
                <td>${item.email}</td>
                <td>${item.subject}</td>
                <td style="font-size: 13px; color: #888;">${createdAt}</td>
                <td style="font-size: 13px; color: #888;">${item.repliedAt ? formatDate(item.repliedAt) : '-'}</td>
                <td>
                    <div class="status-btn-container">
                        ${isReplied
                            ? `<button class="btn-action btn-cancel" onclick="openViewModal('${nameDisplay}', '${item.email}', '${employeeId}', '${employeeName}', '${encodedSubject}', '${encodedContent}', '${encodedReplyContent}')">已回覆</button>`
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

// 開啟待回覆彈窗：解碼並賦值
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

// 開啟已回覆的檢視彈窗 (檢查功能，包含顯示員工ID與員工姓名)
function openViewModal(memberName, email, employeeId, employeeName, encSubject, encContent, encReplyContent) {
    // 檢查頁面上有沒有對應的 viewModal 元素，沒有的話就動態建立一個，保證一定能用
    let viewModal = document.getElementById('viewModal');
    if (!viewModal) {
        const modalHtml = `
        <div id="viewModal" class="modal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); z-index: 9999; align-items: center; justify-content: center;">
            <div class="modal-content" style="background: white; padding: 40px; width: 550px; border: 1px solid #e0dcd5;">
                <header class="modal-header" style="margin-bottom: 20px; background: #f6f2ea; padding: 15px 30px; display: flex; align-items: center; justify-content: flex-start;">
                    <h3 style="margin: 0; font-family: var(--serif); color: #23211c; font-size: 24px; font-weight: 500;">檢視回報詳情</h3>
                </header>
                <div class="modal-body" style="font-size: 14px; color: #5b564c; line-height: 1.8;">
                    <div style="margin-bottom: 12px;"><strong>客人姓名：</strong><span id="viewMemberName"></span></div>
                    <div style="margin-bottom: 12px;"><strong>電子信箱：</strong><span id="viewEmail"></span></div>
                    <div style="margin-bottom: 12px;"><strong>處理員工ID：</strong><span id="viewEmployeeId" style="color: #9c7c4f; font-weight: 500;"></span></div>
                    <div style="margin-bottom: 12px;"><strong>處理員工姓名：</strong><span id="viewEmployeeName" style="color: #9c7c4f; font-weight: 500;"></span></div>
                    <div style="margin-bottom: 12px;"><strong>問題主旨：</strong><span id="viewSubject"></span></div>
                    <div style="margin-bottom: 12px; background: #fbf9f4; padding: 12px; border: 1px solid #e4ddd0;">
                        <strong>客人的問題內容：</strong>
                        <p id="viewContent" style="margin: 5px 0 0 0; white-space: pre-wrap;"></p>
                    </div>
                    <div style="margin-bottom: 12px; background: #f6f2ea; padding: 12px; border: 1px solid #e4ddd0;">
                        <strong>員工回覆內容：</strong>
                        <p id="viewReplyContent" style="margin: 5px 0 0 0; white-space: pre-wrap;"></p>
                    </div>
                </div>
                <footer class="modal-footer" style="margin-top: 20px; display: flex; justify-content: flex-end;">
                    <button onclick="closeViewModal()" class="btn-action">關閉</button>
                </footer>
            </div>
        </div>`;
        document.body.insertAdjacentHTML('beforeend', modalHtml);
        viewModal = document.getElementById('viewModal');
    }

    document.getElementById('viewMemberName').innerText = memberName;
    document.getElementById('viewEmail').innerText = email;
    document.getElementById('viewEmployeeId').innerText = employeeId;
    document.getElementById('viewEmployeeName').innerText = employeeName;
    document.getElementById('viewSubject').innerText = decodeURIComponent(encSubject);
    document.getElementById('viewContent').innerText = decodeURIComponent(encContent);
    document.getElementById('viewReplyContent').innerText = decodeURIComponent(encReplyContent);

    viewModal.style.display = 'flex';
}

// 關閉已回覆的檢視彈窗
function closeViewModal() {
    const viewModal = document.getElementById('viewModal');
    if (viewModal) {
        viewModal.style.display = 'none';
    }
}

// 送出回覆 (採用嚴格驗證：寄信成功才算完成，失敗則資料庫不寫入、不變成已回覆)
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
        const formData = new URLSearchParams();
        formData.append('ticketId', ticketId);
        formData.append('replyContent', replyContent);
        formData.append('email', email); // 將信箱一併傳給後端進行寄信驗證

        // 呼叫合併後的 /feedback/reply 接口
        const response = await fetch('/feedback/reply', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData
        });

        // 如果後端寄信失敗或發生例外，會噴出 500 錯誤與錯誤訊息
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "系統處理失敗");
        }

        // 成功情況
        alert("回覆成功，郵件已順利發送！");
        closeModal();
        loadFeedback(); // 重新載入表格，此時該筆會順利變成「已回覆」

    } catch (error) {
        console.error(error);
        // 畫面直接跳出原因（例如：尚未設定 Gmail 帳號或應用程式密碼），且因為交易 Rollback，資料庫不會被改成已回覆
        alert("回覆失敗！\n錯誤原因：" + error.message);
    } finally {
        btn.disabled = false;
        btn.innerText = "送出回覆";
    }
}