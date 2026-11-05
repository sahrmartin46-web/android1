# Android App Files & Component Breakdown

This artifact contains the synchronized source code structure and key implementation details for the Android native application (`app/src/main/java/c/app/sociamediabackend/`).

---

## 1. Entry Point & Networking

### [MainActivity.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/MainActivity.kt)
- Main container activity hosting bottom navigation and fragment switching (Feed, Discover, Inbox, Profile).

### Networking Layer (`data/api/`)
- **[ApiClient.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/data/api/ApiClient.kt)**: Retrofit client configuration with auth interceptors.
- **[ApiService.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/data/api/ApiService.kt)**: Defines API endpoints for posts, live streams, wallet operations, and user profiles.
- **[SessionManager.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/data/api/SessionManager.kt)**: Manages JWT token persistence and user session state in SharedPreferences.
- **[NetworkResult.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/data/api/NetworkResult.kt)**: Sealed class wrapper (`Success`, `Error`, `Loading`) for API responses.

---

## 2. Authentication (`ui/auth/`)
- **[LoginActivity.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/auth/LoginActivity.kt)**: User sign-in screen with Fugen branding.
- **[RegisterActivity.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/auth/RegisterActivity.kt)**: Account registration screen.
- **[AuthViewModel.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/auth/AuthViewModel.kt)**: ViewModel handling auth business logic and state.

---

## 3. Main Feeds & Content (`ui/main/`)
- **[FeedFragment.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/main/FeedFragment.kt)** & **[FeedViewModel.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/main/FeedViewModel.kt)**: Short-form video feed display and pagination.
- **[PostAdapter.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/main/PostAdapter.kt)**: RecyclerView adapter for video posts, comments, likes, and sharing.
- **[DiscoverFragment.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/main/DiscoverFragment.kt)**: Search and trending discovery.
- **[InboxFragment.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/main/InboxFragment.kt)**: Notifications and messages.
- **[ProfileFragment.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/main/ProfileFragment.kt)**: User profile, grid of posts, and follower management.
- **[CreatePostFragment.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/main/CreatePostFragment.kt)**: Post creation and upload workflow.

---

## 4. LIVE Streaming & Financial Ledger (`ui/live/`)
- **[GoLiveStudioActivity.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/live/GoLiveStudioActivity.kt)**: Live broadcasting studio setup and stream start.
- **[LiveGridStudioActivity.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/live/LiveGridStudioActivity.kt)**: Multi-guest grid management for live sessions.
- **[FugenPayoutDashboardActivity.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/live/FugenPayoutDashboardActivity.kt)**: Bank linking, vault balance, and creator payout management.
- **[CreatorWalletActivity.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/live/CreatorWalletActivity.kt)**: Creator treasury dashboard and diamond earnings.
- **[GiftBottomSheetDialogFragment.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/live/GiftBottomSheetDialogFragment.kt)**: Virtual gifting UI during live streams.

---

## 5. Sound & Camera Creation (`ui/sound/`)
- **[CameraStudioActivity.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/sound/CameraStudioActivity.kt)**: Video effects studio, recording controls, and trending music selection.
- **[DuetStudioActivity.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/sound/DuetStudioActivity.kt)**: Duet reaction recording.
- **[BuyCoinsActivity.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/sound/BuyCoinsActivity.kt)**: In-app coin purchase and wallet top-up.

---

## 6. Theming (`ui/theme/`)
- **[FugenTokens.kt](file:///C:/Users/martin/OneDrive/Desktop/social%20media%20backend/app/src/main/java/c/app/sociamediabackend/ui/theme/FugenTokens.kt)**: Defines brand colors, neon accents, and dual-gradients.
