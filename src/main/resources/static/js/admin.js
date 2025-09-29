function getToken() {
  return localStorage.getItem('token');
}

async function api(path, opts = {}) {
  const token = getToken();
  opts = Object.assign({ credentials: 'same-origin' }, opts);
  opts.headers = Object.assign(opts.headers || {}, { 'Content-Type': 'application/json' });
  if (token) opts.headers['Authorization'] = 'Bearer ' + token;
  const res = await fetch(path, opts);
  if (res.ok) return res.json().catch(() => null);
  const err = new Error('Request failed: ' + res.status);
  err.status = res.status;
  throw err;
}

function createUserItem(user) {
  const el = document.createElement('div');
  el.className = 'user-item';
  const left = document.createElement('div');
  left.className = 'left';
  left.innerHTML = `<strong>${user.fullName}</strong><small>${user.email}</small>`;
  const actions = document.createElement('div');
  actions.className = 'user-actions';

  const isAdmin = (user.roles || []).includes('ROLE_ADMIN');

  const toggleBtn = document.createElement('button');
  toggleBtn.className = 'btn btn-primary';
  toggleBtn.innerText = isAdmin ? 'Demote' : 'Promote';
  toggleBtn.onclick = async () => {
    try {
      if (isAdmin) {
        await api(`/api/admin/users/${user.id}/roles?role=ROLE_ADMIN`, { method: 'DELETE' });
      } else {
        await api(`/api/admin/users/${user.id}/roles?role=ROLE_ADMIN`, { method: 'POST' });
      }
      loadUsers();
    } catch (e) {
      alert('Action failed');
    }
  };

  actions.appendChild(toggleBtn);
  el.appendChild(left);
  el.appendChild(actions);
  return el;
}

async function loadUsers() {
  const list = document.getElementById('userList');
  list.innerHTML = '';
  try {
    // Verify current user and roles first
    const me = await api('/api/auth/me');
    if (!me || !(me.roles || []).includes('ROLE_ADMIN')) {
      list.innerText = 'You are not authorized to view users.';
      return;
    }

    const users = await api('/api/admin/users');
    if (!users || !users.length) {
      list.innerText = 'No users found.';
      return;
    }
    users.forEach(u => list.appendChild(createUserItem(u)));
  } catch (e) {
    if (e && e.status === 401) {
      list.innerText = 'Not authenticated. Please login as admin.';
    } else if (e && e.status === 403) {
      list.innerText = 'Forbidden. You do not have permission to view users.';
    } else {
      list.innerText = 'Server error while loading users.';
    }
  }
}

loadUsers();