import os
import random
import shutil
from pathlib import Path

def shuffler(name):
    dataset_dir = Path(name)
    train_dir = dataset_dir / "images" / "train"
    label_dir = dataset_dir / "labels" / "train"

    train_ratio = 0.7
    val_ratio = 0.2
    test_ratio = 0.1

    random.seed(42)

    # create directories if they don't exist
    for split in ["train", "val", "test"]:
        (dataset_dir / "images" / split).mkdir(parents=True, exist_ok=True)
        (dataset_dir / "labels" / split).mkdir(parents=True, exist_ok=True)

    images = list(train_dir.glob("*.*"))  
    random.shuffle(images)

    n_total = len(images)
    n_train = int(n_total * train_ratio)
    n_val = int(n_total * val_ratio)
    n_test = n_total - n_train - n_val

    # spliting into train, val, test
    splits = {
        "val": images[n_train:n_train + n_val],
        "test": images[n_train + n_val:]
    }

    for split, files in splits.items():
        for img_path in files:
            label_path = label_dir / (img_path.stem + ".txt")

            # moving files to their new location
            shutil.copy(img_path, dataset_dir / "images" / split / img_path.name)
            os.remove(img_path)
            if label_path.exists():
                shutil.copy(label_path, dataset_dir / "labels" / split / label_path.name)
                os.remove(label_path)

    # printing summary
    print(f"Division completed! The collection has:")
    print(f"- Train: {n_train} images")
    print(f"- Val:   {n_val} images")
    print(f"- Test:  {n_test} images")

if __name__ == "__main__":
    shuffler("datasets\\piece_recognition_set\\reinforce_set_part1")