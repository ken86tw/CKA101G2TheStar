document.getElementById('feedbackForm').addEventListener('submit', function(e) {
    e.preventDefault(); // 1. 阻止表單預設跳轉

    // 2. 獲取並處理欄位資料
    const subject = document.getElementById('subject').value.trim();
    const content = document.getElementById('content').value.trim();
    const email = document.getElementById('email').value.trim();

    // 獲取會員ID，若為空則保持 null
    const memberIdValue = document.getElementById('memberId').value;
    const memberId = memberIdValue ? parseInt(memberIdValue) : null;

    // 定義 Email 的正規表達式檢查
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    // 3. 前端簡易防禦性檢查
    if (!subject || !content || !email) {
        alert('請填寫聯絡信箱、主旨與內容！');
        return; // 終止執行
    }

    // Email 格式驗證
    if (!emailPattern.test(email)) {
        alert('請輸入正確的電子郵件格式，例如: example@mail.com');
        return; // 終止執行
    }

    // 建立要發送的資料物件 (現在 memberId 若無值將為 null)
    const feedbackData = {
        memberId: memberId,
        email: email,
        subject: subject,
        content: content
    };

    // 4. 發送 AJAX 請求
    fetch('/feedback/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(feedbackData)
    })
        .then(async response => {
            if (response.ok) {
                alert('感謝您的回報，我們將盡快回覆您！');
                window.location.reload(); // 成功後重整頁面
            } else {
                // 嘗試解析後端拋出的錯誤訊息
                const errorMsg = await response.text();
                alert('送出失敗：' + (errorMsg || '請稍後再試。'));
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('系統連線發生錯誤，請稍後再試。');
        });
});