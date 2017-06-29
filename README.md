Updated. Working app in Alpha Version.
========================================================================
## Download alpha version of our app [here](https://drive.google.com/file/d/0BxbqxKOhDjB6blRKVUtMMTJ2UTA/view?usp=sharing)
Update 2
As per request added [mirror for waya Skin cancer dataset](https://drive.google.com/open?id=0BxbqxKOhDjB6UzdQSkRwMjhCcW8).

Short App Introduction:
--------------------------------
1. Download apk in your phone.( You might be asked to approve an app from unknown source)
2. You will be asked to download a newest model. ( It's 90mb. Use WiFi if available)
3. Wait for download to finish.

<a href="https://github.com/kuboris/smartoscope/blob/master/img/approve.png"><img src="https://github.com/kuboris/smartoscope/blob/master/img/approve.png" align="center" height="23%" width="23%" ></a>
<a href="https://github.com/kuboris/smartoscope/blob/master/img/model.png"><img src="https://github.com/kuboris/smartoscope/blob/master/img/model.png" align="center" height="23%" width="23%" ></a>
<a href="https://github.com/kuboris/smartoscope/blob/master/img/download.png"><img src="https://github.com/kuboris/smartoscope/blob/master/img/download.png" align="center" height="23%" width="23%" ></a>

4. App has 4 classes.
  * Other ( Blue color ) - Model classifies surroundings.  
  * Skin  ( Yellow color ) - Model classifies skin.  
  * Mole  ( Green color) Model classifies picture as non-malignant.
  * Melanoma ( Red color) Model classifies picture as malignant.
  
<a href="https://github.com/kuboris/smartoscope/blob/master/img/other.png"><img src="https://github.com/kuboris/smartoscope/blob/master/img/other.png" align="center" height="23%" width="23%" ></a>
<a href="https://github.com/kuboris/smartoscope/blob/master/img/skin-detected.png"><img src="https://github.com/kuboris/smartoscope/blob/master/img/skin-detected.png" align="center" height="23%" width="23%" ></a>
<a href="https://github.com/kuboris/smartoscope/blob/master/img/mole-detected.png"><img src="https://github.com/kuboris/smartoscope/blob/master/img/mole-detected.png" align="center" height="23%" width="23%" ></a>
<a href="https://github.com/kuboris/smartoscope/blob/master/img/melanoma-detected.png"><img src="https://github.com/kuboris/smartoscope/blob/master/img/melanoma-detected.png" align="center" height="23%" width="23%" ></a>

5. Additionally you can start debug mode by pressing volume down button. (Image in the corner is the real image that is being processed by the model)

<a href="https://github.com/kuboris/smartoscope/blob/master/img/debug-mode.png"><img src="https://github.com/kuboris/smartoscope/blob/master/img/debug-mode.png" align="center" height="23%" width="23%" ></a>

Warning
--------------------------------
This is not medically approved app. Please consult with a professional when you have a doubt.

Make sure that the image is focused. It might produce false positives or false negatives otherwise.

We tested it with cheap microscope add-on.( [example](https://images-na.ssl-images-amazon.com/images/I/41kfY-wrqaL.jpg) ) If your phone doesn't allow for big enough macro shots it's advised to take a picture normally and visualize it on the other screen for to get a bigger picture.


Building an App that uses neural network for real-time skin cancer detection.
========================================================================

>"Innovation is taking two things that already exist and putting them together in a new way." Tom Freston

When we decided to attend V4 [hackathon](https://developer.att.com/static-assets/documents/events/V4_HackFlyer_021517.pdf) we had a simple goal :
Use the open source tools to develop a simple Android app to recognize skin cancer in real-time.

Step zero: Problem definition
--------------------------------

Melanoma is the most common human malignant cancer. With millions of new cases each year it accounts for 75 % of skin cancer deaths - more than 50 thousand deaths per year. Catching it early greatly increases chances of survival and enables removing it. It's primarily diagnosed visually following with biopsy in uncertain cases. Mole can be Benign(Just a mole) or Malignant(Cancerous cells).

![enter image description here](http://www.drspitler.com/images/slide4.jpg)

[Recent deep learning advances](https://www.youtube.com/watch?v=toK1OSLep3s) showed that they can predict malignant mole from the picture with 95% accuracy. 
Sadly none of those models are publicly available.

Step one: Research source data.
-------------------------------

Skin cancer [dataset](https://isic-archive.com/#images).
 - Contains 13786 High res Images divided into categories. For our cause we selected only two categories from this website : NEVUS( Mole) / MELANOMA(Malignant)
 - [Download Mole link](https://isic-archive.com/api/v1/image/download?filter=%7B%22operator%22:%22and%22,%22operands%22:%5B%7B%22operator%22:%22not%20in%22,%22operands%22:%5B%7B%22identifier%22:%22meta.clinical.benign_malignant%22,%22type%22:%22string%22%7D,%5B%22indeterminate%22,%22indeterminate/benign%22,%22indeterminate/malignant%22,%22malignant%22,%22__null__%22%5D%5D%7D,%7B%22operator%22:%22and%22,%22operands%22:%5B%7B%22operator%22:%22not%20in%22,%22operands%22:%5B%7B%22identifier%22:%22meta.clinical.diagnosis%22,%22type%22:%22string%22%7D,%5B%22actinic%20keratosis%22,%22angiofibroma%20or%20fibrous%20papule%22,%22angioma%22,%22atypical%20melanocytic%20proliferation%22,%22basal%20cell%20carcinoma%22,%22dermatofibroma%22,%22lentigo%20NOS%22,%22lentigo%20simplex%22,%22lichenoid%20keratosis%22,%22melanoma%22,%22other%22,%22scar%22,%22seborrheic%20keratosis%22,%22solar%20lentigo%22,%22squamous%20cell%20carcinoma%22,%22__null__%22%5D%5D%7D,%7B%22operator%22:%22in%22,%22operands%22:%5B%7B%22identifier%22:%22meta.tags%22,%22type%22:%22string%22%7D,%5B%22ISBI%202016:%20Test%22,%22ISBI%202016:%20Training%22,%22ISBI%202017:%20Test%22,%22ISBI%202017:%20Training%22,%22ISBI%202017:%20Validation%22,%22accepted%22,%5B%5D%5D%5D%7D%5D%7D%5D%7D)(Few GB)
 - [Download Malignant Link](https://isic-archive.com/api/v1/image/download?filter=%7B%22operator%22:%22and%22,%22operands%22:%5B%7B%22operator%22:%22not%20in%22,%22operands%22:%5B%7B%22identifier%22:%22meta.clinical.benign_malignant%22,%22type%22:%22string%22%7D,%5B%22benign%22,%22indeterminate%22,%22indeterminate/benign%22,%22indeterminate/malignant%22,%22__null__%22%5D%5D%7D,%7B%22operator%22:%22and%22,%22operands%22:%5B%7B%22operator%22:%22not%20in%22,%22operands%22:%5B%7B%22identifier%22:%22meta.clinical.diagnosis%22,%22type%22:%22string%22%7D,%5B%22actinic%20keratosis%22,%22angiofibroma%20or%20fibrous%20papule%22,%22angioma%22,%22atypical%20melanocytic%20proliferation%22,%22basal%20cell%20carcinoma%22,%22dermatofibroma%22,%22lentigo%20NOS%22,%22lentigo%20simplex%22,%22lichenoid%20keratosis%22,%22nevus%22,%22other%22,%22scar%22,%22seborrheic%20keratosis%22,%22solar%20lentigo%22,%22squamous%20cell%20carcinoma%22,%22__null__%22%5D%5D%7D,%7B%22operator%22:%22in%22,%22operands%22:%5B%7B%22identifier%22:%22meta.tags%22,%22type%22:%22string%22%7D,%5B%22ISBI%202016:%20Test%22,%22ISBI%202016:%20Training%22,%22ISBI%202017:%20Test%22,%22ISBI%202017:%20Training%22,%22ISBI%202017:%20Validation%22,%22accepted%22,%5B%5D%5D%5D%7D%5D%7D%5D%7D)(Few GB)
 - Get additional data set from Waia.ai git repo:
https://github.com/wayaai/waya-dc/blob/master/wayadc/utils/get_datasets.py
 - Waia.ai sources use preprocessed 299x299 images. Same input as our model uses. However we want get high resolution images so we’ll be able to do some processing of our own an image cropping if needed.
 - Additional Source 1:(tbd)
 - Additional Source 2:(tbd)
 - Additional Source 3:(tbd)
Step two: Training of a Model
-----------------------------
We planned to use Azure graphic card instance. However the installation of Cuda drivers proved to be buggy and we could not afford to lose more time on it so we went Tensorflow Docker image on high compute Azure instance. (H8 instance)
Most of the steps used are could be found in this tutorial : https://codelabs.developers.google.com/codelabs/tensorflow-for-poets
However we prepared step by step tutorial for training your neural network classifier on Azure:
ADD LINK

Proposed ROAD-MAP :
===================
Future development is dependent on our time abitities.
This is a first draft of Roadmap for App development.

 - Version 0.0 : APP: Stable apk. (Current version is still buggy)
                          Model: Default model trained during hackathon.
**(DONE)**
Version 0.1 : APP: Smaller app, model downloaded from remote server **(DONE)**
                       Model: Trained on full sized images using --random_brightness 10 --flip_left_right true **(IN PROGRESS)**
 - Version 0.2 : APP: Custom model import, custom categories
                          Model: Getting to 90% accuracy. Testing with real world examples. (It’s hard to get a               real skin
   cancer patient)
 - Version 0.3: APP: App-store. Proper medicinal texts in app.
                                  Model: Real world tested accuracy.

We need you help !
------------------
We believe this project could help people. Do you believe the same?
What do we need help with at the moment?
1) Android Designer:  Are you experienced android designer? Our app needs to get some slick visuals! Contact us!
2) Azure/Amazon/? Virtual machine provider. Training takes a lot of CPU/GPU power. Are you able to provide us with credits to train it? Contact us!
3) Doctors. Are you interested in this app? Do you have experience with skin melanoma? Would you like to test our app?
4) Anyone with an idea - Do you wan't to help us with anything else ? Do you have suggestions? Contact us!

Contact email : kuboklauco(magic at sign)gmail(dot)com

