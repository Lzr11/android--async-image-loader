package com.android.imageloader.core;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

public class AsyncImageloader {

	public static final String TAG="AsyncImageloader";
	
	private static Map <String, SoftReference<Bitmap>> imageCaches;
	private static List<Task> taskQuence;
	private static AsyncImageloader instance;
	
	public AsyncImageloader(){
		imageCaches=new HashMap<String,SoftReference<Bitmap>>();
		taskQuence=new ArrayList<Task>();
		new Thread(runnable).start();
	}
	
	public static AsyncImageloader getInstance(){
			if(instance==null)
			{
				synchronized (Object.class) {
					if(instance==null)
					       instance=new AsyncImageloader();
				}
				
			}
		return instance;
	}
	public void displayImage(ImageView imageView,String netPath,int resourceId){
		imageView.setTag(netPath);
		Bitmap cacheBitmap;
		if((cacheBitmap=loadBitmap(netPath,getCallback(imageView)))!=null){
			imageView.setImageBitmap(cacheBitmap);
		}
		else{
			imageView.setImageResource(resourceId);
		}
	}
	private Bitmap loadBitmap(String netPath,ImageCallback imageCallback){
		Bitmap imageBitmap = null;
		if(imageCaches.containsKey(netPath)){
			SoftReference<Bitmap> sr=imageCaches.get(netPath);
			imageBitmap=sr.get();
			if(imageBitmap!=null){
				return imageBitmap;
			}
			else
				imageCaches.remove(netPath);
		}
		else{
			Task taskInstance=new Task(netPath, null,imageCallback);
			if(!taskQuence.contains(taskInstance))
			{
				taskQuence.add(taskInstance);
				this.notify();
			}
		}
		return imageBitmap;
	}
	private ImageCallback getCallback(final ImageView imageView){
		
		return new ImageCallback() {
			
			@Override
			public void loadImage(String netPath, Bitmap imageBitmap) {
				// TODO Auto-generated method stub
				if(imageView.getTag().equals(netPath)){
					imageView.setImageBitmap(imageBitmap);
				}
			}
		};
	}
	
	Runnable runnable=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(taskQuence.size()>0){
				Task taskInstance=taskQuence.remove(0);
				if(taskInstance.execute()){
					Message msg=handler.obtainMessage();
					msg.obj=taskInstance;
					handler.sendMessage(msg);
				}
			}
			synchronized (this) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	Handler handler=new Handler(){
		public void handleMessage(Message msg) {
				if(msg.obj instanceof Task){
					Task completeTask=(Task)msg.obj;
					completeTask.imageCallback.loadImage(completeTask.netPath, completeTask.imageBitmap);
				}
			
		};
	};
	
	public interface ImageCallback{
		public void loadImage(String netPath,Bitmap imageBitmap);
	}
	
	
	
	class Task{
		private String netPath;
		private Bitmap imageBitmap;
		private ImageCallback imageCallback;
		
		public Task(String netPath,Bitmap imageBitmap,ImageCallback imageCallback){
			this.netPath=netPath;
			this.imageBitmap=imageBitmap;
			this.imageCallback=imageCallback;
		}
		@Override
		public boolean equals(Object o) {
			// TODO Auto-generated method stub
			Task task=(Task)o;
			return task.netPath.equals(netPath);
		}
		private boolean execute( ){
			boolean result=false;
			try {
				URL netUrl=new URL(netPath);
				HttpURLConnection urlConnection=(HttpURLConnection) netUrl.openConnection();
				InputStream is=urlConnection.getInputStream();
				imageBitmap=BitmapFactory.decodeStream(is);
				if(imageBitmap!=null)
					result=true;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		}
		
	}
	
	
	
	
}
