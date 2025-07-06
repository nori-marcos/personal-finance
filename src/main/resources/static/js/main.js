document.addEventListener('DOMContentLoaded', () => {

    // --- NAVBAR BURGER & DROPDOWN ---
    document.querySelectorAll('.navbar-burger').forEach(el => {
        el.addEventListener('click', () => {
            const target = document.getElementById(el.dataset.target);
            el.classList.toggle('is-active');
            target.classList.toggle('is-active');
        });
    });

    const dropdown = document.querySelector('#add-transaction-dropdown');
    if (dropdown) {
        dropdown.addEventListener('click', (event) => {
            event.stopPropagation();
            dropdown.classList.toggle('is-active');
        });
        document.addEventListener('click', () => dropdown.classList.remove('is-active'));
    }

    // --- REUSABLE MODAL HANDLER ---
    const setupModal = (buttonId, modalId, formContentId, formUrl, formSubmitHandler) => {
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
                    const form = formContent.querySelector('form');
                    if (form && formSubmitHandler) form.addEventListener('submit', formSubmitHandler);
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

    // --- GENERIC FORM SUBMISSION HANDLER ---
    const handleFormSubmit = (event) => {
        event.preventDefault();
        const form = event.target;
        const formData = new FormData(form);
        fetch(form.action, { method: 'POST', body: formData })
            .then(response => {
                if (response.ok) window.location.reload();
                else alert('Ocorreu um erro ao salvar.');
            });
    };

    // --- INITIALIZE ALL MODALS ---
    setupModal('add-account-button', 'add-account-modal', 'modal-account-form-content', '/accounts/new-form', handleFormSubmit);
    setupModal('add-card-button', 'add-card-modal', 'modal-card-form-content', '/cards/new-form', handleFormSubmit);

    // Initialize modals for the new transaction dropdown items
    setupModal('add-income-expense-trigger', 'add-transaction-modal', 'modal-form-content', '/transactions/new-form', handleFormSubmit);
    setupModal('add-cc-expense-trigger', 'add-transaction-modal', 'modal-form-content', '/transactions/new-form/credit-card', handleFormSubmit);
    setupModal('add-payment-trigger', 'add-transaction-modal', 'modal-form-content', '/card-payments/new-form', handleFormSubmit);
    setupModal('add-transfer-trigger', 'add-transaction-modal', 'modal-form-content', '/transfers/new-form', handleFormSubmit);

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
        if (document.getElementById('mobile-new-transaction-page') && isDesktop) { window.location.href = '/transactions'; return; }
        if (document.getElementById('mobile-account-form-page') && isDesktop) { window.location.href = '/dashboard'; return; }
        if (document.getElementById('mobile-card-form-page') && isDesktop) { window.location.href = '/dashboard'; return; }
        if (document.getElementById('mobile-cc-expense-form-page') && isDesktop) { window.location.href = '/transactions'; return; }
        if (document.getElementById('mobile-card-payment-form-page') && isDesktop) { window.location.href = '/dashboard';  }
    };
    handleResponsiveRedirect();
    window.addEventListener('resize', handleResponsiveRedirect);
});