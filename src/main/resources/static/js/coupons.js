const { createApp } = Vue;

createApp({

    data() {
        return {
            coupons: [],
            activeTab: "ALL",
            loading: true,
            errorMessage: ""
        };
    },

    computed: {

        filteredCoupons() {

            if (this.activeTab === "ALL") {
                return this.coupons;
            }

            return this.coupons.filter(coupon =>
                coupon.displayStatus === this.activeTab
            );
        },

        statusCount() {

            const result = {
                AVAILABLE: 0,
                NOT_STARTED: 0,
                USED: 0,
                EXPIRED: 0
            };

            this.coupons.forEach(coupon => {

                if (
                    Object.prototype.hasOwnProperty.call(
                        result,
                        coupon.displayStatus
                    )
                ) {
                    result[coupon.displayStatus]++;
                }
            });

            return result;
        }
    },

    mounted() {
        this.loadCoupons();
    },

    methods: {

        async loadCoupons() {

            this.loading = true;
            this.errorMessage = "";

            try {

                const response = await fetch(
                    "/api/member/coupons",
                    {
                        method: "GET",
                        credentials: "same-origin",
                        headers: {
                            "Accept": "application/json"
                        }
                    }
                );

                if (response.status === 401) {

                    const redirectUrl =
                        encodeURIComponent("/coupons.html");

                    window.location.href =
                        `/login.html?redirect=${redirectUrl}`;

                    return;
                }

                const data = await response.json();

                if (!response.ok) {

                    this.errorMessage =
                        data.error || "優惠券資料讀取失敗";

                    return;
                }

                if (!Array.isArray(data)) {

                    this.errorMessage =
                        "優惠券資料格式錯誤";

                    return;
                }

                this.coupons = data;

            } catch (error) {

                console.error(
                    "讀取優惠券失敗",
                    error
                );

                this.errorMessage =
                    "無法連線到伺服器，請稍後再試";

            } finally {

                this.loading = false;
            }
        },

        discountText(coupon) {

            if (Number(coupon.discountType) === 1) {

                const amount =
                    Number(coupon.discountAmount || 0);

                return `折抵 NT$ ${amount.toLocaleString("zh-TW")}`;
            }

            if (Number(coupon.discountType) === 2) {

                const percent =
                    Number(coupon.discountPercent || 0);

                const discount =
                    percent / 10;

                return `${discount} 折`;
            }

            return "專屬優惠";
        },

		thresholdText(coupon) {

		    if (Number(coupon.discountType) !== 1) {
		        return "";
		    }

		    const discountAmount =
		        Number(coupon.discountAmount || 0);

		    const minimumSpend =
		        discountAmount + 1;

		    return `消費滿 NT$ ${minimumSpend.toLocaleString("zh-TW")} 可使用`;
		},

        statusText(status) {

            const statusMap = {
                AVAILABLE: "可使用",
                NOT_STARTED: "尚未開始",
                USED: "已使用",
                EXPIRED: "已過期"
            };

            return statusMap[status] || "狀態未知";
        },

        statusClass(status) {

            const classMap = {
                AVAILABLE: "available",
                NOT_STARTED: "not-started",
                USED: "used",
                EXPIRED: "expired"
            };

            return classMap[status] || "";
        },

        usageHint(status) {

            const hintMap = {
                AVAILABLE: "請於期限內使用",
                NOT_STARTED: "活動尚未開始",
                USED: "此優惠券已使用",
                EXPIRED: "此優惠券已過期"
            };

            return hintMap[status] || "";
        },

        issuePeriodText(issuePeriod) {

            if (!issuePeriod) {
                return "未設定";
            }

            if (issuePeriod === "ONCE") {
                return "首次發放";
            }

            if (/^\d{4}-\d{2}$/.test(issuePeriod)) {

                const [year, month] =
                    issuePeriod.split("-");

                return `${year} 年 ${Number(month)} 月`;
            }

            return issuePeriod;
        },

        formatDateTime(value) {

            if (!value) {
                return "未設定";
            }

            const date = new Date(value);

            if (Number.isNaN(date.getTime())) {
                return value;
            }

            return new Intl.DateTimeFormat(
                "zh-TW",
                {
                    year: "numeric",
                    month: "2-digit",
                    day: "2-digit",
                    hour: "2-digit",
                    minute: "2-digit",
                    hour12: false
                }
            ).format(date);
        }
    }

}).mount("#app");