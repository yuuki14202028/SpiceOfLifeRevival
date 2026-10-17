"""Draws the mod icon: a stepped diminishing-returns graph with a pile of food.

Usage: python3 tools/make_icon.py <out.png>   (writes a 128x128 icon and a 512x512 preview)
"""
import sys
from PIL import Image

N = 32
img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
px = img.load()


def c(h, a=255):
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4)) + (a,)


def put(x, y, col):
    if 0 <= x < N and 0 <= y < N:
        px[x, y] = col


def rect(x0, y0, x1, y1, col):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            put(x, y, col)


def sprite(ox, oy, rows, pal):
    for dy, row in enumerate(rows):
        for dx, ch in enumerate(row):
            if ch != ".":
                put(ox + dx, oy + dy, pal[ch])


# --- graph: axes with ticks, and the value per repeat stepping down (100, 100, 75, 50, 25, 0 %)
AXIS = c("3a3a3a")
rect(2, 3, 2, 29, AXIS)
rect(2, 29, 29, 29, AXIS)
for y in (8, 13, 18, 23):
    put(1, y, AXIS)
for x in (7, 12, 17, 22, 27):
    put(x, 30, AXIS)

LINE = c("d8402e")
levels = [4, 4, 10, 16, 22, 28]  # y for 100, 100, 75, 50, 25, 0 %
x = 3
for i, y in enumerate(levels):
    w = 4
    rect(x, y, x + w, y, LINE)
    if i + 1 < len(levels) and levels[i + 1] != y:
        rect(x + w, y, x + w, levels[i + 1], LINE)
    x += w

# --- food pile, top right, drawn on its own layer so it can get an outline
graph = img
img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
px = img.load()
apple = [
    "....d.l..",
    "....dll..",
    ".rrrdrrr.",
    "rRRrrrrrr",
    "rRrrrrrrr",
    "rrrrrrrrk",
    "rrrrrrrrk",
    ".rrrrrrk.",
    "..rk.rk..",
]
sprite(15, 1, apple, {"d": c("5a3a1c"), "l": c("4fa63a"), "r": c("d22b2b"),
                      "R": c("ff8a7a"), "k": c("8f1717")})

bread = [
    "...bbbbbbb..",
    "..bBBbBBbbb.",
    ".bBbbBbbBbbb",
    "bbbbbbbbbbbd",
    "dbbbbbbbbbdd",
    ".dddddddddd.",
]
sprite(20, 6, bread, {"b": c("c98a3c"), "B": c("f0c27a"), "d": c("7a4a1a")})

steak = [
    "..mmmmm...",
    ".mMMmmmmf.",
    "mmMmmmmmff",
    "mmmmmmmmf.",
    ".mmmmmmf..",
    "..ffff....",
]
sprite(16, 10, steak, {"m": c("a0432a"), "M": c("d27a55"), "f": c("f2e2c4")})

cookie = [
    ".ccccc.",
    "ccCcckc",
    "ckccCcc",
    "cccckcc",
    ".ccccc.",
]
sprite(24, 12, cookie, {"c": c("d9a05b"), "C": c("f1c98c"), "k": c("4a2a14")})

# dark outline around the pile, then lay it over the graph
OUTLINE = c("2a1a10")
food = img.load()
for y in range(N):
    for x in range(N):
        if food[x, y][3] == 0 and any(
                0 <= x + dx < N and 0 <= y + dy < N and food[x + dx, y + dy][3] > 0
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
            graph.putpixel((x, y), OUTLINE)
graph.alpha_composite(img)
img = graph

out = sys.argv[1]
img.resize((128, 128), Image.NEAREST).save(out)
img.resize((512, 512), Image.NEAREST).save(out.replace(".png", "_preview.png"))
