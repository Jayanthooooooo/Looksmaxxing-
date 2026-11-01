# Implementation Plan - Face Shape and Style Guide

1. **Face Shape Engine**:
   - Create `reference_bands.json` in assets with provisional thresholds for Oval, Round, Square, Rectangle, Heart, Diamond.
   - Create pure Kotlin `FaceShapeEngine.kt` to compute face ratios and classify top 2 matches with confidence levels. Validate quality / measurements.
   - Create comprehensive unit tests (`FaceShapeEngineUnitTest.kt`) testing synthetic fixtures for each shape and edge cases.

2. **Style Guide Data**:
   - Create `style_guide.json` in assets containing haircuts, beard options, eyebrow tips, glasses frames, "things to avoid", and barber-ready summary lines for each face shape, following respectful wording rules.

3. **Room & Data Layer**:
   - Extend Room database (`LumoraDatabase` / Entity / DAO) to store face shape classification results and favorite style recommendations.
   - Implement `StyleGuideRepository` to load and query `style_guide.json` and face shape recommendations.

4. **UI & Navigation**:
   - Add entry cards on Results screen and Library screen without altering existing screens.
   - Build "Face Shape and Style" screen with result chip, confidence note, plain-language explanation, tabs for Hair, Beard, Brows, Glasses, fullscreen "Show to my barber" card, and save-to-favorites functionality.
   - Implement Teen Mode check (hide beard options & active-ingredient mentions if under 16).

5. **AI Coach Context**:
   - Provide saved face shape and favorites as context to the AI Coach.

6. **Verification**:
   - Compile applet and run unit tests.
