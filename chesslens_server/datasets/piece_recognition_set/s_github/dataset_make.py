import random, shutil
from pathlib import Path

root   = Path("github")
imgs   = list((root/"images").glob("*.*"))
random.shuffle(imgs)

splits = {"train": 0.8, "val": 0.15, "test": 0.05}
cut1   = int(len(imgs)*splits["train"])
cut2   = cut1 + int(len(imgs)*splits["val"])
subsets = {
    "train": imgs[:cut1],
    "val":   imgs[cut1:cut2],
    "test":  imgs[cut2:]
}

for split, files in subsets.items():
    (root/f"images/{split}").mkdir(parents=True, exist_ok=True)
    (root/f"labels/{split}").mkdir(parents=True, exist_ok=True)
    for img in files:
        lbl = root/"labels"/img.with_suffix(".txt").name
        shutil.move(img,   root/f"images/{split}/{img.name}")
        if lbl.exists():
            shutil.move(lbl, root/f"labels/{split}/{lbl.name}")
