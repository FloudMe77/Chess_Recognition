import saved_position
from dataset_maker import Dataset_maker
def main(folder_path):
    model_board_path ="models/yolo_board_seg/weights/best.pt"
    model_pieces_path = "models/yolo_chess_piece/weights/best.pt"
    dataset_maker = Dataset_maker(model_board_path, model_pieces_path)
    dataset_maker.create_dataset(folder_path, "position_1", saved_position.position_1)
    dataset_maker.create_dataset(folder_path, "position_2", saved_position.position_2)
    dataset_maker.create_dataset(folder_path, "position_3", saved_position.position_3)
    dataset_maker.create_dataset(folder_path, "position_4", saved_position.position_4)
    dataset_maker.create_dataset(folder_path, "position_5", saved_position.position_5)
    dataset_maker.create_dataset(folder_path, "position_6", saved_position.position_6)
    dataset_maker.create_dataset(folder_path, "position_7", saved_position.position_7)
    dataset_maker.create_dataset(folder_path, "position_8", saved_position.position_8)
    dataset_maker.create_dataset(folder_path, "position_9", saved_position.position_9)
    dataset_maker.create_dataset(folder_path, "position_010", saved_position.position_10)

if __name__ == "__main__":
    main("datasets\\piece_recognition_set\\reinforce_set_part1")