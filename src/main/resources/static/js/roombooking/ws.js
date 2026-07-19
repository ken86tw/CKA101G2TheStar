/* ============================================================
   roombooking/ws.js — WebSocket(STOMP)即時通知
   內容:員工訂閱 rooms/orders/refunds 頻道、
        會員訂閱自己專屬頻道(退款完成/退房完成/逾時取消)
   ============================================================ */
window.RB = window.RB || {};

RB.ws = {
    methods: {
        connectWs() {
            if (this._stomp) return;
            const proto = location.protocol === 'https:' ? 'wss' : 'ws';
            const client = new StompJs.Client({
                brokerURL: `${proto}://${location.host}/ws`,
                reconnectDelay: 5000,
            });
            client.onConnect = () => {
                this.log('✔ WS', '已連線');
                if (this.employee.on) {
                    client.subscribe('/topic/rooms', (msg) => {
                        const data = JSON.parse(msg.body);
                        this.log('WS rooms', data);
                        const room = this.stay.rooms.find(r => r.roomId ===
                            data.roomId);
                        if (room) room.roomStatus = data.roomStatus;
                    });
                    client.subscribe('/topic/refunds', () => {
                        this.log('WS refunds', '清單變動');
                        //取消訂單或退款完成都會敲這口鐘 人在哪個分頁就重查哪邊
                        if (this.nav === 'refund') this.loadRefunds();
                        //後台訂單的列表跟上面統計數字也會受影響 一起重查
                        if (this.nav === 'admin') this.loadAdmin();
                    });
                    client.subscribe('/topic/orders', () => {
                        this.log('WS orders', '訂單變動');
                        if (this.nav === 'admin') this.loadAdmin();
                    });
                }
                if (this.member.on) {
                    // 名字裡的ID是自己的 所以只會收到自己的通知
                    client.subscribe(`/topic/member/${this.member.on}`, (msg) => {
                        const data = JSON.parse(msg.body);
                        //同一個頻道兩種事件 看event決定跳哪句
                        if (data.event === 'refunded') this.toast('ok', '退款完成', '您的訂單狀態已更新');
                        if (data.event === 'completed') this.toast('ok', '退房完成', '感謝您的入住');
                        if (data.event === 'canceled') {
                            // 收到取消通知先不動作 推遲一秒再處理
                            // setTimeout(要做的事, 毫秒數) 一秒後才執行大括號裡的內容
                            setTimeout(() => {
                                this.toast('ok', '訂單取消', '由於您逾時未付款訂單已取消');
                                // 若客人還停在確認頁 把頁面收掉 避免對著已取消的單按付款
                                this.confirmOrder = null;
                                this.loadOrders();
                            }, 1000);
                            // return 讓下面那行 loadOrders 不要立刻跑 統一等一秒後那次
                            return;
                        }
                        this.loadOrders();
                    });
                }
            };
            client.activate();
            this._stomp = client;
        },
    },
};
