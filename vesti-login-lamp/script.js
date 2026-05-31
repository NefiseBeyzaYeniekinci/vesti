/* ==========================================================================
   VESTI PREMIUM INTERACTIVE FLOOR-LAMP LOGIN PAGE SCRIPT
   ========================================================================== */

document.addEventListener('DOMContentLoaded', () => {
    // ----------------------------------------------------------------------
    // DOM ELEMENTS
    // ----------------------------------------------------------------------
    const body = document.body;
    const pullHitbox = document.getElementById('pull-hitbox');
    const pullChain = document.getElementById('pull-chain');
    const togglePasswordBtn = document.getElementById('toggle-password');
    const inputPassword = document.getElementById('input-password');
    
    // Auth Tabs Switch elements
    const tabLogin = document.getElementById('tab-login');
    const tabRegister = document.getElementById('tab-register');
    const groupName = document.getElementById('group-name');
    const inputName = document.getElementById('input-name');
    const submitBtnText = document.getElementById('submit-btn-text');
    const formActionsRow = document.getElementById('form-actions-row');
    const footerText = document.getElementById('footer-text');
    const footerLink = document.getElementById('footer-link');
    const authForm = document.getElementById('auth-form');
    const btnGoogle = document.getElementById('btn-google');
    
    // Spinner & Loading
    const submitSpinner = document.getElementById('submit-spinner');
    const btnSubmit = document.getElementById('btn-submit');

    // ----------------------------------------------------------------------
    // LAZY WEB AUDIO API SWITCH SYNTHESIS
    // ----------------------------------------------------------------------
    let audioCtx = null;

    /**
     * Synthesizes a high-fidelity mechanical click-clack switch toggle sound
     * using the Web Audio API to bypass any external audio asset overhead.
     */
    function playSwitchSound(isOpening) {
        try {
            // Lazy initialize AudioContext on first user interaction
            if (!audioCtx) {
                audioCtx = new (window.AudioContext || window.webkitAudioContext)();
            }
            if (audioCtx.state === 'suspended') {
                audioCtx.resume();
            }

            const now = audioCtx.currentTime;

            // Toggle switch sound has two parts: 
            // 1. Initial spring toggle snap (0ms)
            triggerSnap(now, isOpening, 1.0);
            
            // 2. Lock latch contact click (30ms later)
            triggerSnap(now + 0.03, isOpening, 0.65);
        } catch (e) {
            console.warn("Web Audio API not supported or blocked by browser policies:", e);
        }
    }

    function triggerSnap(time, isOpening, volume) {
        const osc = audioCtx.createOscillator();
        const gainNode = audioCtx.createGain();
        const filter = audioCtx.createBiquadFilter();

        osc.type = 'triangle';
        
        // High frequency pitch for crisp metallic snapping click
        const startFreq = isOpening ? 2200 : 1800;
        const endFreq = isOpening ? 150 : 120;
        
        osc.frequency.setValueAtTime(startFreq, time);
        osc.frequency.exponentialRampToValueAtTime(endFreq, time + 0.015);

        filter.type = 'bandpass';
        filter.frequency.setValueAtTime(1200, time);
        filter.Q.setValueAtTime(3.0, time);

        // Very short volume envelope (15ms decay) for snappy toggle click
        gainNode.gain.setValueAtTime(0.07 * volume, time);
        gainNode.gain.exponentialRampToValueAtTime(0.0001, time + 0.015);

        osc.connect(filter);
        filter.connect(gainNode);
        gainNode.connect(audioCtx.destination);

        osc.start(time);
        osc.stop(time + 0.02);
    }

    // ----------------------------------------------------------------------
    // INTERACTIVE BEAD CORD DRAG-PHYSICS
    // ----------------------------------------------------------------------
    let isDragging = false;
    let startY = 0;
    let dragY = 0;
    const maxDrag = 55;        // Maximum vertical drag extension in pixels
    const pullThreshold = 30;  // Threshold past which light toggles

    // Cord Click Fallback (single click works instantly too!)
    pullHitbox.addEventListener('click', (e) => {
        if (dragY < 5) {
            toggleLight();
        }
    });

    // Touch & Mouse Drag Start
    const dragStart = (e) => {
        isDragging = true;
        startY = e.touches ? e.touches[0].clientY : e.clientY;
        pullChain.style.transition = 'none'; // Temporarily disable transition during manual drag
        e.preventDefault();
    };

    pullHitbox.addEventListener('mousedown', dragStart);
    pullHitbox.addEventListener('touchstart', dragStart, { passive: false });

    // Drag Motion Move
    const dragMove = (e) => {
        if (!isDragging) return;
        
        const currentY = e.touches ? e.touches[0].clientY : e.clientY;
        const diffY = currentY - startY;

        // Apply physics constraints: cannot drag upwards, limit max pull length
        dragY = Math.max(0, Math.min(diffY, maxDrag));
        
        // Stretch the bead cord with translate transform
        pullChain.style.transform = `translateY(${dragY}px)`;
    };

    document.addEventListener('mousemove', dragMove);
    document.addEventListener('touchmove', dragMove, { passive: false });

    // Drag Finish & Spring Haptic Snap-Back
    const dragEnd = () => {
        if (!isDragging) return;
        isDragging = false;

        // Re-enable smooth spring snap-back transition
        pullChain.style.transition = 'transform 0.25s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
        pullChain.style.transform = 'translateY(0px)';

        // Toggle state if pulled past threshold
        if (dragY >= pullThreshold) {
            toggleLight();
        }
        
        dragY = 0;
    };

    document.addEventListener('mouseup', dragEnd);
    document.addEventListener('touchend', dragEnd);

    // ----------------------------------------------------------------------
    // LIGHT SWITCH ACTIONS
    // ----------------------------------------------------------------------
    function toggleLight() {
        const isTurningOn = body.classList.contains('light-off');
        
        if (isTurningOn) {
            body.classList.remove('light-off');
            body.classList.add('light-on');
            playSwitchSound(true);
            
            // Enable interaction on input form
            enableFormInputs(true);
        } else {
            body.classList.remove('light-on');
            body.classList.add('light-off');
            playSwitchSound(false);
            
            // Disable interaction on input form (stops tab index focus when light off)
            enableFormInputs(false);
        }
    }

    function enableFormInputs(enabled) {
        const inputs = authForm.querySelectorAll('input, button, a');
        inputs.forEach(el => {
            if (enabled) {
                el.removeAttribute('tabindex');
                if (el.tagName === 'INPUT') el.disabled = false;
            } else {
                el.setAttribute('tabindex', '-1');
                if (el.tagName === 'INPUT') el.disabled = true;
            }
        });
    }

    // Start with form inputs fully disabled when light is initially off
    enableFormInputs(false);

    // ----------------------------------------------------------------------
    // PASSWORD VISIBILITY TOGGLING
    // ----------------------------------------------------------------------
    togglePasswordBtn.addEventListener('click', () => {
        const isPassword = inputPassword.getAttribute('type') === 'password';
        
        if (isPassword) {
            inputPassword.setAttribute('type', 'text');
            togglePasswordBtn.innerHTML = `
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="eye-icon">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                    <line x1="1" y1="1" x2="23" y2="23"></line>
                </svg>
            `;
        } else {
            inputPassword.setAttribute('type', 'password');
            togglePasswordBtn.innerHTML = `
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="eye-icon">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                </svg>
            `;
        }
    });

    // ----------------------------------------------------------------------
    // AUTH TABS SYSTEM (LOGIN <-> REGISTER SWITCHING)
    // ----------------------------------------------------------------------
    let isRegisterMode = false;

    function switchMode(register) {
        if (isRegisterMode === register) return;
        isRegisterMode = register;

        if (isRegisterMode) {
            // Switch tabs
            tabLogin.classList.remove('active');
            tabRegister.classList.add('active');

            // Slide down Name Input Group
            groupName.classList.remove('hidden');
            inputName.required = true;

            // Change typography labels
            submitBtnText.textContent = "Kayıt Ol";
            formActionsRow.style.display = 'none'; // Hide remember me & forgot pw

            footerText.innerHTML = `Zaten hesabınız var mı? <a href="#" id="footer-link">Giriş Yap</a>`;
        } else {
            // Switch tabs
            tabRegister.classList.remove('active');
            tabLogin.classList.add('active');

            // Slide up Name Input Group
            groupName.classList.add('hidden');
            inputName.required = false;
            inputName.value = "";

            // Change typography labels
            submitBtnText.textContent = "Giriş Yap";
            formActionsRow.style.display = 'flex'; // Show remember me & forgot pw

            footerText.innerHTML = `Hesabınız yok mu? <a href="#" id="footer-link">Kayıt Ol</a>`;
        }

        // Re-bind footer dynamic link
        const newFooterLink = document.getElementById('footer-link');
        newFooterLink.addEventListener('click', (e) => {
            e.preventDefault();
            switchMode(!isRegisterMode);
        });
    }

    tabLogin.addEventListener('click', () => switchMode(false));
    tabRegister.addEventListener('click', () => switchMode(true));
    footerLink.addEventListener('click', (e) => {
        e.preventDefault();
        switchMode(true);
    });

    // ----------------------------------------------------------------------
    // FORM SUBMISSION & LOADING EFFECTS
    // ----------------------------------------------------------------------
    authForm.addEventListener('submit', (e) => {
        e.preventDefault();
        
        // Prevent double submit
        if (!btnSubmit.disabled) {
            btnSubmit.disabled = true;
            submitSpinner.classList.remove('hidden');
            submitBtnText.classList.add('hidden');
            
            // Mock server response delay
            setTimeout(() => {
                btnSubmit.disabled = false;
                submitSpinner.classList.add('hidden');
                submitBtnText.classList.remove('hidden');
                
                // Show standard greeting alert
                const emailVal = document.getElementById('input-email').value;
                const nameVal = isRegisterMode ? inputName.value : "Vesti Kullanıcısı";
                
                alert(`Tebrikler ${nameVal}! ${isRegisterMode ? "Kayıt işleminiz" : "Girişiniz"} başarıyla tamamlandı.\n(E-posta: ${emailVal})`);
            }, 1800);
        }
    });

    // Mock Google sign in
    btnGoogle.addEventListener('click', () => {
        alert("Google ile Kimlik Doğrulama Servisine yönlendiriliyorsunuz...");
    });
});