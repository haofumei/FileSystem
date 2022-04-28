package ds;

public class Sector {
    int back; // Block number of previous block
    int forward; // Block number of successor block

    public Sector(int back, int forward) {
        this.back = back;
        this.forward = forward;
    }

    public int getBack() {
        return back;
    }

    public int getForward() {
        return forward;
    }

    public void setBack(int back) {
        this.back = back;
    }

    public void setForward(int forward) {
        this.forward = forward;
    }
}
