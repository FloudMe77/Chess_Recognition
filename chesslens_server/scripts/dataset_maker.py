import os
from chess_rec.chess_position_recognizer import ChessPositionRecognizer
import cv2

class Dataset_maker():
    def __init__(self, model_board_path, model_pieces_path):
        self.chess_recognision_model = ChessPositionRecognizer(model_board_path, model_pieces_path)
        
    def _create_label_file(self, image, position_dict):
        # get prediction
        prediction_results = self.chess_recognision_model.recognice_position(image)
        lines=""
        for prediction_piece in prediction_results:
            position = prediction_piece["position"]
            x_min, y_min, x_max, y_max = prediction_piece["bbox"]
            if position in position_dict:
                # changeing to yolo format, change detected label to actual label from position_dict
                lines += f"{position_dict[position]} {(x_max + x_min)/2} {(y_max + y_min)/2} {x_max-x_min} {y_max-y_min}\n"
        return lines

    def create_dataset(self, folder_path, position_name, position_dict):
        image_folder = folder_path+"/images/train"
        labels_folder = folder_path+"/labels/train"
        for file_name in os.listdir(image_folder):
            # simple check if image contains given position
            if position_name in file_name:
                # reading image and creating label file
                image = cv2.imread(image_folder +"/" + file_name)
                label_path = labels_folder + "/"+ file_name.replace("jpg", "txt")
                lines = self._create_label_file(image, position_dict)
                os.makedirs(os.path.dirname(label_path), exist_ok=True)
                with open(label_path, 'w') as f:
                    f.write(lines)

