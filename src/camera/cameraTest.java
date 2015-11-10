/**
 * 
 */
package camera;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
/**
 * @author Matheus
 *
 */
public class cameraTest extends Application {
	
	private ImageView currentFrame = new ImageView();
	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;
	// the OpenCV object that realizes the video capture
	private VideoCapture vCapture = new VideoCapture();
	// a flag to change the button behavior
	private boolean cameraActive = false;
	// container for video
	private HBox camBox = new HBox();
	// switch camera on/off
	private Button button = new Button("Click to begin streaming");
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		BorderPane basePane = new BorderPane();
		camBox.getChildren().add(currentFrame);
		basePane.setCenter(camBox);
		basePane.setBottom(button);
		
		button.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
			this.startCamera();
		});
		
		Scene scene = new Scene(basePane,800,600);
		scene.setFill(Color.BLACK);
		primaryStage.setTitle("Camera Stream");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	protected void startCamera() {	
		
		if (!this.cameraActive)
		{
			// start the video capture
			this.vCapture.open(0);
			
			// is the video stream available?
			if (this.vCapture.isOpened())
			{
				this.cameraActive = true;
				
				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = () -> {
						
						Image imageToShow = grabFrame();
						currentFrame.setImage(imageToShow);
				};
				
				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
				
				// update the button content
				this.button.setText("Stop Camera");
			}
			else
			{
				// log the error
				System.err.println("Impossible to open the camera connection...");
			}
		}
		else {
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
			this.button.setText("Start Camera");
			
			// stop the timer
			try
			{
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{
				// log the exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
			
			// release the camera
			this.vCapture.release();
			// clean the frame
			this.currentFrame.setImage(null);
		}
	}
	
	private Image grabFrame() {
		
		// init everything
		Image imageToShow = null;
		Mat frame = new Mat();
		
		// check if the capture is open
		if (this.vCapture.isOpened())
		{
			try
			{
				// read the current frame
				this.vCapture.read(frame);
				
				// if the frame is not empty, process it
				if (!frame.empty())
				{
					// convert the image to gray scale
					Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
					// convert the Mat object (OpenCV) to Image (JavaFX)
					imageToShow = mat2Image(frame);
				}
				
			}
			catch (Exception e)
			{
				// log the error
				System.err.println("Exception during the image elaboration: " + e);
			}
		}
		
		return imageToShow;
	}
	/**
	 * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
	 * 
	 * @param frame
	 *            the {@link Mat} representing the current frame
	 * @return the {@link Image} to show
	 */
	private Image mat2Image(Mat frame) {
		
		// create a temporary buffer
		MatOfByte buffer = new MatOfByte();
		// encode the frame in the buffer
		Imgcodecs.imencode(".png", frame, buffer);
		// build and return an Image created from the image encoded in the
		// buffer
		return new Image(new ByteArrayInputStream(buffer.toArray()));
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		launch(args);

	}
}
