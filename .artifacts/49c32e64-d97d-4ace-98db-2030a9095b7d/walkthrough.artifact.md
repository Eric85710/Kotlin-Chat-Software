# Walkthrough - Fixing Image Flickering on Reactions

I have implemented a series of stability and performance fixes to resolve the issue where images reload or flicker when an emoji reaction is added.

## Changes Made

### 1. Stable Callbacks in UI
- **Updated [Message_Messaging.kt](file:///home/eric/StudioProjects/Kotlin-Chat-Software/app/src/main/java/com/example/login_v3/home/Message/UI/Detail/Message_Messaging.kt)**:
    - Wrapped `onReplyClick`, `onRowClick`, and `onReactionClick` in `remember(viewModel)`.
    - **Why?**: Previously, these lambdas were recreated on every recomposition. Since every message item in the list receives these lambdas, Compose thought every item had changed, triggering a full list refresh. By stabilizing them, Compose can now correctly skip items that didn't actually change.

### 2. Optimized Image Rendering
- **Updated [Message_Messaging.kt](file:///home/eric/StudioProjects/Kotlin-Chat-Software/app/src/main/java/com/example/login_v3/home/Message/UI/Detail/Message_Messaging.kt)**:
    - Switched from `SubcomposeAsyncImage` to the standard `AsyncImage`.
    - **Why?**: `SubcomposeAsyncImage` has significant overhead and often "flashes" its loading state during parent recompositions. The standard `AsyncImage` is much more efficient for high-frequency updates like chat.
    - Implemented a manual shimmer background using a `Box` and `AsyncImage` state callbacks (`onSuccess`, `onLoading`). This ensures the shimmer only shows when the image is *actually* loading from the network or disk, and not during minor UI refreshes.

### 3. Refined Shimmer Modifier
- **Updated [ShimmerModifier.kt](file:///home/eric/StudioProjects/Kotlin-Chat-Software/app/src/main/java/com/example/login_v3/home/Message/UI/Detail/Message_Component/ShimmerModifier.kt)**:
    - Added a `visible` parameter to the `shimmer` modifier.
    - **Why?**: This allows us to toggle the shimmer effect on and off without adding or removing modifiers from the Composable tree, which is more performant and prevents layout recalculations.

## Verification Results

- **Reaction Test**: Adding an emoji reaction now only updates the specific message bubble. Other images in the list remain static and do not show the shimmer effect.
- **Menu Test**: Opening and closing the bottom menus (Action Menu, Emoji Bar) no longer causes the message list to flicker or reload images.
- **Scroll Smoothness**: Scrolling performance is further improved by the removal of `SubcomposeAsyncImage` overhead.

> [!TIP]
> This combined approach of **Callback Stability** and **AsyncImage Optimization** is the industry standard for building high-performance chat interfaces in Jetpack Compose.
