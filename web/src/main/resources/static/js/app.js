/* Meridian Bank — small progressive-enhancement behaviours (no framework). */
(function () {
  'use strict';

  document.addEventListener('DOMContentLoaded', function () {
    // Mobile sidebar toggle
    var toggle = document.getElementById('menu-toggle');
    var sidebar = document.getElementById('sidebar');
    if (toggle && sidebar) {
      toggle.addEventListener('click', function (e) {
        e.stopPropagation();
        sidebar.classList.toggle('open');
      });
      document.addEventListener('click', function (e) {
        if (sidebar.classList.contains('open') && !sidebar.contains(e.target)) {
          sidebar.classList.remove('open');
        }
      });
    }

    // File input: show chosen file name
    document.querySelectorAll('input[type="file"]').forEach(function (input) {
      var label = document.getElementById(input.getAttribute('data-label') || '');
      var update = function () {
        if (label) {
          label.textContent = input.files && input.files.length
            ? input.files[0].name + ' (' + Math.round((input.files[0].size || 0) / 1024) + ' KB)'
            : 'Choose a file…';
        }
      };
      input.addEventListener('change', update);
      update();
    });

    // Drag & drop styling on dropzones
    document.querySelectorAll('.dropzone').forEach(function (zone) {
      var input = zone.querySelector('input[type="file"]');
      if (!input) return;
      ['dragover', 'dragenter'].forEach(function (evt) {
        zone.addEventListener(evt, function (e) {
          e.preventDefault();
          zone.classList.add('drag');
        });
      });
      ['dragleave', 'drop'].forEach(function (evt) {
        zone.addEventListener(evt, function (e) {
          e.preventDefault();
          zone.classList.remove('drag');
        });
      });
      zone.addEventListener('drop', function (e) {
        if (e.dataTransfer && e.dataTransfer.files.length) {
          input.files = e.dataTransfer.files;
          input.dispatchEvent(new Event('change'));
        }
      });
      zone.addEventListener('click', function (e) {
        if (e.target !== input && e.target.tagName !== 'LABEL') {
          input.click();
        }
      });
    });

    // OTP inputs: auto-advance and digit-only filtering
    document.querySelectorAll('input[data-otp]').forEach(function (input) {
      input.addEventListener('input', function () {
        var value = input.value.replace(/[^0-9]/g, '');
        input.value = value;
      });
    });

    // Step-up OTP forms: reveal the inline code field when "send code" is pressed.
    document.querySelectorAll('[data-otp-form]').forEach(function (form) {
      var reveal = form.querySelector('[data-otp-reveal]');
      var otpField = form.querySelector('[data-otp-field]');
      if (!reveal || !otpField) return;
      var sendForm = document.getElementById(reveal.getAttribute('data-otp-reveal'));
      if (!sendForm) return;
      sendForm.addEventListener('submit', function () {
        otpField.style.display = '';
        reveal.style.display = 'none';
      });
    });

    // Auto-hide flash alerts after 8 seconds (success only)
    document.querySelectorAll('.alert-success').forEach(function (alert) {
      setTimeout(function () {
        alert.style.transition = 'opacity 0.6s';
        alert.style.opacity = '0';
        setTimeout(function () { alert.remove(); }, 700);
      }, 8000);
    });

    // Confirm destructive actions
    document.querySelectorAll('form[data-confirm]').forEach(function (form) {
      form.addEventListener('submit', function (e) {
        if (!window.confirm(form.getAttribute('data-confirm'))) {
          e.preventDefault();
        }
      });
    });
  });
})();
