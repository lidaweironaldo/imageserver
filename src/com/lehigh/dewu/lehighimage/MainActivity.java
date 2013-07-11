package com.lehigh.dewu.lehighimage;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
public class MainActivity extends Activity {

	private InputStream inputStream;
	public static final int PICTURE_ACTIVITY = 35434;
	private static final int SELECT_PHOTO = 100;
	protected static final int TAKE_PICTURE = 0;
	private Uri imageUri = null;
	private String imagePath = "";
	private int trigger = 0;
    public static Context context;
    FeatureDetector detector = null;
   
    double checkGreenRate(Mat img)
    {
    	double totalB1 =  0.0;
    	double totalG1 = 0.0;
    	double totalR1 = 0.0;
    	double borr = 0.0;
    	int count1 = 0;
    	
    	/*
    	 * check all pixels
    	for(int i =0; i<img.rows(); i++)
    	{
    		for(int j=0;j<img.cols(); j++)
    		{
    			count1++;
    			totalB1+=img.get(i, j)[0];
    			totalG1+=img.get(i, j)[1];
    			totalR1+=img.get(i, j)[2];
    		}
    	}
    	*/
    	
    	//check random 100 pixels
    	count1=100;
    	int row = img.rows();
    	int col = img.cols();
    	for (int i=0; i<100; i++)
    	{
    		Random random = new Random();
    		int x = random.nextInt(row);
    		int y = random.nextInt(col);
			totalB1+=img.get(x, y)[0];
			totalG1+=img.get(x, y)[1];
			totalR1+=img.get(x, y)[2];
    		//random = new Random();
    	}
    	
    	if (totalB1>totalR1)
    	{
    		borr = totalB1;
    	}
    	else
    	{
    		borr = totalR1;
    	}
    	double avgG = totalG1/count1;
    	double rate = ((avgG-borr/count1)/avgG);
    	Log.d("green", String.valueOf(rate));
    	
    	//Log.d("", img[0]);
    	return rate;
    }
    
    boolean checkGreen(Mat img)
    {
    	double totalB1 =  0.0;
    	double totalG1 = 0.0;
    	double totalR1 = 0.0;
    	double borr = 0.0;
    	int count1 = 0;
    	
    	/*
    	 * check all pixels
    	for(int i =0; i<img.rows(); i++)
    	{
    		for(int j=0;j<img.cols(); j++)
    		{
    			count1++;
    			totalB1+=img.get(i, j)[0];
    			totalG1+=img.get(i, j)[1];
    			totalR1+=img.get(i, j)[2];
    		}
    	}
    	*/
    	
    	//check random 100 pixels
    	count1=100;
    	int row = img.rows();
    	int col = img.cols();
    	for (int i=0; i<100; i++)
    	{
    		Random random = new Random();
    		int x = random.nextInt(row);
    		int y = random.nextInt(col);
			totalB1+=img.get(x, y)[0];
			totalG1+=img.get(x, y)[1];
			totalR1+=img.get(x, y)[2];
    		//random = new Random();
    	}
    	
    	if (totalB1>totalR1)
    	{
    		borr = totalB1;
    	}
    	else
    	{
    		borr = totalR1;
    	}
    	double avgG = totalG1/count1;
    	double rate = ((avgG-borr/count1)/avgG);
    	Log.d("green", String.valueOf(rate));
    	
    	boolean result;
    	
    	if (rate>0.1)
    	{
    		result = true;
    	}
    	else 
    	{
    		result = false;
    	}
    	
    	//Log.d("", img[0]);
    	return result;
    }
    
    int getMax(double a, double b, double c, double d)
    {
    	int p = 0;
    	double temp;
    	if(a>b)
    	{
    		temp = a;
    		p = 1;
    	}
    	else
    	{
    		temp = b;
    		p = 2;
    	}
    	
    	if(temp<c)
    	{
    		temp = c;
    		p = 3;
    	}
    	
    	if(temp<d)
    	{
    		temp=d;
    		p = 4;
    	}
    	if(temp>0.1)
    	{
    		return p;
    	}
    	else
    	{
    		return 0;
    	}
    }
    
    Mat solveGreenOrder(Mat img)
    {
    	Mat img1 = img.submat(0, (img.rows())/8, 0, img.cols());
    	Mat img2 = img.submat((img.rows())*7/8,img.rows(), 0, img.cols());
    	Mat img3 = img.submat(0, img.rows(), 0, (img.cols())/8);
    	Mat img4 = img.submat(0, img.rows(),(img.cols())*7/8, img.cols());
    	double rate1 = checkGreenRate(img1);
    	double rate2 = checkGreenRate(img2);
    	double rate3 = checkGreenRate(img3);
    	double rate4 = checkGreenRate(img4);
    	int pos = getMax(rate1, rate2, rate3, rate4);
    	if(pos == 1)
    	{
    		img = img.submat((img.rows())/8, img.rows(),0,  img.cols());
    		return solveGreenOrder(img);
    	}
    	if(pos == 2)
    	{
    		img = img.submat(0, (img.rows())*7/8, 0, img.cols());
    		return solveGreenOrder(img);
    	}
    	if(pos == 3)
    	{
    		img = img.submat(0, img.rows(), (img.cols())/8, img.cols());
    		return solveGreenOrder(img);
    	}
    	if(pos == 4)
    	{
    		img = img.submat(0, img.rows(),0,(img.cols())*7/8);
    		return solveGreenOrder(img);
    	}
    	return img;
    }
    
    Mat solveGreen(Mat img)
    {	
    	Mat img1 = img.submat(0, (img.rows())/8, 0, img.cols());
    	if( checkGreen(img1) )
    	{
    		img = img.submat((img.rows())/8, img.rows(),0,  img.cols());
    		return solveGreen1(img);
    	}
    	else
    	{
    		img1 = img.submat(0, (img.rows())/16, 0, img.cols());
    		if(checkGreen(img1))
    		{
    			img = img.submat((img.rows())/16, img.rows(),0,  img.cols());
    			return solveGreen1(img);
    		}
    	}
    	
    	img1 = img.submat((img.rows())*7/8,img.rows(), 0, img.cols());
    	if(checkGreen(img1))
    	{
    		img = img.submat(0, (img.rows())*7/8, 0, img.cols());
    		return solveGreen1(img);
    	}
    	else
    	{
    		img1 = img.submat((img.rows())*15/16, img.rows(), 0, img.cols());
    		if(checkGreen(img1))
    		{
    			img = img.submat(0, (img.rows())*15/16, 0, img.cols());
    			return solveGreen1(img);
    		}
    	}
    	
    	img1 = img.submat(0, img.rows(), 0, (img.cols())/8);
    	if(checkGreen(img1))
    	{
    		img = img.submat(0, img.rows(), (img.cols())/8, img.cols());
    		return solveGreen1(img);
    	}
    	else
    	{
    		img1 = img.submat(0, img.rows(), 0, (img.cols())/16);
    		if(checkGreen(img1))
    		{
    			img = img.submat(0, img.rows(), (img.cols())/16, img.cols());
    			return solveGreen1(img);
    		}
    	}
    	
    	img1 = img.submat(0, img.rows(),(img.cols())*7/8, img.cols());
    	if(checkGreen(img1))
    	{
    		img = img.submat(0, img.rows(),0,(img.cols())*7/8);
    		return solveGreen1(img);
    	}
    	else
    	{
    		img1 = img.submat(0, img.rows(),(img.cols())*15/16, img.cols());
    		if(checkGreen(img1))
    		{
    			img = img.submat(0, img.rows(),0,(img.cols())*15/16);
    			return solveGreen1(img);
    		}
    	}
    	
    	return img;
    }
    
    Mat solveGreen1(Mat img)
    {	
    	
    	Mat img1 = img.submat(0, img.rows(), 0, (img.cols())/8);
    	if(checkGreen(img1))
    	{
    		img = img.submat(0, img.rows(), (img.cols())/8, img.cols());
    		return solveGreen(img);
    	}
    	else
    	{
    		img1 = img.submat(0, img.rows(), 0, (img.cols())/16);
    		if(checkGreen(img1))
    		{
    			img = img.submat(0, img.rows(), (img.cols())/16, img.cols());
    			return solveGreen(img);
    		}
    	}
    	
    	img1 = img.submat(0, img.rows(),(img.cols())*7/8, img.cols());
    	if(checkGreen(img1))
    	{
    		img = img.submat(0, img.rows(),0,(img.cols())*7/8);
    		return solveGreen(img);
    	}
    	else
    	{
    		img1 = img.submat(0, img.rows(),(img.cols())*15/16, img.cols());
    		if(checkGreen(img1))
    		{
    			img = img.submat(0, img.rows(),0,(img.cols())*15/16);
    			return solveGreen(img);
    		}
    	}
    	
    	img1 = img.submat(0, (img.rows())/8, 0, img.cols());
    	if( checkGreen(img1) )
    	{
    		img = img.submat((img.rows())/8, img.rows(),0,  img.cols());
    		return solveGreen(img);
    	}
    	else
    	{
    		img1 = img.submat(0, (img.rows())/16, 0, img.cols());
    		if(checkGreen(img1))
    		{
    			img = img.submat((img.rows())/16, img.rows(),0,  img.cols());
    			return solveGreen(img);
    		}
    	}
    	
    	img1 = img.submat((img.rows())*7/8,img.rows(), 0, img.cols());
    	if(checkGreen(img1))
    	{
    		img = img.submat(0, (img.rows())*7/8, 0, img.cols());
    		return solveGreen(img);
    	}
    	else
    	{
    		img1 = img.submat((img.rows())*15/16, img.rows(), 0, img.cols());
    		if(checkGreen(img1))
    		{
    			img = img.submat(0, (img.rows())*15/16, 0, img.cols());
    			return solveGreen(img);
    		}
    	}
    	
    	return img;
    }
    
    
    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
    	@Override
    	public void onManagerConnected(int status) {
    	   switch (status) {
    	       case LoaderCallbackInterface.SUCCESS:
    	       {
    	        // Create and set View
    	        setContentView(R.layout.activity_main);
    	        
    	        final Button camera= (Button) findViewById(R.id.camera_button);
    	        final Button retrieve = (Button) findViewById(R.id.retrieve_button);
    	        final Button gallery = (Button) findViewById(R.id.gallery_button);
				final EditText resultText = (EditText) findViewById(R.id.resultbox);
				
    	        gallery.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						resultText.setText("Click \"Retrieve\" for result...");
						Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
						photoPickerIntent.setType("image/*");
						startActivityForResult(photoPickerIntent, SELECT_PHOTO);   
					}
    	        	
    	        });
    	        
    	        camera.setOnClickListener(new OnClickListener() {
    				
    				@Override
    				public void onClick(View v) {
    					// TODO Auto-generated method stub
    					resultText.setText("Click \"Retrieve\" for result...");
    				    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
    				    File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
    				    intent.putExtra(MediaStore.EXTRA_OUTPUT,
    				            Uri.fromFile(photo));
    				    imageUri = Uri.fromFile(photo);
    				    startActivityForResult(intent, TAKE_PICTURE);
    				}
    			});
    	        
    	        
    	        
    	        retrieve.setOnClickListener(new OnClickListener() {
    				
    				@Override
    				public void onClick(View v) {
    					if (trigger !=0){
    						long start = System.currentTimeMillis();
    						//String filename = getTempFileName("yml");
    						//writeFile(filename, "%YAML:1.0\nhessianThreshold: 8000.\noctaves:3\noctaveLayers: 4\nupright: 0\n");     			    	    
    						//detector = FeatureDetector.create(1);
    						//DescriptorExtractor de = DescriptorExtractor.create(DescriptorExtractor.BRIEF);
    						Mat mImage = Highgui.imread(imagePath, 1);
    						//mImage = solveGreen(mImage);	
    						//Mat m1 = mImage[1:22,23:222];
    						
    						/*resize image*/
    						//Size dsize = new Size(480, 640);
    						//Imgproc.resize(mImage, mImage, dsize);
    						
    						mImage = solveGreenOrder(mImage);
    						String tempPath = "/mnt/sdcard/DCIM/Camera/lmr.jpg";
    						Highgui.imwrite(tempPath, mImage);
    						//MyFeatureDetector df = (MyFeatureDetector) FeatureDetector.create(FeatureDetector.SURF);
    				        //detector.read(filename);
    						//Mat destImage = new Mat();
    						//Mat desc = new Mat();
    						//MatOfKeyPoint keyPoint = new MatOfKeyPoint();
    						//detector.detect();
    						//detector.detect(mImage, keyPoint);
    						//de.compute(mImage, keyPoint, desc);
    						//List<Float> fs;
    						//fs = new ArrayList<Float>();
    						//Converters.Mat_to_vector_float(desc, fs);
    						/*try {
								sendFile(imagePath);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}*/
    						
    						ByteArrayOutputStream stream = new ByteArrayOutputStream();
    						{
    						Bitmap image = BitmapFactory.decodeFile(tempPath);
    						image.compress(Bitmap.CompressFormat.JPEG, 90, stream);
    						}
    						System.gc();
    						byte [] byte_arr = stream.toByteArray();
    						String image_str = Base64.encodeBytes(byte_arr);
    						ArrayList<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();

    						nameValuePairs.add(new BasicNameValuePair("upfile",image_str));
    						
    						//for (int i =0; i<2; i++)
    						//{
    						try {
    							HttpClient httpclient = new DefaultHttpClient();
    							final String URL = "http://128.180.121.241:8000";
    							//final String URL = "http://192.168.0.10:8000";
    							
    							HttpPost httppost = new HttpPost(URL);
    							httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    							HttpResponse response = httpclient.execute(httppost);
    							String the_string_response = convertResponseToString(response);
    							resultText.setText(imagePath+"\n\nResult:"+the_string_response);
    							long end = System.currentTimeMillis();
    							long time = end - start;
    							Log.d("time",  Long.toString(time));
    							//Toast.makeText(MainActivity.this, "Response: " + the_string_response, Toast.LENGTH_LONG).show();
    							} catch(Exception e){
    								Toast.makeText(MainActivity.this, "ERROR " + e.getMessage(), Toast.LENGTH_LONG).show();
    							System.out.println("Error in http connection "+e.toString());
    							e.printStackTrace();
    							}
    						}
    						//communication();
    						
    						//}
    					}
    				});
    	       } break;
    	       default:
    	       {
    	      super.onManagerConnected(status);
    	       } break;
    	   }
    	    }
    	};
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
          }

        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mOpenCVCallBack))
        {
          Log.e("errorrrr", "Cannot connect to OpenCV Manager");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case TAKE_PICTURE:
            if (resultCode == Activity.RESULT_OK) {
            	trigger = 1;
                Uri selectedImage = imageUri;
                getContentResolver().notifyChange(selectedImage, null);
                ImageView imageView = (ImageView) findViewById(R.id.lehigh_image);
                ContentResolver cr = getContentResolver();
                Bitmap bitmap;
                try {
                     bitmap = android.provider.MediaStore.Images.Media
                     .getBitmap(cr, selectedImage);

                    imageView.setImageBitmap(bitmap);
                    imagePath = selectedImage.toString().substring(11);

                } catch (Exception e) {
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
                            .show();
                    Log.e("Camera", e.toString());
                }
            }
            break;
        case SELECT_PHOTO:
            if(resultCode == RESULT_OK){
            	trigger = 1;
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();
                
                //get orientation of the image
                ExifInterface exif = null;
                imagePath = filePath;
                try {
					exif = new ExifInterface(filePath);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                int degree = exifToDegrees(orientation);
                
                
                Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
                Matrix matrix = new Matrix();
                matrix.postRotate(degree);
                ImageView imageView = (ImageView) findViewById(R.id.lehigh_image);
                int width = yourSelectedImage.getWidth();
                if (yourSelectedImage.getWidth() > imageView.getWidth())
                {
                	width = imageView.getWidth();
                }
                int height = yourSelectedImage.getHeight();
                if (yourSelectedImage.getHeight() > imageView.getHeight())
                {
                	height = imageView.getHeight();
                }
                Bitmap interBitmap = Bitmap.createScaledBitmap(yourSelectedImage, width, height, false);
                Bitmap rotatedBitmap = Bitmap.createBitmap(interBitmap, 0, 0, width, height, matrix, true);
                
                
                imageView.setImageBitmap(rotatedBitmap);
            }

        }
    }
    
    private static int exifToDegrees(int exifOrientation) {        
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; } 
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; } 
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }            
        return 0;    
    }
    
    public String convertResponseToString(HttpResponse response) throws IllegalStateException, IOException{
    	String res = "";
    	StringBuffer buffer = new StringBuffer();
    	inputStream = response.getEntity().getContent();
    	int contentLength = (int) response.getEntity().getContentLength(); //getting content length…..
    	//Toast.makeText(MainActivity.this, "contentLength : " + contentLength, Toast.LENGTH_LONG).show();
    	if (contentLength < 0){ 
    		
    	} 
    	else{ 
    		byte[] data = new byte[512]; 
    		int len = 0; 
    		try { 
    			while (-1 != (len = inputStream.read(data)) ) 
    			{ 
    				buffer.append(new String(data, 0, len)); //converting to string and appending to stringbuffer….. 
    			} 
    			} catch (IOException e)
    			{ e.printStackTrace(); } 
    		try { 
    			inputStream.close(); // closing the stream….. 
    		} 
    		catch (IOException e) 
    		{ e.printStackTrace(); } 
    		res = buffer.toString(); // converting stringbuffer to string…..
    		//Toast.makeText(MainActivity.this, "Result : " + res, Toast.LENGTH_LONG).show(); //System.out.println("Response => " + EntityUtils.toString(response.getEntity()));}
    		}
    		return res;
    }
    
    public void sendFile(String path) throws IOException{
    	Socket socket = null;
    	OutputStream dataOutputStream = null;
    	DataInputStream dataInputStream = null;
    	try {
    		socket = new Socket("128.180.121.241", 54321);
    		dataOutputStream = socket.getOutputStream();
    		File f = new File(path);
    		byte [] buffer = new byte[(int)f.length()];
    		FileInputStream fis = new FileInputStream(f);
    		BufferedInputStream bis = new BufferedInputStream(fis);
    		bis.read(buffer,0,buffer.length);
    		dataOutputStream.write(buffer,0,buffer.length);
    		dataOutputStream.flush();
    	}catch (UnknownHostException e) {
  		  // TODO Auto-generated catch block
  		  e.printStackTrace();
  		 } catch (IOException e) {
  		  // TODO Auto-generated catch block
  		  e.printStackTrace();
  		 }
  		 finally{
  		  if (socket != null){
  		   try {
  		    socket.close();
  		   } catch (IOException e) {
  		    // TODO Auto-generated catch block
  		    e.printStackTrace();
  		   }
  		  }
  		  if (dataOutputStream != null){
  			   try {
  			    dataOutputStream.close();
  			   } catch (IOException e) {
  			    // TODO Auto-generated catch block
  			    e.printStackTrace();
  			   }
  			  }

  			  if (dataInputStream != null){
  			   try {
  			    dataInputStream.close();
  			   } catch (IOException e) {
  			    // TODO Auto-generated catch block
  			    e.printStackTrace();
  			   }
  			  }
  			 }

    }

    public void communication()
    {
    	Socket socket = null;
    	DataOutputStream dataOutputStream = null;
    	DataInputStream dataInputStream = null;
    	
    	try {
    		 socket = new Socket("128.180.121.241", 2345);
    		 dataOutputStream = new DataOutputStream(socket.getOutputStream());
    		 dataInputStream = new DataInputStream(socket.getInputStream());
    		 dataOutputStream.writeUTF("test android");
    		 Log.d("rec",dataInputStream.readUTF());
    	}catch (UnknownHostException e) {
    		  // TODO Auto-generated catch block
    		  e.printStackTrace();
    		 } catch (IOException e) {
    		  // TODO Auto-generated catch block
    		  e.printStackTrace();
    		 }
    		 finally{
    		  if (socket != null){
    		   try {
    		    socket.close();
    		   } catch (IOException e) {
    		    // TODO Auto-generated catch block
    		    e.printStackTrace();
    		   }
    		  }
    		  if (dataOutputStream != null){
    			   try {
    			    dataOutputStream.close();
    			   } catch (IOException e) {
    			    // TODO Auto-generated catch block
    			    e.printStackTrace();
    			   }
    			  }

    			  if (dataInputStream != null){
    			   try {
    			    dataInputStream.close();
    			   } catch (IOException e) {
    			    // TODO Auto-generated catch block
    			    e.printStackTrace();
    			   }
    			  }
    			 }

    }
    
    public String getTempFileName(String extension)
    {
        File cache = MainActivity.this.getCacheDir();
        if (!extension.startsWith("."))
            extension = "." + extension;
        try {
            File tmp = File.createTempFile("OpenCV", extension, cache);
            String path = tmp.getAbsolutePath();
            tmp.delete();
            return path;
        } catch (IOException e) {
            Log.d("error", "Failed to get temp file name. Exception is thrown: " + e);
        }
        return null;
    }
    protected void writeFile(String path, String content) {
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(new File(path));
            FileChannel fc = stream.getChannel();
            fc.write(Charset.defaultCharset().encode(content));
        } catch (IOException e) {
            Log.d("error","Failed to write file \"" + path
                    + "\". Exception is thrown: " + e);
        } finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.d("error","Exception is thrown: " + e);
                }
        }
    }
}
