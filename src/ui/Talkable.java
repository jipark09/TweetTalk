package ui;

import model.Message;
import model.User;

import java.io.ObjectOutputStream;

public interface Talkable {
    public void infoMessage(Message message);
    public void addMessage(Message message);
    public void whisper(Message message);
    public void whisperReturn(Message message, String whisperTo);

    public void whisperOn(User whisperTo);
    public void whisperOff();
    public ObjectOutputStream getOut();
}
