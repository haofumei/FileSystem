package ds;


import java.util.Queue;

public class File extends Sector {
    byte[] userData = new byte[504]; // User data bytes

    public File(int back, int forward) {
        super(back, forward);
    }

    public int length() {
        return userData.length;
    }

    public void writeData(int begin, int end, Queue<Character> chars) {
        int i = begin;
        for (; !chars.isEmpty() && i < end; i++) {
            userData[i] = (byte) chars.poll().charValue();
        }
        while (i < end) userData[i++] = (byte) ' ';
    }

    public void writeData(String[] data) {
        for (int i = 4, j = 0; i < data.length; i++, j++) {
            this.userData[j] = Byte.parseByte(data[i]);
        }
    }

    public String getData(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char) userData[i]);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('U');
        sb.append(' ');
        sb.append(getBack());
        sb.append(' ');
        sb.append(getForward());
        sb.append(' ');
        for (int i = 0; i < length(); i++) {
            sb.append(userData[i]);
            sb.append(' ');
        }
        return sb.toString();
    }
}
