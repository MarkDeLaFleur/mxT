# mxT
This project utilizes android studio / opencv to track Mexican Train Chickenfoot scores 
within the code there are layouts to review a summary of how many tiles have been played and what the current totals for each player are.
On the score layout you can click the camera icon and the code will expose the phones camera using the androidx camera controller object to take a picture 
(to memory) of the dice. It then passes through the jpeg imageproxy to bitmap to Mat. 
on the opencv side the code uses the simple blob detector to find the dots on the domino. It then uses contours to pull rectangles. The it uses a foreach 
loop to find each keypoint within the rectangles (note: there was a cool method within the keypoint called inside that takes the rectangle as the
constructor and returns true if the keypoint is within the rectangle. The code splits the rectangle into an A/B side so that you can sum the domino dots for
each side of the domino.

Summary:
Androidx CameraX Controller
Opencv (quickbird implementation 4.5.3.0)




