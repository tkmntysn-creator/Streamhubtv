# StreamHub TV — design and performance direction

## Direction

The new visual direction is **Midnight Broadcast**: a dark editorial live-TV interface with a compact cinematic spotlight, cobalt-blue interaction states, cyan signal accents, and restrained glass surfaces. The existing Liquid Glass bottom bar remains the signature navigation element. The rest of the screen should stay quieter so live content, labels, and channel logos carry the hierarchy.

Apple's Tab Bars guidance supports keeping a tab bar visible while moving between sections, using short labels, and reserving the bar for navigation rather than actions. The iOS tab bar is described as floating above content on a Liquid Glass background. Source: https://developer.apple.com/design/human-interface-guidelines/tab-bars

Apple's Motion guidance recommends purposeful, brief motion and avoiding gratuitous animation on frequent interactions. Source: https://developer.apple.com/design/human-interface-guidelines/motion

Android's Compose list guidance recommends stable item keys, content types for composition reuse, and avoiding large groups of elements emitted as a single lazy item. Source: https://developer.android.com/develop/ui/compose/lists

Android's Baseline Profile guidance notes that baseline profiles improve first-launch execution and reduce interaction jank. Source: https://developer.android.com/develop/ui/compose/performance/baseline-profiles

## Implementation decisions

- Keep the Liquid Glass bar and improve only its selected-item animation.
- Replace the 420dp mostly-empty Hero with a compact 288–300dp spotlight card.
- Use a single visual focus: one featured channel, one primary action, and one secondary action.
- Make channel rows denser and quieter; use stable `id` keys and `contentType` values.
- Disable unnecessary image crossfades and use Coil's memory/disk cache.
- Prefer Room cache immediately when available; only fetch from the network when there is no cache or the user explicitly refreshes.
- Keep the external Channels repository intact because its stream rights and availability are not verified here. Provide a clean JSON starter template instead of overwriting the user's catalogue.

## Player direction

Apple's Playing Video guidance recommends using familiar system-player behavior when building a custom player, preserving the video's original aspect ratio, keeping overlays limited, starting playback immediately, and showing only a minimal black loading state when needed. Source: https://developer.apple.com/design/human-interface-guidelines/playing-video

Apple's tvOS playback guidance places common actions in a transport bar, supports contextual custom actions and content tabs, and favors quick access to favorite/PiP/preferences without covering the media. Source: https://developer.apple.com/documentation/avkit/customizing-the-tvos-playback-experience

The new player direction is **Signal Glass Player**: a wide video-first surface, a single centered play/pause control, a compact translucent top identity strip, a bottom transport dock with favorite/source/fullscreen/channel actions, and a channel-switcher sheet for live flipping. Controls auto-hide quickly, use short fades, and do not add decorative overlays over the stream.
