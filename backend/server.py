from fastapi import FastAPI, WebSocket, WebSocketDisconnect
import asyncio
import json
import numpy as np
import cv2
import chess_recognision_model
import time
from ultralytics import YOLO

model_s = YOLO("runs/yolo_chess4_finetune/weights/best.pt")
model_board_path = "runs/yolo_phone/weights/best.pt"
model_pieces_path = "runs/yolo_chess4_finetune1_1/weights/best.pt"
model = chess_recognision_model.ChessRecognisionModel(model_board_path, model_pieces_path)
labels = model_s.names

app = FastAPI()

connected_clients = set()  

async def on_image_received(image):
    return await model.recognize_position_async(image)

@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    connected_clients.add(websocket)
    print("Połączono nowe urządzenie")
    try:
        while True:
            print("otrzymałem wiadomość", time.localtime())
            message = await websocket.receive_bytes()
            nparr = np.frombuffer(message, np.uint8)
            image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

            results = await on_image_received(image)
        
            json_results = json.dumps(results)

            # Wyślij wynik do wszystkich podłączonych klientów
            for client in connected_clients.copy():  # copy żeby uniknąć zmian set podczas iteracji
                try:
                    await client.send_text(json_results)
                except:
                    connected_clients.remove(client)  # usuwamy rozłączonych
    except WebSocketDisconnect:
        connected_clients.remove(websocket)
