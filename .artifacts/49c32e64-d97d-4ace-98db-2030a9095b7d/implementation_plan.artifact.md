# Final Implementation Plan - Fix Image Reloading

This plan eliminates the persistent image flickering by ensuring **Instance Stability** in the ViewModel. This prevents Compose from thinking that every image in the chat has changed when a single reaction is added.

## User Review Required

> [!IMPORTANT]
> The previous fix stabilized the click callbacks, but the **data instances** themselves were still being recreated for every message on every update. This plan makes the data mapping "smart" so only modified messages get new UI model instances.

## Proposed Changes

### 1. ViewModel Optimization (Instance Stability)

#### [MODIFY] [ChatViewModel](file:///home/eric/StudioProjects/Kotlin-Chat-Software/app/src/main/java/com/example/login_v3/home/Message/ViewModel/Detail/Message_Messaging_ViewModel.kt)
- **Message Instance Cache**: Maintain a private `Map<String, Pair<Message, MessageUiModel>>` in the ViewModel.
- **Smart Mapping**:
    - When the repository emits a new list of `Message` objects, compare each message with its cached version.
    - If the raw `Message` data is identical (including reactions), reuse the existing `MessageUiModel` instance.
    - Only call `toUiModel()` for messages that have actually changed.
- **Why?**: This allows Jetpack Compose to use its "Smart Recomposition" feature. If the object instance is the same, Compose can skip the entire `MessageRow` and its internal `AsyncImage` completely.

### 2. UI Refinement

#### [MODIFY] [Message_Messaging.kt](file:///home/eric/StudioProjects/Kotlin-Chat-Software/app/src/main/java/com/example/login_v3/home/Message/UI/Detail/Message_Messaging.kt)
- Ensure the `AsyncImage` model is strictly the URL string to prevent any ambiguity.

## Verification Plan

### Manual Verification
- **Stress Test Reactions**: Rapidly add and remove reactions. Confirm that only the affected message's reaction bubble updates, and **zero** other images in the chat flicker or show a shimmer.
- **Scroll Test**: Ensure the cache management doesn't negatively impact scroll performance.
- **Memory Check**: Verify that the cache is cleared when the ViewModel is cleared.
