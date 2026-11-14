"use strict";
// Prosty licznik w oknie przesuwnym, per IP. Wystarczający dla pojedynczej instancji.
function createRateLimiter({ windowMs = 60_000, max = 30 } = {}) {
  const hits = new Map(); // ip -> number[] (timestampy)
  function allow(ip) {
    const now = Date.now();
    const arr = (hits.get(ip) || []).filter((t) => now - t < windowMs);
    if (arr.length >= max) {
      hits.set(ip, arr);
      return { allowed: false, retryAfterSeconds: Math.ceil((windowMs - (now - arr[0])) / 1000) };
    }
    arr.push(now);
    hits.set(ip, arr);
    return { allowed: true };
  }
  // porządkowanie mapy, żeby nie puchła
  setInterval(() => {
    const now = Date.now();
    for (const [ip, arr] of hits) {
      const fresh = arr.filter((t) => now - t < windowMs);
      if (fresh.length) hits.set(ip, fresh); else hits.delete(ip);
    }
  }, windowMs).unref();
  return { allow };
}
module.exports = { createRateLimiter };
