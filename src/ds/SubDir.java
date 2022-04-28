package ds;

public class SubDir {
    byte type; // 'F' = free, 'D' ,=  directory, 'U' = user data.
    byte[] name = new byte[9]; // File name.
    int link; // Block number.
    short size; // Number of bytes used in the last block of file.

    public SubDir() {}
    public SubDir(char type, String name, int link, int size) {

        this.type = (byte) type;
        for (int i = 0; i < 9 && i < name.length(); i++) this.name[i] = (byte) name.charAt(i);
        this.link = link;
        this.size = (short) size;
    }

    public char getType() {
        return (char)type;
    }

    public String getName() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            if (name[i] != 0) sb.append((char) name[i]);
        }
        return sb.toString();
    }

    public int getLink() {
        return link;
    }

    public int getSize() {
        return (int) size;
    }

    public void setType(char type) {
        this.type = (byte) type;
    }

    public void setName(String name) {
        int i = 0;
        for (; i < name.length() && i < 9; i++) this.name[i] = (byte) name.charAt(i);
        while (i < 9) this.name[i++] = 0;
    }

    public void setLink(int link) {
        this.link = link;
    }

    public void setSize(int size) {
        this.size = (short) size;
    }

}

