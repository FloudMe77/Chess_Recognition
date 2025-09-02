import json

import os
def loop_iter(ids, data, folder_name ):
    for position_id in ids:
        file_path = data['images'][position_id]['file_name']
        file_path = "dataset_board/labels/"+folder_name+"/"+file_path.replace("jpg", "txt")
        print(file_path)
        general_corners = data['annotations']['corners']

        total_height = data['images'][position_id]['height']
        total_width = data['images'][position_id]['width']

        for conrner_info in general_corners:
            if conrner_info["image_id"] == position_id:
                bottom_right_x, bottom_right_y = conrner_info['corners']['bottom_right']
                top_right_x, top_right_y = conrner_info['corners']['top_right']
                bottom_left_x, bottom_left_y = conrner_info['corners']['bottom_left']
                top_left_x, top_left_y = conrner_info['corners']['top_left']
                max_x = max(bottom_right_x, top_right_x, bottom_left_x, top_left_x)
                min_x = min(bottom_right_x, top_right_x, bottom_left_x, top_left_x)
                max_y = max(bottom_right_y, top_right_y, bottom_left_y, top_left_y)
                min_y = min(bottom_right_y, top_right_y, bottom_left_y, top_left_y)
                line = (
                    f"{0} "
                    f"{((min_x + max_x)/2)/total_width} "
                    f"{((min_y + max_y)/2)/total_height} "
                    f"{(max_x-min_x)/total_width} "
                    f"{(max_y-min_y)/total_height} "
                    f"{top_left_x/total_width} {top_left_y/total_height} "
                    f"{top_right_x/total_width} {top_right_y/total_height} "
                    f"{bottom_right_x/total_width} {bottom_right_y/total_height} "
                    f"{bottom_left_x/total_width} {bottom_left_y/total_height}"
                )
                break

        os.makedirs(os.path.dirname(file_path), exist_ok=True)
        with open(file_path, 'w') as f:
            f.write(line)

def prepare_custom_labels():
    with open("annotations.json", "r", encoding="utf-8") as f:
        data = json.load(f)

    # print(train_ids)
    # print(data['splits']['chessred2k']['train']['image_ids'])
    loop_iter(data['splits']['chessred2k']['train']['image_ids'], data, "train" )
    loop_iter(data['splits']['chessred2k']['val']['image_ids'], data, "val" )
    # loop_iter(data['splits']['chessred2k']['test']['image_ids'], data, "test" )

prepare_custom_labels()
