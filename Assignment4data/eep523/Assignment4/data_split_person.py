# Lint as: python3
# coding=utf-8
# ==============================================================================

"""Split data into train, validation and test dataset according to person.

That is, use some people's data as train, some other people's data as
validation, and the rest ones' data as test. These data would be saved
separately under "/person_split".

It will generate new files with the following structure:
├──person_split
│   ├── test
│   ├── train
│   └──valid
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import os
import random
import csv
import json
from data_split import read_data


def person_split(whole_data, train_names, valid_names, test_names):  # pylint: disable=redefined-outer-name
  """Split data by person."""
  random.seed(30)
  random.shuffle(whole_data)
  train_data = []  # pylint: disable=redefined-outer-name
  valid_data = []  # pylint: disable=redefined-outer-name
  test_data = []  # pylint: disable=redefined-outer-name
  for idx, data in enumerate(whole_data):  # pylint: disable=redefined-outer-name,unused-variable
    if data["name"] in train_names:
      train_data.append(data)
    elif data["name"] in valid_names:
      valid_data.append(data)
    elif data["name"] in test_names:
      test_data.append(data)
  print("train_length:" + str(len(train_data)))
  print("valid_length:" + str(len(valid_data)))
  print("test_length:" + str(len(test_data)))
  return train_data, valid_data, test_data


# Write data to file
def write_data(data_to_write, path):
  with open(path, "w") as f:
    for idx, item in enumerate(data_to_write):  # pylint: disable=unused-variable,redefined-outer-name
      dic = json.dumps(item, ensure_ascii=False)
      f.write(dic)
      f.write("\n")


if __name__ == "__main__":
  data = read_data("./data/complete_data")
  train_names = [
      "hyw", "shiyun", "tangsy", "dengyl", "jiangyh", "xunkai", "negative3",
      "negative4", "negative5", "negative6"
  ]
  valid_names = ["lsj", "pengxl", "negative2", "negative7"]
  test_names = ["liucx", "zhangxy", "negative1", "negative8"]
  train_data, valid_data, test_data = person_split(data, train_names,
                                                   valid_names, test_names)
  if not os.path.exists("./person_split"):
    os.makedirs("./person_split")
  write_data(train_data, "./person_split/train")
  write_data(valid_data, "./person_split/valid")
  write_data(test_data, "./person_split/test")
