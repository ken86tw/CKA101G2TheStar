/**
 * THE STAR Hotel - 房型管理系統互動模組
 */
document.addEventListener('DOMContentLoaded', () => {
    console.log("THE STAR 系統交互已啟動 (v2.0)");

    // 1. 初始化導覽列與標籤狀態
    initNavigation();

    // 2. 優雅的刪除處理
    initDeleteConfirmations();
    
    // 3. 即時圖片預覽
    initImagePreview();
});

// 優化導覽列切換
function initNavigation() {
    const navLinks = document.querySelectorAll('.nav-links a');
    navLinks.forEach(link => {
        link.addEventListener('click', function() {
            navLinks.forEach(l => l.classList.remove('active'));
            this.classList.add('active');
        });
    });
}

// 刪除確認
function initDeleteConfirmations() {
    const deleteButtons = document.querySelectorAll('.btn-icon');
    
    deleteButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            // 檢查是否為刪除按鈕 (包含 trash 圖示的才彈出確認)
            if (this.querySelector('.fa-trash')) {
                if (!confirm("確定刪除此項目嗎？此操作無法復原。")) {
                    e.preventDefault();
                }
            }
        });
    });
}

// 即時圖片預覽：增強了對新增與編輯模式的兼容性
function initImagePreview() {
    const fileInput = document.getElementById('imageInput');
    const previewImg = document.getElementById('previewImg');

    // 確保這兩個元素都存在才執行，避免在其他頁面報錯
    if (fileInput && previewImg) {
        fileInput.addEventListener('change', function() {
            if (this.files && this.files[0]) {
                const reader = new FileReader();
                
                reader.onload = (e) => {
                    // 設定圖片來源並顯示
                    previewImg.src = e.target.result;
                    previewImg.style.display = 'block';
                    
                    // 加入淡入效果
                    previewImg.style.opacity = 0;
                    previewImg.style.transition = "opacity 0.6s ease-in-out";
                    
                    // 用 setTimeout 確保樣式變更生效
                    setTimeout(() => { 
                        previewImg.style.opacity = 1; 
                    }, 50);
                };
                
                reader.readAsDataURL(this.files[0]);
            }
        });
    }
}