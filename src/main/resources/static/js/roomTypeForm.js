document.addEventListener("DOMContentLoaded", function() {

    // --- 1. 成功提示訊息自動消失功能 ---
    const successMessageDiv = document.getElementById('successMessageDiv');
    if (successMessageDiv) {
        setTimeout(function() {
            successMessageDiv.style.transition = "opacity 0.5s ease";
            successMessageDiv.style.opacity = "0";
            setTimeout(function() {
                successMessageDiv.style.display = 'none';
            }, 500);
        }, 3000);
    }

    // --- 2. 圖片處理邏輯 ---
    const imageInput = document.getElementById('imageFilesInput');
    const uploadBtn = document.getElementById('uploadBtn');
    const previewContainer = document.getElementById('previewContainer');
    let dataTransfer = new DataTransfer();

    if (uploadBtn && imageInput) {
        uploadBtn.addEventListener('click', function() {
            imageInput.click();
        });
    }

    if (imageInput && previewContainer) {
        imageInput.addEventListener('change', function(event) {
            const files = event.target.files;
            const MAX_SIZE = 5 * 1024 * 1024;

            for (let i = 0;i < files.length;i++) {
                const file = files[i];
                if (!file.type.startsWith('image/')) continue;
                if (file.size > MAX_SIZE) {
                    alert(`檔案 ${file.name} 太大，請選擇 5MB 以下的圖片`);
                    continue;
                }

                // 加入 DataTransfer
                dataTransfer.items.add(file);
                const currentIndex = dataTransfer.items.length - 1; // 記錄當前檔案的索引

                // 建立預覽外層容器
                const wrapper = document.createElement('div');
                wrapper.className = 'preview-item-wrapper';
                wrapper.style.position = 'relative';
                wrapper.style.display = 'inline-block';
                wrapper.style.margin = '5px';

                // 建立圖片
                const img = document.createElement('img');
                img.src = URL.createObjectURL(file); // 使用 createObjectURL 效能更好
                img.className = 'preview-img';
                img.style.display = 'block';

                // 建立刪除按鈕 (X)
                const removeBtn = document.createElement('buttontype'); // 或是用一般 button
                const deleteBtn = document.createElement('button');
                deleteBtn.type = 'button';
                deleteBtn.innerHTML = '&times;'; // 顯示打叉符號
                deleteBtn.className = 'preview-delete-btn';
                // 簡易樣式：讓按鈕顯示在圖片右上角
                deleteBtn.style.cssText = 'position: absolute; top: 2px; right: 2px; background: rgba(0,0,0,0.6); color: white; border: none; border-radius: 50%; width: 22px; height: 22px; cursor: pointer; font-size: 14px; line-height: 20px; text-align: center; padding: 0;';

                // 綁定預覽圖上的刪除事件
                deleteBtn.addEventListener('click', function() {
                    // 1. 從 DataTransfer 中移除對應檔案
                    const dt = new DataTransfer();
                    const currentFiles = dataTransfer.files;
                    for (let j = 0;j < currentFiles.length;j++) {
                        if (j !== currentIndex) {
                            dt.items.add(currentFiles[j]);
                        }
                    }
                    dataTransfer = dt;
                    imageInput.files = dataTransfer.files; // 更新 input 的檔案清單

                    // 2. 從畫面上移除該預覽容器
                    wrapper.remove();
                });

                // 組裝元素
                wrapper.appendChild(img);
                wrapper.appendChild(deleteBtn);
                previewContainer.appendChild(wrapper);
            }

            // 同步更新 input 檔案
            imageInput.files = dataTransfer.files;
        });
    }

    // --- 3. 核心驗證邏輯 ---
    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', function(event) {
            // 這裡保留您原本的庫存邏輯即可
            const amountInput = document.getElementById('amountInput'); // 若您頁面有此 ID
            if (amountInput) {
                const enteredValue = parseInt(amountInput.value);
                const minAllowed = parseInt(amountInput.getAttribute('min')) || 1;
                if (enteredValue < minAllowed) {
                    event.preventDefault();
                    alert(`庫存數量不可小於系統中已建立的房間總數 (${minAllowed} 間)。`);
                    return;
                }
            }
        });
    }

    // --- 4. 刪除圖片按鈕確認 ---
    const deleteButtons = document.querySelectorAll('.delete-btn');
    deleteButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            if (!confirm('刪除後不可復原！確定要刪除這張圖片嗎？')) {
                e.preventDefault();
            }
        });
    });
});