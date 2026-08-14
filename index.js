// Interactive JavaScript for Portfolio Modals & Features

function openModal(id) {
  const modal = document.getElementById(id);
  if (modal) {
    modal.classList.remove('hidden');
    modal.classList.add('modal-active');
  }
}

function closeModal(id) {
  const modal = document.getElementById(id);
  if (modal) {
    modal.classList.add('hidden');
    modal.classList.remove('modal-active');
  }
}

// Close modal when clicking outside
window.onclick = function(event) {
  const modals = ['erpModal', 'rrhhModal', 'crmModal'];
  modals.forEach(id => {
    const modal = document.getElementById(id);
    if (event.target === modal) {
      closeModal(id);
    }
  });
};
