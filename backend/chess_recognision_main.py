import chess_recognision_model

import asyncio
import websockets
import numpy as np
import cv2
from PIL import Image
import io
from ultralytics import YOLO
import draw_helper
model_board_path = "runs/yolo_phone/weights/best.pt"
model_pieces_path = "runs/yolo_chess4_finetune/weights/best.pt"
model = chess_recognision_model(model_board_path, model_pieces_path)

async def on_image_received(image):
    result = await model.recognize_position_async(image)
    print(result)

async def handle(websocket):
    async for message in websocket:
        print("📸 Otrzymano obraz: ", len(message), " bajtów")

        nparr = np.frombuffer(message, np.uint8)

        img_bgr = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        img_rgb = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2RGB)

        results = on_image_received(img_rgb)

        # Narysuj bbox na obrazie
        annotated = results.plot()
        draw_helper.plot_cv(annotated)

async def main():
    print("🚀 Serwer działa na ws://0.0.0.0:8765")
    async with websockets.serve(handle, "0.0.0.0", 8765):
        await asyncio.Future()

asyncio.run(main())