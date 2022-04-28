package ds;

import javax.print.DocFlavor;

public class Directory extends Sector {

    int free; // Block number of first unused block, only used in block 0.
    int filler; // Number of used sub dir.
    SubDir[] subDirs = new SubDir[31];

    public Directory(int back, int forward) {
        super(back, forward);
    }

    public Directory(int back, int forward, int free) {
        super(back, forward);
        this.free = free;
    }

    public int getFree() {
        return free;
    }

    public void setFree(int free) {
        this.free = free;
    }

    public int getFiller() {
        return filler;
    }

    public void setFiller(int filler) {
        this.filler = filler;
    }

    public SubDir[] getSubDirs() {
        return subDirs;
    }

    public void setSubDirs(SubDir[] subDirs) {
        this.subDirs = subDirs;
    }

    public SubDir getSubDirByIndex(int blockIndex) {
        /*
         * Get the sub dir based on its block index
         * Parameter: the block index of the sub dir
         * Return: SubDir or null
         */

        for (int i = 0; i < this.filler; i++) {
            if (blockIndex == this.subDirs[i].getLink()) {
                return this.subDirs[i];
            }
        }
        return null;
    }

    public SubDir getSubDirByName(String fileName) {
        /*
        * Get a single sub dir based on its name
        * Parameter: String fileName(name of sub dir)
        * Return: SubDir or null
         */
        for (int i = 0; i < this.filler; i++) {
            if (fileName.equals(this.subDirs[i].getName())) {
                return this.subDirs[i];
            }
        }
        return null;
    }

    public void addSubDir(SubDir subDir) {
        this.subDirs[this.filler++] = subDir;
    }

    public void updateSubDir(String fileName, SubDir subDir) {
        for (int i = 0; i < this.filler; i++) {
            if (fileName.equals(this.subDirs[i].getName())) this.subDirs[i] = subDir;
        }
    }

    public void printSubDirs() {
        for (int i = 0; i < this.filler; i++) {
            System.out.println(this.subDirs[i].getName() + "(" + subDirs[i].getType() + ")");
        }
    }

    public SubDir deleteSubDirByName(String fileName) {
        int i = 0;
        for (; i < this.filler; i++) {
            if (fileName.equals(this.subDirs[i].getName())) {
                SubDir temp = this.subDirs[i];
                this.subDirs[i] = this.subDirs[this.filler - 1];
                this.subDirs[--this.filler] = null;
                return temp;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        /*
         * D, back, forward, free, filler, subDirs(type, name, link, size)
         */
        String res = "";
        res += "D " + getBack() + " " + getForward() + " " + getFree() + " " + getFiller() + " ";
        for (int i = 0; i < getFiller(); i++) {
            SubDir sub = subDirs[i];
            res += sub.getType() + " " + sub.getName() + " " + sub.getLink() + " " + sub.getSize() + " ";
        }
        return res;
    }
}
