"""Render a tmux `capture-pane -pe` dump to a PNG, drawn with a Nerd Font.

usage: ansi2png.py <capture.ans> <out.png> <cols> <rows> [x0 y0 x1 y1]

`cols`/`rows` are the size of the captured pane. The optional crop is in cells, end exclusive.
The font is FiraCode Nerd Font Mono from $DOCS_FONT_DIR (default ~/Library/Fonts). Cells using the
terminal's default colors are drawn in $DOCS_DEFAULT_FG / $DOCS_DEFAULT_BG (RRGGBB, default: the dark
theme's), so a light theme can be shown as it looks in a light terminal.
"""
import os
import sys
import pyte
from PIL import Image, ImageDraw, ImageFont

FONT_DIR = os.environ.get("DOCS_FONT_DIR", os.path.expanduser("~/Library/Fonts")) + "/"
SIZE = 15
fonts = {
    False: ImageFont.truetype(FONT_DIR + "FiraCodeNerdFontMono-Regular.ttf", SIZE),
    True: ImageFont.truetype(FONT_DIR + "FiraCodeNerdFontMono-Bold.ttf", SIZE),
}
def hex_color(value):
    return tuple(int(value[i:i + 2], 16) for i in (0, 2, 4))


DEFAULT_FG = hex_color(os.environ.get("DOCS_DEFAULT_FG", "DDE2E5"))
DEFAULT_BG = hex_color(os.environ.get("DOCS_DEFAULT_BG", "101418"))
ANSI16 = {
    "black": (0, 0, 0), "red": (205, 49, 49), "green": (13, 188, 121), "brown": (229, 229, 16),
    "blue": (36, 114, 200), "magenta": (188, 63, 188), "cyan": (17, 168, 205), "white": (229, 229, 229),
    "brightblack": (102, 102, 102), "brightred": (241, 76, 76), "brightgreen": (35, 209, 139),
    "brightbrown": (245, 245, 67), "brightblue": (59, 142, 234), "brightmagenta": (214, 112, 214),
    "brightcyan": (41, 184, 219), "brightwhite": (255, 255, 255),
}


def color(value, default):
    if value == "default":
        return default
    if value in ANSI16:
        return ANSI16[value]
    try:
        return hex_color(value)
    except ValueError:
        return default


# Characters meant to join their neighbours, drawn as cell-filling shapes so consecutive cells
# touch: (x0, y0, x1, y1) as fractions of the cell.
SOLID = {
    "▬": (0, 0.38, 1, 0.62),
    "█": (0, 0, 1, 1),
    "▀": (0, 0, 1, 0.5),
    "▄": (0, 0.5, 1, 1),
    "▌": (0, 0, 0.5, 1),
    "▐": (0.5, 0, 1, 1),
}


def main():
    src, out, cols, rows = sys.argv[1], sys.argv[2], int(sys.argv[3]), int(sys.argv[4])
    x0, y0, x1, y1 = (map(int, sys.argv[5:9]) if len(sys.argv) >= 9 else (0, 0, cols, rows))
    screen = pyte.Screen(cols, rows)
    stream = pyte.Stream(screen)
    text = open(src, encoding="utf-8").read().rstrip("\n").split("\n")
    stream.feed("\r\n".join(line + "\x1b[0m" for line in text))

    left, top, right, bottom = fonts[False].getbbox("M")
    cw = fonts[False].getlength("M")
    ch = int(SIZE * 1.32)
    w, h = int((x1 - x0) * cw), (y1 - y0) * ch
    pad = 14
    img = Image.new("RGB", (w + 2 * pad, h + 2 * pad), DEFAULT_BG)
    draw = ImageDraw.Draw(img)
    for y in range(y0, y1):
        row = screen.buffer[y]
        for x in range(x0, x1):
            c = row[x]
            fg, bg = color(c.fg, DEFAULT_FG), color(c.bg, DEFAULT_BG)
            if c.reverse:
                fg, bg = bg, fg
            px, py = pad + (x - x0) * cw, pad + (y - y0) * ch
            if bg != DEFAULT_BG:
                draw.rectangle([px, py, px + cw + 0.5, py + ch], fill=bg)
            if c.data in SOLID:
                fx0, fy0, fx1, fy1 = SOLID[c.data]
                draw.rectangle(
                    [round(px + fx0 * cw), round(py + fy0 * ch), round(px + fx1 * cw), round(py + fy1 * ch) - 1],
                    fill=fg,
                )
            elif c.data.strip():
                draw.text((px, py + (ch - SIZE) // 2 - 1), c.data, font=fonts[c.bold], fill=fg)
    img.save(out)
    print(out, img.size)


main()
