# Contract: CvEngine (cv module public interface)

This is the sole interface the `app` and `match` modules use to reach vision functionality.
Per constitution Principle II, no OpenCV (or any future ML backend) type may leak across this
boundary — only plain Kotlin data types.

## Interface shape (conceptual — not final Kotlin syntax)

```
interface CvEngine {
    fun calibrateBoard(frame: FrameInput): BoardCalibrationResult
    fun detectThrows(frame: FrameInput, calibration: BoardCalibration): List<DetectedThrow>
}
```

### `calibrateBoard(frame) -> BoardCalibrationResult`

**Purpose**: Locate the dartboard in a frame/photo and establish the scoring geometry (center,
ring radii, sector angular offsets) used by subsequent `detectThrows` calls. Corresponds to
spec FR-001.

**Input**: `FrameInput` — a single image (from either `PHOTO` or `LIVE_CAMERA` capture mode;
the contract is capture-mode-agnostic per spec Assumptions).

**Output**: `BoardCalibrationResult`, one of:
- `Calibrated(calibration: BoardCalibration, confidence: Float)`
- `NotFound` — no board detected in the frame (edge case in spec.md: "app cannot detect the
  dartboard itself")

**Contract rules**:
- MUST NOT throw for a frame with no board — `NotFound` is the expected result, not an
  exception; exceptions are reserved for truly exceptional conditions (e.g., corrupt image
  data).
- `confidence` MUST be present whenever `Calibrated` is returned, so the caller can apply the
  low-confidence indicator required by FR-012.

### `detectThrows(frame, calibration) -> List<DetectedThrow>`

**Purpose**: Given a previously-established board calibration, detect darts in a frame/photo
and return their scored positions. Corresponds to spec FR-002, FR-003, FR-004.

**Input**: `FrameInput` plus the `BoardCalibration` from a prior `calibrateBoard` call.

**Output**: `List<DetectedThrow>`, where each `DetectedThrow` has:
- `sector`: 1-20, `BULL`, or `MISS`
- `ring`: `SINGLE` | `DOUBLE` | `TRIPLE` | `OUTER_BULL` | `INNER_BULL` | `MISS`
- `value`: computed point value, deterministic from `sector` + `ring`
- `confidence`: Float
- `boardPosition`: normalized (x, y) for overlay rendering (FR-005), independent of the
  original image resolution

**Contract rules**:
- Order of returned `DetectedThrow`s is not guaranteed to match throw order — the `match`
  module and UI treat a turn's darts as an unordered set of up to 3 unless the user manually
  sequences them (spec has no requirement that automatic detection infers throw order).
- MUST return an empty list (not an error) when no darts are found.
- Every entry MUST include `confidence` so the caller can apply FR-012's low-confidence
  surfacing per-dart, not just per-frame.
- This call MUST be safe to invoke repeatedly against new frames during a live-camera session
  (constitution Principle IV — no per-call setup cost beyond the initial `calibrateBoard`).

## What this contract deliberately excludes

- **Match/game-mode logic** (bust, checkout, marks, points) — lives entirely in the `match`
  module, which consumes `DetectedThrow` lists but has no knowledge of `CvEngine`'s internals
  (constitution Principle II).
- **Manual correction UI** — the `app` module owns turning a corrected `DetectedThrow` back
  into the `Throw` data-model entity (see data-model.md); `CvEngine` only ever produces the
  initial automatic detection.
- **Persistence** — `CvEngine` is stateless across calls except for the explicit
  `BoardCalibration` value threaded through by the caller; it does not read or write Room data.
