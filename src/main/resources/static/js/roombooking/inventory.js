/* ============================================================
   roombooking/inventory.js — 「庫存查詢」(員工)
   內容:31 天庫存查詢 + 熱度表的顯示小工具
   對應畫面:templates/roombooking/inventory.html
   ============================================================ */
window.RB = window.RB || {};

RB.inventory = {
    computed: {
        // 表格的欄標題:從 155 筆資料收集「不重複的房型名稱」
        // 例:['豪華雙人房', '家庭房', '總統套房']
        invTypes() {
            const names = [];
            for (const r of this.inv.list) {
                // includes = 問陣列「你裡面有這個值嗎」,沒有才 push,達成去重
                if (!names.includes(r.roomTypeName)) names.push(r.roomTypeName);
            }
            return names;
        },
        // 把平的 155 筆轉成「一天一列」給表格用:
        // [{ date: '2026-07-17', cells: [{remain: 7, total: 10}, ...] }, ...]
        // 思路跟後端 findAdminRoom 的 roomMap 一模一樣:先分組建索引,再照順序組出結果
        invRows() {
            // 第一步:建「日期 -> (房型名稱 -> 該筆資料)」的兩層索引
            // JS 用普通物件 {} 就能當 Map:obj[key] = value 存、obj[key] 取
            const byDate = {};
            for (const r of this.inv.list) {
                if (!byDate[r.date]) byDate[r.date] = {};   // 等同 Java 的 computeIfAbsent
                byDate[r.date][r.roomTypeName] = r;
            }
            // 第二步:日期排序後,每天照 invTypes 的欄位順序排出 cells
            // (順序對齊很重要,不然數字會填到別的房型的欄位下面)
            return Object.keys(byDate).sort().map(date => ({
                date,
                cells: this.invTypes.map(name => {
                    const r = byDate[date][name];
                    // 後端每天每房型都會給一筆,理論上不會缺;防禦性給個 '-' 以防萬一
                    return r ? {remain: r.remainAmount, total: r.totalAmount}
                        : {remain: '-', total: '-'};
                })
            }));
        },
    },
    methods: {
        // 員工庫存查詢:打自己寫的後端 API,拿「每房型 × 31 天」的平面清單
        async loadInventory() {
            try {
                // 有選日期就帶 ?date=,沒選就不帶 → 後端 required=false 收到 null → 自動查今天起
                const url = this.inv.date
                    ? `/find/admin/room?date=${this.inv.date}`
                    : '/find/admin/room';
                this.inv.list = await this.api(url);
            } catch (e) {
                this.inv.list = [];
                this.toast('err', '庫存查詢失敗', this.errMsg(e));
            }
        },

        // ===== 庫存表格的顯示小工具(只負責「算給畫面看」,不打 API)=====

        // 依剩餘量決定格子顏色,回傳的字串會變成 td 的 class(對應 CSS 的三種水色)
        // 滿房 → 'full'(紅) / 有預訂(水位下降)→ 'low'(金) / 完全沒訂(滿水位)→ 'ok'(綠)
        invLevel(c) {
            if (typeof c.remain !== 'number') return '';   // 防禦:資料缺漏('-')時不上色
            if (c.remain === 0) return 'full';
            return c.remain < c.total ? 'low' : 'ok';
        },
        // 水位 = 剩餘比例,例:剩 7/10 → '70%',跟「剩 X 間」的字直接對得上
        invPct(c) {
            if (typeof c.remain !== 'number' || !c.total) return '0%';
            return Math.round(c.remain / c.total * 100) + '%';
        },
        // '2026-07-17' → '7/17':整欄都是同一年,年份是雜訊,拿掉比較好掃
        invMD(d) {
            const [, m, day] = d.split('-');   // 解構陣列,第 0 格(年)用空位跳過
            return `${+m}/${+day}`;            // 字串前加 + 轉成數字,順便把 '07' 的 0 去掉
        },
        // '2026-07-17' → '週五'。getDay() 回 0~6(0=週日),拿去查字串的第幾個字
        invWD(d) {
            return '週' + '日一二三四五六'[new Date(d).getDay()];
        },
        // 週末的日期字改金色,掃視 31 列時好抓住週節奏
        invIsWeekend(d) {
            const w = new Date(d).getDay();
            return w === 0 || w === 6;
        },
        // 是不是今天?用本地時區自己組 'YYYY-MM-DD' 來比
        // (不用 toISOString() 是因為它回 UTC,台灣早上八點前會差一天)
        invIsToday(d) {
            const t = new Date();
            const local = `${t.getFullYear()}-${String(t.getMonth() + 1).padStart(2, '0')}-${String(t.getDate()).padStart(2, '0')}`;
            return d === local;
        },
    },
};
