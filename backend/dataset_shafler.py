import os
import random
import shutil
from pathlib import Path

def shaffle(name):
    dataset_dir = Path(name)
    train_dir = dataset_dir / "images" / "train"
    label_dir = dataset_dir / "labels" / "train"

    # proporcje podziału
    train_ratio = 0.7
    val_ratio = 0.2
    test_ratio = 0.1

    # ustalony seed, żeby podział był powtarzalny
    random.seed(42)

    # === NOWE FOLDERY ===
    for split in ["train", "val", "test"]:
        (dataset_dir / "images" / split).mkdir(parents=True, exist_ok=True)
        (dataset_dir / "labels" / split).mkdir(parents=True, exist_ok=True)

    # === LISTA PLIKÓW ===
    images = list(train_dir.glob("*.*"))  # wszystkie pliki w images/train
    random.shuffle(images)

    n_total = len(images)
    n_train = int(n_total * train_ratio)
    n_val = int(n_total * val_ratio)
    # test to reszta
    n_test = n_total - n_train - n_val

    splits = {
        "val": images[n_train:n_train + n_val],
        "test": images[n_train + n_val:]
    }

    for split, files in splits.items():
        for img_path in files:
            label_path = label_dir / (img_path.stem + ".txt")

            # kopiuj obraz
            shutil.copy(img_path, dataset_dir / "images" / split / img_path.name)
            os.remove(img_path)
            # kopiuj etykietę (jeśli istnieje)
            if label_path.exists():
                shutil.copy(label_path, dataset_dir / "labels" / split / label_path.name)
                os.remove(label_path)


    print(f"Podział zakończony! Zbiór ma:")
    print(f"- Train: {n_train} obrazów")
    print(f"- Val:   {n_val} obrazów")
    print(f"- Test:  {n_test} obrazów")

shaffle("reinforce2")