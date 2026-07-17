document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('roomModal');
    const closeBtn = document.querySelector('.close-btn');
    const wrapper = document.getElementById('modalSwiperWrapper');
    let swiper = null;

    // 處理點擊 "SEE MORE"
    document.querySelectorAll('.btn-more').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const roomId = this.getAttribute('data-id');

            // 發送請求取得該房型的所有照片
            fetch(`/roomList/api/photos/${roomId}`)
                .then(res => {
                    if (!res.ok) throw new Error('網路回應錯誤');
                    return res.json();
                })
                .then(photos => {
                    // 清空舊內容
                    wrapper.innerHTML = '';
                    
                    if (photos.length === 0) {
                        wrapper.innerHTML = '<div class="swiper-slide"><p>暫無圖片</p></div>';
                    } else {
                        // 動態生成 HTML
                        photos.forEach(p => {
                            wrapper.innerHTML += `
                                <div class="swiper-slide">
                                    <img src="/roomtypephoto/display/photo/${p.roomTypePhotoId}" alt="房型圖片">
                                </div>`;
                        });
                    }
                    
                    // 顯示模態框
                    modal.style.display = 'flex'; 

                    // 銷毀舊的 Swiper 實例並重新初始化
                    if (swiper) swiper.destroy(true, true);
                    
                    swiper = new Swiper(".mySwiper", {
                        loop: true,
                        navigation: { 
                            nextEl: ".swiper-button-next", 
                            prevEl: ".swiper-button-prev" 
                        },
                        observer: true,       // 監聽容器變化
                        observeParents: true, // 監聽父層變化
                        slidesPerView: 1
                    });
                })
                .catch(err => {
                    console.error("載入照片失敗:", err);
                    alert("目前無法載入圖片，請稍後再試。");
                });
        });
    });

    // 關閉燈箱功能
    const closeModal = () => {
        modal.style.display = 'none';
        if (swiper) {
            swiper.destroy(true, true);
            swiper = null;
        }
    };

    closeBtn.addEventListener('click', closeModal);

    // 點擊背景空白處關閉
    window.addEventListener('click', (event) => {
        if (event.target === modal) {
            closeModal();
        }
    });
});