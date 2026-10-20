# Just Player integration note

StreamHub's player interaction model is adapted from the public-domain Just (Video) Player project by moneytoo:

- Source: https://github.com/moneytoo/Player
- Upstream license: The Unlicense
- Fetched upstream revision: 2026-10-20 (shallow clone used during integration)

The upstream project is a complete standalone video-player application rather than a drop-in Android library. StreamHub keeps its own channel, favorites, Hilt, and navigation layers, and uses Media3's native `PlayerView` transport surface with the same Just Player interaction principles: fit/crop-safe rendering, native seeking, track/settings controls, PiP, channel switching, and touch-friendly transport controls. No upstream application identity, package, or unrelated file-browser screens are copied into StreamHub.
