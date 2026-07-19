/* ============================================================
   roombooking/orders.js — 「我的預訂」(會員訂單列表)
   內容:訂單列表分頁查詢、模擬付款、取消訂單
   (明細展開 toggleDetail 和統計 refreshCounts 跟後台共用,放 common.js)
   對應畫面:templates/roombooking/orders.html
   ============================================================ */
window.RB = window.RB || {};

RB.orders = {
    methods: {
        async loadOrders() {
            if (!this.member.on) return;
            try {
                const r = await this.api(`/thestar/order/member?orderStatus=${this.orders.status}&page=${this.orders.page}&size=${this.orders.size}`);
                this.orders.list = r.content || [];
                this.orders.totalPages = r.totalPages || 0;
                this.orders.open = {};
                this.refreshCounts(this.orders, '/thestar/order/member');
            } catch {
                this.orders.list = [];
            }
        },
        async devConfirm(orderId) {
            if (!confirm(`模擬付款（不經綠界，直接標記 No.${orderId} 已付款）？`)) return;
            try {
                await this.api(`/dev/confirm/${orderId}`);
                this.toast('ok', '模擬付款成功', `No.${orderId} → 已付款`);
                this.loadOrders();
            } catch (e) {
                this.toast('err', '模擬付款失敗', this.errMsg(e));
            }
        },
        async cancel(orderId) {
            const reason = prompt('取消原因？', '測試取消');
            if (reason === null) return;
            try {
                await this.api(`/thestar/order/cancel/${orderId}`, {
                    method: 'POST',
                    headers: {'Content-Type': 'text/plain'},
                    body: reason
                });
                this.toast('ok', '已取消', `No.${orderId}`);
                this.loadOrders();
            } catch (e) {
                this.toast('err', '取消失敗', this.errMsg(e));
            }
        },
    },
};
