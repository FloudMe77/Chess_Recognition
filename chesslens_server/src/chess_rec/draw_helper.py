import numpy as np
import cv2
import matplotlib.pyplot as plt
import os

def draw_bboxes_on_image(image, bboxes, color=(0, 255, 0), thickness=2):
        # Draw bounding boxes on the image
        h, w = image.shape[:2]
        for bbox in bboxes:
            pts = np.array(bbox, dtype=np.float32)[:2]
            ## if bbox on board
            if np.any((pts[:, 0] >= 0) & (pts[:, 0] <= w) &
                    (pts[:, 1] >= 0) & (pts[:, 1] <= h)):

                pts_int = pts.astype(int)
                
                cv2.polylines(image, [pts_int], isClosed=True, color=color, thickness=thickness)
        
        return image

def plot_cv(image, title="Obraz"):
    plt.figure(figsize=(12, 8))
    plt.imshow(image)
    plt.title(title)
    plt.axis('off')
    plt.show()

def plot_bboxes(image_path, label_path):

    image = cv2.imread(image_path)
    h, w, _ = image.shape

    if os.path.exists(label_path):
        with open(label_path, "r") as f:
            for line in f:
                parts = line.strip().split()
                if len(parts) != 5:
                    continue  
                
                class_id, x_center, y_center, bw, bh = map(float, parts)
                class_id = int(class_id)
                
                x_center *= w
                y_center *= h
                bw *= w
                bh *= h
                
                x1 = int(x_center - bw / 2)
                y1 = int(y_center - bh / 2)
                x2 = int(x_center + bw / 2)
                y2 = int(y_center + bh / 2)
                
                cv2.rectangle(image, (x1, y1), (x2, y2), (0, 255, 0), 2)

                cv2.putText(image, str(class_id), (x1, y1 - 5),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)
    else:
        print("lack of image:", label_path)

    cv2.imshow("image with labels", image)
    plot_cv(image)