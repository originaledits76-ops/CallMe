# CallMe — Premium iOS-Style Redesign Implementation Plan

## Overview
Re-architect and polish the entire CallMe Android application to deliver an authentic, high-end iOS Human Interface Guidelines (HIG) aesthetic. Features Apple system tints (adaptive light/dark), grouped iOS-style list cards, rounded typography, spring animations, and pixel-perfect responsiveness.

## Proposed Changes

### 1. Design System & Typography (`Color.kt`, `Theme.kt`, `Type.kt`)
- **Colors**: Adopt Apple System Blue (`#007AFF`), System Background (`#F2F2F7` light, `#000000` dark), Grouped Card Surface (`#FFFFFF` light, `#1C1C1E` dark), Secondary Label (`#8E8E93`), and Separator (`#C6C6C8`).
- **Typography**: Apply iOS San Francisco style bold and rounded typography hierarchy.

### 2. UI Components & Screens
- **Grouped iOS List Cards**: Style reminder lists, settings, and scheduling inputs into rounded grouped inset cards (`rounded-2xl`).
- **iOS Navigation & Tab Bar**: Floating iOS-style tab bar with frosted glass blur effect (`backdrop-blur`) and icon/label micro-interactions.
- **Schedule Call Screen**: iOS-style wheel/picker or clean iOS list rows for time selection, caller name, script, and numpad action mapping.
- **Incoming Call Screen**: Native iOS incoming call full-screen experience with slide-to-answer/decline tactile buttons and interactive dialpad.

## Verification Plan
- Compile applet with `compile_applet`.
- Verify zero compilation errors and pristine UI layout.
