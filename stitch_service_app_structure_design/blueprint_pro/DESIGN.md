# Design System Documentation: The Architectural Sentinel

## 1. Overview & Creative North Star
This design system is built upon the "Architectural Sentinel" philosophy. In the world of service management, users are often overwhelmed by chaotic, high-density data. This system rejects the cluttered, "spreadsheet-style" legacy UI in favor of an editorial, high-end experience that feels authoritative yet effortless.

The "Architectural Sentinel" uses intentional asymmetry, tonal depth, and generous negative space to guide the eye. By treating every screen as a structured composition rather than a grid of boxes, we transform functional utility into a premium digital tool. We move away from the "template" look by utilizing sophisticated layering, "ghost" elements, and typography that breathes.

---

## 2. Colors & Tonal Depth
The palette is anchored by a commanding Deep Blue (`primary: #002b59`), evoking trust and institutional stability. This is balanced by a sophisticated spectrum of surface tones that allow for complex information nesting without visual noise.

### The "No-Line" Rule
To maintain a high-end feel, **1px solid borders are strictly prohibited for sectioning.** Boundaries must be defined through background color shifts. For example, a `surface-container-low` section should sit against a `surface` background to create a clean, modern break.

### Surface Hierarchy & Nesting
Treat the UI as a physical stack of premium materials.
- **Base Layer:** `surface` (#f8f9fa)
- **Secondary Containers:** `surface_container_low` (#f3f4f5) for large layout blocks.
- **Interactive Elements:** `surface_container_lowest` (#ffffff) for cards and input fields to make them "pop" against the darker base.
- **Active Overlays:** Use `primary_container` (#1a4175) to draw the eye to high-priority active tasks.

### The "Glass & Gradient" Rule
To prevent the UI from feeling "flat," use Glassmorphism for floating navigation bars or action sheets. Utilize `surface` colors at 80% opacity with a `20px` backdrop blur. For primary CTAs, apply a subtle linear gradient from `primary` (#002b59) to `primary_container` (#1a4175) at a 135-degree angle to add a "liquid" professional polish.

---

## 3. Typography: Editorial Authority
We use a dual-font approach to balance personality with extreme legibility.

- **Display & Headlines (Manrope):** Used for large headers and status summaries. Manrope’s geometric yet warm character provides the "editorial" feel. Use `display-lg` for dashboard summaries to create a bold, confident entry point.
- **UI & Data (Inter):** Used for all functional elements, titles, and body text. Inter is the industry standard for legibility in dense service management environments.

**Hierarchy Tip:** Use `label-sm` in all-caps with `0.05em` letter-spacing for metadata (e.g., Service ID numbers) to differentiate it from actionable task titles (`title-md`).

---

## 4. Elevation & Depth
In this design system, depth is communicated through **Tonal Layering** rather than heavy shadows.

- **The Layering Principle:** Place a `surface_container_lowest` (#ffffff) card on top of a `surface_container_low` (#f3f4f5) background. This creates a soft "lift" that feels integrated into the environment.
- **Ambient Shadows:** When a floating state is required (e.g., a "New Task" FAB), use a highly diffused shadow: `box-shadow: 0 12px 32px -4px rgba(0, 43, 89, 0.08)`. Note the use of the `primary` color in the shadow to create a natural, ambient glow rather than a muddy grey.
- **The "Ghost Border" Fallback:** If a container requires further definition (e.g., in high-glare outdoor environments), use an `outline_variant` (#c3c6d1) at **15% opacity**. It should be felt, not seen.

---

## 5. Components

### Cards & Lists
Cards are the primary vehicle for service tickets.
- **Rule:** Forbid the use of divider lines.
- **Implementation:** Separate list items using 12px of vertical white space and subtle shifts between `surface_container_low` and `surface_container_highest`.
- **Corner Radius:** All cards must use sharp, angular corners (roundedness `0`).

### Status Badges
Status is indicated by tonal "pills" rather than high-contrast blocks.
- **Pending:** `tertiary_container` (#593b00) background with `on_tertiary_fixed_variant` (#604100) text.
- **Completed:** Use a soft green tint (custom success token) following the same tonal contrast ratio.
- **Style:** 999px pill shape with `label-md` bold typography.

### Input Fields
- **Background:** `surface_container_lowest` (#ffffff).
- **Focus State:** Instead of a thick border, use a 2px outer glow using `primary_fixed_dim` (#a9c7ff) and shift the label to `primary` (#002b59).
- **Compactness:** For mobile service management, reduce vertical padding to 12px but maintain a minimum touch target height of 48px.

### Progress Indicators
Avoid standard thin bars. Use a thicker 8px "track" with rounded ends. The track should be `surface_container_highest`, and the progress fill should be a gradient of `primary` to `primary_fixed`.

---

## 6. Do's and Don'ts

### Do
- **Do** use `surface_container` tiers to create hierarchy.
- **Do** use Manrope for headlines to maintain a premium "Director-level" feel.
- **Do** use generous white space (16px, 24px, 32px increments) to allow dense service data to breathe.
- **Do** ensure all touch targets for field technicians are at least 48x48px.

### Don't
- **Don't** use 1px solid borders or horizontal dividers.
- **Don't** use pure black (#000000) for shadows; always tint them with the `primary` blue.
- **Don't** use `display` typography for body paragraphs; keep the editorial fonts for high-level headlines only.
- **Don't** overcrowd cards; if a service ticket has more than 5 data points, use a "Secondary Layer" (progressive disclosure).