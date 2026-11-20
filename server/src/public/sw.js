const DB_NAME = 'bazmo_sw_db';
const DB_VERSION = 1;
const STORE_META = 'meta';

function openSwDb() {
  return new Promise((resolve, reject) => {
    const req = indexedDB.open(DB_NAME, DB_VERSION);
    req.onupgradeneeded = () => {
      const db = req.result;
      if (!db.objectStoreNames.contains(STORE_META)) {
        db.createObjectStore(STORE_META);
      }
    };
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);
  });
}

async function idbGet(key) {
  const db = await openSwDb();
  return new Promise((resolve) => {
    const tx = db.transaction(STORE_META, 'readonly');
    const req = tx.objectStore(STORE_META).get(key);
    req.onsuccess = () => resolve(req.result ?? null);
    req.onerror = () => resolve(null);
  });
}

async function idbSet(key, value) {
  const db = await openSwDb();
  return new Promise((resolve) => {
    const tx = db.transaction(STORE_META, 'readwrite');
    tx.objectStore(STORE_META).put(value, key);
    tx.oncomplete = () => resolve(true);
    tx.onerror = () => resolve(false);
  });
}

async function idbDelete(key) {
  const db = await openSwDb();
  return new Promise((resolve) => {
    const tx = db.transaction(STORE_META, 'readwrite');
    tx.objectStore(STORE_META).delete(key);
    tx.oncomplete = () => resolve(true);
    tx.onerror = () => resolve(false);
  });
}

async function checkBackgroundNotifications(triggerSource = 'bg') {
  try {
    const session = await idbGet('session');
    if (!session || !session.token || !session.uid) return;
    if (Notification.permission !== 'granted') return;

    const params = new URLSearchParams({
      province: session.province || 'تهران',
      city: session.city || 'تهران',
      region: session.region || 'همه مناطق'
    });
    const res = await fetch(`/api/notifications/unread?${params.toString()}`, {
      headers: { Authorization: `Bearer ${session.token}` },
      cache: 'no-store'
    });
    if (!res.ok) return;

    const data = await res.json();
    const list = Array.isArray(data.notifications) ? data.notifications : [];
    if (!list.length) return;

    const notified = new Set((await idbGet('notified_ids')) || []);
    const unseen = list.filter((n) => n && n.id && !notified.has(n.id));
    if (!unseen.length) return;

    const toShow = unseen.slice(0, 3);
    for (const n of unseen) {
      notified.add(n.id);
    }
    const trimmed = Array.from(notified).slice(-150);
    await idbSet('notified_ids', trimmed);

    for (const n of toShow) {
      await self.registration.showNotification(n.title || 'اعلان جدید بازمو', {
        body: n.body || '',
        tag: `bazmo_${n.id}`,
        renotify: true,
        dir: 'rtl',
        lang: 'fa',
        data: { notifId: n.id, triggerSource }
      });
    }
  } catch {
    // Ignore transient network errors while offline in background
  }
}

self.addEventListener('install', () => {
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(self.clients.claim());
});

self.addEventListener('message', (event) => {
  const msg = event.data || {};
  if (msg.type === 'SYNC_SESSION') {
    event.waitUntil(
      (async () => {
        if (msg.session && msg.session.token) {
          await idbSet('session', msg.session);
        } else {
          await idbDelete('session');
        }
        if (Array.isArray(msg.markNotifiedIds) && msg.markNotifiedIds.length > 0) {
          const prev = new Set((await idbGet('notified_ids')) || []);
          for (const id of msg.markNotifiedIds) prev.add(id);
          await idbSet('notified_ids', Array.from(prev).slice(-150));
        }
      })()
    );
  } else if (msg.type === 'CLEAR_SESSION') {
    event.waitUntil(idbDelete('session'));
  } else if (msg.type === 'CHECK_NOTIFS') {
    event.waitUntil(checkBackgroundNotifications('message'));
  }
});

self.addEventListener('periodicsync', (event) => {
  if (event.tag === 'bazmo-notif-sync') {
    event.waitUntil(checkBackgroundNotifications('periodicsync'));
  }
});

self.addEventListener('sync', (event) => {
  if (event.tag === 'bazmo-notif-sync') {
    event.waitUntil(checkBackgroundNotifications('sync'));
  }
});

self.addEventListener('push', (event) => {
  let payload = null;
  try {
    payload = event.data ? event.data.json() : null;
  } catch {
    payload = event.data ? { title: 'اعلان جدید بازمو', body: event.data.text() } : null;
  }
  if (payload && (payload.title || payload.body)) {
    event.waitUntil(
      self.registration.showNotification(payload.title || 'اعلان جدید بازمو', {
        body: payload.body || '',
        tag: payload.id ? `bazmo_${payload.id}` : 'bazmo_push',
        dir: 'rtl',
        lang: 'fa'
      })
    );
  } else {
    event.waitUntil(checkBackgroundNotifications('push'));
  }
});

self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  event.waitUntil(
    self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then((clients) => {
      for (const client of clients) {
        if ('focus' in client) {
          client.postMessage({ type: 'OPEN_NOTIFICATIONS' });
          return client.focus();
        }
      }
      if (self.clients.openWindow) {
        return self.clients.openWindow('/?openNotifications=1');
      }
      return null;
    })
  );
});
