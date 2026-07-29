/* ============================================================
   Expense Tracker: Budget & Spend — Animations JS
   ============================================================ */

'use strict';

document.addEventListener('DOMContentLoaded', () => {

  // ==========================================================
  // 1. Hero Gradient Mouse Parallax
  // ==========================================================
  const hero = document.querySelector('.hero');
  const heroGlow = document.querySelector('.hero-glow');

  if (hero && heroGlow) {
    hero.addEventListener('mousemove', (e) => {
      const rect = hero.getBoundingClientRect();
      const x = (e.clientX - rect.left) / rect.width - 0.5;
      const y = (e.clientY - rect.top) / rect.height - 0.5;

      heroGlow.style.transform = `translateX(calc(-50% + ${x * 30}px)) translateY(${y * 30}px)`;
    });

    hero.addEventListener('mouseleave', () => {
      heroGlow.style.transform = 'translateX(-50%) translateY(0)';
      heroGlow.style.transition = 'transform 1s ease';
    });

    hero.addEventListener('mouseenter', () => {
      heroGlow.style.transition = 'none';
    });
  }

  // ==========================================================
  // 2. Cursor Glow Spot (follows mouse smoothly)
  // ==========================================================
  const glowSpot = document.querySelector('.hero-glow-spot');
  let glowX = 0;
  let glowY = 0;
  let currentGlowX = 0;
  let currentGlowY = 0;
  let glowTicking = false;

  // Only enable on devices with actual hover (not touch)
  if (hero && glowSpot && window.matchMedia('(hover: hover)').matches) {
    hero.addEventListener('mousemove', (e) => {
      const rect = hero.getBoundingClientRect();
      glowX = e.clientX - rect.left;
      glowY = e.clientY - rect.top;

      if (!glowTicking) {
        requestAnimationFrame(() => {
          currentGlowX += (glowX - currentGlowX) * 0.12;
          currentGlowY += (glowY - currentGlowY) * 0.12;
          glowSpot.style.transform = `translate(${currentGlowX - 250}px, ${currentGlowY - 250}px)`;
          glowTicking = false;
        });
        glowTicking = true;
      }
    });

    hero.addEventListener('mouseleave', () => {
      glowSpot.style.opacity = '0';
    });

    hero.addEventListener('mouseenter', () => {
      glowSpot.style.opacity = '1';
    });
  }

  // ==========================================================
  // 3. Magnetic Float Effect (cards subtly shift toward cursor)
  // ==========================================================
  const magFloats = document.querySelectorAll('.hero-float');
  let magTicking = false;

  // Only enable on devices with actual hover (not touch)
  if (hero && magFloats.length > 0 && window.matchMedia('(hover: hover)').matches) {
    hero.addEventListener('mousemove', (e) => {
      if (!magTicking) {
        requestAnimationFrame(() => {
          const rect = hero.getBoundingClientRect();
          const mouseX = (e.clientX - rect.left) / rect.width;
          const mouseY = (e.clientY - rect.top) / rect.height;

          magFloats.forEach(el => {
            const rectEl = el.getBoundingClientRect();
            const elCenterX = rectEl.left - rect.left + rectEl.width / 2;
            const elCenterY = rectEl.top - rect.top + rectEl.height / 2;

            const offsetX = (mouseX * rect.width - elCenterX) * 0.008;
            const offsetY = (mouseY * rect.height - elCenterY) * 0.008;

            const maxOffset = 6;
            const clampedX = Math.max(-maxOffset, Math.min(maxOffset, offsetX));
            const clampedY = Math.max(-maxOffset, Math.min(maxOffset, offsetY));

            el.style.transform = `translate(${clampedX}px, ${clampedY}px)`;
          });
          magTicking = false;
        });
        magTicking = true;
      }
    });

    hero.addEventListener('mouseleave', () => {
      magFloats.forEach(el => {
        el.style.transform = '';
        el.style.transition = 'transform 0.4s ease';
      });
    });

    hero.addEventListener('mouseenter', () => {
      magFloats.forEach(el => {
        el.style.transition = 'none';
      });
    });
  }

  // ==========================================================
  // 4. Subtle Floating Elements on Scroll (opacity only — no transform, to avoid conflict with magnetic)
  // ==========================================================
  const scrollFloats = document.querySelectorAll('.hero-float, .floating-card');
  let floatTicking = false;

  function updateFloatOpacity() {
    const scrollY = window.scrollY;
    scrollFloats.forEach(el => {
      if (scrollY > 100) {
        const opacity = Math.max(0, 1 - (scrollY - 100) / 400);
        el.style.opacity = opacity;
      } else {
        el.style.opacity = 1;
      }
    });
  }

  window.addEventListener('scroll', () => {
    if (!floatTicking) {
      window.requestAnimationFrame(() => {
        updateFloatOpacity();
        floatTicking = false;
      });
      floatTicking = true;
    }
  }, { passive: true });

  // ==========================================================
  // 5. Dynamic background grid lines animation
  // ==========================================================
  const gridBg = document.querySelector('.hero-grid-bg');
  if (gridBg) {
    let gridOpacity = 1;

    window.addEventListener('scroll', () => {
      const scrollY = window.scrollY;
      if (scrollY < window.innerHeight) {
        gridOpacity = Math.max(0.2, 1 - scrollY / (window.innerHeight * 0.8));
        gridBg.style.opacity = gridOpacity;
      }
    }, { passive: true });
  }

  // ==========================================================
  // 6. Phone Mockup Screen Transition
  // ==========================================================
  // This complements the carousel.js auto-slide with entrance animation
  const phoneMockup = document.querySelector('.phone-mockup');

  if (phoneMockup) {
    // Entrance animation on scroll
    const phoneObserver = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.style.opacity = '0';
          entry.target.style.transform = 'translateY(40px) scale(0.95)';

          requestAnimationFrame(() => {
            entry.target.style.transition = 'all 1s cubic-bezier(0.34, 1.56, 0.64, 1)';
            entry.target.style.opacity = '1';
            entry.target.style.transform = 'translateY(0) scale(1)';
          });

          phoneObserver.unobserve(entry.target);
        }
      });
    }, { threshold: 0.15 });

    phoneObserver.observe(phoneMockup);
  }

  // ==========================================================
  // 7. Gradient text shimmer effect on hero heading
  // ==========================================================
  const heroTitle = document.querySelector('.hero-title .text-gradient');
  if (heroTitle) {
    heroTitle.style.backgroundSize = '200% auto';
    heroTitle.style.animation = 'shimmer 4s ease-in-out infinite';
  }

  // ==========================================================
  // 8. Card entrance staggered animation
  // ==========================================================
  // Add a small delay observer to handle the cards that don't use stagger
  const singleCards = document.querySelectorAll('.why-card');

  const cardObserver = new IntersectionObserver((entries) => {
    entries.forEach((entry, index) => {
      if (entry.isIntersecting) {
        const el = entry.target;
        const delay = parseFloat(el.getAttribute('data-delay') || '0');

        el.style.transitionDelay = `${delay}s`;
        el.classList.add('visible');

        cardObserver.unobserve(el);
      }
    });
  }, { threshold: 0.1 });

  singleCards.forEach(card => cardObserver.observe(card));

  // ==========================================================
  // 9. Smooth border glow animation on premium featured card
  // ==========================================================
  const featuredCard = document.querySelector('.premium-card.featured');
  if (featuredCard) {
    let hue = 260;
    setInterval(() => {
      hue = (hue + 0.5) % 360;
      // Subtle border color shift
      featuredCard.style.borderColor = `hsla(${hue}, 100%, 70%, ${0.4 + Math.sin(Date.now() / 3000) * 0.1})`;
    }, 100);
  }
});
