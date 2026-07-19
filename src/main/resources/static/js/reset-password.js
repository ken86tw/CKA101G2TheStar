const { createApp } = Vue;

createApp({
  data() {
    return {
      token: '',
      checking: true,
      tokenValid: false,
      resetCompleted: false,
      submitting: false,
      errorMsg: '',
      successMsg: '',
      form: {
        newPassword: '',
        confirmPassword: ''
      }
    };
  },
  mounted() {
    this.token = new URLSearchParams(location.search).get('token') || '';
    this.validateToken();
  },
  methods: {
    async validateToken() {
      if (!this.token) {
        this.checking = false;
        return;
      }
      try {
        const res = await fetch('/api/member/reset-password/validate?token=' + encodeURIComponent(this.token), {
          credentials: 'same-origin'
        });
        const data = await res.json().catch(() => ({}));
        this.tokenValid = res.ok && Boolean(data.valid);
      } catch (e) {
        this.tokenValid = false;
      } finally {
        this.checking = false;
      }
    },

    async submitReset() {
      this.errorMsg = '';
      this.successMsg = '';
      if (this.form.newPassword.length < 6) {
        this.errorMsg = '會員密碼至少需要 6 個字元';
        return;
      }
      if (this.form.newPassword !== this.form.confirmPassword) {
        this.errorMsg = '兩次輸入的密碼不一致';
        return;
      }

      this.submitting = true;
      try {
        const res = await fetch('/api/member/reset-password', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'same-origin',
          body: JSON.stringify({
            token: this.token,
            newPassword: this.form.newPassword,
            confirmPassword: this.form.confirmPassword
          })
        });
        const data = await res.json().catch(() => ({}));
        if (!res.ok) {
          this.errorMsg = data.error || '密碼重設失敗';
          return;
        }
        this.successMsg = data.message || '密碼重設成功';
        this.resetCompleted = true;
        this.form.newPassword = '';
        this.form.confirmPassword = '';
        setTimeout(() => {
          location.href = '/login.html';
        }, 1200);
      } catch (e) {
        this.errorMsg = '無法連線到伺服器';
      } finally {
        this.submitting = false;
      }
    }
  }
}).mount('#app');
