# mxT
This project utilizes android studio / opencv to track Mexican Train Chickenfoot scores. Within the code there are layouts to review a summary of how many tiles have been played and what the current totals for each player are. On the score layout you can click the camera icon and the code will expose the device's camera using the androidx camerax controller object to take a picture (to memory) of the dice. It then passes through the jpeg imageproxy to bitmap to Mat. 
Opencv is used (simple blob detector) to find the domino's dots captured in the picture. It then uses contours to get the bounding rectangles of the dominos. Using foreach loops it finds each keypoint within the rectangle, draws the keypoints and rectangles on the image and then converts it back to a bitmap. Note: There was a cool method within the keypoint class called inside that takes the rectangle as its constructor and returns true if the keypoint is within the rectangle. The code splits the rectangle into an A/B side so that you can sum the domino dots for
each side of the domino.

Summary:
Androidx CameraX Controller, capture to memory, Fragments, Shared Viewmodel
Opencv (quickbird implementation 4.5.3.0) simpleblobdetector, imgproc




