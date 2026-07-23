# Smoother Image Loading Implementation Plan

This plan addresses the 0.5s "blank" period and layout jumps when loading images in the chat room.

## User Review Required

> [!IMPORTANT]
> To perfectly eliminate layout jumps, we would eventually need to store image dimensions in the database. For this first iteration, I will focus on visual smoothness and aggressive prefetching to reduce the "blank" time.

## Proposed Changes

### 1. Visual Polish

#### [NEW] [ShimmerModifier.kt](file:///home/eric/StudioProjects/Kotlin-Chat-Software/app/src/main/java/com/example/login_v3/home/Message/UI/Detail/Message_Component/ShimmerModifier.kt)
- Implement a reusable `shimmer()` Modifier extension to provide a smooth, animated loading state.

#### [MODIFY] [Message_Messaging.kt](file:///home/eric/StudioProjects/Kotlin-Chat-Software/app/src/main/java/com/example/login_v3/home/Message/UI/Detail/Message_Messaging.kt)
- Update `ImageMessageContent` and `VideoMessageContent` to use `SubcomposeAsyncImage`.
- Display a shimmer placeholder during the `Loading` state.
- Use a default aspect ratio (e.g., 4:3) for the placeholder to reserve space and reduce layout jumps.

### 2. Prefetching Optimization

#### [MODIFY] [ChatViewModel](file:///home/eric/StudioProjects/Kotlin-Chat-Software/app/src/main/java/com/example/login_v3/home/Message/ViewModel/Detail/Message_Messaging_ViewModel.kt)
- Update `prefetchMedia` to use the same `ImageRequest` configuration (crossfade, scale, etc.) as the UI.
- Increase prefetching priority for the most recent messages.

### 3. State Management

#### [MODIFY] [MessageUiModel.kt](file:///home/eric/StudioProjects/Kotlin-Chat-Software/app/src/main/java/com/example/login_v3/home/Message/UI/Detail/MessageUiModel.kt)
- Add an optional `aspectRatio` property to allow future dimension caching.

## Verification Plan

### Automated Tests
- Build and run the app.
- Check logcat for any Coil-related errors.

### Manual Verification
- Enter a chat room with multiple images.
- Verify that a shimmer effect is visible immediately.
- Verify that the layout is more stable during image loading.
- Test with both network images and cached images.
