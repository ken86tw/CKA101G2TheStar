const { createApp } = Vue;

createApp({
  data() {
    return {
      loading: true,
      loadError: '',
      submitting: false,
      errorMsg: '',
      pending: {
        mode: '',
        memberEmail: '',
        memberName: ''
      },
      form: {
        memberName: '',
        memberPhone: '',
        memberAddress: '',
        memberBirthday: '',
        memberGender: 2,
        existingPassword: ''
      }
    };
  },
  computed: {
    isLinkMode() {
      return this.pending.mode === 'LINK_EXISTING';
    }
  },
  mounted() {
    this.loadPending();
  },
  methods: {
    async loadPending() {
      try {
        const res = await fetch('/api/member/google/pending', { credentials: 'same-origin' });
        const data = await res.json().catch(() => ({}));
        if (!res.ok) {
          this.loadError = data.error || 'Google 登入資料不存在，請重新登入';
          return;
        }
        this.pending = data;
        this.form.memberName = data.memberName || '';
      } catch (e) {
        this.loadError = '無法連線到伺服器';
      } finally {
        this.loading = false;
      }
    },

    async submitProfile() {
      this.errorMsg = '';
      this.submitting = true;
      try {
        const res = await fetch('/api/member/google/complete', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'same-origin',
          body: JSON.stringify(this.form)
        });
        const data = await res.json().catch(() => ({}));
        if (!res.ok) {
          this.errorMsg = data.error || 'Google 會員資料處理失敗';
          return;
        }
        location.href = data.redirect || '/index.html';
      } catch (e) {
        this.errorMsg = '無法連線到伺服器';
      } finally {
        this.submitting = false;
      }
    }
  }
}).mount('#app');
