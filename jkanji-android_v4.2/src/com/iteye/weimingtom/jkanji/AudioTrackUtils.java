package com.iteye.weimingtom.jkanji;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.util.Log;

/**
 * @see http://www-mmsp.ece.mcgill.ca/documents/audioformats/wave/wave.html
 * @author Administrator
 *
 */
public class AudioTrackUtils {
	private static final boolean D = false;
    private static final String TAG = "AudioTrackUtils";
	
    private static final boolean READ_FULLY = true;
	private static final boolean SET_MARKER = false;
	
	private static final int SAMPLE_RATE_IN_HZ = 44100;
	private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	
    private static byte[] getWavDataChunk(InputStream in) throws IOException {
        byte[] id = new byte[4];
        byte[] intBytes = new byte[4];
        in.read(id);
        assert id[0] == 'R' && id[1] == 'I' && id[2] == 'F' && id[3] == 'F';
        in.read(intBytes);
        in.read(id);
        assert id[0] == 'W' && id[1] == 'A' && id[2] == 'V' && id[3] == 'E';
        in.read(id);
        assert id[0] == 'f' && id[1] == 'm' && id[2] == 't' && id[3] == ' ';
        in.read(intBytes);
        int fmtSize = 
        	((intBytes[0] & 0xff) << 0) | 
        	((intBytes[1] & 0xff) << 8) | 
        	((intBytes[2] & 0xff) << 16) | 
        	((intBytes[3] & 0xff) << 24);
        in.skip(fmtSize);
        
        in.read(id);
        if (id[0] == 'd' && id[1] == 'a' && id[2] == 't' && id[3] == 'a') {
            in.read(intBytes);
            int dataLength = 
            	((intBytes[0] & 0xff) << 0) | 
            	((intBytes[1] & 0xff) << 8) | 
            	((intBytes[2] & 0xff) << 16) | 
            	((intBytes[3] & 0xff) << 24);
            if (D) {
            	Log.d(TAG, "dataLength(1) = " + dataLength);
            }
            byte[] buffer = new byte[dataLength]; //in.available()
            if (READ_FULLY) {
                int pos = 0;
	            while (pos < dataLength) {
	            	int bytes = in.read(buffer, pos, dataLength - pos);
            		if (D) {
            			Log.d(TAG, "dataLength pos = " + pos + ", bytes = " + bytes);
            		}
	            	if (bytes < 0) {
	            		break;
	            	}
	            	if (pos + bytes < dataLength) {
	            		pos += bytes;
	            	} else {
	            		break;
	            	}
	            }
            } else {
            	in.read(buffer);
            }
            return buffer;
        } else if (id[0] == 'f' && id[1] == 'a' && id[2] == 'c' && id[3] == 't') {
            in.read(intBytes);
            int dataLength = 
            	((intBytes[0] & 0xff) << 0) | 
            	((intBytes[1] & 0xff) << 8) | 
            	((intBytes[2] & 0xff) << 16) | 
            	((intBytes[3] & 0xff) << 24);
            in.skip(dataLength);
            
            in.read(id);
            assert id[0] == 'd' && id[1] == 'a' && id[2] == 't' && id[3] == 'a';
            in.read(intBytes);
            dataLength = 
            	((intBytes[0] & 0xff) << 0) | 
            	((intBytes[1] & 0xff) << 8) | 
            	((intBytes[2] & 0xff) << 16) | 
            	((intBytes[3] & 0xff) << 24);
            if (D) {
            	Log.d(TAG, "dataLength(2) = " + dataLength);
            }
            byte[] buffer = new byte[dataLength]; //in.available()
            if (READ_FULLY) {
            	int pos = 0;
	            while (pos < dataLength) {
	            	int bytes = in.read(buffer, pos, dataLength - pos);
            		if (D) {
            			Log.d(TAG, "dataLength pos = " + pos + ", bytes = " + bytes);
            		}
	            	if (bytes < 0) {
	            		break;
	            	}
	            	if (pos + bytes < dataLength) {
	            		pos += bytes;
	            	} else {
	            		break;
	            	}
	            }
            } else {
            	in.read(buffer);
            }
    		if (D) {
    			Log.d(TAG, "dataLength return = " + buffer.length);
    		}
            return buffer;
        }
        return new byte[]{};
    }
    
	private static byte[] getWavDataFromZip(String zipfilename, String filename) {
    	InputStream in = null;
    	ZipFile zip = null;
    	try {
            zip = new ZipFile(zipfilename);
            ZipEntry entry = zip.getEntry(filename);
            if (entry != null) {
            	if (D) {
            		Log.d(TAG, "1 entry.getName:" + new String(entry.getName().getBytes("gb2312"), "utf8"));
            		Log.d(TAG, "2 entry.getName:" + new String(entry.getName().getBytes("utf8"), "gb2312"));
            		Log.d(TAG, "3 entry.getName:" + new String(entry.getName().getBytes("gbk"), "utf8"));
            		Log.d(TAG, "4 entry.getName:" + new String(entry.getName().getBytes("utf8"), "gbk"));
            		//Log.d(TAG, "entry.getCrc:" + Long.toHexString(entry.getCrc()));
            	}
            	in = zip.getInputStream(entry);
                return getWavDataChunk(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	if (in != null) {
        		try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        	if (zip != null) {
        		try {
					zip.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
        return new byte[]{};
    }

    private static byte[] getWavDataFromFile(String filename) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(filename);
            return getWavDataChunk(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	if (in != null) {
        		try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
        return new byte[]{};
    }
    
    public static AudioTrack playWav(AudioTrack audioTrack, String zipfilename, String filename, boolean isStream) {
    	byte[] buffer = null;
    	if (zipfilename == null) {
    		buffer = getWavDataFromFile(filename);
    	} else {
    		buffer = getWavDataFromZip(zipfilename, filename);
    	}
		if (D) {
			Log.d(TAG, "filename : " + filename);
			//Log.d(TAG, "dataLength return2 = " + buffer.length);
		}
    	if (audioTrack != null) {
    		audioTrack.release();
    		audioTrack = null;
    	}
    	if (buffer != null && buffer.length > 0) {
    		if (D) {
    			Log.d(TAG, "========" + buffer.length);
    		}
    		if (!isStream) {
	    		audioTrack = new AudioTrack(
		        		AudioManager.STREAM_MUSIC,
		        		SAMPLE_RATE_IN_HZ, 
		        		CHANNEL_CONFIG,
		        		AUDIO_FORMAT, 
		                buffer.length,
		                AudioTrack.MODE_STATIC);
	    		if (SET_MARKER) {
		    		audioTrack.setNotificationMarkerPosition(buffer.length);
		    		audioTrack.setPlaybackPositionUpdateListener(new OnPlaybackPositionUpdateListener() {
						@Override
						public void onMarkerReached(AudioTrack track) {
							
						}
	
						@Override
						public void onPeriodicNotification(AudioTrack track) {
					    	if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
						        track.stop();
					    	}
						}
		    		});
	    		}
	    		int pos = 0;
	    		int bytes = 0;
	    		while (pos < buffer.length) {
	    			bytes = audioTrack.write(buffer, pos, buffer.length - pos);
            		if (D) {
            			Log.d(TAG, "dataLength audioTrack pos = " + pos + ", bytes = " + bytes);
            		}
	    			if (bytes < 0) {
	    				Log.d(TAG, "=========bytes = " + bytes);
	    				break;
	    			}
	    			if (pos + bytes < buffer.length) {
	    				pos += bytes;
	    				if (D) {
	    					Log.d(TAG, "=========bytes = " + bytes);
	    				}
	    			} else {
	    				break;
	    			}
	    		}
		    	if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
			        audioTrack.play();
		    	}
    		} else {
                int bufSize = AudioTrack.getMinBufferSize(SAMPLE_RATE_IN_HZ,
                		CHANNEL_CONFIG,
                		AUDIO_FORMAT);
        		audioTrack = new AudioTrack(
    	        		AudioManager.STREAM_MUSIC,
    	        		SAMPLE_RATE_IN_HZ,
    	        		CHANNEL_CONFIG,
    	        		AUDIO_FORMAT, 
    	                bufSize,
    	                AudioTrack.MODE_STREAM);
        		if (SET_MARKER) {
		    		audioTrack.setNotificationMarkerPosition(buffer.length);
		    		audioTrack.setPlaybackPositionUpdateListener(new OnPlaybackPositionUpdateListener() {
						@Override
						public void onMarkerReached(AudioTrack track) {
							
						}
	
						@Override
						public void onPeriodicNotification(AudioTrack track) {
					    	if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
						        track.stop();
					    	}
						}
		    		});
        		}
    	    	if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
    	    		final AudioTrack audioTrack2 = audioTrack;
    	    		final byte[] buffer2 = buffer;
    	    		(new Thread() {
    	    			@Override
    	    			public void run() {
    			    		audioTrack2.play();
    			    		int pos = 0;
    			    		int bytes = 0;
    			    		while (pos < buffer2.length) {
    			    			bytes = audioTrack2.write(buffer2, pos, buffer2.length - pos);
    		            		if (D) {
    		            			Log.d(TAG, "dataLength audioTrack pos = " + pos + ", bytes = " + bytes);
    		            		}
    			    			if (bytes < 0) {
    			    				Log.d(TAG, "=========bytes = " + bytes);
    			    				break;
    			    			}
    			    			if (pos + bytes < buffer2.length) {
    			    				pos += bytes;
    			    				if (D) {
    			    					Log.d(TAG, "=========bytes = " + bytes);
    			    				}
    			    			} else {
    			    				break;
    			    			}
    			    		}
    			    		if (D) {
    			    			Log.d(TAG, "bytes = " + bytes + ", length = " + buffer2.length);
    			    		}
    			    	}
    	    		}).start();
    	    	}
    		}
    	}
    	return audioTrack;
    }
}
