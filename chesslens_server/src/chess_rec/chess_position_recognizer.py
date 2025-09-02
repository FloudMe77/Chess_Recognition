from ultralytics import YOLO
import numpy as np
import cv2
import asyncio

class ChessPositionRecognizer():
    def __init__(self, model_board_path, model_pieces_path):
        # Load YOLO models
        if model_board_path and model_pieces_path:
            self.model_board = YOLO(model_board_path)
            self.model_pieces = YOLO(model_pieces_path)
    
    @staticmethod
    def _cals_deg(A, B, C):
        # calculate angle ABC
        BA = A - B
        BC = C - B
        numerator = np.dot(BA,BC)
        denominator = np.linalg.norm(BA) * np.linalg.norm(BC)
        return np.degrees(np.arccos(numerator / denominator))
    
    @staticmethod
    def _center_of_piece(lb, rb):
        # lb - left bottom, rb - right bottom
        vec = rb - lb           
        x, y = vec
        # half width vector perpendicular to vec
        wek = np.array([-y, x]) / 2
        mid = lb + vec / 2
        return mid + wek

    @staticmethod
    def _get_correct_order(corners):
        # zał 
        # ld - left down, pd - right down, lg - left up, pg - right up
        bottom_ind = np.argmax(corners[:,1])
        bottom = corners[bottom_ind]
        right = corners[(bottom_ind+1)%4]
        # if angle bottom-right-bottom+100,0 < 45 then bottom is correct
        # necesery for unambiguity of corner order
        if ChessPositionRecognizer._cals_deg(bottom + [100, 0], bottom, right) < 45:
            return np.roll(corners, -bottom_ind, axis=0)
        return np.roll(corners, -((bottom_ind-1)%4), axis=0)
    
    @staticmethod
    def _convert_to_total_list(bboxes):
        list_bboxes = []

        for x_min, y_min, x_max, y_max in bboxes:
            list_bboxes.append([x_max, y_max])
            list_bboxes.append([x_min, y_max])
            list_bboxes.append([x_min, y_min])
            list_bboxes.append([x_max, y_min])
            
        points_array = np.array(list_bboxes, dtype=np.float32).reshape(-1, 1, 2)
        return points_array
    
    @staticmethod
    def _convert_to_separate_list(bbox_list):
        bboxes = []
        for i in range(0,len(bbox_list),4):
            xs = bbox_list[i:i+4, 0, 0]
            ys = bbox_list[i:i+4, 0, 1]
            bboxes.append(list(zip(xs,ys)))
        return bboxes
    
    def find_corners(self, mask):
        mask_bin = (mask > 0.5).astype(np.uint8)

        kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5,5))
        mask_bin = cv2.morphologyEx(mask_bin, cv2.MORPH_OPEN, kernel)

        # biggest area
        num_labels, labels, stats, _ = cv2.connectedComponentsWithStats(mask_bin, connectivity=8)
        if num_labels > 1:
            largest = 1 + np.argmax(stats[1:, cv2.CC_STAT_AREA])
            mask_bin = (labels == largest).astype(np.uint8)

        # find contours
        contours, _ = cv2.findContours(mask_bin, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        contour = max(contours, key=cv2.contourArea)

        # aproximate to polygon of 4 points
        epsilon = 0.02 * cv2.arcLength(contour, True)
        approx = cv2.approxPolyDP(contour, epsilon, True)

        # if 4 points found, get them in correct order
        if len(approx) == 4:
            corners = approx.reshape(4, 2)
            corners = ChessPositionRecognizer._get_correct_order(corners)
        # else return None
        return corners if len(approx) == 4 else None

    def change_perspective(self, corners, image, bbox_list):
        # define destination points for perspective transform
        dst_pts = np.array([
                [0, 640],
                [640, 640],
                [640, 0],
                [0, 0]
            ], dtype=np.float32)
        M = cv2.getPerspectiveTransform(corners.astype(np.float32), dst_pts)

        # apply perspective transform to image and bboxes
        transformed_image = cv2.warpPerspective(image, M, (640, 640))
        transformed_bbox_list = cv2.perspectiveTransform(bbox_list.astype(np.float32), M)
        return transformed_image, transformed_bbox_list
    

    def get_pieces_position(self, transformed_bboxes, bboxes, h, w, h_resized, w_resized, labels):
        # unique position for each piece
        position_dict = set()
        prediction_result=[]
        for i, bbox in enumerate(transformed_bboxes):
            bbox = np.array(bbox, dtype=np.float32)
            # check if bbox is inside the TRANSFORMED board
            if np.any((bbox[:, 0] >= 0) & (bbox[:, 0] <= w) &
                        (bbox[:, 1] >= 0) & (bbox[:, 1] <= h)):
                # find center of piece
                x, y = ChessPositionRecognizer._center_of_piece(bbox[0], bbox[1])
                # determine position on chessboard
                letter = chr(ord('a') + int(x / w * 8))       
                num = str(8 - int(y / h * 8))
                # if position not already taken
                if f"{letter}{num}" not in position_dict:
                    position_dict.add(f"{letter}{num}")
                    x_min, y_min, x_max, y_max = bboxes[i]
                    prediction_result.append({
                        "id": int(labels[i]),
                        "position":letter+num,
                        "bbox":[float(x_min/w_resized), float(y_min/h_resized), float(x_max/w_resized), float(y_max/h_resized)]
                    })
        return prediction_result
    
    def recognice_position(self, image):
    
        h, w = image.shape[:2]
        # YOLO suggestion for image size 640x384 or 640x480 depending on aspect ratio
        aspect_ratio = w / h

        if 0.7 < aspect_ratio < 0.8 or 0.7 < 1/aspect_ratio < 0.8:
            target_size = (640, 480) if w > h else (480, 640)
        else:
            target_size = (640, 384) if w > h else (384, 640)

        resized = cv2.resize(image, target_size)

        board_results = self.model_board(resized, conf=0.25, imgsz=640)

        pieces_results = self.model_pieces(resized, conf=0.2, imgsz=640)

        try:
            # find corners of the board
            corners = self.find_corners(mask = board_results[0].masks.data[0].cpu().numpy())
            if corners is None:
                return []
            
            bboxes = pieces_results[0].boxes.xyxy.cpu().numpy()
            confidences = pieces_results[0].boxes.conf.cpu().numpy()

            # sorting bboxes by confidence
            indices = np.argsort(-confidences)
            bboxes_sorted = bboxes[indices]
            if len(bboxes)==0:
                return []
            bbox_list = ChessPositionRecognizer._convert_to_total_list(bboxes_sorted)
            labels = pieces_results[0].boxes.cls.cpu().numpy()
            # change perspective of image and bboxes (after this operation image is 640x640, only board)
            transformed_image, transformed_bbox_list = self.change_perspective(corners, resized, bbox_list)

            transformed_bboxes = ChessPositionRecognizer._convert_to_separate_list(transformed_bbox_list)
            h, w = transformed_image.shape[:2]
            h_resized, w_resized = resized.shape[:2]

            # get pieces position on chessboard based on transformed bboxes
            prediction_result = self.get_pieces_position(transformed_bboxes, bboxes, h, w, h_resized, w_resized, labels)
            return prediction_result
        except Exception:
            # in case of any error, return empty list - this is safe
            return []
    
    async def recognize_position_async(self, image):
        loop = asyncio.get_running_loop()
        return await loop.run_in_executor(None, self.recognice_position, image)
