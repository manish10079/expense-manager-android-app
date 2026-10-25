#!/usr/bin/env python3
"""Check the Play Store listing against the Play Console field limits.

Parses docs/play-store/listing-all-languages.md and verifies, for every locale,
that the app name is <= 30 characters, the short description <= 80 and the full
description <= 4000, plus a few structural sanity checks (every locale the app
ships has a section, no field is empty, no stray placeholder markers).

Run: python docs/play-store/check_limits.py
Exit code 0 when every locale passes, 1 otherwise.
"""

import re
import sys
import unicodedata
from pathlib import Path

ROOT = Path(__file__).resolve().parent
LISTING = ROOT / "listing-all-languages.md"

LIMITS = {"App name": 30, "Short description": 80, "Full description": 4000}

# Every locale the app ships in AppLanguages.kt, source included.
EXPECTED = [
    "en", "hi", "bn", "mr", "gu", "kn", "ml", "or", "pa", "ta", "te", "as",
    "es", "pt-BR", "fr", "de", "id", "it", "ja", "ko", "ru",
]


def parse(text):
    """Return {locale: {field: value}} from the markdown listing."""
    sections = re.split(r"^## .+$", text, flags=re.MULTILINE)
    headings = re.findall(r"^## (.+?) — `([^`]+)`\s*$", text, flags=re.MULTILINE)
    result = {}
    # sections[0] is the preamble, so pair each heading with the block after it.
    for (heading, locale), block in zip(headings, sections[1:]):
        fields = {}
        for field, body in re.findall(
            r"^### (.+?)\s*$\s*^```\s*$\n(.*?)^```\s*$", block, flags=re.MULTILINE | re.DOTALL
        ):
            fields[field.strip()] = body.rstrip("\n")
        result[locale] = fields
    return result


def main():
    text = LISTING.read_text(encoding="utf-8")
    listings = parse(text)
    problems = []

    missing_locales = [code for code in EXPECTED if code not in listings]
    if missing_locales:
        problems.append(f"missing locale sections: {', '.join(missing_locales)}")

    extra_locales = [code for code in listings if code not in EXPECTED]
    if extra_locales:
        problems.append(f"unexpected locale sections: {', '.join(extra_locales)}")

    for locale in EXPECTED:
        fields = listings.get(locale)
        if fields is None:
            continue
        for name, limit in LIMITS.items():
            value = fields.get(name)
            if value is None:
                problems.append(f"{locale}: missing field {name!r}")
                continue
            length = len(value)
            status = "ok" if 0 < length <= limit else "FAIL"
            print(f"{locale:6} {name:18} {length:5} / {limit:<5} {status}")
            if not value.strip():
                problems.append(f"{locale}: {name} is empty")
            elif length > limit:
                problems.append(f"{locale}: {name} is {length} chars, limit {limit}")

    # A placeholder left behind would ship as literal text.
    for marker in ("<!-- NEXT -->", "TODO", "TBD"):
        if marker in text:
            problems.append(f"stray marker in listing: {marker}")

    # Mixed-script typos (Latin words containing Cyrillic letters and similar)
    # are invisible in review but visible on the store page.
    for locale, fields in listings.items():
        for field, value in fields.items():
            for word in re.findall(r"\b\w+\b", value):
                scripts = {
                    unicodedata.name(ch, "").split(" ")[0]
                    for ch in word
                    if ch.isalpha()
                }
                if {"LATIN", "CYRILLIC"} <= scripts:
                    problems.append(f"{locale}: {field} mixes Latin and Cyrillic in {word!r}")

    print()
    if problems:
        print("PROBLEMS")
        for problem in problems:
            print(" -", problem)
        return 1

    print(f"All {len(EXPECTED)} locales within limits.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
