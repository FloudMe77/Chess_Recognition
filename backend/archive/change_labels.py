import os

def change(n):
    match n:
        case 1: return 9
        case 2: return 11
        case 3: return 8
        case 4: return 6
        case 5: return 10
        case 6: return 7
        case 7: return 3
        case 8: return 5
        case 9: return 2
        case 10: return 0
        case 11: return 4
        case 12: return 1
        case _: raise ValueError("Nieznana wartość: " + str(n))

def change_labels(folder):
    for file_name in os.listdir(folder):
        full_path = os.path.join(folder, file_name)
        if not os.path.isfile(full_path):
            continue

        with open(full_path, "r") as f:
            lines = f.readlines()

        new_lines = []
        for line in lines:
            words = line.strip().split()
            if not words:
                continue  # pomiń puste linie
            first_num = int(words[0])
            try:
                words[0] = str(change(first_num))
            except:
                print(file_name)
                continue
            new_lines.append(' '.join(words) + '\n')

        with open(full_path, "w") as f:
            f.writelines(new_lines)
change_labels("Chess Pieces.v24-416x416_aug.yolov11/train/labels")