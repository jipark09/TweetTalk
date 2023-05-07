package model;

import java.io.Serializable;

public class Message implements Serializable {
    private long time; // 식별자
    private User from;
    private String content;
    
//    public Message(User from) {
//    	this.from = from;
//    }
    public Message(User from, String content) {
    	time = System.currentTimeMillis();
		this.from = from;
		this.content = content;
	}
    
	public User getFrom() {
        return from;
    }
    public void setFrom(User from) {
        this.from = from;
    }

    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return content + "(" + from + ")" + ": " + time;
    }

    @Override
    public boolean equals(Object object) {
        if(object == null || !(object instanceof Message)) {
            return false;
        }
        Message msg = (Message)object;
        return time == msg.getTime();
    }
}
