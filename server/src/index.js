const http = require('http');
const path = require('path');
const zlib = require('zlib');
const express = require('express');
const cors = require('cors');
const { WebSocketServer } = require('ws');
const db = require('./db');
const { seedIfEmpty, ensureBootstrapAdminPassword } = require('./seed');
const { createApiRouter } = require('./routes');
const { refreshGoogleJwksIfPossible } = require('./auth');

const PORT = Number(process.env.PORT || 3000);
const HOST = '0.0.0.0';

function createApp(broadcastSync = () => {}, sseClients = new Set(), getStateVersion = null) {
  const app = express();
  app.use(cors());
  app.use(express.json({ limit: '5mb' }));

  app.use((req, res, next) => {
    if (req.path === '/api/events') return next();
    const acceptEnc = String(req.headers['accept-encoding'] || '');
    if (!acceptEnc.includes('gzip')) return next();
    const origSend = res.send.bind(res);
    res.send = (body) => {
      if (res.headersSent || res.getHeader('Content-Encoding') || body == null) {
        return origSend(body);
      }
      const isStr = typeof body === 'string';
      const buf = Buffer.isBuffer(body) ? body : Buffer.from(isStr ? body : JSON.stringify(body), 'utf8');
      if (buf.length < 1024) return origSend(body);
      if (!res.getHeader('Content-Type')) {
        res.setHeader('Content-Type', isStr ? 'text/html; charset=utf-8' : 'application/json; charset=utf-8');
      }
      zlib.gzip(buf, { level: zlib.constants.Z_BEST_SPEED }, (err, compressed) => {
        if (err || res.headersSent) return origSend(body);
        res.setHeader('Content-Encoding', 'gzip');
        res.setHeader('Vary', 'Accept-Encoding');
        res.removeHeader('Content-Length');
        return origSend(compressed);
      });
      return res;
    };
    return next();
  });

  const publicDir = path.join(__dirname, 'public');
  app.use(
    express.static(publicDir, {
      maxAge: '1h',
      setHeaders: (res, filePath) => {
        if (
          filePath.endsWith('sw.js') ||
          filePath.endsWith('index.html') ||
          filePath.endsWith('app.js') ||
          filePath.endsWith('styles.css')
        ) {
          res.setHeader('Cache-Control', 'no-cache');
        }
      }
    })
  );

  app.get('/health', async (_req, res) => {
    try {
      await db.pool.query('SELECT 1');
      return res.json({ status: 'ok', service: 'bazmo-server', db: 'connected', ts: Date.now() });
    } catch (err) {
      return res.status(503).json({ status: 'error', service: 'bazmo-server', db: err.message });
    }
  });

  let localVersion = 1;
  const wrappedBroadcast = (topics = ['all']) => {
    localVersion += 1;
    broadcastSync(topics);
  };
  const resolveVersion = typeof getStateVersion === 'function' ? getStateVersion : () => localVersion;

  app.use('/api', createApiRouter(wrappedBroadcast, sseClients, resolveVersion));
  app.use('/api', (_req, res) =>
    res.status(404).json({ error: 'این سرویس روی سرور یافت نشد (۴۰۴). لطفاً کد سرور را بروزرسانی کنید.' })
  );

  app.use((err, _req, res, _next) => {
    console.error('[API Error]', err);
    if (res.headersSent) return;
    return res.status(500).json({ error: err?.message || 'خطای داخلی سرور' });
  });

  return app;
}

async function initDatabaseWithRetry(retries = 10, delayMs = 3000) {
  for (let attempt = 1; attempt <= retries; attempt++) {
    try {
      await db.initSchema();
      await ensureBootstrapAdminPassword();
      await seedIfEmpty('system_admin');
      console.log('[DB] PostgreSQL schema initialized, admin credentials ensured, and seed checked.');
      return;
    } catch (err) {
      console.warn(`[DB] Init attempt ${attempt}/${retries} failed: ${err.message}`);
      if (attempt === retries) throw err;
      await new Promise((r) => setTimeout(r, delayMs));
    }
  }
}

async function start() {
  const clients = new Set();
  const sseClients = new Set();
  let stateVersion = 1;
  const broadcastSync = (topics = ['all']) => {
    stateVersion += 1;
    const payload = JSON.stringify({ type: 'sync', topics, version: stateVersion, ts: Date.now() });
    for (const ws of clients) {
      if (ws.readyState === 1) {
        try {
          ws.send(payload);
        } catch {
          // ignore closed socket
        }
      }
    }
    for (const res of sseClients) {
      try {
        res.write(`event: sync\ndata: ${payload}\n\n`);
      } catch {
        sseClients.delete(res);
      }
    }
  };

  const app = createApp(broadcastSync, sseClients, () => stateVersion);
  const server = http.createServer(app);
  const wss = new WebSocketServer({ server, path: '/ws' });

  wss.on('connection', (ws) => {
    clients.add(ws);
    ws.isAlive = true;
    ws.on('pong', () => {
      ws.isAlive = true;
    });
    ws.on('close', () => clients.delete(ws));
    ws.on('error', () => clients.delete(ws));
    try {
      ws.send(JSON.stringify({ type: 'connected', version: stateVersion, ts: Date.now() }));
    } catch {
      // ignore
    }
  });

  const pingInterval = setInterval(() => {
    for (const ws of clients) {
      if (!ws.isAlive) {
        clients.delete(ws);
        ws.terminate();
        continue;
      }
      ws.isAlive = false;
      ws.ping();
    }
    for (const res of sseClients) {
      try {
        res.write(': keepalive\n\n');
      } catch {
        sseClients.delete(res);
      }
    }
  }, 20000);
  pingInterval.unref();

  await initDatabaseWithRetry();
  refreshGoogleJwksIfPossible(true).catch(() => {});

  server.listen(PORT, HOST, () => {
    console.log(`[Server] Bazmo server listening on http://${HOST}:${PORT}`);
  });
}

if (require.main === module) {
  start().catch((err) => {
    console.error('[Fatal] Could not start server:', err);
    process.exit(1);
  });
}

module.exports = { createApp };
