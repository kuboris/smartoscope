This file contains the details about steps for training machine learing model.

1) https://www.tensorflow.org/versions/r0.12/get_started/os_setup#docker_installation
Install Tensorflow docker image on Azure - Done
Install GPU enabled Tensorflow docker image. - Done.

Select training data :
https://isic-archive.com/#images

Run docker with testing data:
docker run -it -v /mnt:/tf_files  gcr.io/tensorflow/tensorflow:latest-devel
