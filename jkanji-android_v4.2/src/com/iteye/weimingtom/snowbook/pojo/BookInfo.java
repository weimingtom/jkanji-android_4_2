package com.iteye.weimingtom.snowbook.pojo;

import android.os.Parcel;
import android.os.Parcelable;

public class BookInfo implements Parcelable {
	public final static int BOOK_TYPE_DEFAULT = 0;
	public final static int BOOK_TYPE_SPACE = 1;

	private int bookType;
	private String bookName;
	private String coverImage;

	public BookInfo() {
		
	}
	
	public int getBookType() {
		return bookType;
	}

	public void setBookType(int bookType) {
		this.bookType = bookType;
	}

	public String getBookName() {
		return bookName;
	}
	
	public void setBookName(String bookName) {
		this.bookName = bookName;
	}
	
	public String getCoverImage() {
		return coverImage;
	}
	
	public void setCoverImage(String coverImage) {
		this.coverImage = coverImage;
	}

	public String getChapter_path(){
		return null;
	}
	
	public int describeContents() {
		return 0;
	}
	
	public static final Parcelable.Creator<BookInfo> CREATOR = new Parcelable.Creator<BookInfo>() {  
        public BookInfo createFromParcel(Parcel in) {  
            return new BookInfo(in);  
        }  
  
        public BookInfo[] newArray(int size) {  
            return new BookInfo[size];  
        }  
    }; 
    
    public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(bookType);
		dest.writeString(bookName);
		dest.writeString(coverImage);
	}
	
    private BookInfo(Parcel in) {
    	bookType = in.readInt();
    	bookName = in.readString();  
    	coverImage = in.readString();
    }
}
