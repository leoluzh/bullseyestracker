#!/usr/bin/env python3
"""Generate synthetic dartboard test fixtures for the cv module's instrumented tests.

These are code-rendered placeholders, NOT real dartboard photos. They exist to unblock
OpenCvBoardDetectorTest / OpenCvDartDetectorTest / PerformanceBenchmarkTest (which otherwise
fail on a missing-asset error) and to exercise DetectionAccuracyBenchmarkTest's plumbing.
They do NOT validate real-world detection accuracy - see
cv/src/androidTest/assets/fixtures/README.md for why real photos are still needed for that.

Geometry matches OpenCvBoardDetector's regulation-board ring ratios exactly (same constants,
see cv/src/main/java/com/bullseyestracker/cv/opencv/OpenCvBoardDetector.kt) and ScoreMapper's
sector layout/angle convention, so a fixture's rendered dart position actually falls inside the
ring/sector its ground-truth JSON claims.
"""
import json
import math
from pathlib import Path

from PIL import Image, ImageDraw

FIXTURES_DIR = Path(__file__).resolve().parent.parent / "cv" / "src" / "androidTest" / "assets" / "fixtures"

CANVAS = 1000
CENTER = CANVAS / 2
OUTER_RADIUS = 350.0  # 35% of width - inside OpenCvBoardDetector's accepted 20%-55% range

# Same ratios as OpenCvBoardDetector's companion object, relative to the outer double-ring edge.
INNER_BULL_RATIO = 6.35 / 170
OUTER_BULL_RATIO = 15.9 / 170
TRIPLE_INNER_RATIO = 99 / 170
TRIPLE_OUTER_RATIO = 107 / 170
DOUBLE_INNER_RATIO = 162 / 170

SECTOR_ORDER_CLOCKWISE_FROM_TWENTY = [20, 1, 18, 4, 13, 6, 10, 15, 2, 17, 3, 19, 7, 16, 8, 11, 14, 9, 12, 5]
SECTOR_WIDTH_DEGREES = 360 / 20

BACKDROP = (150, 150, 150)
CREAM = (230, 220, 190)
BLACK_SEG = (20, 20, 20)
RED = (170, 30, 30)
GREEN = (20, 110, 60)


def ring_radius_px(ratio: float) -> float:
    return OUTER_RADIUS * ratio


def sector_center_angle_degrees(sector_number: int) -> float:
    """Angle clockwise from top (12 o'clock), matching ScoreMapper.sectorAt's convention
    (rotationOffsetDegrees is always 0 - see OpenCvBoardDetector)."""
    index = SECTOR_ORDER_CLOCKWISE_FROM_TWENTY.index(sector_number)
    return index * SECTOR_WIDTH_DEGREES


def polar_to_pixel(radius: float, angle_degrees: float) -> tuple[float, float]:
    theta = math.radians(angle_degrees)
    dx = radius * math.sin(theta)
    dy = -radius * math.cos(theta)
    return CENTER + dx, CENTER + dy


def dart_pixel_position(sector_number: int | None, ring: str) -> tuple[float, float]:
    band = {
        "INNER_BULL": (0.0, INNER_BULL_RATIO * 0.6),
        "OUTER_BULL": (INNER_BULL_RATIO, OUTER_BULL_RATIO),
        "TRIPLE": (TRIPLE_INNER_RATIO, TRIPLE_OUTER_RATIO),
        "DOUBLE": (DOUBLE_INNER_RATIO, 1.0),
        "SINGLE": (TRIPLE_OUTER_RATIO, DOUBLE_INNER_RATIO),
        "MISS": (1.15, 1.15),
    }[ring]
    radius = ring_radius_px((band[0] + band[1]) / 2)
    angle = sector_center_angle_degrees(sector_number) if sector_number is not None else 0.0
    return polar_to_pixel(radius, angle)


def draw_board(draw: ImageDraw.ImageDraw) -> None:
    outer_bull_px = ring_radius_px(OUTER_BULL_RATIO)
    triple_inner_px = ring_radius_px(TRIPLE_INNER_RATIO)
    triple_outer_px = ring_radius_px(TRIPLE_OUTER_RATIO)
    double_inner_px = ring_radius_px(DOUBLE_INNER_RATIO)
    inner_bull_px = ring_radius_px(INNER_BULL_RATIO)

    def bbox(radius: float) -> tuple[float, float, float, float]:
        return (CENTER - radius, CENTER - radius, CENTER + radius, CENTER + radius)

    # Outer edge of the whole board (single-color base disc) first, then layer rings/wedges on top.
    draw.ellipse(bbox(OUTER_RADIUS), fill=CREAM)

    for i in range(20):
        start = i * SECTOR_WIDTH_DEGREES - SECTOR_WIDTH_DEGREES / 2 - 90
        end = start + SECTOR_WIDTH_DEGREES
        single_color = BLACK_SEG if i % 2 == 0 else CREAM
        double_triple_color = GREEN if i % 2 == 0 else RED

        # Largest radius first, each smaller pieslice overwriting the previous one's center -
        # the only way PIL's pieslice can build concentric bands.
        draw.pieslice(bbox(OUTER_RADIUS), start, end, fill=double_triple_color)  # double ring
        draw.pieslice(bbox(double_inner_px), start, end, fill=single_color)  # single (outer band)
        draw.pieslice(bbox(triple_outer_px), start, end, fill=double_triple_color)  # triple ring
        draw.pieslice(bbox(triple_inner_px), start, end, fill=single_color)  # single (inner band)

    draw.ellipse(bbox(outer_bull_px), fill=GREEN)
    draw.ellipse(bbox(inner_bull_px), fill=RED)


def render_board_image(dim: bool = False) -> Image.Image:
    img = Image.new("RGB", (CANVAS, CANVAS), BACKDROP)
    draw_board(ImageDraw.Draw(img))
    if dim:
        img = Image.eval(img, lambda p: int(p * 0.45))
    return img


def render_no_board_image() -> Image.Image:
    img = Image.new("RGB", (CANVAS, CANVAS), (110, 100, 90))
    draw = ImageDraw.Draw(img)
    for x in range(0, CANVAS, 40):
        draw.line([(x, 0), (x, CANVAS)], fill=(100, 90, 80), width=2)
    return img


def draw_dart(img: Image.Image, sector_number: int | None, ring: str) -> None:
    x, y = dart_pixel_position(sector_number, ring)
    r = 18
    underlying = img.getpixel((int(x), int(y)))
    luminance = 0.299 * underlying[0] + 0.587 * underlying[1] + 0.114 * underlying[2]
    marker_color = (0, 0, 0) if luminance > 128 else (255, 255, 255)
    ImageDraw.Draw(img).ellipse((x - r, y - r, x + r, y + r), fill=marker_color)


def write_fixture(name: str, scenario: str, lighting: str, darts: list[dict], dim: bool = False) -> None:
    base = render_board_image(dim=dim)
    photo = base.copy()
    for dart in darts:
        draw_dart(photo, dart["sectorNumber"], dart["ring"])

    photo.save(FIXTURES_DIR / f"{name}.png")
    if darts:
        base.save(FIXTURES_DIR / f"{name}_baseline.png")

    ground_truth = {"scenario": scenario, "lighting": lighting, "darts": darts}
    (FIXTURES_DIR / f"{name}.json").write_text(json.dumps(ground_truth, indent=2) + "\n")
    print(f"wrote {name}: {len(darts)} dart(s), {lighting}")


def main() -> None:
    FIXTURES_DIR.mkdir(parents=True, exist_ok=True)

    write_fixture("board_calibrated", "clear well-lit board, no darts", "normal", [])

    no_board = render_no_board_image()
    no_board.save(FIXTURES_DIR / "no_board.png")
    (FIXTURES_DIR / "no_board.json").write_text(
        json.dumps({"scenario": "no dartboard visible, plain wall", "lighting": "normal", "darts": []}, indent=2)
        + "\n"
    )
    print("wrote no_board: 0 dart(s), normal")

    write_fixture("empty_board", "calibratable board, zero darts", "normal", [])

    write_fixture(
        "one_dart",
        "single dart, triple 20",
        "normal",
        [{"sectorNumber": 20, "ring": "TRIPLE"}],
    )

    write_fixture(
        "three_darts",
        "three darts across different sectors and rings",
        "normal",
        [
            {"sectorNumber": 20, "ring": "TRIPLE"},
            {"sectorNumber": 5, "ring": "SINGLE"},
            {"sectorNumber": None, "ring": "INNER_BULL"},
        ],
    )

    write_fixture(
        "two_darts_adjacent_20_5",
        "two darts in physically adjacent sectors (20 and 5), ambiguous-boundary case",
        "normal",
        [
            {"sectorNumber": 20, "ring": "SINGLE"},
            {"sectorNumber": 5, "ring": "SINGLE"},
        ],
    )

    write_fixture(
        "bullseye_inner",
        "single dart dead in the inner bull",
        "normal",
        [{"sectorNumber": None, "ring": "INNER_BULL"}],
    )

    write_fixture(
        "bullseye_outer",
        "single dart in the outer bull",
        "normal",
        [{"sectorNumber": None, "ring": "OUTER_BULL"}],
    )

    write_fixture(
        "dart_miss",
        "single dart missing the board entirely",
        "normal",
        [{"sectorNumber": None, "ring": "MISS"}],
    )

    write_fixture(
        "dart_double_ring",
        "single dart on double 5",
        "normal",
        [{"sectorNumber": 5, "ring": "DOUBLE"}],
    )

    write_fixture(
        "dart_triple_ring_alt_sector",
        "single dart on triple 3",
        "normal",
        [{"sectorNumber": 3, "ring": "TRIPLE"}],
    )

    write_fixture(
        "two_darts_mixed_rings",
        "two darts, non-adjacent sectors, single and triple",
        "normal",
        [
            {"sectorNumber": 11, "ring": "SINGLE"},
            {"sectorNumber": 19, "ring": "TRIPLE"},
        ],
    )

    write_fixture(
        "one_dart_dim",
        "single dart, triple 20, dim lighting",
        "dim",
        [{"sectorNumber": 20, "ring": "TRIPLE"}],
        dim=True,
    )

    write_fixture(
        "three_darts_dim",
        "three darts across different sectors and rings, dim lighting",
        "dim",
        [
            {"sectorNumber": 20, "ring": "TRIPLE"},
            {"sectorNumber": 5, "ring": "SINGLE"},
            {"sectorNumber": None, "ring": "INNER_BULL"},
        ],
        dim=True,
    )


if __name__ == "__main__":
    main()
