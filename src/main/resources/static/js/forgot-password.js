const { createApp } = Vue;

createApp({
  data() {
    return {
      memberEmail: '',
      submitting: false,
      errorMsg: '',
      successMsg: ''
    };
  },
  methods: {
    async submitRequest() {
      this.errorMsg = '';
      this.successMsg = '';
      if (!this.memberEmail) {
        this.errorMsg = '請輸入會員信箱';
        return;
      }

      this.submitting = true;
      try {
        const res = await fetch('/api/member/forgot-password', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'same-origin',
          body: JSON.stringify({ memberEmail: this.memberEmail })
        });
        const data = await res.json().catch(() => ({}));
        if (!res.ok) {
          this.errorMsg = data.error || '申請失敗，請稍後再試';
          return;
        }
        this.successMsg = data.message || '若此信箱已註冊，我們將寄送密碼重設信';
      } catch (e) {
        this.errorMsg = '無法連線到伺服器';
      } finally {
        this.submitting = false;
      }
    }
  }
}).mount('#app');
