/* ============================================================
   Expense Tracker: Budget & Spend — Screenshot Carousel
   ============================================================ */

'use strict';

document.addEventListener('DOMContentLoaded', () => {

  // ==========================================================
  // 1. Phone Screen Slider
  // ==========================================================
  const slides = document.querySelectorAll('.phone-screen-slide');
  const dots = document.querySelectorAll('.phone-screen-dot');
  const phonePreview = document.querySelector('.phone-mockup');

  if (slides.length === 0) return;

  let currentSlide = 0;
  let slideInterval;
  const SLIDE_DURATION = 4000; // ms between auto slides
  const TRANSITION_DURATION = 800; // match CSS transition

  // Go to a specific slide
  function goToSlide(index) {
    // Remove active from all
    slides.forEach(s => s.classList.remove('active'));
    dots.forEach(d => d.classList.remove('active'));

    // Set active
    currentSlide = (index + slides.length) % slides.length;
    slides[currentSlide].classList.add('active');
    if (dots[currentSlide]) {
      dots[currentSlide].classList.add('active');
    }
  }

  // Next slide
  function nextSlide() {
    goToSlide(currentSlide + 1);
  }

  // Previous slide
  function prevSlide() {
    goToSlide(currentSlide - 1);
  }

  // Start auto-play
  function startAutoPlay() {
    stopAutoPlay();
    slideInterval = setInterval(nextSlide, SLIDE_DURATION);
  }

  // Stop auto-play
  function stopAutoPlay() {
    if (slideInterval) {
      clearInterval(slideInterval);
      slideInterval = null;
    }
  }

  // Initialize
  goToSlide(0);
  startAutoPlay();

  // ==========================================================
  // 2. Dot Navigation
  // ==========================================================
  dots.forEach((dot, index) => {
    dot.addEventListener('click', () => {
      goToSlide(index);
      // Reset auto-play timer on manual interaction
      startAutoPlay();
    });

    // Keyboard accessibility
    dot.setAttribute('tabindex', '0');
    dot.setAttribute('role', 'button');
    dot.setAttribute('aria-label', `Go to slide ${index + 1}`);

    dot.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        dot.click();
      }
    });
  });

  // ==========================================================
  // 3. Touch/Swipe Support
  // ==========================================================
  let touchStartX = 0;
  let touchEndX = 0;
  let isSwiping = false;

  if (phonePreview) {
    phonePreview.addEventListener('touchstart', (e) => {
      touchStartX = e.changedTouches[0].screenX;
      isSwiping = true;
      stopAutoPlay();
    }, { passive: true });

    phonePreview.addEventListener('touchmove', (e) => {
      if (!isSwiping) return;
      touchEndX = e.changedTouches[0].screenX;
    }, { passive: true });

    phonePreview.addEventListener('touchend', () => {
      if (!isSwiping) return;
      isSwiping = false;

      const swipeDistance = touchStartX - touchEndX;
      const threshold = 50; // minimum swipe distance

      if (Math.abs(swipeDistance) > threshold) {
        if (swipeDistance > 0) {
          nextSlide(); // Swipe left -> next
        } else {
          prevSlide(); // Swipe right -> previous
        }
      }

      // Resume auto-play
      startAutoPlay();
    }, { passive: true });
  }

  // ==========================================================
  // 4. Keyboard Navigation
  // ==========================================================
  document.addEventListener('keydown', (e) => {
    // Only if phone is in viewport
    if (!phonePreview) return;
    const rect = phonePreview.getBoundingClientRect();
    const isVisible = rect.top < window.innerHeight && rect.bottom > 0;

    if (!isVisible) return;

    if (e.key === 'ArrowRight') {
      e.preventDefault();
      nextSlide();
      startAutoPlay();
    } else if (e.key === 'ArrowLeft') {
      e.preventDefault();
      prevSlide();
      startAutoPlay();
    }
  });

  // ==========================================================
  // 5. Pause on hover
  // ==========================================================
  if (phonePreview) {
    phonePreview.addEventListener('mouseenter', stopAutoPlay);
    phonePreview.addEventListener('mouseleave', startAutoPlay);
  }

  console.log('📱 Carousel initialized with', slides.length, 'screens');
});
