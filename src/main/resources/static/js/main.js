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

    // --- MODAL & AJAX FORM FOR DESKTOP ---
    const addTransactionButton = document.getElementById('add-transaction-button');
    const modal = document.getElementById('add-transaction-modal');
    const modalFormContent = document.getElementById('modal-form-content');
    const modalCloseButton = document.getElementById('modal-close-button');

    // Function to open the modal
    const openModal = () => {
        fetch('/transactions/new-form')
            .then(response => response.text())
            .then(html => {
                modalFormContent.innerHTML = html;
                modal.classList.add('is-active');
                attachFormSubmitListener();
            });
    };

    // Function to close the modal
    const closeModal = () => {
        modal.classList.remove('is-active');
        modalFormContent.innerHTML = '';
    };

    // Attach listener to the main "Add Transaction" button on desktop
    if (addTransactionButton) {
        addTransactionButton.addEventListener('click', openModal);
    }

    // Listeners for closing the modal
    if (modalCloseButton) {
        modalCloseButton.addEventListener('click', closeModal);
    }

    // Use event delegation for the dynamically loaded cancel button
    document.addEventListener('click', function (event) {
        if (event.target && event.target.id === 'modal-cancel-button') {
            closeModal();
        }
    });

    // Function to handle the AJAX form submission
    const attachFormSubmitListener = () => {
        const transactionForm = document.getElementById('transaction-form');
        if (transactionForm) {
            transactionForm.addEventListener('submit', function (event) {
                event.preventDefault();

                const formData = new FormData(transactionForm);
                const action = event.submitter.value;

                fetch('/api/transactions', {
                    method: 'POST',
                    body: formData
                })
                    .then(response => {
                        if (response.ok) {
                            if (action === 'save') {
                                window.location.reload();
                            } else {
                                transactionForm.reset();
                                document.getElementById('form-transaction-type').focus();
                            }
                        } else {
                            console.error('Form submission failed');
                        }
                    });
            });
        }
    };

    // --- MOBILE FLOATING ACTION BUTTON (FAB) ---
    // This was the missing part
    const fabToggle = document.getElementById('fab-toggle');
    const fabActions = document.querySelector('.fab-action-buttons');

    if (fabToggle && fabActions) {
        fabToggle.addEventListener('click', () => {
            fabToggle.classList.toggle('is-active');
            fabActions.classList.toggle('is-active');
        });
    }

    const handleResponsiveRedirect = () => {
        const mobileFormPage = document.getElementById('mobile-new-transaction-page');
        const isDesktop = window.innerWidth > 1023; // Bulma's breakpoint for desktop

        if (mobileFormPage && isDesktop) {
            // If we are on the mobile form page but the screen is now desktop-sized,
            // redirect to the main transactions page so the user can use the modal.
            window.location.href = '/transactions';
        }
    };

    // Run the check when the page first loads
    handleResponsiveRedirect();

    // Also run the check whenever the window is resized
    window.addEventListener('resize', handleResponsiveRedirect);
});