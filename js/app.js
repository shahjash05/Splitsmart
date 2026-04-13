// =============================================
// SplitSmart - Main Application (API-Connected)
// =============================================

// Clear any stale data on page load
localStorage.removeItem('splitsmart_token_test');

const AppState = {
    currentUser: null,
    currentPage: 'dashboard',
    isAuthenticated: false,
    tempMembers: [],
    tempNewMembers: [],
    tempRemovedMemberIds: [],
    currentExpenseId: null,
    currentGroupId: null,
    categories: [],
    groups: []
};

// ===== Toast =====
function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type} show`;
    setTimeout(() => toast.classList.remove('show'), 3500);
}

// ===== Modal =====
function showModal(id) { document.getElementById(id)?.classList.add('active'); }
function closeModal(id) { document.getElementById(id)?.classList.remove('active'); }

// ===== Auth =====
function initAuth() {
    document.getElementById('login-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;
        try {
            const res = await AuthAPI.login({ email, password });
            Auth.setToken(res.token);
            AppState.currentUser = res;
            AppState.isAuthenticated = true;
            showApp();
            showToast(`Welcome back, ${res.name}!`);
        } catch (err) {
            showToast(err.message || 'Invalid credentials', 'error');
        }
    });

    document.getElementById('signup-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('signup-name').value;
        const email = document.getElementById('signup-email').value;
        const password = document.getElementById('signup-password').value;
        const confirm = document.getElementById('signup-confirm').value;
        if (password !== confirm) { showToast('Passwords do not match!', 'error'); return; }
        try {
            const res = await AuthAPI.signup({ name, email, password });
            Auth.setToken(res.token);
            AppState.currentUser = res;
            AppState.isAuthenticated = true;
            showApp();
            showToast('Account created!');
        } catch (err) {
            showToast(err.message || 'Signup failed', 'error');
        }
    });

    document.getElementById('show-signup').addEventListener('click', (e) => {
        e.preventDefault();
        document.getElementById('login-page').classList.add('hidden');
        document.getElementById('signup-page').classList.remove('hidden');
    });

    document.getElementById('show-login').addEventListener('click', (e) => {
        e.preventDefault();
        document.getElementById('signup-page').classList.add('hidden');
        document.getElementById('login-page').classList.remove('hidden');
    });

    document.getElementById('logout-btn').addEventListener('click', () => {
        Auth.clearToken();
        AppState.currentUser = null;
        AppState.isAuthenticated = false;
        hideApp();
        showToast('Logged out', 'success');
    });
}

function showApp() {
    document.getElementById('auth-container').classList.add('hidden');
    document.getElementById('app-container').classList.remove('hidden');
    document.getElementById('current-user-name').textContent = AppState.currentUser.name;
    navigateToPage('dashboard');
}

function hideApp() {
    document.getElementById('app-container').classList.add('hidden');
    document.getElementById('auth-container').classList.remove('hidden');
    document.getElementById('login-form').reset();
    document.getElementById('signup-form').reset();
}

// ===== Navigation =====
function initNavigation() {
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            navigateToPage(link.dataset.page);
        });
    });
}

function navigateToPage(pageName) {
    document.querySelectorAll('.nav-link').forEach(l => {
        l.classList.toggle('active', l.dataset.page === pageName);
    });
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.getElementById(`${pageName}-page`)?.classList.add('active');
    AppState.currentPage = pageName;
    switch (pageName) {
        case 'dashboard': loadDashboard(); break;
        case 'groups': loadGroups(); break;
        case 'expenses': loadExpenses(); break;
        case 'settlements': loadSettlements(); break;
        case 'profile': loadProfile(); break;
    }
}

// ===== Dashboard =====
async function loadDashboard() {
    try {
        const data = await DashboardAPI.get();

        document.getElementById('total-expenses').textContent = DataHelpers.formatCurrency(data.totalExpensesThisMonth);
        document.getElementById('amount-owed').textContent = DataHelpers.formatCurrency(data.totalOwed);
        document.getElementById('amount-owing').textContent = DataHelpers.formatCurrency(data.totalOwing);
        document.getElementById('active-groups').textContent = data.activeGroups;

        // Monthly limit warning
        if (data.nearLimit) {
            document.getElementById('warning-percentage').textContent = Math.round(data.limitUsedPercentage) + '%';
            document.getElementById('limit-warning').classList.remove('hidden');
        } else {
            document.getElementById('limit-warning').classList.add('hidden');
        }

        renderRecentExpenses(data.recentExpenses || []);
        renderBalanceSummary(data.balances || []);
    } catch (err) {
        showToast('Failed to load dashboard: ' + err.message, 'error');
    }
}

function renderRecentExpenses(expenses) {
    const container = document.getElementById('recent-expenses');
    if (!expenses.length) {
        container.innerHTML = '<div class="empty-state"><p>No recent expenses</p></div>';
        return;
    }
    container.innerHTML = expenses.map(e => {
        const userPart = (e.participants || []).find(p => p.userId === AppState.currentUser.userId);
        const diff = userPart ? (userPart.paidAmount - userPart.owedAmount) : 0;
        return `
        <div class="expense-item">
            <div class="expense-icon" style="background-color:#e0e7ff"><i class="fas fa-receipt"></i></div>
            <div class="expense-details">
                <div class="expense-title">${e.description}</div>
                <div class="expense-meta">
                    <span>${DataHelpers.formatDate(e.dateOfExpense)}</span>
                    ${e.groupName ? `<span>• ${e.groupName}</span>` : '<span>• Personal</span>'}
                </div>
            </div>
            <div class="expense-amount ${diff >= 0 ? 'positive' : 'negative'}">
                ${diff >= 0 ? '+' : ''}${DataHelpers.formatCurrency(Math.abs(diff))}
            </div>
        </div>`;
    }).join('');
}

function renderBalanceSummary(balances) {
    const container = document.getElementById('balance-summary');
    if (!balances.length) {
        container.innerHTML = '<div class="empty-state"><p>All settled up!</p></div>';
        return;
    }
    container.innerHTML = balances.slice(0, 5).map(b => `
        <div class="balance-item">
            <div class="balance-info">
                <div class="balance-avatar">${DataHelpers.getUserInitials(b.otherUserName)}</div>
                <div>
                    <div class="balance-name">${b.otherUserName}</div>
                    <div class="balance-text">${b.amount > 0 ? 'owes you' : 'you owe'}</div>
                </div>
            </div>
            <div class="balance-amount ${b.amount > 0 ? 'positive' : 'negative'}">
                ${DataHelpers.formatCurrency(Math.abs(b.amount))}
            </div>
        </div>`).join('');
}

// ===== Groups =====
async function loadGroups() {
    try {
        AppState.groups = await GroupAPI.list();
        const container = document.getElementById('groups-container');

        if (!AppState.groups.length) {
            container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-users"></i>
                <h3>No groups yet</h3>
                <p>Create a group to start sharing expenses</p>
                <button class="btn btn-primary" onclick="showModal('create-group-modal')">
                    <i class="fas fa-plus"></i> Create Your First Group
                </button>
            </div>`;
            return;
        }

        container.innerHTML = AppState.groups.map(g => `
            <div class="group-card" onclick="viewGroup('${g.groupId}')">
                <div class="group-header">
                    <div class="group-avatar"><i class="fas fa-users"></i></div>
                    <div class="group-info">
                        <h3>${g.groupName}</h3>
                        <div class="group-members-count">${(g.members || []).length} members</div>
                    </div>
                </div>
                <div class="group-stats">
                    <div class="group-stat">
                        <div class="group-stat-value">${g.totalExpenses}</div>
                        <div class="group-stat-label">Expenses</div>
                    </div>
                    <div class="group-stat">
                        <div class="group-stat-value">${DataHelpers.formatCurrency(g.totalAmount)}</div>
                        <div class="group-stat-label">Total</div>
                    </div>
                    <div class="group-stat">
                        <div class="group-stat-value">${(g.members || []).length}</div>
                        <div class="group-stat-label">Members</div>
                    </div>
                </div>
            </div>`).join('');
    } catch (err) {
        showToast('Failed to load groups: ' + err.message, 'error');
    }
}

async function viewGroup(groupId) {
    AppState.currentGroupId = groupId;
    try {
        const group = await GroupAPI.get(groupId);
        document.getElementById('group-details-title').textContent = group.groupName;

        // Show/hide Edit Members button (creator only)
        const editBtn = document.getElementById('edit-group-members-btn');
        editBtn.style.display = group.createdById === AppState.currentUser.userId ? 'inline-flex' : 'none';

        renderGroupMembers(group);
        await loadGroupExpensesTab(groupId);
        await loadGroupBalancesTab(groupId);

        showModal('group-details-modal');
    } catch (err) {
        showToast('Failed to load group: ' + err.message, 'error');
    }
}

function renderGroupMembers(group) {
    const container = document.getElementById('group-members-list');
    container.innerHTML = (group.members || []).map(m => `
        <div class="member-item">
            <div class="member-avatar">${DataHelpers.getUserInitials(m.name)}</div>
            <div class="member-info">
                <div class="member-name">${m.name}</div>
                <div class="member-email">${m.email}</div>
            </div>
            ${m.isCreator ? '<span class="member-role">Creator</span>' : ''}
        </div>`).join('');
}

async function loadGroupExpensesTab(groupId) {
    const container = document.getElementById('group-expenses-list');
    try {
        const expenses = await GroupAPI.expenses(groupId);
        if (!expenses.length) {
            container.innerHTML = '<div class="empty-state"><p>No expenses in this group yet</p></div>';
            return;
        }
        container.innerHTML = expenses.map(e => `
            <div class="expense-item">
                <div class="expense-icon" style="background-color:#e0e7ff"><i class="fas fa-receipt"></i></div>
                <div class="expense-details">
                    <div class="expense-title">${e.description}</div>
                    <div class="expense-meta">
                        <span>${DataHelpers.formatDate(e.dateOfExpense)}</span>
                        <span>• Paid by ${e.createdByName}</span>
                    </div>
                </div>
                <div class="expense-amount">${DataHelpers.formatCurrency(e.totalAmount)}</div>
            </div>`).join('');
    } catch (err) {
        container.innerHTML = '<div class="empty-state"><p>Failed to load expenses</p></div>';
    }
}

async function loadGroupBalancesTab(groupId) {
    const container = document.getElementById('group-balances-list');
    try {
        const balances = await GroupAPI.balances(groupId);
        if (!balances.length) {
            container.innerHTML = '<div class="empty-state"><p>All settled up!</p></div>';
            return;
        }
        container.innerHTML = balances.map(b => `
            <div class="balance-item">
                <div class="balance-info">
                    <div class="balance-avatar">${DataHelpers.getUserInitials(b.otherUserName)}</div>
                    <div>
                        <div class="balance-name">${b.otherUserName}</div>
                        <div class="balance-text">${b.amount > 0 ? 'owes you' : 'you owe'}</div>
                    </div>
                </div>
                <div class="balance-amount ${b.amount > 0 ? 'positive' : 'negative'}">
                    ${DataHelpers.formatCurrency(Math.abs(b.amount))}
                </div>
            </div>`).join('');
    } catch (err) {
        container.innerHTML = '<div class="empty-state"><p>Failed to load balances</p></div>';
    }
}

// ===== Expenses =====
async function loadExpenses() {
    const activeTab = document.querySelector('#expenses-page .tab-btn.active')?.dataset.tab || 'all-expenses';
    const groupFilter = document.getElementById('expense-group-filter').value;
    const categoryFilter = document.getElementById('expense-category-filter').value;
    const search = document.getElementById('expense-search').value;

    const params = {};
    if (activeTab === 'personal-expenses') params.type = 'personal';
    else if (activeTab === 'group-expenses') params.type = 'group';
    if (groupFilter) params.groupId = groupFilter;
    if (categoryFilter) params.categoryId = categoryFilter;

    try {
        let expenses = await ExpenseAPI.list(params);
        if (search) expenses = expenses.filter(e => e.description.toLowerCase().includes(search.toLowerCase()));
        renderExpenses(expenses);
    } catch (err) {
        showToast('Failed to load expenses: ' + err.message, 'error');
    }
}

function renderExpenses(expenses) {
    const container = document.getElementById('expenses-container');
    const userId = AppState.currentUser.userId;

    if (!expenses.length) {
        container.innerHTML = `
        <div class="empty-state">
            <i class="fas fa-receipt"></i>
            <h3>No expenses found</h3>
            <p>Add your first expense to get started</p>
        </div>`;
        return;
    }

    container.innerHTML = expenses.map(e => {
        const isCreator = e.createdById === userId;
        return `
        <div class="expense-card">
            <div class="expense-card-header">
                <div class="expense-card-info">
                    <div class="expense-card-title">${e.description}</div>
                    <div class="expense-card-meta">
                        <span>${e.categoryName}</span>
                        <span>• ${DataHelpers.formatDate(e.dateOfExpense)}</span>
                        <span>• Paid by ${e.createdByName}</span>
                    </div>
                </div>
                <div class="expense-card-amount">${DataHelpers.formatCurrency(e.totalAmount)}</div>
            </div>
            <div class="expense-card-footer">
                <div>
                    ${e.groupName
                        ? `<span class="expense-badge badge-group"><i class="fas fa-users"></i> ${e.groupName}</span>`
                        : '<span class="expense-badge badge-personal"><i class="fas fa-user"></i> Personal</span>'}
                </div>
                <div class="expense-actions">
                    ${isCreator ? `
                    <button class="icon-btn" onclick="editExpense(${e.expenseId})" title="Edit"><i class="fas fa-edit"></i></button>
                    <button class="icon-btn" onclick="deleteExpense(${e.expenseId})" title="Delete"><i class="fas fa-trash"></i></button>
                    ` : ''}
                    <button class="icon-btn" onclick="viewExpenseDetails(${e.expenseId})" title="View"><i class="fas fa-eye"></i></button>
                </div>
            </div>
        </div>`;
    }).join('');
}

async function populateExpenseFilters() {
    try {
        const groups = await GroupAPI.list();
        AppState.groups = groups;
        const categories = await CategoryAPI.list();
        AppState.categories = categories;

        const gf = document.getElementById('expense-group-filter');
        gf.innerHTML = '<option value="">All Groups</option>' +
            groups.map(g => `<option value="${g.groupId}">${g.groupName}</option>`).join('');

        const cf = document.getElementById('expense-category-filter');
        cf.innerHTML = '<option value="">All Categories</option>' +
            categories.map(c => `<option value="${c.categoryId}">${c.categoryName}</option>`).join('');
    } catch (err) { /* ignore */ }
}

async function viewExpenseDetails(expenseId) {
    try {
        const e = await ExpenseAPI.get(expenseId);
        const parts = (e.participants || []).map(p =>
            `  ${p.userName}: owed ${DataHelpers.formatCurrency(p.owedAmount)}, paid ${DataHelpers.formatCurrency(p.paidAmount)}`
        ).join('\n');
        alert(`${e.description}\nAmount: ${DataHelpers.formatCurrency(e.totalAmount)}\nCategory: ${e.categoryName}\n${e.groupName ? 'Group: ' + e.groupName : 'Personal'}\nDate: ${DataHelpers.formatDate(e.dateOfExpense)}\n\nParticipants:\n${parts}`);
    } catch (err) {
        showToast('Failed to load expense: ' + err.message, 'error');
    }
}

async function editExpense(expenseId) {
    try {
        const e = await ExpenseAPI.get(expenseId);
        AppState.currentExpenseId = expenseId;
        document.getElementById('expense-modal-title').textContent = 'Edit Expense';
        document.getElementById('expense-description').value = e.description;
        document.getElementById('expense-amount').value = e.totalAmount;
        document.getElementById('expense-category').value = e.categoryId;
        document.getElementById('expense-date').value = e.dateOfExpense;
        document.getElementById('expense-group').value = e.groupId || '';
        if (e.groupId) {
            document.getElementById('group-expense-options').classList.remove('hidden');
            loadExpenseGroupOptions(e.groupId, e);
        }
        showModal('expense-modal');
    } catch (err) {
        showToast('Failed to load expense: ' + err.message, 'error');
    }
}

async function deleteExpense(expenseId) {
    if (!confirm('Are you sure you want to delete this expense?')) return;
    try {
        await ExpenseAPI.delete(expenseId);
        showToast('Expense deleted', 'success');
        loadExpenses();
    } catch (err) {
        showToast('Failed to delete: ' + err.message, 'error');
    }
}

// ===== Settlements =====
async function loadSettlements() {
    const activeTab = document.querySelector('#settlements-page .tab-btn.active')?.dataset.tab || 'pending-settlements';
    let status = null;
    if (activeTab === 'pending-settlements') status = 'PENDING';
    else if (activeTab === 'confirmed-settlements') status = 'CONFIRMED';
    else if (activeTab === 'rejected-settlements') status = 'REJECTED';

    try {
        const settlements = await SettlementAPI.list(status);
        renderSettlements(settlements);
    } catch (err) {
        showToast('Failed to load settlements: ' + err.message, 'error');
    }
}

function renderSettlements(settlements) {
    const container = document.getElementById('settlements-container');
    const userId = AppState.currentUser.userId;

    if (!settlements.length) {
        container.innerHTML = `
        <div class="empty-state">
            <i class="fas fa-handshake"></i>
            <h3>No settlements found</h3>
            <p>All payments are up to date</p>
        </div>`;
        return;
    }

    container.innerHTML = settlements.map(s => {
        const isReceiver = s.receiverId === userId;
        const icon = s.status === 'CONFIRMED' ? 'check' : s.status === 'REJECTED' ? 'times' : 'clock';
        return `
        <div class="settlement-card">
            <div class="settlement-icon ${s.status.toLowerCase()}">
                <i class="fas fa-${icon}"></i>
            </div>
            <div class="settlement-details">
                <div class="settlement-title">${s.payerName} → ${s.receiverName}</div>
                <div class="settlement-meta">
                    ${DataHelpers.formatDate(s.createdAt)}
                    ${s.groupName ? ` • ${s.groupName}` : ' • Global'}
                    • ${s.status.charAt(0) + s.status.slice(1).toLowerCase()}
                </div>
            </div>
            <div class="settlement-amount">${DataHelpers.formatCurrency(s.amount)}</div>
            ${isReceiver && s.status === 'PENDING' ? `
            <div class="settlement-actions">
                <button class="btn btn-sm btn-success" onclick="confirmSettlement(${s.settlementId})">
                    <i class="fas fa-check"></i> Confirm
                </button>
                <button class="btn btn-sm btn-danger" onclick="rejectSettlement(${s.settlementId})">
                    <i class="fas fa-times"></i> Reject
                </button>
            </div>` : ''}
        </div>`;
    }).join('');
}

async function confirmSettlement(id) {
    try {
        await SettlementAPI.confirm(id);
        showToast('Settlement confirmed!', 'success');
        loadSettlements();
        if (AppState.currentPage === 'dashboard') loadDashboard();
    } catch (err) {
        showToast('Failed: ' + err.message, 'error');
    }
}

async function rejectSettlement(id) {
    try {
        await SettlementAPI.reject(id);
        showToast('Settlement rejected', 'warning');
        loadSettlements();
    } catch (err) {
        showToast('Failed: ' + err.message, 'error');
    }
}

// ===== Profile =====
async function loadProfile() {
    try {
        const user = await UserAPI.me();
        AppState.currentUser = { ...AppState.currentUser, ...user };
        document.getElementById('profile-name').value = user.name;
        document.getElementById('profile-email').value = user.email;
        const limit = user.monthlySpendingLimit || 0;
        document.getElementById('monthly-limit').value = limit || '';
        updateLimitDisplay(user);
    } catch (err) {
        showToast('Failed to load profile: ' + err.message, 'error');
    }
}

async function updateLimitDisplay(user) {
    try {
        const dash = await DashboardAPI.get();
        const spending = parseFloat(dash.totalExpensesThisMonth || 0);
        const limit = parseFloat(user?.monthlySpendingLimit || AppState.currentUser.monthlySpendingLimit || 0);
        document.getElementById('current-month-spending').textContent = DataHelpers.formatCurrency(spending);
        if (limit > 0) {
            const pct = Math.min((spending / limit) * 100, 100);
            document.getElementById('limit-progress').style.width = pct + '%';
            document.getElementById('limit-percentage-text').textContent = Math.round(pct) + '% of limit used';
        } else {
            document.getElementById('limit-progress').style.width = '0%';
            document.getElementById('limit-percentage-text').textContent = 'No limit set';
        }
    } catch (err) { /* ignore */ }
}

// ===== Modal Logic =====
function initModals() {
    // Close buttons
    document.querySelectorAll('.modal-close, [data-modal]').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.classList.contains('modal-close')
                ? btn.closest('.modal').id
                : btn.dataset.modal;
            if (id) closeModal(id);
        });
    });

    // Click outside to close
    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) closeModal(modal.id);
        });
    });

    // ── Create Group ──
    document.getElementById('create-group-btn').addEventListener('click', () => {
        AppState.tempMembers = [];
        document.getElementById('create-group-form').reset();
        document.getElementById('members-list').innerHTML = '';
        showModal('create-group-modal');
    });

    document.getElementById('add-member-btn').addEventListener('click', () => {
        const email = document.getElementById('member-email').value.trim();
        if (email && !AppState.tempMembers.includes(email)) {
            AppState.tempMembers.push(email);
            renderTempMembers();
            document.getElementById('member-email').value = '';
        }
    });

    document.getElementById('create-group-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const groupName = document.getElementById('group-name').value;
        try {
            await GroupAPI.create({ groupName, memberEmails: AppState.tempMembers });
            closeModal('create-group-modal');
            showToast('Group created!');
            if (AppState.currentPage === 'groups') loadGroups();
        } catch (err) {
            showToast('Failed: ' + err.message, 'error');
        }
    });

    // ── Expense Modal ──
    document.getElementById('add-expense-btn').addEventListener('click', openExpenseModal);
    document.getElementById('quick-add-expense-btn').addEventListener('click', openExpenseModal);

    document.getElementById('expense-group').addEventListener('change', (e) => {
        const groupId = e.target.value;
        if (groupId) {
            document.getElementById('group-expense-options').classList.remove('hidden');
            loadExpenseGroupOptions(groupId);
        } else {
            document.getElementById('group-expense-options').classList.add('hidden');
        }
    });

    document.querySelectorAll('input[name="split-method"]').forEach(radio => {
        radio.addEventListener('change', () => {
            const isManual = radio.value === 'manual';
            document.getElementById('manual-split-container').classList.toggle('hidden', !isManual);
        });
    });

    document.getElementById('expense-form').addEventListener('submit', handleExpenseSubmit);

    // ── Settlement Modal ──
    document.getElementById('create-settlement-btn').addEventListener('click', async () => {
        const select = document.getElementById('settlement-receiver');
        const groupSelect = document.getElementById('settlement-group');
        try {
            const groups = await GroupAPI.list();
            // Build receiver list from all group members (excluding self)
            const usersMap = new Map();
            for (const g of groups) {
                (g.members || []).forEach(m => {
                    if (m.userId !== AppState.currentUser.userId) usersMap.set(m.userId, m.name);
                });
            }
            select.innerHTML = '<option value="">Select person</option>' +
                [...usersMap.entries()].map(([id, name]) => `<option value="${id}">${name}</option>`).join('');
            groupSelect.innerHTML = '<option value="">Global Settlement</option>' +
                groups.map(g => `<option value="${g.groupId}">${g.groupName}</option>`).join('');
        } catch (err) { /* ignore */ }
        showModal('settlement-modal');
    });

    document.getElementById('settlement-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const receiverId = parseInt(document.getElementById('settlement-receiver').value);
        const amount = parseFloat(document.getElementById('settlement-amount').value);
        const groupId = document.getElementById('settlement-group').value || null;
        if (!receiverId || !amount) { showToast('Fill in all fields', 'error'); return; }
        try {
            await SettlementAPI.create({ receiverId, amount, groupId: groupId ? parseInt(groupId) : null });
            closeModal('settlement-modal');
            showToast('Settlement request sent!');
            if (AppState.currentPage === 'settlements') loadSettlements();
        } catch (err) {
            showToast('Failed: ' + err.message, 'error');
        }
    });

    // ── Profile Forms ──
    document.getElementById('profile-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('profile-name').value;
        const email = document.getElementById('profile-email').value;
        try {
            const user = await UserAPI.update({ name, email });
            AppState.currentUser = { ...AppState.currentUser, ...user };
            document.getElementById('current-user-name').textContent = user.name;
            showToast('Profile updated!');
        } catch (err) {
            showToast('Failed: ' + err.message, 'error');
        }
    });

    document.getElementById('limit-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const limit = parseFloat(document.getElementById('monthly-limit').value) || null;
        try {
            const user = await UserAPI.update({ monthlySpendingLimit: limit });
            AppState.currentUser = { ...AppState.currentUser, ...user };
            showToast('Monthly limit updated!');
            updateLimitDisplay(user);
        } catch (err) {
            showToast('Failed: ' + err.message, 'error');
        }
    });

    document.getElementById('password-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const currentPassword = document.getElementById('current-password').value;
        const newPassword = document.getElementById('new-password').value;
        const confirm = document.getElementById('confirm-new-password').value;
        if (newPassword !== confirm) { showToast('Passwords do not match', 'error'); return; }
        try {
            await UserAPI.changePassword({ currentPassword, newPassword });
            showToast('Password changed!');
            e.target.reset();
        } catch (err) {
            showToast('Failed: ' + err.message, 'error');
        }
    });

    // ── Edit Group Members ──
    document.getElementById('edit-group-members-btn').addEventListener('click', async () => {
        AppState.tempNewMembers = [];
        AppState.tempRemovedMemberIds = [];
        try {
            const group = await GroupAPI.get(AppState.currentGroupId);
            const list = document.getElementById('current-members-list');
            list.innerHTML = (group.members || []).map(m => `
                <div class="member-tag" data-id="${m.userId}">
                    <span>${m.name}</span>
                    ${!m.isCreator ? `<button type="button" class="remove-member" onclick="markRemoveMember(${m.userId}, '${m.name}', this)">×</button>` : '<span class="badge">Creator</span>'}
                </div>`).join('');
            document.getElementById('new-members-list').innerHTML = '';
            showModal('edit-members-modal');
        } catch (err) {
            showToast('Failed to load members: ' + err.message, 'error');
        }
    });

    document.getElementById('add-new-member-btn').addEventListener('click', async () => {
        const email = document.getElementById('new-member-email').value.trim();
        if (!email) return;
        if (!AppState.tempNewMembers.includes(email)) {
            AppState.tempNewMembers.push(email);
            const list = document.getElementById('new-members-list');
            list.innerHTML += `<div class="member-tag"><span>${email}</span></div>`;
            document.getElementById('new-member-email').value = '';
        }
    });

    document.getElementById('edit-members-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            await GroupAPI.updateMembers(AppState.currentGroupId, {
                addEmails: AppState.tempNewMembers,
                removeUserIds: AppState.tempRemovedMemberIds
            });
            closeModal('edit-members-modal');
            showToast('Members updated!');
            viewGroup(AppState.currentGroupId);
        } catch (err) {
            showToast('Failed: ' + err.message, 'error');
        }
    });

    // Leave group
    document.getElementById('leave-group-btn').addEventListener('click', async () => {
        if (!confirm('Are you sure you want to leave this group?')) return;
        try {
            await GroupAPI.leave(AppState.currentGroupId);
            closeModal('group-details-modal');
            showToast('Left the group');
            loadGroups();
        } catch (err) {
            showToast('Failed: ' + err.message, 'error');
        }
    });

    // Expense tabs
    document.querySelectorAll('#expenses-page .tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('#expenses-page .tab-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            loadExpenses();
        });
    });

    // Settlement tabs
    document.querySelectorAll('#settlements-page .tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('#settlements-page .tab-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            loadSettlements();
        });
    });

    // Group details tabs
    document.querySelectorAll('#group-details-modal .tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('#group-details-modal .tab-btn').forEach(b => b.classList.remove('active'));
            document.querySelectorAll('#group-details-modal .tab-content').forEach(c => c.classList.remove('active'));
            btn.classList.add('active');
            document.getElementById(btn.dataset.tab)?.classList.add('active');
        });
    });

    // Expense search/filter
    document.getElementById('expense-search').addEventListener('input', loadExpenses);
    document.getElementById('expense-group-filter').addEventListener('change', loadExpenses);
    document.getElementById('expense-category-filter').addEventListener('change', loadExpenses);
}

function markRemoveMember(userId, name, btn) {
    AppState.tempRemovedMemberIds.push(userId);
    btn.closest('.member-tag').style.opacity = '0.4';
    btn.disabled = true;
    showToast(`${name} will be removed on save`, 'info');
}

function renderTempMembers() {
    const list = document.getElementById('members-list');
    list.innerHTML = AppState.tempMembers.map(email => `
        <div class="member-tag">
            <span>${email}</span>
            <button type="button" onclick="this.parentElement.remove(); AppState.tempMembers = AppState.tempMembers.filter(e => e !== '${email}')">×</button>
        </div>`).join('');
}

// ===== Expense Form =====
async function openExpenseModal() {
    AppState.currentExpenseId = null;
    document.getElementById('expense-modal-title').textContent = 'Add Expense';
    document.getElementById('expense-form').reset();
    document.getElementById('group-expense-options').classList.add('hidden');
    document.getElementById('expense-date').value = new Date().toISOString().split('T')[0];

    try {
        const groups = await GroupAPI.list();
        AppState.groups = groups;
        const groupSelect = document.getElementById('expense-group');
        groupSelect.innerHTML = '<option value="">Personal Expense</option>' +
            groups.map(g => `<option value="${g.groupId}">${g.groupName}</option>`).join('');

        // Populate categories
        if (!AppState.categories.length) AppState.categories = await CategoryAPI.list();
        const catSelect = document.getElementById('expense-category');
        catSelect.innerHTML = '<option value="">Select Category</option>' +
            AppState.categories.map(c => `<option value="${c.categoryId}">${c.categoryName}</option>`).join('');
    } catch (err) { /* ignore */ }

    showModal('expense-modal');
}

async function loadExpenseGroupOptions(groupId, existingExpense = null) {
    try {
        const group = await GroupAPI.get(groupId);
        const members = group.members || [];

        // Payers
        const payersContainer = document.getElementById('payers-container');
        payersContainer.innerHTML = members.map(m => {
            const existing = existingExpense?.participants?.find(p => p.userId === m.userId);
            return `
            <div class="payer-row">
                <label>${m.name}</label>
                <input type="number" step="0.01" min="0" placeholder="0.00"
                    class="form-input payer-input" data-user-id="${m.userId}"
                    value="${existing?.paidAmount || 0}">
            </div>`;
        }).join('');

        // Participants
        const partContainer = document.getElementById('participants-container');
        partContainer.innerHTML = members.map(m => {
            const existing = existingExpense?.participants?.find(p => p.userId === m.userId);
            const checked = existing !== undefined;
            return `
            <label class="checkbox-label">
                <input type="checkbox" class="participant-check" data-user-id="${m.userId}"
                    ${checked || !existingExpense ? 'checked' : ''}>
                <span>${m.name}</span>
            </label>`;
        }).join('');

        // Manual split inputs
        const manualContainer = document.getElementById('manual-split-inputs');
        manualContainer.innerHTML = members.map(m => {
            const existing = existingExpense?.participants?.find(p => p.userId === m.userId);
            return `
            <div class="split-row">
                <label>${m.name}</label>
                <input type="number" step="0.01" min="0" placeholder="0.00"
                    class="form-input manual-split-input" data-user-id="${m.userId}"
                    value="${existing?.owedAmount || 0}">
            </div>`;
        }).join('');
    } catch (err) { /* ignore */ }
}

async function handleExpenseSubmit(e) {
    e.preventDefault();
    const description = document.getElementById('expense-description').value;
    const totalAmount = parseFloat(document.getElementById('expense-amount').value);
    const categoryId = parseInt(document.getElementById('expense-category').value);
    const dateOfExpense = document.getElementById('expense-date').value;
    const groupId = document.getElementById('expense-group').value || null;
    const splitMethod = document.querySelector('input[name="split-method"]:checked')?.value || 'equal';

    // Build participants
    const participants = [];
    if (groupId) {
        const checks = document.querySelectorAll('.participant-check:checked');
        checks.forEach(chk => {
            const uid = parseInt(chk.dataset.userId);
            const paidInput = document.querySelector(`.payer-input[data-user-id="${uid}"]`);
            const manualInput = document.querySelector(`.manual-split-input[data-user-id="${uid}"]`);
            participants.push({
                userId: uid,
                paidAmount: parseFloat(paidInput?.value || 0),
                owedAmount: splitMethod === 'manual' ? parseFloat(manualInput?.value || 0) : null
            });
        });
    } else {
        // Personal expense — current user only
        participants.push({
            userId: AppState.currentUser.userId,
            paidAmount: totalAmount,
            owedAmount: totalAmount
        });
    }

    const payload = { description, totalAmount, categoryId, dateOfExpense, groupId: groupId ? parseInt(groupId) : null, splitMethod, participants };

    try {
        if (AppState.currentExpenseId) {
            await ExpenseAPI.update(AppState.currentExpenseId, payload);
            showToast('Expense updated!');
        } else {
            await ExpenseAPI.create(payload);
            showToast('Expense added!');
        }
        closeModal('expense-modal');
        if (AppState.currentPage === 'expenses') loadExpenses();
        if (AppState.currentPage === 'dashboard') loadDashboard();
    } catch (err) {
        showToast('Failed: ' + err.message, 'error');
    }
}

// ===== Init =====
async function init() {
    const loadingScreen = document.getElementById('loading-screen');
    loadingScreen.style.display = 'flex';

    initAuth();
    initNavigation();
    initModals();

    setTimeout(async () => {
        loadingScreen.style.display = 'none';

        if (Auth.isLoggedIn()) {
            try {
                const user = await UserAPI.me();
                AppState.currentUser = user;
                AppState.isAuthenticated = true;
                showApp();
            } catch (err) {
                // Token expired or invalid — clear and show login
                Auth.clearToken();
                document.getElementById('auth-container').classList.remove('hidden');
            }
        } else {
            document.getElementById('auth-container').classList.remove('hidden');
        }

        // Pre-fetch categories in background
        try {
            AppState.categories = await CategoryAPI.list();
            populateExpenseFilters();
        } catch (e) { /* ignore if not logged in yet */ }
    }, 600);
}

document.addEventListener('DOMContentLoaded', init);
