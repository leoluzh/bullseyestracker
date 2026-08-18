# Third-Party Notices

## DeepDarts dataset (DNN detection model training data)

The bundled DNN detection model (`cv/src/main/assets/models/deepdarts-yolov8.onnx`, spec
`014-dnn-dart-detection`) was trained on the **DeepDarts** dataset:

- McNally, W., Walters, P., Vats, K., Wong, A., McPhee, J. — *DeepDarts: Modeling Keypoints as
  Objects for Automatic Scorekeeping in Darts using a Single Camera* (CVSports 2021).
  https://arxiv.org/abs/2105.09880
- Dataset: https://ieee-dataport.org/open-access/deepdarts-dataset
- YOLOv8 object-detection conversion used for training:
  https://universe.roboflow.com/testing-zzmc9/deepdarts-yolov8

Licensed under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/). No modifications were
made to the dataset itself; a YOLOv8n model was trained on it locally and exported to ONNX for
fully offline, on-device inference (constitution Principle I) — no data or inference calls leave
the device.
