module.exports = {
  globDirectory: "dist/",
  globPatterns: [
    "**/*.{html,json,js,css,png,svg,jpg,gif,ico,woff,woff2,ttf,eot}"
  ],
  swDest: "dist/sw.js",
  ignoreURLParametersMatching: [/^utm_/, /^fbclid$/],
  skipWaiting: true,
  clientsClaim: true,
  runtimeCaching: [{
    urlPattern: /\.(?:png|jpg|jpeg|svg|gif)$/,
    handler: "CacheFirst",
    options: {
      cacheName: "images",
      expiration: {
        maxEntries: 60,
        maxAgeSeconds: 30 * 24 * 60 * 60 // 30 days
      }
    }
  }]
};
