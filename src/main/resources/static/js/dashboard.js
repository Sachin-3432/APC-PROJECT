const API_BASE = "/api";

const showToast = (message, type = 'info') => {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
};

const closeModal = (modalId) => {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
        modal.classList.remove('show');
    }
};

const openModal = (modalId) => {
    const modal = document.getElementById(modalId);
    if (!modal) {
        console.error(`Modal with ID ${modalId} not found`);
        showToast("Failed to open modal", "error");
        return;
    }
    modal.style.display = 'block';
    modal.classList.add('show');
};

const escapeJsString = (str) => {
    if (!str || typeof str !== 'string') return 'Unknown';
    return str.replace(/[\0-\x1F\x7F'"]/g, (char) => {
        switch (char) {
            case "'": return "\\'";
            case '"': return '\\"';
            case '\n': return '\\n';
            case '\r': return '\\r';
            case '\t': return '\\t';
            case '\b': return '\\b';
            case '\f': return '\\f';
            default: return `\\u${char.charCodeAt(0).toString(16).padStart(4, '0')}`;
        }
    });
};

const escapeHtml = (unsafe) => {
    if (!unsafe || typeof unsafe !== 'string') return 'Unknown';
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
};

async function checkAuth() {
    const token = localStorage.getItem("token");
    if (!token) {
        window.location.href = "/login.html";
        return;
    }
    try {
        const res = await fetch(`${API_BASE}/auth/me`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error("Unauthorized");
        const user = await res.json();
        window.currentUser = user;
        const displayName = user.fullName || user.email.split('@')[0] || 'User';
        document.getElementById("user-name-display").textContent = escapeHtml(displayName);
        document.getElementById("user-profile-img").src = user.profileImage || `https://ui-avatars.com/api/?name=${encodeURIComponent(displayName)}&background=2563eb&color=fff`;
        const adminLink = document.getElementById('admin-link');
        if (adminLink) adminLink.style.display = (user.roles || []).includes('ROLE_ADMIN') ? 'inline-block' : 'none';
        loadDashboard();
    } catch (error) {
        console.error("Auth check failed:", error);
        localStorage.removeItem("token");
        window.location.href = "/login.html";
    }
}

async function loadDashboard() {
    await Promise.all([
        loadExpenses(),
        loadBudget(),
        loadGroups(),
        loadBalances(),
        loadReminders(),
        loadCategorySummary(),
        loadReports(),
        loadSettlementHistory()
    ]);
}

async function loadSettlementHistory() {
    const token = localStorage.getItem("token");
    const section = document.getElementById("settlement-history-list");
    if (!section) return;
    try {
        const res = await fetch(`${API_BASE}/settlements`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error("Failed to load settlement history");
        const settlements = await res.json();
        if (!settlements.length) {
            section.innerHTML = '<div class="empty">No settlements yet.</div>';
            return;
        }
        section.innerHTML = settlements.map(s => `
            <div class="settlement-item">
                <span><b>${escapeHtml(s.payerName)}</b> paid <b>${escapeHtml(s.receiverName)}</b></span>
                <span>₹${s.amount.toFixed(2)}</span>
                <span class="settlement-date">${new Date(s.createdAt).toLocaleString()}</span>
                <span class="settlement-method">via ${escapeHtml(s.paymentMethod || 'N/A')}</span>
                <span class="settlement-desc">${escapeHtml(s.description || '')}</span>
            </div>
        `).join("");
    } catch (error) {
        section.innerHTML = '<div class="error">Failed to load settlement history</div>';
    }
}

async function loadExpenses() {
    const token = localStorage.getItem("token");
    try {
        const res = await fetch(`${API_BASE}/expenses`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error("Failed to load expenses");
        const expenses = await res.json();
        const expenseList = document.getElementById("expense-list");
        expenseList.innerHTML = expenses.length ? expenses.map(e => `
            <div class="expense-item">
                <div class="expense-details">
                    <strong>${escapeHtml(e.description)}</strong>
                    <div>${escapeHtml(e.category)} - ₹${e.amount.toFixed(2)} on ${e.date}</div>
                </div>
                <div class="expense-actions">
                    <button class="btn btn-small" onclick="editExpense(${e.id})"><i class="fas fa-edit"></i></button>
                    <button class="btn btn-small btn-danger" onclick="deleteExpense(${e.id})"><i class="fas fa-trash"></i></button>
                </div>
            </div>
        `).join("") : `
            <div class="expense-item">
                <div class="expense-details">
                    <strong>No expenses yet</strong>
                    <div>Add your first expense to see it here</div>
                </div>
                <div class="expense-actions">
                    <button class="btn btn-small" disabled><i class="fas fa-edit"></i></button>
                    <button class="btn btn-small btn-danger" disabled><i class="fas fa-trash"></i></button>
                </div>
            </div>
        `;
    } catch (error) {
        console.error("Load expenses error:", error);
        showToast("Failed to load expenses", "error");
    }
}

document.getElementById("expense-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const token = localStorage.getItem("token");
    const amount = parseFloat(document.getElementById("expense-amount").value);
    const splitEqually = document.getElementById("split-equally").checked;
    const expenseGroupId = document.getElementById("expense-group").value || null;
    let participantIds = [];
    // if a group is selected use selected participants, otherwise treat as personal expense
    if (expenseGroupId) {
        participantIds = Array.from(document.getElementById("expense-participants").selectedOptions).map(opt => opt.value);
    } else {
        participantIds = [window.currentUser ? String(window.currentUser.id) : ''];
    }
    const shares = splitEqually ? null : Array.from(document.querySelectorAll(".share-input")).reduce((acc, input, i) => {
        acc[participantIds[i]] = parseFloat(input.value) || 0;
        return acc;
    }, {});
    const totalShares = splitEqually ? amount / participantIds.length : Object.values(shares).reduce((sum, val) => sum + val, 0);
    if (!splitEqually && Math.abs(totalShares - amount) > 0.01) {
        document.getElementById("split-error").style.display = "block";
        return;
    }
    const dateInput = document.getElementById("date-input").value;
    let date = null;
    if (dateInput) {
        const parsedDate = new Date(dateInput);
        if (!isNaN(parsedDate)) {
            date = parsedDate.toISOString().slice(0, 10);
        }
    }
    const data = {
        description: document.getElementById("expense-description").value,
        amount,
        category: document.getElementById("expense-category").value,
        date,
        groupId: expenseGroupId,
        paidBy: expenseGroupId ? document.getElementById("paid-by").value : (window.currentUser ? String(window.currentUser.id) : ''),
        participantIds,
        shares: splitEqually ? null : shares
    };
    try {
        const response = await fetch(`${API_BASE}/expenses`, {
            method: "POST",
            headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
            body: JSON.stringify(data)
        });
        if (!response.ok) throw new Error(`Failed to add expense: ${await response.text()}`);
        showToast("Expense added successfully", "success");
        e.target.reset();
        document.getElementById("split-error").style.display = "none";
        loadDashboard();
    } catch (error) {
        console.error("Add expense error:", error);
        showToast(`Failed to add expense: ${error.message}`, "error");
    }
});

async function editExpense(id) {
    const token = localStorage.getItem("token");
    try {
        const res = await fetch(`${API_BASE}/expenses/${id}`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error("Failed to load expense");
        const expense = await res.json();
        document.getElementById("edit-expense-id").value = expense.id;
        document.getElementById("edit-expense-description").value = escapeHtml(expense.description);
        document.getElementById("edit-expense-amount").value = expense.amount;
        document.getElementById("edit-expense-category").value = expense.category;
        document.getElementById("edit-expense-date").value = expense.date;
        openModal("edit-expense-modal");
    } catch (error) {
        console.error("Edit expense error:", error);
        showToast("Failed to load expense", "error");
    }
}

document.getElementById("edit-expense-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const token = localStorage.getItem("token");
    const id = document.getElementById("edit-expense-id").value;
    const data = {
        description: document.getElementById("edit-expense-description").value,
        amount: parseFloat(document.getElementById("edit-expense-amount").value),
        category: document.getElementById("edit-expense-category").value,
        date: document.getElementById("edit-expense-date").value
    };
    try {
        const response = await fetch(`${API_BASE}/expenses/${id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
            body: JSON.stringify(data)
        });
        if (!response.ok) throw new Error(`Failed to update expense: ${await response.text()}`);
        showToast("Expense updated successfully", "success");
        closeModal("edit-expense-modal");
        loadDashboard();
    } catch (error) {
        console.error("Update expense error:", error);
        showToast(`Failed to update expense: ${error.message}`, "error");
    }
});

async function deleteExpense(id) {
    document.getElementById("modal-message").textContent = "Are you sure you want to delete this expense?";
    openModal("confirm-modal");
    document.getElementById("modal-confirm").onclick = async () => {
        const token = localStorage.getItem("token");
        try {
            const response = await fetch(`${API_BASE}/expenses/${id}`, {
                method: "DELETE",
                headers: { "Authorization": `Bearer ${token}` }
            });
            if (!response.ok) throw new Error(`Failed to delete expense: ${await response.text()}`);
            showToast("Expense deleted successfully", "success");
            closeModal("confirm-modal");
            loadDashboard();
        } catch (error) {
            console.error("Delete expense error:", error);
            showToast(`Failed to delete expense: ${error.message}`, "error");
        }
    };
    document.getElementById("modal-cancel").onclick = () => closeModal("confirm-modal");
}

async function loadBudget() {
    const token = localStorage.getItem("token");
    try {
        const res = await fetch(`${API_BASE}/budget`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error("Failed to load budget");
        const budget = await res.json();
        document.getElementById("monthly-budget").textContent = budget.amount.toFixed(2);
        document.getElementById("remaining-balance").textContent = budget.remaining.toFixed(2);
    } catch (error) {
        console.error("Load budget error:", error);
        showToast("Failed to load budget", "error");
    }
}

function editBudget() {
    document.getElementById("budget-form").style.display = "flex";
    document.querySelector(".budget-edit").style.display = "none";
}

function cancelEditBudget() {
    document.getElementById("budget-form").style.display = "none";
    document.querySelector(".budget-edit").style.display = "flex";
}

document.getElementById("budget-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const token = localStorage.getItem("token");
    const data = { amount: parseFloat(document.getElementById("new-budget").value) };
    try {
        const res = await fetch(`${API_BASE}/budget`, {
            method: "POST",
            headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
            body: JSON.stringify(data)
        });
        if (!res.ok) throw new Error(`Failed to update budget: ${await res.text()}`);
        const budget = await res.json();
        showToast("Budget updated successfully", "success");
        document.getElementById("budget-form").style.display = "none";
        document.querySelector(".budget-edit").style.display = "flex";
        document.getElementById("monthly-budget").textContent = budget.amount.toFixed(2);
        document.getElementById("remaining-balance").textContent = budget.remaining.toFixed(2);
    } catch (error) {
        console.error("Update budget error:", error);
        showToast(`Failed to update budget: ${error.message}`, "error");
    }
});

async function loadGroups() {
    const token = localStorage.getItem("token");
    try {
        const res = await fetch(`${API_BASE}/groups`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error("Failed to load groups");
        const groups = await res.json();
        const currentUserId = window.currentUser ? window.currentUser.id : null;

        const groupList = document.getElementById("groups-list");
        const expenseGroupSelect = document.getElementById("expense-group");
        expenseGroupSelect.innerHTML = `<option value="">Select group (optional)</option>` + groups.map(g => `<option value="${g.id}">${escapeHtml(g.name)}</option>`).join("");
        groupList.innerHTML = groups.length ? groups.map(g => {
            const memberCount = g.memberNames ? g.memberNames.length : 0;
            const isOwner = g.createdBy === currentUserId;
            return `
                <div class="group-item">
                    <div class="group-info">${escapeHtml(g.name)} <span style='color:var(--secondary-color);font-size:0.95em;'>( ${memberCount} member${memberCount === 1 ? '' : 's'} )</span></div>
                    <div>
                        <button class="btn btn-small" ${isOwner ? `onclick="editGroup('${String(g.id).replace(/'/g, "&#039;")}')"` : 'disabled title="Only the group owner can edit the details"'}><i class="fas fa-edit"></i></button>
                        <button class="btn btn-small btn-danger" onclick="deleteGroup('${String(g.id).replace(/'/g, "&#039;")}')"><i class="fas fa-trash"></i></button>
                    </div>
                </div>
            `;
        }).join("") : "<div>No groups yet</div>";
        await loadParticipants();
    } catch (error) {
        console.error("Load groups error:", error);
        showToast("Failed to load groups", "error");
    }
}

async function createGroup() {
    const token = localStorage.getItem("token");
    const name = document.getElementById("new-group").value;
    if (!name) {
        showToast("Group name is required", "error");
        return;
    }
    try {
        const response = await fetch(`${API_BASE}/groups`, {
            method: "POST",
            headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
            body: JSON.stringify({ name })
        });
        if (!response.ok) throw new Error(`Failed to create group: ${await response.text()}`);
        showToast("Group created successfully", "success");
        document.getElementById("new-group").value = "";
        loadGroups();
    } catch (error) {
        console.error("Create group error:", error);
        showToast(`Failed to create group: ${error.message}`, "error");
    }
}

async function joinGroup() {
    const token = localStorage.getItem("token");
    const groupId = document.getElementById("join-group").value;
    if (!groupId) {
        showToast("Group ID is required", "error");
        return;
    }
    try {
        const response = await fetch(`${API_BASE}/groups/join/${groupId}`, {
            method: "POST",
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!response.ok) {
            // Try to parse JSON { error: 'message' } or {message: '...'}; fallback to text
            let errMsg = 'Failed to join group';
            try {
                const errBody = await response.json();
                errMsg = errBody.error || errBody.message || JSON.stringify(errBody);
            } catch (parseErr) {
                try { errMsg = await response.text(); } catch (_) { /* ignore */ }
            }
            showToast(errMsg, 'error');
            return;
        }
        const body = await response.json();
        // body: { group: {...}, alreadyMember: boolean }
        if (body.alreadyMember) {
            showToast("You are already a member of this group", "info");
        } else {
            showToast("Joined group successfully", "success");
        }
        document.getElementById("join-group").value = "";
        loadGroups();
    } catch (error) {
        console.error("Join group error:", error);
        showToast(`Failed to join group: ${error.message}`, "error");
    }
}

async function editGroup(id) {
    const token = localStorage.getItem("token");
    try {
        const res = await fetch(`${API_BASE}/groups/${id}`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error("Failed to load group");
        const group = await res.json();
        document.getElementById("edit-group-id").value = group.id;
        document.getElementById("edit-group-name").value = escapeHtml(group.name);
        document.getElementById("edit-group-code").value = group.code;
        openModal("edit-group-modal");
    } catch (error) {
        console.error("Edit group error:", error);
        showToast("Failed to load group", "error");
    }
}

document.getElementById("edit-group-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const token = localStorage.getItem("token");
    const id = document.getElementById("edit-group-id").value;
    const data = { name: document.getElementById("edit-group-name").value };
    try {
        const response = await fetch(`${API_BASE}/groups/${id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
            body: JSON.stringify(data)
        });
        if (!response.ok) throw new Error(`Failed to update group: ${await response.text()}`);
        showToast("Group updated successfully", "success");
        closeModal("edit-group-modal");
        loadGroups();
    } catch (error) {
        console.error("Update group error:", error);
        showToast(`Failed to update group: ${error.message}`, "error");
    }
});

document.getElementById("copy-code-btn").addEventListener("click", () => {
    const code = document.getElementById("edit-group-code").value;
    navigator.clipboard.writeText(code);
    showToast("Group code copied", "success");
});

async function deleteGroup(id) {
    document.getElementById("modal-message").textContent = "Are you sure you want to delete this group?";
    openModal("confirm-modal");
    document.getElementById("modal-confirm").onclick = async () => {
        const token = localStorage.getItem("token");
        try {
            const response = await fetch(`${API_BASE}/groups/${id}`, {
                method: "DELETE",
                headers: { "Authorization": `Bearer ${token}` }
            });
            if (!response.ok) throw new Error(`Failed to delete group: ${await response.text()}`);
            showToast("Group deleted successfully", "success");
            closeModal("confirm-modal");
            loadGroups();
        } catch (error) {
            console.error("Delete group error:", error);
            showToast(`Failed to delete group: ${error.message}`, "error");
        }
    };
    document.getElementById("modal-cancel").onclick = () => closeModal("confirm-modal");
}

async function loadParticipants() {
    const token = localStorage.getItem("token");
    try {
        const participantSelect = document.getElementById("expense-participants");
        const paidBySelect = document.getElementById("paid-by");

        const groupId = document.getElementById("expense-group").value;
        // if no group selected, this is a personal expense: disable participants and set paid-by to current user
        if (!groupId) {
            participantSelect.innerHTML = "";
            participantSelect.disabled = true;
            document.getElementById('participant-shares-container').style.display = 'none';
            // set paid-by to current user only
            paidBySelect.innerHTML = `<option value="${window.currentUser ? String(window.currentUser.id) : ''}">${window.currentUser ? escapeHtml(window.currentUser.fullName || window.currentUser.email.split('@')[0]) : 'You'}</option>`;
            paidBySelect.value = window.currentUser ? String(window.currentUser.id) : '';
            return;
        }

        participantSelect.disabled = false;
        document.getElementById('participant-shares-container').style.display = '';

        const grpRes = await fetch(`${API_BASE}/groups/${groupId}`, { headers: { "Authorization": `Bearer ${token}` } });
        if (!grpRes.ok) throw new Error('Failed to load group members');
        const grp = await grpRes.json();
        const ids = grp.memberIds || [];
        const names = grp.memberNames || [];
        const users = ids.map((id, i) => ({ id: String(id), fullName: names[i] || 'Unknown User' }));

        participantSelect.innerHTML = users.map(u => `<option value="${u.id}">${escapeHtml(u.fullName)}</option>`).join("");
        paidBySelect.innerHTML = `<option value="">Select who paid</option>` + users.map(u => `<option value="${u.id}">${escapeHtml(u.fullName)}</option>`).join("");
    } catch (error) {
        console.error("Load participants error:", error);
        showToast("Failed to load participants", "error");
    }
}

document.getElementById("split-equally").addEventListener("change", updateSplitInputs);
document.getElementById("expense-amount").addEventListener("input", updateSplitInputs);
document.getElementById("expense-participants").addEventListener("change", updateSplitInputs);

// When user changes the selected group, reload participants to show only group members
const expenseGroupSelectElem = document.getElementById("expense-group");
if (expenseGroupSelectElem) {
    expenseGroupSelectElem.addEventListener('change', () => {
        loadParticipants().catch(err => console.error('Failed to reload participants on group change', err));
    });
}

function updateSplitInputs() {
    const amount = parseFloat(document.getElementById("expense-amount").value) || 0;
    const splitEqually = document.getElementById("split-equally").checked;
    const participants = Array.from(document.getElementById("expense-participants").selectedOptions).map(opt => ({
        id: opt.value,
        name: opt.text
    }));
    const sharesContainer = document.getElementById("participant-shares");
    const summary = document.getElementById("split-summary");
    if (!participants.length || !amount) {
        sharesContainer.innerHTML = "";
        summary.textContent = "Total: ₹0.00 | Remaining: ₹0.00";
        return;
    }
    const shareAmount = splitEqually ? (amount / participants.length).toFixed(2) : 0;
    sharesContainer.innerHTML = participants.map(p => `
        <div class="share-item">
            <div class="share-label">${escapeHtml(p.name)}</div>
            <div class="share-input-container">
                <input type="number" class="share-input" value="${shareAmount}" step="0.01" min="0" ${splitEqually ? "disabled" : ""}>
                <span class="currency-symbol">₹</span>
            </div>
        </div>
    `).join("");
    const total = splitEqually ? amount : Array.from(document.querySelectorAll(".share-input")).reduce((sum, input) => sum + (parseFloat(input.value) || 0), 0);
    const remaining = amount - total;
    summary.textContent = `Total: ₹${total.toFixed(2)} | Remaining: ₹${remaining.toFixed(2)}`;
    document.getElementById("split-error").style.display = remaining < -0.01 || remaining > 0.01 ? "block" : "none";
}

async function loadBalances() {
    const token = localStorage.getItem("token");
    try {
        const res = await fetch(`${API_BASE}/debts`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error(`Failed to load balances: ${await res.text()}`);
        const debts = await res.json();
        const totalExpenses = debts.reduce((sum, d) => sum + (d.amount > 0 ? d.amount : 0), 0);
        document.getElementById("total-expenses").textContent = totalExpenses.toFixed(2);
        const balanceList = document.getElementById("balance-list");
        balanceList.innerHTML = debts.length ? debts.map((d, index) => {
            if (!d.creditorId || isNaN(d.amount) || !d.creditorName) {
                return '';
            }
            return `
                <div class="balance-item" data-creditor-id="${d.creditorId}" data-amount="${d.amount}" data-creditor-name="${escapeHtml(d.creditorName)}">
                    <span>${escapeHtml(d.creditorName)} ${d.amount > 0 ? "owes you" : "you owe"} </span>
                    <span class="${d.amount > 0 ? 'balance-positive' : 'balance-negative'}">₹${Math.abs(d.amount).toFixed(2)}</span>
                    <button class="btn btn-small settle-btn" data-index="${index}"><i class="fas fa-handshake"></i> Settle</button>
                </div>
            `;
        }).join("") : "<div class='balance-item'><span>No balances yet</span><span>₹0.00</span></div>";

        document.querySelectorAll('.settle-btn').forEach(button => {
            button.addEventListener('click', () => {
                const balanceItem = button.closest('.balance-item');
                const creditorId = balanceItem.dataset.creditorId;
                const amount = parseFloat(balanceItem.dataset.amount);
                const creditorName = balanceItem.dataset.creditorName;
                openSettleDebtModal(creditorId, amount, creditorName);
            });
        });
    } catch (error) {
        console.error("Load balances error:", error);
        showToast(`Failed to load balances: ${error.message}`, "error");
    }
}

window.debugRefreshBalances = loadBalances;

function openSettleDebtModal(creditorId, amount, creditorName) {
    if (!creditorId || isNaN(amount) || !creditorName || typeof creditorName !== 'string') {
        showToast("Invalid debt details", "error");
        return;
    }
    // Fetch current user info to determine role
    const token = localStorage.getItem("token");
    fetch(`${API_BASE}/auth/me`, {
        headers: { "Authorization": `Bearer ${token}` }
    })
        .then(res => res.json())
        .then(user => {
            // If amount > 0, creditorName owes current user (user is payee)
            // If amount < 0, current user owes creditorName (user is payer)
            let title, details;
            if (amount > 0) {
                title = `Settle with ${escapeHtml(creditorName)}`;
                details = `${escapeHtml(creditorName)} owes you ₹${Math.abs(amount).toFixed(2)}`;
            } else {
                title = `Settle up with ${escapeHtml(creditorName)}`;
                details = `You owe ${escapeHtml(creditorName)} ₹${Math.abs(amount).toFixed(2)}`;
            }
            document.getElementById("settle-debt-id").value = creditorId;
            document.getElementById("settle-debt-title").textContent = title;
            document.getElementById("settle-debt-details").textContent = details;
            document.getElementById("settle-debt-amount").textContent = `₹${Math.abs(amount).toFixed(2)}`;
            document.getElementById("settle-amount").value = Math.abs(amount).toFixed(2);
            document.getElementById("settle-expense-details").textContent = "Related to: Group expenses";
            openModal("settle-debt-modal");
        })
        .catch(() => {
            showToast("Failed to load user info for settlement modal", "error");
        });
}

function setFullAmount() {
    const amount = document.getElementById("settle-debt-amount").textContent.replace("₹", "");
    document.getElementById("settle-amount").value = parseFloat(amount).toFixed(2);
}

document.getElementById("settle-debt-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const token = localStorage.getItem("token");
    const creditorId = document.getElementById("settle-debt-id").value;
    const amount = parseFloat(document.getElementById("settle-amount").value);
    const paymentMethod = document.getElementById("settle-payment-method").value;
    const description = document.getElementById("settle-description").value;
    if (isNaN(creditorId) || isNaN(amount) || amount <= 0) {
        showToast("Invalid creditor ID or amount", "error");
        return;
    }
    const data = { creditorId, amount, paymentMethod, description };
    try {
        const response = await fetch(`${API_BASE}/debts/settle`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(data)
        });
        if (!response.ok) throw new Error(`Failed to settle debt: ${await response.text()}`);
        showToast("Debt settled successfully", "success");
        closeModal("settle-debt-modal");
        loadDashboard();
    } catch (error) {
        console.error("Settle debt error:", error);
        showToast(`Failed to settle debt: ${error.message}`, "error");
    }
});

async function loadReminders() {
    const token = localStorage.getItem("token");
    try {
        const res = await fetch(`${API_BASE}/reminders`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error("Failed to load reminders");
        const reminders = await res.json();
        const notificationList = document.getElementById("notifications-list");
        const badge = document.getElementById("notification-badge");
        const unreadCount = reminders.filter(r => !r.read).length;
        badge.textContent = unreadCount;
        badge.style.display = unreadCount > 0 ? "flex" : "none";
        notificationList.innerHTML = reminders.length ? reminders.map(r => `
            <div class="notification-item ${r.read ? '' : 'unread'}" onclick="markReminderAsRead(${r.id})">
                <div class="notification-sender">${escapeHtml(r.senderName)}</div>
                <div class="notification-message">${escapeHtml(r.message)}</div>
                <div class="notification-time">${new Date(r.createdAt).toLocaleString()}</div>
            </div>
        `).join("") : "<div class='notification-item'>No notifications</div>";
    } catch (error) {
        console.error("Load reminders error:", error);
        showToast("Failed to load reminders", "error");
    }
}

function toggleNotifications() {
    const panel = document.getElementById("notifications-panel");
    panel.style.display = panel.style.display === "none" ? "block" : "none";
}

async function markReminderAsRead(id) {
    const token = localStorage.getItem("token");
    try {
        const response = await fetch(`${API_BASE}/reminders/${id}/read`, {
            method: "POST",
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!response.ok) throw new Error(`Failed to mark reminder as read: ${await response.text()}`);
        loadReminders();
    } catch (error) {
        console.error("Mark reminder error:", error);
        showToast("Failed to mark reminder as read", "error");
    }
}

async function markAllRemindersAsRead() {
    const token = localStorage.getItem("token");
    try {
        const response = await fetch(`${API_BASE}/reminders/read-all`, {
            method: "POST",
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!response.ok) throw new Error(`Failed to mark all reminders as read: ${await response.text()}`);
        loadReminders();
    } catch (error) {
        console.error("Mark all reminders error:", error);
        showToast("Failed to mark all reminders as read", "error");
    }
}

document.getElementById("reminder-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const token = localStorage.getItem("token");
    const data = {
        debtorId: document.getElementById("reminder-debtor-id").value,
        message: document.getElementById("reminder-message").value,
        method: document.getElementById("reminder-method").value
    };
    try {
        const response = await fetch(`${API_BASE}/reminders`, {
            method: "POST",
            headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
            body: JSON.stringify(data)
        });
        if (!response.ok) throw new Error(`Failed to send reminder: ${await response.text()}`);
        showToast("Reminder sent successfully", "success");
        closeModal("reminder-modal");
        loadReminders();
    } catch (error) {
        console.error("Send reminder error:", error);
        showToast(`Failed to send reminder: ${error.message}`, "error");
    }
});

function openProfileModal() {
    const token = localStorage.getItem("token");
    fetch(`${API_BASE}/auth/me`, {
        headers: { "Authorization": `Bearer ${token}` }
    }).then(res => {
        if (!res.ok) throw new Error("Failed to load profile");
        return res.json();
    }).then(user => {
        const displayName = user.fullName || user.email.split('@')[0] || 'User';
        document.getElementById("profile-name-input").value = escapeHtml(displayName);
        document.getElementById("profile-email-input").value = user.email;
        document.getElementById("profile-img-preview").src = user.profileImage || `https://ui-avatars.com/api/?name=${encodeURIComponent(displayName)}&background=2563eb&color=fff`;
        openModal("profile-modal");
    }).catch(error => {
        console.error("Load profile error:", error);
        showToast("Failed to load profile", "error");
    });
}

document.getElementById("profile-img-input").addEventListener("change", (e) => {
    const file = e.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = () => document.getElementById("profile-img-preview").src = reader.result;
        reader.readAsDataURL(file);
    }
});

document.getElementById("profile-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const token = localStorage.getItem("token");
    const formData = new FormData();
    const fullName = document.getElementById("profile-name-input").value;
    const email = document.getElementById("profile-email-input").value;
    const profileImageFile = document.getElementById("profile-img-input").files[0];
    // Validate full name: only alphabets and spaces
    if (!/^[A-Za-z ]+$/.test(fullName)) {
        showToast("Full name can only contain letters and spaces", "error");
        return;
    }
    if (fullName) formData.append("fullName", fullName);
    if (email) formData.append("email", email);
    if (profileImageFile) formData.append("profileImage", profileImageFile);
    const password = document.getElementById("profile-password-input").value;
    let passwordChanged = false;
    if (password) {
        try {
            const res = await fetch(`${API_BASE}/users/me/password`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify({ newPassword: password })
            });
            if (!res.ok) throw new Error(`Failed to update password: ${await res.text()}`);
            passwordChanged = true;
        } catch (error) {
            console.error("Password update error:", error);
            showToast(`Failed to update password: ${error.message}`, "error");
            return;
        }
    }
    try {
        const res = await fetch(`${API_BASE}/auth/profile`, {
            method: "PUT",
            headers: { "Authorization": `Bearer ${token}` },
            body: formData
        });
        if (!res.ok) {
            const text = await res.text();
            if (text.includes("Account with this email already exists")) {
                showToast("Account with this email already exists", "error");
                return;
            }
            throw new Error(`Failed to update profile: ${text}`);
        }
        const updatedUser = await res.json();
        showToast(passwordChanged ? "Profile and password updated successfully" : "Profile updated successfully", "success");
        closeModal("profile-modal");
        const displayName = updatedUser.fullName || updatedUser.email.split('@')[0] || 'User';
        document.getElementById("user-name-display").textContent = escapeHtml(displayName);
        document.getElementById("user-profile-img").src = updatedUser.profileImage || `https://ui-avatars.com/api/?name=${encodeURIComponent(displayName)}&background=2563eb&color=fff`;
    } catch (error) {
        console.error("Update profile error:", error);
        showToast(`Failed to update profile: ${error.message}`, "error");
    }
});

let monthlyChart, categoryChart;

async function loadReports() {
    const token = localStorage.getItem("token");
    try {
        const res = await fetch(`${API_BASE}/reports`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error("Failed to load reports");
        const reports = await res.json();
        const monthlyData = reports.monthly || {};
        const categoryData = reports.category || {};

        if (monthlyChart) monthlyChart.destroy();
        monthlyChart = new Chart(document.getElementById("monthly-chart"), {
            type: 'bar',
            data: {
                labels: Object.keys(monthlyData),
                datasets: [{
                    label: 'Monthly Expenses',
                    data: Object.values(monthlyData),
                    backgroundColor: 'rgba(37, 99, 235, 0.6)',
                    borderColor: 'rgba(37, 99, 235, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: { y: { beginAtZero: true } }
            }
        });

        if (categoryChart) categoryChart.destroy();
        categoryChart = new Chart(document.getElementById("category-chart"), {
            type: 'pie',
            data: {
                labels: Object.keys(categoryData),
                datasets: [{
                    data: Object.values(categoryData),
                    backgroundColor: ['#2563eb', '#10b981', '#f59e0b', '#ef4444', '#6b7280'],
                    borderColor: '#fff',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false
            }
        });
    } catch (error) {
        console.error("Load reports error:", error);
        showToast("Failed to load reports", "error");
    }
}

async function loadCategorySummary() {
    const token = localStorage.getItem("token");
    try {
        const res = await fetch(`${API_BASE}/reports/category`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error("Failed to load category summary");
        const categories = await res.json();
        const categoryList = document.getElementById("category-list");
        const total = Object.values(categories).reduce((sum, val) => sum + val, 0);
        categoryList.innerHTML = Object.entries(categories).map(([category, amount]) => {
            const percentage = total ? (amount / total * 100).toFixed(1) : 0;
            return `
                <div class="category-item">
                    <span>${escapeHtml(category)}</span>
                    <span>₹${amount.toFixed(2)}</span>
                </div>
                <div class="category-progress">
                    <div class="category-progress-bar" style="width: ${percentage}%"></div>
                </div>
            `;
        }).join("");
    } catch (error) {
        console.error("Load category summary error:", error);
        showToast("Failed to load category summary", "error");
    }
}

document.getElementById("download-csv").addEventListener("click", async () => {
    const token = localStorage.getItem("token");
    try {
        const res = await fetch(`${API_BASE}/expenses`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error("Failed to export CSV");
        const expenses = await res.json();
        const data = expenses.map(e => ({
            Description: e.description,
            Amount: e.amount,
            Category: e.category,
            Date: e.date,
            Group: e.groupName || "None",
            PaidBy: e.paidByName
        }));
        const csv = [
            "Description,Amount,Category,Date,Group,PaidBy",
            ...data.map(row => `${row.Description},${row.Amount},${row.Category},${row.Date},${row.Group},${row.PaidBy}`)
        ].join("\n");
        const blob = new Blob([csv], { type: "text/csv" });
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "expenses.csv";
        a.click();
        URL.revokeObjectURL(url);
    } catch (error) {
        console.error("Export CSV error:", error);
        showToast("Failed to export CSV", "error");
    }
});

document.getElementById("download-xlsx").addEventListener("click", async () => {
    const token = localStorage.getItem("token");
    try {
        const res = await fetch(`${API_BASE}/expenses`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error("Failed to export Excel");
        const expenses = await res.json();
        const data = expenses.map(e => ({
            Description: e.description,
            Amount: e.amount,
            Category: e.category,
            Date: e.date,
            Group: e.groupName || "None",
            PaidBy: e.paidByName
        }));
        const ws = XLSX.utils.json_to_sheet(data);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, "Expenses");
        XLSX.writeFile(wb, "expenses.xlsx");
    } catch (error) {
        console.error("Export Excel error:", error);
        showToast("Failed to export Excel", "error");
    }
});

document.querySelectorAll(".tab").forEach(tab => {
    tab.addEventListener("click", () => {
        document.querySelectorAll(".tab").forEach(t => t.classList.remove("active"));
        document.querySelectorAll(".tab-content").forEach(c => c.classList.remove("active"));
        tab.classList.add("active");
        document.getElementById(`${tab.dataset.tab}-tab`).classList.add("active");
    });
});

document.getElementById("user-info").addEventListener("click", () => {
    document.getElementById("user-dropdown").classList.toggle("show");
});

document.addEventListener("click", (event) => {
    const userDropdown = document.getElementById("user-dropdown");
    const userInfo = document.getElementById("user-info");
    if (!userInfo.contains(event.target) && !userDropdown.contains(event.target)) {
        userDropdown.classList.remove("show");
    }
});

document.addEventListener("click", (event) => {
    const notificationsPanel = document.getElementById("notifications-panel");
    const notificationIcon = document.getElementById("notification-icon");
    if (!notificationIcon.contains(event.target) && !notificationsPanel.contains(event.target)) {
        notificationsPanel.style.display = "none";
    }
});

function logoutUser() {
    // Call server logout to clear HttpOnly JWT cookie, then clear local token
    fetch(`${API_BASE}/auth/logout`, { method: 'POST', credentials: 'same-origin' })
        .finally(() => {
            localStorage.removeItem("token");
            window.location.href = "/login.html";
        });
}

document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
});