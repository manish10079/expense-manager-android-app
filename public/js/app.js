/* ============================================================
   Expense Tracker: Budget & Spend — Main Application JS
   ============================================================ */

'use strict';

document.addEventListener('DOMContentLoaded', () => {

  // ==========================================================
  // 1. Theme Management
  // ==========================================================
  const themeToggle = document.querySelector('.theme-toggle');
  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)');
  const storedTheme = localStorage.getItem('expense-tracker-theme');

  // Set initial theme
  function getInitialTheme() {
    if (storedTheme) return storedTheme;
    return prefersDark.matches ? 'dark' : 'light';
  }

  document.documentElement.setAttribute('data-theme', getInitialTheme());
  updateThemeIcon(getInitialTheme());

  function toggleTheme() {
    const current = document.documentElement.getAttribute('data-theme');
    const next = current === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem('expense-tracker-theme', next);
    updateThemeIcon(next);
  }

  function updateThemeIcon(theme) {
    if (!themeToggle) return;
    themeToggle.setAttribute('aria-label', `Switch to ${theme === 'dark' ? 'light' : 'dark'} mode`);
    themeToggle.innerHTML = theme === 'dark'
      ? '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>'
      : '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="5"/><path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/></svg>';
  }

  if (themeToggle) {
    themeToggle.addEventListener('click', toggleTheme);
  }

  // Listen for system preference changes
  prefersDark.addEventListener('change', (e) => {
    if (!localStorage.getItem('expense-tracker-theme')) {
      const theme = e.matches ? 'dark' : 'light';
      document.documentElement.setAttribute('data-theme', theme);
      updateThemeIcon(theme);
    }
  });

  // ==========================================================
  // 2. Mobile Navigation
  // ==========================================================
  const hamburger = document.querySelector('.hamburger');
  const navLinks = document.querySelector('.nav-links');

  if (hamburger && navLinks) {
    hamburger.addEventListener('click', () => {
      hamburger.classList.toggle('active');
      navLinks.classList.toggle('open');
      const isOpen = navLinks.classList.contains('open');
      hamburger.setAttribute('aria-expanded', isOpen);
    });

    // Close menu on link click
    navLinks.querySelectorAll('.nav-link').forEach(link => {
      link.addEventListener('click', () => {
        hamburger.classList.remove('active');
        navLinks.classList.remove('open');
        hamburger.setAttribute('aria-expanded', 'false');
      });
    });

    // Close on Escape
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && navLinks.classList.contains('open')) {
        hamburger.classList.remove('active');
        navLinks.classList.remove('open');
        hamburger.setAttribute('aria-expanded', 'false');
      }
    });
  }

  // ==========================================================
  // 3. Navbar Scroll Effect
  // ==========================================================
  const nav = document.querySelector('.nav');
  let lastScrollY = 0;

  function handleNavScroll() {
    const scrollY = window.scrollY;

    if (scrollY > 50) {
      nav.classList.add('scrolled');
    } else {
      nav.classList.remove('scrolled');
    }

    lastScrollY = scrollY;
  }

  window.addEventListener('scroll', handleNavScroll, { passive: true });
  // Initial check
  handleNavScroll();

  // ==========================================================
  // 4. FAQ Accordion (with proper aria-expanded)
  // ==========================================================
  const faqItems = document.querySelectorAll('.faq-item');

  faqItems.forEach(item => {
    const question = item.querySelector('.faq-question');
    const answer = item.querySelector('.faq-answer');

    if (!question || !answer) return;

    question.addEventListener('click', () => {
      const isActive = item.classList.contains('active');

      // Close all other FAQs
      faqItems.forEach(other => {
        if (other !== item) {
          other.classList.remove('active');
          const otherAnswer = other.querySelector('.faq-answer');
          if (otherAnswer) {
            otherAnswer.style.maxHeight = '0';
          }
          const otherQ = other.querySelector('.faq-question');
          if (otherQ) {
            otherQ.setAttribute('aria-expanded', 'false');
          }
        }
      });

      // Toggle current
      if (isActive) {
        item.classList.remove('active');
        answer.style.maxHeight = '0';
        question.setAttribute('aria-expanded', 'false');
      } else {
        item.classList.add('active');
        answer.style.maxHeight = answer.scrollHeight + 'px';
        question.setAttribute('aria-expanded', 'true');
      }
    });

    // Make FAQ items keyboard accessible
    question.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        question.click();
      }
    });
  });

  // ==========================================================
  // 5. Animated Counter (supports decimal values)
  // ==========================================================
  function animateCounters() {
    const counters = document.querySelectorAll('.counter-value');

    counters.forEach(counter => {
      const rawTarget = counter.getAttribute('data-target');
      const target = parseFloat(rawTarget.replace(/,/g, ''));
      const suffix = counter.getAttribute('data-suffix') || '';
      const duration = parseInt(counter.getAttribute('data-duration') || '2000', 10);
      const startTime = performance.now();
      const isDecimal = rawTarget.includes('.');

      // If already animated, skip
      if (counter.classList.contains('animated')) return;

      function updateCounter(currentTime) {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);

        // Ease out cubic
        const eased = 1 - Math.pow(1 - progress, 3);

        let displayValue;
        if (isDecimal) {
          displayValue = (eased * target).toFixed(1);
        } else {
          displayValue = Math.floor(eased * target).toLocaleString();
        }

        counter.textContent = displayValue + suffix;

        if (progress < 1) {
          requestAnimationFrame(updateCounter);
        } else {
          counter.textContent = target.toLocaleString(undefined, {
            minimumFractionDigits: isDecimal ? 1 : 0,
            maximumFractionDigits: isDecimal ? 1 : 0
          }) + suffix;
          counter.classList.add('animated');
        }
      }

      requestAnimationFrame(updateCounter);
    });
  }

  // ==========================================================
  // 6. Intersection Observer for Scroll Reveal & Counters
  // ==========================================================
  const revealObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const el = entry.target;

        // Handle counter animation
        if (el.classList.contains('counter-value') && !el.classList.contains('animated')) {
          animateCounters();
        }

        // Handle reveal
        if (el.classList.contains('reveal') ||
            el.classList.contains('reveal-left') ||
            el.classList.contains('reveal-right') ||
            el.classList.contains('reveal-scale')) {
          el.classList.add('visible');
        }

        // Handle feature cards staggered reveal
        if (el.classList.contains('feature-card') || el.classList.contains('step')) {
          el.classList.add('visible');
        }

        if (!el.classList.contains('counter-value')) {
          revealObserver.unobserve(el);
        }
      }
    });
  }, {
    threshold: 0.1,
    rootMargin: '0px 0px -40px 0px'
  });

  // Observe all elements with animation classes
  document.querySelectorAll(
    '.reveal, .reveal-left, .reveal-right, .reveal-scale, ' +
    '.feature-card, .step, .counter-value'
  ).forEach(el => revealObserver.observe(el));

  // ==========================================================
  // 7. Smooth scroll for anchor links (enhanced)
  // ==========================================================
  document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', (e) => {
      const targetId = anchor.getAttribute('href');
      if (targetId === '#') return;

      const target = document.querySelector(targetId);
      if (target) {
        e.preventDefault();
        const navHeight = nav ? nav.offsetHeight : 72;
        const targetPosition = target.getBoundingClientRect().top + window.pageYOffset - navHeight;

        window.scrollTo({
          top: targetPosition,
          behavior: 'smooth'
        });

        // Update URL hash without jump
        history.pushState(null, null, targetId);
      }
    });
  });

  // ==========================================================
  // 8. Premium Plan Toggle
  // ==========================================================
  const planBtns = document.querySelectorAll('.premium-toggle-btn');

  function updatePricing(plan) {
    const prices = document.querySelectorAll('.premium-card-price');

    prices.forEach(price => {
      const monthly = price.getAttribute('data-monthly');
      const semiannual = price.getAttribute('data-semiannual');
      const semiannualTotal = price.getAttribute('data-semiannual-total');
      const semiannualSave = price.getAttribute('data-semiannual-save');
      const yearly = price.getAttribute('data-yearly');
      const yearlyTotal = price.getAttribute('data-yearly-total');
      const yearlySave = price.getAttribute('data-yearly-save');
      const originalPrice = price.getAttribute('data-actual-monthly');
      const original = price.querySelector('.premium-card-original');
      const save = price.querySelector('.premium-card-save');

      if (plan === 'monthly') {
        price.querySelector('.amount').textContent = originalPrice || monthly;
        price.querySelector('.period').textContent = '/month';
        if (original) original.style.display = 'none';
        if (save) save.style.display = 'none';
      } else if (plan === 'semiannual') {
        price.querySelector('.amount').textContent = semiannual || monthly;
        price.querySelector('.period').textContent = '/mo';
        if (original) {
          original.style.display = 'block';
          original.textContent = `${semiannualTotal || '₹474'} for 6 months`;
        }
        if (save) {
          save.style.display = 'inline-block';
          save.textContent = `Save ${semiannualSave || '₹120'} (20% off)`;
        }
      } else if (plan === 'yearly') {
        price.querySelector('.amount').textContent = yearly || monthly;
        price.querySelector('.period').textContent = '/mo';
        if (original) {
          original.style.display = 'block';
          original.textContent = `${yearlyTotal || '₹708'} for 12 months`;
        }
        if (save) {
          save.style.display = 'inline-block';
          save.textContent = `Save ${yearlySave || '₹480'} (40% off)`;
        }
      }
    });
  }

  planBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      planBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      updatePricing(btn.getAttribute('data-plan'));
    });
  });

  // ==========================================================
  // 9. Ripple effect on buttons
  // ==========================================================
  document.querySelectorAll('.btn').forEach(btn => {
    btn.addEventListener('click', function(e) {
      const rect = this.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;

      const ripple = document.createElement('span');
      ripple.className = 'btn-ripple';
      ripple.style.left = x + 'px';
      ripple.style.top = y + 'px';
      ripple.style.width = '20px';
      ripple.style.height = '20px';
      ripple.style.marginLeft = '-10px';
      ripple.style.marginTop = '-10px';

      this.appendChild(ripple);

      ripple.addEventListener('animationend', () => {
        ripple.remove();
      });
    });
  });

  // ==========================================================
  // 10. Lazy image loading (blur-up)
  // ==========================================================
  const lazyImages = document.querySelectorAll('.lazy-image');

  const imageObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const img = entry.target;
        if (img.dataset.src) {
          const tempImg = new Image();
          tempImg.onload = () => {
            img.src = img.dataset.src;
            img.classList.add('loaded');
          };
          tempImg.src = img.dataset.src;
        }
        imageObserver.unobserve(img);
      }
    });
  }, { rootMargin: '200px' });

  lazyImages.forEach(img => imageObserver.observe(img));

  // ==========================================================
  // 11. Active section tracking for nav links
  // ==========================================================
  const sections = document.querySelectorAll('section[id]');
  const navLinkEls = document.querySelectorAll('.nav-link');

  function updateActiveSection() {
    let currentSection = '';
    const navHeight = nav ? nav.offsetHeight : 72;

    sections.forEach(section => {
      const sectionTop = section.offsetTop - navHeight - 100;
      if (window.scrollY >= sectionTop) {
        currentSection = section.getAttribute('id');
      }
    });

    navLinkEls.forEach(link => {
      link.classList.remove('active');
      if (link.getAttribute('href') === '#' + currentSection) {
        link.classList.add('active');
      }
    });
  }

  // Add active style for nav
  const style = document.createElement('style');
  style.textContent = `
    .nav-link.active {
      color: var(--color-primary);
    }
    .nav-link.active::after {
      transform: scaleX(1);
    }
  `;
  document.head.appendChild(style);

  window.addEventListener('scroll', updateActiveSection, { passive: true });
  updateActiveSection();

  // ==========================================================
  // 12. Parallax effect on hero
  // ==========================================================
  const heroSection = document.querySelector('.hero');
  let ticking = false;

  function handleParallax() {
    if (!heroSection) return;

    const scrollY = window.scrollY;
    const heroHeight = heroSection.offsetHeight;

    // Only apply when hero is visible
    if (scrollY < heroHeight) {
      const parallaxElements = heroSection.querySelectorAll('.parallax');
      parallaxElements.forEach(el => {
        const speed = parseFloat(el.getAttribute('data-speed') || '0.3');
        el.style.transform = `translateY(${scrollY * speed}px)`;
      });
    }
  }

  window.addEventListener('scroll', () => {
    if (!ticking) {
      window.requestAnimationFrame(() => {
        handleParallax();
        ticking = false;
      });
      ticking = true;
    }
  }, { passive: true });

  // ==========================================================
  // 13. Keyboard accessibility
  // ==========================================================
  // Make interactive elements focusable where needed
  document.querySelectorAll('.feature-card, .why-card').forEach(card => {
    if (!card.getAttribute('tabindex')) {
      card.setAttribute('tabindex', '0');
    }
  });

  // ==========================================================
  // 14. Lightbox Gallery
  // ==========================================================
  function openLightbox(element) {
    const img = element.querySelector('img');
    if (!img) return;

    const lightbox = document.getElementById('lightbox');
    const lightboxImg = document.getElementById('lightbox-image');

    if (lightbox && lightboxImg) {
      lightboxImg.src = img.src;
      lightboxImg.alt = img.alt;
      lightbox.classList.add('open');
      document.body.style.overflow = 'hidden';
    }
  }

  function closeLightbox() {
    const lightbox = document.getElementById('lightbox');
    if (lightbox) {
      lightbox.classList.remove('open');
      document.body.style.overflow = '';
    }
  }

  // Close lightbox on Escape key
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      closeLightbox();
    }
  });

  // Expose functions globally for inline onclick usage
  window.openLightbox = openLightbox;
  window.closeLightbox = closeLightbox;

  console.log('🚀 Expense Tracker: Budget & Spend — Landing Page loaded');
  console.log('📱 Premium by Design, Private by Nature');
});
