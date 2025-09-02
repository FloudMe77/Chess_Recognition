from fastapi import FastAPI, WebSocket, WebSocketDisconnect
import json
import numpy as np
import cv2
from chess_rec.chess_position_recognizer import ChessPositionRecognizer
import time

model_board_path = "models/yolo_board_seg/weights/best.pt"
model_pieces_path = "models/yolo_chess_piece/weights/best.pt"
model = ChessPositionRecognizer(model_board_path, model_pieces_path)


app = FastAPI()

connected_clients = set()  

async def on_image_received(image):
    return await model.recognize_position_async(image)

@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    connected_clients.add(websocket)
    print("New client connected")
    try:
        while True:
            
            message = await websocket.receive_bytes()
            print("massage getted", time.localtime())
            # time maserment start
            start_time = time.time()

            nparr = np.frombuffer(message, np.uint8)
            image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

            results = await on_image_received(image)

            json_results = json.dumps(results)

            # time maserment end
            end_time = time.time()  # 
            elapsed_ms = (end_time - start_time) * 1000
            print(f"Time: {elapsed_ms:.2f} ms")

            for client in connected_clients.copy():  
                try:
                    await client.send_text(json_results)
                except:
                    connected_clients.remove(client) 
    except WebSocketDisconnect:
        connected_clients.remove(websocket)
