(function () {
  'use strict';

  const state = { page: 0, totalPages: 0, articleId: null };
  const el = (id) => document.getElementById(id);

  function formatDate(value) {
    if (!value) return '';
    return new Intl.DateTimeFormat('zh-TW', { year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date(value));
  }

  function card(article) {
    const node = document.createElement('article');
    node.className = 'article-card';
    const cover = document.createElement('div');
    cover.className = 'card-cover';
    if (article.coverImageUrl) {
      const image = document.createElement('img');
      image.src = article.coverImageUrl;
      image.alt = article.title + '封面';
      image.loading = 'lazy';
      cover.appendChild(image);
    }
    const body = document.createElement('div');
    body.className = 'card-body';
    const meta = document.createElement('div');
    meta.className = 'card-meta';
    meta.textContent = [article.category, formatDate(article.createAt), (article.viewCount || 0) + ' 次瀏覽'].join(' · ');
    const title = document.createElement('h3');
    title.textContent = article.title;
    const preview = document.createElement('p');
    preview.textContent = article.contentPreview || '';
    const link = document.createElement('a');
    link.href = '?article=' + article.articleId;
    link.className = 'read-more';
    link.textContent = '閱讀全文';
    body.append(meta, title, preview, link);
    node.append(cover, body);
    return node;
  }

  async function loadArticles(page) {
    el('articleMessage').textContent = '文章載入中...';
    el('articleGrid').replaceChildren();
    try {
      const response = await fetch('/thestar/content/articles?page=' + page + '&size=6');
      if (!response.ok) throw new Error();
      const data = await response.json();
      state.page = data.number;
      state.totalPages = data.totalPages;
      (data.content || []).forEach((article) => el('articleGrid').appendChild(card(article)));
      el('articleMessage').textContent = data.content && data.content.length ? '' : '目前還沒有已發布的文章。';
      el('pageInfo').textContent = state.totalPages ? (state.page + 1) + ' / ' + state.totalPages : '';
      el('prevPage').disabled = state.page <= 0;
      el('nextPage').disabled = state.page + 1 >= state.totalPages;
    } catch (error) {
      el('articleMessage').textContent = '文章暫時無法載入，請稍後再試。';
    }
  }

  async function loadDetail(id) {
    el('listView').hidden = true;
    el('detailView').hidden = false;
    try {
      const response = await fetch('/thestar/content/articles/' + encodeURIComponent(id));
      if (!response.ok) throw new Error();
      const article = await response.json();
      state.articleId = article.articleId;
      document.title = article.title + '｜東方之星';
      el('detailCategory').textContent = article.category;
      el('detailDate').textContent = formatDate(article.createAt);
      el('detailViews').textContent = (article.viewCount || 0) + ' 次瀏覽';
      el('detailTitle').textContent = article.title;
      el('detailContent').textContent = article.content || '';
      if (article.coverImageUrl) {
        el('detailCover').hidden = false;
        el('detailCoverImage').src = article.coverImageUrl;
        el('detailCoverImage').alt = article.title + '封面';
      }
      await loadReviews(article.articleId);
    } catch (error) {
      el('detailTitle').textContent = '找不到這篇文章';
      el('detailContent').textContent = '文章可能已下架或不存在。';
      el('reviewForm').hidden = true;
    }
  }

  async function loadReviews(id) {
    const response = await fetch('/thestar/content/articles/' + id + '/reviews');
    if (!response.ok) return;
    const reviews = await response.json();
    el('reviewCount').textContent = reviews.length + ' 則';
    el('reviewList').replaceChildren();
    if (!reviews.length) {
      const empty = document.createElement('p');
      empty.className = 'state-message';
      empty.textContent = '還沒有留言，歡迎成為第一位分享者。';
      el('reviewList').appendChild(empty);
      return;
    }
    reviews.forEach((review) => {
      const item = document.createElement('article');
      item.className = 'review-item';
      const header = document.createElement('header');
      const member = document.createElement('span');
      member.textContent = '會員 #' + review.memberId;
      const date = document.createElement('time');
      date.textContent = formatDate(review.createdAt);
      const content = document.createElement('p');
      content.textContent = review.content;
      header.append(member, date);
      item.append(header, content);
      el('reviewList').appendChild(item);
    });
  }

  el('reviewForm').addEventListener('submit', async function (event) {
    event.preventDefault();
    el('reviewFormMessage').textContent = '送出中...';
    const content = el('reviewContent').value.trim();
    try {
      const response = await fetch('/thestar/content/articles/' + state.articleId + '/reviews', {
        method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ content: content })
      });
      if (response.status === 401) {
        el('reviewFormMessage').textContent = '請先登入會員再留言。';
        return;
      }
      if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.error || '留言送出失敗');
      }
      el('reviewContent').value = '';
      el('reviewFormMessage').textContent = '留言已送出';
      await loadReviews(state.articleId);
    } catch (error) {
      el('reviewFormMessage').textContent = error.message;
    }
  });

  el('prevPage').addEventListener('click', () => loadArticles(state.page - 1));
  el('nextPage').addEventListener('click', () => loadArticles(state.page + 1));
  el('backToList').addEventListener('click', () => { window.location.href = '/articles.html'; });

  const requestedId = new URLSearchParams(location.search).get('article');
  if (requestedId && /^\d+$/.test(requestedId)) loadDetail(requestedId); else loadArticles(0);
}());
