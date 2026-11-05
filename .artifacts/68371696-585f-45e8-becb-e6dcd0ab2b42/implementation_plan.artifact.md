# Implementation Plan - Fugen LIVE Socket.IO Integration for Real-Time Chat & Join Notifications

Integrate Socket.IO client into the Android app to connect with the Node.js backend real-time event system, enabling real-time chat comments, viewer join notifications, likes, and gift animations during LIVE streaming sessions (`GoLiveStudioActivity` and `LiveViewerActivity`).

## User Review Required

> [!IMPORTANT]
> This feature adds `io.socket:socket.io-client:2.1.0` dependency to Gradle and wires up automatic socket connection, joining room (`live.join`), sending comments (`live.comment`), and receiving real-time events (`live:comment_created`, `live:participant_joined`, `live:gift_sent`).

## Open Questions

- None. The backend Socket.IO events and contracts are fully defined in `src/lib/socket.js` and `docs/realtime-events.md`.

## Proposed Changes

### Android App Module (`app/`)

#### [MODIFY] [build.gradle.kts](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/build.gradle.kts)
- Add `io.socket:socket.io-client:2.1.0` dependency (excluding `org.json:json` if needed to prevent conflict with Android SDK).

#### [NEW] [LiveSocketManager.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/data/api/LiveSocketManager.kt)
- Create a singleton/manager class to handle Socket.IO connection, authentication with JWT token, joining live rooms, emitting comments/likes/gifts, and exposing callbacks for incoming real-time events.

#### [MODIFY] [GoLiveStudioActivity.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/live/GoLiveStudioActivity.kt)
- Integrate `LiveSocketManager` to connect upon stream creation, broadcast host events, display incoming viewer join notifications in the notification ticker, and receive real-time chat & gift messages.

#### [MODIFY] [LiveViewerActivity.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/live/LiveViewerActivity.kt)
- Integrate `LiveSocketManager` to join the live room as a viewer, send chat comments, send likes/gifts, and display real-time chat and join notifications.

## Verification Plan

### Automated Tests
- Build the project using `gradle_build` to verify Gradle dependency resolution and Kotlin compilation.

### Manual Verification
- Deploy and launch the Android app to test live broadcasting and viewer socket connection.
