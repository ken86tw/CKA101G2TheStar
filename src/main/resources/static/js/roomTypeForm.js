document.addEventListener("DOMContentLoaded", function() {
    
    // --- 1. 初始化 DOM 元素 ---
    const imageInput = document.getElementById('imageFilesInput'); 
    const uploadBtn = document.getElementById('uploadBtn');
    const previewContainer = document.getElementById('previewContainer');
    
    // 建立一個 DataTransfer 物件來存放「累積」的所有檔案
    let dataTransfer = new DataTransfer();

    // 綁定按鈕觸發 input
    if (uploadBtn && imageInput) {
        uploadBtn.addEventListener('click', function() {
            imageInput.click();
        });
    }

    if (imageInput && previewContainer) {
        imageInput.addEventListener('change', function(event) {
            // 修正：移除 previewContainer.innerHTML = ''; 這樣舊預覽才不會消失
            const files = event.target.files;
            
            // 限制檔案大小 (例如 5MB)
            const MAX_SIZE = 5 * 1024 * 1024; 
            
            for (let i = 0; i < files.length; i++) {
                const file = files[i];
                
                // 檢查檔案類型
                if (!file.type.startsWith('image/')) continue;

                // 檢查大小
                if (file.size > MAX_SIZE) {
                    alert(`檔案 ${file.name} 太大，請選擇 5MB 以下的圖片`);
                    continue;
                }

                // 【累加關鍵】將新選擇的檔案加入到 dataTransfer 清單中
                dataTransfer.items.add(file);

                const reader = new FileReader();
                reader.onload = function(e) {
                    const img = document.createElement('img');
                    img.src = e.target.result;
                    img.className = 'preview-img'; 
                    previewContainer.appendChild(img);
                };
                reader.readAsDataURL(file);
            }
            
            // 【累加關鍵】將累積後的完整檔案清單塞回 input 元素
            // 這樣表單送出時，Controller 才能拿到累加後的所有圖片
            imageInput.files = dataTransfer.files;
        });
    }

    // --- 2. 表單驗證 ---
    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', function(event) {
            const amountInput = document.getElementById('amountInput');
            if (amountInput && (parseInt(amountInput.value) < 1 || parseInt(amountInput.value) > 99)) {
                alert('庫存數量必須在 1 到 99 之間');
                event.preventDefault(); 
            }
        });
    }

    // --- 3. 刪除圖片按鈕確認 ---
    const deleteButtons = document.querySelectorAll('.delete-btn');
    deleteButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            if (!confirm('確定要刪除這張圖片嗎？')) {
                e.preventDefault();
            }
        });
    });
});