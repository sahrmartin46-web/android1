# Fugen App Codebase Synchronization & Architecture Overview

This document provides a comprehensive synchronization and structural breakdown of the entire **Fugen** short-form video and real-time interactive LIVE streaming application codebase (Android Native + Flutter + Node.js Backend).

---

## 1. Project Structure & Directory Layout

```
social media backend/
├── .artifacts/                      # Agent architecture & sync artifacts
├── apps/
│   └── mobile/                      # Flutter mobile client
│       └── lib/
│           └── features/
│               └── video_creation/  # Camera, filters, and review (e.g., fugen_camera_screen.dart)
├── app/                             # Android native Kotlin application
│   ├── build.gradle.kts
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/c/app/sociamediabackend/
│           │   ├── data/            # API client, models, repositories, session manager
│           │   ├── ui/
│           │   │   ├── analytics/   # Analytics activities
│           │   │   ├── auth/        # Login, Register, ViewModels
│           │   │   ├── live/        # Go Live Studio, Live Grid, Payouts, Wallet, Gifts
│           │   │   ├── main/        # Feed, Discover, Inbox, Profile, Create Post
│           │   │   ├── settings/    # Account management, privacy, guidelines
│           │   │   ├── sound/       # Camera Studio, Duet, Buy Coins, Post Preview
│           │   │   └── theme/       # Fugen Design Tokens & Gradients
│           │   └── MainActivity.kt
│           └── res/                 # Layout XMLs, drawables, colors, strings, mipmaps
├── src/                             # Node.js backend services & controllers
│   ├── controllers/                 # Live streaming & business logic controllers
│   └── routes/                      # API routing definitions
├── docker/                          # Docker Compose configuration (PostgreSQL, Redis, LiveKit)
└── docs/                            # Documentation (Architecture, API, LiveKit, Wallet, Security)
```

---

## 2. Android Application Modules (`app/src/main`)

### 🔑 Core Authentication & State
- [`ApiClient.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/data/api/ApiClient.kt) & [`ApiService.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/data/api/ApiService.kt): Retrofit networking layer for communicating with the Fugen backend.
- [`SessionManager.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/data/api/SessionManager.kt): Manages user JWT tokens and session state.
- [`AuthViewModel.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/auth/AuthViewModel.kt), [`LoginActivity.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/auth/LoginActivity.kt), [`RegisterActivity.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/auth/RegisterActivity.kt).

### 🎬 Main Feeds & Content (`ui/main`)
- [`FeedFragment.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/main/FeedFragment.kt) & [`FeedViewModel.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/main/FeedViewModel.kt): Short-form video feed ranked by the Fugen FeedScore algorithm.
- [`DiscoverFragment.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/main/DiscoverFragment.kt), [`InboxFragment.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/main/InboxFragment.kt), [`ProfileFragment.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/main/ProfileFragment.kt).
- [`CreatePostFragment.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/main/CreatePostFragment.kt) & [`PostAdapter.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/main/PostAdapter.kt).

### 🔴 LIVE Streaming & Financial Ledger (`ui/live`)
- [`GoLiveStudioActivity.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/live/GoLiveStudioActivity.kt) & [`LiveGridStudioActivity.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/live/LiveGridStudioActivity.kt): Live broadcasting and multi-guest grid management.
- [`CreatorWalletActivity.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/live/CreatorWalletActivity.kt) & [`FugenPayoutDashboardActivity.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/live/FugenPayoutDashboardActivity.kt): Treasury vault, gifting ledger, and bank account linking.
- [`GiftBottomSheetDialogFragment.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/live/GiftBottomSheetDialogFragment.kt) & [`LiveViewerActivity.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/live/LiveViewerActivity.kt).

### ✨ Video & Sound Creation Studio (`ui/sound`)
- [`CameraStudioActivity.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/sound/CameraStudioActivity.kt): Video effects studio and camera recording.
- [`DuetStudioActivity.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/sound/DuetStudioActivity.kt), [`BuyCoinsActivity.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/sound/BuyCoinsActivity.kt), [`PostPreviewActivity.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/sound/PostPreviewActivity.kt).

### 🎨 Theming & Styling
- [`FugenTokens.kt`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/theme/FugenTokens.kt): Dual-gradients and brand accent colors.
- UI Layouts in `app/src/main/res/layout/` (e.g., [`activity_fugen_payout_dashboard.xml`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/res/layout/activity_fugen_payout_dashboard.xml), [`activity_login.xml`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/res/layout/activity_login.xml), etc.).

---

## 3. Flutter Mobile Client (`apps/mobile/`)
- [`fugen_camera_screen.dart`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/apps/mobile/lib/features/video_creation/fugen_camera_screen.dart): Cross-platform camera capture screen with preset color filters (`FugenEffect`: Natural, Warm, Cool, Vintage, Mono, Glow, Soft).

---

## 4. Backend & Services (`src/`)
- [`liveController.js`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/src/controllers/liveController.js): Live streaming management, token generation, and stream studio endpoints.
- [`screenRoutes.js`](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/src/routes/screenRoutes.js): Screen rendering and data routing.

---

## 5. Architecture & Documentation (`docs/`)
- [architecture.md](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/docs/architecture.md): Two-plane architecture (Metadata/Financial Plane vs Real-Time Media Plane).
- [api.md](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/docs/api.md), [livekit.md](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/docs/livekit.md), [realtime-events.md](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/docs/realtime-events.md), [wallet.md](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/docs/wallet.md), [security.md](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/docs/security.md), [deployment.md](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/docs/deployment.md).
