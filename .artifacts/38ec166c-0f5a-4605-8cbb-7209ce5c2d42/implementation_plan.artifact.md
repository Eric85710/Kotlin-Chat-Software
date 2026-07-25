# Implementation Plan - Optimize ImageLoader Usage

Ensure all components, specifically `Loaded_setting_profile_page`, use the optimized global `ImageLoader` provided by `MyApplication` to prevent resource waste and improve performance.

## User Review Required

> [!IMPORTANT]
> I will be removing local `ImageLoader` initializations in `Login_Page.kt` and potentially other files. These components will now rely on the singleton `ImageLoader` configured in `MyApplication.kt`.

## Proposed Changes

### [Component Name] Global Image Loader Optimization

#### [MODIFY] [MyApplication.kt](file:///home/eric/StudioProjects/Kotlin-Chat-Software/app/src/main/java/com/example/login_v3/MyApplication.kt)
- Ensure the `ImageLoader` is correctly configured and accessible. (Already looks good, but will double-check).

#### [MODIFY] [Technologia.kt](file:///home/eric/StudioProjects/Kotlin-Chat-Software/app/src/main/java/com/example/login_v3/Technologia.kt)
- Wrap the root composable with `CompositionLocalProvider(LocalImageLoader provides LocalContext.current.imageLoader)` to ensure all `AsyncImage` calls use the global singleton.

#### [MODIFY] [Login_Page.kt](file:///home/eric/StudioProjects/Kotlin-Chat-Software/app/src/main/java/com/example/login_v3/auth/login/Login_Page.kt)
- Refactor `SvgImage` to use `AsyncImage` with the global loader.
- Remove redundant local `ImageLoader` builder.

#### [MODIFY] [setting_Profile_Page.kt](file:///home/eric/StudioProjects/Kotlin-Chat-Software/app/src/main/java/com/example/login_v3/home/setting/setting_detail_page/detail_UI/setting_Profile_Page.kt)
- Ensure `AsyncImage` calls in `Loaded_setting_profile_page` are optimized.
- (Optional) Use `LocalImageLoader.current` if we decide to provide it via `CompositionLocalProvider` for better control, though `MyApplication`'s factory is usually sufficient.

#### [MODIFY] [Add_Account_Page.kt](file:///home/eric/StudioProjects/Kotlin-Chat-Software/app/src/main/java/com/example/login_v3/auth/SwitchAccount/Add_Account_Page.kt)
- Remove unused `ImageLoader` and `SvgDecoder` imports.

## Verification Plan

### Automated Tests
- Build the project to ensure no compilation errors after removing redundant imports and loaders.

### Manual Verification
- Verify that SVGs and GIFs still load correctly in the Login and Profile pages.
- Monitor logcat for any `ImageLoader` initialization logs to ensure only one instance is created.
