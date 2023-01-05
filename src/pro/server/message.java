package pro.server;



public class message {
    private String text;
    private int destination;
    private int sender;
    private boolean isCorrect=false;

    public message() {
        this.text="";
    }

    public message(String text, int destination, int sender) {
        this.text = text;
        this.destination = destination;
        this.sender = sender;
    }
    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }


    public void setText(String text) {
        this.text = text;
    }


    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "Message{" +
                "text='" + text + '\'' +
                ", destination=" + destination +
                ", sender=" + sender +
                '}';
    }
}
