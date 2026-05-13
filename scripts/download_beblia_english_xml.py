#!/usr/bin/env python3

import argparse
import json
import os
import re
import sys
import tempfile
import urllib.error
import urllib.request
from pathlib import Path


DEFAULT_OWNER = "Beblia"
DEFAULT_REPO = "Holy-Bible-XML-Format"
DEFAULT_BRANCH = "master"
DEFAULT_OUTPUT_DIR = Path("app/src/main/assets/bibles/beblia")
DEFAULT_PATTERN = re.compile(r"^English.*Bible\.xml$")


def fetch_json(url: str) -> dict:
    request = urllib.request.Request(url, headers={"User-Agent": "OnlyJesus-Beblia-Downloader"})
    with urllib.request.urlopen(request) as response:
        return json.load(response)


def list_english_xml_files(owner: str, repo: str, branch: str) -> list[str]:
    api_url = f"https://api.github.com/repos/{owner}/{repo}/git/trees/{branch}?recursive=1"
    payload = fetch_json(api_url)
    tree = payload.get("tree", [])
    return sorted(
        item["path"]
        for item in tree
        if item.get("type") == "blob" and DEFAULT_PATTERN.match(item.get("path", ""))
    )


def download_file(raw_url: str, destination: Path) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile(delete=False, dir=str(destination.parent)) as temp_file:
        temp_path = Path(temp_file.name)
        try:
            request = urllib.request.Request(raw_url, headers={"User-Agent": "OnlyJesus-Beblia-Downloader"})
            with urllib.request.urlopen(request) as response:
                temp_file.write(response.read())
            temp_file.flush()
            os.fsync(temp_file.fileno())
            temp_path.replace(destination)
        finally:
            if temp_path.exists():
                temp_path.unlink(missing_ok=True)


def main() -> int:
    parser = argparse.ArgumentParser(description="Download Beblia English XML files into app assets.")
    parser.add_argument("--owner", default=DEFAULT_OWNER)
    parser.add_argument("--repo", default=DEFAULT_REPO)
    parser.add_argument("--branch", default=DEFAULT_BRANCH)
    parser.add_argument("--output", default=str(DEFAULT_OUTPUT_DIR))
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()

    output_dir = Path(args.output)
    try:
        files = list_english_xml_files(args.owner, args.repo, args.branch)
    except urllib.error.HTTPError as exc:
        print(f"Failed to list XML files: {exc}", file=sys.stderr)
        return 1

    if not files:
        print("No English XML files were found.", file=sys.stderr)
        return 1

    print(f"Found {len(files)} English XML files.")
    for relative_path in files:
        destination = output_dir / Path(relative_path).name
        raw_url = f"https://raw.githubusercontent.com/{args.owner}/{args.repo}/{args.branch}/{relative_path}"
        if args.dry_run:
            print(f"Would download {relative_path} -> {destination}")
            continue

        print(f"Downloading {relative_path} -> {destination}")
        try:
            download_file(raw_url, destination)
        except urllib.error.HTTPError as exc:
            print(f"Failed to download {relative_path}: {exc}", file=sys.stderr)
            return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())