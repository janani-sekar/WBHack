# guest-i-mate UI preview (static, no model)

The real guest-i-mate web UI (`hospitality/ui/index.html`) with the local coach API swapped for sample responses (`demo-mock.js`, `fixtures.js`). No Qwen, no backend, nothing leaves the browser. Used only for a public click-through link; the real app runs Qwen locally (see the root README).

Deploy: any static host with `web-demo/` as the root (Vercel: Root Directory = `web-demo`, Framework = Other). Preview locally: `python3 -m http.server -d web-demo 8090`.
