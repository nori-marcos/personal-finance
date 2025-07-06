document.addEventListener('DOMContentLoaded', () => {

    // --- NAVBAR BURGER ---
    const $navbarBurgers = Array.prototype.slice.call(document.querySelectorAll('.navbar-burger'), 0);
    if ($navbarBurgers.length > 0) {
        $navbarBurgers.forEach(el => {
            el.addEventListener('click', () => {
                const target = el.dataset.target;
                const $target = document.getElementById(target);
                el.classList.toggle('is-active');
                $target.classList.toggle('is-active');
            });
        });
    }

    // --- REUSABLE MODAL HANDLER ---
    const setupModal = (buttonId, modalId, formContentId, formUrl) => {
        const triggerButton = document.getElementById(buttonId);
        const modal = document.getElementById(modalId);
        const formContent = document.getElementById(formContentId);

        if (!triggerButton || !modal || !formContent) return;

        const openModal = () => {
            fetch(formUrl)
                .then(response => response.ok ? response.text() : Promise.reject('Failed to load form'))
                .then(html => {
                    formContent.innerHTML = html;
                    modal.classList.add('is-active');
                }).catch(error => console.error("Error loading modal content:", error));
        };

        const closeModal = () => modal.classList.remove('is-active');

        triggerButton.addEventListener('click', openModal);
        modal.querySelector('.modal-background').addEventListener('click', closeModal);
        modal.querySelector('.delete').addEventListener('click', closeModal);
        document.addEventListener('click', event => {
            if (event.target && event.target.matches(`#${modalId} #modal-cancel-button`)) {
                closeModal();
            }
        });
    };

    // --- INITIALIZE ALL MODALS ---
    setupModal('add-transaction-button', 'add-transaction-modal', 'modal-form-content', '/transactions/new-form');
    setupModal('add-account-button', 'add-account-modal', 'modal-account-form-content', '/accounts/new-form');
    setupModal('add-card-button', 'add-card-modal', 'modal-card-form-content', '/cards/new-form');


    // --- MOBILE FLOATING ACTION BUTTON (FAB) ---
    const fabToggle = document.getElementById('fab-toggle');
    const fabActions = document.querySelector('.fab-action-buttons');
    if (fabToggle && fabActions) {
        fabToggle.addEventListener('click', () => {
            fabToggle.classList.toggle('is-active');
            fabActions.classList.toggle('is-active');
        });
    }

    // --- RESPONSIVE REDIRECT LOGIC ---
    const handleResponsiveRedirect = () => {
        const isDesktop = window.innerWidth > 1023;

        if (document.getElementById('mobile-new-transaction-page') && isDesktop) {
            window.location.href = '/transactions';
            return;
        }
        if (document.getElementById('mobile-account-form-page') && isDesktop) {
            window.location.href = '/dashboard';
            return;
        }
        if (document.getElementById('mobile-card-form-page') && isDesktop) {
            window.location.href = '/dashboard';

        }
    };

    handleResponsiveRedirect();
    window.addEventListener('resize', handleResponsiveRedirect);
});