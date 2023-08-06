#!/usr/bin/env python3

import argparse as AP
import json
from pathlib import Path
from typing import TypedDict
from collections import defaultdict

class OldBiomeEntry(TypedDict):
    color: int
    name: str | None
    cave: bool | None

class OldStructureEntry(TypedDict):
    icon: str | None
    name: str | None
    showByDefault: bool | None

class NewBiomeEntry(TypedDict):
    r: int
    g: int
    b: int
    name: str | None

class NewBiomeEntry(TypedDict):
    icon: str
    name: str | None

def write_json(data: dict, path: Path, root: Path, force: bool, prefix: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    if path.exists() and not force:
        print(f'{prefix}\x1b[1;31mSkipping, because {path.relative_to(root)} already exists!\x1b[0m')
        return
    elif path.exists():
        print(f'{prefix}\x1b[1;33mOverwriting existing {path.relative_to(root)}\x1b[0m')
    else:
        print(f'{prefix}\x1b[35mWriting {path.relative_to(root)}')
    path.write_text(json.dumps(data, indent=2) + '\n', encoding='utf-8')

def main() -> int:
    parser = AP.ArgumentParser(description='Convert from the old pack format to the new format')
    parser.add_argument('dir', type=Path, help='The path to the root datapack dir')
    parser.add_argument('-f', '--force', action='store_true', help='Overwrite existing files')

    args = parser.parse_args()
    root: Path = args.dir.resolve()

    if root.name != 'data' or not root.exists() or not root.is_dir():
        print(f'Invalid root data pack dir {root} (The path name must be an existing `data` directory).')
        return 1

    print('\x1b[1;32mProcessing biomes:\x1b[0m')
    biome_colors_data: dict[str, NewBiomeEntry] = {}
    cave_biome_list: list[str] = []
    for structure_root in root.glob('**/biome_preview/'):
        print(f' - \x1b[1mFound biome root: {structure_root.relative_to(root)}\x1b[0m')

        for biome_raw in structure_root.glob('**/*.json'):
            print(f'   - Processing: {biome_raw.relative_to(root)}')
            data: dict[str, OldBiomeEntry] = json.loads(biome_raw.read_text(encoding='utf-8'))

            for k, v in data.items():
                color = v['color']
                biome_colors_data[k] = {
                    'r': (color >> 16) & 0xFF,
                    'g': (color >> 8)  & 0xFF,
                    'b': (color >> 0)  & 0xFF,
                }
                if 'name' in v:
                    biome_colors_data[k]['name'] = v['name']
                if 'cave' in v and v['cave']:
                    cave_biome_list += [k]

    # Write data
    write_json(biome_colors_data, root / 'c' / 'worldgen' / 'biome_colors.json', root, args.force, '   - ')

    # Write cave tags
    write_json(
        {
            'replace': False,
            'values': cave_biome_list
        },
        root / 'c' / 'tags' / 'worldgen' / 'biome' / 'is_cave.json',
        root,
        args.force,
        ' - ',
    )


    print('\x1b[1;32mProcessing structures:\x1b[0m')
    structure_data: dict[str, NewBiomeEntry] = defaultdict(dict)
    display_by_default_list: list[str] = []

    for structure_root in root.glob('**/structure_preview/'):
        print(f' - \x1b[1mFound structure root: {structure_root.relative_to(root)}\x1b[0m')

        for structure_raw in structure_root.glob('**/*.json'):
            print(f'   - Processing: {structure_raw.relative_to(root)}')
            data: dict[str, OldStructureEntry] = json.loads(structure_raw.read_text(encoding='utf-8'))

            for k, v in data.items():
                if 'icon' in v:
                    structure_data[k]['texture'] = v['icon']
                if 'name' in v:
                    structure_data[k]['name'] = v['name']
                if 'showByDefault' in v and v['showByDefault']:
                    display_by_default_list += [k]

    # Write data
    write_json(structure_data, root / 'c' / 'worldgen' / 'structure_icons.json', root, args.force, '   - ')

    # Write cave tags
    write_json(
        {
            'replace': False,
            'values': display_by_default_list
        },
        root / 'c' / 'tags' / 'worldgen' / 'structure' / 'display_on_map_by_default.json',
        root,
        args.force,
        ' - ',
    )

    return 0

if __name__ == '__main__':
    raise SystemExit(main())
