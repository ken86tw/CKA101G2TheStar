(function () {
  const storyCards = Array.from(document.querySelectorAll('.story-card'));
  const yearButtons = Array.from(document.querySelectorAll('.year-button'));
  const currentStoryNumber = document.getElementById('currentStoryNumber');
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  function setActiveStory(index) {
    const safeIndex = Math.max(0, Math.min(index, storyCards.length - 1));

    storyCards.forEach(function (card, cardIndex) {
      card.classList.toggle('active', cardIndex === safeIndex);
    });

    yearButtons.forEach(function (button, buttonIndex) {
      const active = buttonIndex === safeIndex;
      button.classList.toggle('active', active);

      if (active) {
        button.setAttribute('aria-current', 'step');
      } else {
        button.removeAttribute('aria-current');
      }
    });

    if (currentStoryNumber) {
      currentStoryNumber.textContent = String(safeIndex + 1).padStart(2, '0');
    }


    if (window.innerWidth <= 820 && yearButtons[safeIndex]) {
      yearButtons[safeIndex].scrollIntoView({
        behavior: reduceMotion ? 'auto' : 'smooth',
        block: 'nearest',
        inline: 'center'
      });
    }
  }

  if (storyCards.length > 0) {
    const storyObserver = new IntersectionObserver(function (entries) {
      const visibleEntries = entries
        .filter(function (entry) {
          return entry.isIntersecting;
        })
        .sort(function (a, b) {
          return b.intersectionRatio - a.intersectionRatio;
        });

      if (visibleEntries.length === 0) {
        return;
      }

      const index = Number(visibleEntries[0].target.dataset.storyIndex);
      setActiveStory(index);
    }, {
      root: null,
      rootMargin: '-24% 0px -24% 0px',
      threshold: [0.15, 0.35, 0.55, 0.75]
    });

    storyCards.forEach(function (card) {
      storyObserver.observe(card);
    });
  }

  yearButtons.forEach(function (button) {
    button.addEventListener('click', function () {
      const index = Number(button.dataset.storyIndex);
      const target = storyCards[index];

      if (!target) {
        return;
      }

      target.scrollIntoView({
        behavior: reduceMotion ? 'auto' : 'smooth',
        block: 'center'
      });
    });
  });

  const revealElements = document.querySelectorAll('.reveal');

  if (reduceMotion) {
    revealElements.forEach(function (element) {
      element.classList.add('visible');
    });
  } else {
    const revealObserver = new IntersectionObserver(function (entries, observer) {
      entries.forEach(function (entry) {
        if (!entry.isIntersecting) {
          return;
        }

        entry.target.classList.add('visible');
        observer.unobserve(entry.target);
      });
    }, {
      threshold: 0.14,
      rootMargin: '0px 0px -8% 0px'
    });

    revealElements.forEach(function (element) {
      revealObserver.observe(element);
    });
  }

  window.addEventListener('resize', function () {
    const activeIndex = yearButtons.findIndex(function (button) {
      return button.classList.contains('active');
    });

    setActiveStory(activeIndex < 0 ? 0 : activeIndex);
  });
})();
