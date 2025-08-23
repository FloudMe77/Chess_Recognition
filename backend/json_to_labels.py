import json

import os
def loop_iter(ids, data, folder_name ):
    for position_id in ids:
        line = ""
        file_path = data['images'][position_id]['file_name']
        file_path = "dataset/labels/"+folder_name+"/"+file_path.replace("jpg", "txt")
        print(file_path)
        all_pieces = data['annotations']['pieces']

        total_height = data['images'][position_id]['height']
        total_width = data['images'][position_id]['width']

        for pieces_info in all_pieces:
            if pieces_info["image_id"] == position_id:
                start = pieces_info["id"]
                end = start
                # print(data['annotations']['pieces'])
                while end<len(all_pieces) and all_pieces[end]["image_id"] == position_id:
                    end+=1
                break
        for i in range(start,end):
            piece_id = all_pieces[i]['category_id']
            x1, y1, bbox_width, bbox_height = all_pieces[i]['bbox']
            line += f"{piece_id} {(x1 + bbox_width/2)/total_width} {(y1 + bbox_height/2)/total_height} {bbox_width/total_width} {bbox_height/total_height}\n"

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
    loop_iter(data['splits']['chessred2k']['test']['image_ids'], data, "test" )

prepare_custom_labels()
