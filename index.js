// Interactive JavaScript for Portfolio Modals & Code Viewer Tabs

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
  const modals = ['erpModal', 'nodeAwsModal', 'rrhhModal', 'appModal', 'crmModal'];
  modals.forEach(id => {
    const modal = document.getElementById(id);
    if (event.target === modal) {
      closeModal(id);
    }
  });
};

// Interactive Code Viewer Tabs Controller
function showCodeTab(tabName) {
  const tabs = ['backend', 'node', 'frontend', 'database'];
  
  tabs.forEach(tab => {
    const content = document.getElementById(`code-${tab}`);
    const button = document.getElementById(`tab-${tab}`);
    
    if (content && button) {
      if (tab === tabName) {
        content.classList.remove('hidden');
        button.classList.add('bg-sky-500', 'text-slate-950');
        button.classList.remove('text-slate-400', 'hover:text-white');
      } else {
        content.classList.add('hidden');
        button.classList.remove('bg-sky-500', 'text-slate-950');
        button.classList.add('text-slate-400', 'hover:text-white');
      }
    }
  });
}
