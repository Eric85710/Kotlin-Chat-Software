# Instant Media Loading Implementation Plan

Optimize the loading of images and video thumbnails in the chat room to ensure they appear instantly without "blank" moments.

## User Review Required

> [!IMPORTANT]
> I will be configuring a global `ImageLoader` for the entire app. This centralizes the logic for decoding videos, GIFs, and SVGs, which improves performance and reduces memory usage.

## Proposed Changes

### [Core Framework]

#### [MODIFY] [MyApplication.kt](file:///home/eric/StudioProjects/Kotlin-Chat-Software/app/src/main/java/com/example/login_v3/MyApplication.kt)
- Implement `ImageLoaderFactory` to provide a singleton `ImageLoader`.
- Configure the `ImageLoader` with specialized decoders:
    - `VideoFrameDecoder`: For video thumbnails.
    - `GifDecoder` / `ImageDecoderDecoder`: For GIF support.
    - `SvgDecoder`: For SVG support.
- Set up optimized memory and disk caching.

### [Data Model]

#### [MODIFY] [DM_Basic.kt](file:///home/eric/StudioProjects/Kotlin-Chat-Software/app/src/main/java/com/example/login_v3/data/api/api_class/DM_Basic.kt)
- Add extension properties to the `Message` class:
    - `mediaUrl`: Centralized logic for calculating the full URL for attachments.
    - `isImage`, `isVideo`, `isAudio`, `isGif`, `isFile`: Boolean helpers to simplify UI logic.

### [Chat Feature]

#### [MODIFY] [Message_Messaging_ViewModel.kt](file:///home/eric/StudioProjects/Kotlin-Chat-Software/app/src/main/java/com/example/login_v3/home/Message/ViewModel/Detail/Message_Messaging_ViewModel.kt)
- Implement a pre-fetching mechanism. When messages are loaded into the `uiState`, the ViewModel will trigger Coil to pre-download and cache the media URLs.

#### [MODIFY] [Message_Messaging.kt](file:///home/eric/StudioProjects/Kotlin-Chat-Software/app/src/main/java/com/example/login_v3/home/Message/UI/Detail/Message_Messaging.kt)
- Simplify `MessageRow` by using the new properties from the `Message` class.
- Remove redundant local `ImageLoader` initializations.
- Ensure `AsyncImage` uses the optimized global `ImageLoader`.

## Verification Plan

### Manual Verification
- **Entering Room:** Enter a chat room with images/videos and verify they appear nearly instantly.
- **Scrolling:** Scroll through long history and verify that media items are pre-loaded and don't show "blank" placeholders.
- **Media Types:** Verify that GIFs play correctly, video thumbnails show up, and SVGs (if any) are rendered.
- **Network Resilience:** Ensure the app handles slow network gracefully with placeholders (if pre-fetching is still in progress).
